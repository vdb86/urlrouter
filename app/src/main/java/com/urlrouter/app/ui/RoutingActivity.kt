package com.urlrouter.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.urlrouter.app.UrlRouterApp
import com.urlrouter.app.model.MatchType
import com.urlrouter.app.model.RoutingRule
import com.urlrouter.app.ui.components.BrowserChooserSheet
import com.urlrouter.app.ui.theme.UrlRouterTheme
import com.urlrouter.app.util.BrowserLauncher

class RoutingActivity : ComponentActivity() {

    private val routingViewModel: RoutingViewModel by viewModels()

    // Compose content is only inflated when the chooser is actually needed;
    // silent rule-matched launches never pay Compose startup cost.
    private var contentSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent?.data?.toString()
        if (url.isNullOrBlank()) { finish(); return }

        observeState()
        routingViewModel.processUrl(url)
    }

    // singleTask: a link arriving while an instance exists lands here, not onCreate.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val url = intent.data?.toString()
        if (url.isNullOrBlank()) { finish(); return }
        routingViewModel.processUrl(url)
    }

    // ---- State handling ----

    private fun observeState() {
        lifecycleScope.launch {
            routingViewModel.state.collect { s ->
                when (s) {
                    is RoutingState.Loading -> Unit

                    is RoutingState.LaunchBrowser -> {
                        val launched = BrowserLauncher.launch(applicationContext, s.url, s.browser)
                        if (launched) {
                            finish()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Could not open ${s.browser.label}",
                                Toast.LENGTH_SHORT
                            ).show()
                            routingViewModel.fallbackToChooser(s.url)
                        }
                    }

                    is RoutingState.NoEnabledBrowsers -> {
                        Toast.makeText(
                            applicationContext,
                            "No browsers enabled. Open URL Router settings.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }

                    is RoutingState.ShowChooser -> showChooserContent()
                }
            }
        }
    }

    // ---- Chooser UI ----

    private fun showChooserContent() {
        if (contentSet) return
        contentSet = true
        setContent {
            UrlRouterTheme {
                val state by routingViewModel.state.collectAsStateWithLifecycle()
                val s = state as? RoutingState.ShowChooser ?: return@UrlRouterTheme

                var showSheet by remember(s.url) { mutableStateOf(true) }
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
                                Uri.parse(s.url).host ?: s.url
                            }.getOrElse { s.url }
                            // Application scope: survives finish(), unlike a
                            // composition- or ViewModel-bound scope.
                            (application as UrlRouterApp).appScope.launch {
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
        }
    }
}
