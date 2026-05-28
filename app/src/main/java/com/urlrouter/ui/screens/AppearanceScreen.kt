package com.urlrouter.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urlrouter.model.ChooserAlignment
import com.urlrouter.model.ChooserDisplayMode
import com.urlrouter.ui.MainViewModel
import com.urlrouter.ui.components.BrowserChooserSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val saved by viewModel.appearance.collectAsStateWithLifecycle()
    var settings by remember(saved) { mutableStateOf(saved) }
    var showPreview by remember { mutableStateOf(false) }
    var showBgColourPicker by remember { mutableStateOf(false) }
    var showTextColourPicker by remember { mutableStateOf(false) }
    val enabledBrowsers by viewModel.enabledBrowsers.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showPreview = true }) { Text("Preview") }
                    TextButton(onClick = { viewModel.saveAppearance(settings); onBack() }) { Text("Save") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Background Colour
            SectionTitle("Background Colour")
            ColourButton(
                colour = Color(settings.backgroundColor),
                onClick = { showBgColourPicker = true }
            )

            // Display Mode
            SectionTitle("Display Mode")
            DisplayModeRow(
                selected = settings.displayMode,
                onSelected = { settings = settings.copy(displayMode = it) }
            )

            // Show Browser Icons
            LabeledSwitch("Show Browser Icons", settings.showIcons) {
                settings = settings.copy(showIcons = it)
            }
            if (settings.showIcons) {
                SectionTitle("Icon Size: ${settings.iconSize}dp")
                Slider(
                    value = settings.iconSize.toFloat(),
                    onValueChange = { settings = settings.copy(iconSize = it.toInt()) },
                    valueRange = 24f..72f,
                    steps = 47
                )
            }

            // Show Browser Names
            LabeledSwitch("Show Browser Names", settings.showNames) {
                settings = settings.copy(showNames = it)
            }
            if (settings.showNames) {
                SectionTitle("Text Colour")
                ColourButton(
                    colour = Color(settings.textColor),
                    onClick = { showTextColourPicker = true }
                )
                SectionTitle("Text Size: ${settings.textSize}sp")
                Slider(
                    value = settings.textSize.toFloat(),
                    onValueChange = { settings = settings.copy(textSize = it.toInt()) },
                    valueRange = 8f..24f,
                    steps = 15
                )
            }

            // Alignment
            SectionTitle("Alignment")
            SegmentedRow(
                options = ChooserAlignment.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercaseChar() } },
                selectedIndex = ChooserAlignment.entries.indexOf(settings.alignment),
                onSelected = { settings = settings.copy(alignment = ChooserAlignment.entries[it]) }
            )

            // Vertical Position
            SectionTitle("Vertical Position: ${(settings.verticalPosition * 100).toInt()}%")
            Slider(
                value = settings.verticalPosition,
                onValueChange = { settings = settings.copy(verticalPosition = it) },
                valueRange = 0f..1f
            )

            // Corner Radius
            SectionTitle("Corner Radius: ${settings.cornerRadius}dp")
            Slider(
                value = settings.cornerRadius.toFloat(),
                onValueChange = { settings = settings.copy(cornerRadius = it.toInt()) },
                valueRange = 0f..40f,
                steps = 39
            )

            // Padding
            SectionTitle("Padding: ${settings.padding}dp")
            Slider(
                value = settings.padding.toFloat(),
                onValueChange = { settings = settings.copy(padding = it.toInt()) },
                valueRange = 4f..32f,
                steps = 27
            )

            // Item Spacing
            SectionTitle("Item Spacing: ${settings.itemSpacing}dp")
            Slider(
                value = settings.itemSpacing.toFloat(),
                onValueChange = { settings = settings.copy(itemSpacing = it.toInt()) },
                valueRange = 0f..24f,
                steps = 23
            )
        }
    }

    // Background colour picker dialog
    if (showBgColourPicker) {
        ColourPickerDialog(
            title = "Background Colour",
            initialColour = Color(settings.backgroundColor),
            onColourSelected = {
                settings = settings.copy(backgroundColor = it.toArgb().toLong() and 0xFFFFFFFFL)
                showBgColourPicker = false
            },
            onDismiss = { showBgColourPicker = false }
        )
    }

    // Text colour picker dialog
    if (showTextColourPicker) {
        ColourPickerDialog(
            title = "Text Colour",
            initialColour = Color(settings.textColor),
            onColourSelected = {
                settings = settings.copy(textColor = it.toArgb().toLong() and 0xFFFFFFFFL)
                showTextColourPicker = false
            },
            onDismiss = { showTextColourPicker = false }
        )
    }

    // Preview
    if (showPreview) {
        BrowserChooserSheet(
            browsers = enabledBrowsers,
            appearance = settings,
            onBrowserSelected = { showPreview = false },
            onBrowserLongPressed = { showPreview = false },
            onDismiss = { showPreview = false }
        )
    }
}

