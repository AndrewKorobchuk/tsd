package com.sh.an.tsd.data.repository

import android.content.Context
import android.os.Build
import com.sh.an.tsd.data.api.TsdDeviceApiService
import com.sh.an.tsd.data.factory.ApiServiceFactory
import com.sh.an.tsd.data.manager.SettingsManager
import com.sh.an.tsd.data.model.TsdDevice
import com.sh.an.tsd.data.model.TsdDeviceRegisterRequest
import com.sh.an.tsd.data.model.TsdDeviceRegisterResponse
import com.sh.an.tsd.data.model.DocumentNumberRequest
import com.sh.an.tsd.data.model.DocumentNumberResponse
import java.util.UUID

class TsdDeviceRepository(
    private val context: Context,
    private val settingsManager: SettingsManager,
    private val apiServiceFactory: ApiServiceFactory
) {
    
    private fun getTsdDeviceApiService(): TsdDeviceApiService {
        return apiServiceFactory.createTsdDeviceApiService()
    }
    
    /**
     * Получает уникальный ID устройства
     */
    fun getDeviceId(): String {
        var deviceId = settingsManager.getDeviceId()
        if (deviceId.isEmpty()) {
            // Генерируем уникальный ID на основе Android ID или создаем новый
            deviceId = generateDeviceId()
            // Сохраняем в настройки
            settingsManager.saveDeviceInfo(
                deviceId = deviceId,
                prefix = "",
                deviceName = getDeviceName(),
                deviceModel = getDeviceModel()
            )
        }
        return deviceId
    }
    
    /**
     * Генерирует уникальный ID устройства
     */
    private fun generateDeviceId(): String {
        val androidId = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        return if (androidId != null && androidId != "9774d56d682e549c") {
            "TSD_${androidId}"
        } else {
            "TSD_${UUID.randomUUID().toString().replace("-", "").substring(0, 16)}"
        }
    }
    
    /**
     * Получает название устройства
     */
    private fun getDeviceName(): String {
        return Build.MANUFACTURER + " " + Build.MODEL
    }
    
    /**
     * Получает модель устройства
     */
    private fun getDeviceModel(): String {
        return Build.MODEL
    }
    
    /**
     * Получает версию Android
     */
    private fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }
    
    /**
     * Получает версию приложения
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    /**
     * Регистрирует устройство на сервере
     */
    suspend fun registerDevice(): Result<TsdDeviceRegisterResponse> {
        return try {
            val deviceId = getDeviceId()
            val request = TsdDeviceRegisterRequest(
                deviceId = deviceId,
                deviceName = getDeviceName(),
                deviceModel = getDeviceModel(),
                androidVersion = getAndroidVersion(),
                appVersion = getAppVersion()
            )
            
            val response = getTsdDeviceApiService().registerDevice(
                authorization = "Bearer ${settingsManager.getAccessToken()}",
                request = request
            )
            
            if (response.isSuccessful) {
                val result = response.body()!!
                // Сохраняем полученный префикс
                settingsManager.saveDeviceInfo(
                    deviceId = result.deviceId,
                    prefix = result.prefix,
                    deviceName = getDeviceName(),
                    deviceModel = getDeviceModel()
                )
                Result.success(result)
            } else {
                Result.failure(Exception("Failed to register device: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получает информацию о текущем устройстве
     */
    suspend fun getMyDevice(): Result<TsdDevice> {
        return try {
            val response = getTsdDeviceApiService().getMyDevice(
                authorization = "Bearer ${settingsManager.getAccessToken()}"
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get device info: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Проверяет, зарегистрировано ли устройство
     */
    fun isDeviceRegistered(): Boolean {
        return settingsManager.isDeviceRegistered() && settingsManager.getDevicePrefix().isNotEmpty()
    }
    
    /**
     * Получает префикс устройства
     */
    fun getDevicePrefix(): String {
        return settingsManager.getDevicePrefix()
    }
    
    /**
     * Инициализирует устройство при запуске приложения
     */
    suspend fun initializeDevice(): Result<String> {
        return try {
            if (!isDeviceRegistered()) {
                // Устройство не зарегистрировано, регистрируем
                val result = registerDevice()
                if (result.isSuccess) {
                    Result.success(result.getOrNull()?.prefix ?: "")
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            } else {
                // Устройство уже зарегистрировано, возвращаем префикс
                Result.success(getDevicePrefix())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получает следующий номер документа для ТСД устройства
     */
    suspend fun getNextDocumentNumber(documentType: String = "input_balance"): Result<DocumentNumberResponse> {
        return try {
            val deviceId = getDeviceId()
            val request = DocumentNumberRequest(
                deviceId = deviceId,
                documentType = documentType
            )
            
            val response = getTsdDeviceApiService().getNextDocumentNumber(
                authorization = "Bearer ${settingsManager.getAccessToken()}",
                request = request
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get next document number: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

