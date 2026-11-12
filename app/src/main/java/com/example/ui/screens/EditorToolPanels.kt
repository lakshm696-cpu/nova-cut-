package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AspectRatioPreset
import com.example.model.FilterPreset
import com.example.model.FontChoice
import com.example.model.TextAnimationType
import com.example.model.TransitionType
import com.example.model.VideoClip
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCharcoal
import com.example.ui.theme.NovaCoralRed
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaViolet
import com.example.viewmodel.NovaCutViewModel

/**
 * Stage 1 & Stage 2 Clip Edit Panel: Split, Trim Start/End, Merge, Duplicate, Delete,
 * Rotate, Crop Zoom, Aspect Ratio, Playback Speed, Reverse, Volume, and Transitions.
 */
@Composable
fun ClipEditToolbar(
    viewModel: NovaCutViewModel,
    selectedClip: VideoClip?,
    aspectRatio: AspectRatioPreset
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Horizontal Action Strip (CapCut-style quick action buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EditorActionPill(
                label = "Split",
                icon = Icons.Default.ContentCut,
                accent = NovaCyan,
                testTag = "action_split_clip",
                onClick = { viewModel.splitSelectedClipAtPlayhead() }
            )
            EditorActionPill(
                label = "Filter Grid",
                icon = Icons.Default.ColorLens,
                accent = NovaAmber,
                testTag = "action_open_filter_grid",
                onClick = { viewModel.openFiltersBottomSheet() }
            )
            EditorActionPill(
                label = "Merge Next",
                icon = Icons.Default.CallMerge,
                accent = NovaViolet,
                testTag = "action_merge_clip",
                onClick = { viewModel.mergeSelectedClipWithNext() }
            )
            EditorActionPill(
                label = "Duplicate",
                icon = Icons.Default.ContentCopy,
                accent = NovaEmerald,
                testTag = "action_duplicate_clip",
                onClick = { viewModel.duplicateSelectedClip() }
            )
            EditorActionPill(
                label = "Rotate 90°",
                icon = Icons.Default.RotateRight,
                accent = NovaAmber,
                testTag = "action_rotate_clip",
                onClick = { viewModel.rotateSelectedClip() }
            )
            EditorActionPill(
                label = if (selectedClip?.isReversed == true) "Reversed" else "Reverse",
                icon = Icons.Default.History,
                accent = if (selectedClip?.isReversed == true) NovaMagenta else NovaTextPrimary,
                testTag = "action_reverse_clip",
                onClick = { viewModel.toggleReverseSelectedClip() }
            )
            EditorActionPill(
                label = if (selectedClip?.trackIndex == 1) "To Main" else "To PIP",
                icon = Icons.Default.Layers,
                accent = NovaViolet,
                testTag = "action_switch_track",
                onClick = { viewModel.toggleSelectedClipTrack() }
            )
            EditorActionPill(
                label = "Move ←",
                icon = Icons.Default.SwapHoriz,
                accent = NovaTextSecondary,
                testTag = "action_move_left",
                onClick = { viewModel.moveSelectedClipOrder(moveRight = false) }
            )
            EditorActionPill(
                label = "Move →",
                icon = Icons.Default.SwapHoriz,
                accent = NovaTextSecondary,
                testTag = "action_move_right",
                onClick = { viewModel.moveSelectedClipOrder(moveRight = true) }
            )
            EditorActionPill(
                label = "Extract Audio",
                icon = Icons.Default.GraphicEq,
                accent = NovaEmerald,
                testTag = "action_extract_audio",
                onClick = { viewModel.extractAudioFromSelectedClip() }
            )
            EditorActionPill(
                label = "Delete",
                icon = Icons.Default.DeleteOutline,
                accent = NovaCoralRed,
                testTag = "action_delete_clip",
                onClick = { viewModel.deleteSelectedClip() }
            )
        }

        if (selectedClip != null) {
            // Trim Start & End Controls + Crop & Volume
            Surface(
                color = NovaSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CLIP: ${selectedClip.title}",
                            style = MaterialTheme.typography.labelLarge,
                            color = NovaCyan
                        )
                        Text(
                            text = "Trim: ${String.format("%.1fs", selectedClip.trimStartMs / 1000f)} - ${String.format("%.1fs", selectedClip.trimEndMs / 1000f)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaTextSecondary
                        )
                    }

                    // Trim Start Slider
                    CompactSliderRow(
                        label = "Trim In",
                        valueText = "${String.format("%.1fs", selectedClip.trimStartMs / 1000f)}",
                        value = selectedClip.trimStartMs.toFloat(),
                        valueRange = 0f..(selectedClip.trimEndMs - 400L).coerceAtLeast(100L).toFloat(),
                        onValueChange = { newIn ->
                            viewModel.updateClipTrim(selectedClip.id, newIn.toLong(), selectedClip.trimEndMs)
                        }
                    )

                    // Trim End Slider
                    CompactSliderRow(
                        label = "Trim Out",
                        valueText = "${String.format("%.1fs", selectedClip.trimEndMs / 1000f)}",
                        value = selectedClip.trimEndMs.toFloat(),
                        valueRange = (selectedClip.trimStartMs + 400L).toFloat()..selectedClip.sourceDurationMs.coerceAtLeast(1000L).toFloat(),
                        onValueChange = { newOut ->
                            viewModel.updateClipTrim(selectedClip.id, selectedClip.trimStartMs, newOut.toLong())
                        }
                    )

                    // Playback Speed Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = NovaAmber, modifier = Modifier.size(16.dp))
                        Text("Speed:", style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
                        listOf(0.5f, 1.0f, 1.5f, 2.0f, 4.0f).forEach { spd ->
                            val active = kotlin.math.abs(selectedClip.playbackSpeed - spd) < 0.05f
                            Surface(
                                color = if (active) NovaAmber.copy(alpha = 0.25f) else NovaSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clickable { viewModel.updateSelectedClipSpeed(spd) }
                                    .border(
                                        width = 1.dp,
                                        color = if (active) NovaAmber else NovaBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("speed_chip_${spd}")
                            ) {
                                Text(
                                    text = "${spd}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) NovaAmber else NovaTextPrimary
                                )
                            }
                        }
                    }

                    // Crop Zoom & Clip Volume Sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CompactSliderRow(
                                label = "Zoom",
                                valueText = "${String.format("%.1fx", selectedClip.cropScale)}",
                                value = selectedClip.cropScale,
                                valueRange = 0.6f..2.2f,
                                onValueChange = { viewModel.updateSelectedClipCropScale(it) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CompactSliderRow(
                                label = "Vol",
                                valueText = if (selectedClip.isMuted) "Mute" else "${(selectedClip.volume * 100).toInt()}%",
                                value = if (selectedClip.isMuted) 0f else selectedClip.volume,
                                valueRange = 0f..1.5f,
                                onValueChange = { viewModel.updateSelectedClipVolume(it) }
                            )
                        }
                    }
                }
            }

            // Aspect Ratio & Clip Transitions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AspectRatio, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(16.dp))
                Text("Canvas:", style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
                AspectRatioPreset.entries.forEach { preset ->
                    val isSel = aspectRatio == preset
                    Surface(
                        color = if (isSel) NovaCyan.copy(alpha = 0.22f) else NovaSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { viewModel.setAspectRatio(preset) }
                            .border(1.dp, if (isSel) NovaCyan else NovaBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("editor_ratio_${preset.name}")
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) NovaCyan else NovaTextPrimary
                        )
                    }
                }
            }

            // Transitions Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transition:", style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
                TransitionType.entries.forEach { trans ->
                    val isSel = selectedClip.transitionIn == trans
                    FilterChip(
                        selected = isSel,
                        onClick = { viewModel.updateSelectedClipTransition(trans) },
                        label = { Text(trans.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NovaMagenta.copy(alpha = 0.24f),
                            selectedLabelColor = NovaMagenta
                        )
                    )
                }
            }
        }
    }
}

