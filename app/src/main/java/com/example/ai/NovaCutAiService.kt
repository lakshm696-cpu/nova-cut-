package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.model.AiCustomVideoEffect
import com.example.model.AiEnhancementSuggestion
import com.example.model.AiSceneCut
import com.example.model.BuiltInSoundEffectsCatalog
import com.example.model.FilterPreset
import com.example.model.FontChoice
import com.example.model.TextAnimationType
import com.example.model.TextOverlayItem
import com.example.model.VideoEffectType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "NovaCutAiService"

sealed interface AiTaskState {
    data object Idle : AiTaskState
    data class Running(val taskLabel: String) : AiTaskState
    data class Success(val message: String) : AiTaskState
    data class Error(val errorMessage: String) : AiTaskState
}

/**
 * Real Firebase AI Logic (`Firebase.ai`) service for NOVACUT PRO using `gemini-3.8-flash`
 * with structured JSON schemas (`Schema.obj`) and multimodal frame analysis:
 * 1. AI Custom Video Effect Generator from Text Description (`generateCustomVideoEffectFromPrompt`)
 * 2. AI Automatic Subtitles (`generateAutoSubtitles`)
 * 3. AI Multimodal Scene & Highlight Detection (`detectScenesAndHighlights`)
 * 4. AI Video Colorist & Enhancement (`analyzeAndEnhanceVideo`)
 */
class NovaCutAiService {

    // 1. Schema for AI Text-to-Effect Custom Video Effect Generator
    private val customEffectSchema = Schema.obj(
        properties = mapOf(
            "effectName" to Schema.string(description = "Luxury creative name for the custom video effect (2 to 4 words)"),
            "tagline" to Schema.string(description = "Concise technical summary of the visual shader and motion look"),
            "baseEffectType" to Schema.enumeration(
                values = VideoEffectType.entries.map { it.name },
                description = "Closest matching base shader architecture from the studio catalog"
            ),
            "tintColorHex" to Schema.string(description = "6-digit hex color code starting with #, e.g. #D6B46A or #9B75FF"),
            "bloomIntensity" to Schema.double(description = "Optical halation and glow bloom intensity from 0.0 to 1.0"),
            "glitchIntensity" to Schema.double(description = "Digital datamosh and RGB slice displacement from 0.0 to 1.0"),
            "grainAmount" to Schema.double(description = "35mm film grain texture density from 0.0 to 1.0"),
            "vignetteAmount" to Schema.double(description = "Anamorphic edge darkening vignette from 0.0 to 1.0"),
            "shakeIntensity" to Schema.double(description = "Camera tremor or handheld sway intensity from 0.0 to 1.0"),
            "zoomPulse" to Schema.double(description = "Rhythmic beat zoom or dolly push intensity from 0.0 to 1.0"),
            "scanlinesIntensity" to Schema.double(description = "CRT or VHS scanline overlay intensity from 0.0 to 1.0"),
            "chromaticShift" to Schema.double(description = "Chromatic prism dispersion shift from 0.0 to 1.0"),
            "recommendedSfxId" to Schema.enumeration(
                values = BuiltInSoundEffectsCatalog.map { it.id },
                description = "Best matching synthesized sound effect ID to pair with this visual effect"
            )
        )
    )

