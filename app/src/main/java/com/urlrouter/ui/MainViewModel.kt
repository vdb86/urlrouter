package com.urlrouter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.urlrouter.UrlRouterApp
import com.urlrouter.model.*
import com.urlrouter.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    val appearance: StateFlow<AppearanceSettings> = prefs.appearanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppearanceSettings())

    val defaultBrowserPkg: StateFlow<String> = prefs.defaultBrowserPkgFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val defaultBrowserEnabled: StateFlow<Boolean> = prefs.defaultBrowserEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun rescanBrowsers() {
        viewModelScope.launch {
            val discovered = BrowserDiscovery.discoverBrowsers(getApplication())
            val existing = browserDao.getAll().associateBy { it.packageName }
            val merged = discovered.map { fresh ->
                existing[fresh.packageName]?.copy(
                    label = fresh.label,
                    activityName = fresh.activityName
                ) ?: fresh
            }
            browserDao.removeStale(discovered.map { it.packageName })
            browserDao.upsertAll(merged)
            prefs.setBrowsersScanned(true)
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

    fun saveAppearance(settings: AppearanceSettings) {
        viewModelScope.launch { prefs.saveAppearance(settings) }
    }

    suspend fun diagnose(url: String): DiagnosticsResult {
        val enabledRules = ruleDao.getEnabled()
        val matched = RoutingEngine.evaluate(url, enabledRules)
        val browser = matched?.let { browserDao.getByPackage(it.browserPackage) }
        return DiagnosticsResult(
            url = url,
            matchedRule = matched,
            matchType = matched?.matchType,
            matchReason = matched?.let { RoutingEngine.matchReason(url, it) } ?: "No rule matched",
            browserPackage = matched?.browserPackage,
            browserLabel = browser?.label
        )
    }

    fun exportConfig(onResult: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val allBrowsers = browserDao.getAll()
                val schema = ExportSchema(
                    version = 1,
                    rules = ruleDao.getEnabled(),
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
