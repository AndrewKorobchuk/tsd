package com.sh.an.tsd.ui.nomenclature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.ui.theme.TsdTheme
import com.sh.an.tsd.data.model.NomenclatureCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NomenclatureCategoriesScreen(
    categories: List<NomenclatureCategory> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onClearError: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Обновляем поиск при изменении
    LaunchedEffect(searchQuery) {
        onSearchQueryChange(searchQuery)
    }
    

    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок с кнопкой назад
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("← Назад")
            }
            Text(
                text = "Категорії товарів",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Поиск
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Поиск категорий") },
            placeholder = { Text("Название категории или штрих-код товара") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
            trailingIcon = {
                Row {
                    IconButton(onClick = { /* TODO: Сканировать штрих-код */ }) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Сканировать штрих-код")
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Очистить")
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Список категорий
        val filteredCategories = if (searchQuery.isEmpty()) {
            categories
        } else {
            // Проверяем, является ли поисковый запрос штрих-кодом (только цифры)
            val isBarcode = searchQuery.all { it.isDigit() } && searchQuery.length >= 8
            
            if (isBarcode) {
                // Если это штрих-код, показываем все категории с подсветкой
                categories
            } else {
                // Обычный поиск по названию и описанию категории
                categories.filter { category ->
                    category.name.contains(searchQuery, ignoreCase = true) ||
                    (category.description?.contains(searchQuery, ignoreCase = true) == true)
                }
            }
        }
        
        // Индикатор поиска по штрих-коду
        if (searchQuery.isNotEmpty() && searchQuery.all { it.isDigit() } && searchQuery.length >= 8) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCode,
                        contentDescription = "Штрих-код",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Поиск по штрих-коду: $searchQuery",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = filteredCategories,
                key = { category -> category.id }
            ) { category ->
                CategoryCard(
                    category = category,
                    onItemClick = { onCategoryClick(category.id.toString()) }
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: com.sh.an.tsd.data.model.NomenclatureCategory,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onItemClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка категории
            Card(
                modifier = Modifier.size(68.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Category,
                        contentDescription = category.name,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Информация о категории
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = category.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                category.description?.let { description ->
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                }
                
                Text(
                    text = "Код: ${category.code}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Стрелка
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Открыть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun NomenclatureCategoriesScreenPreview() {
    TsdTheme {
        NomenclatureCategoriesScreen(
            categories = listOf(
                com.sh.an.tsd.data.model.NomenclatureCategory(
                    id = 1,
                    code = "FOOD",
                    name = "Продукты питания",
                    description = "Категория продуктов питания и напитков",
                    isActive = true,
                    createdAt = "2025-10-12T18:59:20",
                    updatedAt = null
                )
            )
        )
    }
}
