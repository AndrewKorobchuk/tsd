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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DocumentsScreen()
                1 -> DirectoriesScreen()
                2 -> SettingsScreen()
            }
        }
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector
)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TsdTheme {
        MainScreen()
    }
}
