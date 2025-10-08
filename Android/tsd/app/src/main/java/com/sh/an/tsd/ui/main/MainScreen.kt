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
import com.sh.an.tsd.ui.directories.DirectoriesScreen
import com.sh.an.tsd.ui.settings.SettingsScreen
import com.sh.an.tsd.ui.theme.TsdTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
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
                        0 -> DocumentsScreen()
                        1 -> DirectoriesScreen(
                            onNomenclatureClick = { currentScreen = "nomenclature_categories" }
                        )
                        2 -> SettingsScreen()
                    }
                }
                "nomenclature_categories" -> {
                    com.sh.an.tsd.ui.nomenclature.NomenclatureCategoriesScreen(
                        onBackClick = { currentScreen = "main" },
                        onCategoryClick = { categoryId ->
                            currentCategoryId = categoryId
                            currentCategoryName = getCategoryNameById(categoryId)
                            currentScreen = "nomenclature_items"
                        }
                    )
                }
                "nomenclature_items" -> {
                    com.sh.an.tsd.ui.nomenclature.NomenclatureItemsScreen(
                        categoryId = currentCategoryId,
                        categoryName = currentCategoryName,
                        onBackClick = { currentScreen = "nomenclature_categories" },
                        onItemClick = { /* TODO: Открыть детали товара */ }
                    )
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
