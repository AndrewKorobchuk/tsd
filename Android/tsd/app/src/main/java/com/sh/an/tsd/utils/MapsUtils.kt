package com.sh.an.tsd.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object MapsUtils {
    
    /**
     * Открывает Google Maps с указанным адресом
     * @param context Контекст приложения
     * @param address Адрес для поиска на карте
     */
    fun openGoogleMaps(context: Context, address: String) {
        try {
            // Кодируем адрес для URL
            val encodedAddress = Uri.encode(address)
            
            // Создаем Intent для Google Maps
            val mapIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=$encodedAddress")
                setPackage("com.google.android.apps.maps") // Принудительно используем Google Maps
            }
            
            // Проверяем, установлено ли приложение Google Maps
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Если Google Maps не установлен, открываем в браузере
                openMapsInBrowser(context, address)
            }
        } catch (e: Exception) {
            // В случае ошибки показываем Toast и пытаемся открыть в браузере
            Toast.makeText(context, "Ошибка открытия карт", Toast.LENGTH_SHORT).show()
            openMapsInBrowser(context, address)
        }
    }
    
    /**
     * Открывает карты в браузере как fallback
     * @param context Контекст приложения
     * @param address Адрес для поиска на карте
     */
    private fun openMapsInBrowser(context: Context, address: String) {
        try {
            val encodedAddress = Uri.encode(address)
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedAddress")
            }
            
            if (browserIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(browserIntent)
            } else {
                Toast.makeText(context, "Не удалось открыть карты", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка открытия карт в браузере", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Открывает Google Maps с координатами
     * @param context Контекст приложения
     * @param latitude Широта
     * @param longitude Долгота
     * @param label Подпись для маркера
     */
    fun openGoogleMapsWithCoordinates(
        context: Context, 
        latitude: Double, 
        longitude: Double, 
        label: String = ""
    ) {
        try {
            val mapIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
                setPackage("com.google.android.apps.maps")
            }
            
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback в браузер
                val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка открытия карт", Toast.LENGTH_SHORT).show()
        }
    }
}
