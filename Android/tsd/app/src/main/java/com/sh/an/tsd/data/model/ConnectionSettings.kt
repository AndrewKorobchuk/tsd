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
        return if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            baseUrl
        } else {
            "http://$baseUrl"
        }
    }
}