/**
 * Stage 2 Audio Editing Panel: Music Library, SFX, Device Audio Import,
 * Extract Audio from Video, Voiceover Recorder with Permission Handling, Volume & Fade-In/Out.
 */
@Composable
fun AudioEditorPanel(viewModel: NovaCutViewModel) {
    val context = LocalContext.current
    val studioAudios by viewModel.studioAudios.collectAsStateWithLifecycle()
    val audioTracks by viewModel.audioTracks.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecordingVoice.collectAsStateWithLifecycle()
    val recDurationMs by viewModel.voiceRecordingDurationMs.collectAsStateWithLifecycle()
    val liveAmps by viewModel.liveMicAmplitudes.collectAsStateWithLifecycle()

    val audioFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importExternalAudioUri(uri)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        } else {
            viewModel.showStatus("Microphone permission denied; using studio voiceover synthesizer.")
            viewModel.startVoiceRecording()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Audio Action Buttons: Record Voiceover, Extract Audio, Import Audio File
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (isRecording) {
                        viewModel.stopVoiceRecording()
                    } else {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) {
                            viewModel.startVoiceRecording()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) NovaCoralRed else NovaMagenta,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("record_voiceover_button")
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRecording) "Stop (${String.format("%.1fs", recDurationMs / 1000f)})" else "Voiceover",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = { viewModel.extractAudioFromSelectedClip() },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("audio_panel_extract_button")
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Extract Clip", style = MaterialTheme.typography.labelLarge, color = NovaCyan)
            }

            OutlinedButton(
                onClick = { audioFileLauncher.launch("audio/*") },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("import_device_audio_button")
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = NovaTextPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import MP3", style = MaterialTheme.typography.labelLarge, color = NovaTextPrimary)
            }
        }

        // Live Microphone Waveform Meter while recording
        if (isRecording) {
            Surface(
                color = NovaSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .border(1.dp, NovaCoralRed, RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    val bars = liveAmps.ifEmpty { listOf(0.4f, 0.7f, 0.5f, 0.9f) }
                    val barW = 8f
                    val gap = 6f
                    bars.forEachIndexed { idx, amp ->
                        val x = idx * (barW + gap)
                        if (x < size.width) {
                            val h = (size.height * amp).coerceIn(4f, size.height)
                            drawRoundRect(
                                color = NovaCoralRed,
                                topLeft = Offset(x, (size.height - h) / 2f),
                                size = Size(barW, h),
                                cornerRadius = CornerRadius(3f, 3f)
                            )
                        }
                    }
                }
            }
        }

        // Active Timeline Audio Tracks Mixer (Pro Audio Controls: Trim, Volume, Fade, Speed, Pitch, Beat Markers, Noise Reduction, Ducking)
        if (audioTracks.isNotEmpty()) {
            Text(
                text = "PRO TIMELINE AUDIO MIXER (${audioTracks.size})",
                style = MaterialTheme.typography.labelSmall,
                color = NovaCyan
            )
            audioTracks.forEach { track ->
                ProAudioInspectorCard(
                    viewModel = viewModel,
                    track = track,
                    allTracks = audioTracks
                )
            }
        }

        // Studio Music & Sound Effects Library
        Text(
            text = "ROYALTY-FREE STUDIO MUSIC & SOUND EFFECTS",
            style = MaterialTheme.typography.labelSmall,
            color = NovaTextSecondary
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(studioAudios, key = { it.title }) { audio ->
                Surface(
                    color = NovaSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(175.dp)
                        .clickable { viewModel.addStudioAudioTrack(audio) }
                        .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                        .testTag("add_studio_audio_${audio.title.take(8)}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = audio.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (audio.isSfx) NovaAmber else NovaEmerald
                            )
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = NovaCyan, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = audio.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = NovaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${audio.durationMs / 1000}s WAV",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaTextMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * Stage 3 Text, Subtitles & Stickers Editor Panel.
 */
@Composable
fun TextAndStickerPanel(
    viewModel: NovaCutViewModel,
    showStickersFirst: Boolean = false
) {
    val textOverlays by viewModel.textOverlays.collectAsStateWithLifecycle()
    val stickers by viewModel.stickerOverlayList.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var selectedFont by remember { mutableStateOf(FontChoice.SPACE_GROTESK) }
    var selectedAnim by remember { mutableStateOf(TextAnimationType.BOUNCE_IN) }
    var selectedColorHex by remember { mutableStateOf(0xFF00F0FFL) }
    var isSubtitleMode by remember { mutableStateOf(false) }

    val stickerCatalog = remember {
        listOf(
            Triple("Cyber Bolt", "⚡", 0xFF00F0FFL),
            Triple("Fire Cut", "🔥", 0xFFFF4757L),
            Triple("Cinema Slate", "🎬", 0xFFFFB800L),
            Triple("Neon Spark", "✨", 0xFFFF2A85L),
            Triple("Beat Drop", "🎧", 0xFF8B5CF6L),
            Triple("4K Ultra", "💎", 0xFF10B981L),
            Triple("Velocity", "🚀", 0xFF00F0FFL),
            Triple("Focus Lock", "🎯", 0xFFFFB800L)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!showStickersFirst) {
            // Text & Subtitle Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Enter title or caption...", color = NovaTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = NovaBorder,
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("text_overlay_input")
                )

                Button(
                    onClick = {
                        val textToAdd = inputText.ifBlank { "NOVACUT STUDIO" }
                        viewModel.addTextOverlay(
                            text = textToAdd,
                            fontChoice = selectedFont,
                            animationType = selectedAnim,
                            colorHex = selectedColorHex,
                            isSubtitle = isSubtitleMode
                        )
                        inputText = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaCyan,
                        contentColor = Color(0xFF001F24)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("add_text_overlay_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSubtitleMode) "Add CC" else "Add Text", fontWeight = FontWeight.Bold)
                }
            }

            // Custom Fonts & CC Track Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = isSubtitleMode,
                    onClick = { isSubtitleMode = !isSubtitleMode },
                    label = { Text("CC Track", fontSize = 11.sp) }
                )

                FontChoice.entries.forEach { font ->
                    val active = selectedFont == font
                    Surface(
                        color = if (active) NovaCyan.copy(alpha = 0.22f) else NovaSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { selectedFont = font }
                            .border(1.dp, if (active) NovaCyan else NovaBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = font.label,
                            fontFamily = font.toFontFamily(),
                            fontSize = 12.sp,
                            color = if (active) NovaCyan else NovaTextPrimary
                        )
                    }
                }
            }

            // Text Animation & Color Swatches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(0xFFFFFFFFL, 0xFF00F0FFL, 0xFFFF2A85L, 0xFFFFB800L, 0xFF10B981L).forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .border(
                                width = if (selectedColorHex == hex) 2.5.dp else 1.dp,
                                color = if (selectedColorHex == hex) Color.White else NovaBorder,
                                shape = CircleShape
                            )
                            .clickable { selectedColorHex = hex }
                    )
                }

                TextAnimationType.entries.forEach { anim ->
                    val isSel = selectedAnim == anim
                    Surface(
                        color = if (isSel) NovaMagenta.copy(alpha = 0.22f) else NovaSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { selectedAnim = anim }
                            .border(1.dp, if (isSel) NovaMagenta else NovaBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = anim.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) NovaMagenta else NovaTextSecondary
                        )
                    }
                }
            }
        }

        // Stickers & Overlays Pack
        Text(
            text = "INTERACTIVE STICKERS & MOTION OVERLAYS (DRAG ON PREVIEW)",
            style = MaterialTheme.typography.labelSmall,
            color = NovaTextSecondary
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(stickerCatalog, key = { it.first }) { (label, symbol, hex) ->
                Surface(
                    color = NovaSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clickable { viewModel.addStickerOverlay(label, symbol, hex) }
                        .border(1.dp, Color(hex).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("sticker_item_${label.replace(' ', '_')}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = symbol, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = label, style = MaterialTheme.typography.labelLarge, color = NovaTextPrimary)
                    }
                }
            }
        }

        // Existing Text & Sticker Layers List for quick delete
        if (textOverlays.isNotEmpty() || stickers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                textOverlays.forEach { txt ->
                    Surface(
                        color = NovaSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = txt.text.take(16),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(txt.colorHex)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove Text",
                                tint = NovaCoralRed,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.deleteTextOverlay(txt.id) }
                            )
                        }
                    }
                }
                stickers.forEach { stk ->
                    Surface(
                        color = NovaSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stk.symbol} ${stk.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove Sticker",
                                tint = NovaCoralRed,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.deleteStickerOverlay(stk.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stage 3 Filters, Color Grading (Brightness, Contrast, Saturation, Vignette) & Green-Screen Chroma Key Panel.
 */
@Composable
fun FiltersAndChromaPanel(
    viewModel: NovaCutViewModel,
    selectedClip: VideoClip?,
    focusChromaFirst: Boolean = false
) {
    var applyToAllClips by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Color LUT Filter Presets Row + Open Media3 Filter Grid Bottom Sheet Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CINEMA COLOR LUT FILTERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NovaCyan
                )
                Surface(
                    color = NovaAmber.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(1.dp, NovaAmber, RoundedCornerShape(8.dp))
                        .clickable { viewModel.openFiltersBottomSheet() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("open_filters_bottom_sheet_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Open Filter Grid Sheet",
                            tint = NovaAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Filter Grid (${FilterPreset.entries.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaAmber
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Apply to All", style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = applyToAllClips,
                    onCheckedChange = { applyToAllClips = it },
                    modifier = Modifier.scale(0.75f)
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FilterPreset.entries, key = { it.name }) { preset ->
                val isSel = selectedClip?.filterPreset == preset
                Surface(
                    color = if (isSel) NovaCyan.copy(alpha = 0.22f) else NovaSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(98.dp)
                        .clickable { viewModel.updateSelectedClipFilter(preset, applyToAll = applyToAllClips) }
                        .border(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isSel) NovaCyan else NovaBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                        .testTag("filter_preset_${preset.name}")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Color(
                                        red = (preset.rScale / 1.4f).coerceIn(0f, 1f),
                                        green = (preset.gScale / 1.4f).coerceIn(0f, 1f),
                                        blue = (preset.bScale / 1.4f).coerceIn(0f, 1f)
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) NovaCyan else NovaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = preset.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaTextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        if (selectedClip != null) {
            // Color Grading Sliders (Brightness, Contrast, Saturation, Vignette)
            Surface(
                color = NovaSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            CompactSliderRow(
                                label = "Bright",
                                valueText = String.format("%+.2f", selectedClip.brightness),
                                value = selectedClip.brightness,
                                valueRange = -0.4f..0.4f,
                                onValueChange = { viewModel.updateSelectedClipColorAdjustments(brightness = it) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CompactSliderRow(
                                label = "Contrast",
                                valueText = String.format("%.2fx", selectedClip.contrast),
                                value = selectedClip.contrast,
                                valueRange = 0.5f..1.8f,
                                onValueChange = { viewModel.updateSelectedClipColorAdjustments(contrast = it) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            CompactSliderRow(
                                label = "Saturate",
                                valueText = String.format("%.2fx", selectedClip.saturation),
                                value = selectedClip.saturation,
                                valueRange = 0.0f..2.0f,
                                onValueChange = { viewModel.updateSelectedClipColorAdjustments(saturation = it) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CompactSliderRow(
                                label = "Vignette",
                                valueText = "${(selectedClip.vignette * 100).toInt()}%",
                                value = selectedClip.vignette,
                                valueRange = 0.0f..1.0f,
                                onValueChange = { viewModel.updateSelectedClipColorAdjustments(vignette = it) }
                            )
                        }
                    }
                }
            }

            // Green-Screen Chroma Key Compositor Controls
            Surface(
                color = NovaSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (selectedClip.chromaKeyEnabled) NovaEmerald else NovaBorder,
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GREEN-SCREEN CHROMA KEY & BG MATTE",
                                style = MaterialTheme.typography.labelLarge,
                                color = NovaEmerald
                            )
                            Text(
                                text = "Key out solid studio background on PIP or Main clips",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaTextSecondary
                            )
                        }
                        Switch(
                            checked = selectedClip.chromaKeyEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateSelectedClipChromaKey(enabled = enabled)
                            },
                            modifier = Modifier.testTag("chroma_key_switch")
                        )
                    }

                    if (selectedClip.chromaKeyEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Key Color:", style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
                            listOf(
                                "Green" to 0xFF00FF00L,
                                "Blue" to 0xFF0066FFL,
                                "Magenta" to 0xFFFF00FFL
                            ).forEach { (name, hex) ->
                                val active = selectedClip.chromaKeyColorHex == hex
                                Surface(
                                    color = Color(hex).copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.updateSelectedClipChromaKey(
                                                enabled = true,
                                                keyColorHex = hex
                                            )
                                        }
                                        .border(
                                            width = if (active) 2.dp else 1.dp,
                                            color = if (active) Color(hex) else NovaBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(name, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                        }

                        CompactSliderRow(
                            label = "Tolerance",
                            valueText = "${(selectedClip.chromaKeyTolerance * 100).toInt()}%",
                            value = selectedClip.chromaKeyTolerance,
                            valueRange = 0.05f..0.85f,
                            onValueChange = { tol ->
                                viewModel.updateSelectedClipChromaKey(enabled = true, tolerance = tol)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorActionPill(
    label: String,
    icon: ImageVector,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = NovaSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .clickable { onClick() }
            .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = NovaTextPrimary
            )
        }
    }
}

@Composable
private fun CompactSliderRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
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
            modifier = Modifier.width(56.dp)
        )
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = NovaCyan,
                activeTrackColor = NovaCyan,
                inactiveTrackColor = NovaBorder
            ),
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelSmall,
            color = NovaCyan,
            modifier = Modifier.width(44.dp)
        )
    }
}
