package com.sh.an.tsd.data.factory

import com.sh.an.tsd.data.api.*
import com.sh.an.tsd.data.manager.SettingsManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiServiceFactory(private val settingsManager: SettingsManager) {
    
    private var _retrofit: Retrofit? = null
    
    private val retrofit: Retrofit
        get() {
            if (_retrofit == null) {
                val settings = settingsManager.getConnectionSettings()
                val baseUrl = settings.getFullUrl()
                
                println("ApiServiceFactory: Creating Retrofit with base URL: $baseUrl")
                
                if (baseUrl.isBlank()) {
                    throw IllegalStateException("Server URL is not configured. Please set up connection settings first.")
                }
                
                _retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return _retrofit!!
        }
    
    fun createAuthApiService(): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    fun createUnitsApiService(): UnitsApiService {
        return retrofit.create(UnitsApiService::class.java)
    }
    
    fun createNomenclatureCategoriesApiService(): NomenclatureCategoriesApiService {
        return retrofit.create(NomenclatureCategoriesApiService::class.java)
    }
    
    fun createNomenclatureApiService(): NomenclatureApiService {
        return retrofit.create(NomenclatureApiService::class.java)
    }
    
    fun createWarehousesApiService(): WarehousesApiService {
        return retrofit.create(WarehousesApiService::class.java)
    }
    
    fun createDocumentsApiService(): DocumentsApiService {
        return retrofit.create(DocumentsApiService::class.java)
    }
    
    fun createBarcodesApiService(): BarcodesApiService {
        return retrofit.create(BarcodesApiService::class.java)
    }
    
    fun createTsdDeviceApiService(): TsdDeviceApiService {
        return retrofit.create(TsdDeviceApiService::class.java)
    }
    
    // Метод для сброса кэша retrofit (например, при изменении настроек)
    fun reset() {
        println("ApiServiceFactory: Resetting Retrofit cache")
        _retrofit = null
    }
}
