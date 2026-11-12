package com.example.model

import androidx.compose.ui.text.font.FontFamily
import com.example.R
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PacificoFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class AppScreen {
    SPLASH,
    HOME_DASHBOARD,
    NEW_PROJECT,
    MEDIA_GALLERY,
    VIDEO_EDITOR,
    AUDIO_LIBRARY,
    TEXT_STICKER_EDITOR,
    FILTERS_EFFECTS,
    EFFECTS_STORE,
    EXPORT_SETTINGS,
    EXPORT_PROGRESS,
    PROJECT_MANAGEMENT
}

/**
 * Complete NOVACUT Studio Toolbar with all 13 creative studio modules (100% Free & Unlocked).
 */
enum class EditorBottomTab(val label: String) {
    TIMELINE("Edit"),
    MEDIA("Media"),
    AUDIO("Audio"),
    TEXT("Text"),
    VIDEO_FX("Effects"),
    TRANSITIONS("Transitions"),
    FILTERS("Filters"),
    STICKERS("Stickers"),
    SPEED("Speed"),
    COLOR("Color"),
    SFX_STUDIO("Sound FX"),
    FX_STORE("FX Store"),
    EXPORT("Export")
}

enum class AspectRatioPreset(
    val label: String,
    val description: String,
    val widthRatio: Float,
    val heightRatio: Float
) {
    RATIO_9_16("9:16", "TikTok / Reels / Shorts", 9f, 16f),
    RATIO_16_9("16:9", "YouTube / Cinema Widescreen", 16f, 9f),
    RATIO_1_1("1:1", "Square Feed Post", 1f, 1f),
    RATIO_4_5("4:5", "Portrait Social Feed", 4f, 5f);

    val ratioValue: Float get() = widthRatio / heightRatio
}

/**
 * Export Resolutions: 720p HD, 1080p Full HD, 2K QHD, and 4K Ultra HD (All Free & Unlocked).
 */
enum class ExportResolution(
    val label: String,
    val badge: String,
    val heightPx: Int,
    val bitrateMbps: Float,
    val requiresPro: Boolean = false
) {
    RES_720P("720p HD", "Fast Social Share", 720, 8f, requiresPro = false),
    RES_1080P("1080p Full HD", "Studio Standard", 1080, 16f, requiresPro = false),
    RES_2K("2K QHD", "Quad HD Master", 1440, 26f, requiresPro = false),
    RES_4K("4K Ultra HD", "Cinema Master", 2160, 44f, requiresPro = false)
}

enum class ExportFps(val fps: Int, val label: String, val requiresPro: Boolean = false) {
    FPS_24(24, "24 FPS (Cinematic)", requiresPro = false),
    FPS_30(30, "30 FPS (Broadcast)", requiresPro = false),
    FPS_60(60, "60 FPS (Ultra Smooth)", requiresPro = false)
}

enum class ExportBitratePreset(
    val label: String,
    val subtitle: String,
    val multiplier: Float,
    val requiresPro: Boolean = false
) {
    STANDARD("Standard VBR", "Balanced size & clarity", 0.8f, requiresPro = false),
    HIGH("High Studio", "Recommended for social & web", 1.0f, requiresPro = false),
    CINEMA_MASTER("Cinema Master CBR", "Maximum archival quality", 1.4f, requiresPro = false)
}

data class ExportHistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val projectTitle: String,
    val resolutionLabel: String,
    val fpsLabel: String,
    val bitrateMbps: Float,
    val fileSizeKb: Long,
    val durationMs: Long,
    val outputFilePath: String,
    val exportedAtMs: Long = System.currentTimeMillis()
)

/**
 * 12 Professional Video Transitions with adjustable duration & real-time preview support.
 */
enum class TransitionType(val label: String, val defaultDurationMs: Long, val badgeColorHex: Long) {
    NONE("None", 0L, 0xFF6E7282),
    SMOOTH_ZOOM("Smooth Zoom", 500L, 0xFFD6B46A),
    SWIPE("Swipe", 450L, 0xFF9B75FF),
    SLIDE("Whip Slide", 400L, 0xFFB99CFF),
    SPIN("Spin 360°", 550L, 0xFF9B75FF),
    FADE("Cross Fade", 600L, 0xFFD6B46A),
    FLASH("Gold Flash", 350L, 0xFFF0D799),
    GLITCH("Cyber Glitch", 450L, 0xFFFF5A67),
    BLUR("Zoom Blur", 500L, 0xFFD6B46A),
    WHIP_PAN("Whip Pan", 350L, 0xFFE5C478),
    LIGHT_LEAK("Light Leak", 650L, 0xFFD6B46A),
    FILM_BURN("Film Burn", 600L, 0xFFFF5A67),
    ROTATION_3D("3D Rotation", 550L, 0xFF9B75FF);

    val durationMs: Long get() = defaultDurationMs
}

/**
 * All 8 Luxury Effects Categories for the NOVACUT PRO Effects Studio:
 * Cinematic, Trending, AI Effects, Glitch, Vintage, Neon, Light, and Motion.
 */
enum class VideoEffectCategory(val label: String, val accentHex: Long) {
    CINEMATIC("Cinematic", 0xFFD6B46A),
    TRENDING("Trending", 0xFF9B75FF),
    AI_CREATIVE("Creative", 0xFFB99CFF),
    GLITCH("Glitch", 0xFFFF5A67),
    VINTAGE("Vintage", 0xFFD99B4E),
    NEON("Neon", 0xFF9B75FF),
    LIGHT("Light", 0xFFF0D799),
    MOTION("Motion", 0xFFD6B46A)
}

/**
 * 32+ Working Video Effects mapped across the 8 Luxury Categories.
 */
