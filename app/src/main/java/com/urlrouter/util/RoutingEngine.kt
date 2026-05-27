package com.urlrouter.util

import com.urlrouter.model.MatchType
import com.urlrouter.model.RoutingRule
import java.net.URI

object RoutingEngine {

    /**
     * Evaluate enabled rules against [url] and return the first matching rule.
     *
     * Priority order (as per spec):
     *   1. EXACT_HOSTNAME
     *   2. WILDCARD_HOSTNAME
     *   3. PREFIX
     *   4. REGEX
     *   5. CONTAINS
     *
     * Within the same type, the first rule (by id / list order) wins.
     */
    fun evaluate(url: String, rules: List<RoutingRule>): RoutingRule? {
        val enabled = rules.filter { it.isEnabled }

        // Group by type in priority order
        val ordered = listOf(
            MatchType.EXACT_HOSTNAME,
            MatchType.WILDCARD_HOSTNAME,
            MatchType.PREFIX,
            MatchType.REGEX,
            MatchType.CONTAINS
        )

        for (type in ordered) {
            val group = enabled.filter { it.matchType == type }
            for (rule in group) {
                if (matches(url, rule)) return rule
            }
        }

        return null
    }

    fun matches(url: String, rule: RoutingRule): Boolean {
        return try {
            when (rule.matchType) {
                MatchType.EXACT_HOSTNAME -> matchExactHostname(url, rule.pattern)
                MatchType.WILDCARD_HOSTNAME -> matchWildcardHostname(url, rule.pattern)
                MatchType.PREFIX -> url.startsWith(rule.pattern)
                MatchType.REGEX -> Regex(rule.pattern).containsMatchIn(url)
                MatchType.CONTAINS -> url.contains(rule.pattern, ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun matchExactHostname(url: String, pattern: String): Boolean {
        val host = extractHost(url) ?: return false
        // Strip leading "www." for comparison
        val normalHost = host.removePrefix("www.")
        val normalPattern = pattern.removePrefix("www.")
        return normalHost.equals(normalPattern, ignoreCase = true)
    }

    private fun matchWildcardHostname(url: String, pattern: String): Boolean {
        // pattern looks like *.youtube.com
        val host = extractHost(url) ?: return false
        if (!pattern.startsWith("*.")) return false
        val suffix = pattern.removePrefix("*").lowercase() // ".youtube.com"
        return host.lowercase().endsWith(suffix) || host.lowercase() == suffix.removePrefix(".")
    }

    private fun extractHost(url: String): String? {
        return try {
            URI(url).host
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Describe why a rule matched, for diagnostics.
     */
    fun matchReason(url: String, rule: RoutingRule): String {
        return when (rule.matchType) {
            MatchType.EXACT_HOSTNAME -> "Hostname matches '${rule.pattern}' exactly"
            MatchType.WILDCARD_HOSTNAME -> "Hostname matches wildcard '${rule.pattern}'"
            MatchType.PREFIX -> "URL starts with '${rule.pattern}'"
            MatchType.REGEX -> "URL matches regex '${rule.pattern}'"
            MatchType.CONTAINS -> "URL contains '${rule.pattern}'"
        }
    }
}
