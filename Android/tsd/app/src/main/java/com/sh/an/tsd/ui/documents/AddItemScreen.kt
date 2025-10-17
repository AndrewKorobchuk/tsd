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
import com.sh.an.tsd.data.model.*

@Composable
fun AddItemScreen(
    nomenclature: List<Nomenclature>,
    units: List<UnitOfMeasure>,
    barcodes: List<Barcode>,
    onBackClick: () -> Unit,
    onItemAdd: (DocumentItem) -> Unit,
    onBarcodeScan: () -> Unit,
    onManualAdd: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf<AddMethod?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredBarcodes by remember { mutableStateOf(barcodes) }
    
    // Фильтрация штрих-кодов при изменении поискового запроса
    LaunchedEffect(searchQuery) {
        filteredBarcodes = if (searchQuery.isBlank()) {
            barcodes
        } else {
            barcodes.filter { barcode ->
                barcode.barcode.contains(searchQuery, ignoreCase = true) ||
                barcode.nomenclatureName?.contains(searchQuery, ignoreCase = true) == true
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
            Text(
                text = "Добавить товар",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.Close, contentDescription = "Закрыть")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedMethod) {
            null -> {
                // Выбор способа добавления
                MethodSelectionScreen(
                    onBarcodeScanClick = { selectedMethod = AddMethod.BARCODE_SCAN },
                    onManualAddClick = { selectedMethod = AddMethod.MANUAL_ADD }
                )
            }
            
            AddMethod.BARCODE_SCAN -> {
                // Непрерывное сканирование штрих-кодов
                BarcodeScanScreen(
                    barcodes = filteredBarcodes,
                    onBackClick = { selectedMethod = null },
                    onBarcodeScanned = { barcode ->
                        // TODO: Добавить логику обработки отсканированного штрих-кода
                        onItemAdd(createDocumentItemFromBarcode(barcode))
                    }
                )
            }
            
            AddMethod.MANUAL_ADD -> {
                // Ручное добавление с автодополнением
                ManualAddScreen(
                    nomenclature = nomenclature,
                    units = units,
                    barcodes = filteredBarcodes,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onBackClick = { selectedMethod = null },
                    onItemAdd = onItemAdd
                )
            }
        }
    }
}

@Composable
private fun MethodSelectionScreen(
    onBarcodeScanClick: () -> Unit,
    onManualAddClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Выберите способ добавления товара",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Кнопка сканирования штрих-кодов
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            onClick = onBarcodeScanClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Сканирование штрих-кодов",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Непрерывное сканирование",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Кнопка ручного добавления
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            onClick = onManualAddClick
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ручное добавление",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "С автодополнением",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BarcodeScanScreen(
    barcodes: List<Barcode>,
    onBackClick: () -> Unit,
    onBarcodeScanned: (Barcode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Сканирование штрих-кодов",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // TODO: Здесь будет реализован интерфейс сканирования
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Сканирование штрих-кодов",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Наведите камеру на штрих-код",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Список доступных штрих-кодов
        Text(
            text = "Доступные штрих-коды:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(barcodes) { barcode ->
                BarcodeItemCard(
                    barcode = barcode,
                    onClick = { onBarcodeScanned(barcode) }
                )
            }
        }
    }
}

@Composable
private fun ManualAddScreen(
    nomenclature: List<Nomenclature>,
    units: List<UnitOfMeasure>,
    barcodes: List<Barcode>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onItemAdd: (DocumentItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ручное добавление",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Поле поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Поиск по наименованию или штрих-коду") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Список результатов поиска
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // Показываем штрих-коды, если есть поисковый запрос
            if (searchQuery.isNotBlank()) {
                items(barcodes) { barcode ->
                    BarcodeItemCard(
                        barcode = barcode,
                        onClick = { onItemAdd(createDocumentItemFromBarcode(barcode)) }
                    )
                }
            }
            
            // Показываем номенклатуру
            items(nomenclature) { item ->
                NomenclatureItemCard(
                    nomenclature = item,
                    units = units,
                    onClick = { nomenclature, unit ->
                        onItemAdd(createDocumentItemFromNomenclature(nomenclature, unit))
                    }
                )
            }
        }
    }
}

@Composable
private fun BarcodeItemCard(
    barcode: Barcode,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = barcode.nomenclatureName ?: "Неизвестная номенклатура",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Штрих-код: ${barcode.barcode}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Единица: ${barcode.unitShortName ?: barcode.unitName}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NomenclatureItemCard(
    nomenclature: Nomenclature,
    units: List<UnitOfMeasure>,
    onClick: (Nomenclature, UnitOfMeasure) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { 
            // Берем первую доступную единицу измерения
            val defaultUnit = units.firstOrNull() ?: return@Card
            onClick(nomenclature, defaultUnit)
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = nomenclature.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Код: ${nomenclature.code}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private enum class AddMethod {
    BARCODE_SCAN,
    MANUAL_ADD
}

private fun createDocumentItemFromBarcode(barcode: Barcode): DocumentItem {
    return DocumentItem(
        id = 0, // Временный ID
        documentId = 0, // Будет установлен при сохранении документа
        nomenclatureId = barcode.nomenclatureId,
        quantity = 1.0, // По умолчанию количество 1
        unitId = barcode.unitId,
        price = 0.0,
        total = 0.0,
        description = null,
        createdAt = null,
        updatedAt = null
    )
}

private fun createDocumentItemFromNomenclature(nomenclature: Nomenclature, unit: UnitOfMeasure): DocumentItem {
    return DocumentItem(
        id = 0, // Временный ID
        documentId = 0, // Будет установлен при сохранении документа
        nomenclatureId = nomenclature.id,
        quantity = 1.0, // По умолчанию количество 1
        unitId = unit.id,
        price = 0.0,
        total = 0.0,
        description = null,
        createdAt = null,
        updatedAt = null
    )
}