enum class VideoEffectType(
    val label: String,
    val category: VideoEffectCategory,
    val subtitle: String,
    val accentHex: Long,
    val isPremium: Boolean = false,
    val isTrending: Boolean = false,
    val isNew: Boolean = false,
    val extraCategories: Set<VideoEffectCategory> = emptySet()
) {
    // 1. Cinematic & Light & Vintage Effects
    CINEMATIC_GLOW(
        "Cinematic Glow",
        VideoEffectCategory.CINEMATIC,
        "Soft champagne halation highlight bloom",
        0xFFD6B46A,
        isTrending = true,
        extraCategories = setOf(VideoEffectCategory.LIGHT, VideoEffectCategory.TRENDING)
    ),
    FILM_GRAIN(
        "Film Grain",
        VideoEffectCategory.CINEMATIC,
        "35mm organic silver halide grain",
        0xFFD99B4E,
        extraCategories = setOf(VideoEffectCategory.VINTAGE)
    ),
    LENS_FLARE(
        "Lens Flare",
        VideoEffectCategory.CINEMATIC,
        "Gold & violet horizontal optical flare",
        0xFFD6B46A,
        isTrending = true,
        extraCategories = setOf(VideoEffectCategory.LIGHT, VideoEffectCategory.TRENDING)
    ),
    MOTION_BLUR(
        "Motion Blur",
        VideoEffectCategory.CINEMATIC,
        "180° shutter directional streak",
        0xFF9B75FF,
        extraCategories = setOf(VideoEffectCategory.MOTION)
    ),
    LIGHT_LEAK(
        "Light Leak",
        VideoEffectCategory.CINEMATIC,
        "Warm gold & amber film edge burn",
        0xFFF0D799,
        extraCategories = setOf(VideoEffectCategory.LIGHT, VideoEffectCategory.VINTAGE)
    ),
    VINTAGE_FILM(
        "Vintage Film",
        VideoEffectCategory.CINEMATIC,
        "Super 8mm sepia & dust gate",
        0xFFD99B4E,
        extraCategories = setOf(VideoEffectCategory.VINTAGE)
    ),
    CINEMATIC_COLOR(
        "Cinema Grade",
        VideoEffectCategory.CINEMATIC,
        "Luxury gold & royal shadow contrast",
        0xFFD6B46A,
        isPremium = false
    ),
    ANAMORPHIC_LENS(
        "Anamorphic Lens",
        VideoEffectCategory.CINEMATIC,
        "2.39:1 letterbox + oval bokeh",
        0xFFD6B46A,
        isPremium = false,
        extraCategories = setOf(VideoEffectCategory.LIGHT)
    ),

    // 2. Trending, Glitch, Neon & Vintage Effects
    GLITCH_EFFECT(
        "Glitch Effect",
        VideoEffectCategory.TRENDING,
        "Digital datamosh scan displacement",
        0xFF9B75FF,
        isTrending = true,
        extraCategories = setOf(VideoEffectCategory.GLITCH)
    ),
    RGB_SPLIT(
        "RGB Split",
        VideoEffectCategory.TRENDING,
        "Gold/Violet horizontal channel shift",
        0xFFFF5A67,
        isTrending = true,
        extraCategories = setOf(VideoEffectCategory.GLITCH)
    ),
    VHS_EFFECT(
        "VHS Tape 1998",
        VideoEffectCategory.TRENDING,
        "Tracking lines, noise & OSD timecode",
        0xFF9B75FF,
        isTrending = true,
        extraCategories = setOf(VideoEffectCategory.VINTAGE, VideoEffectCategory.GLITCH)
    ),
    RETRO_CRT(
        "Retro CRT",
        VideoEffectCategory.TRENDING,
        "Curved phosphor scanline matrix",
        0xFFD99B4E,
        extraCategories = setOf(VideoEffectCategory.VINTAGE)
    ),
    NEON_GLOW(
        "Neon Edge Glow",
        VideoEffectCategory.TRENDING,
        "Royal purple & gold contour bloom",
        0xFF9B75FF,
        isTrending = true,
        extraCategories = setOf(VideoEffectCategory.NEON, VideoEffectCategory.LIGHT)
    ),
    FLASH_STROBE(
        "Flash Strobe",
        VideoEffectCategory.TRENDING,
        "Rhythmic high-energy beat flash",
        0xFFF0D799,
        extraCategories = setOf(VideoEffectCategory.LIGHT)
    ),
    SHAKE_IMPACT(
        "Bass Shake",
        VideoEffectCategory.TRENDING,
        "High-frequency impact tremor",
        0xFFD6B46A,
        isTrending = true,
        extraCategories = setOf(VideoEffectCategory.MOTION)
    ),
    ZOOM_BLUR(
        "Radial Zoom Blur",
        VideoEffectCategory.TRENDING,
        "Center-burst velocity streaks",
        0xFF9B75FF,
        extraCategories = setOf(VideoEffectCategory.MOTION)
    ),
    CHROMATIC_ABERRATION(
        "Chromatic Prism",
        VideoEffectCategory.TRENDING,
        "Optical fringe lens dispersion",
        0xFFB99CFF,
        isNew = true,
        extraCategories = setOf(VideoEffectCategory.GLITCH, VideoEffectCategory.LIGHT)
    ),
    PIXEL_SORTING(
        "Pixel Sorting",
        VideoEffectCategory.TRENDING,
        "Vertical luminance glitch sort",
        0xFFFF5A67,
        isPremium = false,
        isNew = true,
        extraCategories = setOf(VideoEffectCategory.GLITCH)
    ),

    // 3. Creative & Stylize Effects
    BG_BLUR(
        "Bokeh Portrait",
        VideoEffectCategory.AI_CREATIVE,
        "Depth-style background defocus",
        0xFF9B75FF,
        isTrending = true
    ),
    BG_REPLACEMENT(
        "Cyber Backdrop",
        VideoEffectCategory.AI_CREATIVE,
        "Virtual studio neon grid composite",
        0xFF9B75FF,
        isPremium = false,
        extraCategories = setOf(VideoEffectCategory.NEON)
    ),
    CARTOON_STYLE(
        "Anime / Toon",
        VideoEffectCategory.AI_CREATIVE,
        "Cel-shaded posterized illustration",
        0xFFB99CFF,
        isNew = true
    ),
    SKETCH_EFFECT(
        "Noir Gold Sketch",
        VideoEffectCategory.AI_CREATIVE,
        "High-contrast graphic novel lines",
        0xFFD6B46A,
        extraCategories = setOf(VideoEffectCategory.VINTAGE)
    ),
    FACE_TRACKING(
        "Face Lock HUD",
        VideoEffectCategory.AI_CREATIVE,
        "Dynamic biometric focus frame",
        0xFFD6B46A,
        isNew = true
    ),
    OBJECT_TRACKING(
        "Object Track Box",
        VideoEffectCategory.AI_CREATIVE,
        "Motion-locked telemetry callout",
        0xFF9B75FF,
        isNew = true,
        extraCategories = setOf(VideoEffectCategory.MOTION)
    ),
    AI_BG_REMOVAL(
        "Cutout Glow",
        VideoEffectCategory.AI_CREATIVE,
        "Edge-highlighted subject isolation",
        0xFFD6B46A,
        isPremium = false,
        extraCategories = setOf(VideoEffectCategory.NEON)
    ),
    AI_VIDEO_ENHANCE(
        "Super Clarity",
        VideoEffectCategory.AI_CREATIVE,
        "HDR micro-contrast & detail boost",
        0xFFD6B46A,
        isTrending = true,
        isPremium = false
    ),

    // 4. Motion Effects
    SLOW_MOTION(
        "Optical Slow-Mo",
        VideoEffectCategory.MOTION,
        "Smooth 0.4x cinematic time stretch",
        0xFFD6B46A
    ),
    SPEED_RAMP(
        "Velocity Ramp",
        VideoEffectCategory.MOTION,
        "Fast-slow-fast curve acceleration",
        0xFF9B75FF,
        isTrending = true
    ),
    SMOOTH_ZOOM_FX(
        "Smooth Push-In",
        VideoEffectCategory.MOTION,
        "Gradual 1.25x dolly zoom",
        0xFFD6B46A
    ),
    CAMERA_SHAKE(
        "Handheld Cinema",
        VideoEffectCategory.MOTION,
        "Organic documentary camera sway",
        0xFFE5C478
    ),
    FREEZE_FRAME(
        "Freeze Frame",
        VideoEffectCategory.MOTION,
        "Dramatic snapshot hold + flash",
        0xFFF0D799,
        isNew = true,
        extraCategories = setOf(VideoEffectCategory.LIGHT)
    ),
    MOTION_TRAIL(
        "Ghost Trail",
        VideoEffectCategory.MOTION,
        "Multi-frame echo afterimage",
        0xFF9B75FF,
        extraCategories = setOf(VideoEffectCategory.NEON)
    ),
    SPIN_MOTION(
        "Vortex Spin",
        VideoEffectCategory.MOTION,
        "Dynamic rotational roll",
        0xFFB99CFF
    ),
    DYNAMIC_ZOOM(
        "Beat Bounce Zoom",
        VideoEffectCategory.MOTION,
        "Rhythmic pulse zoom on beat",
        0xFFD6B46A,
        isTrending = true
    );

    fun matchesCategory(cat: VideoEffectCategory): Boolean {
        return category == cat || cat in extraCategories
    }
}

