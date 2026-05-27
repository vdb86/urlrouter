package com.urlrouter.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import com.urlrouter.model.BrowserInfo

object BrowserDiscovery {

    fun discoverBrowsers(context: Context): List<BrowserInfo> {
        val pm = context.packageManager
        val ownPackage = context.packageName
        val schemes = listOf("http", "https")
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PackageManager.ResolveInfoFlags.of(
                (PackageManager.MATCH_ALL or PackageManager.GET_RESOLVED_FILTER).toLong()
            )
        } else null

        val seen = mutableSetOf<String>()
        val results = mutableListOf<ResolveInfo>()

        for (scheme in schemes) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$scheme://example.com")).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            val resolved: List<ResolveInfo> = if (flags != null) {
                pm.queryIntentActivities(intent, flags)
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, PackageManager.MATCH_ALL or PackageManager.GET_RESOLVED_FILTER)
            }
            for (info in resolved) {
                val pkg = info.activityInfo.packageName
                if (pkg != ownPackage && seen.add(pkg)) results.add(info)
            }
        }

        return results.mapIndexed { index, info ->
            BrowserInfo(
                packageName = info.activityInfo.packageName,
                label = info.loadLabel(pm).toString(),
                activityName = info.activityInfo.name,
                isEnabled = true,
                displayOrder = index
            )
        }
    }
}
