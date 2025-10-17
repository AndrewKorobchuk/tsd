package com.sh.an.tsd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.google.gson.annotations.SerializedName

@Entity(tableName = "document_items")
data class DocumentItem(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "document_id")
    @SerializedName("document_id")
    val documentId: Int,
    @ColumnInfo(name = "nomenclature_id")
    @SerializedName("nomenclature_id")
    val nomenclatureId: Int,
    val quantity: Double,
    @ColumnInfo(name = "unit_id")
    @SerializedName("unit_id")
    val unitId: Int,
    val price: Double?,
    val total: Double?,
    val description: String?,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String?,
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: String?
) {
    // Связанные объекты (не сохраняются в БД)
    @Ignore
    var nomenclature: Nomenclature? = null
    
    @Ignore
    var unit: UnitOfMeasure? = null
}

