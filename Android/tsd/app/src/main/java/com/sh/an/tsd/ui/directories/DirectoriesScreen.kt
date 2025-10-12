package com.sh.an.tsd.ui.directories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.ui.theme.TsdTheme

@Composable
fun DirectoriesScreen(
    onNomenclatureClick: () -> Unit = {},
    onUnitsClick: () -> Unit = {},
    onWarehousesClick: () -> Unit = {},
    unitsCount: Int = 0,
    categoriesCount: Int = 0,
    nomenclatureCount: Int = 0,
    warehousesCount: Int = 0
) {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Поиск
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Поиск в справочниках") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Список справочников
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getDirectories(unitsCount, categoriesCount, nomenclatureCount, warehousesCount)) { directory ->
                DirectoryCard(
                    directory = directory,
                    onItemClick = { 
                        when (directory.id) {
                            "1" -> onNomenclatureClick() // Товари -> Номенклатура
                            "3" -> onWarehousesClick() // Склади -> Склады
                            "4" -> onUnitsClick() // Одиниці виміру
                            "5" -> onNomenclatureClick() // Категорії товарів -> Номенклатура
                            else -> { /* TODO: Открыть другие справочники */ }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DirectoryCard(
    directory: Directory,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onItemClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = directory.icon,
                contentDescription = directory.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = directory.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = directory.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Записей: ${directory.itemCount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Открыть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class Directory(
    val id: String,
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val itemCount: Int
)

fun getDirectories(unitsCount: Int = 0, categoriesCount: Int = 0, nomenclatureCount: Int = 0, warehousesCount: Int = 0): List<Directory> {
    return listOf(
        Directory(
            id = "1",
            name = "Товари",
            description = "Справочник товаров и услуг",
            icon = Icons.Filled.Inventory,
            itemCount = nomenclatureCount
        ),
        Directory(
            id = "2",
            name = "Клієнти",
            description = "Справочник клиентов и контрагентов",
            icon = Icons.Filled.People,
            itemCount = 340
        ),
        Directory(
            id = "3",
            name = "Склади",
            description = "Справочник складов и мест хранения",
            icon = Icons.Filled.Store,
            itemCount = warehousesCount
        ),
        Directory(
            id = "4",
            name = "Одиниці виміру",
            description = "Справочник единиц измерения",
            icon = Icons.Filled.Straighten,
            itemCount = unitsCount
        ),
        Directory(
            id = "5",
            name = "Категорії товарів",
            description = "Справочник категорий номенклатуры",
            icon = Icons.Filled.Category,
            itemCount = categoriesCount
        ),
        Directory(
            id = "6",
            name = "Ставки ПДВ",
            description = "Справочник налоговых ставок",
            icon = Icons.Filled.Percent,
            itemCount = 5
        ),
        Directory(
            id = "7",
            name = "Країни",
            description = "Справочник стран",
            icon = Icons.Filled.Public,
            itemCount = 195
        )
    )
}

@Preview(showBackground = true)
@Composable
fun DirectoriesScreenPreview() {
    TsdTheme {
        DirectoriesScreen()
    }
}
