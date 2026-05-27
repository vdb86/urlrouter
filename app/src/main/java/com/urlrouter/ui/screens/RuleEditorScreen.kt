package com.urlrouter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urlrouter.model.MatchType
import com.urlrouter.model.RoutingRule
import com.urlrouter.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditorScreen(ruleId: Long, viewModel: MainViewModel, onBack: () -> Unit) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val browsers by viewModel.browsers.collectAsStateWithLifecycle()
    val existing = remember(ruleId, rules) { if (ruleId == -1L) null else rules.firstOrNull { it.id == ruleId } }

    var matchType by remember(existing) { mutableStateOf(existing?.matchType ?: MatchType.EXACT_HOSTNAME) }
    var pattern by remember(existing) { mutableStateOf(existing?.pattern ?: "") }
    var selectedBrowserPkg by remember(existing) { mutableStateOf(existing?.browserPackage ?: browsers.firstOrNull()?.packageName ?: "") }
    var isEnabled by remember(existing) { mutableStateOf(existing?.isEnabled ?: true) }
    var browserDropdownExpanded by remember { mutableStateOf(false) }
    var matchTypeDropdownExpanded by remember { mutableStateOf(false) }

    val isEditing = existing != null
    val canSave = pattern.isNotBlank() && selectedBrowserPkg.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Rule" else "Add Rule") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val rule = RoutingRule(
                                id = existing?.id ?: 0,
                                isEnabled = isEnabled,
                                matchType = matchType,
                                pattern = pattern.trim(),
                                browserPackage = selectedBrowserPkg,
                                createdAt = existing?.createdAt ?: System.currentTimeMillis()
                            )
                            if (isEditing) viewModel.updateRule(rule) else viewModel.addRule(rule)
                            onBack()
                        },
                        enabled = canSave
                    ) { Text("Save") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExposedDropdownMenuBox(expanded = matchTypeDropdownExpanded, onExpandedChange = { matchTypeDropdownExpanded = it }) {
                OutlinedTextField(
                    value = matchType.displayName(), onValueChange = {}, readOnly = true,
                    label = { Text("Match Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = matchTypeDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = matchTypeDropdownExpanded, onDismissRequest = { matchTypeDropdownExpanded = false }) {
                    MatchType.entries.forEach { type ->
                        DropdownMenuItem(text = { Text(type.displayName()) }, onClick = { matchType = type; matchTypeDropdownExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = pattern, onValueChange = { pattern = it },
                label = { Text("Pattern") },
                placeholder = { Text(matchType.placeholder()) },
                supportingText = { Text(matchType.hint()) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = browserDropdownExpanded, onExpandedChange = { browserDropdownExpanded = it }) {
                val selectedLabel = browsers.firstOrNull { it.packageName == selectedBrowserPkg }?.label ?: selectedBrowserPkg
                OutlinedTextField(
                    value = selectedLabel, onValueChange = {}, readOnly = true,
                    label = { Text("Browser") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = browserDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = browserDropdownExpanded, onDismissRequest = { browserDropdownExpanded = false }) {
                    browsers.forEach { browser ->
                        DropdownMenuItem(text = { Text(browser.label) }, onClick = { selectedBrowserPkg = browser.packageName; browserDropdownExpanded = false })
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rule Enabled", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
            }
        }
    }
}

private fun MatchType.displayName() = when (this) {
    MatchType.EXACT_HOSTNAME -> "Exact Hostname"
    MatchType.WILDCARD_HOSTNAME -> "Wildcard Hostname"
    MatchType.PREFIX -> "URL Prefix"
    MatchType.REGEX -> "Regular Expression"
    MatchType.CONTAINS -> "Contains"
}

private fun MatchType.placeholder() = when (this) {
    MatchType.EXACT_HOSTNAME -> "google.com"
    MatchType.WILDCARD_HOSTNAME -> "*.youtube.com"
    MatchType.PREFIX -> "https://youtube.com/watch"
    MatchType.REGEX -> ".*youtube\\.com.*"
    MatchType.CONTAINS -> "youtube"
}

private fun MatchType.hint() = when (this) {
    MatchType.EXACT_HOSTNAME -> "Matches the exact hostname"
    MatchType.WILDCARD_HOSTNAME -> "Matches subdomains (*.example.com)"
    MatchType.PREFIX -> "Matches URLs starting with this prefix"
    MatchType.REGEX -> "Full regex pattern matched against the URL"
    MatchType.CONTAINS -> "Matches URLs containing this text"
}
