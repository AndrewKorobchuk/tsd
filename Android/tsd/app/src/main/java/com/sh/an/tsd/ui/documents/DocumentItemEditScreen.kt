package com.sh.an.tsd.ui.documents

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.data.model.DocumentItem
import com.sh.an.tsd.data.model.Nomenclature
import com.sh.an.tsd.data.model.UnitOfMeasure

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentItemEditScreen(
    nomenclature: List<Nomenclature>,
    units: List<UnitOfMeasure>,
    documentItems: List<DocumentItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onEditItemClick: (DocumentItem) -> Unit,
    onDeleteItemClick: (DocumentItem) -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Строки документа",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                TextButton(
                    onClick = onBackClick,
                    enabled = !isLoading
                ) {
                    Text("Назад")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onAddItemClick,
                    enabled = !isLoading
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Додати")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Показать ошибку
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
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

        // Список строк документа
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (documentItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Строки документа відсутні",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Натисніть 'Додати' для створення першої строки",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(documentItems) { item ->
                    DocumentItemCard(
                        item = item,
                        nomenclature = nomenclature.find { n -> n.id == item.nomenclatureId },
                        unit = units.find { u -> u.id == item.unitId },
                        onEditClick = { onEditItemClick(item) },
                        onDeleteClick = { onDeleteItemClick(item) },
                        enabled = !isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentItemCard(
    item: DocumentItem,
    nomenclature: Nomenclature?,
    unit: UnitOfMeasure?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nomenclature?.name ?: "Невідома номенклатура",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Кількість: ${item.quantity} ${unit?.shortName ?: ""}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                item.description?.let { description ->
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }
            
            Row {
                IconButton(
                    onClick = onEditClick,
                    enabled = enabled
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Редагувати")
                }
                
                IconButton(
                    onClick = onDeleteClick,
                    enabled = enabled
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Видалити")
                }
            }
        }
    }
}


