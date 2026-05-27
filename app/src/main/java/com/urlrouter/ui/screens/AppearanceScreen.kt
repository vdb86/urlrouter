package com.urlrouter.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.urlrouter.model.ChooserAlignment
import com.urlrouter.model.ChooserDisplayMode
import com.urlrouter.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val saved by viewModel.appearance.collectAsStateWithLifecycle()
    var settings by remember(saved) { mutableStateOf(saved) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
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
            SectionTitle("Popup Background Colour")
            ColourPicker(
                currentColour = Color(settings.backgroundColor),
                onColourChanged = { settings = settings.copy(backgroundColor = it.toArgb().toLong() and 0xFFFFFFFFL) }
            )

            SectionTitle("Display Mode")
            SegmentedRow(
                options = ChooserDisplayMode.entries.map { it.label() },
                selectedIndex = ChooserDisplayMode.entries.indexOf(settings.displayMode),
                onSelected = { settings = settings.copy(displayMode = ChooserDisplayMode.entries[it]) }
            )

            SectionTitle("Alignment")
            SegmentedRow(
                options = ChooserAlignment.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercaseChar() } },
                selectedIndex = ChooserAlignment.entries.indexOf(settings.alignment),
                onSelected = { settings = settings.copy(alignment = ChooserAlignment.entries[it]) }
            )

            SectionTitle("Visibility")
            LabeledSwitch("Show Browser Icons", settings.showIcons) { settings = settings.copy(showIcons = it) }
            LabeledSwitch("Show Browser Names", settings.showNames) { settings = settings.copy(showNames = it) }

            SectionTitle("Corner Radius: ${settings.cornerRadius}dp")
            Slider(value = settings.cornerRadius.toFloat(), onValueChange = { settings = settings.copy(cornerRadius = it.toInt()) }, valueRange = 0f..40f, steps = 39)

            SectionTitle("Elevation: ${settings.elevation}dp")
            Slider(value = settings.elevation.toFloat(), onValueChange = { settings = settings.copy(elevation = it.toInt()) }, valueRange = 0f..24f, steps = 23)

            SectionTitle("Padding: ${settings.padding}dp")
            Slider(value = settings.padding.toFloat(), onValueChange = { settings = settings.copy(padding = it.toInt()) }, valueRange = 4f..32f, steps = 27)

            SectionTitle("Item Spacing: ${settings.itemSpacing}dp")
            Slider(value = settings.itemSpacing.toFloat(), onValueChange = { settings = settings.copy(itemSpacing = it.toInt()) }, valueRange = 0f..24f, steps = 23)

            SectionTitle("Animations")
            LabeledSwitch("Enable Animations", settings.animationsEnabled) { settings = settings.copy(animationsEnabled = it) }
            if (settings.animationsEnabled) {
                SectionTitle("Animation Duration: ${settings.animationDurationMs}ms")
                Slider(value = settings.animationDurationMs.toFloat(), onValueChange = { settings = settings.copy(animationDurationMs = it.toInt()) }, valueRange = 100f..800f, steps = 13)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Colour picker — SB panel + hue slider + hex input
// ---------------------------------------------------------------------------

@Composable
private fun ColourPicker(currentColour: Color, onColourChanged: (Color) -> Unit) {
    val initialHsv = remember { currentColour.toHsv() }
    var hue by remember { mutableStateOf(initialHsv[0]) }
    var sat by remember { mutableStateOf(initialHsv[1]) }
    var bri by remember { mutableStateOf(initialHsv[2]) }
    var alpha by remember { mutableStateOf(currentColour.alpha) }

    var hexInput by remember { mutableStateOf(currentColour.toHex()) }
    var hexError by remember { mutableStateOf(false) }

    fun emit() {
        val c = Color.hsv(hue, sat, bri, alpha)
        onColourChanged(c)
        hexInput = c.toHex()
        hexError = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SatBriPanel(
            hue = hue, sat = sat, bri = bri,
            onChanged = { s, b -> sat = s; bri = b; emit() }
        )

        HueSlider(hue = hue, onHueChanged = { hue = it; emit() })

        // Transparency slider (0% = fully opaque, 100% = fully transparent)
        val transparencyPct = ((1f - alpha) * 100).toInt()
        SectionTitle("Transparency: $transparencyPct%")
        Slider(
            value = 1f - alpha,
            onValueChange = { alpha = 1f - it; emit() },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = hexInput,
                onValueChange = { input ->
                    hexInput = input
                    val parsed = parseHex(input)
                    if (parsed != null) {
                        val h = parsed.toHsv()
                        hue = h[0]; sat = h[1]; bri = h[2]; alpha = parsed.alpha
                        onColourChanged(parsed)
                        hexError = false
                    } else {
                        hexError = input.length > 1
                    }
                },
                label = { Text("Hex (RRGGBB or AARRGGBB)") },
                placeholder = { Text("#1C1B1F") },
                isError = hexError,
                supportingText = if (hexError) {{ Text("Enter 6 or 8 hex digits") }} else null,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.hsv(hue, sat, bri, alpha))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            )
        }
    }
}

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

        fun handleOffset(offset: Offset) {
            val s = (offset.x / widthPx).coerceIn(0f, 1f)
            val b = (1f - offset.y / heightPx).coerceIn(0f, 1f)
            onChanged(s, b)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { handleOffset(it) } }
                .pointerInput(Unit) { detectDragGestures { change, _ -> handleOffset(change.position) } }
        ) {
            drawRect(brush = Brush.horizontalGradient(listOf(Color.White, hueColor)))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        }

        // Selector circle — convert px to dp using LocalDensity
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

        fun handleOffset(x: Float) {
            onHueChanged((x / widthPx * 360f).coerceIn(0f, 360f))
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { handleOffset(it.x) } }
                .pointerInput(Unit) { detectDragGestures { change, _ -> handleOffset(change.position.x) } }
        ) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = (0..12).map { Color.hsv(it * 30f, 1f, 1f) }
                )
            )
        }

        // Hue thumb — convert px to dp using LocalDensity
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


private fun Color.toHsv(): FloatArray {
    val r = red; val g = green; val b = blue
    val max = maxOf(r, g, b); val min = minOf(r, g, b); val delta = max - min
    val bri = max
    val sat = if (max == 0f) 0f else delta / max
    val hue = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else      -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0) it + 360f else it }
    return floatArrayOf(hue, sat, bri)
}

private fun Color.toHex(): String {
    val argb = toArgb()
    val a = (argb ushr 24) and 0xFF
    val rgb = argb and 0x00FFFFFF
    return if (a == 0xFF) "#%06X".format(rgb) else "#%02X%06X".format(a, rgb)
}

private fun parseHex(input: String): Color? {
    val clean = input.trimStart('#')
    return try {
        when (clean.length) {
            6 -> Color(0xFF000000L or clean.toLong(16))
            8 -> Color(clean.toLong(16))
            else -> null
        }
    } catch (e: NumberFormatException) { null }
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

private fun ChooserDisplayMode.label() = when (this) {
    ChooserDisplayMode.HORIZONTAL_ICONS -> "Icons"
    ChooserDisplayMode.HORIZONTAL_ICON_TEXT -> "Icon + Text"
    ChooserDisplayMode.VERTICAL_LIST -> "List"
}
