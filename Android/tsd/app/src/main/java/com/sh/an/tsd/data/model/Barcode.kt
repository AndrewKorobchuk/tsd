package com.sh.an.tsd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "barcodes")
data class Barcode(
    @PrimaryKey
    val id: Int = 0,
    
    val barcode: String,
    
    @SerializedName("nomenclature_id")
    val nomenclatureId: Int,
    
    @SerializedName("unit_id")
    val unitId: Int,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    // Дополнительные поля для отображения
    @SerializedName("nomenclature_name")
    val nomenclatureName: String? = null,
    
    @SerializedName("unit_name")
    val unitName: String? = null,
    
    @SerializedName("unit_short_name")
    val unitShortName: String? = null
)

// Запрос на создание штрих-кода
data class BarcodeCreateRequest(
    val barcode: String,
    @SerializedName("nomenclature_id")
    val nomenclatureId: Int,
    @SerializedName("unit_id")
    val unitId: Int,
    @SerializedName("is_active")
    val isActive: Boolean = true
)

// Запрос на обновление штрих-кода
data class BarcodeUpdateRequest(
    @SerializedName("nomenclature_id")
    val nomenclatureId: Int? = null,
    @SerializedName("unit_id")
    val unitId: Int? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null
)

// Запрос на поиск штрих-кодов
data class BarcodeSearchRequest(
    val query: String,
    val limit: Int = 10
)
