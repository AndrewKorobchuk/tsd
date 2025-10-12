package com.sh.an.tsd.data.api

import com.sh.an.tsd.data.model.Warehouse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WarehousesApiService {
    @GET("/api/v1/warehouses/")
    suspend fun getWarehouses(
        @Header("Authorization") authorization: String,
        @Query("skip") skip: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("active_only") activeOnly: Boolean? = null,
        @Query("search") search: String? = null
    ): Response<List<Warehouse>>
}