/**
 * Stackable, timeline-draggable Video Effect instance placed on the Effects Timeline track.
 */
data class VideoEffectItem(
    val id: String = UUID.randomUUID().toString(),
    val effectType: VideoEffectType,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val intensity: Float = 0.78f, // 0.1f to 1.0f
    val targetClipId: String? = null, // null = global timeline effect at range
    val customEffectName: String? = null,
    val customPrompt: String? = null,
    val customTintHex: Long? = null,
    val customBloom: Float = 0f,
    val customGlitch: Float = 0f,
    val customGrain: Float = 0f,
    val customVignette: Float = 0f,
    val customShake: Float = 0f,
    val customZoomPulse: Float = 0f,
    val customScanlines: Float = 0f,
    val customChromatic: Float = 0f
) {
    val durationMs: Long get() = (endTimeMs - startTimeMs).coerceAtLeast(300L)
    val displayLabel: String get() = customEffectName?.ifBlank { null } ?: effectType.label
    val displayAccentHex: Long get() = customTintHex ?: effectType.accentHex
    val isAiCustom: Boolean get() = !customEffectName.isNullOrBlank()

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("effectType", effectType.name)
        put("startTimeMs", startTimeMs)
        put("endTimeMs", endTimeMs)
        put("intensity", intensity.toDouble())
        put("targetClipId", targetClipId ?: "")
        put("customEffectName", customEffectName ?: "")
        put("customPrompt", customPrompt ?: "")
        if (customTintHex != null) put("customTintHex", customTintHex)
        put("customBloom", customBloom.toDouble())
        put("customGlitch", customGlitch.toDouble())
        put("customGrain", customGrain.toDouble())
        put("customVignette", customVignette.toDouble())
        put("customShake", customShake.toDouble())
        put("customZoomPulse", customZoomPulse.toDouble())
        put("customScanlines", customScanlines.toDouble())
        put("customChromatic", customChromatic.toDouble())
    }

    companion object {
        fun fromJson(obj: JSONObject): VideoEffectItem = VideoEffectItem(
            id = obj.optString("id", UUID.randomUUID().toString()),
            effectType = runCatching {
                VideoEffectType.valueOf(obj.optString("effectType", VideoEffectType.CINEMATIC_GLOW.name))
            }.getOrDefault(VideoEffectType.CINEMATIC_GLOW),
            startTimeMs = obj.optLong("startTimeMs", 0L),
            endTimeMs = obj.optLong("endTimeMs", 3000L),
            intensity = obj.optDouble("intensity", 0.78).toFloat().coerceIn(0.1f, 1.0f),
            targetClipId = obj.optString("targetClipId", "").ifBlank { null },
            customEffectName = obj.optString("customEffectName", "").ifBlank { null },
            customPrompt = obj.optString("customPrompt", "").ifBlank { null },
            customTintHex = if (obj.has("customTintHex")) obj.optLong("customTintHex", 0xFFD6B46A) else null,
            customBloom = obj.optDouble("customBloom", 0.0).toFloat(),
            customGlitch = obj.optDouble("customGlitch", 0.0).toFloat(),
            customGrain = obj.optDouble("customGrain", 0.0).toFloat(),
            customVignette = obj.optDouble("customVignette", 0.0).toFloat(),
            customShake = obj.optDouble("customShake", 0.0).toFloat(),
            customZoomPulse = obj.optDouble("customZoomPulse", 0.0).toFloat(),
            customScanlines = obj.optDouble("customScanlines", 0.0).toFloat(),
            customChromatic = obj.optDouble("customChromatic", 0.0).toFloat()
        )
    }
}

/**
 * Sound Effects Categories for the NOVACUT PRO Sound Studio:
 * Cinematic, Whoosh & Transitions, Bass & Action, Ambient & Nature, Social & Foley.
 */
enum class SfxCategory(val label: String, val accentHex: Long) {
    CINEMATIC("Cinematic", 0xFFD6B46A),
    TRANSITION("Whoosh & Cuts", 0xFF9B75FF),
    ACTION("Bass & Impact", 0xFFFF5A67),
    NATURE("Ambient & Nature", 0xFF34D399),
    SOCIAL("Social & Foley", 0xFFB99CFF)
}

/**
 * Catalog entry for the 30+ Original Synthesized Sound Effects.
 */
data class SoundEffectAsset(
    val id: String,
    val title: String,
    val category: SfxCategory,
    val durationMs: Long,
    val synthRecipeId: Int,
    val tags: String,
    val isTrending: Boolean = false
)

