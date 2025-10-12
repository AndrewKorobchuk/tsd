package com.sh.an.tsd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.google.gson.annotations.SerializedName

@Entity(tableName = "nomenclature")
data class Nomenclature(
    @PrimaryKey
    val id: Int,
    val code: String,
    @ColumnInfo(name = "category_id")
    @SerializedName("category_id")
    val categoryId: Int,
    val name: String,
    @ColumnInfo(name = "base_unit_id")
    @SerializedName("base_unit_id")
    val baseUnitId: Int,
    @ColumnInfo(name = "description_ru")
    @SerializedName("description_ru")
    val descriptionRu: String?,
    @ColumnInfo(name = "description_ua")
    @SerializedName("description_ua")
    val descriptionUa: String?,
    @ColumnInfo(name = "is_active")
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    // Связанные данные (не сохраняются в БД, загружаются отдельно)
    @Ignore
    var category: NomenclatureCategory? = null
    
    @Ignore
    var baseUnit: UnitOfMeasure? = null
    
    // Конструктор для создания объекта с связанными данными
    constructor(
        id: Int,
        code: String,
        categoryId: Int,
        name: String,
        baseUnitId: Int,
        descriptionRu: String?,
        descriptionUa: String?,
        isActive: Boolean,
        createdAt: String,
        updatedAt: String?,
        category: NomenclatureCategory?,
        baseUnit: UnitOfMeasure?
    ) : this(id, code, categoryId, name, baseUnitId, descriptionRu, descriptionUa, isActive, createdAt, updatedAt) {
        this.category = category
        this.baseUnit = baseUnit
    }
}