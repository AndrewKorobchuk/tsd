package com.sh.an.tsd.data.database

import androidx.room.*
import com.sh.an.tsd.data.model.Nomenclature
import kotlinx.coroutines.flow.Flow

@Dao
interface NomenclatureDao {

    @Query("SELECT * FROM nomenclature WHERE is_active = 1 ORDER BY name")
    fun getAllActiveNomenclature(): Flow<List<Nomenclature>>

    @Query("SELECT * FROM nomenclature WHERE is_active = 1 AND category_id = :categoryId ORDER BY name")
    fun getActiveNomenclatureByCategory(categoryId: Int): Flow<List<Nomenclature>>

    @Query("SELECT * FROM nomenclature WHERE is_active = 1 AND (name LIKE '%' || :search || '%' OR code LIKE '%' || :search || '%' OR description_ru LIKE '%' || :search || '%' OR description_ua LIKE '%' || :search || '%') ORDER BY name")
    fun searchActiveNomenclature(search: String): Flow<List<Nomenclature>>

    @Query("SELECT * FROM nomenclature WHERE is_active = 1 AND category_id = :categoryId AND (name LIKE '%' || :search || '%' OR code LIKE '%' || :search || '%' OR description_ru LIKE '%' || :search || '%' OR description_ua LIKE '%' || :search || '%') ORDER BY name")
    fun searchActiveNomenclatureByCategory(categoryId: Int, search: String): Flow<List<Nomenclature>>

    @Query("SELECT * FROM nomenclature WHERE id = :id")
    suspend fun getNomenclatureById(id: Int): Nomenclature?

    @Query("SELECT * FROM nomenclature WHERE code = :code")
    suspend fun getNomenclatureByCode(code: String): Nomenclature?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNomenclature(nomenclature: List<Nomenclature>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNomenclatureItem(nomenclature: Nomenclature)

    @Update
    suspend fun updateNomenclature(nomenclature: Nomenclature)

    @Delete
    suspend fun deleteNomenclature(nomenclature: Nomenclature)

    @Query("DELETE FROM nomenclature")
    suspend fun deleteAllNomenclature()

    @Query("SELECT COUNT(*) FROM nomenclature")
    suspend fun getNomenclatureCount(): Int

    @Query("SELECT COUNT(*) FROM nomenclature WHERE category_id = :categoryId")
    suspend fun getNomenclatureCountByCategory(categoryId: Int): Int

    @Query("SELECT MAX(updated_at) FROM nomenclature")
    suspend fun getLastUpdateTime(): String?
}
