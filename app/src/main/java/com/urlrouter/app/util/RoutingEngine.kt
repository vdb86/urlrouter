package com.urlrouter.app.util

import com.urlrouter.app.model.MatchType
import com.urlrouter.app.model.RoutingRule
import java.net.URI

object RoutingEngine {

    // ---- Regex cache ----
    // Compiled patterns are cached so repeated link taps don't recompile.
    // Access-ordered LRU capped at 64 entries.
    private val regexCache = object : LinkedHashMap<String, Regex>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Regex>): Boolean =
            size > 64
    }

    @Synchronized
    private fun cachedRegex(pattern: String): Regex =
        regexCache.getOrPut(pattern) { Regex(pattern) }

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
                MatchType.PREFIX -> url.startsWith(rule.pattern, ignoreCase = true)
                MatchType.REGEX -> cachedRegex(rule.pattern).containsMatchIn(url)
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

    /**
     * Extract the hostname. android.net.Uri is far more tolerant of the messy
     * URLs real apps share (unencoded spaces, pipes, brackets) than java.net.URI,
     * which throws on them; URI is kept only as a secondary fallback.
     */
    private fun extractHost(url: String): String? {
        val androidHost = try {
            android.net.Uri.parse(url).host
        } catch (e: Exception) {
            null
        }
        if (!androidHost.isNullOrBlank()) return androidHost

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
