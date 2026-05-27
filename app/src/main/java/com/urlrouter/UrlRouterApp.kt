package com.urlrouter

import android.app.Application
import com.urlrouter.data.db.AppDatabase
import com.urlrouter.data.datastore.AppPreferences

class UrlRouterApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val preferences by lazy { AppPreferences(this) }

}
