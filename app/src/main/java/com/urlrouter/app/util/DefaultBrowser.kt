package com.urlrouter.app.util

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

// ---- Default browser role ----
// minSdk is 29, so RoleManager (API 29) is always available. ROLE_BROWSER lets
// the app trigger the system "set as default browser" dialog directly, with a
// Default-apps settings fallback for the rare device where the role is absent.

object DefaultBrowser {

    private fun roleManager(context: Context): RoleManager? =
        context.getSystemService(RoleManager::class.java)

    // True when this app currently holds the browser role.
    fun isDefault(context: Context): Boolean {
        val rm = roleManager(context) ?: return false
        return rm.isRoleAvailable(RoleManager.ROLE_BROWSER) && rm.isRoleHeld(RoleManager.ROLE_BROWSER)
    }

    // Intent that asks the user to make this app the default browser: the system
    // role dialog when possible, otherwise the Default-apps settings screen.
    // Returns null when the role is already held (nothing to request).
    fun requestIntent(context: Context): Intent? {
        val rm = roleManager(context)
        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
            if (rm.isRoleHeld(RoleManager.ROLE_BROWSER)) return null
            return rm.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
        }
        return Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    }
}
