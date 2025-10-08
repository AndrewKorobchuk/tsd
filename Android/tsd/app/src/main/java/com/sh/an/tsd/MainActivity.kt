package com.sh.an.tsd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sh.an.tsd.ui.login.LoginScreen
import com.sh.an.tsd.ui.main.MainScreen
import com.sh.an.tsd.ui.settings.ConnectionSettingsScreen
import com.sh.an.tsd.ui.theme.TsdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TsdTheme {
                TsdApp()
            }
        }
    }
}

@Composable
fun TsdApp() {
    var currentScreen by remember { mutableStateOf("login") }
    var isLoggedIn by remember { mutableStateOf(false) }
    
    if (!isLoggedIn) {
        // Экран входа
        when (currentScreen) {
            "login" -> {
                LoginScreen(
                    onLoginClick = { username, password ->
                        // Заглушка логина - считаем что залогинились
                        println("Login attempt: $username")
                        isLoggedIn = true
                    },
                    onSettingsClick = {
                        currentScreen = "settings"
                    }
                )
            }
            "settings" -> {
                ConnectionSettingsScreen(
                    onBackClick = {
                        currentScreen = "login"
                    },
                    onSaveClick = { serverUrl, port, apiKey ->
                        // TODO: Сохранить настройки подключения
                        println("Settings saved: $serverUrl:$port")
                        currentScreen = "login"
                    }
                )
            }
        }
    } else {
        // Главный экран после входа
        MainScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun TsdAppPreview() {
    TsdTheme {
        TsdApp()
    }
}