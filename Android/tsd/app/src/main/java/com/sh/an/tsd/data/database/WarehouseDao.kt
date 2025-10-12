package com.sh.an.tsd.data.database

import androidx.room.*
import com.sh.an.tsd.data.model.Warehouse
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {

    @Query("SELECT * FROM warehouses WHERE is_active = 1 ORDER BY name")
    fun getAllActiveWarehouses(): Flow<List<Warehouse>>

    @Query("SELECT * FROM warehouses WHERE is_active = 1 AND (name LIKE '%' || :search || '%' OR code LIKE '%' || :search || '%' OR address LIKE '%' || :search || '%') ORDER BY name")
    fun searchActiveWarehouses(search: String): Flow<List<Warehouse>>

    @Query("SELECT * FROM warehouses WHERE id = :id")
    suspend fun getWarehouseById(id: Int): Warehouse?

    @Query("SELECT * FROM warehouses WHERE code = :code")
    suspend fun getWarehouseByCode(code: String): Warehouse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouses(warehouses: List<Warehouse>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: Warehouse)

    @Update
    suspend fun updateWarehouse(warehouse: Warehouse)

    @Delete
    suspend fun deleteWarehouse(warehouse: Warehouse)

    @Query("DELETE FROM warehouses")
    suspend fun deleteAllWarehouses()

    @Query("SELECT COUNT(*) FROM warehouses")
    suspend fun getWarehousesCount(): Int

    @Query("SELECT MAX(updated_at) FROM warehouses")
    suspend fun getLastUpdateTime(): String?
}
