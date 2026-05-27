package com.urlrouter.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.urlrouter.model.AppearanceSettings
import com.urlrouter.model.BrowserInfo
import com.urlrouter.model.ChooserAlignment
import com.urlrouter.model.ChooserDisplayMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BrowserChooserSheet(
    browsers: List<BrowserInfo>,
    appearance: AppearanceSettings,
    onBrowserSelected: (BrowserInfo) -> Unit,
    onBrowserLongPressed: (BrowserInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val containerColor = Color(appearance.backgroundColor)
    val shape = RoundedCornerShape(topStart = appearance.cornerRadius.dp, topEnd = appearance.cornerRadius.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        shape = shape,
        tonalElevation = appearance.elevation.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = appearance.padding.dp,
                    end = appearance.padding.dp,
                    top = appearance.padding.dp,
                    bottom = appearance.padding.dp
                )
                .navigationBarsPadding()
        ) {
            when (appearance.displayMode) {
                ChooserDisplayMode.HORIZONTAL_ICONS,
                ChooserDisplayMode.HORIZONTAL_ICON_TEXT -> {
                    val rowAlignment = when (appearance.alignment) {
                        ChooserAlignment.START -> Alignment.Start
                        ChooserAlignment.CENTER -> Alignment.CenterHorizontally
                        ChooserAlignment.END -> Alignment.End
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(appearance.itemSpacing.dp, rowAlignment),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(browsers, key = { it.packageName }) { browser ->
                            BrowserItem(
                                browser = browser,
                                showIcon = appearance.showIcons,
                                showName = appearance.showNames && appearance.displayMode == ChooserDisplayMode.HORIZONTAL_ICON_TEXT,
                                vertical = true,
                                onTap = { onBrowserSelected(browser) },
                                onLongPress = { onBrowserLongPressed(browser) }
                            )
                        }
                    }
                }

                ChooserDisplayMode.VERTICAL_LIST -> {
                    browsers.forEach { browser ->
                        BrowserItem(
                            browser = browser,
                            showIcon = appearance.showIcons,
                            showName = appearance.showNames,
                            vertical = false,
                            onTap = { onBrowserSelected(browser) },
                            onLongPress = { onBrowserLongPressed(browser) }
                        )
                        Spacer(modifier = Modifier.height(appearance.itemSpacing.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BrowserItem(
    browser: BrowserInfo,
    showIcon: Boolean,
    showName: Boolean,
    vertical: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    if (vertical) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .combinedClickable(onClick = onTap, onLongClick = onLongPress)
                .padding(8.dp)
        ) {
            if (showIcon) BrowserIcon(packageName = browser.packageName, size = 48.dp)
            if (showName) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = browser.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 64.dp)
                )
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onTap, onLongClick = onLongPress)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            if (showIcon) {
                BrowserIcon(packageName = browser.packageName, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
            }
            if (showName) {
                Text(
                    text = browser.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
