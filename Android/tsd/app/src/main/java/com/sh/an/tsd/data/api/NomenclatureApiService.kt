package com.sh.an.tsd.data.api

import com.sh.an.tsd.data.model.NomenclatureResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NomenclatureApiService {
    
    @GET("api/nomenclature")
    suspend fun getNomenclature(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("search") search: String? = null,
        @Query("category") category: String? = null
    ): Response<NomenclatureResponse>
    
    @GET("api/nomenclature/{id}")
    suspend fun getNomenclatureById(
        @Query("id") id: String
    ): Response<NomenclatureResponse>
    
    @GET("api/nomenclature/search")
    suspend fun searchNomenclature(
        @Query("q") query: String,
        @Query("limit") limit: Int = 50
    ): Response<NomenclatureResponse>
}