val BuiltInSoundEffectsCatalog: List<SoundEffectAsset> = listOf(
    // Cinematic Sounds
    SoundEffectAsset("sfx_cin_impact", "Cinematic Impact", SfxCategory.CINEMATIC, 2200L, 101, "boom hit trailer", isTrending = true),
    SoundEffectAsset("sfx_cin_deep_boom", "Deep Sub Boom", SfxCategory.CINEMATIC, 2500L, 102, "bass low sub drop", isTrending = true),
    SoundEffectAsset("sfx_cin_dramatic_hit", "Dramatic Braaam Hit", SfxCategory.CINEMATIC, 2400L, 103, "horn brass inception"),
    SoundEffectAsset("sfx_cin_rising_tension", "Rising Tension Swell", SfxCategory.CINEMATIC, 3000L, 104, "riser suspense build"),
    SoundEffectAsset("sfx_cin_suspense", "Dark Suspense Drone", SfxCategory.CINEMATIC, 3200L, 105, "thriller dark pulse"),
    SoundEffectAsset("sfx_cin_whoosh", "Cinematic Heavy Whoosh", SfxCategory.CINEMATIC, 1600L, 106, "flyby air sweep", isTrending = true),

    // Social Media & Foley Sounds
    SoundEffectAsset("sfx_soc_pop", "Bubble Pop", SfxCategory.SOCIAL, 600L, 201, "pop cute ui", isTrending = true),
    SoundEffectAsset("sfx_soc_click", "Mechanical Tactile Click", SfxCategory.SOCIAL, 500L, 202, "button tap switch"),
    SoundEffectAsset("sfx_soc_notif", "Crystal Notification Ding", SfxCategory.SOCIAL, 1100L, 203, "bell chime alert", isTrending = true),
    SoundEffectAsset("sfx_soc_shutter", "DSLR Camera Shutter", SfxCategory.SOCIAL, 700L, 204, "photo snap freeze"),
    SoundEffectAsset("sfx_soc_swipe", "Velvet Card Swipe", SfxCategory.SOCIAL, 800L, 205, "slide whoosh fast"),
    SoundEffectAsset("sfx_soc_funny", "Quirky Spring Boing", SfxCategory.SOCIAL, 1200L, 206, "comedy bounce meme"),
    SoundEffectAsset("sfx_soc_cartoon", "Cartoon Slide Whistle", SfxCategory.SOCIAL, 1300L, 207, "zip slip toon"),

    // Bass & Impact / Action Sounds
    SoundEffectAsset("sfx_act_explosion", "Cinema Explosion Blast", SfxCategory.ACTION, 2600L, 301, "boom blast detonate", isTrending = true),
    SoundEffectAsset("sfx_act_thunder", "Rolling Thunder Crack", SfxCategory.ACTION, 2800L, 302, "storm lightning"),
    SoundEffectAsset("sfx_act_wind", "Arctic Gale Wind", SfxCategory.ACTION, 3000L, 303, "blizzard air"),
    SoundEffectAsset("sfx_act_rain", "Heavy Storm Downpour", SfxCategory.ACTION, 3000L, 304, "rain weather"),
    SoundEffectAsset("sfx_act_fire", "Roaring Fire Torch", SfxCategory.ACTION, 2500L, 305, "flame crackle burn"),
    SoundEffectAsset("sfx_act_mech", "Cyber Mech Servo Lock", SfxCategory.ACTION, 1500L, 306, "robot gear transform", isTrending = true),

    // Whoosh & Transition Sounds
    SoundEffectAsset("sfx_trn_whoosh", "Hyper Whip Whoosh", SfxCategory.TRANSITION, 900L, 401, "fast cut pan", isTrending = true),
    SoundEffectAsset("sfx_trn_rev_whoosh", "Reverse Suction Whoosh", SfxCategory.TRANSITION, 1100L, 402, "reverse pull zoom"),
    SoundEffectAsset("sfx_trn_glitch", "Cyber Datamosh Glitch", SfxCategory.TRANSITION, 1000L, 403, "digital stutter static", isTrending = true),
    SoundEffectAsset("sfx_trn_sweep", "Neon Laser Sweep", SfxCategory.TRANSITION, 1200L, 404, "synth filter pass"),
    SoundEffectAsset("sfx_trn_bass_drop", "808 Sub Bass Drop", SfxCategory.TRANSITION, 1800L, 405, "sub beat drop", isTrending = true),
    SoundEffectAsset("sfx_trn_short_impact", "Punchy Cut Impact", SfxCategory.TRANSITION, 750L, 406, "thud hit accent"),

    // Ambient & Nature Sounds
    SoundEffectAsset("sfx_nat_birds", "Dawn Songbirds Chorus", SfxCategory.NATURE, 3000L, 501, "chirp morningpeace"),
    SoundEffectAsset("sfx_nat_ocean", "Pacific Ocean Waves", SfxCategory.NATURE, 3500L, 502, "beach surf tide", isTrending = true),
    SoundEffectAsset("sfx_nat_forest", "Enchanted Forest Breeze", SfxCategory.NATURE, 3200L, 503, "leaves woods ambient"),
    SoundEffectAsset("sfx_nat_water", "Mountain Creek Stream", SfxCategory.NATURE, 3000L, 504, "river brook babble"),
    SoundEffectAsset("sfx_nat_rainfall", "Soft Window Rainfall", SfxCategory.NATURE, 3200L, 505, "lofi cozy rain"),
    SoundEffectAsset("sfx_nat_wind", "Canyon Whispering Wind", SfxCategory.NATURE, 3000L, 506, "desert breeze")
)

/**
 * Downloadable Effect & Sound Pack in the NOVACUT PRO Effects Store Marketplace.
 */
data class EffectStorePack(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String, // "TRENDING", "NEW", "FREE", "PREMIUM"
    val accentHex: Long,
    val previewResId: Int,
    val includedEffects: List<VideoEffectType>,
    val includedSfxIds: List<String>,
    val isFree: Boolean,
    val sizeMb: String
)

