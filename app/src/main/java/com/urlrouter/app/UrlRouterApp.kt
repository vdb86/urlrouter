package com.urlrouter.app

import android.app.Application
import com.urlrouter.app.data.db.AppDatabase
import com.urlrouter.app.data.datastore.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class UrlRouterApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val preferences by lazy { AppPreferences(this) }

    // ---- Application scope ----
    // Survives activity destruction. Used for fire-and-forget work (e.g. rule
    // creation from the chooser long-press) that must not be cancelled when the
    // transient RoutingActivity calls finish().
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

}
