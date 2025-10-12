package com.sh.an.tsd.data.repository

import com.sh.an.tsd.data.api.WarehousesApiService
import com.sh.an.tsd.data.database.WarehouseDao
import com.sh.an.tsd.data.model.Warehouse
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class WarehousesRepository(
    private val warehousesApiService: WarehousesApiService,
    private val warehouseDao: WarehouseDao
) {

    val allActiveWarehouses: Flow<List<Warehouse>> = warehouseDao.getAllActiveWarehouses()

    fun searchActiveWarehouses(query: String): Flow<List<Warehouse>> {
        return warehouseDao.searchActiveWarehouses(query)
    }

    suspend fun syncWarehousesFromServer(token: String): Result<Unit> {
        return try {
            val response = warehousesApiService.getWarehouses(authorization = token)
            if (response.isSuccessful) {
                val warehouses = response.body()
                if (warehouses != null) {
                    warehouseDao.deleteAllWarehouses() // Очищаем старые данные
                    warehouseDao.insertWarehouses(warehouses) // Вставляем новые
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Empty response body from server"))
                }
            } else {
                Result.failure(IOException("Failed to fetch warehouses: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLocalWarehousesCount(): Int {
        return warehouseDao.getWarehousesCount()
    }

    suspend fun getLastSyncTime(): String? {
        return warehouseDao.getLastUpdateTime()
    }
}
