package com.sh.an.tsd.ui.nomenclature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.ui.theme.TsdTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NomenclatureCategoriesScreen(
    onBackClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Заглушка данных категорий
    val categories = remember {
        listOf(
            NomenclatureCategory(
                id = "1",
                name = "Хлібобулочні вироби",
                description = "Хліб, булочки, випічка",
                icon = Icons.Filled.BakeryDining,
                color = Color(0xFF8D6E63),
                itemCount = 25
            ),
            NomenclatureCategory(
                id = "2",
                name = "Молочні продукти",
                description = "Молоко, сир, йогурт, масло",
                icon = Icons.Filled.LocalDrink,
                color = Color(0xFF2196F3),
                itemCount = 18
            ),
            NomenclatureCategory(
                id = "3",
                name = "М'ясо та ковбаси",
                description = "М'ясо, ковбаси, копченості",
                icon = Icons.Filled.Restaurant,
                color = Color(0xFFF44336),
                itemCount = 32
            ),
            NomenclatureCategory(
                id = "4",
                name = "Овочі та фрукти",
                description = "Свіжі овочі та фрукти",
                icon = Icons.Filled.LocalGroceryStore,
                color = Color(0xFF4CAF50),
                itemCount = 45
            ),
            NomenclatureCategory(
                id = "5",
                name = "Крупи та цукор",
                description = "Крупи, макарони, цукор",
                icon = Icons.Filled.Grain,
                color = Color(0xFFFF9800),
                itemCount = 15
            ),
            NomenclatureCategory(
                id = "6",
                name = "Консерви",
                description = "Консервовані продукти",
                icon = Icons.Filled.Inventory,
                color = Color(0xFF9C27B0),
                itemCount = 28
            ),
            NomenclatureCategory(
                id = "7",
                name = "Напої",
                description = "Соки, вода, газовані напої",
                icon = Icons.Filled.LocalBar,
                color = Color(0xFF00BCD4),
                itemCount = 22
            ),
            NomenclatureCategory(
                id = "8",
                name = "Солодощі",
                description = "Цукерки, печиво, шоколад",
                icon = Icons.Filled.Cake,
                color = Color(0xFFE91E63),
                itemCount = 35
            )
        )
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
                    category.description.contains(searchQuery, ignoreCase = true)
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
                    onItemClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: NomenclatureCategory,
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
                    containerColor = category.color.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = category.color,
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
                
                Text(
                    text = category.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Товарів: ${category.itemCount}",
                    fontSize = 12.sp,
                    color = category.color,
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

data class NomenclatureCategory(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val itemCount: Int
)

@Preview(showBackground = true)
@Composable
fun NomenclatureCategoriesScreenPreview() {
    TsdTheme {
        NomenclatureCategoriesScreen()
    }
}
