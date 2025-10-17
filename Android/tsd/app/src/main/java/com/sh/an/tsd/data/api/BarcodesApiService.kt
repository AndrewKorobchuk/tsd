package com.sh.an.tsd.data.api

import com.sh.an.tsd.data.model.Barcode
import com.sh.an.tsd.data.model.BarcodeCreateRequest
import com.sh.an.tsd.data.model.BarcodeUpdateRequest
import com.sh.an.tsd.data.model.BarcodeSearchRequest
import retrofit2.Response
import retrofit2.http.*

interface BarcodesApiService {
    
    @GET("api/v1/barcodes/")
    suspend fun getBarcodes(
        @Header("Authorization") authorization: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
        @Query("active_only") activeOnly: Boolean = true
    ): Response<List<Barcode>>
    
    @GET("api/v1/barcodes/search")
    suspend fun searchBarcodes(
        @Header("Authorization") authorization: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): Response<List<Barcode>>
    
    @GET("api/v1/barcodes/{barcode_id}")
    suspend fun getBarcode(
        @Header("Authorization") authorization: String,
        @Path("barcode_id") barcodeId: Int
    ): Response<Barcode>
    
    @POST("api/v1/barcodes/")
    suspend fun createBarcode(
        @Header("Authorization") authorization: String,
        @Body request: BarcodeCreateRequest
    ): Response<Barcode>
    
    @PUT("api/v1/barcodes/{barcode_id}")
    suspend fun updateBarcode(
        @Header("Authorization") authorization: String,
        @Path("barcode_id") barcodeId: Int,
        @Body request: BarcodeUpdateRequest
    ): Response<Barcode>
    
    @DELETE("api/v1/barcodes/{barcode_id}")
    suspend fun deleteBarcode(
        @Header("Authorization") authorization: String,
        @Path("barcode_id") barcodeId: Int
    ): Response<Unit>
}
