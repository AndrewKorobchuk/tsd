package com.sh.an.tsd.data.repository

import com.sh.an.tsd.data.api.NomenclatureApiService
import com.sh.an.tsd.data.database.NomenclatureDao
import com.sh.an.tsd.data.model.Nomenclature
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class NomenclatureRepository(
    private val nomenclatureApiService: NomenclatureApiService,
    private val nomenclatureDao: NomenclatureDao
) {

    val allActiveNomenclature: Flow<List<Nomenclature>> = nomenclatureDao.getAllActiveNomenclature()

    fun getActiveNomenclatureByCategory(categoryId: Int): Flow<List<Nomenclature>> {
        return nomenclatureDao.getActiveNomenclatureByCategory(categoryId)
    }

    fun searchActiveNomenclature(query: String): Flow<List<Nomenclature>> {
        return nomenclatureDao.searchActiveNomenclature(query)
    }

    fun searchActiveNomenclatureByCategory(categoryId: Int, query: String): Flow<List<Nomenclature>> {
        return nomenclatureDao.searchActiveNomenclatureByCategory(categoryId, query)
    }

    suspend fun syncNomenclatureFromServer(token: String): Result<Unit> {
        return try {
            val response = nomenclatureApiService.getNomenclature(authorization = token)
            if (response.isSuccessful) {
                val nomenclature = response.body()
                if (nomenclature != null) {
                    nomenclatureDao.deleteAllNomenclature() // Очищаем старые данные
                    nomenclatureDao.insertNomenclature(nomenclature) // Вставляем новые
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Empty response body from server"))
                }
            } else {
                Result.failure(IOException("Failed to fetch nomenclature: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLocalNomenclatureCount(): Int {
        return nomenclatureDao.getNomenclatureCount()
    }

    suspend fun getNomenclatureCountByCategory(categoryId: Int): Int {
        return nomenclatureDao.getNomenclatureCountByCategory(categoryId)
    }

    suspend fun getLastSyncTime(): String? {
        return nomenclatureDao.getLastUpdateTime()
    }
}