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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.data.model.Nomenclature
import com.sh.an.tsd.data.repository.ApiResult
import com.sh.an.tsd.ui.theme.TsdTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NomenclatureScreen(
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var nomenclatureResult by remember { mutableStateOf<ApiResult<List<Nomenclature>>>(ApiResult.loading()) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Заглушка данных для демонстрации
    val sampleNomenclature = remember {
        listOf(
            Nomenclature(
                id = "1",
                code = "001",
                name = "Хліб білий",
                unit = "шт",
                description = "Свіжий білий хліб",
                category = "Хлібобулочні вироби",
                barcode = "1234567890123",
                price = 25.50
            ),
            Nomenclature(
                id = "2",
                code = "002",
                name = "Молоко 1л",
                unit = "шт",
                description = "Молоко пастеризоване",
                category = "Молочні продукти",
                barcode = "1234567890124",
                price = 45.00
            ),
            Nomenclature(
                id = "3",
                code = "003",
                name = "Яйця курячі",
                unit = "упак",
                description = "Яйця курячі свіжі",
                category = "Яйця",
                barcode = "1234567890125",
                price = 80.00
            ),
            Nomenclature(
                id = "4",
                code = "004",
                name = "Масло вершкове",
                unit = "кг",
                description = "Масло вершкове 82.5%",
                category = "Молочні продукти",
                barcode = "1234567890126",
                price = 120.00
            ),
            Nomenclature(
                id = "5",
                code = "005",
                name = "Цукор",
                unit = "кг",
                description = "Цукор білий кристалічний",
                category = "Крупи та цукор",
                barcode = "1234567890127",
                price = 35.00
            )
        )
    }
    
    // Инициализация данных
    LaunchedEffect(Unit) {
        nomenclatureResult = ApiResult.success(sampleNomenclature)
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
                text = "Номенклатура",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Поиск
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Поиск по названию или коду") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Очистить")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Список номенклатуры
        when (nomenclatureResult) {
            is ApiResult.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Загрузка номенклатуры...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is ApiResult.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Ошибка",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ошибка загрузки",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (nomenclatureResult as ApiResult.Error).message,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* TODO: Повторить загрузку */ }
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            }
            is ApiResult.Success -> {
                val successResult = nomenclatureResult as ApiResult.Success<List<Nomenclature>>
                val nomenclature = successResult.data
                val filteredNomenclature = if (searchQuery.isEmpty()) {
                    nomenclature
                } else {
                    nomenclature.filter { item ->
                        item.name.contains(searchQuery, ignoreCase = true) ||
                        item.code.contains(searchQuery, ignoreCase = true) ||
                        item.description?.contains(searchQuery, ignoreCase = true) == true
                    }
                }
                
                if (filteredNomenclature.isEmpty()) {
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
                                text = "Ничего не найдено",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (searchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Попробуйте изменить поисковый запрос",
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
                            items = filteredNomenclature,
                            key = { item -> item.id }
                        ) { item ->
                            NomenclatureCard(
                                nomenclature = item,
                                onItemClick = { /* TODO: Открыть детали */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NomenclatureCard(
    nomenclature: Nomenclature,
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
                    text = nomenclature.code,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (nomenclature.price != null) {
                    Text(
                        text = "${nomenclature.price} ₴",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = nomenclature.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (nomenclature.description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nomenclature.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                        text = nomenclature.unit,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (nomenclature.category != null) {
                    Text(
                        text = nomenclature.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (nomenclature.barcode != null) {
                Spacer(modifier = Modifier.height(4.dp))
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
                        text = nomenclature.barcode,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NomenclatureScreenPreview() {
    TsdTheme {
        NomenclatureScreen()
    }
}
