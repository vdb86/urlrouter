package com.urlrouter.app.ui.components

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun BrowserIcon(
    packageName: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Icon loading is binder IPC + drawable decode; remember it per package so
    // it isn't repeated on every recomposition (also keeps Coil's cache stable).
    val model = remember(packageName) {
        ImageRequest.Builder(context)
            .data(loadAppIcon(context, packageName))
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = model,
        contentDescription = packageName,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}

private fun loadAppIcon(context: android.content.Context, packageName: String): Drawable? {
    return try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}
