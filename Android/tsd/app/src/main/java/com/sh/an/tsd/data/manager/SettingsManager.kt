package com.sh.an.tsd.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.sh.an.tsd.data.model.ConnectionSettings
import com.sh.an.tsd.data.model.OAuthClientResponse

class SettingsManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("tsd_settings", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_PORT = "port"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_CLIENT_ID = "client_id"
        private const val KEY_CLIENT_SECRET = "client_secret"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    fun saveConnectionSettings(settings: ConnectionSettings) {
        prefs.edit().apply {
            putString(KEY_SERVER_URL, settings.serverUrl)
            putString(KEY_PORT, settings.port)
            putString(KEY_API_KEY, settings.apiKey)
            apply()
        }
    }
    
    fun getConnectionSettings(): ConnectionSettings {
        val serverUrl = prefs.getString(KEY_SERVER_URL, null)
        val port = prefs.getString(KEY_PORT, null)
        
        // Если настройки не сохранены, возвращаем значения по умолчанию
        return ConnectionSettings(
            serverUrl = serverUrl ?: "localhost",
            port = port ?: "8001",
            apiKey = prefs.getString(KEY_API_KEY, "") ?: ""
        )
    }
    
    fun saveOAuthClient(client: OAuthClientResponse) {
        prefs.edit().apply {
            putString(KEY_CLIENT_ID, client.clientId)
            putString(KEY_CLIENT_SECRET, client.clientSecret)
            apply()
        }
    }
    
    fun getOAuthClientId(): String {
        return prefs.getString(KEY_CLIENT_ID, "") ?: ""
    }
    
    fun getOAuthClientSecret(): String {
        return prefs.getString(KEY_CLIENT_SECRET, "") ?: ""
    }
    
    fun getOAuthClient(): OAuthClientResponse? {
        val clientId = getOAuthClientId()
        val clientSecret = getOAuthClientSecret()
        return if (clientId.isNotEmpty() && clientSecret.isNotEmpty()) {
            OAuthClientResponse(
                id = 0, // ID не сохраняется, но нужен для совместимости
                clientId = clientId,
                clientSecret = clientSecret,
                clientName = "TSD Mobile App",
                redirectUris = listOf("http://localhost:8001/callback"),
                scope = "read write",
                isActive = true,
                createdAt = ""
            )
        } else {
            null
        }
    }
    
    fun saveAuthData(accessToken: String, refreshToken: String?, expiresIn: Int) {
        val expiresAt = System.currentTimeMillis() + (expiresIn * 1000L)
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_TOKEN_EXPIRES_AT, expiresAt)
            apply()
        }
    }
    
    fun getAccessToken(): String {
        return prefs.getString(KEY_ACCESS_TOKEN, "") ?: ""
    }
    
    fun getRefreshToken(): String {
        return prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
    }
    
    fun isTokenValid(): Boolean {
        val expiresAt = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0)
        return expiresAt > System.currentTimeMillis()
    }
    
    fun saveUserData(userId: Int, username: String, email: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getUserData(): Triple<Int, String, String> {
        return Triple(
            prefs.getInt(KEY_USER_ID, 0),
            prefs.getString(KEY_USERNAME, "") ?: "",
            prefs.getString(KEY_USER_EMAIL, "") ?: ""
        )
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && isTokenValid()
    }
    
    fun logout() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRES_AT)
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_USER_EMAIL)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
    
    fun clearAuthData() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRES_AT)
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_CLIENT_ID)
            remove(KEY_CLIENT_SECRET)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

