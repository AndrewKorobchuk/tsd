package com.sh.an.tsd.data.api

import com.sh.an.tsd.data.model.TsdDevice
import com.sh.an.tsd.data.model.TsdDeviceRegisterRequest
import com.sh.an.tsd.data.model.TsdDeviceRegisterResponse
import com.sh.an.tsd.data.model.DocumentNumberRequest
import com.sh.an.tsd.data.model.DocumentNumberResponse
import retrofit2.Response
import retrofit2.http.*

interface TsdDeviceApiService {
    
    @POST("api/v1/tsd-devices/register")
    suspend fun registerDevice(
        @Header("Authorization") authorization: String,
        @Body request: TsdDeviceRegisterRequest
    ): Response<TsdDeviceRegisterResponse>
    
    @GET("api/v1/tsd-devices/me")
    suspend fun getMyDevice(
        @Header("Authorization") authorization: String
    ): Response<TsdDevice>
    
    @PUT("api/v1/tsd-devices/me")
    suspend fun updateDevice(
        @Header("Authorization") authorization: String,
        @Body request: TsdDeviceRegisterRequest
    ): Response<TsdDevice>
    
    @GET("api/v1/tsd-devices/")
    suspend fun getDevices(
        @Header("Authorization") authorization: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
        @Query("active_only") activeOnly: Boolean = true
    ): Response<List<TsdDevice>>
    
    @POST("api/v1/tsd-devices/next-document-number")
    suspend fun getNextDocumentNumber(
        @Header("Authorization") authorization: String,
        @Body request: DocumentNumberRequest
    ): Response<DocumentNumberResponse>
}

