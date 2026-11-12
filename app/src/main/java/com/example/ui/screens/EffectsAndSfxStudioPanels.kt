package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppScreen
import com.example.model.AudioTrackItem
import com.example.model.BuiltInEffectStorePacks
import com.example.model.BuiltInSoundEffectsCatalog
import com.example.model.EffectStorePack
import com.example.model.SfxCategory
import com.example.model.SoundEffectAsset
import com.example.model.TransitionType
import com.example.model.VideoClip
import com.example.model.VideoEffectCategory
import com.example.model.VideoEffectItem
import com.example.model.VideoEffectType
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCharcoal
import com.example.ui.theme.NovaCoralRed
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaViolet
import com.example.viewmodel.NovaCutViewModel

/**
 * 1. Advanced Video Effects Studio Panel:
 * - Search bar + Category tabs (Cinematic, Trending, AI & Creative, Motion)
 * - Effect Preview Thumbnail Cards for all 32 effects
 * - Adjustable Intensity slider, Duration / Timeline Drag sliders, Stack / Replace / Remove controls
 */
@Composable
fun VideoEffectsStudioPanel(viewModel: NovaCutViewModel) {
    val videoEffects by viewModel.videoEffects.collectAsStateWithLifecycle()
    val selectedEffectId by viewModel.selectedEffectId.collectAsStateWithLifecycle()
    val totalDurationMs = viewModel.totalTimelineDurationMs

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<VideoEffectCategory?>(null) }
    var bindToSelectedClip by remember { mutableStateOf(false) }
    var pendingIntensity by remember { mutableFloatStateOf(0.82f) }

    val selectedEffect = videoEffects.firstOrNull { it.id == selectedEffectId } ?: videoEffects.lastOrNull()

    val filteredEffects = remember(searchQuery, selectedCategory) {
        VideoEffectType.entries.filter { fx ->
            val matchesCat = selectedCategory == null || fx.matchesCategory(selectedCategory!!)
            val matchesQuery = searchQuery.isBlank() ||
                fx.label.contains(searchQuery, ignoreCase = true) ||
                fx.subtitle.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search Bar & Effects Store Shortcut
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search 32 effects (Glitch, VHS, Flare, Neon...)", color = NovaTextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(16.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaCyan,
                    unfocusedBorderColor = NovaBorder,
                    focusedTextColor = NovaTextPrimary,
                    unfocusedTextColor = NovaTextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("video_effects_search_input")
            )

            Surface(
                color = NovaMagenta.copy(alpha = 0.22f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clickable { viewModel.navigateTo(AppScreen.EFFECTS_STORE) }
                    .border(1.dp, NovaMagenta, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .testTag("open_fx_store_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, contentDescription = "FX Store", tint = NovaMagenta, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Store", style = MaterialTheme.typography.labelLarge, color = NovaMagenta)
                }
            }
        }

        // Category Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All (${VideoEffectType.entries.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NovaCyan.copy(alpha = 0.22f),
                    selectedLabelColor = NovaCyan
                )
            )
            VideoEffectCategory.entries.forEach { cat ->
                val isSel = selectedCategory == cat
                val catColor = Color(cat.accentHex)
                FilterChip(
                    selected = isSel,
                    onClick = { selectedCategory = if (isSel) null else cat },
                    label = { Text(cat.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = catColor.copy(alpha = 0.22f),
                        selectedLabelColor = catColor
                    ),
                    modifier = Modifier.testTag("fx_cat_${cat.name}")
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Clip Lock", style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
                Switch(
                    checked = bindToSelectedClip,
                    onCheckedChange = { bindToSelectedClip = it },
                    modifier = Modifier.scale(0.72f)
                )
            }
        }

        // Horizontal Visual Effect Preview Thumbnail Cards
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredEffects, key = { it.name }) { fxType ->
                val accent = Color(fxType.accentHex)
                val isCurrentlySelected = selectedEffect?.effectType == fxType && selectedEffect.customEffectName == null
                Surface(
                    color = if (isCurrentlySelected) accent.copy(alpha = 0.22f) else NovaSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .width(136.dp)
                        .clickable {
                            viewModel.addVideoEffectToTimeline(
                                effectType = fxType,
                                intensity = pendingIntensity,
                                bindToSelectedClip = bindToSelectedClip
                            )
                        }
                        .border(
                            width = if (isCurrentlySelected) 2.dp else 1.dp,
                            color = if (isCurrentlySelected) accent else NovaBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(10.dp)
                        .testTag("video_effect_card_${fxType.name}")
                ) {
                    Column {
                        // Effect Thumbnail Preview Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(accent.copy(alpha = 0.45f), Color(0xFF08090D))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (fxType.category) {
                                    VideoEffectCategory.CINEMATIC,
                                    VideoEffectCategory.LIGHT,
                                    VideoEffectCategory.VINTAGE -> Icons.Default.Flare
                                    VideoEffectCategory.TRENDING,
                                    VideoEffectCategory.GLITCH,
                                    VideoEffectCategory.NEON -> Icons.Default.Bolt
                                    VideoEffectCategory.AI_CREATIVE -> Icons.Default.AutoAwesome
                                    VideoEffectCategory.MOTION -> Icons.Default.MotionPhotosOn
                                },
                                contentDescription = fxType.label,
                                tint = accent,
                                modifier = Modifier.size(24.dp)
                            )

                            if (fxType.isTrending || fxType.isNew) {
                                val badgeLabel = if (fxType.isNew) "NEW" else "HOT"
                                Surface(
                                    color = Color(0xCC08090D),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = badgeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accent,
                                        fontSize = 8.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = fxType.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = NovaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = fxType.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaTextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+ Stack FX",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent
                            )
                            if (selectedEffect != null) {
                                Text(
                                    text = "Replace",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaTextSecondary,
                                    modifier = Modifier.clickable {
                                        viewModel.replaceSelectedVideoEffect(fxType)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active / Selected Timeline Effect Controls (Intensity, Timeline Position & Duration, Stack Manager)
        if (selectedEffect != null) {
            val activeAccent = Color(selectedEffect.displayAccentHex)
            Surface(
                color = NovaSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeAccent, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selectedEffect.isAiCustom) Icons.Default.AutoAwesome else Icons.Default.Layers,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE FX: ${selectedEffect.displayLabel} (${videoEffects.size} Stacked)",
                                style = MaterialTheme.typography.labelLarge,
                                color = activeAccent
                            )
                        }
                        IconButton(
                            onClick = { viewModel.deleteVideoEffect(selectedEffect.id) },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("delete_selected_fx_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove Effect",
                                tint = NovaCoralRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Adjustable Effect Intensity Slider
                    StudioSliderRow(
                        label = "Intensity",
                        valueText = "${(selectedEffect.intensity * 100).toInt()}%",
                        value = selectedEffect.intensity,
                        valueRange = 0.1f..1.0f,
                        accent = activeAccent,
                        onValueChange = { newInt ->
                            pendingIntensity = newInt
                            viewModel.updateVideoEffectParameters(
                                effectId = selectedEffect.id,
                                intensity = newInt
                            )
                        }
                    )

                    // Timeline Position (Drag Start) & Duration End Sliders
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            StudioSliderRow(
                                label = "Start",
                                valueText = "${String.format("%.1fs", selectedEffect.startTimeMs / 1000f)}",
                                value = selectedEffect.startTimeMs.toFloat(),
                                valueRange = 0f..(selectedEffect.endTimeMs - 300L).coerceAtLeast(100L).toFloat(),
                                accent = NovaCyan,
                                onValueChange = { startMs ->
                                    viewModel.updateVideoEffectParameters(
                                        effectId = selectedEffect.id,
                                        startTimeMs = startMs.toLong()
                                    )
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            StudioSliderRow(
                                label = "End",
                                valueText = "${String.format("%.1fs", selectedEffect.endTimeMs / 1000f)}",
                                value = selectedEffect.endTimeMs.toFloat(),
                                valueRange = (selectedEffect.startTimeMs + 300L).toFloat()..totalDurationMs.coerceAtLeast(1000L).toFloat(),
                                accent = NovaCyan,
                                onValueChange = { endMs ->
                                    viewModel.updateVideoEffectParameters(
                                        effectId = selectedEffect.id,
                                        endTimeMs = endMs.toLong()
                                    )
                                }
                            )
                        }
                    }

                    // Stacked Effects Pills Row
                    if (videoEffects.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            videoEffects.forEachIndexed { idx, item ->
                                val isSel = item.id == selectedEffect.id
                                val c = Color(item.displayAccentHex)
                                Surface(
                                    color = if (isSel) c.copy(alpha = 0.25f) else NovaSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable { viewModel.selectVideoEffect(item.id) }
                                        .border(1.dp, if (isSel) c else NovaBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "#${idx + 1} ${item.displayLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSel) c else NovaTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Advanced Video Transitions Studio Panel:
 * - All 12 Professional Transitions (Smooth Zoom, Swipe, Slide, Spin, Fade, Flash, Glitch, Blur, Whip Pan, Light Leak, Film Burn, 3D Rotation)
 * - Adjustable Transition Duration slider (150ms - 2000ms)
 * - Live Preview Transition button + Apply to Selected / All Clips
 */
@Composable
fun TransitionsStudioPanel(
    viewModel: NovaCutViewModel,
    selectedClip: VideoClip?
) {
    var applyToAll by remember { mutableStateOf(false) }
    var transitionDurationMs by remember(selectedClip?.transitionDurationMs) {
        mutableLongStateOf(selectedClip?.transitionDurationMs ?: 500L)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PRO VIDEO TRANSITIONS (12 PRESETS)",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaCyan
                )
                Text(
                    text = "Clip: ${selectedClip?.title ?: "Select a clip"} • Active: ${selectedClip?.transitionIn?.label ?: "None"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { viewModel.previewSelectedTransition() },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("preview_transition_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("All", style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
                    Switch(
                        checked = applyToAll,
                        onCheckedChange = { applyToAll = it },
                        modifier = Modifier.scale(0.72f)
                    )
                }
            }
        }

        // Transition Cards Row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TransitionType.entries, key = { it.name }) { trans ->
                val isSelected = selectedClip?.transitionIn == trans
                val accent = Color(trans.badgeColorHex)
                Surface(
                    color = if (isSelected) accent.copy(alpha = 0.24f) else NovaSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .width(112.dp)
                        .clickable {
                            val dur = if (trans == TransitionType.NONE) 0L else transitionDurationMs
                            viewModel.updateSelectedClipTransition(
                                transition = trans,
                                durationMs = dur,
                                applyToAllClips = applyToAll
                            )
                        }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) accent else NovaBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(10.dp)
                        .testTag("transition_card_${trans.name}")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = trans.label,
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = trans.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) accent else NovaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (trans == TransitionType.NONE) "Cut" else "${trans.defaultDurationMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaTextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Transition Duration Slider & Sync SFX Shortcut
        Surface(
            color = NovaSurface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StudioSliderRow(
                    label = "Duration",
                    valueText = "${transitionDurationMs}ms",
                    value = transitionDurationMs.toFloat(),
                    valueRange = 150f..2000f,
                    accent = NovaMagenta,
                    onValueChange = { newDur ->
                        transitionDurationMs = newDur.toLong()
                        val currentTrans = selectedClip?.transitionIn ?: TransitionType.FADE
                        if (currentTrans != TransitionType.NONE) {
                            viewModel.updateSelectedClipTransition(
                                transition = currentTrans,
                                durationMs = transitionDurationMs,
                                applyToAllClips = applyToAll
                            )
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auto-attach Whoosh / Glitch SFX to all clip transitions",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.syncSoundEffectsToTransitions() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovaCyan,
                            contentColor = Color(0xFF001F24)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("transitions_sync_sfx_button")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync SFX", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 3. Searchable Sound Effects Library & Professional Audio Controls Panel:
 * - Search bar + 5 Categories (Cinematic, Social Media, Action, Transitions, Nature)
 * - 30+ Original Synthesized Sound Effects with real audio Preview & Add to Timeline
 * - One-tap "Sync SFX with Transitions"
 * - Pro Audio Controls for selected track: Trim In/Out, Volume, Fade In/Out, Speed, Pitch, Loop, Beat Markers, Noise Reduction, Audio Ducking
 */
@Composable
fun SoundEffectsStudioPanel(viewModel: NovaCutViewModel) {
    val audioTracks by viewModel.audioTracks.collectAsStateWithLifecycle()
    val selectedAudioId by viewModel.selectedAudioId.collectAsStateWithLifecycle()
    val previewingSfxId by viewModel.previewingSfxId.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SfxCategory?>(null) }

    val selectedTrack = audioTracks.firstOrNull { it.id == selectedAudioId } ?: audioTracks.lastOrNull()

    val filteredSfx = remember(searchQuery, selectedCategory) {
        BuiltInSoundEffectsCatalog.filter { sfx ->
            val matchesCat = selectedCategory == null || sfx.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                sfx.title.contains(searchQuery, ignoreCase = true) ||
                sfx.tags.contains(searchQuery, ignoreCase = true) ||
                sfx.category.label.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search Bar & Sync SFX to Transitions Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search 30+ SFX (Whoosh, Boom, Pop, Rain...)", color = NovaTextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaEmerald, modifier = Modifier.size(16.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaEmerald,
                    unfocusedBorderColor = NovaBorder,
                    focusedTextColor = NovaTextPrimary,
                    unfocusedTextColor = NovaTextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("sfx_search_input")
            )

            Button(
                onClick = { viewModel.syncSoundEffectsToTransitions() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaEmerald,
                    contentColor = Color(0xFF002114)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                modifier = Modifier.testTag("sync_sfx_transitions_button")
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sync Cuts", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }

        // 5 SFX Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All (${BuiltInSoundEffectsCatalog.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NovaEmerald.copy(alpha = 0.22f),
                    selectedLabelColor = NovaEmerald
                )
            )
            SfxCategory.entries.forEach { cat ->
                val isSel = selectedCategory == cat
                val catColor = Color(cat.accentHex)
                FilterChip(
                    selected = isSel,
                    onClick = { selectedCategory = if (isSel) null else cat },
                    label = { Text(cat.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = catColor.copy(alpha = 0.22f),
                        selectedLabelColor = catColor
                    ),
                    modifier = Modifier.testTag("sfx_cat_${cat.name}")
                )
            }
        }

        // Horizontal Playable Sound Effects Cards
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredSfx, key = { it.id }) { sfx ->
                val accent = Color(sfx.category.accentHex)
                val isPreviewing = previewingSfxId == sfx.id
                Surface(
                    color = if (isPreviewing) accent.copy(alpha = 0.22f) else NovaSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .width(172.dp)
                        .border(
                            width = if (isPreviewing) 2.dp else 1.dp,
                            color = if (isPreviewing) accent else NovaBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(10.dp)
                        .testTag("sfx_card_${sfx.id}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sfx.category.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${String.format("%.1fs", sfx.durationMs / 1000f)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sfx.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = NovaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Audition / Preview Button
                            Surface(
                                color = NovaSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.previewSoundEffectAsset(sfx) }
                                    .padding(vertical = 6.dp)
                                    .testTag("preview_sfx_${sfx.id}")
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Preview",
                                        tint = accent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Play", style = MaterialTheme.typography.labelSmall, color = NovaTextPrimary)
                                }
                            }

                            // Add to SFX Timeline Button
                            Surface(
                                color = accent,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.addSoundEffectAssetToTimeline(sfx) }
                                    .padding(vertical = 6.dp)
                                    .testTag("add_sfx_${sfx.id}")
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add SFX",
                                        tint = Color(0xFF07090E),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Add",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF07090E),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Professional Audio & SFX Inspector for Selected Audio Track
        if (selectedTrack != null) {
            ProAudioInspectorCard(
                track = selectedTrack,
                allTracks = audioTracks,
                viewModel = viewModel
            )
        }
    }
}

/**
 * 4. Professional Audio Controls Card (Trimming, Position, Volume, Fade In/Out, Speed, Pitch,
 * Loop, Beat Markers, Noise Reduction, and Audio Ducking).
 */
@Composable
fun ProAudioInspectorCard(
    track: AudioTrackItem,
    allTracks: List<AudioTrackItem>,
    viewModel: NovaCutViewModel
) {
    Surface(
        color = NovaSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NovaEmerald.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Equalizer, contentDescription = null, tint = NovaEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRO AUDIO: ${track.title}",
                        style = MaterialTheme.typography.labelLarge,
                        color = NovaEmerald,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = NovaSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { viewModel.previewSelectedAudioTrack(track) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Audition", style = MaterialTheme.typography.labelSmall, color = NovaCyan)
                    }
                    Surface(
                        color = NovaSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { viewModel.addBeatMarkerToAudioTrack(track.id) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("add_beat_marker_button")
                    ) {
                        Text("+ Beat (${track.beatMarkersMs.size})", style = MaterialTheme.typography.labelSmall, color = NovaAmber)
                    }
                    IconButton(
                        onClick = { viewModel.deleteAudioTrack(track.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = NovaCoralRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Track Switcher Pills if multiple audio/SFX tracks exist
            if (allTracks.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allTracks.forEach { item ->
                        val isSel = item.id == track.id
                        Surface(
                            color = if (isSel) NovaEmerald.copy(alpha = 0.24f) else NovaSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { viewModel.selectAudioTrack(item.id) }
                                .border(1.dp, if (isSel) NovaEmerald else NovaBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = item.title.take(18),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSel) NovaEmerald else NovaTextPrimary
                            )
                        }
                    }
                }
            }

            // Row 1: Timeline Position & Volume
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    StudioSliderRow(
                        label = "Pos",
                        valueText = "${String.format("%.1fs", track.timelineStartMs / 1000f)}",
                        value = track.timelineStartMs.toFloat(),
                        valueRange = 0f..viewModel.totalTimelineDurationMs.coerceAtLeast(2000L).toFloat(),
                        accent = NovaCyan,
                        onValueChange = { viewModel.updateAudioTrack(track.id, timelineStartMs = it.toLong()) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StudioSliderRow(
                        label = "Vol",
                        valueText = "${(track.volume * 100).toInt()}%",
                        value = track.volume,
                        valueRange = 0f..1.5f,
                        accent = NovaEmerald,
                        onValueChange = { viewModel.updateAudioTrack(track.id, volume = it) }
                    )
                }
            }

            // Row 2: Audio Trimming (Trim In / Trim Out)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    StudioSliderRow(
                        label = "Trim In",
                        valueText = "${String.format("%.1fs", track.trimStartMs / 1000f)}",
                        value = track.trimStartMs.toFloat(),
                        valueRange = 0f..(track.trimEndMs - 250L).coerceAtLeast(100L).toFloat(),
                        accent = NovaAmber,
                        onValueChange = { viewModel.updateAudioTrack(track.id, trimStartMs = it.toLong()) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StudioSliderRow(
                        label = "Trim Out",
                        valueText = "${String.format("%.1fs", track.trimEndMs / 1000f)}",
                        value = track.trimEndMs.toFloat(),
                        valueRange = (track.trimStartMs + 250L).toFloat()..track.durationMs.coerceAtLeast(500L).toFloat(),
                        accent = NovaAmber,
                        onValueChange = { viewModel.updateAudioTrack(track.id, trimEndMs = it.toLong()) }
                    )
                }
            }

            // Row 3: Audio Speed (0.5x - 2.0x) & Pitch Adjustment (-12 to +12 Semitones)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    StudioSliderRow(
                        label = "Speed",
                        valueText = "${String.format("%.1fx", track.speed)}",
                        value = track.speed,
                        valueRange = 0.5f..2.0f,
                        accent = NovaCyan,
                        onValueChange = { viewModel.updateAudioTrack(track.id, speed = it) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StudioSliderRow(
                        label = "Pitch",
                        valueText = String.format("%+.0f st", track.pitchSemitones),
                        value = track.pitchSemitones,
                        valueRange = -12f..12f,
                        accent = NovaMagenta,
                        onValueChange = { viewModel.updateAudioTrack(track.id, pitchSemitones = it) }
                    )
                }
            }

            // Row 4: Fade In / Fade Out
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    StudioSliderRow(
                        label = "Fade In",
                        valueText = "${track.fadeInMs}ms",
                        value = track.fadeInMs.toFloat(),
                        valueRange = 0f..2000f,
                        accent = NovaViolet,
                        onValueChange = { viewModel.updateAudioTrack(track.id, fadeInMs = it.toLong()) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StudioSliderRow(
                        label = "Fade Out",
                        valueText = "${track.fadeOutMs}ms",
                        value = track.fadeOutMs.toFloat(),
                        valueRange = 0f..2000f,
                        accent = NovaViolet,
                        onValueChange = { viewModel.updateAudioTrack(track.id, fadeOutMs = it.toLong()) }
                    )
                }
            }

            // Row 5: Pro Toggle Chips (Loop Audio, AI Noise Reduction, Auto Audio Ducking)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = track.isLooped,
                    onClick = { viewModel.updateAudioTrack(track.id, isLooped = !track.isLooped) },
                    label = { Text("Loop 2x", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.testTag("audio_loop_toggle")
                )
                FilterChip(
                    selected = track.noiseReductionEnabled,
                    onClick = { viewModel.updateAudioTrack(track.id, noiseReductionEnabled = !track.noiseReductionEnabled) },
                    label = { Text("Noise Reduction", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.testTag("audio_denoise_toggle")
                )
                FilterChip(
                    selected = track.audioDuckingEnabled,
                    onClick = { viewModel.updateAudioTrack(track.id, audioDuckingEnabled = !track.audioDuckingEnabled) },
                    label = { Text("Audio Ducking", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.testTag("audio_ducking_toggle")
                )
            }
        }
    }
}

/**
 * 5. Full-Screen Effects Store Marketplace (Trending, New, Free, Premium Packs, Search,
 * Sound-Effect Preview, Download Progress, and One-Tap Apply).
 */
@Composable
fun EffectsStoreScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }
    val downloadedIds by viewModel.downloadedPackIds.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.packDownloadProgress.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBadgeFilter by remember { mutableStateOf("ALL") }

    val filteredPacks = remember(searchQuery, selectedBadgeFilter) {
        BuiltInEffectStorePacks.filter { pack ->
            val matchesBadge = when (selectedBadgeFilter) {
                "ALL" -> true
                "FREE" -> pack.isFree
                "PREMIUM" -> !pack.isFree
                else -> pack.badge.equals(selectedBadgeFilter, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                pack.title.contains(searchQuery, ignoreCase = true) ||
                pack.subtitle.contains(searchQuery, ignoreCase = true)
            matchesBadge && matchesSearch
        }
    }

    Scaffold(
        containerColor = NovaObsidian,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("fx_store_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaTextPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "NOVACUT Effects Store",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = "Original Licensed Shader Packs & Acoustic Foley SFX",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search effect & SFX packs...", color = NovaTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaCyan) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaCyan,
                    unfocusedBorderColor = NovaBorder,
                    focusedTextColor = NovaTextPrimary,
                    unfocusedTextColor = NovaTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fx_store_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs (All, Trending, New, Free, Premium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "TRENDING", "NEW", "FREE", "PREMIUM").forEach { tab ->
                    FilterChip(
                        selected = selectedBadgeFilter == tab,
                        onClick = { selectedBadgeFilter = tab },
                        label = { Text(tab) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NovaCyan.copy(alpha = 0.22f),
                            selectedLabelColor = NovaCyan
                        ),
                        modifier = Modifier.testTag("store_filter_$tab")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Marketplace Packs List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPacks, key = { it.id }) { pack ->
                    EffectStorePackCard(
                        pack = pack,
                        isDownloaded = pack.id in downloadedIds,
                        progressPct = downloadProgress[pack.id],
                        onPreviewSfx = {
                            val firstSfx = BuiltInSoundEffectsCatalog.firstOrNull { it.id in pack.includedSfxIds }
                            if (firstSfx != null) {
                                viewModel.previewSoundEffectAsset(firstSfx)
                            }
                        },
                        onDownloadAndApply = {
                            viewModel.downloadAndInstallEffectPack(pack, applyAfterDownload = true)
                            viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EffectStorePackCard(
    pack: EffectStorePack,
    isDownloaded: Boolean,
    progressPct: Int?,
    onPreviewSfx: () -> Unit,
    onDownloadAndApply: () -> Unit
) {
    val accent = Color(pack.accentHex)
    Card(
        colors = CardDefaults.cardColors(containerColor = NovaSurface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .testTag("store_pack_${pack.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = painterResource(id = pack.previewResId),
                    contentDescription = pack.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xEE07090E))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = accent,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = pack.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF07090E),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = Color(0xCC07090E),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${if (pack.isFree) "FREE" else "PRO"} • ${pack.sizeMb}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaTextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = pack.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = NovaTextPrimary
                )
                Text(
                    text = pack.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextSecondary
                )

                // Included Effects Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pack.includedEffects.forEach { fx ->
                        Surface(
                            color = NovaSurfaceElevated,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "✨ ${fx.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (progressPct != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Downloading Pack Assets... $progressPct%",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaCyan
                        )
                        LinearProgressIndicator(
                            progress = { progressPct / 100f },
                            color = accent,
                            trackColor = NovaSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onPreviewSfx,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview SFX", color = NovaTextPrimary)
                    }

                    Button(
                        onClick = onDownloadAndApply,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color(0xFF07090E)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("download_apply_pack_${pack.id}")
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDownloaded) "Apply Pack" else "Get & Apply",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioSliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accent: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NovaTextSecondary,
            modifier = Modifier.width(54.dp)
        )
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = NovaBorder
            ),
            modifier = Modifier
                .weight(1f)
                .height(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            modifier = Modifier.width(46.dp)
        )
    }
}
