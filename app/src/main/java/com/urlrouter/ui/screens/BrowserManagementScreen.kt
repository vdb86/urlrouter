package com.urlrouter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urlrouter.model.BrowserInfo
import com.urlrouter.ui.MainViewModel
import com.urlrouter.ui.components.BrowserIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserManagementScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val browsers by viewModel.browsers.collectAsStateWithLifecycle()
    val defaultBrowserPkg by viewModel.defaultBrowserPkg.collectAsStateWithLifecycle()
    val defaultBrowserEnabled by viewModel.defaultBrowserEnabled.collectAsStateWithLifecycle()
    var scanning by remember { mutableStateOf(false) }
    var defaultDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (browsers.isEmpty()) { scanning = true; viewModel.rescanBrowsers(); scanning = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browser Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { scanning = true; viewModel.rescanBrowsers(); scanning = false }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rescan")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (scanning) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(contentPadding = paddingValues, modifier = Modifier.fillMaxSize()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("Default Browser", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "When no routing rule matches, open links in this browser instead of showing the chooser.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable default browser", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = defaultBrowserEnabled,
                            onCheckedChange = { viewModel.setDefaultBrowser(defaultBrowserPkg, it) },
                            enabled = defaultBrowserPkg.isNotBlank()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = defaultDropdownExpanded,
                        onExpandedChange = { defaultDropdownExpanded = it }
                    ) {
                        val selectedLabel = browsers.firstOrNull { it.packageName == defaultBrowserPkg }?.label
                            ?: if (defaultBrowserPkg.isBlank()) "Not set" else defaultBrowserPkg
                        OutlinedTextField(
                            value = selectedLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Default browser") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = defaultDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = defaultDropdownExpanded, onDismissRequest = { defaultDropdownExpanded = false }) {
                            browsers.forEach { browser ->
                                DropdownMenuItem(
                                    text = { Text(browser.label) },
                                    onClick = { viewModel.setDefaultBrowser(browser.packageName, defaultBrowserEnabled); defaultDropdownExpanded = false }
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                Text("Installed Browsers", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            itemsIndexed(browsers, key = { _, b -> b.packageName }) { index, browser ->
                BrowserRow(
                    browser = browser,
                    isDefault = browser.packageName == defaultBrowserPkg && defaultBrowserEnabled,
                    canMoveUp = index > 0,
                    canMoveDown = index < browsers.size - 1,
                    onToggleEnabled = { viewModel.setBrowserEnabled(browser.packageName, it) },
                    onMoveUp = {
                        val m = browsers.toMutableList(); m.removeAt(index); m.add(index - 1, browser)
                        viewModel.reorderBrowsers(m)
                    },
                    onMoveDown = {
                        val m = browsers.toMutableList(); m.removeAt(index); m.add(index + 1, browser)
                        viewModel.reorderBrowsers(m)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
            }
        }
    }
}

@Composable
private fun BrowserRow(
    browser: BrowserInfo,
    isDefault: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BrowserIcon(packageName = browser.packageName, size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = browser.label, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (isDefault) {
                    Spacer(modifier = Modifier.width(6.dp))
                    AssistChip(onClick = {}, label = { Text("Default", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.height(22.dp))
                }
            }
            Text(text = browser.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onMoveUp, enabled = canMoveUp) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up") }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down") }
        }
        Switch(checked = browser.isEnabled, onCheckedChange = onToggleEnabled, modifier = Modifier.padding(start = 4.dp))
    }
}
