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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.data.model.Nomenclature
import com.sh.an.tsd.ui.theme.TsdTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NomenclatureItemsScreen(
    nomenclature: List<com.sh.an.tsd.data.model.Nomenclature> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    categoryId: String,
    categoryName: String,
    onBackClick: () -> Unit = {},
    onItemClick: (com.sh.an.tsd.data.model.Nomenclature) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onClearError: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Обновляем поиск при изменении
    LaunchedEffect(searchQuery) {
        onSearchQueryChange(searchQuery)
    }
    
    // Заглушка данных товаров по категориям (используется только в Preview)
    val mockItemsByCategory = remember {
        mapOf(
            "1" to listOf( // Хлібобулочні вироби
                MockNomenclature("1", "001", "Хліб білий", "шт", "Свіжий білий хліб", "Хлібобулочні вироби", "1234567890123", 25.50),
                MockNomenclature("2", "002", "Хліб чорний", "шт", "Хліб з житнього борошна", "Хлібобулочні вироби", "1234567890124", 28.00),
                MockNomenclature("3", "003", "Булочка з маком", "шт", "Солодка булочка з маковою начинкою", "Хлібобулочні вироби", "1234567890125", 15.00),
                MockNomenclature("4", "004", "Круасан", "шт", "Французький круасан", "Хлібобулочні вироби", "1234567890126", 22.00),
                MockNomenclature("5", "005", "Печиво овсяне", "упак", "Печиво з вівсяних пластівців", "Хлібобулочні вироби", "1234567890127", 45.00)
            ),
            "2" to listOf( // Молочні продукти
                MockNomenclature("6", "006", "Молоко 1л", "шт", "Молоко пастеризоване", "Молочні продукти", "1234567890128", 45.00),
                MockNomenclature("7", "007", "Сир твердий", "кг", "Сир твердий 50%", "Молочні продукти", "1234567890129", 180.00),
                MockNomenclature("8", "008", "Йогурт натуральний", "шт", "Йогурт без добавок", "Молочні продукти", "1234567890130", 35.00),
                MockNomenclature("9", "009", "Масло вершкове", "кг", "Масло вершкове 82.5%", "Молочні продукти", "1234567890131", 120.00),
                MockNomenclature("10", "010", "Сметана 20%", "шт", "Сметана 20% жирності", "Молочні продукти", "1234567890132", 55.00)
            ),
            "3" to listOf( // М'ясо та ковбаси
                MockNomenclature("11", "011", "Ковбаса варена", "кг", "Ковбаса варена вища сорт", "М'ясо та ковбаси", "1234567890133", 250.00),
                MockNomenclature("12", "012", "Сосиски молочні", "кг", "Сосиски молочні", "М'ясо та ковбаси", "1234567890134", 180.00),
                MockNomenclature("13", "013", "Бекон", "кг", "Бекон копчений", "М'ясо та ковбаси", "1234567890135", 320.00),
                MockNomenclature("14", "014", "Куряче філе", "кг", "Філе куряче свіже", "М'ясо та ковбаси", "1234567890136", 150.00),
                MockNomenclature("15", "015", "Свинина", "кг", "Свинина свіжа", "М'ясо та ковбаси", "1234567890137", 200.00)
            ),
            "4" to listOf( // Овочі та фрукти
                MockNomenclature("16", "016", "Картопля", "кг", "Картопля свіжа", "Овочі та фрукти", "1234567890138", 25.00),
                MockNomenclature("17", "017", "Морква", "кг", "Морква свіжа", "Овочі та фрукти", "1234567890139", 30.00),
                MockNomenclature("18", "018", "Яблука", "кг", "Яблука свіжі", "Овочі та фрукти", "1234567890140", 40.00),
                MockNomenclature("19", "019", "Банани", "кг", "Банани свіжі", "Овочі та фрукти", "1234567890141", 60.00),
                MockNomenclature("20", "020", "Помідори", "кг", "Помідори свіжі", "Овочі та фрукти", "1234567890142", 80.00)
            )
        )
    }
    
    // Используем реальные данные или mock данные для Preview
    val items = if (nomenclature.isNotEmpty()) {
        nomenclature
    } else {
        mockItemsByCategory[categoryId] ?: emptyList()
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
                text = categoryName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Поиск
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Поиск товаров") },
            placeholder = { Text("Название, код или штрих-код") },
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
        
        // Список товаров
        val filteredItems = if (searchQuery.isEmpty()) {
            items
        } else {
            items.filter { item ->
                when (item) {
                    is com.sh.an.tsd.data.model.Nomenclature -> {
                        item.name.contains(searchQuery, ignoreCase = true) ||
                        item.code.contains(searchQuery, ignoreCase = true) ||
                        item.descriptionRu?.contains(searchQuery, ignoreCase = true) == true ||
                        item.descriptionUa?.contains(searchQuery, ignoreCase = true) == true
                    }
                    is MockNomenclature -> {
                        item.name.contains(searchQuery, ignoreCase = true) ||
                        item.code.contains(searchQuery, ignoreCase = true) ||
                        item.description?.contains(searchQuery, ignoreCase = true) == true ||
                        item.barcode?.contains(searchQuery, ignoreCase = true) == true
                    }
                    else -> false
                }
            }
        }
        
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.SearchOff,
                        contentDescription = "Не найдено",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Товари не знайдені",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Спробуйте змінити пошуковий запит",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredItems,
                    key = { item -> 
                        when (item) {
                            is com.sh.an.tsd.data.model.Nomenclature -> "real_${item.id}"
                            is MockNomenclature -> "mock_${item.id}"
                            else -> "unknown_${item.hashCode()}"
                        }
                    }
                ) { item ->
                    NomenclatureItemCard(
                        nomenclature = item,
                        onItemClick = { 
                            when (item) {
                                is com.sh.an.tsd.data.model.Nomenclature -> onItemClick(item)
                                is MockNomenclature -> {
                                    // Для mock данных создаем реальную модель
                                    val realNomenclature = com.sh.an.tsd.data.model.Nomenclature(
                                        id = item.id.toInt(),
                                        code = item.code,
                                        categoryId = 1,
                                        name = item.name,
                                        baseUnitId = 1,
                                        descriptionRu = item.description,
                                        descriptionUa = null,
                                        isActive = true,
                                        createdAt = "2025-01-01T00:00:00",
                                        updatedAt = null
                                    )
                                    onItemClick(realNomenclature)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NomenclatureItemCard(
    nomenclature: Any, // Может быть как реальной моделью, так и mock моделью
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onItemClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (nomenclature) {
                        is com.sh.an.tsd.data.model.Nomenclature -> nomenclature.code
                        is MockNomenclature -> nomenclature.code
                        else -> ""
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                when (nomenclature) {
                    is MockNomenclature -> {
                        Text(
                            text = "${nomenclature.price} ₴",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (nomenclature) {
                    is com.sh.an.tsd.data.model.Nomenclature -> nomenclature.name
                    is MockNomenclature -> nomenclature.name
                    else -> ""
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            when (nomenclature) {
                is com.sh.an.tsd.data.model.Nomenclature -> {
                    nomenclature.descriptionRu?.let { description ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is MockNomenclature -> {
                    nomenclature.description?.let { description ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Straighten,
                        contentDescription = "Единица измерения",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (nomenclature) {
                            is com.sh.an.tsd.data.model.Nomenclature -> nomenclature.baseUnit?.shortName ?: "шт"
                            is MockNomenclature -> nomenclature.unit
                            else -> "шт"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                when (nomenclature) {
                    is MockNomenclature -> {
                        nomenclature.barcode?.let { barcode ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.QrCode,
                                    contentDescription = "Штрих-код",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = barcode,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Локальная модель для mock данных
data class MockNomenclature(
    val id: String,
    val code: String,
    val name: String,
    val unit: String,
    val description: String?,
    val category: String,
    val barcode: String?,
    val price: Double
)

@Preview(showBackground = true)
@Composable
fun NomenclatureItemsScreenPreview() {
    TsdTheme {
        NomenclatureItemsScreen(
            categoryId = "1",
            categoryName = "Хлібобулочні вироби"
        )
    }
}
