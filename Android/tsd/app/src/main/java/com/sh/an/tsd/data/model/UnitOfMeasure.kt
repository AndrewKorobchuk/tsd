package com.sh.an.tsd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

@Entity(tableName = "units_of_measure")
data class UnitOfMeasure(
    @PrimaryKey
    val id: Int,
    val code: String,
    val name: String,
    @ColumnInfo(name = "short_name")
    @SerializedName("short_name")
    val shortName: String,
    val description: String?,
    @ColumnInfo(name = "is_active")
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: String? = null
)
