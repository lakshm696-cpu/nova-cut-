package com.example.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.RgbFilter
import com.example.model.FilterPreset
import com.example.model.VideoClip
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCharcoal
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaGold
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaPurpleLight
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Builds the list of AndroidX Media3 [Effect] instances corresponding to a [FilterPreset]
 * and its [intensity] (0f..1f), combined with the active track's fine-tuning color adjustments.
 *
 * Uses Media3's:
 * - [RgbFilter.createGrayscaleFilter] for monochrome / B&W film presets
 * - [RgbFilter.createInvertedFilter] for negative / solarize presets
 * - [RgbAdjustment] for 3-channel LUT color scaling and brightness offset
 * - [Contrast] for tonal dynamic range expansion/compression
 * - [HslAdjustment] for hue rotation, saturation boost/cut, and lightness shifts
 */
@OptIn(UnstableApi::class)
fun buildFilterPresetMedia3Effects(
    preset: FilterPreset,
    intensity: Float = 1.0f,
    brightness: Float = 0f,
    contrast: Float = 1.0f,
    saturation: Float = 1.0f
): List<Effect> {
    val effects = mutableListOf<Effect>()
    val amt = intensity.coerceIn(0f, 1f)

    // 1. Monochrome / Inverted Media3 RgbFilter transformations
    if (preset.isGrayscale && amt >= 0.35f) {
        effects.add(RgbFilter.createGrayscaleFilter())
    }
    if (preset.isInverted && amt >= 0.35f) {
        effects.add(RgbFilter.createInvertedFilter())
    }

    // 2. Interpolated R, G, B channel scales + brightness offset via Media3 RgbAdjustment
    val rScale = (1.0f + (preset.rScale - 1.0f) * amt + brightness).coerceIn(0.2f, 2.2f)
    val gScale = (1.0f + (preset.gScale - 1.0f) * amt + brightness).coerceIn(0.2f, 2.2f)
    val bScale = (1.0f + (preset.bScale - 1.0f) * amt + brightness).coerceIn(0.2f, 2.2f)

    if (abs(rScale - 1.0f) > 0.015f || abs(gScale - 1.0f) > 0.015f || abs(bScale - 1.0f) > 0.015f) {
        effects.add(
            RgbAdjustment.Builder()
                .setRedScale(rScale)
                .setGreenScale(gScale)
                .setBlueScale(bScale)
                .build()
        )
    }

    // 3. Interpolated Contrast transformation via Media3 Contrast
    val presetContrastDelta = (preset.contrastBoost - 1.0f) * amt
    val clipContrastDelta = contrast - 1.0f
    val totalContrast = (presetContrastDelta + clipContrastDelta).coerceIn(-0.8f, 0.85f)
    if (abs(totalContrast) > 0.015f) {
        effects.add(Contrast(totalContrast))
    }

    // 4. Interpolated Hue, Saturation & Lightness via Media3 HslAdjustment
    val effectiveSatMultiplier = (1.0f + (preset.saturationBoost - 1.0f) * amt) * saturation
    val satAdjustment = ((effectiveSatMultiplier - 1.0f) * 85f).coerceIn(-100f, 100f)
    val hueAdjustment = (preset.hueShiftDegrees * amt).coerceIn(-180f, 180f)
    val lightnessAdjustment = (brightness * 45f).coerceIn(-50f, 50f)

    if (abs(satAdjustment) > 1.0f || abs(hueAdjustment) > 0.5f || abs(lightnessAdjustment) > 1.0f) {
        val hslBuilder = HslAdjustment.Builder()
        if (abs(hueAdjustment) > 0.5f) {
            hslBuilder.adjustHue(hueAdjustment)
        }
        if (abs(satAdjustment) > 1.0f) {
            hslBuilder.adjustSaturation(satAdjustment)
        }
        if (abs(lightnessAdjustment) > 1.0f) {
            hslBuilder.adjustLightness(lightnessAdjustment)
        }
        effects.add(hslBuilder.build())
    }

    return effects
}

/**
 * Generates concise telemetry labels describing each active Media3 [Effect] in the filter chain.
 */
