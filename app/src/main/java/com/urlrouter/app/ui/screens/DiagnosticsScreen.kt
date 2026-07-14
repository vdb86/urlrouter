package com.urlrouter.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.urlrouter.app.model.DiagnosticsResult
import com.urlrouter.app.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<DiagnosticsResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Paste a URL to see which rule would match and which browser would open it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it; result = null },
                label = { Text("URL") },
                placeholder = { Text("https://example.com/path") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (url.isNotEmpty()) {
                        IconButton(onClick = { url = ""; result = null }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            result = viewModel.diagnose(url.trim())
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = url.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Test URL")
                }
            }

            result?.let { r ->
                HorizontalDivider()
                DiagnosticsResultCard(r)
            }
        }
    }
}

@Composable
private fun DiagnosticsResultCard(result: DiagnosticsResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Result", style = MaterialTheme.typography.titleMedium)

            if (result.matchedRule != null) {
                DiagnosticsRow(
                    icon = Icons.Default.CheckCircle,
                    iconColor = MaterialTheme.colorScheme.primary,
                    label = "Matched Rule",
                    value = "#${result.matchedRule.id}: ${result.matchedRule.pattern}"
                )
                DiagnosticsRow(
                    icon = Icons.Default.Category,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    label = "Match Type",
                    value = result.matchType?.name ?: "-"
                )
                DiagnosticsRow(
                    icon = Icons.Default.Info,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    label = "Reason",
                    value = result.matchReason
                )
                DiagnosticsRow(
                    icon = Icons.Default.Language,
                    iconColor = MaterialTheme.colorScheme.primary,
                    label = "Browser",
                    value = result.browserLabel ?: result.browserPackage ?: "Unknown"
                )
            } else {
                DiagnosticsRow(
                    icon = Icons.Default.Info,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "Result",
                    value = result.matchReason
                )
                if (result.browserLabel != null) {
                    DiagnosticsRow(
                        icon = Icons.Default.Language,
                        iconColor = MaterialTheme.colorScheme.primary,
                        label = "Browser",
                        value = result.browserLabel
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
