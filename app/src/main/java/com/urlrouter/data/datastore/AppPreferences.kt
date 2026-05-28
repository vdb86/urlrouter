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
        val KEY_TEXT_COLOR = longPreferencesKey("text_color")
        val KEY_CORNER_RADIUS = intPreferencesKey("corner_radius")
        val KEY_PADDING = intPreferencesKey("padding")
        val KEY_ITEM_SPACING = intPreferencesKey("item_spacing")
        val KEY_DISPLAY_MODE = stringPreferencesKey("display_mode")
        val KEY_ALIGNMENT = stringPreferencesKey("alignment")
        val KEY_SHOW_ICONS = booleanPreferencesKey("show_icons")
        val KEY_SHOW_NAMES = booleanPreferencesKey("show_names")
        val KEY_ICON_SIZE = intPreferencesKey("icon_size")
        val KEY_TEXT_SIZE = intPreferencesKey("text_size")
        val KEY_VERTICAL_POSITION = floatPreferencesKey("vertical_position")
        val KEY_BROWSERS_SCANNED = booleanPreferencesKey("browsers_scanned")
        val KEY_DEFAULT_BROWSER_PKG = stringPreferencesKey("default_browser_pkg")
        val KEY_DEFAULT_BROWSER_ENABLED = booleanPreferencesKey("default_browser_enabled")
    }

    val appearanceFlow: Flow<AppearanceSettings> = context.dataStore.data.map { prefs ->
        AppearanceSettings(
            backgroundColor = prefs[KEY_BG_COLOR] ?: 0xFF1C1B1F,
            textColor = prefs[KEY_TEXT_COLOR] ?: 0xFFFFFFFF,
            cornerRadius = prefs[KEY_CORNER_RADIUS] ?: 28,
            padding = prefs[KEY_PADDING] ?: 16,
            itemSpacing = prefs[KEY_ITEM_SPACING] ?: 8,
            displayMode = try {
                ChooserDisplayMode.valueOf(prefs[KEY_DISPLAY_MODE] ?: ChooserDisplayMode.GRID.name)
            } catch (e: IllegalArgumentException) {
                ChooserDisplayMode.GRID
            },
            alignment = ChooserAlignment.valueOf(
                prefs[KEY_ALIGNMENT] ?: ChooserAlignment.CENTER.name
            ),
            showIcons = prefs[KEY_SHOW_ICONS] ?: true,
            showNames = prefs[KEY_SHOW_NAMES] ?: true,
            iconSize = prefs[KEY_ICON_SIZE] ?: 48,
            textSize = prefs[KEY_TEXT_SIZE] ?: 12,
            verticalPosition = prefs[KEY_VERTICAL_POSITION] ?: 0f
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
            prefs[KEY_TEXT_COLOR] = settings.textColor
            prefs[KEY_CORNER_RADIUS] = settings.cornerRadius
            prefs[KEY_PADDING] = settings.padding
            prefs[KEY_ITEM_SPACING] = settings.itemSpacing
            prefs[KEY_DISPLAY_MODE] = settings.displayMode.name
            prefs[KEY_ALIGNMENT] = settings.alignment.name
            prefs[KEY_SHOW_ICONS] = settings.showIcons
            prefs[KEY_SHOW_NAMES] = settings.showNames
            prefs[KEY_ICON_SIZE] = settings.iconSize
            prefs[KEY_TEXT_SIZE] = settings.textSize
            prefs[KEY_VERTICAL_POSITION] = settings.verticalPosition
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
