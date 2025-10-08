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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.ui.theme.TsdTheme

@Composable
fun DocumentsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Поиск
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Поиск документов") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Список типов документов
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getDocumentTypes()) { documentType ->
                DocumentTypeCard(
                    documentType = documentType,
                    onItemClick = { /* TODO: Открыть тип документа */ }
                )
            }
        }
    }
}

@Composable
fun DocumentTypeCard(
    documentType: DocumentType,
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
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка
            Card(
                modifier = Modifier.size(64.dp),
                colors = CardDefaults.cardColors(
                    containerColor = documentType.color.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = documentType.icon,
                        contentDescription = documentType.title,
                        tint = documentType.color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Текстовая информация
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = documentType.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = documentType.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Документів: ${documentType.documentCount}",
                    fontSize = 12.sp,
                    color = documentType.color,
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

data class DocumentType(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val documentCount: Int
)

data class Document(
    val id: String,
    val number: String,
    val description: String,
    val date: String,
    val status: DocumentStatus
)

enum class DocumentStatus(val displayName: String) {
    NEW("Новый"),
    IN_PROGRESS("В работе"),
    COMPLETED("Завершен")
}

fun getDocumentTypes(): List<DocumentType> {
    return listOf(
        DocumentType(
            id = "1",
            title = "Інвентаризація",
            description = "Проведення інвентаризації товарів на складі",
            icon = Icons.Filled.Inventory,
            color = androidx.compose.ui.graphics.Color(0xFF2196F3),
            documentCount = 5
        ),
        DocumentType(
            id = "2",
            title = "Прихідні накладні",
            description = "Документи приходу товарів на склад",
            icon = Icons.Filled.Input,
            color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
            documentCount = 12
        ),
        DocumentType(
            id = "3",
            title = "Видаткові накладні",
            description = "Документи видачі товарів зі складу",
            icon = Icons.Filled.Output,
            color = androidx.compose.ui.graphics.Color(0xFFFF9800),
            documentCount = 8
        ),
        DocumentType(
            id = "4",
            title = "Переміщення",
            description = "Документи переміщення між складами",
            icon = Icons.Filled.SwapHoriz,
            color = androidx.compose.ui.graphics.Color(0xFF9C27B0),
            documentCount = 3
        ),
        DocumentType(
            id = "5",
            title = "Списання",
            description = "Документи списання товарів",
            icon = Icons.Filled.Delete,
            color = androidx.compose.ui.graphics.Color(0xFFF44336),
            documentCount = 2
        ),
        DocumentType(
            id = "6",
            title = "Оприбуткування",
            description = "Документи оприбуткування товарів",
            icon = Icons.Filled.AddBox,
            color = androidx.compose.ui.graphics.Color(0xFF00BCD4),
            documentCount = 7
        )
    )
}

fun getSampleDocuments(): List<Document> {
    return listOf(
        Document(
            id = "1",
            number = "ПН-001",
            description = "Приходная накладная от поставщика",
            date = "15.01.2024",
            status = DocumentStatus.NEW
        ),
        Document(
            id = "2",
            number = "РН-002",
            description = "Расходная накладная на отгрузку",
            date = "14.01.2024",
            status = DocumentStatus.IN_PROGRESS
        ),
        Document(
            id = "3",
            number = "ПН-003",
            description = "Приходная накладная на склад",
            date = "13.01.2024",
            status = DocumentStatus.COMPLETED
        ),
        Document(
            id = "4",
            number = "ИН-004",
            description = "Инвентаризационная накладная",
            date = "12.01.2024",
            status = DocumentStatus.NEW
        )
    )
}

@Preview(showBackground = true)
@Composable
fun DocumentsScreenPreview() {
    TsdTheme {
        DocumentsScreen()
    }
}
