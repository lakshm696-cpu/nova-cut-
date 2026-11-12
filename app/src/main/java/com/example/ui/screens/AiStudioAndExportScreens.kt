package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiTaskState
import com.example.media.ExportState
import com.example.media.VideoExportEngine
import com.example.model.AppScreen
import com.example.model.ExportBitratePreset
import com.example.model.ExportFps
import com.example.model.ExportResolution
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCoralRed
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaGold
import com.example.ui.theme.NovaGoldBorder
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaPurpleLight
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaViolet
import com.example.viewmodel.NovaCutViewModel

/**
 * Stage 4 Real Firebase AI Logic Studio Panel:
 * - AI Custom Video Effect Generator (Synthesize custom shaders from text prompt)
 * - AI Automatic Subtitles (Gemini 3.8 Flash structured JSON captions)
 * - AI Scene Detection & Highlight Selection (Multimodal frame analysis)
 * - AI Video Colorist & Enhancement (Multimodal frame grading & pacing advisor)
 */
@Composable
fun AiStudioPanel(viewModel: NovaCutViewModel) {
    val aiTaskState by viewModel.aiTaskState.collectAsStateWithLifecycle()
    val detectedScenes by viewModel.detectedScenes.collectAsStateWithLifecycle()
    val aiEnhancement by viewModel.aiEnhancement.collectAsStateWithLifecycle()
    val customAiEffects by viewModel.customAiEffects.collectAsStateWithLifecycle()

    var customPrompt by remember {
        mutableStateOf("Luxury 24K gold cinema lens flare with royal purple chromatic aberration")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // AI Status Banner (Running / Success / Error)
        when (val state = aiTaskState) {
            is AiTaskState.Running -> {
                Surface(
                    color = NovaPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NovaPurple, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = NovaGold,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = state.taskLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaTextPrimary
                        )
                    }
                }
            }
            is AiTaskState.Success -> {
                Surface(
                    color = NovaEmerald.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NovaEmerald, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NovaEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            is AiTaskState.Error -> {
                Surface(
                    color = NovaCoralRed.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NovaCoralRed, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = NovaCoralRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            AiTaskState.Idle -> Unit
        }

        // AI-Powered Custom Video Effect Generator Card
        Surface(
            color = NovaSurface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(NovaGold, NovaPurple)),
                    RoundedCornerShape(14.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NovaGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI CUSTOM EFFECT GENERATOR",
                            style = MaterialTheme.typography.labelLarge,
                            color = NovaGold
                        )
                    }
                    Surface(
                        color = NovaEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "FREE AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaEmerald,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { customPrompt = it },
                        placeholder = {
                            Text("Describe custom video effect...", color = NovaTextMuted, fontSize = 11.sp)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NovaGold,
                            unfocusedBorderColor = NovaBorder,
                            focusedTextColor = NovaTextPrimary,
                            unfocusedTextColor = NovaTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("ai_studio_custom_effect_input")
                    )

                    Button(
                        onClick = { viewModel.runAiCustomEffectGenerator(customPrompt) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NovaGold,
                            contentColor = Color(0xFF1A1200)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("ai_studio_generate_effect_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Synthesize", fontWeight = FontWeight.Bold)
                    }
                }

                if (customAiEffects.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(customAiEffects, key = { it.id }) { fx ->
                            val c = Color(fx.tintColorHex)
                            Surface(
                                color = NovaSurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clickable { viewModel.applyAiCustomEffectToTimeline(fx) }
                                    .border(1.dp, c, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✦ ${fx.effectName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NovaTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3 Core Gemini 3.8 Flash AI Action Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiFeatureCard(
                title = "Auto Captions",
                subtitle = "Timed AI CC",
                accent = NovaGold,
                icon = Icons.Default.ClosedCaption,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_auto_subtitles_button"),
                onClick = { viewModel.runAiAutoSubtitles() }
            )
            AiFeatureCard(
                title = "Scene Detect",
                subtitle = "AI Highlights",
                accent = NovaPurpleLight,
                icon = Icons.Default.ContentCut,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_scene_detection_button"),
                onClick = { viewModel.runAiSceneDetection() }
            )
            AiFeatureCard(
                title = "AI Enhance",
                subtitle = "Auto Colorist",
                accent = NovaEmerald,
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_video_enhancement_button"),
                onClick = { viewModel.runAiVideoEnhancement() }
            )
        }

        // AI Video Enhancement Recommendation Result
        val suggestion = aiEnhancement
        if (suggestion != null) {
            Surface(
                color = NovaSurface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NovaGold, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GEMINI AI COLORIST REPORT",
                            style = MaterialTheme.typography.labelLarge,
                            color = NovaGold
                        )
                        Button(
                            onClick = { viewModel.applyAiEnhancementSuggestion(suggestion) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NovaGold,
                                contentColor = Color(0xFF1A1200)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("apply_ai_enhancement_button")
                        ) {
                            Text("Apply Grade & Hook", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = suggestion.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextPrimary
                    )
                    Text(
                        text = "LUT: ${suggestion.recommendedFilter.label} • Contrast: ${String.format("%.2fx", suggestion.recommendedContrast)} • Sat: ${String.format("%.2fx", suggestion.recommendedSaturation)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovaPurpleLight
                    )
                    Text(
                        text = "Pacing: ${suggestion.pacingAdvice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextSecondary
                    )
                }
            }
        }

        // AI Detected Scene Cuts & Highlights List
        if (detectedScenes.isNotEmpty()) {
            Text(
                text = "DETECTED SCENE BOUNDARIES & HIGHLIGHTS",
                style = MaterialTheme.typography.labelSmall,
                color = NovaGold
            )
            detectedScenes.forEach { cut ->
                Surface(
                    color = NovaSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (cut.isHighlight) NovaGold else NovaBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatTimecode(cut.timestampMs),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NovaGold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (cut.isHighlight) {
                                    Surface(
                                        color = NovaGold.copy(alpha = 0.22f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "★ HIGHLIGHT",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NovaGold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = cut.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = cut.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaTextSecondary
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.applyDetectedHighlightToTimeline(cut) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, tint = NovaGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Split Here", style = MaterialTheme.typography.labelSmall, color = NovaGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiFeatureCard(
    title: String,
    subtitle: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = NovaSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .clickable { onClick() }
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = title, tint = accent, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = NovaTextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = NovaTextSecondary)
        }
    }
}

/**
 * Luxury NOVACUT PRO Export Settings Screen:
 * - Device & source capability-aware Resolutions: 720p HD, 1080p Full HD, 2K QHD, 4K Ultra HD
 * - Frame Rates: 24 FPS, 30 FPS, 60 FPS where supported
 * - Bitrate Selection: Standard, High, Cinema Master
 * - Estimated File Size & Export History
 */
@Composable
fun ExportSettingsScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }

    val projectTitle by viewModel.projectTitle.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val selectedRes by viewModel.exportResolution.collectAsStateWithLifecycle()
    val selectedFps by viewModel.exportFps.collectAsStateWithLifecycle()
    val selectedBitrate by viewModel.exportBitratePreset.collectAsStateWithLifecycle()
    val clips by viewModel.videoClips.collectAsStateWithLifecycle()
    val videoEffects by viewModel.videoEffects.collectAsStateWithLifecycle()
    val exportHistory by viewModel.exportHistory.collectAsStateWithLifecycle()

    val supportedResolutions = remember { VideoExportEngine.getSupportedResolutions() }
    val supportedFps = remember(selectedRes) { VideoExportEngine.getSupportedFps(selectedRes) }

    val totalDurationSec = (viewModel.totalTimelineDurationMs / 1000f).coerceAtLeast(1f)
    val effectiveMbps = selectedRes.bitrateMbps * selectedBitrate.multiplier
    val estimatedSizeMb = ((effectiveMbps * totalDurationSec) / 8f).coerceAtLeast(0.8f)

    Scaffold(
        containerColor = NovaObsidian,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier.testTag("export_settings_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NovaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "NOVACUT Master Export",
                                style = MaterialTheme.typography.headlineMedium,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = "AndroidX Media3 Transformer • Hardware Encoder",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaGold
                            )
                        }
                    }

                    Surface(
                        color = NovaEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .border(1.dp, NovaEmerald, RoundedCornerShape(50))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NovaEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ALL FREE",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Project Summary & Estimated Size Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NovaSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(NovaGold, NovaPurple)),
                            RoundedCornerShape(18.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = projectTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = NovaTextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Clips: ${clips.size} • FX: ${videoEffects.size}", style = MaterialTheme.typography.bodyMedium, color = NovaTextSecondary)
                            Text("Ratio: ${aspectRatio.label}", style = MaterialTheme.typography.bodyMedium, color = NovaGold)
                            Text("Duration: ${String.format("%.1fs", totalDurationSec)}", style = MaterialTheme.typography.bodyMedium, color = NovaTextSecondary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimated File Size: ~${String.format("%.1f MB", estimatedSizeMb)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = NovaEmerald
                            )
                            Text(
                                text = "${String.format("%.1f", effectiveMbps)} Mbps H.264/AAC",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaPurpleLight
                            )
                        }
                    }
                }
            }

            // 1. Resolution Selection (720p HD, 1080p Full HD, 2K QHD, 4K Ultra HD)
            item {
                Text(
                    text = "1. EXPORT RESOLUTION (DEVICE CAPABILITY VERIFIED)",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaGold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    supportedResolutions.forEach { res ->
                        val active = selectedRes == res
                        Surface(
                            color = if (active) NovaGold.copy(alpha = 0.18f) else NovaSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setExportResolution(res) }
                                .border(
                                    width = if (active) 2.dp else 1.dp,
                                    color = if (active) NovaGold else NovaBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(14.dp)
                                .testTag("export_res_${res.name}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${res.label} (${res.badge})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (active) NovaGold else NovaTextPrimary
                                    )
                                    Text(
                                        text = "${res.heightPx}p Short-Side • ${res.bitrateMbps.toInt()} Mbps base bitrate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NovaTextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.HighQuality,
                                    contentDescription = null,
                                    tint = if (active) NovaGold else NovaTextMuted
                                )
                            }
                        }
                    }
                }
            }

            // 2. Frame Rate Selection (24, 30, 60 FPS)
            item {
                Text(
                    text = "2. FRAME RATE (FPS)",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaGold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    supportedFps.forEach { fps ->
                        val active = selectedFps == fps
                        Surface(
                            color = if (active) NovaPurple.copy(alpha = 0.22f) else NovaSurface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setExportFps(fps) }
                                .border(
                                    width = if (active) 2.dp else 1.dp,
                                    color = if (active) NovaPurple else NovaBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                                .testTag("export_fps_${fps.fps}")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = if (active) NovaPurpleLight else NovaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${fps.fps} FPS",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (active) NovaPurpleLight else NovaTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Bitrate Selection
            item {
                Text(
                    text = "3. VIDEO BITRATE QUALITY",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaGold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExportBitratePreset.entries.forEach { preset ->
                        val active = selectedBitrate == preset
                        Surface(
                            color = if (active) NovaGold.copy(alpha = 0.18f) else NovaSurface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setExportBitratePreset(preset) }
                                .border(
                                    width = if (active) 2.dp else 1.dp,
                                    color = if (active) NovaGold else NovaBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                                .testTag("export_bitrate_${preset.name}")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = if (active) NovaGold else NovaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.label.substringBefore(" "),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) NovaGold else NovaTextPrimary
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NovaTextSecondary,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Render Button
            item {
                Button(
                    onClick = { viewModel.startVideoExport() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaGold,
                        contentColor = Color(0xFF1A1200)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_export_mp4_button")
                ) {
                    Icon(Icons.Default.MovieCreation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Render & Save ${selectedRes.label} MP4 to Device",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Recent Export History
            if (exportHistory.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = NovaGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RECENT MASTER EXPORTS",
                            style = MaterialTheme.typography.labelLarge,
                            color = NovaGold
                        )
                    }
                }
                items(exportHistory.take(4), key = { it.id }) { rec ->
                    Surface(
                        color = NovaSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NovaBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(rec.projectTitle, style = MaterialTheme.typography.titleSmall, color = NovaTextPrimary)
                                Text("${rec.resolutionLabel} • ${rec.fpsLabel}", style = MaterialTheme.typography.bodySmall, color = NovaGold)
                            }
                            Text("${rec.fileSizeKb} KB", style = MaterialTheme.typography.labelMedium, color = NovaEmerald)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stage 1 & Stage 5 Export Progress & Completion Screen.
 */
@Composable
fun ExportProgressScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = NovaObsidian,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = exportState) {
                is ExportState.Exporting -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { state.progressPercent / 100f },
                            color = NovaGold,
                            trackColor = NovaSurfaceElevated,
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(96.dp)
                        )
                        Text(
                            text = "${state.progressPercent}%",
                            style = MaterialTheme.typography.displayMedium,
                            color = NovaGold
                        )
                        Text(
                            text = "Exporting ${state.resolutionLabel} @ ${state.fpsLabel}",
                            style = MaterialTheme.typography.titleLarge,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = state.statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NovaTextSecondary
                        )
                        LinearProgressIndicator(
                            progress = { state.progressPercent / 100f },
                            color = NovaGold,
                            trackColor = NovaSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
                is ExportState.Completed -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NovaSurface),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, NovaGold, RoundedCornerShape(24.dp))
                            .testTag("export_completed_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = NovaEmerald,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Master MP4 Saved to Device!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = "Size: ${state.fileSizeKb} KB • Duration: ${String.format("%.1fs", state.durationMs / 1000f)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = NovaGold
                            )
                            Text(
                                text = "Location: Movies/NovaCut (${state.outputFilePath.substringAfterLast('/')})",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_TEXT, "Created with NOVACUT PRO Studio")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share MP4 Video"))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share")
                                }
                                Button(
                                    onClick = {
                                        viewModel.resetExportState()
                                        viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NovaGold,
                                        contentColor = Color(0xFF1A1200)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("export_done_return_button")
                                ) {
                                    Text("Back to Studio", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                is ExportState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NovaSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NovaCoralRed, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = NovaCoralRed, modifier = Modifier.size(48.dp))
                            Text("Export Encountered an Issue", style = MaterialTheme.typography.titleLarge, color = NovaTextPrimary)
                            Text(state.message, style = MaterialTheme.typography.bodyMedium, color = NovaTextSecondary)
                            Button(
                                onClick = { viewModel.navigateBack() },
                                colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = Color(0xFF1A1200))
                            ) {
                                Text("Return to Editor", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                ExportState.Idle -> {
                    Button(onClick = { viewModel.startVideoExport() }) {
                        Text("Start Export")
                    }
                }
            }
        }
    }
}
