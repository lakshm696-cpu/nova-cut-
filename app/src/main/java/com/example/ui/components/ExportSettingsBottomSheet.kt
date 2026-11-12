package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.VideoExportEngine
import com.example.model.AspectRatioPreset
import com.example.model.ExportBitratePreset
import com.example.model.ExportFps
import com.example.model.ExportResolution
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCharcoal
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaGold
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaPurpleLight
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceElevated
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import java.util.Locale

/**
 * Material 3 Modal Bottom Sheet for configuring AndroidX Media3 Transformer export settings
 * (resolution, bitrate, frame rate) directly in the editor workspace before finalizing the MP4 render.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSettingsBottomSheet(
    projectTitle: String,
    aspectRatio: AspectRatioPreset,
    clipCount: Int,
    effectCount: Int,
    totalDurationMs: Long,
    selectedResolution: ExportResolution,
    selectedFps: ExportFps,
    selectedBitratePreset: ExportBitratePreset,
    onSelectResolution: (ExportResolution) -> Unit,
    onSelectFps: (ExportFps) -> Unit,
    onSelectBitratePreset: (ExportBitratePreset) -> Unit,
    onFinalizeExport: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val supportedResolutions = remember { VideoExportEngine.getSupportedResolutions() }
    val supportedFps = remember(selectedResolution) {
        VideoExportEngine.getSupportedFps(selectedResolution)
    }

    val totalDurationSec = (totalDurationMs / 1000f).coerceAtLeast(1f)
    val effectiveMbps = selectedResolution.bitrateMbps * selectedBitratePreset.multiplier
    val estimatedSizeMb = ((effectiveMbps * totalDurationSec) / 8f).coerceAtLeast(0.8f)

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
        modifier = Modifier.testTag("export_settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Row
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
                            .size(38.dp)
                            .border(1.dp, NovaGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = NovaGold,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EXPORT SETTINGS",
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = NovaPurple.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.border(
                                    0.5.dp,
                                    NovaPurpleLight.copy(alpha = 0.6f),
                                    RoundedCornerShape(6.dp)
                                )
                            ) {
                                Text(
                                    text = "MEDIA3",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaPurpleLight,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "$projectTitle • ${aspectRatio.label} • ${String.format(Locale.US, "%.1fs", totalDurationSec)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("export_sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Export Settings",
                        tint = NovaTextSecondary
                    )
                }
            }

            // 2. Live Media3 Transformer Pipeline & File Size Summary Card
            Surface(
                color = NovaSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(NovaGold, NovaPurple)),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ESTIMATED MP4 OUTPUT",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaTextSecondary
                            )
                            Text(
                                text = String.format(Locale.US, "~%.1f MB", estimatedSizeMb),
                                style = MaterialTheme.typography.headlineSmall,
                                color = NovaEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("export_sheet_estimated_size")
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${selectedResolution.label} • ${selectedFps.fps} FPS",
                                style = MaterialTheme.typography.labelLarge,
                                color = NovaGold
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f Mbps H.264 / AAC", effectiveMbps),
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaPurpleLight
                            )
                        }
                    }

                    Surface(
                        color = NovaObsidian,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, NovaBorder, RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = "Transformer: Presentation(${selectedResolution.heightPx}p) • FrameDrop(${selectedFps.fps}fps) • VideoEncoder(${String.format(Locale.US, "%.1f", effectiveMbps)}M) • $clipCount clips, $effectCount FX",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaTextSecondary,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // 3. Resolution Selection (720p / 1080p / 2K / 4K)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RESOLUTION",
                        style = MaterialTheme.typography.labelLarge,
                        color = NovaGold
                    )
                    Text(
                        text = "Hardware verified",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovaTextMuted
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    supportedResolutions.chunked(2).forEach { rowResolutions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowResolutions.forEach { res ->
                                val active = selectedResolution == res
                                Surface(
                                    color = if (active) NovaGold.copy(alpha = 0.16f) else NovaSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSelectResolution(res) }
                                        .border(
                                            width = if (active) 1.5.dp else 1.dp,
                                            color = if (active) NovaGold else NovaBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("export_sheet_res_${res.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = res.label,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = if (active) NovaGold else NovaTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${res.badge} • ${res.bitrateMbps.toInt()}M",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NovaTextSecondary,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = if (active) Icons.Default.CheckCircle else Icons.Default.HighQuality,
                                            contentDescription = null,
                                            tint = if (active) NovaGold else NovaTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            if (rowResolutions.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 4. Frame Rate Selection (24 / 30 / 60 FPS)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FRAME RATE",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaGold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    supportedFps.forEach { fps ->
                        val active = selectedFps == fps
                        val subtitle = when (fps) {
                            ExportFps.FPS_24 -> "Cinematic"
                            ExportFps.FPS_30 -> "Broadcast"
                            ExportFps.FPS_60 -> "Ultra Smooth"
                        }
                        Surface(
                            color = if (active) NovaPurple.copy(alpha = 0.22f) else NovaSurface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectFps(fps) }
                                .border(
                                    width = if (active) 1.5.dp else 1.dp,
                                    color = if (active) NovaPurple else NovaBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                                .testTag("export_sheet_fps_${fps.fps}")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = if (active) NovaPurpleLight else NovaTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${fps.fps} FPS",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (active) NovaPurpleLight else NovaTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaTextSecondary,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 5. Bitrate Quality Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "BITRATE QUALITY",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaGold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportBitratePreset.entries.forEach { preset ->
                        val active = selectedBitratePreset == preset
                        val presetMbps = selectedResolution.bitrateMbps * preset.multiplier
                        Surface(
                            color = if (active) NovaGold.copy(alpha = 0.16f) else NovaSurface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectBitratePreset(preset) }
                                .border(
                                    width = if (active) 1.5.dp else 1.dp,
                                    color = if (active) NovaGold else NovaBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                                .testTag("export_sheet_bitrate_${preset.name}")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = if (active) NovaGold else NovaTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.label.substringBefore(" "),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (active) NovaGold else NovaTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f Mbps", presetMbps),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) NovaEmerald else NovaTextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 6. Finalize Render Button
            Button(
                onClick = onFinalizeExport,
                enabled = clipCount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaGold,
                    contentColor = Color(0xFF1A1200),
                    disabledContainerColor = NovaSurfaceElevated,
                    disabledContentColor = NovaTextMuted
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("export_sheet_render_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MovieCreation,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Render ${selectedResolution.label} • ${selectedFps.fps} FPS MP4",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
