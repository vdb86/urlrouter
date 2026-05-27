package com.urlrouter.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.urlrouter.model.AppearanceSettings
import com.urlrouter.model.ChooserAlignment
import com.urlrouter.model.ChooserDisplayMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("app_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        val KEY_BG_COLOR = longPreferencesKey("bg_color")
        val KEY_CORNER_RADIUS = intPreferencesKey("corner_radius")
        val KEY_ELEVATION = intPreferencesKey("elevation")
        val KEY_PADDING = intPreferencesKey("padding")
        val KEY_ITEM_SPACING = intPreferencesKey("item_spacing")
        val KEY_DISPLAY_MODE = stringPreferencesKey("display_mode")
        val KEY_ALIGNMENT = stringPreferencesKey("alignment")
        val KEY_SHOW_ICONS = booleanPreferencesKey("show_icons")
        val KEY_SHOW_NAMES = booleanPreferencesKey("show_names")
        val KEY_ANIMATIONS = booleanPreferencesKey("animations_enabled")
        val KEY_ANIM_DURATION = intPreferencesKey("anim_duration")
        val KEY_BROWSERS_SCANNED = booleanPreferencesKey("browsers_scanned")
        val KEY_DEFAULT_BROWSER_PKG = stringPreferencesKey("default_browser_pkg")
        val KEY_DEFAULT_BROWSER_ENABLED = booleanPreferencesKey("default_browser_enabled")
    }

    val appearanceFlow: Flow<AppearanceSettings> = context.dataStore.data.map { prefs ->
        AppearanceSettings(
            backgroundColor = prefs[KEY_BG_COLOR] ?: 0xFF1C1B1F,
            cornerRadius = prefs[KEY_CORNER_RADIUS] ?: 28,
            elevation = prefs[KEY_ELEVATION] ?: 6,
            padding = prefs[KEY_PADDING] ?: 16,
            itemSpacing = prefs[KEY_ITEM_SPACING] ?: 8,
            displayMode = ChooserDisplayMode.valueOf(
                prefs[KEY_DISPLAY_MODE] ?: ChooserDisplayMode.HORIZONTAL_ICON_TEXT.name
            ),
            alignment = ChooserAlignment.valueOf(
                prefs[KEY_ALIGNMENT] ?: ChooserAlignment.CENTER.name
            ),
            showIcons = prefs[KEY_SHOW_ICONS] ?: true,
            showNames = prefs[KEY_SHOW_NAMES] ?: true,
            animationsEnabled = prefs[KEY_ANIMATIONS] ?: true,
            animationDurationMs = prefs[KEY_ANIM_DURATION] ?: 300
        )
    }

    val browsersScannedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BROWSERS_SCANNED] ?: false
    }

    val defaultBrowserPkgFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_BROWSER_PKG] ?: ""
    }

    val defaultBrowserEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_BROWSER_ENABLED] ?: false
    }

    suspend fun saveAppearance(settings: AppearanceSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BG_COLOR] = settings.backgroundColor
            prefs[KEY_CORNER_RADIUS] = settings.cornerRadius
            prefs[KEY_ELEVATION] = settings.elevation
            prefs[KEY_PADDING] = settings.padding
            prefs[KEY_ITEM_SPACING] = settings.itemSpacing
            prefs[KEY_DISPLAY_MODE] = settings.displayMode.name
            prefs[KEY_ALIGNMENT] = settings.alignment.name
            prefs[KEY_SHOW_ICONS] = settings.showIcons
            prefs[KEY_SHOW_NAMES] = settings.showNames
            prefs[KEY_ANIMATIONS] = settings.animationsEnabled
            prefs[KEY_ANIM_DURATION] = settings.animationDurationMs
        }
    }

    suspend fun setBrowsersScanned(scanned: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BROWSERS_SCANNED] = scanned
        }
    }

    suspend fun setDefaultBrowser(packageName: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_BROWSER_PKG] = packageName
            prefs[KEY_DEFAULT_BROWSER_ENABLED] = enabled
        }
    }
}
