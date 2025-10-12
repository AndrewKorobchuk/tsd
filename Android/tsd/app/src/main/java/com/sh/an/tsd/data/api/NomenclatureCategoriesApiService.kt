package com.sh.an.tsd.data.api

import com.sh.an.tsd.data.model.NomenclatureCategory
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NomenclatureCategoriesApiService {
    @GET("/api/v1/nomenclature-categories/")
    suspend fun getNomenclatureCategories(
        @Header("Authorization") authorization: String,
        @Query("skip") skip: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("active_only") activeOnly: Boolean? = null,
        @Query("search") search: String? = null
    ): Response<List<NomenclatureCategory>>
}