val BuiltInEffectStorePacks: List<EffectStorePack> = listOf(
    EffectStorePack(
        id = "pack_cyber_glitch_pro",
        title = "Royal Amethyst Glitch & VHS Suite",
        subtitle = "RGB Split, VHS 1998, Pixel Sort + 4 Glitch & Laser SFX",
        badge = "TRENDING",
        accentHex = 0xFF9B75FF,
        previewResId = R.drawable.img_novacut_pro_hero_1794494239931,
        includedEffects = listOf(
            VideoEffectType.GLITCH_EFFECT,
            VideoEffectType.RGB_SPLIT,
            VideoEffectType.VHS_EFFECT,
            VideoEffectType.PIXEL_SORTING
        ),
        includedSfxIds = listOf("sfx_trn_glitch", "sfx_trn_sweep", "sfx_act_mech"),
        isFree = true,
        sizeMb = "4.2 MB"
    ),
    EffectStorePack(
        id = "pack_anamorphic_cinema",
        title = "24K Gold Anamorphic & Flare",
        subtitle = "2.39:1 Letterbox, Gold Lens Flare, 35mm Grain + Braaam Hits",
        badge = "FREE",
        accentHex = 0xFFD6B46A,
        previewResId = R.drawable.img_template_cinematic_1794471451410,
        includedEffects = listOf(
            VideoEffectType.ANAMORPHIC_LENS,
            VideoEffectType.LENS_FLARE,
            VideoEffectType.FILM_GRAIN,
            VideoEffectType.CINEMATIC_GLOW
        ),
        includedSfxIds = listOf("sfx_cin_impact", "sfx_cin_deep_boom", "sfx_cin_dramatic_hit"),
        isFree = true,
        sizeMb = "6.8 MB"
    ),
    EffectStorePack(
        id = "pack_ai_motion_tracker",
        title = "Biometric HUD & Velocity",
        subtitle = "Face Tracking HUD, Object Lock, Bokeh + Whip Transitions",
        badge = "NEW",
        accentHex = 0xFFD6B46A,
        previewResId = R.drawable.img_hero_studio_banner_1794471447657,
        includedEffects = listOf(
            VideoEffectType.FACE_TRACKING,
            VideoEffectType.OBJECT_TRACKING,
            VideoEffectType.BG_BLUR,
            VideoEffectType.SPEED_RAMP
        ),
        includedSfxIds = listOf("sfx_trn_whoosh", "sfx_soc_shutter", "sfx_trn_bass_drop"),
        isFree = true,
        sizeMb = "5.1 MB"
    ),
    EffectStorePack(
        id = "pack_nature_ambient_lofi",
        title = "Organic Film & Ambient Foley",
        subtitle = "Light Leak, Vintage 8mm, Cozy Rain, Ocean Waves & Dawn Birds",
        badge = "FREE",
        accentHex = 0xFF34D399,
        previewResId = R.drawable.img_template_cinematic_1794471451410,
        includedEffects = listOf(
            VideoEffectType.LIGHT_LEAK,
            VideoEffectType.VINTAGE_FILM,
            VideoEffectType.CAMERA_SHAKE
        ),
        includedSfxIds = listOf("sfx_nat_ocean", "sfx_nat_rainfall", "sfx_nat_birds"),
        isFree = true,
        sizeMb = "3.9 MB"
    )
)

enum class FilterPreset(
    val label: String,
    val category: String,
    val rScale: Float,
    val gScale: Float,
    val bScale: Float,
    val saturationBoost: Float,
    val contrastBoost: Float,
    val hueShiftDegrees: Float = 0f,
    val isGrayscale: Boolean = false,
    val isInverted: Boolean = false,
    val accentHex: Long = 0xFFD6B46A,
    val effectSummary: String = "RgbAdjustment + Contrast + HslAdjustment"
) {
    ORIGINAL(
        label = "Original",
        category = "None",
        rScale = 1.0f,
        gScale = 1.0f,
        bScale = 1.0f,
        saturationBoost = 1.0f,
        contrastBoost = 1.0f,
        accentHex = 0xFFA4A7B3,
        effectSummary = "Bypass (Identity Matrix)"
    ),
    CYBERPUNK_NEON(
        label = "Royal Amethyst",
        category = "Luxury",
        rScale = 1.08f,
        gScale = 0.94f,
        bScale = 1.34f,
        saturationBoost = 1.32f,
        contrastBoost = 1.18f,
        hueShiftDegrees = -12f,
        accentHex = 0xFF9B75FF,
        effectSummary = "RgbAdjustment + HslAdjustment + Contrast"
    ),
    TEAL_ORANGE(
        label = "Teal & Gold",
        category = "Cinema",
        rScale = 1.18f,
        gScale = 1.06f,
        bScale = 0.88f,
        saturationBoost = 1.25f,
        contrastBoost = 1.16f,
        hueShiftDegrees = 6f,
        accentHex = 0xFFD6B46A,
        effectSummary = "RgbAdjustment + Contrast + HslAdjustment"
    ),
    GOLDEN_HOUR(
        label = "24K Champagne",
        category = "Warm",
        rScale = 1.26f,
        gScale = 1.14f,
        bScale = 0.80f,
        saturationBoost = 1.22f,
        contrastBoost = 1.10f,
        hueShiftDegrees = 8f,
        accentHex = 0xFFF0D799,
        effectSummary = "RgbAdjustment + HslAdjustment"
    ),
    VINTAGE_KODAK(
        label = "Kodak 2383",
        category = "Film",
        rScale = 1.14f,
        gScale = 1.06f,
        bScale = 0.88f,
        saturationBoost = 0.86f,
        contrastBoost = 1.14f,
        hueShiftDegrees = 4f,
        accentHex = 0xFFD99B4E,
        effectSummary = "RgbAdjustment + Contrast + HslAdjustment"
    ),
    NOIR_CONTRAST(
        label = "Obsidian Noir",
        category = "B&W",
        rScale = 1.0f,
        gScale = 1.0f,
        bScale = 1.0f,
        saturationBoost = 0.0f,
        contrastBoost = 1.38f,
        isGrayscale = true,
        accentHex = 0xFFE2E8F0,
        effectSummary = "RgbFilter.Grayscale + Contrast"
    ),
    DREAMY_PASTEL(
        label = "Velvet Glow",
        category = "Aesthetic",
        rScale = 1.10f,
        gScale = 1.04f,
        bScale = 1.16f,
        saturationBoost = 0.86f,
        contrastBoost = 0.92f,
        hueShiftDegrees = -6f,
        accentHex = 0xFFB99CFF,
        effectSummary = "RgbAdjustment + Soft Contrast"
    ),
    MATRIX_GREEN(
        label = "Emerald Studio",
        category = "LUT",
        rScale = 0.82f,
        gScale = 1.28f,
        bScale = 0.88f,
        saturationBoost = 1.16f,
        contrastBoost = 1.22f,
        hueShiftDegrees = 14f,
        accentHex = 0xFF10B981,
        effectSummary = "RgbAdjustment + HslAdjustment + Contrast"
    ),
    TOKYO_MIDNIGHT(
        label = "Tokyo Midnight",
        category = "Cyber",
        rScale = 0.86f,
        gScale = 1.12f,
        bScale = 1.38f,
        saturationBoost = 1.36f,
        contrastBoost = 1.24f,
        hueShiftDegrees = -18f,
        accentHex = 0xFF00F0FF,
        effectSummary = "RgbAdjustment + HslAdjustment(-18°)"
    ),
    SAHARA_WARMTH(
        label = "Sahara Amber",
        category = "Warm",
        rScale = 1.30f,
        gScale = 1.08f,
        bScale = 0.76f,
        saturationBoost = 1.28f,
        contrastBoost = 1.15f,
        hueShiftDegrees = 12f,
        accentHex = 0xFFF59E0B,
        effectSummary = "RgbAdjustment + Warm Hue(+12°)"
    ),
    ARCTIC_CINEMA(
        label = "Arctic Cinema",
        category = "Cinema",
        rScale = 0.84f,
        gScale = 1.08f,
        bScale = 1.32f,
        saturationBoost = 0.92f,
        contrastBoost = 1.26f,
        hueShiftDegrees = -10f,
        accentHex = 0xFF38BDF8,
        effectSummary = "RgbAdjustment + High Contrast"
    ),
    FUJI_ETERNA(
        label = "Fuji Eterna",
        category = "Film",
        rScale = 0.96f,
        gScale = 1.12f,
        bScale = 1.06f,
        saturationBoost = 0.82f,
        contrastBoost = 1.06f,
        hueShiftDegrees = 5f,
        accentHex = 0xFF34D399,
        effectSummary = "RgbAdjustment + Cinema Desat"
    ),
    ROSE_QUARTZ(
        label = "Rose Quartz",
        category = "Luxury",
        rScale = 1.24f,
        gScale = 0.94f,
        bScale = 1.14f,
        saturationBoost = 1.18f,
        contrastBoost = 1.08f,
        hueShiftDegrees = -8f,
        accentHex = 0xFFF472B6,
        effectSummary = "RgbAdjustment + Rose Hue(-8°)"
    ),
    SILVER_HALIDE(
        label = "Silver Halide",
        category = "B&W",
        rScale = 1.12f,
        gScale = 1.12f,
        bScale = 1.16f,
        saturationBoost = 0.0f,
        contrastBoost = 1.24f,
        isGrayscale = true,
        accentHex = 0xFFCBD5E1,
        effectSummary = "RgbFilter.Grayscale + Silver Gain"
    ),
    ANAMORPHIC_TEAL(
        label = "Blockbuster LUT",
        category = "LUT",
        rScale = 1.20f,
        gScale = 1.02f,
        bScale = 1.18f,
        saturationBoost = 1.34f,
        contrastBoost = 1.28f,
        hueShiftDegrees = -4f,
        accentHex = 0xFFD6B46A,
        effectSummary = "RgbAdjustment + Master Contrast"
    ),
    CYBER_INVERT(
        label = "Solarize Neg",
        category = "Cyber",
        rScale = 1.10f,
        gScale = 0.95f,
        bScale = 1.25f,
        saturationBoost = 1.35f,
        contrastBoost = 1.25f,
        isInverted = true,
        accentHex = 0xFFFF5A67,
        effectSummary = "RgbFilter.Inverted + RgbAdjustment"
    )
}

