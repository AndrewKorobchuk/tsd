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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sh.an.tsd.data.model.DocumentType
import com.sh.an.tsd.ui.theme.TsdTheme

@Composable
fun DocumentsMainScreen(
    onStockInputClick: () -> Unit = {},
    onReceiptClick: () -> Unit = {},
    onExpenseClick: () -> Unit = {},
    onTransferClick: () -> Unit = {},
    onInventoryClick: () -> Unit = {},
    stockInputCount: Int = 0,
    receiptCount: Int = 0,
    expenseCount: Int = 0,
    transferCount: Int = 0,
    inventoryCount: Int = 0
) {
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
            label = { Text("Поиск в документах") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Список типов документов
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getDocumentTypes(stockInputCount, receiptCount, expenseCount, transferCount, inventoryCount)) { documentType ->
                DocumentTypeCard(
                    documentType = documentType,
                    onItemClick = { 
                        when (documentType.id) {
                            "stock_input" -> onStockInputClick()
                            "receipt" -> onReceiptClick()
                            "expense" -> onExpenseClick()
                            "transfer" -> onTransferClick()
                            "inventory" -> onInventoryClick()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DocumentTypeCard(
    documentType: DocumentTypeInfo,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onItemClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = documentType.icon,
                contentDescription = documentType.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = documentType.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = documentType.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Документов: ${documentType.itemCount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Открыть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class DocumentTypeInfo(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val itemCount: Int
)

fun getDocumentTypes(
    stockInputCount: Int = 0,
    receiptCount: Int = 0,
    expenseCount: Int = 0,
    transferCount: Int = 0,
    inventoryCount: Int = 0
): List<DocumentTypeInfo> {
    return listOf(
        DocumentTypeInfo(
            id = "stock_input",
            name = "Ввод остатков",
            description = "Документы для ввода начальных остатков товаров",
            icon = Icons.Filled.Input,
            itemCount = stockInputCount
        ),
        DocumentTypeInfo(
            id = "receipt",
            name = "Приход",
            description = "Документы поступления товаров на склад",
            icon = Icons.Filled.AddBox,
            itemCount = receiptCount
        ),
        DocumentTypeInfo(
            id = "expense",
            name = "Расход",
            description = "Документы списания товаров со склада",
            icon = Icons.Filled.RemoveCircle,
            itemCount = expenseCount
        ),
        DocumentTypeInfo(
            id = "transfer",
            name = "Перемещение",
            description = "Документы перемещения товаров между складами",
            icon = Icons.Filled.SwapHoriz,
            itemCount = transferCount
        ),
        DocumentTypeInfo(
            id = "inventory",
            name = "Инвентаризация",
            description = "Документы инвентаризации товаров на складе",
            icon = Icons.Filled.Inventory,
            itemCount = inventoryCount
        )
    )
}

@Preview(showBackground = true)
@Composable
fun DocumentsMainScreenPreview() {
    TsdTheme {
        DocumentsMainScreen(
            stockInputCount = 5,
            receiptCount = 12,
            expenseCount = 8,
            transferCount = 3,
            inventoryCount = 2
        )
    }
}

