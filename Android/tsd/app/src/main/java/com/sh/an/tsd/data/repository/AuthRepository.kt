package com.sh.an.tsd.data.repository

import com.sh.an.tsd.data.api.AuthApiService
import com.sh.an.tsd.data.api.UnitsApiService
import com.sh.an.tsd.data.api.NomenclatureCategoriesApiService
import com.sh.an.tsd.data.api.NomenclatureApiService
import com.sh.an.tsd.data.api.WarehousesApiService
import com.sh.an.tsd.data.api.DocumentsApiService
import com.sh.an.tsd.data.manager.SettingsManager
import com.sh.an.tsd.data.factory.ApiServiceFactory
import com.sh.an.tsd.data.model.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class AuthRepository(private val settingsManager: SettingsManager) {
    
    private val apiServiceFactory = ApiServiceFactory(settingsManager)
    
    private fun getAuthApiService(): AuthApiService {
        return apiServiceFactory.createAuthApiService()
    }
    
    
    suspend fun registerOAuthClient(): Result<OAuthClientResponse> {
        return try {
            val settings = settingsManager.getConnectionSettings()
            val baseUrl = settings.getFullUrl()
            println("AuthRepository: Attempting to register OAuth client with URL: $baseUrl")
            
            val clientData = OAuthClientCreate(
                clientName = "TSD Mobile App",
                redirectUris = listOf("http://localhost:8001/callback"),
                scope = "read write"
            )
            
            val response = getAuthApiService().registerOAuthClient(clientData)
            println("AuthRepository: OAuth registration response code: ${response.code()}")
            if (response.isSuccessful) {
                val client = response.body()!!
                settingsManager.saveOAuthClient(client)
                println("AuthRepository: OAuth client registered successfully")
                Result.success(client)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                println("AuthRepository: OAuth registration failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to register OAuth client: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            println("AuthRepository: OAuth registration exception: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun login(username: String, password: String): Result<User> {
        return try {
            // Проверяем, есть ли OAuth клиент
            val clientId = settingsManager.getOAuthClientId()
            val clientSecret = settingsManager.getOAuthClientSecret()
            
            println("AuthRepository: Login attempt - clientId: '$clientId', clientSecret: '${clientSecret.take(10)}...'")
            
            if (clientId.isEmpty()) {
                println("AuthRepository: No OAuth client found, registering new one")
                // Регистрируем OAuth клиента, если его нет
                val clientResult = registerOAuthClient()
                if (clientResult.isFailure) {
                    return Result.failure(clientResult.exceptionOrNull()!!)
                }
            } else {
                println("AuthRepository: Using existing OAuth client")
            }
            
            // Получаем токен
            val tokenResponse = getAuthApiService().getToken(
                grantType = "password",
                clientId = settingsManager.getOAuthClientId(),
                clientSecret = settingsManager.getOAuthClientSecret(),
                username = username,
                password = password,
                scope = "read write"
            )
            
            if (tokenResponse.isSuccessful) {
                val authResponse = tokenResponse.body()!!
                
                // Сохраняем токен
                settingsManager.saveAuthData(
                    authResponse.accessToken,
                    authResponse.refreshToken,
                    authResponse.expiresIn
                )
                
                // Получаем информацию о пользователе
                val userResponse = getAuthApiService().getCurrentUser(
                    "Bearer ${authResponse.accessToken}"
                )
                
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()!!
                    settingsManager.saveUserData(user.id, user.username, user.email)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Failed to get user info: ${userResponse.code()}"))
                }
            } else {
                Result.failure(Exception("Login failed: ${tokenResponse.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): Result<User> {
        return try {
            if (!settingsManager.isTokenValid()) {
                return Result.failure(Exception("Token expired"))
            }
            
            val token = settingsManager.getAccessToken()
            val response = getAuthApiService().getCurrentUser("Bearer $token")
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get user info: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
        fun logout() {
            settingsManager.clearAuthData()
        }
    
    fun isLoggedIn(): Boolean {
        return settingsManager.isLoggedIn()
    }
    
    fun hasConnectionSettings(): Boolean {
        val settings = settingsManager.getConnectionSettings()
        return settings.serverUrl.trim().isNotEmpty() && settings.port.trim().isNotEmpty()
    }
    
    fun resetApiServiceFactory() {
        apiServiceFactory.reset()
    }
    
    fun getCurrentUserData(): Triple<Int, String, String> {
        return settingsManager.getUserData()
    }
    
        fun createUnitsApiService(): UnitsApiService {
            return apiServiceFactory.createUnitsApiService()
        }
    
        fun getAccessToken(): String {
            return settingsManager.getAccessToken()
        }
        
        fun createNomenclatureCategoriesApiService(): NomenclatureCategoriesApiService {
            return apiServiceFactory.createNomenclatureCategoriesApiService()
        }
        
        fun createNomenclatureApiService(): NomenclatureApiService {
            return apiServiceFactory.createNomenclatureApiService()
        }
        
        fun createWarehousesApiService(): WarehousesApiService {
            return apiServiceFactory.createWarehousesApiService()
        }
        
        fun createDocumentsApiService(): DocumentsApiService {
            return apiServiceFactory.createDocumentsApiService()
        }
        
        // Метод для сброса API сервисов при изменении настроек
        fun resetApiServices() {
            apiServiceFactory.reset()
        }
}

