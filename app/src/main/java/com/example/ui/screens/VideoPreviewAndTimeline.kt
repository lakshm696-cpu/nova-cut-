package com.example.ui.screens

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.model.AspectRatioPreset
import com.example.model.AudioTrackItem
import com.example.model.StickerOverlayItem
import com.example.model.TextAnimationType
import com.example.model.TextOverlayItem
import com.example.model.TransitionType
import com.example.model.VideoClip
import com.example.model.VideoEffectItem
import com.example.model.VideoEffectType
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCharcoal
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
import com.example.ui.theme.TrackAudioColor
import com.example.ui.theme.TrackPipColor
import com.example.ui.theme.TrackSubtitleColor
import com.example.ui.theme.TrackTextColor
import com.example.ui.theme.TrackVideoColor
import com.example.ui.theme.TrackVoiceColor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Upper-screen Real-Time Video Preview Stage powered by the Media3 & ExoPlayer [com.example.ui.components.VideoPlayer]
 * component, handling multi-track playback of edited video clips, PIP overlays, timeline audio/SFX tracks,
 * real-time Media3 Effects, transitions, and draggable Text & Stickers.
 */
@Composable
fun VideoPreviewStage(
    videoClips: List<VideoClip> = emptyList(),
    activeClip: VideoClip?,
    pipClip: VideoClip?,
    activeEffects: List<VideoEffectItem>,
    audioTracks: List<AudioTrackItem> = emptyList(),
    aspectRatio: AspectRatioPreset,
    playheadMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    textOverlays: List<TextOverlayItem>,
    stickerOverlays: List<StickerOverlayItem>,
    onDragTextOverlay: (String, Float, Float) -> Unit,
    onDragStickerOverlay: (String, Float, Float) -> Unit,
    onTogglePlayback: (() -> Unit)? = null,
    onOpenFiltersSheet: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    com.example.ui.components.VideoPlayer(
        videoClips = if (videoClips.isNotEmpty()) videoClips else listOfNotNull(activeClip, pipClip),
        activeClip = activeClip,
        pipClip = pipClip,
        activeEffects = activeEffects,
        audioTracks = audioTracks,
        aspectRatio = aspectRatio,
        playheadMs = playheadMs,
        totalDurationMs = totalDurationMs,
        isPlaying = isPlaying,
        textOverlays = textOverlays,
        stickerOverlays = stickerOverlays,
        onDragTextOverlay = onDragTextOverlay,
        onDragStickerOverlay = onDragStickerOverlay,
        onTogglePlayback = onTogglePlayback,
        onOpenFiltersSheet = onOpenFiltersSheet,
        modifier = modifier
    )
}

/**
 * Precision Transport & Timecode Bar (Play/Pause, Frame Step |< >|, Timecode, Undo/Redo, Magnetic Snap).
 */
