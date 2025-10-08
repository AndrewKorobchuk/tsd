package com.sh.an.tsd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nomenclature")
data class Nomenclature(
    @PrimaryKey
    val id: String,
    val code: String,
    val name: String,
    val unit: String,
    val description: String? = null,
    val category: String? = null,
    val barcode: String? = null,
    val price: Double? = null,
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

// DTO для API ответа
data class NomenclatureResponse(
    val success: Boolean,
    val data: List<NomenclatureItem>,
    val message: String? = null
)

data class NomenclatureItem(
    val id: String,
    val code: String,
    val name: String,
    val unit: String,
    val description: String? = null,
    val category: String? = null,
    val barcode: String? = null,
    val price: Double? = null,
    val isActive: Boolean = true
)

// Функция для конвертации API ответа в модель
fun NomenclatureItem.toNomenclature(): Nomenclature {
    return Nomenclature(
        id = this.id,
        code = this.code,
        name = this.name,
        unit = this.unit,
        description = this.description,
        category = this.category,
        barcode = this.barcode,
        price = this.price,
        isActive = this.isActive,
        lastUpdated = System.currentTimeMillis()
    )
}
