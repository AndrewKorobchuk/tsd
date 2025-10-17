package com.sh.an.tsd.data.model

data class ConnectionSettings(
    val serverUrl: String,
    val port: String,
    val apiKey: String = ""
) {
    fun getBaseUrl(): String {
        val trimmedServerUrl = serverUrl.trim()
        val trimmedPort = port.trim()
        return if (trimmedPort.isBlank()) {
            trimmedServerUrl
        } else {
            "$trimmedServerUrl:$trimmedPort"
        }
    }
    
    fun getFullUrl(): String {
        val baseUrl = getBaseUrl()
        if (baseUrl.isBlank()) {
            return "" // Возвращаем пустую строку, если настройки не заданы
        }
        val fullUrl = if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            baseUrl
        } else {
            "http://$baseUrl"
        }
        return "$fullUrl/"
    }
}

