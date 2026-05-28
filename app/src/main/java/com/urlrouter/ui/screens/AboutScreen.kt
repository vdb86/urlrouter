package com.urlrouter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Dedication + acknowledgement
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dedicated to my son Mihajlo.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Thank you HumanMade and Altis for making the development of this app possible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { uriHandler.openUri("https://humanmade.com/") }) {
                            Text("HumanMade")
                        }
                        OutlinedButton(onClick = { uriHandler.openUri("https://www.altis-dxp.com/") }) {
                            Text("Altis")
                        }
                    }
                }
            }

            // Source + licence
            HorizontalDivider()
            Text("Source & Licence", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            TextButton(
                onClick = { uriHandler.openUri("https://github.com/vdb86/urlrouter") },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "github.com/vdb86/urlrouter",
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = TextDecoration.Underline
                )
            }
            Text(
                text = "Released under the GPL-3.0 licence.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Privacy
            HorizontalDivider()
            Text("Privacy", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(
                text = "URL Router processes all URLs locally on your device. No data is sent anywhere. No analytics. No tracking.",
                style = MaterialTheme.typography.bodyMedium
            )

            // How it works
            HorizontalDivider()
            Text("How it works", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(
                text = "URL Router registers itself as a browser. When you open a link anywhere on Android, URL Router receives it first, evaluates your rules, and silently forwards the URL to the correct browser — with no visible UI when a rule matches.\n\nIf no rule matches, a minimal browser chooser appears so you can pick manually.",
                style = MaterialTheme.typography.bodyMedium
            )

            // Features
            HorizontalDivider()
            Text("Features", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            val features = listOf(
                "Rule-based routing" to "Route URLs by exact hostname, wildcard hostname (*.example.com), URL prefix, substring match, or full regex.",
                "Rule priority" to "Exact hostname → wildcard → prefix → regex → contains; first match wins.",
                "Default browser" to "Optionally define a fallback browser that opens when no rule matches, skipping the chooser entirely.",
                "Minimal chooser" to "A clean bottom sheet with only the browsers you want; fully customisable appearance.",
                "No recent apps entry" to "URL Router disappears after routing; it never appears in your app switcher.",
                "Long-press to create rule" to "Long-press any browser in the chooser to automatically create a rule for that domain.",
                "Browser management" to "Enable/disable browsers, set display order, rescan installed browsers.",
                "Appearance settings" to "Configure the chooser display mode (grid/list), alignment, icon size, text colour, vertical position, corner radius, padding, and background colour with a full HSV colour picker.",
                "Import / Export" to "Back up and restore your entire configuration using the system file picker.",
                "Diagnostics" to "Paste any URL to see exactly which rule would match and which browser would open it."
            )

            features.forEach { (title, description) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
