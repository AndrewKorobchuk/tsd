package com.sh.an.tsd.data.model

data class ConnectionSettings(
    val serverUrl: String,
    val port: String,
    val apiKey: String = ""
) {
    fun getBaseUrl(): String {
        return if (port.isBlank()) {
            serverUrl
        } else {
            "$serverUrl:$port"
        }
    }
    
    fun getFullUrl(): String {
        val baseUrl = getBaseUrl()
        if (baseUrl.isBlank()) {
            return "" // Возвращаем пустую строку, если настройки не заданы
        }
        return if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            baseUrl
        } else {
            "http://$baseUrl"
        }
    }
}

