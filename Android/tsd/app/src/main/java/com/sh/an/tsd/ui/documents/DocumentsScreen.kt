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
        
        // Список документов
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getSampleDocuments()) { document ->
                DocumentCard(
                    document = document,
                    onItemClick = { /* TODO: Открыть документ */ }
                )
            }
        }
    }
}

@Composable
fun DocumentCard(
    document: Document,
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
                    text = document.number,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = document.date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = document.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (document.status) {
                        DocumentStatus.NEW -> Icons.Filled.FiberNew
                        DocumentStatus.IN_PROGRESS -> Icons.Filled.HourglassEmpty
                        DocumentStatus.COMPLETED -> Icons.Filled.CheckCircle
                    },
                    contentDescription = document.status.name,
                    tint = when (document.status) {
                        DocumentStatus.NEW -> MaterialTheme.colorScheme.primary
                        DocumentStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                        DocumentStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = document.status.displayName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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
