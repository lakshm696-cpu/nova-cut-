package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.ProjectEntity
import com.example.model.AppScreen
import com.example.model.AspectRatioPreset
import com.example.model.BuiltInSmartTemplates
import com.example.model.BuiltInSoundEffectsCatalog
import com.example.model.EditorBottomTab
import com.example.model.ExportFps
import com.example.model.ExportResolution
import com.example.model.SmartTemplate
import com.example.model.VideoEffectCategory
import com.example.model.VideoEffectType
import com.example.ui.theme.NovaAmber
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaCharcoal
import com.example.ui.theme.NovaCoralRed
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaGold
import com.example.ui.theme.NovaGoldBorder
import com.example.ui.theme.NovaGoldLight
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SplashScreen(onSkipToHome: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1C172B), NovaObsidian)
                )
            )
            .clickable { onSkipToHome() }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(30.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(NovaGold, NovaPurple)),
                        shape = RoundedCornerShape(30.dp)
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_novacut_pro_icon_fg_1794494239729),
                    contentDescription = "NOVACUT PRO Emblem",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NOVACUT",
                    style = MaterialTheme.typography.displayLarge,
                    color = NovaTextPrimary,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "LUXURY CINEMA & AI VIDEO STUDIO",
                style = MaterialTheme.typography.labelMedium,
                color = NovaGold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                color = NovaSurface,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .border(1.dp, NovaGoldBorder, RoundedCornerShape(50))
                    .clickable { onSkipToHome() }
                    .testTag("splash_enter_studio_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = NovaGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INITIALIZING MEDIA3 & GEMINI 3.8 STUDIO...",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovaTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun HomeDashboardScreen(viewModel: NovaCutViewModel) {
    val savedProjects by viewModel.savedProjects.collectAsStateWithLifecycle()
    val exportHistory by viewModel.exportHistory.collectAsStateWithLifecycle()
    val customAiEffects by viewModel.customAiEffects.collectAsStateWithLifecycle()
    var bottomNavIndex by remember { mutableStateOf(0) }
    var aiQuickPrompt by remember { mutableStateOf("24K gold cinema lens flare with neon purple halation") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importVideosFromUris(uris)
            viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
        }
    }

    val trendingEffects = remember {
        VideoEffectType.entries.filter {
            it.category == VideoEffectCategory.TRENDING ||
                it.category == VideoEffectCategory.CINEMATIC ||
                it.category == VideoEffectCategory.NEON
        }.take(8)
    }

    val featuredSounds = remember {
        BuiltInSoundEffectsCatalog.take(8)
    }

    Scaffold(
        containerColor = NovaObsidian,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = NovaCharcoal,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = bottomNavIndex == 0,
                    onClick = { bottomNavIndex = 0 },
                    icon = { Icon(Icons.Default.MovieCreation, contentDescription = "Studio") },
                    label = { Text("Studio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NovaGold,
                        selectedTextColor = NovaGold,
                        indicatorColor = NovaSurface
                    ),
                    modifier = Modifier.testTag("nav_home_tab")
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 1,
                    onClick = { viewModel.navigateTo(AppScreen.EFFECTS_STORE) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "FX Store") },
                    label = { Text("FX Store") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NovaPurple,
                        selectedTextColor = NovaPurple,
                        indicatorColor = NovaSurface
                    ),
                    modifier = Modifier.testTag("nav_fx_store_tab")
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 2,
                    onClick = { viewModel.navigateTo(AppScreen.PROJECT_MANAGEMENT) },
                    icon = { Icon(Icons.Default.FolderOpen, contentDescription = "My Projects") },
                    label = { Text("Projects (${savedProjects.size})") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NovaGold,
                        selectedTextColor = NovaGold,
                        indicatorColor = NovaSurface
                    ),
                    modifier = Modifier.testTag("nav_projects_tab")
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 3,
                    onClick = {
                        viewModel.selectBottomTab(EditorBottomTab.AI_TOOLS)
                        viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Studio") },
                    label = { Text("AI Studio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NovaGold,
                        selectedTextColor = NovaGold,
                        indicatorColor = NovaSurface
                    ),
                    modifier = Modifier.testTag("nav_ai_studio_tab")
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. NOVACUT Brand Header + Full Free Edition Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_novacut_pro_icon_fg_1794494239729),
                            contentDescription = "NOVACUT Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, NovaGoldBorder, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NOVACUT",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = NovaTextPrimary,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = NovaEmerald.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.border(1.dp, NovaEmerald, RoundedCornerShape(6.dp))
                                ) {
                                    Text(
                                        text = "FREE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NovaEmerald,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Creative Video Studio • Media3 & Gemini AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaTextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = NovaEmerald.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    NovaEmerald,
                                    RoundedCornerShape(50)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "All Features Free",
                                    tint = NovaEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ALL UNLOCKED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = NovaSurface,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .border(1.dp, NovaBorder, RoundedCornerShape(50))
                                .clickable { viewModel.navigateTo(AppScreen.VIDEO_EDITOR) }
                                .testTag("open_active_editor_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume Editor",
                                    tint = NovaGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Editor",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NovaTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Hero New Project Card (Luxury Black + Gold + Purple)
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(listOf(NovaGold, NovaPurple)),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_novacut_pro_hero_1794494239931),
                            contentDescription = "NOVACUT PRO Studio Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x3308090D),
                                            Color(0xEE08090D)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = NovaGold.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "CINEMA 4K • 60 FPS • AI SHADERS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NovaGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Master Luxury Cinema Edits",
                                style = MaterialTheme.typography.headlineMedium,
                                color = NovaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.navigateTo(AppScreen.NEW_PROJECT) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NovaGold,
                                        contentColor = Color(0xFF1A1200)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_new_project_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("New Project", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("home_import_gallery_button")
                                ) {
                                    Icon(
                                        Icons.Default.PhotoLibrary,
                                        contentDescription = "Import Gallery",
                                        tint = NovaTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Import", color = NovaTextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // 3. AI Video Studio & Custom AI Effect Generator Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NovaSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(NovaPurple, NovaGold)),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NovaPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "AI VIDEO STUDIO & EFFECT GENERATOR",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NovaTextPrimary
                                    )
                                    Text(
                                        text = "Describe any visual style to synthesize a custom timeline video effect",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NovaTextSecondary
                                    )
                                }
                            }
                            Surface(
                                color = NovaEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "FREE AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NovaEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = aiQuickPrompt,
                                onValueChange = { aiQuickPrompt = it },
                                singleLine = true,
                                placeholder = {
                                    Text("e.g., Cyberpunk RGB split with gold film burn", color = NovaTextMuted)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NovaGold,
                                    unfocusedBorderColor = NovaBorder,
                                    focusedTextColor = NovaTextPrimary,
                                    unfocusedTextColor = NovaTextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dashboard_ai_effect_prompt_input")
                            )
                            Button(
                                onClick = {
                                    viewModel.selectBottomTab(EditorBottomTab.VIDEO_FX)
                                    viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                                    viewModel.runAiCustomEffectGenerator(aiQuickPrompt)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NovaPurple,
                                    contentColor = NovaTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("dashboard_generate_ai_effect_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate FX", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickStudioActionCard(
                                title = "AI FX Studio",
                                subtitle = "${customAiEffects.size} Custom",
                                icon = Icons.Default.AutoAwesome,
                                accent = NovaGold,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_video_fx_button"),
                                onClick = {
                                    viewModel.selectBottomTab(EditorBottomTab.VIDEO_FX)
                                    viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                                }
                            )
                            QuickStudioActionCard(
                                title = "Sound Studio",
                                subtitle = "30+ SFX",
                                icon = Icons.Default.GraphicEq,
                                accent = NovaPurple,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_sfx_studio_button"),
                                onClick = {
                                    viewModel.selectBottomTab(EditorBottomTab.SFX_STUDIO)
                                    viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                                }
                            )
                            QuickStudioActionCard(
                                title = "FX Store",
                                subtitle = "Pro Packs",
                                icon = Icons.Default.WorkspacePremium,
                                accent = NovaGoldLight,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_fx_store_button"),
                                onClick = {
                                    viewModel.navigateTo(AppScreen.EFFECTS_STORE)
                                }
                            )
                            QuickStudioActionCard(
                                title = "AI Captions",
                                subtitle = "Auto CC",
                                icon = Icons.Default.ClosedCaption,
                                accent = NovaEmerald,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_ai_captions_button"),
                                onClick = {
                                    viewModel.selectBottomTab(EditorBottomTab.AI_TOOLS)
                                    viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                                    viewModel.runAiAutoSubtitles()
                                }
                            )
                        }
                    }
                }
            }

            // 4. Trending Effects Carousel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRENDING & LUXURY VIDEO EFFECTS",
                        style = MaterialTheme.typography.labelLarge,
                        color = NovaGold
                    )
                    Text(
                        text = "Open Effects Studio →",
                        style = MaterialTheme.typography.labelMedium,
                        color = NovaPurpleLight,
                        modifier = Modifier.clickable {
                            viewModel.selectBottomTab(EditorBottomTab.VIDEO_FX)
                            viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(trendingEffects, key = { it.name }) { fx ->
                        val effectColor = Color(fx.accentHex)
                        Surface(
                            color = NovaSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .width(156.dp)
                                .border(1.dp, effectColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.selectBottomTab(EditorBottomTab.VIDEO_FX)
                                    viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                                    viewModel.addVideoEffectToTimeline(fx)
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = fx.category.label.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = effectColor,
                                        fontSize = 9.sp
                                    )
                                    if (fx.isPremium) {
                                        Surface(
                                            color = NovaGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "PRO",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NovaGold,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = fx.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NovaTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = fx.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NovaTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 5. Sound Effects Library Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SOUND EFFECTS LIBRARY (CINEMA & WHOOSH)",
                        style = MaterialTheme.typography.labelLarge,
                        color = NovaGold
                    )
                    Text(
                        text = "Open Sound Studio →",
                        style = MaterialTheme.typography.labelMedium,
                        color = NovaPurpleLight,
                        modifier = Modifier.clickable {
                            viewModel.selectBottomTab(EditorBottomTab.SFX_STUDIO)
                            viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(featuredSounds, key = { it.id }) { sfx ->
                        Surface(
                            color = NovaSurface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .width(175.dp)
                                .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.previewSoundEffectAsset(sfx)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NovaPurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Preview SFX",
                                        tint = NovaGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sfx.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = NovaTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${sfx.category.label} • ${String.format("%.1fs", sfx.durationMs / 1000f)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NovaTextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Premium Smart Templates Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PREMIUM STUDIO TEMPLATES",
                        style = MaterialTheme.typography.labelLarge,
                        color = NovaGold
                    )
                    Text(
                        text = "${BuiltInSmartTemplates.size} Master Presets",
                        style = MaterialTheme.typography.labelMedium,
                        color = NovaPurpleLight
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(BuiltInSmartTemplates, key = { it.id }) { tpl ->
                        SmartTemplateCard(
                            template = tpl,
                            onUseTemplate = { viewModel.applySmartTemplate(tpl) }
                        )
                    }
                }
            }

            // 7. Recent Projects & My Projects Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT PROJECTS & MY PROJECTS",
                        style = MaterialTheme.typography.labelLarge,
                        color = NovaGold
                    )
                    Text(
                        text = "Manage All (${savedProjects.size}) →",
                        style = MaterialTheme.typography.labelLarge,
                        color = NovaPurpleLight,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(AppScreen.PROJECT_MANAGEMENT) }
                            .padding(4.dp)
                            .testTag("manage_all_projects_button")
                    )
                }
            }

            if (savedProjects.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NovaSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MovieCreation,
                                contentDescription = null,
                                tint = NovaTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No saved projects yet. Tap 'New Project' above to start editing!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NovaTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(savedProjects.take(5), key = { it.id }) { project ->
                    SavedProjectRowCard(
                        project = project,
                        onOpen = { viewModel.openSavedProject(project) },
                        onDuplicate = { viewModel.duplicateSavedProject(project) },
                        onDelete = { viewModel.deleteSavedProject(project.id) }
                    )
                }
            }

            // 8. Video Export History Section
            item {
                val exportedProjects = savedProjects.filter { !it.lastExportedUri.isNullOrBlank() }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = NovaGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VIDEO EXPORT HISTORY",
                                style = MaterialTheme.typography.labelLarge,
                                color = NovaGold
                            )
                        }
                        Text(
                            text = "Export Settings →",
                            style = MaterialTheme.typography.labelMedium,
                            color = NovaPurpleLight,
                            modifier = Modifier.clickable {
                                viewModel.navigateTo(AppScreen.EXPORT_SETTINGS)
                            }
                        )
                    }

                    if (exportHistory.isEmpty() && exportedProjects.isEmpty()) {
                        Surface(
                            color = NovaSurface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NovaBorder, RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HighQuality,
                                    contentDescription = null,
                                    tint = NovaGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Ready for Hardware-Accelerated MP4 Export",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NovaTextPrimary
                                    )
                                    Text(
                                        text = "Export your timeline in 720p, 1080p, 2K, or 4K Ultra HD to log master renders here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NovaTextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        exportHistory.take(4).forEach { record ->
                            Surface(
                                color = NovaSurface,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, NovaGoldBorder, RoundedCornerShape(14.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = NovaEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = record.projectTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = NovaTextPrimary
                                        )
                                        Text(
                                            text = "${record.resolutionLabel} • ${record.fpsLabel} • ${record.fileSizeKb} KB",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NovaGold
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
}

@Composable
private fun QuickStudioActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = NovaCharcoal,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, NovaBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = NovaTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NovaTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SmartTemplateCard(
    template: SmartTemplate,
    onUseTemplate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NovaSurface),
        modifier = Modifier
            .width(250.dp)
            .clickable { onUseTemplate() }
            .border(
                1.dp,
                NovaBorder,
                RoundedCornerShape(18.dp)
            )
            .testTag("template_card_${template.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
            ) {
                Image(
                    painter = painterResource(id = template.previewResId),
                    contentDescription = template.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color(0xCC08090D),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${template.aspectRatio.label} • ${template.bpm} BPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovaGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = NovaEmerald.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .border(1.dp, NovaEmerald.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "FREE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovaEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = template.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = NovaPurpleLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = NovaTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SavedProjectRowCard(
    project: ProjectEntity,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    Card(
        colors = CardDefaults.cardColors(containerColor = NovaSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .border(1.dp, NovaBorder, RoundedCornerShape(16.dp))
            .testTag("project_row_${project.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NovaGold.copy(alpha = 0.25f), NovaPurple.copy(alpha = 0.25f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCut,
                    contentDescription = null,
                    tint = NovaGold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = NovaTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${project.clipCount} clips • ${String.format("%.1fs", project.durationMs / 1000f)} • ${project.aspectRatioName.removePrefix("RATIO_").replace('_', ':')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NovaGold
                )
                Text(
                    text = "Updated ${dateFormat.format(Date(project.updatedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextMuted
                )
            }
            IconButton(
                onClick = onDuplicate,
                modifier = Modifier.testTag("duplicate_project_${project.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate Project",
                    tint = NovaTextSecondary
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_project_${project.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Project",
                    tint = NovaCoralRed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }

    var title by remember { mutableStateOf("Cinema Reel") }
    var selectedRatio by remember { mutableStateOf(AspectRatioPreset.RATIO_9_16) }
    var selectedRes by remember { mutableStateOf(ExportResolution.RES_1080P) }
    var selectedFps by remember { mutableStateOf(ExportFps.FPS_30) }
    var includeStarterClips by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = NovaObsidian,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("new_project_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Studio Project",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NovaTextPrimary
                    )
                }
            }

            item {
                Text(
                    text = "PROJECT TITLE",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaGold,
                        unfocusedBorderColor = NovaBorder,
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_title_input")
                )
            }

            item {
                Text(
                    text = "CANVAS ASPECT RATIO",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioPreset.entries.forEach { preset ->
                        val isSelected = selectedRatio == preset
                        Surface(
                            color = if (isSelected) NovaGold.copy(alpha = 0.2f) else NovaSurface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedRatio = preset }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NovaGold else NovaBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 6.dp)
                                .testTag("ratio_option_${preset.name}")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) NovaGold else NovaTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = preset.description.substringBefore(" /"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NovaTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "TARGET MASTER RESOLUTION",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ExportResolution.entries, key = { it.name }) { res ->
                        FilterChip(
                            selected = selectedRes == res,
                            onClick = { selectedRes = res },
                            label = {
                                Text(res.label)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.HighQuality,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaGold.copy(alpha = 0.22f),
                                selectedLabelColor = NovaGold
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "TIMELINE FRAME RATE",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExportFps.entries.forEach { fps ->
                        FilterChip(
                            selected = selectedFps == fps,
                            onClick = { selectedFps = fps },
                            label = { Text("${fps.fps} FPS") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaPurple.copy(alpha = 0.22f),
                                selectedLabelColor = NovaPurpleLight
                            )
                        )
                    }
                }
            }

            item {
                Surface(
                    color = NovaSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Preload Studio Sample Clips",
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = "Includes H.264 MP4 clips ready for immediate timeline cutting",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaTextSecondary
                            )
                        }
                        Switch(
                            checked = includeStarterClips,
                            onCheckedChange = { includeStarterClips = it }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.startNewProject(
                            title = title,
                            ratio = selectedRatio,
                            resolution = selectedRes,
                            fps = selectedFps,
                            includeStarterClips = includeStarterClips
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaGold,
                        contentColor = Color(0xFF1A1200)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("create_project_confirm_button")
                ) {
                    Icon(Icons.Default.MovieCreation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Launch NOVACUT Editor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MediaGalleryScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }
    val studioVideos by viewModel.studioVideos.collectAsStateWithLifecycle()
    var importAsPip by remember { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importVideosFromUris(uris, asPipOverlay = importAsPip)
            viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
        }
    }

    Scaffold(
        containerColor = NovaObsidian,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("media_gallery_back_button")
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
                            text = "Media Gallery & Stock Hub",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = "Scoped Android Photo Picker + On-Device H.264 Studio Clips",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaTextSecondary
                        )
                    }
                }
            }

            item {
                Surface(
                    color = NovaSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Import Destination Track",
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = if (importAsPip) "PIP / Overlay Video Track (Track 2)" else "Main Timeline Video Track (Track 1)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (importAsPip) NovaPurpleLight else NovaGold
                            )
                        }
                        Switch(
                            checked = importAsPip,
                            onCheckedChange = { importAsPip = it },
                            modifier = Modifier.testTag("toggle_pip_import_switch")
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaGold,
                        contentColor = Color(0xFF1A1200)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pick_device_videos_button")
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select Videos from Device Gallery",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Text(
                    text = "STUDIO H.264 SAMPLE CLIPS (READY FOR TIMELINE)",
                    style = MaterialTheme.typography.labelLarge,
                    color = NovaTextSecondary
                )
            }

            items(studioVideos, key = { it.title }) { sample ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NovaSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NovaBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NovaSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = null,
                                tint = NovaGold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sample.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = "${String.format("%.1fs", sample.durationMs / 1000f)} • 640x360 H.264 MP4",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaGold
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.addStudioSampleClip(sample, asPipOverlay = importAsPip)
                                viewModel.navigateTo(AppScreen.VIDEO_EDITOR)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NovaSurfaceElevated,
                                contentColor = NovaGold
                            ),
                            modifier = Modifier.testTag("add_sample_clip_${sample.themeIndex}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectManagementScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }
    val projects by viewModel.savedProjects.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val filteredProjects = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) projects
        else projects.filter { it.title.contains(searchQuery, ignoreCase = true) }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("project_mgmt_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "My Projects (${projects.size})",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NovaTextPrimary
                    )
                }

                Button(
                    onClick = { viewModel.navigateTo(AppScreen.NEW_PROJECT) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaGold,
                        contentColor = Color(0xFF1A1200)
                    ),
                    modifier = Modifier.testTag("project_mgmt_new_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search saved projects...", color = NovaTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaGold) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaGold,
                    unfocusedBorderColor = NovaBorder,
                    focusedTextColor = NovaTextPrimary,
                    unfocusedTextColor = NovaTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_search_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProjects, key = { it.id }) { proj ->
                    SavedProjectRowCard(
                        project = proj,
                        onOpen = { viewModel.openSavedProject(proj) },
                        onDuplicate = { viewModel.duplicateSavedProject(proj) },
                        onDelete = { viewModel.deleteSavedProject(proj.id) }
                    )
                }
            }
        }
    }
}
