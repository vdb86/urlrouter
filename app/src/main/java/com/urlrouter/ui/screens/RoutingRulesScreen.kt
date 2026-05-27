package com.urlrouter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urlrouter.model.MatchType
import com.urlrouter.model.RoutingRule
import com.urlrouter.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutingRulesScreen(viewModel: MainViewModel, onBack: () -> Unit, onAddRule: () -> Unit, onEditRule: (Long) -> Unit) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val browsers by viewModel.browsers.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<RoutingRule?>(null) }

    deleteTarget?.let { rule ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Rule") },
            text = { Text("Delete rule for '${rule.pattern}'?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteRule(rule); deleteTarget = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routing Rules") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAddRule) { Icon(Icons.Default.Add, contentDescription = "Add rule") } }
    ) { paddingValues ->
        if (rules.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.Rule, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("No rules yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Tap + to add a routing rule", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Scaffold
        }

        LazyColumn(contentPadding = paddingValues, modifier = Modifier.fillMaxSize()) {
            items(rules, key = { it.id }) { rule ->
                val browser = browsers.firstOrNull { it.packageName == rule.browserPackage }
                RuleRow(
                    rule = rule,
                    browserLabel = browser?.label ?: rule.browserPackage,
                    isBrowserMissing = browser == null,
                    onToggleEnabled = { viewModel.setRuleEnabled(rule.id, it) },
                    onEdit = { onEditRule(rule.id) },
                    onDelete = { deleteTarget = rule }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}

@Composable
private fun RuleRow(rule: RoutingRule, browserLabel: String, isBrowserMissing: Boolean, onToggleEnabled: (Boolean) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text(matchTypeLabel(rule.matchType), style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.height(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(rule.pattern, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isBrowserMissing) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(browserLabel, style = MaterialTheme.typography.bodySmall,
                    color = if (isBrowserMissing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = rule.isEnabled, onCheckedChange = onToggleEnabled, modifier = Modifier.padding(start = 4.dp))
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
    }
}

private fun matchTypeLabel(type: MatchType) = when (type) {
    MatchType.EXACT_HOSTNAME -> "Exact"
    MatchType.WILDCARD_HOSTNAME -> "Wildcard"
    MatchType.PREFIX -> "Prefix"
    MatchType.REGEX -> "Regex"
    MatchType.CONTAINS -> "Contains"
}
