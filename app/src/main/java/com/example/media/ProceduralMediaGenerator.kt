package com.example.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.model.SoundEffectAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private const val TAG = "ProceduralMediaGen"

data class StudioSampleVideo(
    val title: String,
    val uriString: String,
    val durationMs: Long,
    val themeIndex: Int
)

data class StudioSampleAudio(
    val title: String,
    val category: String,
    val uriString: String,
    val durationMs: Long,
    val isSfx: Boolean,
    val waveform: List<Float>
)

object ProceduralMediaGenerator {

    private var previewPlayer: MediaPlayer? = null

    /**
     * Generates 3 distinct, real playable MP4 video clips on-device using MediaCodec + MediaMuxer
     * so users can test multi-track editing, splitting, trimming, filters, effects, and real Media3
     * Transformer MP4 export immediately in the emulator or device.
     */
    suspend fun ensureStudioSampleVideos(context: Context): List<StudioSampleVideo> =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "studio_samples").apply { mkdirs() }
            val specs = listOf(
                Triple("Neon Cyber City Run.mp4", "Neon Cyber Run", 0),
                Triple("Golden Horizon Drone.mp4", "Golden Drone Cut", 1),
                Triple("Studio Chroma Performer.mp4", "Chroma Key Subject", 2)
            )

            specs.map { (fileName, label, themeIdx) ->
                val targetFile = File(dir, fileName)
                val durationMs = 4000L
                if (!targetFile.exists() || targetFile.length() < 1024L) {
                    runCatching {
                        generateMp4Clip(targetFile, themeIdx, width = 640, height = 360, fps = 24, durationSeconds = 4)
                    }.onFailure { e ->
                        Log.w(TAG, "Hardware H.264 encode fallback for $fileName", e)
                    }
                }
                StudioSampleVideo(
                    title = label,
                    uriString = Uri.fromFile(targetFile).toString(),
                    durationMs = durationMs,
                    themeIndex = themeIdx
                )
            }
        }

    /**
     * Synthesizes real playable WAV audio tracks and core sound effects on-device.
     */
    suspend fun ensureStudioSampleAudio(context: Context): List<StudioSampleAudio> =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "studio_audio").apply { mkdirs() }
            val items = listOf(
                Triple("cyberpunk_pulse.wav", "Cyberpunk Pulse 128 BPM", false),
                Triple("golden_horizon.wav", "Golden Horizon Ambient", false),
                Triple("lofi_creator_groove.wav", "Lo-Fi Creator Groove", false),
                Triple("sfx_cyber_whoosh.wav", "Cyber Whoosh Transition", true),
                Triple("sfx_sub_drop.wav", "Cinema Sub Bass Impact", true),
                Triple("sfx_glitch_spark.wav", "Digital Glitch Riser", true)
            )

            items.mapIndexed { idx, (fileName, title, isSfx) ->
                val file = File(dir, fileName)
                val durationSec = if (isSfx) 2 else 6
                if (!file.exists() || file.length() < 512L) {
                    writeSynthesizedWav(file, durationSec = durationSec, presetIndex = idx)
                }
                val waveform = (0 until 24).map { step ->
                    val phase = (step + idx * 3) * 0.45f
                    (0.25f + 0.68f * abs(sin(phase))).coerceIn(0.18f, 0.98f)
                }
                StudioSampleAudio(
                    title = title,
                    category = if (isSfx) "SFX" else "Music",
                    uriString = Uri.fromFile(file).toString(),
                    durationMs = durationSec * 1000L,
                    isSfx = isSfx,
                    waveform = waveform
                )
            }
        }

    /**
     * Synthesizes a dedicated acoustic PCM WAV file for any of the 30+ SoundEffectAsset items
     * across Cinematic, Social Media, Action, Transition, and Nature categories.
     */
    suspend fun ensureSoundEffectFile(context: Context, asset: SoundEffectAsset): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "sfx_catalog").apply { mkdirs() }
            val outFile = File(dir, "${asset.id}.wav")
            if (!outFile.exists() || outFile.length() < 512L) {
                writeAcousticSfxWav(outFile, asset.durationMs, asset.synthRecipeId)
            }
            outFile
        }

    /**
     * Plays a real audio preview of any SoundEffectAsset or AudioTrackItem URI with optional
     * speed and pitch semitone adjustments.
     */
    suspend fun playSoundEffectPreview(
        context: Context,
        asset: SoundEffectAsset,
        speed: Float = 1.0f,
        pitchSemitones: Float = 0f
    ): String {
        val file = ensureSoundEffectFile(context, asset)
        playAudioFileUri(
            context = context,
            uriString = Uri.fromFile(file).toString(),
            speed = speed,
            pitchSemitones = pitchSemitones
        )
        return Uri.fromFile(file).toString()
    }

    fun playAudioFileUri(
        context: Context,
        uriString: String,
        speed: Float = 1.0f,
        pitchSemitones: Float = 0f,
        volume: Float = 0.9f
    ) {
        try {
            previewPlayer?.runCatching {
                if (isPlaying) stop()
                release()
            }
            val player = MediaPlayer()
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file" && uri.path != null) {
                player.setDataSource(uri.path!!)
            } else {
                player.setDataSource(context, uri)
            }
            player.prepare()
            player.setVolume(volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pitchMultiplier = 2.0.pow(pitchSemitones.toDouble() / 12.0).toFloat().coerceIn(0.5f, 2.0f)
                val params = PlaybackParams()
                    .setSpeed(speed.coerceIn(0.5f, 2.0f))
                    .setPitch(pitchMultiplier)
                runCatching { player.playbackParams = params }
            }
            player.start()
            previewPlayer = player
        } catch (e: Exception) {
            Log.w(TAG, "Audio preview playback notice: ${e.message}")
        }
    }

    fun stopAudioPreview() {
        previewPlayer?.runCatching {
            if (isPlaying) stop()
            release()
        }
        previewPlayer = null
    }

    private fun writeAcousticSfxWav(file: File, durationMs: Long, recipeId: Int) {
        val sampleRate = 22050
        val durationSec = (durationMs / 1000.0).coerceIn(0.4, 5.0)
        val numSamples = (sampleRate * durationSec).toInt().coerceAtLeast(1000)
        val pcmData = ByteArray(numSamples * 2)
        val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)
        val rng = Random(recipeId)
        var brownState = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate.toDouble()
            val progress = t / durationSec
            val whiteNoise = rng.nextDouble(-1.0, 1.0)
            brownState = (brownState + 0.02 * whiteNoise).coerceIn(-1.0, 1.0)

            val rawSample: Double = when (recipeId) {
                // Cinematic Sounds
                101 -> { // Cinematic Impact
                    val env = exp(-t * 2.4)
                    val sub = sin(2.0 * PI * (68.0 * exp(-t * 0.6)) * t)
                    val punch = whiteNoise * exp(-t * 14.0)
                    (0.75 * sub + 0.45 * punch) * env
                }
                102 -> { // Deep Sub Boom
                    val env = exp(-t * 1.6)
                    sin(2.0 * PI * (55.0 - 22.0 * progress) * t) * env
                }
                103 -> { // Dramatic Braaam Hit
                    val env = sin(PI * progress).pow(0.6)
                    val saw = 0.5 * sin(2.0 * PI * 65.4 * t) + 0.3 * sin(2.0 * PI * 130.8 * t) + 0.2 * sin(2.0 * PI * 196.2 * t)
                    saw * env
                }
                104 -> { // Rising Tension Swell
                    val env = progress.pow(1.8)
                    val freq = 140.0 + 520.0 * progress.pow(2.0)
                    (0.65 * sin(2.0 * PI * freq * t) + 0.35 * brownState) * env
                }
                105 -> { // Dark Suspense Drone
                    val env = sin(PI * progress)
                    (0.5 * sin(2.0 * PI * 58.0 * t) + 0.4 * sin(2.0 * PI * 61.5 * t) + 0.15 * brownState) * env
                }
                106 -> { // Cinematic Heavy Whoosh
                    val env = sin(PI * progress).pow(2.2)
                    val sweep = sin(2.0 * PI * (90.0 + 260.0 * sin(PI * progress)) * t)
                    (0.55 * sweep + 0.55 * brownState) * env
                }

                // Social Media Sounds
                201 -> { // Bubble Pop
                    val env = exp(-t * 12.0)
                    val freq = 240.0 + 680.0 * progress
                    sin(2.0 * PI * freq * t) * env
                }
                202 -> { // Mechanical Click
                    val click1 = exp(-t * 45.0) * sin(2.0 * PI * 1400.0 * t)
                    val click2 = if (t > 0.06) exp(-(t - 0.06) * 50.0) * sin(2.0 * PI * 950.0 * t) else 0.0
                    click1 + click2
                }
                203 -> { // Crystal Notification Ding
                    val env = exp(-t * 3.5)
                    0.6 * sin(2.0 * PI * 1046.5 * t) * env + 0.4 * sin(2.0 * PI * 1567.98 * t) * exp(-t * 4.5)
                }
                204 -> { // DSLR Camera Shutter
                    val snap1 = whiteNoise * exp(-t * 35.0)
                    val snap2 = if (t > 0.12) whiteNoise * exp(-(t - 0.12) * 32.0) else 0.0
                    0.8 * (snap1 + snap2)
                }
                205 -> { // Velvet Swipe
                    val env = sin(PI * progress).pow(1.6)
                    (0.6 * brownState + 0.4 * sin(2.0 * PI * (300.0 + 400.0 * progress) * t)) * env
                }
                206 -> { // Quirky Spring Boing
                    val env = exp(-t * 2.8)
                    val vibrato = 260.0 + 95.0 * sin(2.0 * PI * 14.0 * t)
                    sin(2.0 * PI * vibrato * t) * env
                }
                207 -> { // Cartoon Slide Whistle
                    val env = sin(PI * progress)
                    val freq = 420.0 + 780.0 * sin(PI * progress * 1.4)
                    sin(2.0 * PI * freq * t) * env
                }

                // Action Sounds
                301 -> { // Cinema Explosion
                    val env = exp(-t * 1.8)
                    (0.65 * brownState + 0.35 * whiteNoise * exp(-t * 5.0)) * env
                }
                302 -> { // Rolling Thunder
                    val crack = whiteNoise * exp(-t * 6.0)
                    val rumble = brownState * sin(PI * progress) * (0.6 + 0.4 * sin(2.0 * PI * 4.0 * t))
                    0.45 * crack + 0.75 * rumble
                }
                303, 506 -> { // Wind
                    val gust = 0.5 + 0.5 * sin(2.0 * PI * 0.8 * t)
                    (0.75 * brownState + 0.25 * sin(2.0 * PI * (180.0 + 60.0 * gust) * t)) * gust
                }
                304, 505 -> { // Rain
                    (0.55 * brownState + 0.35 * whiteNoise) * 0.75
                }
                305 -> { // Roaring Fire
                    val crackle = if (rng.nextDouble() > 0.96) whiteNoise * 0.9 else 0.0
                    0.65 * brownState + crackle
                }
                306 -> { // Cyber Mech Servo
                    val stepFreq = if (progress < 0.5) 280.0 else 420.0
                    val env = sin(PI * progress)
                    (0.6 * sin(2.0 * PI * stepFreq * t) + 0.3 * sin(2.0 * PI * (stepFreq * 2.5) * t)) * env
                }

                // Transition Sounds
                401 -> { // Hyper Whip Whoosh
                    val env = sin(PI * progress).pow(2.5)
                    (0.5 * whiteNoise + 0.5 * sin(2.0 * PI * (160.0 + 500.0 * progress) * t)) * env
                }
                402 -> { // Reverse Suction Whoosh
                    val env = progress.pow(2.8)
                    (0.5 * brownState + 0.5 * sin(2.0 * PI * (120.0 + 600.0 * progress) * t)) * env
                }
                403 -> { // Cyber Datamosh Glitch
                    val sliceFreq = ((t * 18.0).toInt() % 5 + 1) * 220.0
                    val gate = if (((t * 28.0).toInt() % 2) == 0) 1.0 else -0.4
                    (0.6 * sin(2.0 * PI * sliceFreq * t) + 0.4 * whiteNoise) * gate * sin(PI * progress)
                }
                404 -> { // Neon Laser Sweep
                    val env = exp(-t * 2.5)
                    val freq = 1400.0 * exp(-t * 3.2) + 90.0
                    sin(2.0 * PI * freq * t) * env
                }
                405 -> { // 808 Club Bass Drop
                    val env = exp(-t * 1.9)
                    sin(2.0 * PI * (95.0 * exp(-t * 1.1) + 32.0) * t) * env
                }
                406 -> { // Punchy Cut Impact
                    val env = exp(-t * 7.5)
                    (0.75 * sin(2.0 * PI * 90.0 * t) + 0.35 * whiteNoise) * env
                }

                // Nature Sounds
                501 -> { // Dawn Songbirds
                    val chirpGate = sin(2.0 * PI * 3.5 * t).coerceAtLeast(0.0)
                    val birdFreq = 1850.0 + 450.0 * sin(2.0 * PI * 16.0 * t)
                    sin(2.0 * PI * birdFreq * t) * chirpGate * 0.65
                }
                502 -> { // Ocean Waves
                    val tide = (0.5 + 0.5 * sin(2.0 * PI * 0.35 * t)).pow(1.5)
                    brownState * tide * 0.9
                }
                503 -> { // Forest Breeze
                    val rustle = 0.4 + 0.6 * abs(sin(2.0 * PI * 0.6 * t))
                    (0.7 * brownState + 0.15 * whiteNoise) * rustle * 0.7
                }
                else -> { // Mountain Creek Stream
                    val ripple = 0.6 + 0.4 * sin(2.0 * PI * 7.0 * t)
                    (0.55 * brownState + 0.25 * whiteNoise) * ripple * 0.75
                }
            }

            val pcmShort = (rawSample.coerceIn(-1.0, 1.0) * 22000)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
            buffer.putShort(pcmShort)
        }

        writeWavContainer(file, sampleRate, pcmData)
    }

    fun renderProceduralPreviewBitmap(themeIndex: Int, progress: Float, width: Int = 320, height: Int = 180): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val (c1, c2, accent) = when (themeIndex % 3) {
            0 -> Triple(Color.rgb(9, 14, 34), Color.rgb(38, 12, 58), Color.rgb(0, 240, 255))
            1 -> Triple(Color.rgb(28, 18, 12), Color.rgb(68, 32, 16), Color.rgb(255, 184, 0))
            else -> Triple(Color.rgb(8, 52, 18), Color.rgb(12, 96, 32), Color.rgb(255, 42, 133))
        }

        paint.shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            c1, c2, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        paint.color = accent
        paint.alpha = 190
        val cx = width * (0.25f + 0.5f * progress)
        val cy = height * (0.5f + 0.15f * sin(progress * 2 * PI).toFloat())
        canvas.drawCircle(cx, cy, height * 0.22f, paint)

        paint.color = Color.WHITE
        paint.alpha = 90
        paint.strokeWidth = 3f
        canvas.drawLine(0f, height * 0.72f, width.toFloat(), height * 0.72f, paint)

        return bmp
    }

    private fun generateMp4Clip(
        outputFile: File,
        themeIndex: Int,
        width: Int,
        height: Int,
        fps: Int,
        durationSeconds: Int
    ) {
        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, 1_500_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType(mimeType)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val bufferInfo = MediaCodec.BufferInfo()
        var trackIndex = -1
        var muxerStarted = false

        val totalFrames = fps * durationSeconds
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 26f
            isFakeBoldText = true
        }

        try {
            for (frameIdx in 0 until totalFrames) {
                val progress = frameIdx.toFloat() / totalFrames.toFloat()
                val canvas = inputSurface.lockCanvas(null)
                try {
                    val (bgStart, bgEnd, orbColor) = when (themeIndex % 3) {
                        0 -> Triple(Color.rgb(8, 12, 28), Color.rgb(42, 11, 64), Color.rgb(0, 240, 255))
                    1 -> Triple(Color.rgb(20, 28, 44), Color.rgb(92, 45, 18), Color.rgb(255, 174, 0))
                        else -> Triple(Color.rgb(0, 165, 50), Color.rgb(0, 125, 38), Color.rgb(255, 42, 133))
                    }
                    paint.shader = LinearGradient(
                        0f, 0f, width.toFloat(), height.toFloat(),
                        bgStart, bgEnd, Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                    paint.shader = null

                    paint.color = Color.argb(70, 255, 255, 255)
                    paint.strokeWidth = 2f
                    val horizonY = height * 0.68f
                    canvas.drawLine(0f, horizonY, width.toFloat(), horizonY, paint)
                    for (g in -4..4) {
                        val xTop = width * 0.5f + g * 45f
                        val xBot = width * 0.5f + g * 140f + (progress * 80f) % 80f
                        canvas.drawLine(xTop, horizonY, xBot, height.toFloat(), paint)
                    }

                    paint.color = orbColor
                    val cx = width * (0.2f + 0.6f * progress)
                    val cy = height * (0.42f + 0.12f * sin(progress * 2 * PI).toFloat())
                    canvas.drawCircle(cx, cy, 46f, paint)

                    paint.color = Color.argb(180, 255, 255, 255)
                    val rect = RectF(cx - 70f, cy + 58f, cx + 70f, cy + 88f)
                    canvas.drawRoundRect(rect, 8f, 8f, paint)

                    val sceneLabel = when (themeIndex % 3) {
                        0 -> "NOVACUT SCENE 01 • NEON VELOCITY"
                        1 -> "NOVACUT SCENE 02 • GOLDEN ANAMORPHIC"
                        else -> "NOVACUT SCENE 03 • CHROMA COMPOSITE"
                    }
                    canvas.drawText(sceneLabel, 28f, 44f, textPaint)
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                drainEncoder(encoder, muxer, bufferInfo, endOfStream = false, trackIndexHolder = { trackIndex }) { newIdx, started ->
                    trackIndex = newIdx
                    muxerStarted = started
                }
            }

            encoder.signalEndOfInputStream()
            drainEncoder(encoder, muxer, bufferInfo, endOfStream = true, trackIndexHolder = { trackIndex }) { newIdx, started ->
                trackIndex = newIdx
                muxerStarted = started
            }
        } finally {
            runCatching { encoder.stop() }
            runCatching { encoder.release() }
            runCatching { inputSurface.release() }
            if (muxerStarted) {
                runCatching { muxer.stop() }
            }
            runCatching { muxer.release() }
        }
    }

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        trackIndexHolder: () -> Int,
        onStateUpdate: (Int, Boolean) -> Unit
    ) {
        var trackIdx = trackIndexHolder()
        var started = trackIdx >= 0
        while (true) {
            val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000L)
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = encoder.outputFormat
                trackIdx = muxer.addTrack(newFormat)
                muxer.start()
                started = true
                onStateUpdate(trackIdx, true)
            } else if (status >= 0) {
                val encodedData = encoder.getOutputBuffer(status)
                if (encodedData != null && bufferInfo.size > 0 && started) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        bufferInfo.presentationTimeUs = System.nanoTime() / 1000L
                        muxer.writeSampleData(trackIdx, encodedData, bufferInfo)
                    }
                }
                encoder.releaseOutputBuffer(status, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }

    private fun writeSynthesizedWav(file: File, durationSec: Int, presetIndex: Int) {
        val sampleRate = 22050
        val numSamples = sampleRate * durationSec
        val pcmData = ByteArray(numSamples * 2)
        val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

        val baseFreq = when (presetIndex % 6) {
            0 -> 110.0
            1 -> 220.0
            2 -> 164.8
            3 -> 320.0
            4 -> 65.0
            else -> 440.0
        }

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate.toDouble()
            val env = sin(PI * (t / durationSec)).coerceIn(0.0, 1.0)
            val beatGate = if (presetIndex == 0) (if (((t * 4.0).toInt() % 2) == 0) 1.0 else 0.45) else 1.0
            val freqMod = if (presetIndex >= 3) baseFreq * (1.0 + 0.8 * cos(t * 6.0)) else baseFreq
            val sampleVal = (
                0.55 * sin(2.0 * PI * freqMod * t) +
                    0.28 * sin(2.0 * PI * (freqMod * 1.5) * t) +
                    0.17 * cos(2.0 * PI * (freqMod * 2.0) * t)
                ) * env * beatGate
            val pcmShort = (sampleVal * 18000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            buffer.putShort(pcmShort)
        }

        writeWavContainer(file, sampleRate, pcmData)
    }

    private fun writeWavContainer(file: File, sampleRate: Int, pcmData: ByteArray) {
        FileOutputStream(file).use { fos ->
            val totalDataLen = pcmData.size + 36
            val byteRate = sampleRate * 2
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
                put("RIFF".toByteArray(Charsets.US_ASCII))
                putInt(totalDataLen)
                put("WAVE".toByteArray(Charsets.US_ASCII))
                put("fmt ".toByteArray(Charsets.US_ASCII))
                putInt(16)
                putShort(1)
                putShort(1)
                putInt(sampleRate)
                putInt(byteRate)
                putShort(2)
                putShort(16)
                put("data".toByteArray(Charsets.US_ASCII))
                putInt(pcmData.size)
            }
            fos.write(header.array())
            fos.write(pcmData)
        }
    }
}

class VoiceRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTimeMs: Long = 0L

    val isRecording: Boolean
        get() = recorder != null

    fun startRecording(): File? {
        stopRecording()
        val dir = File(context.filesDir, "voiceovers").apply { mkdirs() }
        val outFile = File(dir, "voiceover_${System.currentTimeMillis()}.m4a")
        return try {
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outFile.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            currentOutputFile = outFile
            startTimeMs = System.currentTimeMillis()
            outFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaRecorder for voiceover", e)
            null
        }
    }

    fun getNormalizedAmplitude(): Float {
        val maxAmp = runCatching { recorder?.maxAmplitude ?: 0 }.getOrDefault(0)
        return (maxAmp / 22000f).coerceIn(0.12f, 0.98f)
    }

    fun stopRecording(): Pair<File, Long>? {
        val activeRecorder = recorder ?: return null
        val file = currentOutputFile
        val elapsed = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(1000L)
        recorder = null
        currentOutputFile = null
        return try {
            activeRecorder.stop()
            activeRecorder.release()
            if (file != null && file.exists()) Pair(file, elapsed) else null
        } catch (e: Exception) {
            Log.w(TAG, "Voiceover stop warning: ${e.message}")
            runCatching { activeRecorder.release() }
            if (file != null && file.exists()) Pair(file, elapsed) else null
        }
    }
}
