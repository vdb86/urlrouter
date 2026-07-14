package com.urlrouter.app.util

import com.urlrouter.app.model.AppearanceSettings
import com.urlrouter.app.model.ChooserAlignment
import com.urlrouter.app.model.ChooserDisplayMode
import com.urlrouter.app.model.ExportSchema
import com.urlrouter.app.model.MatchType
import com.urlrouter.app.model.RoutingRule
import com.google.gson.Gson
import com.google.gson.JsonObject

object ImportExport {

    private val gson = Gson()

    fun export(schema: ExportSchema): String = gson.toJson(schema)

    /**
     * Parse and validate an import JSON string.
     * Returns [ExportSchema] or throws [ImportException] with a user-friendly message.
     */
    fun import(json: String): ExportSchema {
        return try {
            val obj = gson.fromJson(json, JsonObject::class.java)
                ?: throw ImportException("File is empty or not valid JSON")

            val version = obj.get("version")?.asInt
                ?: throw ImportException("Missing 'version' field")

            if (version != 1) throw ImportException("Unsupported version: $version")

            val rules = mutableListOf<RoutingRule>()
            obj.getAsJsonArray("rules")?.forEach { element ->
                val ruleObj = element.asJsonObject
                rules.add(
                    RoutingRule(
                        id = 0,
                        isEnabled = ruleObj.get("isEnabled")?.asBoolean ?: true,
                        matchType = ruleObj.get("matchType")?.asString?.let { mt ->
                            try {
                                MatchType.valueOf(mt)
                            } catch (e: IllegalArgumentException) {
                                throw ImportException("Unknown match type: $mt")
                            }
                        } ?: throw ImportException("Rule missing matchType"),
                        pattern = ruleObj.get("pattern")?.asString
                            ?: throw ImportException("Rule missing pattern"),
                        browserPackage = ruleObj.get("browserPackage")?.asString
                            ?: throw ImportException("Rule missing browserPackage"),
                        createdAt = ruleObj.get("createdAt")?.asLong ?: System.currentTimeMillis()
                    )
                )
            }

            val browserOrder = mutableListOf<String>()
            obj.getAsJsonArray("browserOrder")?.forEach { browserOrder.add(it.asString) }

            val enabledBrowsers = mutableListOf<String>()
            obj.getAsJsonArray("enabledBrowsers")?.forEach { enabledBrowsers.add(it.asString) }

            val appearance = obj.getAsJsonObject("appearance")?.let { ap ->
                AppearanceSettings(
                    backgroundColor = ap.get("backgroundColor")?.asLong ?: 0xFF1C1B1F,
                    textColor = ap.get("textColor")?.asLong ?: 0xFFFFFFFF,
                    cornerRadius = ap.get("cornerRadius")?.asInt ?: 28,
                    padding = ap.get("padding")?.asInt ?: 16,
                    itemSpacing = ap.get("itemSpacing")?.asInt ?: 8,
                    displayMode = try {
                        ChooserDisplayMode.valueOf(
                            ap.get("displayMode")?.asString ?: ChooserDisplayMode.GRID.name
                        )
                    } catch (e: IllegalArgumentException) {
                        ChooserDisplayMode.GRID
                    },
                    alignment = try {
                        ChooserAlignment.valueOf(
                            ap.get("alignment")?.asString ?: ChooserAlignment.CENTER.name
                        )
                    } catch (e: IllegalArgumentException) {
                        ChooserAlignment.CENTER
                    },
                    showIcons = ap.get("showIcons")?.asBoolean ?: true,
                    showNames = ap.get("showNames")?.asBoolean ?: true,
                    iconSize = ap.get("iconSize")?.asInt ?: 48,
                    textSize = ap.get("textSize")?.asInt ?: 12,
                    verticalPosition = ap.get("verticalPosition")?.asFloat ?: 0f
                )
            } ?: AppearanceSettings()

            ExportSchema(
                version = version,
                rules = rules,
                browserOrder = browserOrder,
                enabledBrowsers = enabledBrowsers,
                appearance = appearance
            )
        } catch (e: ImportException) {
            throw e
        } catch (e: Exception) {
            throw ImportException("Failed to parse file: ${e.message}")
        }
    }
}

class ImportException(message: String) : Exception(message)
