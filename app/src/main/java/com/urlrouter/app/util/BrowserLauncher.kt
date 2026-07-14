package com.urlrouter.app.util

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.urlrouter.app.model.BrowserInfo

object BrowserLauncher {

    fun launch(context: Context, url: String, browser: BrowserInfo): Boolean {
        val success = tryLaunch(context) {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                component = ComponentName(browser.packageName, browser.activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        if (success) return true

        return tryLaunch(context) {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                `package` = browser.packageName
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    private fun tryLaunch(context: Context, intentBuilder: () -> Intent): Boolean {
        return try {
            context.startActivity(intentBuilder())
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    fun isBrowserAvailable(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