enum class FontChoice(val label: String) {
    SPACE_GROTESK("Space Grotesk"),
    BEBAS_NEUE("Bebas Neue"),
    INTER("Inter Clean"),
    PLAYFAIR("Playfair Serif"),
    PACIFICO("Pacifico Script"),
    JETBRAINS_MONO("JetBrains Mono");

    fun toFontFamily(): FontFamily = when (this) {
        SPACE_GROTESK -> SpaceGroteskFontFamily
        BEBAS_NEUE -> BebasNeueFontFamily
        INTER -> InterFontFamily
        PLAYFAIR -> PlayfairDisplayFontFamily
        PACIFICO -> PacificoFontFamily
        JETBRAINS_MONO -> JetBrainsMonoFontFamily
    }
}

enum class TextAnimationType(val label: String) {
    NONE("Static"),
    TYPEWRITER("Typewriter"),
    NEON_FLICKER("Gold Shimmer"),
    BOUNCE_IN("Pop Bounce"),
    SLIDE_UP("Rise Up"),
    KARAOKE_HIGHLIGHT("Beat Box")
}

data class VideoClip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val uriString: String,
    val sourceDurationMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = sourceDurationMs,
    val timelineStartMs: Long = 0L,
    val trackIndex: Int = 0, // 0 = Main Track, 1 = PIP / Overlay Video Track
    val playbackSpeed: Float = 1.0f,
    val isReversed: Boolean = false,
    val rotationDegrees: Float = 0f,
    val cropScale: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val transitionIn: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 500L,
    val filterPreset: FilterPreset = FilterPreset.ORIGINAL,
    val filterIntensity: Float = 1.0f, // 0.0f to 1.0f strength of Media3 Effect filter transformation
    val brightness: Float = 0f, // -0.5f to +0.5f
    val contrast: Float = 1.0f, // 0.5f to 1.8f
    val saturation: Float = 1.0f, // 0.0f to 2.0f
    val vignette: Float = 0.0f, // 0.0f to 1.0f
    val chromaKeyEnabled: Boolean = false,
    val chromaKeyColorHex: Long = 0xFF00FF00,
    val chromaKeyTolerance: Float = 0.25f,
    val isHighlight: Boolean = false
) {
    val effectiveDurationMs: Long
        get() {
            val raw = (trimEndMs - trimStartMs).coerceAtLeast(200L)
            return (raw / playbackSpeed.coerceIn(0.25f, 4.0f)).toLong().coerceAtLeast(200L)
        }

    val timelineEndMs: Long
        get() = timelineStartMs + effectiveDurationMs

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("uriString", uriString)
        put("sourceDurationMs", sourceDurationMs)
        put("trimStartMs", trimStartMs)
        put("trimEndMs", trimEndMs)
        put("timelineStartMs", timelineStartMs)
        put("trackIndex", trackIndex)
        put("playbackSpeed", playbackSpeed.toDouble())
        put("isReversed", isReversed)
        put("rotationDegrees", rotationDegrees.toDouble())
        put("cropScale", cropScale.toDouble())
        put("volume", volume.toDouble())
        put("isMuted", isMuted)
        put("transitionIn", transitionIn.name)
        put("transitionDurationMs", transitionDurationMs)
        put("filterPreset", filterPreset.name)
        put("filterIntensity", filterIntensity.toDouble())
        put("brightness", brightness.toDouble())
        put("contrast", contrast.toDouble())
        put("saturation", saturation.toDouble())
        put("vignette", vignette.toDouble())
        put("chromaKeyEnabled", chromaKeyEnabled)
        put("chromaKeyColorHex", chromaKeyColorHex)
        put("chromaKeyTolerance", chromaKeyTolerance.toDouble())
        put("isHighlight", isHighlight)
    }

    companion object {
        fun fromJson(obj: JSONObject): VideoClip {
            val rawTrans = obj.optString("transitionIn", "NONE")
            val mappedTrans = when (rawTrans) {
                "CROSS_DISSOLVE" -> TransitionType.FADE
                "FLASH_WHITE" -> TransitionType.FLASH
                "GLITCH_PULSE" -> TransitionType.GLITCH
                "ZOOM_WARP" -> TransitionType.SMOOTH_ZOOM
                "SLIDE_LEFT" -> TransitionType.SLIDE
                else -> runCatching { TransitionType.valueOf(rawTrans) }.getOrDefault(TransitionType.NONE)
            }
            return VideoClip(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", "Clip"),
                uriString = obj.optString("uriString", ""),
                sourceDurationMs = obj.optLong("sourceDurationMs", 5000L),
                trimStartMs = obj.optLong("trimStartMs", 0L),
                trimEndMs = obj.optLong("trimEndMs", 5000L),
                timelineStartMs = obj.optLong("timelineStartMs", 0L),
                trackIndex = obj.optInt("trackIndex", 0),
                playbackSpeed = obj.optDouble("playbackSpeed", 1.0).toFloat(),
                isReversed = obj.optBoolean("isReversed", false),
                rotationDegrees = obj.optDouble("rotationDegrees", 0.0).toFloat(),
                cropScale = obj.optDouble("cropScale", 1.0).toFloat(),
                volume = obj.optDouble("volume", 1.0).toFloat(),
                isMuted = obj.optBoolean("isMuted", false),
                transitionIn = mappedTrans,
                transitionDurationMs = obj.optLong("transitionDurationMs", mappedTrans.defaultDurationMs.coerceAtLeast(350L)),
                filterPreset = runCatching { FilterPreset.valueOf(obj.optString("filterPreset", "ORIGINAL")) }
                    .getOrDefault(FilterPreset.ORIGINAL),
                filterIntensity = obj.optDouble("filterIntensity", 1.0).toFloat().coerceIn(0f, 1f),
                brightness = obj.optDouble("brightness", 0.0).toFloat(),
                contrast = obj.optDouble("contrast", 1.0).toFloat(),
                saturation = obj.optDouble("saturation", 1.0).toFloat(),
                vignette = obj.optDouble("vignette", 0.0).toFloat(),
                chromaKeyEnabled = obj.optBoolean("chromaKeyEnabled", false),
                chromaKeyColorHex = obj.optLong("chromaKeyColorHex", 0xFF00FF00),
                chromaKeyTolerance = obj.optDouble("chromaKeyTolerance", 0.25).toFloat(),
                isHighlight = obj.optBoolean("isHighlight", false)
            )
        }
    }
}

