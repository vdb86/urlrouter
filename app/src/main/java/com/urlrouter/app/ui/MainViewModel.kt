package com.urlrouter.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.urlrouter.app.UrlRouterApp
import com.urlrouter.app.model.*
import com.urlrouter.app.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as UrlRouterApp
    private val browserDao = app.database.browserDao()
    private val ruleDao = app.database.ruleDao()
    private val prefs = app.preferences

    val browsers: StateFlow<List<BrowserInfo>> = browserDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enabledBrowsers: StateFlow<List<BrowserInfo>> = browserDao.observeEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rules: StateFlow<List<RoutingRule>> = ruleDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nullable until the real value loads, so screens can initialise local edit
    // state from the loaded value instead of the placeholder default.
    val appearance: StateFlow<AppearanceSettings?> = prefs.appearanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val defaultBrowserPkg: StateFlow<String> = prefs.defaultBrowserPkgFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val defaultBrowserEnabled: StateFlow<Boolean> = prefs.defaultBrowserEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ---- Browser scanning ----

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    init {
        // Populate browsers on first launch so any screen (Rules, Rule Editor,
        // etc.) has data without the user first visiting Browser Management.
        viewModelScope.launch {
            if (!prefs.browsersScannedFlow.first()) {
                rescanBrowsers()
            }
        }
    }

    fun rescanBrowsers() {
        viewModelScope.launch {
            if (_scanning.value) return@launch
            _scanning.value = true
            try {
                val discovered = withContext(Dispatchers.IO) {
                    BrowserDiscovery.discoverBrowsers(getApplication())
                }
                // Guard: never wipe existing config on an empty/failed scan.
                if (discovered.isEmpty()) return@launch

                val existing = browserDao.getAll().associateBy { it.packageName }
                var nextOrder = (existing.values.maxOfOrNull { it.displayOrder } ?: -1) + 1
                val merged = discovered.map { fresh ->
                    existing[fresh.packageName]?.copy(
                        label = fresh.label,
                        activityName = fresh.activityName
                    ) ?: fresh.copy(displayOrder = nextOrder++)
                }
                browserDao.removeStale(discovered.map { it.packageName })
                browserDao.upsertAll(merged)
                prefs.setBrowsersScanned(true)
            } finally {
                _scanning.value = false
            }
        }
    }

    fun setBrowserEnabled(packageName: String, enabled: Boolean) {
        viewModelScope.launch { browserDao.setEnabled(packageName, enabled) }
    }

    fun setDefaultBrowser(packageName: String, enabled: Boolean) {
        viewModelScope.launch { prefs.setDefaultBrowser(packageName, enabled) }
    }

    fun reorderBrowsers(reordered: List<BrowserInfo>) {
        viewModelScope.launch {
            reordered.forEachIndexed { index, browser ->
                browserDao.setOrder(browser.packageName, index)
            }
        }
    }

    // ---- Rules ----

    fun addRule(rule: RoutingRule) {
        viewModelScope.launch { ruleDao.insert(rule) }
    }

    fun updateRule(rule: RoutingRule) {
        viewModelScope.launch { ruleDao.update(rule) }
    }

    fun deleteRule(rule: RoutingRule) {
        viewModelScope.launch { ruleDao.delete(rule) }
    }

    fun setRuleEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch { ruleDao.setEnabled(id, enabled) }
    }

    fun addQuickRule(hostname: String, browserPackage: String) {
        viewModelScope.launch {
            ruleDao.insert(
                RoutingRule(
                    matchType = MatchType.EXACT_HOSTNAME,
                    pattern = hostname,
                    browserPackage = browserPackage
                )
            )
        }
    }

    // ---- Appearance ----

    fun saveAppearance(settings: AppearanceSettings) {
        viewModelScope.launch { prefs.saveAppearance(settings) }
    }

    // ---- Diagnostics ----

    suspend fun diagnose(url: String): DiagnosticsResult {
        val enabledRules = ruleDao.getEnabled()
        val matched = RoutingEngine.evaluate(url, enabledRules)
        if (matched != null) {
            val browser = browserDao.getByPackage(matched.browserPackage)
            return DiagnosticsResult(
                url = url,
                matchedRule = matched,
                matchType = matched.matchType,
                matchReason = RoutingEngine.matchReason(url, matched),
                browserPackage = matched.browserPackage,
                browserLabel = browser?.label
            )
        }

        // Mirror the real routing path: no rule -> default browser -> chooser.
        val defaultEnabled = prefs.defaultBrowserEnabledFlow.first()
        val defaultPkg = prefs.defaultBrowserPkgFlow.first()
        if (defaultEnabled && defaultPkg.isNotBlank()) {
            val defaultBrowser = browserDao.getByPackage(defaultPkg)
            if (defaultBrowser != null && defaultBrowser.isEnabled) {
                return DiagnosticsResult(
                    url = url,
                    matchedRule = null,
                    matchType = null,
                    matchReason = "No rule matched — would open in the default browser",
                    browserPackage = defaultPkg,
                    browserLabel = defaultBrowser.label
                )
            }
        }

        return DiagnosticsResult(
            url = url,
            matchedRule = null,
            matchType = null,
            matchReason = "No rule matched — browser chooser would appear",
            browserPackage = null,
            browserLabel = null
        )
    }

    // ---- Import / Export ----

    fun exportConfig(onResult: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val allBrowsers = browserDao.getAll()
                val schema = ExportSchema(
                    version = 1,
                    rules = ruleDao.getAll(),
                    browserOrder = allBrowsers.sortedBy { it.displayOrder }.map { it.packageName },
                    enabledBrowsers = allBrowsers.filter { it.isEnabled }.map { it.packageName },
                    appearance = prefs.appearanceFlow.first()
                )
                onResult(ImportExport.export(schema))
            } catch (e: Exception) {
                onError(e.message ?: "Export failed")
            }
        }
    }

    fun importConfig(json: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val schema = ImportExport.import(json)
                ruleDao.deleteAll()
                ruleDao.insertAll(schema.rules.map { it.copy(id = 0) })
                schema.browserOrder.forEachIndexed { index, pkg -> browserDao.setOrder(pkg, index) }
                val all = browserDao.getAll()
                all.forEach { b ->
                    browserDao.setEnabled(b.packageName, b.packageName in schema.enabledBrowsers)
                }
                prefs.saveAppearance(schema.appearance)
                onSuccess()
            } catch (e: ImportException) {
                onError(e.message ?: "Import failed")
            } catch (e: Exception) {
                onError("Import failed: ${e.message}")
            }
        }
    }
}
