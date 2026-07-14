package com.urlrouter.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// ---------------------------------------------------------------------------
// Browser model
// ---------------------------------------------------------------------------

@Entity(tableName = "browsers")
data class BrowserInfo(
    @PrimaryKey val packageName: String,
    val label: String,
    val activityName: String,
    val isWorkProfile: Boolean = false,
    val isEnabled: Boolean = true,
    val displayOrder: Int = 0
)

// ---------------------------------------------------------------------------
// Routing rule model
// ---------------------------------------------------------------------------

enum class MatchType {
    EXACT_HOSTNAME,
    WILDCARD_HOSTNAME,
    PREFIX,
    REGEX,
    CONTAINS
}

@Entity(tableName = "rules")
data class RoutingRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isEnabled: Boolean = true,
    val matchType: MatchType,
    val pattern: String,
    val browserPackage: String,
    val createdAt: Long = System.currentTimeMillis()
)

// ---------------------------------------------------------------------------
// Chooser appearance settings
// ---------------------------------------------------------------------------

enum class ChooserDisplayMode { GRID, LIST }
enum class ChooserAlignment { START, CENTER, END }

data class AppearanceSettings(
    val backgroundColor: Long = 0xFF1C1B1F,
    val textColor: Long = 0xFFFFFFFF,
    val cornerRadius: Int = 28,
    val padding: Int = 16,
    val itemSpacing: Int = 8,
    val displayMode: ChooserDisplayMode = ChooserDisplayMode.GRID,
    val alignment: ChooserAlignment = ChooserAlignment.CENTER,
    val showIcons: Boolean = true,
    val showNames: Boolean = true,
    val iconSize: Int = 48,
    val textSize: Int = 12,
    val verticalPosition: Float = 0f  // 0.0 = bottom, 1.0 = top
)

// ---------------------------------------------------------------------------
// Export / Import schema
// ---------------------------------------------------------------------------

data class ExportSchema(
    val version: Int = 1,
    val rules: List<RoutingRule> = emptyList(),
    val browserOrder: List<String> = emptyList(),
    val enabledBrowsers: List<String> = emptyList(),
    val appearance: AppearanceSettings = AppearanceSettings()
)

// ---------------------------------------------------------------------------
// Diagnostics result
// ---------------------------------------------------------------------------

data class DiagnosticsResult(
    val url: String,
    val matchedRule: RoutingRule?,
    val matchType: MatchType?,
    val matchReason: String,
    val browserPackage: String?,
    val browserLabel: String?
)