// ---------------------------------------------------------------------------
// Colour button — shows current colour as a swatch, opens picker on tap
// ---------------------------------------------------------------------------

@Composable
private fun ColourButton(colour: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // Checkerboard + colour swatch
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cell = 8.dp.toPx()
                val cols = (size.width / cell).toInt() + 1
                val rows = (size.height / cell).toInt() + 1
                for (r in 0..rows) for (c in 0..cols) {
                    drawRect(
                        color = if ((r + c) % 2 == 0) Color.LightGray else Color.White,
                        topLeft = Offset(c * cell, r * cell),
                        size = androidx.compose.ui.geometry.Size(cell, cell)
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize().background(colour))
        }
        Text(
            text = colour.toHex(),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Change",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ---------------------------------------------------------------------------
// Colour picker dialog
// ---------------------------------------------------------------------------

@Composable
private fun ColourPickerDialog(
    title: String,
    initialColour: Color,
    onColourSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // State lives inside the dialog — only committed on OK
    val initialHsv = remember { initialColour.toHsv() }
    var hue by remember { mutableStateOf(initialHsv[0]) }
    var sat by remember { mutableStateOf(initialHsv[1]) }
    var bri by remember { mutableStateOf(initialHsv[2]) }
    var alpha by remember { mutableStateOf(initialColour.alpha) }
    var hexInput by remember { mutableStateOf(initialColour.toHex()) }
    var hexError by remember { mutableStateOf(false) }

    val currentColour = Color.hsv(hue, sat, bri, alpha)

    fun syncHex() {
        hexInput = currentColour.toHex()
        hexError = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)

                SatBriPanel(hue = hue, sat = sat, bri = bri) { s, b -> sat = s; bri = b; syncHex() }
                HueSlider(hue = hue) { hue = it; syncHex() }
                AlphaSlider(alpha = alpha, colour = Color.hsv(hue, sat, bri)) { alpha = it; syncHex() }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { input ->
                            hexInput = input
                            val parsed = parseHex(input)
                            if (parsed != null) {
                                val h = parsed.toHsv()
                                hue = h[0]; sat = h[1]; bri = h[2]; alpha = parsed.alpha
                                hexError = false
                            } else {
                                hexError = input.length > 1
                            }
                        },
                        label = { Text("Hex") },
                        isError = hexError,
                        supportingText = if (hexError) {{ Text("e.g. #FF1C1B1F") }} else null,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    // Preview swatch
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cell = 8.dp.toPx()
                            val cols = (size.width / cell).toInt() + 1
                            val rows = (size.height / cell).toInt() + 1
                            for (r in 0..rows) for (c in 0..cols) {
                                drawRect(
                                    color = if ((r + c) % 2 == 0) Color.LightGray else Color.White,
                                    topLeft = Offset(c * cell, r * cell),
                                    size = androidx.compose.ui.geometry.Size(cell, cell)
                                )
                            }
                        }
                        Box(modifier = Modifier.fillMaxSize().background(currentColour))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onColourSelected(currentColour) }) { Text("OK") }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Colour picker components (used inside the dialog)
// ---------------------------------------------------------------------------

