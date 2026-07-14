package com.urlrouter.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urlrouter.app.model.AppearanceSettings
import com.urlrouter.app.model.BrowserInfo
import com.urlrouter.app.model.ChooserAlignment
import com.urlrouter.app.model.ChooserDisplayMode
import kotlinx.coroutines.launch

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
    val shape = if (appearance.verticalPosition > 0f) {
        RoundedCornerShape(appearance.cornerRadius.dp)
    } else {
        RoundedCornerShape(topStart = appearance.cornerRadius.dp, topEnd = appearance.cornerRadius.dp)
    }

    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val spacerHeight = screenHeightDp * appearance.verticalPosition

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Animate the sheet hiding before calling onDismiss
    val animatedDismiss: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(0.dp),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        Surface(
            color = containerColor,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = animatedDismiss
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(appearance.padding.dp)
            ) {
                when (appearance.displayMode) {
                    ChooserDisplayMode.GRID -> {
                        // Column count derived from the actual available width,
                        // not a hardcoded value, so it adapts to any screen.
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val availableWidth = maxWidth.value.toInt().coerceAtLeast(1)
                            // Approximate item footprint: icon + item padding (8dp
                            // each side) + spacing between items.
                            val itemWidth =
                                (appearance.iconSize + 16 + appearance.itemSpacing).coerceAtLeast(1)
                            val itemsPerRow = maxOf(1, availableWidth / itemWidth)
                            val rows = browsers.chunked(itemsPerRow)

                            Column(modifier = Modifier.fillMaxWidth()) {
                                rows.forEachIndexed { rowIndex, rowBrowsers ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(
                                            appearance.itemSpacing.dp,
                                            when (appearance.alignment) {
                                                ChooserAlignment.START -> Alignment.Start
                                                ChooserAlignment.CENTER -> Alignment.CenterHorizontally
                                                ChooserAlignment.END -> Alignment.End
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rowBrowsers.forEach { browser ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .combinedClickable(
                                                        onClick = { onBrowserSelected(browser) },
                                                        onLongClick = { onBrowserLongPressed(browser) }
                                                    )
                                                    .padding(8.dp)
                                            ) {
                                                if (appearance.showIcons) {
                                                    BrowserIcon(packageName = browser.packageName, size = appearance.iconSize.dp)
                                                }
                                                if (appearance.showNames) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = browser.label,
                                                        fontSize = appearance.textSize.sp,
                                                        lineHeight = appearance.textSize.sp,
                                                        color = Color(appearance.textColor),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.widthIn(max = 120.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (rowIndex < rows.lastIndex) {
                                        Spacer(modifier = Modifier.height(appearance.itemSpacing.dp))
                                    }
                                }
                            }
                        }
                    }

                    ChooserDisplayMode.LIST -> {
                        browsers.forEachIndexed { index, browser ->
                            Box(
                                contentAlignment = when (appearance.alignment) {
                                    ChooserAlignment.START -> Alignment.CenterStart
                                    ChooserAlignment.CENTER -> Alignment.Center
                                    ChooserAlignment.END -> Alignment.CenterEnd
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onBrowserSelected(browser) },
                                        onLongClick = { onBrowserLongPressed(browser) }
                                    )
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    if (appearance.showIcons) {
                                        BrowserIcon(packageName = browser.packageName, size = appearance.iconSize.dp)
                                    }
                                    if (appearance.showNames) {
                                        Text(
                                            text = browser.label,
                                            fontSize = appearance.textSize.sp,
                                            color = Color(appearance.textColor)
                                        )
                                    }
                                }
                            }
                            if (index < browsers.lastIndex) {
                                Spacer(modifier = Modifier.height(appearance.itemSpacing.dp))
                            }
                        }
                    }
                }
            }
        }

        // Transparent spacer pushes the Surface upward - tappable to dismiss with animation
        Spacer(
            modifier = Modifier
                .height(spacerHeight)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = animatedDismiss
                )
        )
    }
}
