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
            val clientData = OAuthClientCreate(
                clientName = "TSD Mobile App",
                redirectUris = listOf("http://localhost:3000/callback"),
                scope = "read write"
            )
            
            val response = getAuthApiService().registerOAuthClient(clientData)
            if (response.isSuccessful) {
                val client = response.body()!!
                settingsManager.saveOAuthClient(client)
                Result.success(client)
            } else {
                Result.failure(Exception("Failed to register OAuth client: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(username: String, password: String): Result<User> {
        return try {
            // Проверяем, есть ли OAuth клиент
            val clientId = settingsManager.getOAuthClientId()
            val clientSecret = settingsManager.getOAuthClientSecret()
            
            if (clientId.isEmpty()) {
                // Регистрируем OAuth клиента, если его нет
                val clientResult = registerOAuthClient()
                if (clientResult.isFailure) {
                    return Result.failure(clientResult.exceptionOrNull()!!)
                }
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