data class AudioTrackItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val uriString: String,
    val durationMs: Long,
    val timelineStartMs: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
    val volume: Float = 0.85f,
    val fadeInMs: Long = 300L,
    val fadeOutMs: Long = 500L,
    val speed: Float = 1.0f, // 0.5f to 2.0f
    val pitchSemitones: Float = 0f, // -12f to +12f
    val isLooped: Boolean = false,
    val noiseReductionEnabled: Boolean = false,
    val audioDuckingEnabled: Boolean = false,
    val isVoiceover: Boolean = false,
    val isExtracted: Boolean = false,
    val isSfx: Boolean = false,
    val sfxCategoryName: String = "Music",
    val beatMarkersMs: List<Long> = listOf(500L, 1000L, 1500L, 2000L, 2500L, 3000L, 3500L),
    val waveformAmplitudes: List<Float> = listOf(
        0.25f, 0.6f, 0.85f, 0.45f, 0.9f, 0.7f, 0.35f, 0.8f, 0.95f, 0.5f,
        0.65f, 0.4f, 0.75f, 0.88f, 0.3f, 0.6f, 0.92f, 0.55f, 0.4f, 0.7f
    )
) {
    val effectiveDurationMs: Long
        get() {
            val base = ((trimEndMs - trimStartMs).coerceAtLeast(250L) / speed.coerceIn(0.5f, 2.0f)).toLong()
            return if (isLooped) base * 2L else base.coerceAtLeast(250L)
        }

    val timelineEndMs: Long
        get() = timelineStartMs + effectiveDurationMs

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("uriString", uriString)
        put("durationMs", durationMs)
        put("timelineStartMs", timelineStartMs)
        put("trimStartMs", trimStartMs)
        put("trimEndMs", trimEndMs)
        put("volume", volume.toDouble())
        put("fadeInMs", fadeInMs)
        put("fadeOutMs", fadeOutMs)
        put("speed", speed.toDouble())
        put("pitchSemitones", pitchSemitones.toDouble())
        put("isLooped", isLooped)
        put("noiseReductionEnabled", noiseReductionEnabled)
        put("audioDuckingEnabled", audioDuckingEnabled)
        put("isVoiceover", isVoiceover)
        put("isExtracted", isExtracted)
        put("isSfx", isSfx)
        put("sfxCategoryName", sfxCategoryName)
        val beatArr = JSONArray()
        beatMarkersMs.forEach { beatArr.put(it) }
        put("beatMarkersMs", beatArr)
        val waveArr = JSONArray()
        waveformAmplitudes.forEach { waveArr.put(it.toDouble()) }
        put("waveformAmplitudes", waveArr)
    }

    companion object {
        fun fromJson(obj: JSONObject): AudioTrackItem {
            val waveArr = obj.optJSONArray("waveformAmplitudes")
            val wave = if (waveArr != null && waveArr.length() > 0) {
                (0 until waveArr.length()).map { waveArr.optDouble(it, 0.5).toFloat() }
            } else {
                listOf(0.3f, 0.7f, 0.5f, 0.9f, 0.4f, 0.8f, 0.6f, 0.75f)
            }
            val beatArr = obj.optJSONArray("beatMarkersMs")
            val beats = if (beatArr != null && beatArr.length() > 0) {
                (0 until beatArr.length()).map { beatArr.optLong(it, 500L) }
            } else {
                listOf(500L, 1000L, 1500L, 2000L, 2500L, 3000L)
            }
            return AudioTrackItem(
                id = obj.optString("id", UUID.randomUUID().toString()),
                title = obj.optString("title", "Audio Track"),
                uriString = obj.optString("uriString", ""),
                durationMs = obj.optLong("durationMs", 8000L),
                timelineStartMs = obj.optLong("timelineStartMs", 0L),
                trimStartMs = obj.optLong("trimStartMs", 0L),
                trimEndMs = obj.optLong("trimEndMs", 8000L),
                volume = obj.optDouble("volume", 0.85).toFloat(),
                fadeInMs = obj.optLong("fadeInMs", 300L),
                fadeOutMs = obj.optLong("fadeOutMs", 500L),
                speed = obj.optDouble("speed", 1.0).toFloat(),
                pitchSemitones = obj.optDouble("pitchSemitones", 0.0).toFloat(),
                isLooped = obj.optBoolean("isLooped", false),
                noiseReductionEnabled = obj.optBoolean("noiseReductionEnabled", false),
                audioDuckingEnabled = obj.optBoolean("audioDuckingEnabled", false),
                isVoiceover = obj.optBoolean("isVoiceover", false),
                isExtracted = obj.optBoolean("isExtracted", false),
                isSfx = obj.optBoolean("isSfx", false),
                sfxCategoryName = obj.optString("sfxCategoryName", "Music"),
                beatMarkersMs = beats,
                waveformAmplitudes = wave
            )
        }
    }
}

