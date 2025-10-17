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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.sh.an.tsd.data.manager.SettingsManager
import com.sh.an.tsd.data.repository.AuthRepository
import com.sh.an.tsd.data.repository.UnitsRepository
import com.sh.an.tsd.data.repository.NomenclatureCategoriesRepository
import com.sh.an.tsd.data.repository.NomenclatureRepository
import com.sh.an.tsd.data.repository.WarehousesRepository
import com.sh.an.tsd.data.repository.DocumentsRepository
import com.sh.an.tsd.data.database.TsdDatabase
import com.sh.an.tsd.ui.units.UnitsScreen
import com.sh.an.tsd.ui.units.UnitsViewModel
import com.sh.an.tsd.ui.directories.DirectoriesViewModel
import com.sh.an.tsd.ui.nomenclature.NomenclatureCategoriesViewModel
import com.sh.an.tsd.ui.nomenclature.NomenclatureItemsViewModel
import com.sh.an.tsd.ui.warehouses.WarehousesViewModel
import com.sh.an.tsd.ui.documents.DocumentsViewModel
import com.sh.an.tsd.ui.documents.DocumentsMainViewModel
import com.sh.an.tsd.ui.documents.DocumentCreateViewModel
import com.sh.an.tsd.ui.login.LoginScreen
import com.sh.an.tsd.ui.main.MainScreen
import com.sh.an.tsd.ui.settings.ConnectionSettingsScreen
import com.sh.an.tsd.ui.theme.TsdTheme
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val authRepository = remember { AuthRepository(settingsManager) }
    
        // Инициализация базы данных и репозиториев
        val database = remember { TsdDatabase.getDatabase(context) }
        // Создаем репозитории лениво, только когда они нужны и есть настройки подключения
        val unitsRepository = remember { 
            if (authRepository.hasConnectionSettings()) {
                UnitsRepository(
                    authRepository.createUnitsApiService(),
                    database.unitOfMeasureDao()
                )
            } else null
        }
        val nomenclatureCategoriesRepository = remember {
            if (authRepository.hasConnectionSettings()) {
                NomenclatureCategoriesRepository(
                    authRepository.createNomenclatureCategoriesApiService(),
                    database.nomenclatureCategoryDao()
                )
            } else null
        }
        val nomenclatureRepository = remember {
            if (authRepository.hasConnectionSettings()) {
                NomenclatureRepository(
                    authRepository.createNomenclatureApiService(),
                    database.nomenclatureDao()
                )
            } else null
        }
        val warehousesRepository = remember {
            if (authRepository.hasConnectionSettings()) {
                WarehousesRepository(
                    authRepository.createWarehousesApiService(),
                    database.warehouseDao()
                )
            } else null
        }
        val documentsRepository = remember {
            if (authRepository.hasConnectionSettings()) {
                DocumentsRepository(
                    authRepository.createDocumentsApiService(),
                    database.documentDao(),
                    database.documentItemDao()
                )
            } else null
        }
        val unitsViewModel = remember { unitsRepository?.let { UnitsViewModel(it) } }
        val directoriesViewModel = remember { 
            if (unitsRepository != null && nomenclatureCategoriesRepository != null && 
                nomenclatureRepository != null && warehousesRepository != null) {
                DirectoriesViewModel(
                    unitsRepository,
                    nomenclatureCategoriesRepository,
                    nomenclatureRepository,
                    warehousesRepository
                )
            } else null
        }
        val nomenclatureCategoriesViewModel = remember {
            nomenclatureCategoriesRepository?.let { NomenclatureCategoriesViewModel(it) }
        }
        val nomenclatureItemsViewModel = remember {
            nomenclatureRepository?.let { NomenclatureItemsViewModel(it) }
        }
        val warehousesViewModel = remember {
            warehousesRepository?.let { WarehousesViewModel(it) }
        }
        val documentsViewModel = remember {
            documentsRepository?.let { DocumentsViewModel(it) }
        }
        val documentsMainViewModel = remember {
            documentsRepository?.let { DocumentsMainViewModel(it) }
        }
        val documentCreateViewModel = remember {
            if (documentsRepository != null && warehousesRepository != null && 
                nomenclatureRepository != null && unitsRepository != null) {
                DocumentCreateViewModel(
                    documentsRepository,
                    warehousesRepository,
                    nomenclatureRepository,
                    unitsRepository
                )
            } else null
        }
    
    var currentScreen by remember { mutableStateOf("login") }
    var isLoggedIn by remember { mutableStateOf(authRepository.isLoggedIn()) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    if (!isLoggedIn) {
        // Экран входа
        when (currentScreen) {
            "login" -> {
                LoginScreen(
                    onLoginClick = { username, password ->
                        coroutineScope.launch {
                            isLoading = true
                            loginError = null
                            
                            val result = authRepository.login(username, password)
                            result.fold(
                                onSuccess = { user ->
                                    isLoggedIn = true
                                    println("Login successful: ${user.username}")
                                },
                                onFailure = { error ->
                                    loginError = error.message ?: "Ошибка авторизации"
                                    println("Login failed: ${error.message}")
                                }
                            )
                            isLoading = false
                        }
                    },
                    onSettingsClick = {
                        currentScreen = "settings"
                    },
                    isLoading = isLoading,
                    errorMessage = loginError
                )
            }
            "settings" -> {
                val currentSettings = settingsManager.getConnectionSettings()
                ConnectionSettingsScreen(
                    onBackClick = {
                        currentScreen = "login"
                    },
                    onSaveClick = { serverUrl, port, apiKey ->
                        val settings = com.sh.an.tsd.data.model.ConnectionSettings(serverUrl, port, apiKey)
                        settingsManager.saveConnectionSettings(settings)
                        // Сбрасываем кэш Retrofit для использования новых настроек
                        authRepository.resetApiServiceFactory()
                        println("Settings saved: ${settings.getFullUrl()}")
                        currentScreen = "login"
                    },
                    initialServerUrl = currentSettings.serverUrl,
                    initialPort = currentSettings.port,
                    initialApiKey = currentSettings.apiKey
                )
            }
        }
    } else {
                // Главный экран после входа
                MainScreen(
                    onLogoutClick = {
                        authRepository.logout()
                        isLoggedIn = false
                    },
                    unitsViewModel = unitsViewModel,
                    directoriesViewModel = directoriesViewModel,
                    nomenclatureCategoriesViewModel = nomenclatureCategoriesViewModel,
                    nomenclatureItemsViewModel = nomenclatureItemsViewModel,
                    warehousesViewModel = warehousesViewModel,
                    documentsViewModel = documentsViewModel,
                    documentsMainViewModel = documentsMainViewModel,
                    documentCreateViewModel = documentCreateViewModel,
                    authRepository = authRepository
                )
    }
}

@Preview(showBackground = true)
@Composable
fun TsdAppPreview() {
    TsdTheme {
        TsdApp()
    }
}