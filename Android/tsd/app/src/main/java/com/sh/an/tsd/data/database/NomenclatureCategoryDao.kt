package com.sh.an.tsd.data.database

import androidx.room.*
import com.sh.an.tsd.data.model.NomenclatureCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface NomenclatureCategoryDao {

    @Query("SELECT * FROM nomenclature_categories WHERE is_active = 1 ORDER BY name")
    fun getAllActiveCategories(): Flow<List<NomenclatureCategory>>

    @Query("SELECT * FROM nomenclature_categories WHERE is_active = 1 AND (name LIKE '%' || :search || '%' OR code LIKE '%' || :search || '%') ORDER BY name")
    fun searchActiveCategories(search: String): Flow<List<NomenclatureCategory>>

    @Query("SELECT * FROM nomenclature_categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): NomenclatureCategory?

    @Query("SELECT * FROM nomenclature_categories WHERE code = :code")
    suspend fun getCategoryByCode(code: String): NomenclatureCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<NomenclatureCategory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: NomenclatureCategory)

    @Update
    suspend fun updateCategory(category: NomenclatureCategory)

    @Delete
    suspend fun deleteCategory(category: NomenclatureCategory)

    @Query("DELETE FROM nomenclature_categories")
    suspend fun deleteAllCategories()

    @Query("SELECT COUNT(*) FROM nomenclature_categories")
    suspend fun getCategoriesCount(): Int

    @Query("SELECT MAX(updated_at) FROM nomenclature_categories")
    suspend fun getLastUpdateTime(): String?
}