data class TextOverlayItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val xNormalized: Float = 0.5f,
    val yNormalized: Float = 0.82f,
    val fontSizeSp: Float = 22f,
    val colorHex: Long = 0xFFD6B46A,
    val backgroundColorHex: Long = 0xCC08090D,
    val fontChoice: FontChoice = FontChoice.SPACE_GROTESK,
    val animationType: TextAnimationType = TextAnimationType.BOUNCE_IN,
    val isSubtitle: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("text", text)
        put("startTimeMs", startTimeMs)
        put("endTimeMs", endTimeMs)
        put("xNormalized", xNormalized.toDouble())
        put("yNormalized", yNormalized.toDouble())
        put("fontSizeSp", fontSizeSp.toDouble())
        put("colorHex", colorHex)
        put("backgroundColorHex", backgroundColorHex)
        put("fontChoice", fontChoice.name)
        put("animationType", animationType.name)
        put("isSubtitle", isSubtitle)
    }

    companion object {
        fun fromJson(obj: JSONObject): TextOverlayItem = TextOverlayItem(
            id = obj.optString("id", UUID.randomUUID().toString()),
            text = obj.optString("text", ""),
            startTimeMs = obj.optLong("startTimeMs", 0L),
            endTimeMs = obj.optLong("endTimeMs", 3000L),
            xNormalized = obj.optDouble("xNormalized", 0.5).toFloat(),
            yNormalized = obj.optDouble("yNormalized", 0.82).toFloat(),
            fontSizeSp = obj.optDouble("fontSizeSp", 22.0).toFloat(),
            colorHex = obj.optLong("colorHex", 0xFFD6B46A),
            backgroundColorHex = obj.optLong("backgroundColorHex", 0xCC08090D),
            fontChoice = runCatching { FontChoice.valueOf(obj.optString("fontChoice", "SPACE_GROTESK")) }
                .getOrDefault(FontChoice.SPACE_GROTESK),
            animationType = runCatching { TextAnimationType.valueOf(obj.optString("animationType", "BOUNCE_IN")) }
                .getOrDefault(TextAnimationType.BOUNCE_IN),
            isSubtitle = obj.optBoolean("isSubtitle", false)
        )
    }
}

data class StickerOverlayItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val symbol: String,
    val accentHex: Long,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val xNormalized: Float = 0.5f,
    val yNormalized: Float = 0.35f,
    val scale: Float = 1.0f,
    val rotationDegrees: Float = 0f
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("symbol", symbol)
        put("accentHex", accentHex)
        put("startTimeMs", startTimeMs)
        put("endTimeMs", endTimeMs)
        put("xNormalized", xNormalized.toDouble())
        put("yNormalized", yNormalized.toDouble())
        put("scale", scale.toDouble())
        put("rotationDegrees", rotationDegrees.toDouble())
    }

    companion object {
        fun fromJson(obj: JSONObject): StickerOverlayItem = StickerOverlayItem(
            id = obj.optString("id", UUID.randomUUID().toString()),
            label = obj.optString("label", "Sticker"),
            symbol = obj.optString("symbol", "✨"),
            accentHex = obj.optLong("accentHex", 0xFFD6B46A),
            startTimeMs = obj.optLong("startTimeMs", 0L),
            endTimeMs = obj.optLong("endTimeMs", 4000L),
            xNormalized = obj.optDouble("xNormalized", 0.5).toFloat(),
            yNormalized = obj.optDouble("yNormalized", 0.35).toFloat(),
            scale = obj.optDouble("scale", 1.0).toFloat(),
            rotationDegrees = obj.optDouble("rotationDegrees", 0.0).toFloat()
        )
    }
}

data class SmartTemplate(
    val id: String,
    val title: String,
    val category: String,
    val subtitle: String,
    val aspectRatio: AspectRatioPreset,
    val bpm: Int,
    val clipCount: Int,
    val durationSec: Int,
    val filterPreset: FilterPreset,
    val transitionType: TransitionType,
    val previewResId: Int,
    val musicPresetName: String,
    val defaultTitles: List<String>,
    val isPremium: Boolean = false
)

data class EditorSnapshot(
    val description: String,
    val videoClips: List<VideoClip>,
    val videoEffects: List<VideoEffectItem>,
    val audioTracks: List<AudioTrackItem>,
    val textOverlays: List<TextOverlayItem>,
    val stickerOverlays: List<StickerOverlayItem>,
    val aspectRatio: AspectRatioPreset
)

val BuiltInSmartTemplates = listOf(
    SmartTemplate(
        id = "tpl_cyberpunk_velocity",
        title = "Royal Gold & Amethyst Reel",
        category = "Cinema • Beat Sync",
        subtitle = "128 BPM Synthwave • Gold Flash Cuts • Amethyst LUT",
        aspectRatio = AspectRatioPreset.RATIO_9_16,
        bpm = 128,
        clipCount = 3,
        durationSec = 12,
        filterPreset = FilterPreset.CYBERPUNK_NEON,
        transitionType = TransitionType.GLITCH,
        previewResId = R.drawable.img_novacut_pro_hero_1794494239931,
        musicPresetName = "Cyberpunk Pulse 128 BPM",
        defaultTitles = listOf("NOVACUT STUDIO", "CINEMA CUTS", "MASTERED IN 4K"),
        isPremium = false
    ),
    SmartTemplate(
        id = "tpl_golden_cinema",
        title = "24K Anamorphic Golden Hour",
        category = "Cinema • Widescreen",
        subtitle = "24 FPS Film Look • Warm Teal & Gold • Smooth Dissolves",
        aspectRatio = AspectRatioPreset.RATIO_16_9,
        bpm = 96,
        clipCount = 3,
        durationSec = 15,
        filterPreset = FilterPreset.TEAL_ORANGE,
        transitionType = TransitionType.FADE,
        previewResId = R.drawable.img_template_cinematic_1794471451410,
        musicPresetName = "Golden Horizon Ambient",
        defaultTitles = listOf("CHASING GOLDEN LIGHT", "BEYOND THE HORIZON", "DIRECTED IN NOVACUT"),
        isPremium = false
    ),
    SmartTemplate(
        id = "tpl_creator_reel",
        title = "Creator Hook & Captions",
        category = "Reels • Captions",
        subtitle = "Dynamic Gold Captions • Smooth Zoom Cuts • 60 FPS",
        aspectRatio = AspectRatioPreset.RATIO_9_16,
        bpm = 120,
        clipCount = 3,
        durationSec = 10,
        filterPreset = FilterPreset.GOLDEN_HOUR,
        transitionType = TransitionType.SMOOTH_ZOOM,
        previewResId = R.drawable.img_hero_studio_banner_1794471447657,
        musicPresetName = "Lo-Fi Creator Groove",
        defaultTitles = listOf("WAIT FOR THE DROP ✨", "STUDIO EDITS", "NOVACUT"),
        isPremium = false
    )
)
