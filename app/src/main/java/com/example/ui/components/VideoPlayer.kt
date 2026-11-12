package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
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
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaGold
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaPurpleLight
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaViolet
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Multi-Track Media3 & ExoPlayer VideoPlayer component that orchestrates real-time playback
 * of edited timeline tracks:
 * - Main Video Track Sequence (with MediaItem.ClippingConfiguration, PlaybackParameters, and live Media3 Effect chains)
 * - Picture-in-Picture (PIP) Overlay Video Track (secondary synchronized ExoPlayer)
 * - Edited Timeline Audio & Sound Effects Track (synchronized audio ExoPlayer with fade-in/out, pitch/speed, and voice ducking)
 * - Real-time Shader Canvas for stacked VideoEffectItems, AI Custom Effects, Transitions, Text/Subtitles, and Stickers.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoClips: List<VideoClip>,
    activeClip: VideoClip?,
    pipClip: VideoClip?,
    activeEffects: List<VideoEffectItem>,
    audioTracks: List<AudioTrackItem>,
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
    val context = LocalContext.current

    val mainClips = remember(videoClips) {
        videoClips.filter { it.trackIndex == 0 }.sortedBy { it.timelineStartMs }
    }

    // Player HUD & View Options
    var isMasterMuted by remember { mutableStateOf(false) }
    var isLoopEnabled by remember { mutableStateOf(true) }
    var useZoomResizeMode by remember { mutableStateOf(false) }
    var playbackState by remember { mutableIntStateOf(Player.STATE_IDLE) }
    var decodedVideoWidth by remember { mutableIntStateOf(0) }
    var decodedVideoHeight by remember { mutableIntStateOf(0) }
    var playerErrorText by remember { mutableStateOf<String?>(null) }

    // Draggable PIP window normalized position
    var pipOffsetXNorm by remember { mutableFloatStateOf(0.74f) }
    var pipOffsetYNorm by remember { mutableFloatStateOf(0.16f) }

    // 1. Primary Video Track ExoPlayer (handles Main Track sequence & Media3 Effects)
    val mainExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = false
        }
    }

    // 2. Secondary PIP Track ExoPlayer (handles Track 1 Picture-in-Picture video clips)
    val pipExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = false
        }
    }

    // 3. Synchronized Timeline Audio & Sound Effects ExoPlayer
    val audioExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = false
        }
    }

    // Attach Player.Listener to observe Media3 state, video resolution, and recover gracefully from errors
    DisposableEffect(mainExoPlayer, pipExoPlayer, audioExoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                decodedVideoWidth = videoSize.width
                decodedVideoHeight = videoSize.height
            }

            override fun onPlayerError(error: PlaybackException) {
                playerErrorText = error.errorCodeName
                runCatching {
                    mainExoPlayer.prepare()
                }
            }
        }
        mainExoPlayer.addListener(listener)

        onDispose {
            mainExoPlayer.removeListener(listener)
            runCatching { mainExoPlayer.release() }
            runCatching { pipExoPlayer.release() }
            runCatching { audioExoPlayer.release() }
        }
    }

    // Keep repeat mode synced with Loop toggle
    LaunchedEffect(isLoopEnabled) {
        mainExoPlayer.repeatMode = if (isLoopEnabled) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    // Build Media3 playlist of edited Main Track clips with MediaItem.ClippingConfiguration
    val playlistSignature = remember(mainClips) {
        mainClips.joinToString("|") { "${it.id}:${it.uriString}:${it.trimStartMs}:${it.trimEndMs}" }
    }

    LaunchedEffect(playlistSignature) {
        playerErrorText = null
        runCatching {
            if (mainClips.isEmpty()) {
                mainExoPlayer.clearMediaItems()
            } else {
                val mediaItems = mainClips.map { clip ->
                    val safeStart = clip.trimStartMs.coerceAtLeast(0L)
                    val safeEnd = clip.trimEndMs.coerceAtLeast(safeStart + 200L)
                    MediaItem.Builder()
                        .setMediaId(clip.id)
                        .setUri(Uri.parse(clip.uriString))
                        .setClippingConfiguration(
                            MediaItem.ClippingConfiguration.Builder()
                                .setStartPositionMs(safeStart)
                                .setEndPositionMs(safeEnd)
                                .build()
                        )
                        .build()
                }
                mainExoPlayer.setMediaItems(mediaItems, /* resetPosition = */ false)
                mainExoPlayer.prepare()
            }
        }
    }

    // Compile Media3 Video Effects descriptor (RgbAdjustment, Contrast, HslAdjustment, ScaleAndRotate)
    // Note: Live preview renders effects via our 60fps Compose Canvas shader pipeline above PlayerView
    // rather than invoking ExoPlayer.setVideoEffects() on PlaybackVideoGraphWrapper, avoiding
    // EGL_BAD_ATTRIBUTE (0x32c0) and LongArrayQueue.remove() NoSuchElementException on Android emulators.
    val media3EffectsSignature = remember(
        activeClip?.id,
        activeClip?.filterPreset,
        activeClip?.filterIntensity,
        activeClip?.brightness,
        activeClip?.contrast,
        activeClip?.saturation,
        activeClip?.rotationDegrees,
        activeClip?.cropScale,
        activeEffects
    ) {
        buildString {
            append(activeClip?.id ?: "none")
            append(":${activeClip?.filterPreset?.name}:${activeClip?.filterIntensity}:${activeClip?.brightness}:${activeClip?.contrast}:${activeClip?.saturation}")
            append(":${activeClip?.rotationDegrees}:${activeClip?.cropScale}:")
            activeEffects.forEach { fx ->
                append("${fx.id}_${fx.effectType.name}_${fx.intensity}_${fx.customTintHex}_")
            }
        }
    }

    val compiledMedia3Effects = remember(media3EffectsSignature) {
        if (activeClip != null) {
            buildRealtimeMedia3Effects(activeClip, activeEffects)
        } else {
            emptyList()
        }
    }

    // Synchronize Main Track active clip index, source position, speed, and volume envelope
    val activeClipIndex = remember(mainClips, activeClip?.id) {
        if (activeClip == null) -1 else mainClips.indexOfFirst { it.id == activeClip.id }.coerceAtLeast(0)
    }

    LaunchedEffect(
        activeClip?.id,
        activeClip?.playbackSpeed,
        activeClip?.volume,
        activeClip?.isMuted,
        isMasterMuted
    ) {
        if (activeClip != null) {
            runCatching {
                val speed = activeClip.playbackSpeed.coerceIn(0.25f, 4.0f)
                if (mainExoPlayer.playbackParameters.speed != speed) {
                    mainExoPlayer.playbackParameters = PlaybackParameters(speed)
                }
                val effectiveVol = if (isMasterMuted || activeClip.isMuted) {
                    0f
                } else {
                    activeClip.volume.coerceIn(0f, 1f)
                }
                mainExoPlayer.volume = effectiveVol
            }
        }
    }

    // Synchronize Main Track playhead & seamless clip transitions
    LaunchedEffect(playheadMs, isPlaying, activeClipIndex, activeClip?.id) {
        if (activeClip != null && activeClipIndex >= 0 && mainExoPlayer.mediaItemCount > activeClipIndex) {
            runCatching {
                val offsetInClipTimeline = (playheadMs - activeClip.timelineStartMs).coerceAtLeast(0L)
                val clipDurationSource = (activeClip.trimEndMs - activeClip.trimStartMs).coerceAtLeast(200L)
                val targetPositionInClip = if (activeClip.isReversed) {
                    (clipDurationSource - (offsetInClipTimeline * activeClip.playbackSpeed).toLong())
                        .coerceIn(0L, clipDurationSource)
                } else {
                    (offsetInClipTimeline * activeClip.playbackSpeed).toLong()
                        .coerceIn(0L, clipDurationSource)
                }

                val currentMediaIdx = mainExoPlayer.currentMediaItemIndex
                val currentPos = mainExoPlayer.currentPosition
                val driftMs = abs(currentPos - targetPositionInClip)

                if (currentMediaIdx != activeClipIndex) {
                    mainExoPlayer.seekTo(activeClipIndex, targetPositionInClip)
                } else if (!isPlaying || activeClip.isReversed || driftMs > 420L) {
                    mainExoPlayer.seekTo(activeClipIndex, targetPositionInClip)
                }
            }
        }
    }

    LaunchedEffect(isPlaying) {
        runCatching {
            if (isPlaying) {
                if (mainExoPlayer.playbackState == Player.STATE_IDLE) {
                    mainExoPlayer.prepare()
                }
                mainExoPlayer.play()
            } else {
                mainExoPlayer.pause()
            }
        }
    }

    // Synchronize Secondary PIP Video Track ExoPlayer
    LaunchedEffect(pipClip?.id, pipClip?.uriString, pipClip?.trimStartMs, pipClip?.trimEndMs) {
        runCatching {
            if (pipClip != null && pipClip.uriString.isNotBlank()) {
                val safeStart = pipClip.trimStartMs.coerceAtLeast(0L)
                val safeEnd = pipClip.trimEndMs.coerceAtLeast(safeStart + 200L)
                val item = MediaItem.Builder()
                    .setMediaId(pipClip.id)
                    .setUri(Uri.parse(pipClip.uriString))
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(safeStart)
                            .setEndPositionMs(safeEnd)
                            .build()
                    )
                    .build()
                pipExoPlayer.setMediaItem(item)
                pipExoPlayer.prepare()
            } else {
                pipExoPlayer.clearMediaItems()
            }
        }
    }

    LaunchedEffect(pipClip?.id, playheadMs, isPlaying, isMasterMuted) {
        runCatching {
            if (pipClip != null) {
                val pipOffset = (playheadMs - pipClip.timelineStartMs).coerceAtLeast(0L)
                val pipClippedDur = (pipClip.trimEndMs - pipClip.trimStartMs).coerceAtLeast(200L)
                val targetPipPos = (pipOffset * pipClip.playbackSpeed).toLong().coerceIn(0L, pipClippedDur)
                pipExoPlayer.playbackParameters = PlaybackParameters(pipClip.playbackSpeed.coerceIn(0.25f, 4.0f))
                pipExoPlayer.volume = if (isMasterMuted || pipClip.isMuted) 0f else (pipClip.volume * 0.8f).coerceIn(0f, 1f)

                if (!isPlaying || abs(pipExoPlayer.currentPosition - targetPipPos) > 420L) {
                    pipExoPlayer.seekTo(targetPipPos)
                }
                if (isPlaying && !pipExoPlayer.isPlaying) {
                    pipExoPlayer.play()
                } else if (!isPlaying && pipExoPlayer.isPlaying) {
                    pipExoPlayer.pause()
                }
            } else if (pipExoPlayer.isPlaying) {
                pipExoPlayer.pause()
            }
        }
    }

    // Synchronize Edited Audio & SFX Track ExoPlayer at Playhead
    val activeAudioTracksAtPlayhead = remember(audioTracks, playheadMs) {
        audioTracks.filter { it.volume > 0.01f && playheadMs in it.timelineStartMs..it.timelineEndMs }
    }
    val primaryActiveAudio = remember(activeAudioTracksAtPlayhead) {
        // Prioritize voiceover or SFX, then music track
        activeAudioTracksAtPlayhead.firstOrNull { it.isVoiceover }
            ?: activeAudioTracksAtPlayhead.firstOrNull { it.isSfx }
            ?: activeAudioTracksAtPlayhead.firstOrNull()
    }
    val isVoiceoverActiveAtPlayhead = remember(activeAudioTracksAtPlayhead) {
        activeAudioTracksAtPlayhead.any { it.isVoiceover }
    }

    LaunchedEffect(
        primaryActiveAudio?.id,
        primaryActiveAudio?.uriString,
        primaryActiveAudio?.trimStartMs,
        primaryActiveAudio?.trimEndMs
    ) {
        runCatching {
            if (primaryActiveAudio != null && primaryActiveAudio.uriString.isNotBlank()) {
                val safeStart = primaryActiveAudio.trimStartMs.coerceAtLeast(0L)
                val safeEnd = primaryActiveAudio.trimEndMs.coerceAtLeast(safeStart + 150L)
                val item = MediaItem.Builder()
                    .setMediaId(primaryActiveAudio.id)
                    .setUri(Uri.parse(primaryActiveAudio.uriString))
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(safeStart)
                            .setEndPositionMs(safeEnd)
                            .build()
                    )
                    .build()
                audioExoPlayer.setMediaItem(item)
                audioExoPlayer.prepare()
            } else {
                audioExoPlayer.clearMediaItems()
            }
        }
    }

    LaunchedEffect(
        primaryActiveAudio?.id,
        playheadMs,
        isPlaying,
        isMasterMuted,
        isVoiceoverActiveAtPlayhead
    ) {
        runCatching {
            val track = primaryActiveAudio
            if (track != null) {
                val elapsedInTrack = (playheadMs - track.timelineStartMs).coerceAtLeast(0L)
                val remainingInTrack = (track.timelineEndMs - playheadMs).coerceAtLeast(0L)
                val clippedDur = (track.trimEndMs - track.trimStartMs).coerceAtLeast(150L)
                val sourcePos = ((elapsedInTrack * track.speed).toLong()).let { pos ->
                    if (track.isLooped && clippedDur > 0L) pos % clippedDur else pos.coerceIn(0L, clippedDur)
                }

                // Compute Fade-In and Fade-Out envelope
                val fadeInGain = if (track.fadeInMs > 0L) {
                    (elapsedInTrack.toFloat() / track.fadeInMs.toFloat()).coerceIn(0f, 1f)
                } else 1f
                val fadeOutGain = if (track.fadeOutMs > 0L) {
                    (remainingInTrack.toFloat() / track.fadeOutMs.toFloat()).coerceIn(0f, 1f)
                } else 1f

                // Apply automatic audio ducking when a voiceover track is active alongside music/SFX
                val duckingGain = if (!track.isVoiceover && track.audioDuckingEnabled && isVoiceoverActiveAtPlayhead) {
                    0.30f
                } else {
                    1.0f
                }

                val finalAudioVol = if (isMasterMuted || track.volume <= 0.01f) {
                    0f
                } else {
                    (track.volume * fadeInGain * fadeOutGain * duckingGain).coerceIn(0f, 1f)
                }

                audioExoPlayer.volume = finalAudioVol
                val pitchMultiplier = Math.pow(2.0, (track.pitchSemitones / 12.0)).toFloat().coerceIn(0.5f, 2.0f)
                audioExoPlayer.playbackParameters = PlaybackParameters(
                    track.speed.coerceIn(0.5f, 2.0f),
                    pitchMultiplier
                )

                if (!isPlaying || abs(audioExoPlayer.currentPosition - sourcePos) > 420L) {
                    audioExoPlayer.seekTo(sourcePos)
                }
                if (isPlaying && !audioExoPlayer.isPlaying) {
                    audioExoPlayer.play()
                } else if (!isPlaying && audioExoPlayer.isPlaying) {
                    audioExoPlayer.pause()
                }
            } else if (audioExoPlayer.isPlaying) {
                audioExoPlayer.pause()
            }
        }
    }

    // Compute real-time spatial transforms from active Motion Effects & Transitions
    val clipLocalMs = if (activeClip != null) (playheadMs - activeClip.timelineStartMs).coerceAtLeast(0L) else 0L
    val transDur = activeClip?.transitionDurationMs?.coerceAtLeast(150L) ?: 500L
    val inTransitionWindow = activeClip != null &&
        activeClip.transitionIn != TransitionType.NONE &&
        clipLocalMs <= transDur
    val transProgress = if (inTransitionWindow) {
        (1f - (clipLocalMs.toFloat() / transDur.toFloat())).coerceIn(0f, 1f)
    } else 0f

    var dynamicScale = activeClip?.cropScale ?: 1.0f
    var dynamicRotation = activeClip?.rotationDegrees ?: 0f
    var shakeOffsetX = 0f
    var shakeOffsetY = 0f

    if (inTransitionWindow && activeClip != null) {
        when (activeClip.transitionIn) {
            TransitionType.SMOOTH_ZOOM, TransitionType.BLUR -> dynamicScale *= (1f + 0.32f * transProgress)
            TransitionType.SPIN, TransitionType.ROTATION_3D -> dynamicRotation += 180f * transProgress
            TransitionType.SWIPE, TransitionType.SLIDE, TransitionType.WHIP_PAN -> {
                shakeOffsetX += 90f * transProgress
            }
            else -> Unit
        }
    }

    activeEffects.forEach { fx ->
        val amt = fx.intensity
        val fxElapsedSec = ((playheadMs - fx.startTimeMs).coerceAtLeast(0L)) / 1000f
        when (fx.effectType) {
            VideoEffectType.SHAKE_IMPACT -> {
                shakeOffsetX += (sin(playheadMs * 0.09f) * 14f * amt)
                shakeOffsetY += (cos(playheadMs * 0.11f) * 10f * amt)
            }
            VideoEffectType.CAMERA_SHAKE -> {
                shakeOffsetX += (sin(fxElapsedSec * 5f) * 6f * amt)
                shakeOffsetY += (cos(fxElapsedSec * 4f) * 5f * amt)
            }
            VideoEffectType.SMOOTH_ZOOM_FX -> {
                val p = ((playheadMs - fx.startTimeMs).toFloat() / fx.durationMs.toFloat()).coerceIn(0f, 1f)
                dynamicScale *= (1f + 0.25f * p * amt)
            }
            VideoEffectType.DYNAMIC_ZOOM, VideoEffectType.ZOOM_BLUR -> {
                dynamicScale *= (1f + 0.14f * abs(sin(fxElapsedSec * 6f)) * amt)
            }
            VideoEffectType.SPIN_MOTION -> {
                dynamicRotation += (fxElapsedSec * 90f * amt) % 360f
            }
            else -> Unit
        }
        if (fx.isAiCustom && fx.customZoomPulse > 0.02f) {
            val pulse = fx.customZoomPulse
            dynamicScale *= (1f + 0.16f * pulse * abs(sin(fxElapsedSec * 5f)) * amt)
        }
        if (fx.isAiCustom && fx.customShake > 0.02f) {
            shakeOffsetX += (sin(playheadMs * 0.08f) * 12f * fx.customShake * amt)
            shakeOffsetY += (cos(playheadMs * 0.10f) * 8f * fx.customShake * amt)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF04060A))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("video_preview_stage"),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(aspectRatio.ratioValue, matchHeightConstraintsFirst = true)
                .clip(RoundedCornerShape(14.dp))
                .border(1.5.dp, NovaBorder, RoundedCornerShape(14.dp))
                .background(NovaObsidian)
                .testTag("media3_timeline_video_player")
        ) {
            val stageWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val stageHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

            if (activeClip != null) {
                // 1. Hardware Media3 ExoPlayer Surface with Real-Time Motion FX & Transition Transforms
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(shakeOffsetX.roundToInt(), shakeOffsetY.roundToInt()) }
                        .rotate(dynamicRotation)
                        .scale(dynamicScale)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                resizeMode = if (useZoomResizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                } else {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                                player = mainExoPlayer
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        update = { view ->
                            if (view.player != mainExoPlayer) {
                                view.player = mainExoPlayer
                            }
                            val desiredResize = if (useZoomResizeMode) {
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            } else {
                                AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                            if (view.resizeMode != desiredResize) {
                                view.resizeMode = desiredResize
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("exo_main_player_view")
                    )
                }

                // 2. Real-Time Stacked VideoEffects, Color LUTs & Transition Shader Canvas
                val preset = activeClip.filterPreset
                val filterStrength = activeClip.filterIntensity.coerceIn(0f, 1f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Base LUT Tint Overlay (scaled by filterIntensity)
                    if (preset.name != "ORIGINAL" && filterStrength > 0.01f) {
                        if (preset.isGrayscale) {
                            drawRect(
                                color = Color(0xFF1A1D24).copy(alpha = (0.38f * filterStrength).coerceIn(0f, 0.55f))
                            )
                        } else if (preset.isInverted) {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFF0055).copy(alpha = 0.28f * filterStrength),
                                        Color(0xFF00F0FF).copy(alpha = 0.28f * filterStrength)
                                    )
                                )
                            )
                        } else {
                            val tintColor = Color(
                                red = (preset.rScale / 1.45f).coerceIn(0f, 1f),
                                green = (preset.gScale / 1.45f).coerceIn(0f, 1f),
                                blue = (preset.bScale / 1.45f).coerceIn(0f, 1f),
                                alpha = (0.26f * filterStrength).coerceIn(0f, 0.42f)
                            )
                            drawRect(color = tintColor)
                        }
                    }

                    // Brightness adjustment
                    if (activeClip.brightness != 0f) {
                        val bAlpha = abs(activeClip.brightness).coerceIn(0f, 0.45f)
                        val bColor = if (activeClip.brightness > 0f) {
                            Color.White.copy(alpha = bAlpha)
                        } else {
                            Color.Black.copy(alpha = bAlpha)
                        }
                        drawRect(color = bColor)
                    }

                    // Render each Stacked VideoEffectItem in real time
                    activeEffects.forEach { fx ->
                        val amt = fx.intensity.coerceIn(0.1f, 1.0f)
                        when (fx.effectType) {
                            VideoEffectType.CINEMATIC_GLOW, VideoEffectType.AI_VIDEO_ENHANCE -> {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFB800).copy(alpha = 0.28f * amt),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                            VideoEffectType.LENS_FLARE -> {
                                val flareY = size.height * 0.35f
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            NovaCyan.copy(alpha = 0.85f * amt),
                                            Color.White,
                                            NovaCyan.copy(alpha = 0.85f * amt),
                                            Color.Transparent
                                        )
                                    ),
                                    start = Offset(0f, flareY),
                                    end = Offset(size.width, flareY),
                                    strokeWidth = 10f * amt
                                )
                                drawCircle(
                                    color = NovaCyan.copy(alpha = 0.35f * amt),
                                    radius = 48f * amt,
                                    center = Offset(size.width * 0.58f, flareY)
                                )
                            }
                            VideoEffectType.LIGHT_LEAK, VideoEffectType.VINTAGE_FILM -> {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFF6B00).copy(alpha = 0.48f * amt),
                                            Color(0xFFFFB800).copy(alpha = 0.22f * amt),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                            VideoEffectType.FILM_GRAIN -> {
                                val seed = (playheadMs / 40L).toInt()
                                for (g in 0 until 36) {
                                    val gx = ((seed * 31 + g * 53) % 100) / 100f * size.width
                                    val gy = ((seed * 17 + g * 79) % 100) / 100f * size.height
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.32f * amt),
                                        radius = 2.2f,
                                        center = Offset(gx, gy)
                                    )
                                }
                            }
                            VideoEffectType.ANAMORPHIC_LENS -> {
                                val barH = size.height * 0.12f * amt
                                drawRect(color = Color.Black, topLeft = Offset.Zero, size = Size(size.width, barH))
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(0f, size.height - barH),
                                    size = Size(size.width, barH)
                                )
                            }
                            VideoEffectType.GLITCH_EFFECT,
                            VideoEffectType.RGB_SPLIT,
                            VideoEffectType.CHROMATIC_ABERRATION,
                            VideoEffectType.PIXEL_SORTING -> {
                                val shift = 14f * amt
                                drawRect(
                                    color = Color(0xFFFF0055).copy(alpha = 0.24f * amt),
                                    topLeft = Offset(-shift, 0f),
                                    size = size
                                )
                                drawRect(
                                    color = Color(0xFF00F0FF).copy(alpha = 0.24f * amt),
                                    topLeft = Offset(shift, 0f),
                                    size = size
                                )
                                val sliceY = ((playheadMs * 0.35f) % size.height.coerceAtLeast(1f))
                                drawRect(
                                    color = NovaCyan.copy(alpha = 0.45f * amt),
                                    topLeft = Offset(0f, sliceY),
                                    size = Size(size.width, 14f * amt)
                                )
                            }
                            VideoEffectType.VHS_EFFECT, VideoEffectType.RETRO_CRT -> {
                                val step = 10f
                                var y = 0f
                                while (y < size.height) {
                                    drawLine(
                                        color = Color.Black.copy(alpha = 0.32f * amt),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 2f
                                    )
                                    y += step
                                }
                            }
                            VideoEffectType.NEON_GLOW,
                            VideoEffectType.BG_REPLACEMENT,
                            VideoEffectType.AI_BG_REMOVAL -> {
                                drawRoundRect(
                                    brush = Brush.linearGradient(listOf(NovaCyan, NovaMagenta)),
                                    topLeft = Offset(6f, 6f),
                                    size = Size(size.width - 12f, size.height - 12f),
                                    cornerRadius = CornerRadius(18f, 18f),
                                    style = Stroke(width = 6f * amt)
                                )
                            }
                            VideoEffectType.FLASH_STROBE, VideoEffectType.FREEZE_FRAME -> {
                                val flashGate = if (((playheadMs / 140L) % 2L) == 0L) 0.55f * amt else 0.05f
                                drawRect(color = Color.White.copy(alpha = flashGate))
                            }
                            VideoEffectType.FACE_TRACKING, VideoEffectType.OBJECT_TRACKING -> {
                                val boxW = size.width * 0.38f
                                val boxH = size.height * 0.34f
                                val bx = (size.width - boxW) * 0.5f + sin(playheadMs * 0.003f) * 18f
                                val by = (size.height - boxH) * 0.38f
                                val hudColor = if (fx.effectType == VideoEffectType.FACE_TRACKING) NovaEmerald else NovaCyan
                                drawRoundRect(
                                    color = hudColor.copy(alpha = 0.9f * amt),
                                    topLeft = Offset(bx, by),
                                    size = Size(boxW, boxH),
                                    cornerRadius = CornerRadius(12f, 12f),
                                    style = Stroke(width = 3f)
                                )
                            }
                            VideoEffectType.BG_BLUR,
                            VideoEffectType.MOTION_BLUR,
                            VideoEffectType.MOTION_TRAIL -> {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            NovaViolet.copy(alpha = 0.34f * amt)
                                        )
                                    )
                                )
                            }
                            else -> {
                                drawRect(color = Color(fx.effectType.accentHex).copy(alpha = 0.16f * amt))
                            }
                        }

                        // Additional Custom AI-Synthesized Multi-Pass Shader Layer
                        if (fx.isAiCustom) {
                            val c1 = Color(fx.customTintHex ?: 0xFFD6B46A)
                            val c2 = Color(0xFF9B75FF)
                            val glow = fx.customBloom * amt
                            val aberr = (fx.customChromatic + fx.customGlitch * 0.5f) * amt
                            val grain = fx.customGrain * amt
                            val vig = fx.customVignette * amt
                            val scan = fx.customScanlines * amt

                            if (glow > 0.05f) {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            c1.copy(alpha = 0.34f * glow),
                                            c2.copy(alpha = 0.18f * glow),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                            if (aberr > 0.05f) {
                                val shift = 16f * aberr
                                drawRect(
                                    color = c1.copy(alpha = 0.25f * aberr),
                                    topLeft = Offset(-shift, 0f),
                                    size = size
                                )
                                drawRect(
                                    color = c2.copy(alpha = 0.25f * aberr),
                                    topLeft = Offset(shift, 0f),
                                    size = size
                                )
                            }
                            if (scan > 0.05f) {
                                var y = 0f
                                val step = (14f - 6f * scan).coerceAtLeast(6f)
                                while (y < size.height) {
                                    drawLine(
                                        color = c2.copy(alpha = 0.28f * scan),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 1.8f
                                    )
                                    y += step
                                }
                            }
                            if (grain > 0.05f) {
                                val seed = (playheadMs / 45L).toInt()
                                for (g in 0 until 28) {
                                    val gx = ((seed * 29 + g * 47) % 100) / 100f * size.width
                                    val gy = ((seed * 19 + g * 73) % 100) / 100f * size.height
                                    drawCircle(
                                        color = c1.copy(alpha = 0.36f * grain),
                                        radius = 2.0f,
                                        center = Offset(gx, gy)
                                    )
                                }
                            }
                            if (vig > 0.05f) {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = (0.65f * vig).coerceIn(0f, 0.8f))
                                        )
                                    )
                                )
                            }
                        }
                    }

                    // Vignette radial shadow
                    if (activeClip.vignette > 0.02f) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = (activeClip.vignette * 0.75f).coerceIn(0f, 0.85f))
                                )
                            )
                        )
                    }

                    // 12 Advanced Transition Shaders
                    if (inTransitionWindow) {
                        val transColor = when (activeClip.transitionIn) {
                            TransitionType.FLASH -> Color.White.copy(alpha = transProgress * 0.8f)
                            TransitionType.GLITCH -> NovaMagenta.copy(alpha = transProgress * 0.55f)
                            TransitionType.LIGHT_LEAK -> Color(0xFFFF8C00).copy(alpha = transProgress * 0.65f)
                            TransitionType.FILM_BURN -> Color(0xFFEF4444).copy(alpha = transProgress * 0.65f)
                            TransitionType.BLUR, TransitionType.SMOOTH_ZOOM -> NovaCyan.copy(alpha = transProgress * 0.35f)
                            else -> Color.Black.copy(alpha = transProgress * 0.6f)
                        }
                        drawRect(color = transColor)
                    }
                }

                // 3. Secondary Hardware PIP / Overlay Track ExoPlayer Window (Track 1)
                if (pipClip != null) {
                    val pipX = ((pipOffsetXNorm - 0.5f) * stageWidthPx).roundToInt()
                    val pipY = ((pipOffsetYNorm - 0.5f) * stageHeightPx).roundToInt()
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(pipX, pipY) }
                            .size(width = 104.dp, height = 70.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.5.dp,
                                color = if (pipClip.chromaKeyEnabled) NovaEmerald else NovaPurpleLight,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(NovaObsidian)
                            .pointerInput(pipClip.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    pipOffsetXNorm = (pipOffsetXNorm + dragAmount.x / stageWidthPx).coerceIn(0.15f, 0.85f)
                                    pipOffsetYNorm = (pipOffsetYNorm + dragAmount.y / stageHeightPx).coerceIn(0.15f, 0.85f)
                                }
                            }
                            .testTag("pip_overlay_player_box")
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    player = pipExoPlayer
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            update = { view ->
                                if (view.player != pipExoPlayer) {
                                    view.player = pipExoPlayer
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // PIP Header Overlay Badge
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .fillMaxWidth()
                                .background(Color(0xAA08090D))
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPicture,
                                contentDescription = "PIP Overlay",
                                tint = if (pipClip.chromaKeyEnabled) NovaEmerald else NovaGold,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (pipClip.chromaKeyEnabled) "CHROMA PIP" else pipClip.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 4. Upper-Left Media3 Telemetry & Active FX Status Badges
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val clipPositionLabel = if (mainClips.size > 1 && activeClipIndex >= 0) {
                            "CLIP ${activeClipIndex + 1}/${mainClips.size}"
                        } else {
                            "TRACK 1"
                        }
                        PlayerHudBadge(
                            text = if (decodedVideoHeight > 0) "$clipPositionLabel • ${decodedVideoHeight}p" else clipPositionLabel,
                            color = NovaGold
                        )

                        if (activeClip.playbackSpeed != 1.0f || activeClip.isReversed) {
                            val speedLabel = buildString {
                                if (activeClip.isReversed) append("REV ")
                                append("${((activeClip.playbackSpeed * 100).roundToInt() / 100f)}x")
                            }
                            PlayerHudBadge(text = speedLabel, color = NovaCyan)
                        }

                        if (compiledMedia3Effects.isNotEmpty()) {
                            PlayerHudBadge(
                                text = "M3 FX (${compiledMedia3Effects.size})",
                                color = NovaPurpleLight
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeClip.filterPreset.name != "ORIGINAL") {
                            PlayerHudBadge(text = activeClip.filterPreset.label, color = NovaCyan)
                        }
                        activeEffects.take(2).forEach { fx ->
                            PlayerHudBadge(
                                text = "FX: ${fx.displayLabel}",
                                color = Color(fx.displayAccentHex)
                            )
                        }
                        if (activeClip.transitionIn != TransitionType.NONE && inTransitionWindow) {
                            PlayerHudBadge(text = activeClip.transitionIn.label, color = NovaMagenta)
                        }
                        if (activeClip.chromaKeyEnabled) {
                            PlayerHudBadge(text = "CHROMA KEY", color = NovaEmerald)
                        }
                    }
                }

                // 5. Bottom-Left Active Audio Track Sync Indicator
                if (primaryActiveAudio != null) {
                    Surface(
                        color = Color(0xCC08090D),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .border(0.8.dp, NovaEmerald.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Active Audio Track",
                                tint = NovaEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buildString {
                                    append(primaryActiveAudio.title)
                                    if (activeAudioTracksAtPlayhead.size > 1) {
                                        append(" (+${activeAudioTracksAtPlayhead.size - 1})")
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaTextPrimary,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 6. Bottom-Right Media3 Quick Stage Controls (Filters Grid, Mute, Resize Mode, Loop)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onOpenFiltersSheet != null) {
                        StageMiniToggleButton(
                            icon = Icons.Default.FilterVintage,
                            contentDescription = "Open Video Filters Grid",
                            isActive = activeClip.filterPreset.name != "ORIGINAL",
                            activeColor = NovaGold,
                            onClick = onOpenFiltersSheet,
                            testTag = "player_open_filters_sheet_button"
                        )
                    }
                    StageMiniToggleButton(
                        icon = if (isMasterMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMasterMuted) "Unmute Preview" else "Mute Preview",
                        isActive = !isMasterMuted,
                        activeColor = NovaGold,
                        onClick = { isMasterMuted = !isMasterMuted },
                        testTag = "player_mute_toggle_button"
                    )
                    StageMiniToggleButton(
                        icon = if (useZoomResizeMode) Icons.Default.CropFree else Icons.Default.AspectRatio,
                        contentDescription = "Toggle Fit/Fill Mode",
                        isActive = useZoomResizeMode,
                        activeColor = NovaCyan,
                        onClick = { useZoomResizeMode = !useZoomResizeMode },
                        testTag = "player_resize_mode_button"
                    )
                    StageMiniToggleButton(
                        icon = Icons.Default.Repeat,
                        contentDescription = "Toggle Loop Playback",
                        isActive = isLoopEnabled,
                        activeColor = NovaPurpleLight,
                        onClick = { isLoopEnabled = !isLoopEnabled },
                        testTag = "player_loop_toggle_button"
                    )
                }

                // 7. Buffering Indicator
                AnimatedVisibility(
                    visible = playbackState == Player.STATE_BUFFERING,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        color = NovaGold,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        tint = NovaTextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Import a video clip to begin editing",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextSecondary
                    )
                }
            }

            // 8. Draggable Sticker Overlays active at Playhead
            stickerOverlays
                .filter { playheadMs in it.startTimeMs..it.endTimeMs }
                .forEach { sticker ->
                    val offsetX = ((sticker.xNormalized - 0.5f) * stageWidthPx).roundToInt()
                    val offsetY = ((sticker.yNormalized - 0.5f) * stageHeightPx).roundToInt()
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(offsetX, offsetY) }
                            .scale(sticker.scale)
                            .rotate(sticker.rotationDegrees)
                            .pointerInput(sticker.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newX = sticker.xNormalized + (dragAmount.x / stageWidthPx)
                                    val newY = sticker.yNormalized + (dragAmount.y / stageHeightPx)
                                    onDragStickerOverlay(sticker.id, newX, newY)
                                }
                            }
                            .background(
                                color = Color(sticker.accentHex).copy(alpha = 0.22f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color(sticker.accentHex), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${sticker.symbol} ${sticker.label}",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                }

            // 9. Draggable Animated Text & Subtitle Overlays active at Playhead
            textOverlays
                .filter { playheadMs in it.startTimeMs..it.endTimeMs }
                .forEach { item ->
                    val elapsedMs = (playheadMs - item.startTimeMs).coerceAtLeast(0L)
                    val visibleText = if (item.animationType == TextAnimationType.TYPEWRITER) {
                        val charCount = ((elapsedMs / 55L).toInt() + 1).coerceIn(1, item.text.length)
                        item.text.take(charCount)
                    } else {
                        item.text
                    }
                    val animScale = when (item.animationType) {
                        TextAnimationType.BOUNCE_IN -> {
                            val t = (elapsedMs / 350f).coerceIn(0f, 1f)
                            0.85f + 0.15f * sin(t * PI.toFloat() * 0.5f)
                        }
                        TextAnimationType.NEON_FLICKER -> {
                            0.97f + 0.04f * sin(elapsedMs * 0.02f)
                        }
                        else -> 1.0f
                    }

                    val offsetX = ((item.xNormalized - 0.5f) * stageWidthPx).roundToInt()
                    val offsetY = ((item.yNormalized - 0.5f) * stageHeightPx).roundToInt()

                    Surface(
                        color = Color(item.backgroundColorHex),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .offset { IntOffset(offsetX, offsetY) }
                            .scale(animScale)
                            .pointerInput(item.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newX = item.xNormalized + (dragAmount.x / stageWidthPx)
                                    val newY = item.yNormalized + (dragAmount.y / stageHeightPx)
                                    onDragTextOverlay(item.id, newX, newY)
                                }
                            }
                            .border(
                                width = if (item.isSubtitle) 1.dp else 1.5.dp,
                                color = Color(item.colorHex).copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text = visibleText,
                            fontFamily = item.fontChoice.toFontFamily(),
                            fontSize = item.fontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(item.colorHex),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
        }
    }
}

/**
 * Compiles the active clip's color grading, LUT preset, and overlapping stacked VideoEffectItems
 * (including AI-generated custom effects) into a real-time list of AndroidX Media3 [Effect] instances
 * applied directly to [ExoPlayer.setVideoEffects].
 */
@OptIn(UnstableApi::class)
fun buildRealtimeMedia3Effects(
    clip: VideoClip,
    activeEffects: List<VideoEffectItem>
): List<Effect> {
    val effects = mutableListOf<Effect>()

    val preset = clip.filterPreset
    val filterStrength = clip.filterIntensity.coerceIn(0f, 1f)

    if (preset.isGrayscale && filterStrength >= 0.35f) {
        effects.add(RgbFilter.createGrayscaleFilter())
    }
    if (preset.isInverted && filterStrength >= 0.35f) {
        effects.add(RgbFilter.createInvertedFilter())
    }

    var rScale = (1.0f + (preset.rScale - 1.0f) * filterStrength) + clip.brightness
    var gScale = (1.0f + (preset.gScale - 1.0f) * filterStrength) + clip.brightness
    var bScale = (1.0f + (preset.bScale - 1.0f) * filterStrength) + clip.brightness
    var contrastDelta = (clip.contrast - 1.0f) + ((preset.contrastBoost - 1.0f) * filterStrength)
    val effectivePresetSat = 1.0f + (preset.saturationBoost - 1.0f) * filterStrength
    var satDelta = ((clip.saturation * effectivePresetSat) - 1.0f) * 80f
    val hueDelta = (preset.hueShiftDegrees * filterStrength).coerceIn(-180f, 180f)

    activeEffects.forEach { fx ->
        val amt = fx.intensity.coerceIn(0.1f, 1.0f)
        if (fx.isAiCustom) {
            val tint = fx.customTintHex ?: 0xFFD6B46AL
            val tr = ((tint shr 16) and 0xFF) / 255f
            val tg = ((tint shr 8) and 0xFF) / 255f
            val tb = (tint and 0xFF) / 255f
            rScale += (tr - 0.5f) * 0.32f * amt + (fx.customBloom * 0.14f * amt)
            gScale += (tg - 0.5f) * 0.32f * amt
            bScale += (tb - 0.5f) * 0.32f * amt + (fx.customChromatic * 0.16f * amt)
            contrastDelta += (fx.customGlitch * 0.22f + fx.customVignette * 0.14f) * amt
        }

        when (fx.effectType) {
            VideoEffectType.CINEMATIC_GLOW, VideoEffectType.LENS_FLARE -> {
                rScale += 0.12f * amt
                gScale += 0.08f * amt
            }
            VideoEffectType.LIGHT_LEAK, VideoEffectType.VINTAGE_FILM -> {
                rScale += 0.18f * amt
                gScale += 0.10f * amt
                bScale -= 0.10f * amt
                satDelta -= 18f * amt
            }
            VideoEffectType.CINEMATIC_COLOR, VideoEffectType.ANAMORPHIC_LENS -> {
                rScale += 0.10f * amt
                bScale += 0.12f * amt
                contrastDelta += 0.18f * amt
            }
            VideoEffectType.GLITCH_EFFECT, VideoEffectType.RGB_SPLIT, VideoEffectType.CHROMATIC_ABERRATION -> {
                rScale += 0.14f * amt
                bScale += 0.20f * amt
                contrastDelta += 0.15f * amt
            }
            VideoEffectType.VHS_EFFECT, VideoEffectType.RETRO_CRT -> {
                gScale += 0.10f * amt
                bScale += 0.14f * amt
                satDelta -= 22f * amt
            }
            VideoEffectType.NEON_GLOW, VideoEffectType.AI_BG_REMOVAL, VideoEffectType.BG_REPLACEMENT -> {
                bScale += 0.20f * amt
                rScale += 0.12f * amt
                satDelta += 30f * amt
            }
            VideoEffectType.CARTOON_STYLE, VideoEffectType.AI_VIDEO_ENHANCE -> {
                contrastDelta += 0.25f * amt
                satDelta += 35f * amt
            }
            VideoEffectType.SKETCH_EFFECT -> {
                satDelta = -95f
                contrastDelta += 0.40f * amt
            }
            else -> Unit
        }
    }

    val finalR = rScale.coerceIn(0.25f, 2.0f)
    val finalG = gScale.coerceIn(0.25f, 2.0f)
    val finalB = bScale.coerceIn(0.25f, 2.0f)
    if (abs(finalR - 1.0f) > 0.02f || abs(finalG - 1.0f) > 0.02f || abs(finalB - 1.0f) > 0.02f) {
        effects.add(
            RgbAdjustment.Builder()
                .setRedScale(finalR)
                .setGreenScale(finalG)
                .setBlueScale(finalB)
                .build()
        )
    }

    val clampedContrast = contrastDelta.coerceIn(-0.75f, 0.80f)
    if (abs(clampedContrast) > 0.02f) {
        effects.add(Contrast(clampedContrast))
    }

    val clampedSat = satDelta.coerceIn(-95f, 95f)
    if (abs(clampedSat) > 1.5f || abs(hueDelta) > 0.5f) {
        val hslBuilder = HslAdjustment.Builder()
        if (abs(hueDelta) > 0.5f) {
            hslBuilder.adjustHue(hueDelta)
        }
        if (abs(clampedSat) > 1.5f) {
            hslBuilder.adjustSaturation(clampedSat)
        }
        effects.add(hslBuilder.build())
    }

    if (clip.rotationDegrees != 0f || clip.cropScale != 1.0f) {
        effects.add(
            ScaleAndRotateTransformation.Builder()
                .setRotationDegrees(clip.rotationDegrees)
                .setScale(clip.cropScale.coerceIn(0.5f, 2.5f), clip.cropScale.coerceIn(0.5f, 2.5f))
                .build()
        )
    }

    return effects
}

@Composable
private fun PlayerHudBadge(text: String, color: Color) {
    Surface(
        color = Color(0xCC07090E),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(1.dp, color.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun StageMiniToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = Color(0xCC08090D),
        shape = CircleShape,
        modifier = Modifier
            .size(28.dp)
            .border(
                width = 1.dp,
                color = if (isActive) activeColor.copy(alpha = 0.75f) else NovaBorder,
                shape = CircleShape
            )
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isActive) activeColor else NovaTextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
