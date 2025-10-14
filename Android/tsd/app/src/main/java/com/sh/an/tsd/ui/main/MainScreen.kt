package com.sh.an.tsd.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.ui.documents.DocumentsScreen
import com.sh.an.tsd.ui.documents.DocumentsViewModel
import com.sh.an.tsd.ui.directories.DirectoriesScreen
import com.sh.an.tsd.ui.settings.SettingsScreen
import com.sh.an.tsd.ui.units.UnitsScreen
import com.sh.an.tsd.ui.units.UnitsViewModel
import com.sh.an.tsd.ui.directories.DirectoriesViewModel
import com.sh.an.tsd.ui.nomenclature.NomenclatureCategoriesViewModel
import com.sh.an.tsd.ui.nomenclature.NomenclatureItemsViewModel
import com.sh.an.tsd.ui.warehouses.WarehousesViewModel
import com.sh.an.tsd.data.repository.AuthRepository
import com.sh.an.tsd.ui.theme.TsdTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogoutClick: () -> Unit = {},
    unitsViewModel: UnitsViewModel? = null,
    directoriesViewModel: DirectoriesViewModel? = null,
    nomenclatureCategoriesViewModel: NomenclatureCategoriesViewModel? = null,
    nomenclatureItemsViewModel: NomenclatureItemsViewModel? = null,
    warehousesViewModel: WarehousesViewModel? = null,
    documentsViewModel: DocumentsViewModel? = null,
    authRepository: AuthRepository? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var currentScreen by remember { mutableStateOf("main") }
    var currentCategoryId by remember { mutableStateOf("") }
    var currentCategoryName by remember { mutableStateOf("") }
    
        val tabs = listOf(
            TabItem("Документи", Icons.Filled.Description),
            TabItem("Довідники", Icons.Filled.List),
            TabItem("Налаштування", Icons.Filled.Settings)
        )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ТСД Система",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Выйти",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (currentScreen == "main") {
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
                when (currentScreen) {
                    "main" -> {
                        when (selectedTab) {
                            0 -> {
                                if (documentsViewModel != null) {
                                    val documents by documentsViewModel.documents.collectAsState()
                                    val isLoading by documentsViewModel.isLoading.collectAsState()
                                    val errorMessage by documentsViewModel.errorMessage.collectAsState()
                                    val selectedDocumentType by documentsViewModel.selectedDocumentType.collectAsState()
                                    val selectedStatus by documentsViewModel.selectedStatus.collectAsState()
                                    
                                    DocumentsScreen(
                                        documents = documents,
                                        isLoading = isLoading,
                                        errorMessage = errorMessage,
                                        selectedDocumentType = selectedDocumentType,
                                        selectedStatus = selectedStatus,
                                        onDocumentClick = { /* TODO: Открыть детали документа */ },
                                        onFilterByType = { documentsViewModel.filterByDocumentType(it) },
                                        onFilterByStatus = { documentsViewModel.filterByStatus(it) },
                                        onSyncClick = { 
                                            authRepository?.let { auth ->
                                                documentsViewModel.syncDocumentsFromServer("Bearer ${auth.getAccessToken()}")
                                            }
                                        },
                                        onClearFilters = { documentsViewModel.clearFilters() },
                                        onClearError = { documentsViewModel.clearError() },
                                        onBackClick = { /* Не нужно для главного экрана */ }
                                    )
                                } else {
                                    DocumentsScreen(
                                        documents = emptyList(),
                                        isLoading = false,
                                        errorMessage = "DocumentsViewModel не инициализирован",
                                        selectedDocumentType = null,
                                        selectedStatus = null,
                                        onDocumentClick = { },
                                        onFilterByType = { },
                                        onFilterByStatus = { },
                                        onSyncClick = { },
                                        onClearFilters = { },
                                        onClearError = { },
                                        onBackClick = { }
                                    )
                                }
                            }
                            1 -> {
                                if (directoriesViewModel != null) {
                                    val unitsCount by directoriesViewModel.unitsCount.collectAsState()
                                    val categoriesCount by directoriesViewModel.categoriesCount.collectAsState()
                                    val nomenclatureCount by directoriesViewModel.nomenclatureCount.collectAsState()
                                    val warehousesCount by directoriesViewModel.warehousesCount.collectAsState()
                                    
                                    DirectoriesScreen(
                                        onNomenclatureClick = { currentScreen = "nomenclature_categories" },
                                        onUnitsClick = { currentScreen = "units" },
                                        onWarehousesClick = { currentScreen = "warehouses" },
                                        unitsCount = unitsCount,
                                        categoriesCount = categoriesCount,
                                        nomenclatureCount = nomenclatureCount,
                                        warehousesCount = warehousesCount
                                    )
                                } else {
                                    DirectoriesScreen(
                                        onNomenclatureClick = { currentScreen = "nomenclature_categories" },
                                        onUnitsClick = { currentScreen = "units" },
                                        onWarehousesClick = { currentScreen = "warehouses" },
                                        unitsCount = 0,
                                        categoriesCount = 0,
                                        nomenclatureCount = 0,
                                        warehousesCount = 0
                                    )
                                }
                            }
                            2 -> {
                                if (directoriesViewModel != null && authRepository != null) {
                                    val isLoadingDirectories by directoriesViewModel.isLoading.collectAsState()
                                    val directoriesError by directoriesViewModel.errorMessage.collectAsState()
                                    val syncProgress by directoriesViewModel.syncProgress.collectAsState()
                                    
                                    SettingsScreen(
                                        onLoadDirectoriesClick = {
                                            val token = "Bearer ${authRepository.getAccessToken()}"
                                            directoriesViewModel.syncAllDirectories(token)
                                        },
                                        isLoadingDirectories = isLoadingDirectories,
                                        directoriesError = directoriesError,
                                        syncProgress = syncProgress,
                                        onClearDirectoriesError = {
                                            directoriesViewModel.clearError()
                                        },
                                        onClearProgress = {
                                            directoriesViewModel.clearProgress()
                                        }
                                    )
                                } else {
                                    SettingsScreen()
                                }
                            }
                        }
                    }
                "warehouses" -> {
                    if (warehousesViewModel != null) {
                        val warehouses by warehousesViewModel.warehouses.collectAsState()
                        val isLoading by warehousesViewModel.isLoading.collectAsState()
                        val errorMessage by warehousesViewModel.errorMessage.collectAsState()
                        
                        com.sh.an.tsd.ui.warehouses.WarehousesScreen(
                            warehouses = warehouses,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onBackClick = { currentScreen = "main" },
                            onWarehouseClick = { /* TODO: Открыть детали склада */ },
                            onSearchQueryChange = { query ->
                                warehousesViewModel.searchWarehouses(query)
                            },
                            onClearError = {
                                warehousesViewModel.clearError()
                            }
                        )
                    } else {
                        com.sh.an.tsd.ui.warehouses.WarehousesScreen(
                            warehouses = emptyList(),
                            isLoading = false,
                            errorMessage = null,
                            onBackClick = { currentScreen = "main" },
                            onWarehouseClick = { /* TODO: Открыть детали склада */ },
                            onSearchQueryChange = {},
                            onClearError = {}
                        )
                    }
                }
                "nomenclature_categories" -> {
                    if (nomenclatureCategoriesViewModel != null) {
                        val categories by nomenclatureCategoriesViewModel.categories.collectAsState()
                        val isLoading by nomenclatureCategoriesViewModel.isLoading.collectAsState()
                        val errorMessage by nomenclatureCategoriesViewModel.errorMessage.collectAsState()
                        
                        com.sh.an.tsd.ui.nomenclature.NomenclatureCategoriesScreen(
                            categories = categories,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onBackClick = { currentScreen = "main" },
                            onCategoryClick = { categoryId ->
                                currentCategoryId = categoryId
                                currentCategoryName = getCategoryNameById(categoryId)
                                currentScreen = "nomenclature_items"
                            },
                            onSearchQueryChange = { query ->
                                nomenclatureCategoriesViewModel.searchCategories(query)
                            },
                            onClearError = {
                                nomenclatureCategoriesViewModel.clearError()
                            }
                        )
                    } else {
                        com.sh.an.tsd.ui.nomenclature.NomenclatureCategoriesScreen(
                            categories = emptyList(),
                            isLoading = false,
                            errorMessage = null,
                            onBackClick = { currentScreen = "main" },
                            onCategoryClick = { categoryId ->
                                currentCategoryId = categoryId
                                currentCategoryName = getCategoryNameById(categoryId)
                                currentScreen = "nomenclature_items"
                            },
                            onSearchQueryChange = {},
                            onClearError = {}
                        )
                    }
                }
                    "nomenclature_items" -> {
                        if (nomenclatureItemsViewModel != null) {
                            val nomenclature by nomenclatureItemsViewModel.nomenclature.collectAsState()
                            val isLoading by nomenclatureItemsViewModel.isLoading.collectAsState()
                            val errorMessage by nomenclatureItemsViewModel.errorMessage.collectAsState()
                            
                            // Устанавливаем categoryId в ViewModel
                            LaunchedEffect(currentCategoryId) {
                                if (currentCategoryId.isNotEmpty()) {
                                    nomenclatureItemsViewModel.setCategoryId(currentCategoryId.toInt())
                                }
                            }
                            
                            com.sh.an.tsd.ui.nomenclature.NomenclatureItemsScreen(
                                nomenclature = nomenclature,
                                isLoading = isLoading,
                                errorMessage = errorMessage,
                                categoryId = currentCategoryId,
                                categoryName = currentCategoryName,
                                onBackClick = { currentScreen = "nomenclature_categories" },
                                onItemClick = { /* TODO: Открыть детали товара */ },
                                onSearchQueryChange = { query ->
                                    nomenclatureItemsViewModel.searchNomenclature(query)
                                },
                                onClearError = {
                                    nomenclatureItemsViewModel.clearError()
                                }
                            )
                        } else {
                            com.sh.an.tsd.ui.nomenclature.NomenclatureItemsScreen(
                                nomenclature = emptyList(),
                                isLoading = false,
                                errorMessage = null,
                                categoryId = currentCategoryId,
                                categoryName = currentCategoryName,
                                onBackClick = { currentScreen = "nomenclature_categories" },
                                onItemClick = { /* TODO: Открыть детали товара */ },
                                onSearchQueryChange = {},
                                onClearError = {}
                            )
                        }
                    }
                    "units" -> {
                        if (unitsViewModel != null && authRepository != null) {
                            val units by unitsViewModel.units.collectAsState()
                            val isLoading by unitsViewModel.isLoading.collectAsState()
                            val errorMessage by unitsViewModel.errorMessage.collectAsState()
                            val lastSyncTime by unitsViewModel.lastSyncTime.collectAsState()
                            val localUnitsCount by unitsViewModel.localUnitsCount.collectAsState()
                            
                            UnitsScreen(
                                units = units,
                                isLoading = isLoading,
                                errorMessage = errorMessage,
                                lastSyncTime = lastSyncTime,
                                localUnitsCount = localUnitsCount,
                                onSearchQueryChange = { query ->
                                    unitsViewModel.searchUnits(query)
                                },
                                onClearError = {
                                    unitsViewModel.clearError()
                                },
                                onBackClick = { currentScreen = "main" }
                            )
                        } else {
                            androidx.compose.material3.Text("Одиниці виміру")
                        }
                    }
            }
        }
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector
)

// Функция для получения названия категории по ID
fun getCategoryNameById(categoryId: String): String {
    return when (categoryId) {
        "1" -> "Хлібобулочні вироби"
        "2" -> "Молочні продукти"
        "3" -> "М'ясо та ковбаси"
        "4" -> "Овочі та фрукти"
        "5" -> "Крупи та цукор"
        "6" -> "Консерви"
        "7" -> "Напої"
        "8" -> "Солодощі"
        else -> "Невідома категорія"
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TsdTheme {
        MainScreen()
    }
}
