package com.urlrouter.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.urlrouter.UrlRouterApp
import com.urlrouter.model.MatchType
import com.urlrouter.model.RoutingRule
import com.urlrouter.ui.components.BrowserChooserSheet
import com.urlrouter.ui.theme.UrlRouterTheme
import com.urlrouter.util.BrowserLauncher

class RoutingActivity : ComponentActivity() {

    private val routingViewModel: RoutingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent?.data?.toString()
        if (url.isNullOrBlank()) { finish(); return }

        routingViewModel.processUrl(url)

        setContent {
            UrlRouterTheme {
                val state by routingViewModel.state.collectAsStateWithLifecycle()
                

                val scope = rememberCoroutineScope()

                when (val s = state) {
                    is RoutingState.Loading -> {}

                    is RoutingState.LaunchBrowser -> {
                        LaunchedEffect(s) {
                            BrowserLauncher.launch(applicationContext, s.url, s.browser)
                            finish()
                        }
                    }

                    is RoutingState.ShowChooser -> {
                        var showSheet by remember { mutableStateOf(true) }
                        if (showSheet) {
                            BrowserChooserSheet(
                                browsers = s.browsers,
                                appearance = s.appearance,
                                onBrowserSelected = { browser ->
                                    showSheet = false
                                    BrowserLauncher.launch(applicationContext, s.url, browser)
                                    finish()
                                },
                                onBrowserLongPressed = { browser ->
                                    showSheet = false
                                    val host = runCatching {
                                        java.net.URI(s.url).host ?: s.url
                                    }.getOrElse { s.url }
                                    scope.launch {
                                        (application as UrlRouterApp).database.ruleDao().insert(
                                            RoutingRule(
                                                matchType = MatchType.EXACT_HOSTNAME,
                                                pattern = host,
                                                browserPackage = browser.packageName
                                            )
                                        )
                                    }
                                    BrowserLauncher.launch(applicationContext, s.url, browser)
                                    Toast.makeText(
                                        this@RoutingActivity,
                                        "This domain will now open automatically in ${browser.label}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                },
                                onDismiss = { showSheet = false; finish() }
                            )
                        }
                    }

                    is RoutingState.NoEnabledBrowsers -> {
                        LaunchedEffect(Unit) {
                            Toast.makeText(
                                applicationContext,
                                "No browsers enabled. Open URL Router settings.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    }
                }
            }
        }
    }
}
