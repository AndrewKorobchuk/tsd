package com.sh.an.tsd.ui.settings

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
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(getSettingsItems()) { item ->
            SettingsItem(
                item = item,
                onItemClick = { /* TODO: Обработать нажатие */ }
            )
        }
    }
}

@Composable
fun SettingsItem(
    item: SettingsItem,
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
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (item.hasSwitch) {
                Switch(
                    checked = item.switchState,
                    onCheckedChange = { /* TODO: Обработать изменение */ }
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Открыть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class SettingsItem(
    val id: String,
    val title: String,
    val description: String = "",
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val hasSwitch: Boolean = false,
    val switchState: Boolean = false
)

fun getSettingsItems(): List<SettingsItem> {
    return listOf(
        SettingsItem(
            id = "1",
            title = "Підключення до сервера",
            description = "Настройки подключения к серверу",
            icon = Icons.Filled.CloudSync
        ),
        SettingsItem(
            id = "2",
            title = "Синхронізація",
            description = "Настройки синхронизации данных",
            icon = Icons.Filled.Sync
        ),
        SettingsItem(
            id = "3",
            title = "Автоматичне оновлення",
            description = "Автоматическое обновление данных",
            icon = Icons.Filled.Update,
            hasSwitch = true,
            switchState = true
        ),
        SettingsItem(
            id = "4",
            title = "Звукові сигнали",
            description = "Звуковые уведомления",
            icon = Icons.Filled.VolumeUp,
            hasSwitch = true,
            switchState = false
        ),
        SettingsItem(
            id = "5",
            title = "Вібрація",
            description = "Виброотклик при сканировании",
            icon = Icons.Filled.Vibration,
            hasSwitch = true,
            switchState = true
        ),
        SettingsItem(
            id = "6",
            title = "Темна тема",
            description = "Использование темной темы",
            icon = Icons.Filled.DarkMode,
            hasSwitch = true,
            switchState = false
        ),
        SettingsItem(
            id = "7",
            title = "Мова інтерфейсу",
            description = "Українська",
            icon = Icons.Filled.Language
        ),
        SettingsItem(
            id = "8",
            title = "Про програму",
            description = "Версия 1.0.0",
            icon = Icons.Filled.Info
        ),
        SettingsItem(
            id = "9",
            title = "Вийти",
            description = "Выйти из системы",
            icon = Icons.Filled.ExitToApp
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TsdTheme {
        SettingsScreen()
    }
}