@Composable
private fun SatBriPanel(hue: Float, sat: Float, bri: Float, onChanged: (Float, Float) -> Unit) {
    val hueColor = Color.hsv(hue, 1f, 1f)
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        onChanged(
                            (down.position.x / widthPx).coerceIn(0f, 1f),
                            (1f - down.position.y / heightPx).coerceIn(0f, 1f)
                        )
                        drag(down.id) { change ->
                            onChanged(
                                (change.position.x / widthPx).coerceIn(0f, 1f),
                                (1f - change.position.y / heightPx).coerceIn(0f, 1f)
                            )
                        }
                    }
                }
        ) {
            drawRect(brush = Brush.horizontalGradient(listOf(Color.White, hueColor)))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        }

        val cx = with(density) { (sat * widthPx).toDp() }
        val cy = with(density) { ((1f - bri) * heightPx).toDp() }
        Box(
            modifier = Modifier
                .offset(x = cx - 10.dp, y = cy - 10.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

@Composable
private fun HueSlider(hue: Float, onHueChanged: (Float) -> Unit) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        val widthPx = constraints.maxWidth.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        onHueChanged((down.position.x / widthPx * 360f).coerceIn(0f, 360f))
                        drag(down.id) { change ->
                            onHueChanged((change.position.x / widthPx * 360f).coerceIn(0f, 360f))
                        }
                    }
                }
        ) {
            drawRect(brush = Brush.horizontalGradient(colors = (0..12).map { Color.hsv(it * 30f, 1f, 1f) }))
        }

        val thumbX = with(density) { (hue / 360f * widthPx).toDp() } - 10.dp
        Box(
            modifier = Modifier
                .offset(x = thumbX, y = 4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.hsv(hue, 1f, 1f))
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

@Composable
private fun AlphaSlider(alpha: Float, colour: Color, onAlphaChanged: (Float) -> Unit) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        val widthPx = constraints.maxWidth.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        onAlphaChanged((down.position.x / widthPx).coerceIn(0f, 1f))
                        drag(down.id) { change ->
                            onAlphaChanged((change.position.x / widthPx).coerceIn(0f, 1f))
                        }
                    }
                }
        ) {
            val cellSize = 8.dp.toPx()
            val cols = (size.width / cellSize).toInt() + 1
            for (col in 0..cols) {
                drawRect(
                    color = if (col % 2 == 0) Color.LightGray else Color.White,
                    topLeft = Offset(col * cellSize, 0f),
                    size = androidx.compose.ui.geometry.Size(cellSize, size.height)
                )
            }
            drawRect(brush = Brush.horizontalGradient(listOf(Color.Transparent, colour)))
        }

        val thumbX = with(density) { (alpha * widthPx).toDp() } - 10.dp
        Box(
            modifier = Modifier
                .offset(x = thumbX, y = 4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(colour.copy(alpha = alpha))
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

// ---------------------------------------------------------------------------
// Display mode row with icons
// ---------------------------------------------------------------------------

@Composable
private fun DisplayModeRow(selected: ChooserDisplayMode, onSelected: (ChooserDisplayMode) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChooserDisplayMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { onSelected(mode) }
                    .padding(vertical = 10.dp, horizontal = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = when (mode) {
                            ChooserDisplayMode.GRID -> Icons.Default.GridView
                            ChooserDisplayMode.LIST -> Icons.AutoMirrored.Filled.ViewList
                        },
                        contentDescription = mode.name,
                        modifier = Modifier.size(18.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (mode) { ChooserDisplayMode.GRID -> "Grid"; ChooserDisplayMode.LIST -> "List" },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared UI helpers
// ---------------------------------------------------------------------------

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun LabeledSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SegmentedRow(options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { onSelected(index) }
                    .padding(vertical = 10.dp, horizontal = 4.dp)
            ) {
                Text(text = label, style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Colour helpers
// ---------------------------------------------------------------------------

private fun Color.toHsv(): FloatArray {
    val r = red; val g = green; val b = blue
    val max = maxOf(r, g, b); val min = minOf(r, g, b); val delta = max - min
    val sat = if (max == 0f) 0f else delta / max
    val hue = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else      -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0) it + 360f else it }
    return floatArrayOf(hue, sat, max)
}

private fun Color.toHex(): String = "#%08X".format(toArgb())

private fun parseHex(input: String): Color? {
    val clean = input.trimStart('#')
    return when (clean.length) {
        6 -> try { Color(0xFF000000L or clean.toLong(16)) } catch (e: NumberFormatException) { null }
        8 -> try { Color(clean.toLong(16)) } catch (e: NumberFormatException) { null }
        else -> null
    }
}
