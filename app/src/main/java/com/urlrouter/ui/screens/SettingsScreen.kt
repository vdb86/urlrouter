package com.urlrouter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.urlrouter.ui.MainViewModel
import com.urlrouter.ui.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("URL Router") }) }) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.fillMaxSize()) {
            item { SettingsItem(Icons.Default.Language, "Browser Management", "Manage and reorder installed browsers") { onNavigate(NavRoutes.BROWSER_MANAGEMENT) } }
            item { SettingsItem(Icons.AutoMirrored.Filled.List, "Routing Rules", "Configure URL routing rules") { onNavigate(NavRoutes.ROUTING_RULES) } }
            item { SettingsItem(Icons.Default.Palette, "Appearance", "Customize the browser chooser") { onNavigate(NavRoutes.APPEARANCE) } }
            item { SettingsItem(Icons.Default.BugReport, "Diagnostics", "Test URL routing rules") { onNavigate(NavRoutes.DIAGNOSTICS) } }
            item { SettingsItem(Icons.Default.ImportExport, "Import / Export", "Backup and restore configuration") { onNavigate(NavRoutes.IMPORT_EXPORT) } }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
}
