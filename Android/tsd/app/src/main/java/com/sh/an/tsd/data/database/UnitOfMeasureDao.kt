package com.sh.an.tsd.data.database

import androidx.room.*
import com.sh.an.tsd.data.model.UnitOfMeasure
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitOfMeasureDao {

    @Query("SELECT * FROM units_of_measure WHERE is_active = 1 ORDER BY name")
    fun getAllActiveUnits(): Flow<List<UnitOfMeasure>>

    @Query("SELECT * FROM units_of_measure WHERE is_active = 1 AND (name LIKE '%' || :search || '%' OR code LIKE '%' || :search || '%' OR short_name LIKE '%' || :search || '%') ORDER BY name")
    fun searchActiveUnits(search: String): Flow<List<UnitOfMeasure>>

    @Query("SELECT * FROM units_of_measure WHERE id = :id")
    suspend fun getUnitById(id: Int): UnitOfMeasure?

    @Query("SELECT * FROM units_of_measure WHERE code = :code")
    suspend fun getUnitByCode(code: String): UnitOfMeasure?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitOfMeasure>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitOfMeasure)

    @Update
    suspend fun updateUnit(unit: UnitOfMeasure)

    @Delete
    suspend fun deleteUnit(unit: UnitOfMeasure)

    @Query("DELETE FROM units_of_measure")
    suspend fun deleteAllUnits()

    @Query("SELECT COUNT(*) FROM units_of_measure")
    suspend fun getUnitsCount(): Int

    @Query("SELECT MAX(updated_at) FROM units_of_measure")
    suspend fun getLastUpdateTime(): String?
}
