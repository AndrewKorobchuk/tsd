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
import com.sh.an.tsd.data.model.Nomenclature
import com.sh.an.tsd.data.model.UnitOfMeasure

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentItemAddScreen(
    nomenclature: List<Nomenclature>,
    units: List<UnitOfMeasure>,
    onSaveClick: (Int, Double, Int, String?) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedNomenclature by remember { mutableStateOf<Nomenclature?>(null) }
    var selectedUnit by remember { mutableStateOf<UnitOfMeasure?>(null) }
    var quantity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showNomenclatureDropdown by remember { mutableStateOf(false) }
    var showUnitsDropdown by remember { mutableStateOf(false) }

    // Фильтрация номенклатуры по поисковому запросу
    val filteredNomenclature = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            nomenclature
        } else {
            nomenclature.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true)
            }
        }
    }

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
                    text = "Добавить строку документа",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(
                onClick = {
                    if (selectedNomenclature != null && selectedUnit != null && quantity.isNotBlank()) {
                        val quantityValue = quantity.toDoubleOrNull()
                        if (quantityValue != null && quantityValue > 0) {
                            onSaveClick(
                                selectedNomenclature!!.id,
                                quantityValue,
                                selectedUnit!!.id,
                                description.takeIf { it.isNotBlank() }
                            )
                        }
                    }
                },
                enabled = selectedNomenclature != null && selectedUnit != null && 
                         quantity.isNotBlank() && quantity.toDoubleOrNull() != null && 
                         quantity.toDoubleOrNull()!! > 0
            ) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = "Сохранить",
                    tint = if (selectedNomenclature != null && selectedUnit != null && 
                              quantity.isNotBlank() && quantity.toDoubleOrNull() != null && 
                              quantity.toDoubleOrNull()!! > 0) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Поиск номенклатуры
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        showNomenclatureDropdown = true
                    },
                    label = { Text("Поиск номенклатуры") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                // Выбор номенклатуры
                ExposedDropdownMenuBox(
                    expanded = showNomenclatureDropdown,
                    onExpandedChange = { showNomenclatureDropdown = !showNomenclatureDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedNomenclature?.name ?: "",
                        onValueChange = { 
                            searchQuery = it
                            showNomenclatureDropdown = true
                        },
                        label = { Text("Номенклатура *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showNomenclatureDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        readOnly = false
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showNomenclatureDropdown,
                        onDismissRequest = { showNomenclatureDropdown = false }
                    ) {
                        if (filteredNomenclature.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Номенклатура не найдена") },
                                onClick = { }
                            )
                        } else {
                            filteredNomenclature.forEach { item ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.Medium)
                                            Text(
                                                text = "Код: ${item.code}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedNomenclature = item
                                        selectedUnit = units.find { it.id == item.baseUnitId }
                                        showNomenclatureDropdown = false
                                        searchQuery = item.name
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // Выбор единицы измерения
                ExposedDropdownMenuBox(
                    expanded = showUnitsDropdown,
                    onExpandedChange = { showUnitsDropdown = !showUnitsDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedUnit?.name ?: "",
                        onValueChange = { },
                        label = { Text("Единица измерения *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitsDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        readOnly = true
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showUnitsDropdown,
                        onDismissRequest = { showUnitsDropdown = false }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(unit.name)
                                        Text(
                                            text = "Краткое: ${unit.shortName}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedUnit = unit
                                    showUnitsDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                // Количество
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Количество *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (quantity.isNotBlank()) {
                            val qty = quantity.toDoubleOrNull()
                            if (qty == null) {
                                Text("Введите корректное число", color = MaterialTheme.colorScheme.error)
                            } else if (qty <= 0) {
                                Text("Количество должно быть больше 0", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                )
            }
            
            item {
                // Описание
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        }
    }
}