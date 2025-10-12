package com.sh.an.tsd.data.model

import com.google.gson.annotations.SerializedName

data class OAuthClient(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("client_id")
    val clientId: String,
    
    @SerializedName("client_name")
    val clientName: String,
    
    @SerializedName("redirect_uris")
    val redirectUris: List<String>,
    
    @SerializedName("scope")
    val scope: String,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String
)

data class OAuthClientCreate(
    @SerializedName("client_name")
    val clientName: String,
    
    @SerializedName("redirect_uris")
    val redirectUris: List<String>,
    
    @SerializedName("scope")
    val scope: String = "read write"
)

data class OAuthClientResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("client_id")
    val clientId: String,
    
    @SerializedName("client_name")
    val clientName: String,
    
    @SerializedName("redirect_uris")
    val redirectUris: List<String>,
    
    @SerializedName("scope")
    val scope: String,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("client_secret")
    val clientSecret: String
)

