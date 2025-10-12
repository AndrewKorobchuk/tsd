package com.sh.an.tsd.data.repository

import com.sh.an.tsd.data.api.NomenclatureCategoriesApiService
import com.sh.an.tsd.data.database.NomenclatureCategoryDao
import com.sh.an.tsd.data.model.NomenclatureCategory
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class NomenclatureCategoriesRepository(
    private val nomenclatureCategoriesApiService: NomenclatureCategoriesApiService,
    private val nomenclatureCategoryDao: NomenclatureCategoryDao
) {

    val allActiveCategories: Flow<List<NomenclatureCategory>> = nomenclatureCategoryDao.getAllActiveCategories()

    fun searchActiveCategories(query: String): Flow<List<NomenclatureCategory>> {
        return nomenclatureCategoryDao.searchActiveCategories(query)
    }

    suspend fun syncCategoriesFromServer(token: String): Result<Unit> {
        return try {
            val response = nomenclatureCategoriesApiService.getNomenclatureCategories(authorization = token)
            if (response.isSuccessful) {
                val categories = response.body()
                if (categories != null) {
                    nomenclatureCategoryDao.deleteAllCategories() // Очищаем старые данные
                    nomenclatureCategoryDao.insertCategories(categories) // Вставляем новые
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Empty response body from server"))
                }
            } else {
                Result.failure(IOException("Failed to fetch categories: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLocalCategoriesCount(): Int {
        return nomenclatureCategoryDao.getCategoriesCount()
    }

    suspend fun getLastSyncTime(): String? {
        return nomenclatureCategoryDao.getLastUpdateTime()
    }
}