    // 2. Schema for Automatic Timed Subtitles
    private val subtitlesSchema = Schema.obj(
        properties = mapOf(
            "captions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "text" to Schema.string(description = "Short punchy subtitle text (3 to 7 words)"),
                        "startMs" to Schema.integer(description = "Start timestamp in milliseconds"),
                        "endMs" to Schema.integer(description = "End timestamp in milliseconds")
                    )
                ),
                description = "Timed subtitle segments covering the video timeline"
            )
        )
    )

    // 3. Schema for Multimodal Scene Detection & Highlight Selection
    private val sceneDetectionSchema = Schema.obj(
        properties = mapOf(
            "scenes" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "timestampMs" to Schema.integer(description = "Timestamp of scene cut or highlight in milliseconds"),
                        "label" to Schema.string(description = "Concise title of the detected visual scene"),
                        "confidence" to Schema.double(description = "Confidence score from 0.6 to 0.99"),
                        "isHighlight" to Schema.boolean(description = "True if this moment is a high-energy visual highlight"),
                        "reason" to Schema.string(description = "Why this frame stands out visually")
                    )
                ),
                description = "Detected scene cuts and highlight moments"
            )
        )
    )

    // 4. Schema for AI Video Colorist & Enhancement
    private val enhancementSchema = Schema.obj(
        properties = mapOf(
            "summary" to Schema.string(description = "Visual analysis of lighting, dynamic range, and color balance"),
            "recommendedFilter" to Schema.enumeration(
                values = FilterPreset.entries.map { it.name },
                description = "Best matching LUT filter preset name"
            ),
            "brightness" to Schema.double(description = "Recommended brightness offset between -0.25 and 0.25"),
            "contrast" to Schema.double(description = "Recommended contrast multiplier between 0.9 and 1.45"),
            "saturation" to Schema.double(description = "Recommended saturation multiplier between 0.85 and 1.45"),
            "pacingAdvice" to Schema.string(description = "Actionable editing advice on cut speed and transitions"),
            "suggestedHookTitle" to Schema.string(description = "High-retention opening title overlay text")
        )
    )

    private val customEffectModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = customEffectSchema
            }
        )
    }

    private val subtitleModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = subtitlesSchema
            }
        )
    }

    private val sceneDetectionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = sceneDetectionSchema
            }
        )
    }

    private val enhancementModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = enhancementSchema
            }
        )
    }

    /**
     * AI-Powered Custom Video Effect Generator:
     * Translates a user's natural-language text description (plus optional video frame) into a
     * multi-parameter visual shader & motion effect recipe that renders live on the preview stage
     * and exports through AndroidX Media3 Transformer.
     */
    suspend fun generateCustomVideoEffectFromPrompt(
        userPrompt: String,
        sampleFrame: Bitmap?
    ): Result<AiCustomVideoEffect> {
        return try {
            val promptText = """
                You are the lead visual effects shader architect for NOVACUT PRO, a luxury cinema video editing studio.
                The user wants to create a custom real-time video effect from this description:
                "$userPrompt"
                
                Design a cohesive, expressive multi-layer video effect recipe that matches their exact creative vision:
                - Choose the closest baseEffectType from the enum.
                - Pick an atmospheric hex tintColorHex (e.g. #D6B46A champagne gold, #9B75FF royal purple, #FF5A67 crimson, #38BDF8 ice cyan, #F59E0B amber).
                - Dial in precise 0.0 to 1.0 shader parameters for bloomIntensity, glitchIntensity, grainAmount, vignetteAmount, shakeIntensity, zoomPulse, scanlinesIntensity, and chromaticShift.
                - Choose the best matching recommendedSfxId from the sound effects catalog.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                if (sampleFrame != null) {
                    customEffectModel.generateContent(
                        content {
                            image(sampleFrame)
                            text(promptText)
                        }
                    )
                } else {
                    customEffectModel.generateContent(promptText)
                }
            }

            val jsonText = response.text ?: throw IllegalStateException("Model returned empty custom effect JSON.")
            val root = JSONObject(jsonText)

            val baseTypeName = root.optString("baseEffectType", VideoEffectType.CINEMATIC_GLOW.name)
            val baseType = runCatching { VideoEffectType.valueOf(baseTypeName) }
                .getOrDefault(VideoEffectType.CINEMATIC_GLOW)

            val rawHex = root.optString("tintColorHex", "#D6B46A").removePrefix("#").trim()
            val parsedColorHex = runCatching {
                ("FF" + rawHex.takeLast(6).padStart(6, '0')).toLong(16)
            }.getOrDefault(0xFFD6B46AL)

            val effect = AiCustomVideoEffect(
                userPrompt = userPrompt,
                effectName = root.optString("effectName", "AI Custom Gold Shader"),
                tagline = root.optString("tagline", "Custom Gemini 3.8 multi-pass optical & motion shader"),
                baseEffectType = baseType,
                tintColorHex = parsedColorHex,
                bloomIntensity = root.optDouble("bloomIntensity", 0.65).toFloat().coerceIn(0f, 1f),
                glitchIntensity = root.optDouble("glitchIntensity", 0.15).toFloat().coerceIn(0f, 1f),
                grainAmount = root.optDouble("grainAmount", 0.25).toFloat().coerceIn(0f, 1f),
                vignetteAmount = root.optDouble("vignetteAmount", 0.35).toFloat().coerceIn(0f, 1f),
                shakeIntensity = root.optDouble("shakeIntensity", 0.10).toFloat().coerceIn(0f, 1f),
                zoomPulse = root.optDouble("zoomPulse", 0.20).toFloat().coerceIn(0f, 1f),
                scanlinesIntensity = root.optDouble("scanlinesIntensity", 0.10).toFloat().coerceIn(0f, 1f),
                chromaticShift = root.optDouble("chromaticShift", 0.25).toFloat().coerceIn(0f, 1f),
                recommendedSfxId = root.optString("recommendedSfxId", "sfx_cin_impact")
            )
            Result.success(effect)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Custom Effect generator timed out", e)
            Result.failure(Exception("AI Custom Effect generation timed out after 60s. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Custom Effect generator failed", e)
            Result.failure(e)
        }
    }

    /**
     * Generates frame-accurate timed subtitles using Gemini 3.8 Flash multimodal analysis.
     */
    suspend fun generateAutoSubtitles(
        projectTitle: String,
        clipTitles: List<String>,
        totalDurationMs: Long,
        sampleFrame: Bitmap?
    ): Result<List<TextOverlayItem>> {
        return try {
            val safeDuration = totalDurationMs.coerceAtLeast(3000L)
            val promptText = """
                You are an expert luxury video editor generating timed dynamic captions for a video project titled "$projectTitle".
                The timeline contains clips: ${clipTitles.joinToString(", ")}.
                Total timeline duration is $safeDuration ms.
                Generate 4 to 6 engaging, rhythmic, creator-style subtitle segments that span from 0 ms to $safeDuration ms without overlapping.
                Keep each caption concise (3 to 7 words) and impactful.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                if (sampleFrame != null) {
                    subtitleModel.generateContent(
                        content {
                            image(sampleFrame)
                            text(promptText)
                        }
                    )
                } else {
                    subtitleModel.generateContent(promptText)
                }
            }

            val jsonText = response.text ?: throw IllegalStateException("Model returned empty subtitle JSON.")
            val root = JSONObject(jsonText)
            val arr = root.getJSONArray("captions")
            val subtitles = (0 until arr.length()).map { i ->
                val item = arr.getJSONObject(i)
                val start = item.optLong("startMs", i * 2000L).coerceIn(0L, (safeDuration - 500L).coerceAtLeast(0L))
                val end = item.optLong("endMs", start + 1800L).coerceIn(start + 400L, safeDuration)
                TextOverlayItem(
                    text = item.optString("text", "CAPTION ${i + 1}"),
                    startTimeMs = start,
                    endTimeMs = end,
                    xNormalized = 0.5f,
                    yNormalized = 0.84f,
                    fontSizeSp = 20f,
                    colorHex = 0xFFD6B46A,
                    backgroundColorHex = 0xDD08090D,
                    fontChoice = FontChoice.SPACE_GROTESK,
                    animationType = TextAnimationType.KARAOKE_HIGHLIGHT,
                    isSubtitle = true
                )
            }
            Result.success(subtitles)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Auto Subtitles timed out", e)
            Result.failure(Exception("AI subtitle generation timed out after 60s. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Auto Subtitles failed", e)
            Result.failure(e)
        }
    }

    /**
     * Multimodal AI Scene Detection & Highlight Selection using extracted video frames.
     */
    suspend fun detectScenesAndHighlights(
        clipTitle: String,
        durationMs: Long,
        frames: List<Bitmap>
    ): Result<List<AiSceneCut>> {
        return try {
            val safeDuration = durationMs.coerceAtLeast(2000L)
            val promptText = """
                Analyze these sequential video frames from clip "$clipTitle" (duration $safeDuration ms).
                Identify 3 to 4 distinct scene cut points or visual highlight moments across the timeline (between 400 ms and ${safeDuration - 400L} ms).
                Mark the most visually dynamic moments with isHighlight = true and explain why.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                sceneDetectionModel.generateContent(
                    content {
                        frames.take(3).forEach { bmp -> image(bmp) }
                        text(promptText)
                    }
                )
            }

            val jsonText = response.text ?: throw IllegalStateException("Model returned empty scene detection JSON.")
            val root = JSONObject(jsonText)
            val arr = root.getJSONArray("scenes")
            val cuts = (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                AiSceneCut(
                    timestampMs = obj.optLong("timestampMs", ((i + 1) * safeDuration) / (arr.length() + 1))
                        .coerceIn(300L, (safeDuration - 300L).coerceAtLeast(400L)),
                    label = obj.optString("label", "Scene ${i + 1}"),
                    confidenceScore = obj.optDouble("confidence", 0.92).toFloat().coerceIn(0.5f, 0.99f),
                    isHighlight = obj.optBoolean("isHighlight", i == 0),
                    reason = obj.optString("reason", "Strong visual contrast and motion energy")
                )
            }
            Result.success(cuts)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Scene Detection timed out", e)
            Result.failure(Exception("AI Scene Detection timed out after 60s. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Scene Detection failed", e)
            Result.failure(e)
        }
    }

    /**
     * Multimodal AI Video Enhancement & Color Grading Advisor.
     */
    suspend fun analyzeAndEnhanceVideo(
        clipTitle: String,
        currentFilter: FilterPreset,
        sampleFrame: Bitmap?
    ): Result<AiEnhancementSuggestion> {
        return try {
            val promptText = """
                You are a senior Hollywood colorist and luxury video director analyzing a frame from clip "$clipTitle" (current LUT: ${currentFilter.label}).
                Recommend the optimal color grading preset, fine brightness (-0.2 to +0.2), contrast (0.95 to 1.4), and saturation (0.9 to 1.4) values to maximize cinematic luxury, plus pacing advice and a punchy opening hook title.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                if (sampleFrame != null) {
                    enhancementModel.generateContent(
                        content {
                            image(sampleFrame)
                            text(promptText)
                        }
                    )
                } else {
                    enhancementModel.generateContent(promptText)
                }
            }

            val jsonText = response.text ?: throw IllegalStateException("Model returned empty enhancement JSON.")
            val root = JSONObject(jsonText)
            val filterName = root.optString("recommendedFilter", FilterPreset.GOLDEN_HOUR.name)
            val preset = runCatching { FilterPreset.valueOf(filterName) }.getOrDefault(FilterPreset.TEAL_ORANGE)

            Result.success(
                AiEnhancementSuggestion(
                    summary = root.optString("summary", "Enhanced shadow detail and boosted golden midtone separation."),
                    recommendedFilter = preset,
                    recommendedBrightness = root.optDouble("brightness", 0.05).toFloat().coerceIn(-0.3f, 0.3f),
                    recommendedContrast = root.optDouble("contrast", 1.18).toFloat().coerceIn(0.8f, 1.5f),
                    recommendedSaturation = root.optDouble("saturation", 1.22).toFloat().coerceIn(0.6f, 1.6f),
                    pacingAdvice = root.optString("pacingAdvice", "Use smooth 0.5s gold flash cuts on the beat drop."),
                    suggestedHookTitle = root.optString("suggestedHookTitle", "NOVACUT PRO MASTER")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Video Enhancement timed out", e)
            Result.failure(Exception("AI Video Enhancement timed out after 60s. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Video Enhancement failed", e)
            Result.failure(e)
        }
    }
}