@Composable
fun TransportControlBar(
    isPlaying: Boolean,
    playheadMs: Long,
    totalDurationMs: Long,
    canUndo: Boolean,
    canRedo: Boolean,
    magneticSnapEnabled: Boolean,
    onTogglePlay: () -> Unit,
    onStepBackward: () -> Unit,
    onStepForward: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleMagneticSnap: () -> Unit
) {
    Surface(
        color = NovaCharcoal,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = NovaBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatTimecode(playheadMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = NovaCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " / ${formatTimecode(totalDurationMs)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = NovaTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onStepBackward,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("step_frame_backward_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Frame",
                        tint = NovaTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    color = NovaCyan,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onTogglePlay() }
                        .testTag("play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color(0xFF001F24),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onStepForward,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("step_frame_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Frame",
                        tint = NovaTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) NovaTextPrimary else NovaTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) NovaTextPrimary else NovaTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onToggleMagneticSnap,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("magnetic_snap_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Magnetic Snapping",
                        tint = if (magneticSnapEnabled) NovaCyan else NovaTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 5-Track Zoomable Timeline:
 * - Time Ruler
 * - MAIN Video Track (with Transition Badges)
 * - FX Track (Stackable, clickable & draggable VideoEffectItems)
 * - PIP / Overlay Video Track
 * - TEXT/CC Track
 * - AUDIO & SFX Track (with Waveforms + Beat Markers + Loop indicators)
 */
@Composable
fun MultiTrackTimeline(
    videoClips: List<VideoClip>,
    videoEffects: List<VideoEffectItem>,
    audioTracks: List<AudioTrackItem>,
    textOverlays: List<TextOverlayItem>,
    selectedClipId: String?,
    selectedEffectId: String?,
    selectedAudioId: String?,
    playheadMs: Long,
    totalDurationMs: Long,
    timelineZoom: Float,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onSelectEffect: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    onZoomChange: (Float) -> Unit,
    onQuickAddMedia: () -> Unit,
    onQuickSplitAtPlayhead: () -> Unit
) {
    val dpPerSecond = 44.dp * timelineZoom
    val scrollState = rememberScrollState()

    val mainClips = remember(videoClips) { videoClips.filter { it.trackIndex == 0 } }
    val pipClips = remember(videoClips) { videoClips.filter { it.trackIndex == 1 } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NovaObsidian)
            .testTag("multi_track_timeline")
    ) {
        // Timeline Scrubber Slider & Zoom Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NovaSurface)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = NovaSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .clickable { onQuickSplitAtPlayhead() }
                    .testTag("timeline_quick_split_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = "Split Clip",
                        tint = NovaCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Split",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovaTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Slider(
                value = playheadMs.toFloat().coerceIn(0f, totalDurationMs.toFloat().coerceAtLeast(1f)),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(1000f),
                colors = SliderDefaults.colors(
                    thumbColor = NovaCyan,
                    activeTrackColor = NovaCyan,
                    inactiveTrackColor = NovaBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .testTag("timeline_playhead_slider")
            )

            IconButton(
                onClick = { onZoomChange((timelineZoom - 0.25f).coerceAtLeast(0.5f)) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = NovaTextSecondary, modifier = Modifier.size(16.dp))
            }
            Text(
                text = "${(timelineZoom * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = NovaTextSecondary
            )
            IconButton(
                onClick = { onZoomChange((timelineZoom + 0.25f).coerceAtMost(3.0f)) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = NovaTextSecondary, modifier = Modifier.size(16.dp))
            }
        }

        // Scrollable Multi-Track Lanes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(178.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // Track Labels Column
                Column(
                    modifier = Modifier
                        .width(56.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TrackHeaderBadge(label = "MAIN", color = TrackVideoColor)
                    TrackHeaderBadge(label = "FX (${videoEffects.size})", color = NovaMagenta)
                    TrackHeaderBadge(label = "PIP", color = TrackPipColor)
                    TrackHeaderBadge(label = "TEXT/CC", color = TrackSubtitleColor)
                    TrackHeaderBadge(label = "SFX/AUD", color = TrackAudioColor)
                }

                Spacer(modifier = Modifier.width(8.dp))

                val totalWidthDp = ((totalDurationMs / 1000f).coerceAtLeast(6f) * dpPerSecond.value).dp + 120.dp

                Box(
                    modifier = Modifier
                        .width(totalWidthDp)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 0. Time Ruler
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val totalSeconds = (totalDurationMs / 1000L).toInt().coerceAtLeast(6)
                            for (sec in 0..totalSeconds) {
                                Box(modifier = Modifier.width(dpPerSecond)) {
                                    Text(
                                        text = String.format("00:%02d", sec),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NovaTextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        // 1. Main Video Track (Track 0) with Transition Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            mainClips.forEachIndexed { idx, clip ->
                                val clipWidthDp = ((clip.effectiveDurationMs / 1000f) * dpPerSecond.value)
                                    .coerceAtLeast(48f).dp
                                val isSelected = clip.id == selectedClipId

                                Box(
                                    modifier = Modifier
                                        .width(clipWidthDp)
                                        .fillMaxHeight()
                                        .padding(end = 2.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = if (isSelected) {
                                                    listOf(TrackVideoColor, NovaCyan.copy(alpha = 0.75f))
                                                } else {
                                                    listOf(Color(0xFF153E5C), Color(0xFF1E293B))
                                                }
                                            )
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.8.dp,
                                            color = if (isSelected) NovaCyan else NovaBorder,
                                            shape = RoundedCornerShape(7.dp)
                                        )
                                        .clickable {
                                            onSelectClip(clip.id)
                                            onSeek(clip.timelineStartMs + 100L)
                                        }
                                        .padding(horizontal = 6.dp)
                                        .testTag("timeline_clip_${clip.id}"),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (idx > 0 && clip.transitionIn != TransitionType.NONE) {
                                            Text(
                                                text = "⇄ ${clip.transitionIn.label.take(6)} ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NovaAmber,
                                                fontSize = 8.sp
                                            )
                                        }
                                        Text(
                                            text = clip.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${String.format("%.1fs", clip.effectiveDurationMs / 1000f)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NovaCyan,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = NovaSurfaceElevated,
                                shape = RoundedCornerShape(7.dp),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { onQuickAddMedia() }
                                    .testTag("timeline_add_clip_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Video Clip",
                                        tint = NovaCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // 2. Dedicated Effects Timeline Track (FX Track)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(25.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(NovaCharcoal)
                                .testTag("effects_timeline_lane")
                        ) {
                            if (videoEffects.isEmpty()) {
                                Text(
                                    text = "Effects Timeline (Tap 'Effects' tab to stack Glitch, Flare, VHS, Neon...)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaTextMuted,
                                    fontSize = 8.sp,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 8.dp)
                                )
                            } else {
                                videoEffects.forEach { fx ->
                                    val startDp = ((fx.startTimeMs / 1000f) * dpPerSecond.value).dp
                                    val widthDp = ((fx.durationMs / 1000f) * dpPerSecond.value).coerceAtLeast(48f).dp
                                    val fxColor = Color(fx.displayAccentHex)
                                    val isSel = fx.id == selectedEffectId
                                    Box(
                                        modifier = Modifier
                                            .offset(x = startDp)
                                            .width(widthDp)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(fxColor.copy(alpha = if (isSel) 0.85f else 0.55f))
                                            .border(
                                                width = if (isSel) 1.5.dp else 0.8.dp,
                                                color = if (isSel) Color.White else fxColor,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                onSelectEffect(fx.id)
                                                onSeek(fx.startTimeMs + 100L)
                                            }
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color(0xFF08090D),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${fx.displayLabel} ${(fx.intensity * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF08090D),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. PIP / Overlay Video Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(NovaCharcoal)
                        ) {
                            if (pipClips.isEmpty()) {
                                Text(
                                    text = "PIP / Green-Screen Overlay Track",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaTextMuted,
                                    fontSize = 8.sp,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 8.dp)
                                )
                            } else {
                                pipClips.forEach { pip ->
                                    val startDp = ((pip.timelineStartMs / 1000f) * dpPerSecond.value).dp
                                    val widthDp = ((pip.effectiveDurationMs / 1000f) * dpPerSecond.value).coerceAtLeast(44f).dp
                                    val isSel = pip.id == selectedClipId
                                    Box(
                                        modifier = Modifier
                                            .offset(x = startDp)
                                            .width(widthDp)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TrackPipColor.copy(alpha = 0.8f))
                                            .border(
                                                width = if (isSel) 1.5.dp else 0.5.dp,
                                                color = if (isSel) NovaCyan else Color.White.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { onSelectClip(pip.id) }
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = pip.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Text & Subtitle Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(NovaCharcoal)
                        ) {
                            if (textOverlays.isEmpty()) {
                                Text(
                                    text = "Text & Subtitles Layer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaTextMuted,
                                    fontSize = 8.sp,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 8.dp)
                                )
                            } else {
                                textOverlays.forEach { txt ->
                                    val startDp = ((txt.startTimeMs / 1000f) * dpPerSecond.value).dp
                                    val durSec = ((txt.endTimeMs - txt.startTimeMs).coerceAtLeast(500L) / 1000f)
                                    val widthDp = (durSec * dpPerSecond.value).coerceAtLeast(42f).dp
                                    val bgColor = if (txt.isSubtitle) TrackSubtitleColor else TrackTextColor
                                    Box(
                                        modifier = Modifier
                                            .offset(x = startDp)
                                            .width(widthDp)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bgColor.copy(alpha = 0.85f))
                                            .clickable { onSeek(txt.startTimeMs + 100L) }
                                            .padding(horizontal = 6.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ClosedCaption,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = txt.text,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Sound Effects & Audio Waveform Track (with Beat Markers)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(NovaCharcoal)
                                .testTag("sfx_audio_timeline_lane")
                        ) {
                            if (audioTracks.isEmpty()) {
                                Text(
                                    text = "Sound Effects, Music & Voiceover Waveform Track",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaTextMuted,
                                    fontSize = 8.sp,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 8.dp)
                                )
                            } else {
                                audioTracks.forEach { audio ->
                                    val startDp = ((audio.timelineStartMs / 1000f) * dpPerSecond.value).dp
                                    val widthDp = ((audio.effectiveDurationMs / 1000f) * dpPerSecond.value).coerceAtLeast(56f).dp
                                    val trackColor = when {
                                        audio.isVoiceover -> TrackVoiceColor
                                        audio.isSfx -> NovaAmber
                                        else -> TrackAudioColor
                                    }
                                    val isSel = audio.id == selectedAudioId
                                    Box(
                                        modifier = Modifier
                                            .offset(x = startDp)
                                            .width(widthDp)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(trackColor.copy(alpha = 0.35f))
                                            .border(
                                                width = if (isSel) 1.5.dp else 0.8.dp,
                                                color = if (isSel) Color.White else trackColor,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                onSelectAudio(audio.id)
                                                onSeek(audio.timelineStartMs + 50L)
                                            }
                                    ) {
                                        // Draw Audio Waveform + Yellow Beat Markers
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val bars = audio.waveformAmplitudes
                                            if (bars.isNotEmpty()) {
                                                val barWidth = (size.width / (bars.size * 1.6f)).coerceAtLeast(2f)
                                                val gap = barWidth * 0.6f
                                                bars.forEachIndexed { idx, amp ->
                                                    val x = idx * (barWidth + gap) + 4f
                                                    if (x < size.width - 4f) {
                                                        val barHeight = (size.height * 0.78f * amp * audio.volume.coerceIn(0.2f, 1.2f))
                                                            .coerceIn(3f, size.height - 2f)
                                                        val y = (size.height - barHeight) / 2f
                                                        drawRoundRect(
                                                            color = trackColor,
                                                            topLeft = Offset(x, y),
                                                            size = Size(barWidth, barHeight),
                                                            cornerRadius = CornerRadius(2f, 2f)
                                                        )
                                                    }
                                                }
                                            }

                                            // Draw Beat Markers
                                            val durMs = audio.effectiveDurationMs.coerceAtLeast(1L).toFloat()
                                            audio.beatMarkersMs.forEach { beatMs ->
                                                val bx = (beatMs.toFloat() / durMs) * size.width
                                                if (bx in 2f..(size.width - 2f)) {
                                                    drawCircle(
                                                        color = Color(0xFFFFD600),
                                                        radius = 3.2f,
                                                        center = Offset(bx, size.height - 4f)
                                                    )
                                                }
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(horizontal = 5.dp, vertical = 1.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (audio.isVoiceover) Icons.Default.Mic else Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(9.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = buildString {
                                                    append(audio.title)
                                                    if (audio.isLooped) append(" [2x]")
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Glowing Vertical Playhead Line
                    val playheadOffsetDp = ((playheadMs / 1000f) * dpPerSecond.value).dp
                    Box(
                        modifier = Modifier
                            .offset(x = playheadOffsetDp)
                            .width(2.5.dp)
                            .fillMaxHeight()
                            .background(NovaCyan)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackHeaderBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .border(0.5.dp, color.copy(alpha = 0.6f), RoundedCornerShape(5.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

fun formatTimecode(ms: Long): String {
    val safeMs = ms.coerceAtLeast(0L)
    val totalSec = safeMs / 1000L
    val minutes = totalSec / 60L
    val seconds = totalSec % 60L
    val frames = ((safeMs % 1000L) * 30L) / 1000L
    return String.format("%02d:%02d.%02d", minutes, seconds, frames)
}
