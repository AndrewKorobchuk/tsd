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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.data.model.Document
import com.sh.an.tsd.data.model.DocumentType
import com.sh.an.tsd.data.model.DocumentStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documents: List<Document>,
    isLoading: Boolean,
    errorMessage: String?,
    selectedDocumentType: DocumentType?,
    selectedStatus: DocumentStatus?,
    onDocumentClick: (Document) -> Unit,
    onFilterByType: (DocumentType?) -> Unit,
    onFilterByStatus: (DocumentStatus?) -> Unit,
    onSyncClick: () -> Unit,
    onClearFilters: () -> Unit,
    onClearError: () -> Unit,
    onBackClick: () -> Unit
) {
    var showTypeFilter by remember { mutableStateOf(false) }
    var showStatusFilter by remember { mutableStateOf(false) }

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
                text = "Документи",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = { showTypeFilter = true }) {
                    Icon(
                        Icons.Filled.FilterList,
                        contentDescription = "Фільтр по типу",
                        tint = if (selectedDocumentType != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                IconButton(onClick = { showStatusFilter = true }) {
                    Icon(
                        Icons.Filled.FilterAlt,
                        contentDescription = "Фільтр по статусу",
                        tint = if (selectedStatus != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                IconButton(onClick = onSyncClick) {
                    Icon(
                        Icons.Filled.Sync,
                        contentDescription = "Синхронізація"
                    )
                }
            }
        }

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

        // Активные фильтры
        if (selectedDocumentType != null || selectedStatus != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Активні фільтри:",
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = onClearFilters) {
                    Text("Очистити")
                }
            }
        }

        // Список документов
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (documents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Документи не знайдені",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Натисніть кнопку синхронізації для завантаження",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onSyncClick
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Завантажити документи")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(documents) { document ->
                    DocumentCard(
                        document = document,
                        onClick = { onDocumentClick(document) }
                    )
                }
            }
        }
    }

    // Диалог фильтра по типу документа
    if (showTypeFilter) {
        AlertDialog(
            onDismissRequest = { showTypeFilter = false },
            title = { Text("Фільтр по типу документа") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            onFilterByType(null)
                            showTypeFilter = false
                        }
                    ) {
                        Text("Всі типи")
                    }
                    DocumentType.values().forEach { type ->
                        TextButton(
                            onClick = {
                                onFilterByType(type)
                                showTypeFilter = false
                            }
                        ) {
                            Text(getDocumentTypeDisplayName(type))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTypeFilter = false }) {
                    Text("Скасувати")
                }
            }
        )
    }

    // Диалог фильтра по статусу
    if (showStatusFilter) {
        AlertDialog(
            onDismissRequest = { showStatusFilter = false },
            title = { Text("Фільтр по статусу") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            onFilterByStatus(null)
                            showStatusFilter = false
                        }
                    ) {
                        Text("Всі статуси")
                    }
                    DocumentStatus.values().forEach { status ->
                        TextButton(
                            onClick = {
                                onFilterByStatus(status)
                                showStatusFilter = false
                            }
                        ) {
                            Text(getDocumentStatusDisplayName(status))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusFilter = false }) {
                    Text("Скасувати")
                }
            }
        )
    }
}

@Composable
fun DocumentCard(
    document: Document,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
                    text = document.documentNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = document.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = getDocumentTypeDisplayName(DocumentType.fromString(document.documentType)),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatDate(document.date),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            document.description?.let { description ->
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val statusEnum = DocumentStatus.fromString(status)
    val (backgroundColor, textColor) = when (statusEnum) {
        DocumentStatus.DRAFT -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        DocumentStatus.POSTED -> Color(0xFF4CAF50) to Color.White
        DocumentStatus.CANCELLED -> Color(0xFFF44336) to Color.White
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = getDocumentStatusDisplayName(statusEnum),
            color = textColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getDocumentTypeDisplayName(type: DocumentType): String {
    return when (type) {
        DocumentType.RECEIPT -> "Приход"
        DocumentType.EXPENSE -> "Расход"
        DocumentType.TRANSFER -> "Перемещение"
        DocumentType.INVENTORY -> "Инвентаризация"
        DocumentType.STOCK_INPUT -> "Ввод остатков"
    }
}

private fun getDocumentStatusDisplayName(status: DocumentStatus): String {
    return when (status) {
        DocumentStatus.DRAFT -> "Черновик"
        DocumentStatus.POSTED -> "Проведен"
        DocumentStatus.CANCELLED -> "Отменен"
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}