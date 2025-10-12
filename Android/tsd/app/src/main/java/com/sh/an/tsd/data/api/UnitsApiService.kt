package com.sh.an.tsd.data.api

import com.sh.an.tsd.data.model.UnitOfMeasure
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UnitsApiService {

    @GET("/api/v1/units/")
    suspend fun getUnitsOfMeasure(
        @Header("Authorization") authorization: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 1000,
        @Query("active_only") activeOnly: Boolean = true,
        @Query("search") search: String? = null
    ): Response<List<UnitOfMeasure>>

    @GET("/api/v1/units/{id}")
    suspend fun getUnitOfMeasure(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Path("id") id: Int
    ): Response<UnitOfMeasure>

    @GET("/api/v1/units/code/{code}")
    suspend fun getUnitByCode(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Path("code") code: String
    ): Response<UnitOfMeasure>
}
