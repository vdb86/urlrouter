package com.urlrouter.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.urlrouter.app.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isWorking by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        isWorking = true
        viewModel.exportConfig(
            onResult = { json ->
                scope.launch {
                    try {
                        writeToUri(context, uri, json)
                        statusMessage = "Configuration exported successfully"
                        isError = false
                    } catch (e: Exception) {
                        statusMessage = "Export failed: ${e.message}"
                        isError = true
                    } finally {
                        isWorking = false
                    }
                }
            },
            onError = { msg ->
                statusMessage = msg
                isError = true
                isWorking = false
            }
        )
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        isWorking = true
        scope.launch {
            try {
                val json = readFromUri(context, uri)
                viewModel.importConfig(
                    json = json,
                    onSuccess = {
                        statusMessage = "Configuration imported successfully"
                        isError = false
                        isWorking = false
                    },
                    onError = { msg ->
                        statusMessage = msg
                        isError = true
                        isWorking = false
                    }
                )
            } catch (e: Exception) {
                statusMessage = "Could not read file: ${e.message}"
                isError = true
                isWorking = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import / Export") },
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
            if (statusMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = statusMessage,
                        modifier = Modifier.padding(12.dp),
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text("Export", style = MaterialTheme.typography.titleMedium)
            Text(
                "Save your rules, browser settings, and appearance configuration to a JSON file.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { exportLauncher.launch("urlrouter_config.json") },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isWorking) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Export Configuration")
            }

            HorizontalDivider()

            Text("Import", style = MaterialTheme.typography.titleMedium)
            Text(
                "Restore a previously exported configuration file.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isWorking) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Import Configuration")
            }
        }
    }
}

private suspend fun writeToUri(context: Context, uri: Uri, content: String) =
    withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray())
        } ?: throw Exception("Could not open file for writing")
    }

private suspend fun readFromUri(context: Context, uri: Uri): String =
    withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: throw Exception("Could not open file for reading")
    }