fun describeFilterMedia3Effects(
    preset: FilterPreset,
    intensity: Float,
    brightness: Float,
    contrast: Float,
    saturation: Float
): List<String> {
    val amt = intensity.coerceIn(0f, 1f)
    val descriptors = mutableListOf<String>()

    if (preset.isGrayscale && amt >= 0.35f) {
        descriptors.add("RgbFilter.Grayscale")
    }
    if (preset.isInverted && amt >= 0.35f) {
        descriptors.add("RgbFilter.Inverted")
    }

    val rScale = (1.0f + (preset.rScale - 1.0f) * amt + brightness).coerceIn(0.2f, 2.2f)
    val gScale = (1.0f + (preset.gScale - 1.0f) * amt + brightness).coerceIn(0.2f, 2.2f)
    val bScale = (1.0f + (preset.bScale - 1.0f) * amt + brightness).coerceIn(0.2f, 2.2f)
    if (abs(rScale - 1.0f) > 0.015f || abs(gScale - 1.0f) > 0.015f || abs(bScale - 1.0f) > 0.015f) {
        descriptors.add(
            String.format(Locale.US, "RgbAdjustment(%.2f, %.2f, %.2f)", rScale, gScale, bScale)
        )
    }

    val totalContrast = (((preset.contrastBoost - 1.0f) * amt) + (contrast - 1.0f)).coerceIn(-0.8f, 0.85f)
    if (abs(totalContrast) > 0.015f) {
        descriptors.add(String.format(Locale.US, "Contrast(%+.2f)", totalContrast))
    }

    val effectiveSatMultiplier = (1.0f + (preset.saturationBoost - 1.0f) * amt) * saturation
    val satAdjustment = ((effectiveSatMultiplier - 1.0f) * 85f).coerceIn(-100f, 100f)
    val hueAdjustment = (preset.hueShiftDegrees * amt).coerceIn(-180f, 180f)
    if (abs(satAdjustment) > 1.0f || abs(hueAdjustment) > 0.5f) {
        descriptors.add(
            String.format(Locale.US, "HslAdjustment(hue=%+.0f°, sat=%+.0f)", hueAdjustment, satAdjustment)
        )
    }

    if (descriptors.isEmpty()) {
        descriptors.add("Identity (Unfiltered Track)")
    }
    return descriptors
}

