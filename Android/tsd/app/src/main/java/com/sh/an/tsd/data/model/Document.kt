package com.sh.an.tsd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import kotlin.properties.Delegates

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "document_type")
    @SerializedName("document_type")
    val documentType: String,
    @ColumnInfo(name = "document_number")
    @SerializedName("document_number")
    val documentNumber: String,
    @ColumnInfo(name = "warehouse_id")
    @SerializedName("warehouse_id")
    val warehouseId: Int,
    val date: String,
    val status: String,
    @ColumnInfo(name = "created_by")
    @SerializedName("created_by")
    val createdBy: Int,
    val description: String?,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: String?
) {
    // Связанные объекты (не сохраняются в БД)
    @Ignore
    var warehouse: Warehouse? = null
    
    @Ignore
    var creator: User? = null
    
    @Ignore
    var items: List<DocumentItem>? = null
}

// Enum для типов документов
enum class DocumentType(val value: String) {
    RECEIPT("receipt"),
    EXPENSE("expense"),
    TRANSFER("transfer"),
    INVENTORY("inventory"),
    STOCK_INPUT("stock_input");
    
    companion object {
        fun fromString(value: String): DocumentType {
            return values().find { it.value == value } ?: RECEIPT
        }
    }
}

// Enum для статусов документов
enum class DocumentStatus(val value: String) {
    DRAFT("draft"),
    POSTED("posted"),
    CANCELLED("cancelled");
    
    companion object {
        fun fromString(value: String): DocumentStatus {
            return values().find { it.value == value } ?: DRAFT
        }
    }
}
