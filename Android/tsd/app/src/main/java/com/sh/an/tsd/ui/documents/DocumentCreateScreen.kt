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
import com.sh.an.tsd.data.model.Document
import com.sh.an.tsd.data.model.DocumentItem
import com.sh.an.tsd.data.model.DocumentType
import com.sh.an.tsd.data.model.DocumentStatus
import com.sh.an.tsd.data.model.Warehouse
import com.sh.an.tsd.data.model.Nomenclature
import com.sh.an.tsd.data.model.UnitOfMeasure
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCreateScreen(
    documentType: DocumentType,
    warehouses: List<Warehouse>,
    nomenclature: List<Nomenclature>,
    units: List<UnitOfMeasure>,
    documentItems: List<DocumentItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onSaveClick: (Document) -> Unit,
    onAddItemClick: () -> Unit,
    onEditItemClick: (DocumentItem) -> Unit,
    onDeleteItemClick: (DocumentItem) -> Unit,
    onClearError: () -> Unit
) {
    var documentNumber by remember { mutableStateOf("") }
    var selectedWarehouse by remember { mutableStateOf<Warehouse?>(null) }
    var documentDate by remember { mutableStateOf(getCurrentDate()) }
    var description by remember { mutableStateOf("") }
    var showWarehouseDialog by remember { mutableStateOf(false) }

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
                text = "Створити документ",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                TextButton(
                    onClick = onBackClick,
                    enabled = !isLoading
                ) {
                    Text("Скасувати")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        if (selectedWarehouse != null && documentNumber.isNotBlank() && documentItems.isNotEmpty()) {
                            val document = Document(
                                id = 0, // Будет установлен сервером
                                documentType = documentType.value,
                                documentNumber = documentNumber,
                                warehouseId = selectedWarehouse!!.id,
                                date = documentDate,
                                status = DocumentStatus.DRAFT.value,
                                createdBy = 0, // Будет установлен сервером
                                description = description.takeIf { it.isNotBlank() },
                                createdAt = getCurrentDateTime(),
                                updatedAt = null
                            )
                            onSaveClick(document)
                        }
                    },
                    enabled = !isLoading && selectedWarehouse != null && documentNumber.isNotBlank() && documentItems.isNotEmpty()
                ) {
                    Text("Зберегти")
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Основная информация о документе
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Основна інформація",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Номер документа
                        OutlinedTextField(
                            value = documentNumber,
                            onValueChange = { documentNumber = it },
                            label = { Text("Номер документа") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Склад
                        OutlinedTextField(
                            value = selectedWarehouse?.name ?: "",
                            onValueChange = { },
                            label = { Text("Склад") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showWarehouseDialog = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Вибрати склад")
                                }
                            },
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Дата документа
                        OutlinedTextField(
                            value = documentDate,
                            onValueChange = { documentDate = it },
                            label = { Text("Дата документа") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Описание
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Опис (необов'язково)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            enabled = !isLoading
                        )
                    }
                }
            }
            
            item {
                // Строки документа
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Строки документа",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Button(
                                onClick = onAddItemClick,
                                enabled = !isLoading && selectedWarehouse != null
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Додати")
                            }
                        }
                        
                        if (documentItems.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Додайте строки до документа",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            documentItems.forEach { item ->
                                DocumentItemCard(
                                    item = item,
                                    nomenclature = nomenclature.find { n -> n.id == item.nomenclatureId },
                                    unit = units.find { u -> u.id == item.unitId },
                                    onEditClick = { onEditItemClick(item) },
                                    onDeleteClick = { onDeleteItemClick(item) },
                                    enabled = !isLoading
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог выбора склада
    if (showWarehouseDialog) {
        AlertDialog(
            onDismissRequest = { showWarehouseDialog = false },
            title = { Text("Вибрати склад") },
            text = {
                Column {
                    warehouses.forEach { warehouse ->
                        TextButton(
                            onClick = {
                                selectedWarehouse = warehouse
                                showWarehouseDialog = false
                            }
                        ) {
                            Text(warehouse.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWarehouseDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
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
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Кількість: ${item.quantity} ${unit?.shortName ?: ""}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
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

private fun getCurrentDate(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(Date())
}

private fun getCurrentDateTime(): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    return format.format(Date())
}
