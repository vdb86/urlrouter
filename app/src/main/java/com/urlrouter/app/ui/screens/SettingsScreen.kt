package com.urlrouter.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.urlrouter.app.ui.MainViewModel
import com.urlrouter.app.ui.NavRoutes
import com.urlrouter.app.util.DefaultBrowser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("URL Router") }) }) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.fillMaxSize()) {
            item { DefaultBrowserCard() }
            item { SettingsItem(Icons.Default.Language, "Browser Management", "Manage and reorder installed browsers") { onNavigate(NavRoutes.BROWSER_MANAGEMENT) } }
            item { SettingsItem(Icons.AutoMirrored.Filled.List, "Routing Rules", "Configure URL routing rules") { onNavigate(NavRoutes.ROUTING_RULES) } }
            item { SettingsItem(Icons.Default.Palette, "Appearance", "Customize the browser chooser") { onNavigate(NavRoutes.APPEARANCE) } }
            item { SettingsItem(Icons.Default.BugReport, "Diagnostics", "Test URL routing rules") { onNavigate(NavRoutes.DIAGNOSTICS) } }
            item { SettingsItem(Icons.Default.ImportExport, "Import / Export", "Backup and restore configuration") { onNavigate(NavRoutes.IMPORT_EXPORT) } }
            item { SettingsItem(Icons.Default.Info, "About", "Dedication, licence, privacy, features") { onNavigate(NavRoutes.ABOUT) } }
        }
    }
}

// ---- Default browser card ----

@Composable
fun DefaultBrowserCard() {
    val context = LocalContext.current
    var isDefault by remember { mutableStateOf(DefaultBrowser.isDefault(context)) }

    // The role dialog returns through this launcher; re-check when it closes.
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { isDefault = DefaultBrowser.isDefault(context) }

    // The Settings fallback returns no useful result, so also re-check on resume
    // (covers returning from the system settings screen).
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) isDefault = DefaultBrowser.isDefault(context)
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    val containerColor = if (isDefault)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isDefault)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDefault) Icons.Default.CheckCircle else Icons.Default.Language,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isDefault)
                        "URL Router is your default browser"
                    else
                        "Set URL Router as your default browser",
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
            }
            if (!isDefault) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Links only route through URL Router once it is set as your default browser.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { DefaultBrowser.requestIntent(context)?.let(launcher::launch) }) {
                    Text("Set as default browser")
                }
            }
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
