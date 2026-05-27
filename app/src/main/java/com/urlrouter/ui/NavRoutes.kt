package com.urlrouter.ui

object NavRoutes {
    const val SETTINGS = "settings"
    const val BROWSER_MANAGEMENT = "browsers"
    const val ROUTING_RULES = "rules"
    const val RULE_EDITOR = "rule_editor/{ruleId}"
    const val RULE_EDITOR_NEW = "rule_editor/-1"
    const val APPEARANCE = "appearance"
    const val IMPORT_EXPORT = "import_export"
    const val DIAGNOSTICS = "diagnostics"

    fun ruleEditor(ruleId: Long) = "rule_editor/$ruleId"
}
