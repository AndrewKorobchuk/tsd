package com.sh.an.tsd.ui.units

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
import com.sh.an.tsd.data.model.UnitOfMeasure
import com.sh.an.tsd.ui.theme.TsdTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsScreen(
    units: List<UnitOfMeasure>,
    isLoading: Boolean,
    errorMessage: String?,
    lastSyncTime: String?,
    localUnitsCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок с кнопкой "Назад"
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
                text = "Одиниці виміру",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Информация о синхронизации
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
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
                        text = "Локальних записів: $localUnitsCount",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (lastSyncTime != null) {
                        Text(
                            text = "Оновлено: ${formatDateTime(lastSyncTime)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }


        // Отображение ошибки
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClearError) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Закрити",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Поле поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                onSearchQueryChange(it)
            },
            label = { Text("Пошук одиниць виміру") },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        searchQuery = ""
                        onSearchQueryChange("")
                    }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Очистити")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        // Список единиц измерения
        if (units.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Нічого не знайдено" else "Немає одиниць виміру",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (searchQuery.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Натисніть 'Завантажити довідники' для завантаження даних",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(units) { unit ->
                    UnitOfMeasureCard(unit = unit)
                }
            }
        }
    }
}

@Composable
fun UnitOfMeasureCard(unit: UnitOfMeasure) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = unit.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = unit.shortName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Код: ${unit.code}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (!unit.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = unit.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDateTime(dateTimeString: String): String {
    return try {
        // Простое форматирование даты для отображения
        // В реальном приложении лучше использовать DateFormatter
        dateTimeString.substring(0, 16).replace("T", " ")
    } catch (e: Exception) {
        dateTimeString
    }
}

@Preview(showBackground = true)
@Composable
fun UnitsScreenPreview() {
    TsdTheme {
        UnitsScreen(
            units = listOf(
                UnitOfMeasure(
                    id = 1,
                    code = "kg",
                    name = "Кілограм",
                    shortName = "кг",
                    description = "Основна одиниця виміру маси в системі СІ",
                    isActive = true,
                    createdAt = "2025-10-12T17:57:42",
                    updatedAt = null
                ),
                UnitOfMeasure(
                    id = 2,
                    code = "g",
                    name = "Грам",
                    shortName = "г",
                    description = "Одиниця виміру маси, рівна 1/1000 кілограма",
                    isActive = true,
                    createdAt = "2025-10-12T17:57:42",
                    updatedAt = null
                )
            ),
            isLoading = false,
            errorMessage = null,
            lastSyncTime = "2025-10-12T17:57:42",
            localUnitsCount = 2,
            onSearchQueryChange = {},
            onClearError = {},
            onBackClick = {}
        )
    }
}
