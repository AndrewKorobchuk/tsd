package com.sh.an.tsd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tsd_device")
data class TsdDevice(
    @PrimaryKey
    val id: Int = 0,
    
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("device_name")
    val deviceName: String,
    
    @SerializedName("device_model")
    val deviceModel: String,
    
    @SerializedName("android_version")
    val androidVersion: String,
    
    @SerializedName("app_version")
    val appVersion: String,
    
    val prefix: String,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("last_seen")
    val lastSeen: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

// Запрос на регистрацию устройства
data class TsdDeviceRegisterRequest(
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("device_name")
    val deviceName: String,
    
    @SerializedName("device_model")
    val deviceModel: String,
    
    @SerializedName("android_version")
    val androidVersion: String,
    
    @SerializedName("app_version")
    val appVersion: String
)

// Ответ сервера при регистрации
data class TsdDeviceRegisterResponse(
    val id: Int,
    @SerializedName("device_id")
    val deviceId: String,
    val prefix: String,
    @SerializedName("is_active")
    val isActive: Boolean
)

// Запрос на получение следующего номера документа
data class DocumentNumberRequest(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("document_type")
    val documentType: String = "input_balance"
)

// Ответ с номером документа
data class DocumentNumberResponse(
    @SerializedName("document_number")
    val documentNumber: String,
    @SerializedName("next_counter")
    val nextCounter: Int
)