/**
 * Material 3 Bottom Sheet interface presenting a categorized grid of selectable video filters
 * that leverage AndroidX Media3 [Effect] transformations (`RgbAdjustment`, `Contrast`,
 * `HslAdjustment`, `RgbFilter`) on the active timeline track.
 */
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(UnstableApi::class)
@Composable
fun VideoFiltersBottomSheet(
    selectedClip: VideoClip?,
    totalTrackClips: Int,
    onSelectFilter: (FilterPreset, Boolean) -> Unit,
    onUpdateFilterIntensity: (Float, Boolean) -> Unit,
    onUpdateColorAdjustments: (Float?, Float?, Float?, Float?) -> Unit,
    onResetFilterAndColor: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf("All") }
    var applyToAllClips by remember { mutableStateOf(false) }
    var showFineTuneSliders by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("All", "Luxury", "Cinema", "Warm", "Film", "B&W", "Aesthetic", "Cyber", "LUT")
    }

    val filteredPresets = remember(selectedCategory) {
        if (selectedCategory == "All") {
            FilterPreset.entries
        } else {
            FilterPreset.entries.filter {
                it.category.equals(selectedCategory, ignoreCase = true) || it == FilterPreset.ORIGINAL
            }
        }
    }

    val currentPreset = selectedClip?.filterPreset ?: FilterPreset.ORIGINAL
    val currentIntensity = selectedClip?.filterIntensity ?: 1.0f
    val currentBrightness = selectedClip?.brightness ?: 0f
    val currentContrast = selectedClip?.contrast ?: 1.0f
    val currentSaturation = selectedClip?.saturation ?: 1.0f
    val currentVignette = selectedClip?.vignette ?: 0f

    val activeMedia3Effects = remember(
        currentPreset,
        currentIntensity,
        currentBrightness,
        currentContrast,
        currentSaturation
    ) {
        buildFilterPresetMedia3Effects(
            preset = currentPreset,
            intensity = currentIntensity,
            brightness = currentBrightness,
            contrast = currentContrast,
            saturation = currentSaturation
        )
    }

    val effectTelemetryLabels = remember(
        currentPreset,
        currentIntensity,
        currentBrightness,
        currentContrast,
        currentSaturation
    ) {
        describeFilterMedia3Effects(
            preset = currentPreset,
            intensity = currentIntensity,
            brightness = currentBrightness,
            contrast = currentContrast,
            saturation = currentSaturation
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NovaCharcoal,
        contentColor = NovaTextPrimary,
        scrimColor = Color(0xB3040508),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 42.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(NovaGold.copy(alpha = 0.65f))
                )
            }
        },
        modifier = Modifier.testTag("video_filters_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Sheet Header Bar with Active Track & Reset / Close Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = NovaGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, NovaGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FilterVintage,
                                contentDescription = null,
                                tint = NovaGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MEDIA3 VIDEO FILTERS",
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = NovaPurple.copy(alpha = 0.20f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.border(
                                    0.5.dp,
                                    NovaPurpleLight.copy(alpha = 0.6f),
                                    RoundedCornerShape(6.dp)
                                )
                            ) {
                                Text(
                                    text = "${activeMedia3Effects.size} FX",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaPurpleLight,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (applyToAllClips) {
                                "Target: All $totalTrackClips Timeline Clips • ${currentPreset.label}"
                            } else {
                                "Active Track: ${selectedClip?.title ?: "Main Track"} • ${currentPreset.label}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = NovaSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(0.8.dp, NovaBorder, RoundedCornerShape(8.dp))
                            .clickable { onResetFilterAndColor(applyToAllClips) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("filter_sheet_reset_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Filters",
                                tint = NovaTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("filter_sheet_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Filters Sheet",
                            tint = NovaTextPrimary
                        )
                    }
                }
            }

            // 2. Active Media3 Effect Pipeline Telemetry & Apply-to-All Bar
            Surface(
                color = NovaObsidian,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.8.dp, NovaBorder, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NovaCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            effectTelemetryLabels.forEach { badge ->
                                Surface(
                                    color = NovaSurface,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.border(
                                        0.5.dp,
                                        NovaCyan.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                ) {
                                    Text(
                                        text = badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NovaCyan,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "All Clips",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaTextSecondary,
                                fontSize = 10.sp
                            )
                            Switch(
                                checked = applyToAllClips,
                                onCheckedChange = { applyToAllClips = it },
                                modifier = Modifier
                                    .scale(0.68f)
                                    .testTag("filter_sheet_apply_all_switch")
                            )
                        }
                    }
                }
            }

            // 3. Category Filter Pills + Fine-Tune Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            color = if (isSelected) NovaGold.copy(alpha = 0.22f) else NovaSurface,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedCategory = category }
                                .border(
                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                    color = if (isSelected) NovaGold else NovaBorder,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 11.dp, vertical = 5.dp)
                                .testTag("filter_category_chip_$category")
                        ) {
                            Text(
                                text = if (category == "All") "All (${FilterPreset.entries.size})" else category,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) NovaGold else NovaTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (showFineTuneSliders) NovaPurple.copy(alpha = 0.24f) else NovaSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (showFineTuneSliders) NovaPurpleLight else NovaBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { showFineTuneSliders = !showFineTuneSliders }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("filter_sheet_toggle_sliders_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Fine Tune Media3 Parameters",
                            tint = if (showFineTuneSliders) NovaPurpleLight else NovaTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tune",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (showFineTuneSliders) NovaPurpleLight else NovaTextSecondary
                        )
                    }
                }
            }

            // 4. Selectable Grid of Video Filters
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 210.dp, max = 290.dp)
                    .testTag("video_filters_grid"),
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPresets, key = { it.name }) { preset ->
                    val isSelected = currentPreset == preset
                    FilterGridCard(
                        preset = preset,
                        isSelected = isSelected,
                        intensity = if (isSelected) currentIntensity else 1.0f,
                        onClick = {
                            onSelectFilter(preset, applyToAllClips)
                        }
                    )
                }
            }

            // 5. Filter Intensity & Optional Fine-Tune Media3 Color Sliders
            Surface(
                color = NovaSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.8.dp, NovaBorder, RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FILTER INTENSITY (${currentPreset.label.uppercase(Locale.US)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(currentIntensity * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = currentIntensity,
                        onValueChange = { onUpdateFilterIntensity(it, applyToAllClips) },
                        valueRange = 0.0f..1.0f,
                        enabled = currentPreset != FilterPreset.ORIGINAL,
                        colors = SliderDefaults.colors(
                            thumbColor = NovaGold,
                            activeTrackColor = NovaGold,
                            inactiveTrackColor = NovaObsidian
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .testTag("filter_sheet_intensity_slider")
                    )

                    if (showFineTuneSliders && selectedClip != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SheetMiniSlider(
                                    label = "Brightness",
                                    valueText = String.format(Locale.US, "%+.2f", currentBrightness),
                                    value = currentBrightness,
                                    valueRange = -0.4f..0.4f,
                                    accentColor = NovaCyan,
                                    onValueChange = { onUpdateColorAdjustments(it, null, null, null) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SheetMiniSlider(
                                    label = "Contrast",
                                    valueText = String.format(Locale.US, "%.2fx", currentContrast),
                                    value = currentContrast,
                                    valueRange = 0.5f..1.8f,
                                    accentColor = NovaPurpleLight,
                                    onValueChange = { onUpdateColorAdjustments(null, it, null, null) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                SheetMiniSlider(
                                    label = "Saturation",
                                    valueText = String.format(Locale.US, "%.2fx", currentSaturation),
                                    value = currentSaturation,
                                    valueRange = 0.0f..2.0f,
                                    accentColor = NovaEmerald,
                                    onValueChange = { onUpdateColorAdjustments(null, null, it, null) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SheetMiniSlider(
                                    label = "Vignette",
                                    valueText = "${(currentVignette * 100).roundToInt()}%",
                                    value = currentVignette,
                                    valueRange = 0.0f..1.0f,
                                    accentColor = NovaGold,
                                    onValueChange = { onUpdateColorAdjustments(null, null, null, it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterGridCard(
    preset: FilterPreset,
    isSelected: Boolean,
    intensity: Float,
    onClick: () -> Unit
) {
    val accentColor = Color(preset.accentHex)

    Surface(
        color = if (isSelected) accentColor.copy(alpha = 0.16f) else NovaSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else NovaBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("filter_grid_item_${preset.name}")
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Miniature Cinema Frame Preview Swatch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NovaObsidian)
                    .border(0.6.dp, NovaBorder, RoundedCornerShape(10.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rNorm = (preset.rScale / 1.38f).coerceIn(0.15f, 1f)
                    val gNorm = (preset.gScale / 1.38f).coerceIn(0.15f, 1f)
                    val bNorm = (preset.bScale / 1.38f).coerceIn(0.15f, 1f)

                    val skyTop = if (preset.isGrayscale) {
                        Color(0.18f, 0.18f, 0.20f)
                    } else if (preset.isInverted) {
                        Color(1f - rNorm * 0.6f, 1f - gNorm * 0.6f, 1f - bNorm * 0.5f)
                    } else {
                        Color(rNorm * 0.35f, gNorm * 0.28f, bNorm * 0.48f)
                    }

                    val horizonGlow = if (preset.isGrayscale) {
                        Color(0.75f, 0.75f, 0.78f)
                    } else if (preset.isInverted) {
                        Color(0.95f, 0.25f, 0.55f)
                    } else {
                        Color(rNorm, gNorm * 0.85f, bNorm * 0.85f)
                    }

                    // Sky gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(skyTop, horizonGlow)
                        )
                    )

                    // Simulated Sun / Lens Orb
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = size.minDimension * 0.18f,
                        center = Offset(size.width * 0.72f, size.height * 0.36f)
                    )

                    // Foreground silhouette bar
                    drawRect(
                        color = Color(0xAA06070B),
                        topLeft = Offset(0f, size.height * 0.70f),
                        size = Size(size.width, size.height * 0.30f)
                    )

                    // R / G / B LUT channel micro-bars along bottom edge
                    val barWidth = size.width / 3.6f
                    val barY = size.height * 0.82f
                    drawRoundRect(
                        color = Color(0xFFFF5A67).copy(alpha = 0.85f),
                        topLeft = Offset(6f, barY),
                        size = Size((barWidth * (preset.rScale / 1.4f)).coerceAtLeast(6f), 4f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        color = NovaEmerald.copy(alpha = 0.85f),
                        topLeft = Offset(8f + barWidth, barY),
                        size = Size((barWidth * (preset.gScale / 1.4f)).coerceAtLeast(6f), 4f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        color = NovaCyan.copy(alpha = 0.85f),
                        topLeft = Offset(10f + barWidth * 2f, barY),
                        size = Size((barWidth * (preset.bScale / 1.4f)).coerceAtLeast(6f), 4f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }

                // Category Pill (Top-Start)
                Surface(
                    color = Color(0xCC08090D),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                ) {
                    Text(
                        text = preset.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                // Selected Checkmark (Top-End)
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected Filter",
                        tint = NovaGold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(16.dp)
                    )
                }
            }

            // Filter Title & Media3 Effect Summary
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) NovaGold else NovaTextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected && preset != FilterPreset.ORIGINAL) {
                        Text(
                            text = "${(intensity * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaCyan,
                            fontSize = 8.sp
                        )
                    }
                }
                Text(
                    text = preset.effectSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextMuted,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SheetMiniSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary, fontSize = 10.sp)
            Text(valueText, style = MaterialTheme.typography.labelSmall, color = accentColor, fontSize = 10.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = NovaObsidian
            ),
            modifier = Modifier.height(22.dp)
        )
    }
}
