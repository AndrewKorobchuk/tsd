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
import com.sh.an.tsd.data.model.DocumentType
import com.sh.an.tsd.data.model.Warehouse
import com.sh.an.tsd.data.model.DocumentItem
import com.sh.an.tsd.data.model.Nomenclature
import com.sh.an.tsd.data.model.UnitOfMeasure
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCreateScreen(
    documentType: DocumentType,
    warehouses: List<Warehouse>,
    documentItems: List<DocumentItem>,
    nomenclature: List<Nomenclature>,
    units: List<UnitOfMeasure>,
    isLoading: Boolean,
    errorMessage: String?,
    documentNumber: String,
    selectedWarehouse: Warehouse?,
    documentDate: Date,
    description: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onEditItemClick: (DocumentItem) -> Unit,
    onDeleteItemClick: (DocumentItem) -> Unit,
    onClearError: () -> Unit,
    onDocumentNumberChange: (String) -> Unit,
    onWarehouseChange: (Warehouse?) -> Unit,
    onDateChange: (Date) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
                
                Text(
                    text = when (documentType) {
                        DocumentType.STOCK_INPUT -> "Создание документа: Ввод остатков"
                        DocumentType.RECEIPT -> "Создание документа: Приход"
                        DocumentType.EXPENSE -> "Создание документа: Расход"
                        DocumentType.TRANSFER -> "Создание документа: Перемещение"
                        DocumentType.INVENTORY -> "Создание документа: Инвентаризация"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(
                onClick = onSaveClick,
                enabled = selectedWarehouse != null && documentItems.isNotEmpty()
            ) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = "Сохранить",
                    tint = if (selectedWarehouse != null && documentItems.isNotEmpty()) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Показать ошибку
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
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Форма документа
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Номер документа
                OutlinedTextField(
                    value = documentNumber,
                    onValueChange = onDocumentNumberChange,
                    label = { Text("Номер документа") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                // Склад
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedWarehouse?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Склад *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        warehouses.forEach { warehouse ->
                            DropdownMenuItem(
                                text = { Text(warehouse.name) },
                                onClick = {
                                    onWarehouseChange(warehouse)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                // Дата документа
                OutlinedTextField(
                    value = dateFormatter.format(documentDate),
                    onValueChange = { },
                    label = { Text("Дата документа") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { /* TODO: Добавить выбор даты */ }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Выбрать дату")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                // Описание
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
            
            item {
                // Заголовок строк документа
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Строки документа",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Button(
                        onClick = onAddItemClick,
                        enabled = selectedWarehouse != null
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Добавить")
                    }
                }
            }
            
            // Строки документа
            items(documentItems) { item ->
                DocumentItemCard(
                    item = item,
                    nomenclature = nomenclature.find { it.id == item.nomenclatureId },
                    unit = units.find { it.id == item.unitId },
                    onEditClick = { onEditItemClick(item) },
                    onDeleteClick = { onDeleteItemClick(item) },
                    enabled = true
                )
            }
            
            if (documentItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Нет строк документа",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Нажмите 'Добавить' для добавления номенклатуры",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Показать индикатор загрузки
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}