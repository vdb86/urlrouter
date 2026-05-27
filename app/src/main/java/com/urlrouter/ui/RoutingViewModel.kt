package com.urlrouter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.urlrouter.UrlRouterApp
import com.urlrouter.model.*
import com.urlrouter.util.RoutingEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class RoutingState {
    object Loading : RoutingState()
    data class LaunchBrowser(val browser: BrowserInfo, val url: String) : RoutingState()
    data class ShowChooser(
        val browsers: List<BrowserInfo>,
        val url: String,
        val appearance: AppearanceSettings
    ) : RoutingState()
    object NoEnabledBrowsers : RoutingState()
}

class RoutingViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as UrlRouterApp
    private val browserDao = app.database.browserDao()
    private val ruleDao = app.database.ruleDao()
    private val prefs = app.preferences

    private val _state = MutableStateFlow<RoutingState>(RoutingState.Loading)
    val state: StateFlow<RoutingState> = _state.asStateFlow()

    fun processUrl(url: String) {
        viewModelScope.launch {
            val matched = RoutingEngine.evaluate(url, ruleDao.getEnabled())

            if (matched != null) {
                val browser = browserDao.getByPackage(matched.browserPackage)
                if (browser != null && browser.isEnabled) {
                    _state.value = RoutingState.LaunchBrowser(browser, url)
                    return@launch
                }
            }

            // No rule matched — check default browser
            val defaultEnabled = prefs.defaultBrowserEnabledFlow.first()
            val defaultPkg = prefs.defaultBrowserPkgFlow.first()
            if (defaultEnabled && defaultPkg.isNotBlank()) {
                val defaultBrowser = browserDao.getByPackage(defaultPkg)
                if (defaultBrowser != null && defaultBrowser.isEnabled) {
                    _state.value = RoutingState.LaunchBrowser(defaultBrowser, url)
                    return@launch
                }
            }

            val browsers = browserDao.getAll().filter { it.isEnabled }
            if (browsers.isEmpty()) {
                _state.value = RoutingState.NoEnabledBrowsers
                return@launch
            }
            _state.value = RoutingState.ShowChooser(browsers, url, prefs.appearanceFlow.first())
        }
    }
}
