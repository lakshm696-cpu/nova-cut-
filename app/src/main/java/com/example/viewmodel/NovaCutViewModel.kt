package com.example.viewmodel

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiTaskState
import com.example.ai.NovaCutAiService
import com.example.data.local.NovaCutDatabase
import com.example.data.local.ProjectEntity
import com.example.data.local.ProjectRepository
import com.example.media.ExportState
import com.example.media.ProceduralMediaGenerator
import com.example.media.StudioSampleAudio
import com.example.media.StudioSampleVideo
import com.example.media.VideoExportEngine
import com.example.media.VoiceRecorderManager
import android.content.Context
import com.example.model.AiCustomVideoEffect
import com.example.model.AiEnhancementSuggestion
import com.example.model.AiSceneCut
import com.example.model.AppScreen
import com.example.model.AspectRatioPreset
import com.example.model.AudioTrackItem
import com.example.model.BuiltInSoundEffectsCatalog
import com.example.model.EditorBottomTab
import com.example.model.EditorSnapshot
import com.example.model.EffectStorePack
import com.example.model.ExportBitratePreset
import com.example.model.ExportFps
import com.example.model.ExportHistoryRecord
import com.example.model.ExportResolution
import com.example.model.FilterPreset
import com.example.model.FontChoice
import com.example.model.SmartTemplate
import com.example.model.SoundEffectAsset
import com.example.model.StickerOverlayItem
import com.example.model.TextAnimationType
import com.example.model.TextOverlayItem
import com.example.model.TransitionType
import com.example.model.VideoClip
import com.example.model.VideoEffectItem
import com.example.model.VideoEffectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.abs
import kotlin.math.sin

private const val TAG = "NovaCutViewModel"

class NovaCutViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NovaCutDatabase.getInstance(application)
    private val repository = ProjectRepository(database.projectDao())
    private val aiService = NovaCutAiService()
    private val voiceRecorder = VoiceRecorderManager(application)

    val savedProjects: StateFlow<List<ProjectEntity>> = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Navigation & UI state
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val screenBackStack = ArrayDeque<AppScreen>()

    private val _activeBottomTab = MutableStateFlow(EditorBottomTab.TIMELINE)
    val activeBottomTab: StateFlow<EditorBottomTab> = _activeBottomTab.asStateFlow()

    // Studio Sample Assets
    private val _studioVideos = MutableStateFlow<List<StudioSampleVideo>>(emptyList())
    val studioVideos: StateFlow<List<StudioSampleVideo>> = _studioVideos.asStateFlow()

    private val _studioAudios = MutableStateFlow<List<StudioSampleAudio>>(emptyList())
    val studioAudios: StateFlow<List<StudioSampleAudio>> = _studioAudios.asStateFlow()

    // Current Project State
    private val _currentProjectId = MutableStateFlow(UUID.randomUUID().toString())
    val currentProjectId: StateFlow<String> = _currentProjectId.asStateFlow()

    private val _projectTitle = MutableStateFlow("Neon Cyber Edit")
    val projectTitle: StateFlow<String> = _projectTitle.asStateFlow()

    private val _aspectRatio = MutableStateFlow(AspectRatioPreset.RATIO_9_16)
    val aspectRatio: StateFlow<AspectRatioPreset> = _aspectRatio.asStateFlow()

    private val _exportResolution = MutableStateFlow(ExportResolution.RES_1080P)
    val exportResolution: StateFlow<ExportResolution> = _exportResolution.asStateFlow()

    private val _exportFps = MutableStateFlow(ExportFps.FPS_30)
    val exportFps: StateFlow<ExportFps> = _exportFps.asStateFlow()

    private val _exportBitratePreset = MutableStateFlow(ExportBitratePreset.HIGH)
    val exportBitratePreset: StateFlow<ExportBitratePreset> = _exportBitratePreset.asStateFlow()

    private val _exportHistory = MutableStateFlow<List<ExportHistoryRecord>>(emptyList())
    val exportHistory: StateFlow<List<ExportHistoryRecord>> = _exportHistory.asStateFlow()

    private val _videoClips = MutableStateFlow<List<VideoClip>>(emptyList())
    val videoClips: StateFlow<List<VideoClip>> = _videoClips.asStateFlow()

    private val _videoEffects = MutableStateFlow<List<VideoEffectItem>>(emptyList())
    val videoEffects: StateFlow<List<VideoEffectItem>> = _videoEffects.asStateFlow()

    private val _audioTracks = MutableStateFlow<List<AudioTrackItem>>(emptyList())
    val audioTracks: StateFlow<List<AudioTrackItem>> = _audioTracks.asStateFlow()

    private val _textOverlays = MutableStateFlow<List<TextOverlayItem>>(emptyList())
    val textOverlays: StateFlow<List<TextOverlayItem>> = _textOverlays.asStateFlow()

    private val _stickerOverlays = MutableStateFlow<List<StickerOverlayItem>>(emptyList())
    val stickerOverlayList: StateFlow<List<StickerOverlayItem>> = _stickerOverlays.asStateFlow()

    // Selection State
    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedEffectId = MutableStateFlow<String?>(null)
    val selectedEffectId: StateFlow<String?> = _selectedEffectId.asStateFlow()

    private val _selectedAudioId = MutableStateFlow<String?>(null)
    val selectedAudioId: StateFlow<String?> = _selectedAudioId.asStateFlow()

    private val _selectedTextId = MutableStateFlow<String?>(null)
    val selectedTextId: StateFlow<String?> = _selectedTextId.asStateFlow()

    private val _selectedStickerId = MutableStateFlow<String?>(null)
    val selectedStickerId: StateFlow<String?> = _selectedStickerId.asStateFlow()

    // Effects Store Marketplace State
    private val _downloadedPackIds = MutableStateFlow(setOf("pack_cyber_glitch_pro"))
    val downloadedPackIds: StateFlow<Set<String>> = _downloadedPackIds.asStateFlow()

    private val _packDownloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val packDownloadProgress: StateFlow<Map<String, Int>> = _packDownloadProgress.asStateFlow()

    private val _previewingSfxId = MutableStateFlow<String?>(null)
    val previewingSfxId: StateFlow<String?> = _previewingSfxId.asStateFlow()

    // Playback & Timeline State
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playheadMs = MutableStateFlow(0L)
    val playheadMs: StateFlow<Long> = _playheadMs.asStateFlow()

    private val _timelineZoom = MutableStateFlow(1.0f) // 0.5f to 3.0f
    val timelineZoom: StateFlow<Float> = _timelineZoom.asStateFlow()

    private val _magneticSnapEnabled = MutableStateFlow(true)
    val magneticSnapEnabled: StateFlow<Boolean> = _magneticSnapEnabled.asStateFlow()

    // Undo / Redo History
    private val undoStack = ArrayDeque<EditorSnapshot>()
    private val redoStack = ArrayDeque<EditorSnapshot>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Voiceover Recording State
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _voiceRecordingDurationMs = MutableStateFlow(0L)
    val voiceRecordingDurationMs: StateFlow<Long> = _voiceRecordingDurationMs.asStateFlow()

    private val _liveMicAmplitudes = MutableStateFlow<List<Float>>(emptyList())
    val liveMicAmplitudes: StateFlow<List<Float>> = _liveMicAmplitudes.asStateFlow()

    private var voiceTimerJob: Job? = null
    private var playbackTickerJob: Job? = null

    // AI Studio State & Custom AI Effects Generator
    private val _aiTaskState = MutableStateFlow<AiTaskState>(AiTaskState.Idle)
    val aiTaskState: StateFlow<AiTaskState> = _aiTaskState.asStateFlow()

    private val _detectedScenes = MutableStateFlow<List<AiSceneCut>>(emptyList())
    val detectedScenes: StateFlow<List<AiSceneCut>> = _detectedScenes.asStateFlow()

    private val _aiEnhancement = MutableStateFlow<AiEnhancementSuggestion?>(null)
    val aiEnhancement: StateFlow<AiEnhancementSuggestion?> = _aiEnhancement.asStateFlow()

    private val _customAiEffects = MutableStateFlow<List<AiCustomVideoEffect>>(
        listOf(
            AiCustomVideoEffect(
                userPrompt = "Golden anamorphic cinema lens flare with warm halation and subtle film grain",
                effectName = "24K Gold Anamorphic Bloom",
                tagline = "Warm golden halation with cinema flare & 35mm grain",
                baseEffectType = VideoEffectType.ANAMORPHIC_LENS,
                tintColorHex = 0xFFD6B46A,
                bloomIntensity = 0.86f,
                glitchIntensity = 0.0f,
                grainAmount = 0.22f,
                vignetteAmount = 0.48f,
                shakeIntensity = 0.0f,
                zoomPulse = 0.15f,
                scanlinesIntensity = 0.0f,
                chromaticShift = 0.25f
            )
        )
    )
    val customAiEffects: StateFlow<List<AiCustomVideoEffect>> = _customAiEffects.asStateFlow()

    // Export State
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // Status Toast Banner
    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    init {
        initializeStudioEnvironment()
    }

    private fun initializeStudioEnvironment() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val videos = ProceduralMediaGenerator.ensureStudioSampleVideos(context)
            val audios = ProceduralMediaGenerator.ensureStudioSampleAudio(context)
            _studioVideos.value = videos
            _studioAudios.value = audios

            if (repository.getProjectCount() == 0 && videos.isNotEmpty()) {
                loadDefaultShowcaseProject(videos, audios)
                saveCurrentProjectToRoom()
            } else if (_videoClips.value.isEmpty() && videos.isNotEmpty()) {
                loadDefaultShowcaseProject(videos, audios)
            }

            delay(1100L)
            if (_currentScreen.value == AppScreen.SPLASH) {
                _currentScreen.value = AppScreen.HOME_DASHBOARD
            }
        }
    }

    private fun loadDefaultShowcaseProject(
        videos: List<StudioSampleVideo>,
        audios: List<StudioSampleAudio>
    ) {
        var cursorMs = 0L
        val clips = videos.mapIndexed { idx, sample ->
            val clip = VideoClip(
                title = sample.title,
                uriString = sample.uriString,
                sourceDurationMs = sample.durationMs,
                trimStartMs = 0L,
                trimEndMs = sample.durationMs,
                timelineStartMs = cursorMs,
                trackIndex = 0,
                transitionIn = when (idx) {
                    0 -> TransitionType.NONE
                    1 -> TransitionType.GLITCH
                    else -> TransitionType.WHIP_PAN
                },
                transitionDurationMs = 450L,
                filterPreset = when (idx) {
                    0 -> FilterPreset.CYBERPUNK_NEON
                    1 -> FilterPreset.TEAL_ORANGE
                    else -> FilterPreset.ORIGINAL
                }
            )
            cursorMs += clip.effectiveDurationMs
            clip
        }

        val defaultEffects = listOf(
            VideoEffectItem(
                effectType = VideoEffectType.CINEMATIC_GLOW,
                startTimeMs = 0L,
                endTimeMs = 3800L,
                intensity = 0.75f
            ),
            VideoEffectItem(
                effectType = VideoEffectType.RGB_SPLIT,
                startTimeMs = 3800L,
                endTimeMs = 7600L,
                intensity = 0.82f
            )
        )

        val defaultAudio = audios.take(2).mapIndexed { idx, sample ->
            AudioTrackItem(
                title = sample.title,
                uriString = sample.uriString,
                durationMs = sample.durationMs,
                timelineStartMs = if (idx == 0) 0L else 3800L,
                trimStartMs = 0L,
                trimEndMs = sample.durationMs,
                volume = 0.85f,
                isSfx = sample.isSfx,
                sfxCategoryName = sample.category,
                waveformAmplitudes = sample.waveform
            )
        }

        val defaultTexts = listOf(
            TextOverlayItem(
                text = "NOVACUT STUDIO",
                startTimeMs = 200L,
                endTimeMs = 3400L,
                xNormalized = 0.5f,
                yNormalized = 0.16f,
                fontSizeSp = 24f,
                colorHex = 0xFFD6B46A,
                backgroundColorHex = 0xAA08090D,
                fontChoice = FontChoice.BEBAS_NEUE,
                animationType = TextAnimationType.NEON_FLICKER,
                isSubtitle = false
            ),
            TextOverlayItem(
                text = "Pro Effects & Sound Studio",
                startTimeMs = 3600L,
                endTimeMs = 7400L,
                xNormalized = 0.5f,
                yNormalized = 0.84f,
                fontSizeSp = 18f,
                colorHex = 0xFFFFFFFF,
                backgroundColorHex = 0xCC0E121B,
                fontChoice = FontChoice.SPACE_GROTESK,
                animationType = TextAnimationType.KARAOKE_HIGHLIGHT,
                isSubtitle = true
            )
        )

        val defaultStickers = listOf(
            StickerOverlayItem(
                label = "Cyber Bolt",
                symbol = "⚡",
                accentHex = 0xFF00F0FF,
                startTimeMs = 400L,
                endTimeMs = 4200L,
                xNormalized = 0.82f,
                yNormalized = 0.22f,
                scale = 1.15f
            )
        )

        _currentProjectId.value = UUID.randomUUID().toString()
        _projectTitle.value = "Neon Velocity Showcase"
        _aspectRatio.value = AspectRatioPreset.RATIO_9_16
        _videoClips.value = clips
        _videoEffects.value = defaultEffects
        _audioTracks.value = defaultAudio
        _textOverlays.value = defaultTexts
        _stickerOverlays.value = defaultStickers
        _selectedClipId.value = clips.firstOrNull()?.id
        _selectedEffectId.value = defaultEffects.firstOrNull()?.id
        _selectedAudioId.value = defaultAudio.firstOrNull()?.id
        _playheadMs.value = 0L
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoFlags()
    }

    // Navigation API
    fun navigateTo(screen: AppScreen) {
        val current = _currentScreen.value
        if (current != screen && current != AppScreen.SPLASH) {
            screenBackStack.addLast(current)
        }
        if (screen != AppScreen.VIDEO_EDITOR) {
            pausePlayback()
        }
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        pausePlayback()
        ProceduralMediaGenerator.stopAudioPreview()
        return if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeLast()
            true
        } else if (_currentScreen.value != AppScreen.HOME_DASHBOARD) {
            _currentScreen.value = AppScreen.HOME_DASHBOARD
            true
        } else {
            false
        }
    }

    fun selectBottomTab(tab: EditorBottomTab) {
        _activeBottomTab.value = tab
    }

    fun showStatus(message: String) {
        _statusBanner.value = message
        viewModelScope.launch {
            delay(3200L)
            if (_statusBanner.value == message) {
                _statusBanner.value = null
            }
        }
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    // Project Lifecycle
    fun startNewProject(
        title: String,
        ratio: AspectRatioPreset,
        resolution: ExportResolution,
        fps: ExportFps,
        includeStarterClips: Boolean = true
    ) {
        pausePlayback()
        _currentProjectId.value = UUID.randomUUID().toString()
        _projectTitle.value = title.ifBlank { "Untitled Cut" }
        _aspectRatio.value = ratio
        _exportResolution.value = resolution
        _exportFps.value = fps

        val starterClips = if (includeStarterClips) {
            var offset = 0L
            _studioVideos.value.take(2).map { s ->
                val clip = VideoClip(
                    title = s.title,
                    uriString = s.uriString,
                    sourceDurationMs = s.durationMs,
                    trimStartMs = 0L,
                    trimEndMs = s.durationMs,
                    timelineStartMs = offset
                )
                offset += clip.effectiveDurationMs
                clip
            }
        } else {
            emptyList()
        }

        _videoClips.value = starterClips
        _videoEffects.value = emptyList()
        _audioTracks.value = emptyList()
        _textOverlays.value = emptyList()
        _stickerOverlays.value = emptyList()
        _selectedClipId.value = starterClips.firstOrNull()?.id
        _selectedEffectId.value = null
        _playheadMs.value = 0L
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoFlags()
        saveCurrentProjectToRoom()
        navigateTo(AppScreen.VIDEO_EDITOR)
        showStatus("Created project '${_projectTitle.value}' (${ratio.label})")
    }

    fun applySmartTemplate(template: SmartTemplate) {
        pausePlayback()
        _currentProjectId.value = UUID.randomUUID().toString()
        _projectTitle.value = template.title
        _aspectRatio.value = template.aspectRatio
        _exportResolution.value = ExportResolution.RES_1080P
        _exportFps.value = ExportFps.FPS_60

        val samples = _studioVideos.value
        var offset = 0L
        val clips = samples.mapIndexed { idx, sample ->
            val clip = VideoClip(
                title = "${template.title} #${idx + 1}",
                uriString = sample.uriString,
                sourceDurationMs = sample.durationMs,
                trimStartMs = 0L,
                trimEndMs = sample.durationMs,
                timelineStartMs = offset,
                transitionIn = if (idx == 0) TransitionType.NONE else template.transitionType,
                transitionDurationMs = template.transitionType.defaultDurationMs,
                filterPreset = template.filterPreset,
                contrast = 1.12f,
                saturation = 1.18f
            )
            offset += clip.effectiveDurationMs
            clip
        }

        val templateFx = listOf(
            VideoEffectItem(
                effectType = if (template.filterPreset == FilterPreset.CYBERPUNK_NEON) {
                    VideoEffectType.GLITCH_EFFECT
                } else {
                    VideoEffectType.ANAMORPHIC_LENS
                },
                startTimeMs = 0L,
                endTimeMs = 4500L,
                intensity = 0.8f
            )
        )

        val matchedAudio = _studioAudios.value.firstOrNull {
            it.title.contains(template.musicPresetName.take(8), ignoreCase = true)
        } ?: _studioAudios.value.firstOrNull()

        val audios = if (matchedAudio != null) {
            listOf(
                AudioTrackItem(
                    title = matchedAudio.title,
                    uriString = matchedAudio.uriString,
                    durationMs = matchedAudio.durationMs,
                    timelineStartMs = 0L,
                    trimStartMs = 0L,
                    trimEndMs = matchedAudio.durationMs,
                    volume = 0.9f,
                    waveformAmplitudes = matchedAudio.waveform
                )
            )
        } else emptyList()

        val texts = template.defaultTitles.mapIndexed { i, titleText ->
            val start = i * 3200L
            TextOverlayItem(
                text = titleText,
                startTimeMs = start,
                endTimeMs = start + 2800L,
                xNormalized = 0.5f,
                yNormalized = if (i == 0) 0.22f else 0.82f,
                fontSizeSp = if (i == 0) 25f else 20f,
                colorHex = if (i % 2 == 0) 0xFF00F0FF else 0xFFFF2A85,
                backgroundColorHex = 0xCC07090E,
                fontChoice = FontChoice.BEBAS_NEUE,
                animationType = TextAnimationType.NEON_FLICKER,
                isSubtitle = i > 0
            )
        }

        _videoClips.value = clips
        _videoEffects.value = templateFx
        _audioTracks.value = audios
        _textOverlays.value = texts
        _stickerOverlays.value = emptyList()
        _selectedClipId.value = clips.firstOrNull()?.id
        _selectedEffectId.value = templateFx.firstOrNull()?.id
        _playheadMs.value = 0L
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoFlags()
        saveCurrentProjectToRoom()
        navigateTo(AppScreen.VIDEO_EDITOR)
        showStatus("Applied Smart Template: ${template.title}")
    }

    fun openSavedProject(entity: ProjectEntity) {
        pausePlayback()
        val decoded = repository.decodePayload(entity)
        _currentProjectId.value = entity.id
        _projectTitle.value = entity.title
        _aspectRatio.value = decoded.aspectRatio
        _exportResolution.value = decoded.exportResolution
        _exportFps.value = decoded.exportFps
        _videoClips.value = decoded.videoClips
        _videoEffects.value = decoded.videoEffects
        _audioTracks.value = decoded.audioTracks
        _textOverlays.value = decoded.textOverlays
        _stickerOverlays.value = decoded.stickerOverlays
        _selectedClipId.value = decoded.videoClips.firstOrNull()?.id
        _selectedEffectId.value = decoded.videoEffects.firstOrNull()?.id
        _playheadMs.value = 0L
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoFlags()
        navigateTo(AppScreen.VIDEO_EDITOR)
        showStatus("Opened '${entity.title}'")
    }

    fun duplicateSavedProject(entity: ProjectEntity) {
        viewModelScope.launch {
            val decoded = repository.decodePayload(entity)
            val newId = UUID.randomUUID().toString()
            repository.saveProjectState(
                id = newId,
                title = "${entity.title} (Copy)",
                aspectRatio = decoded.aspectRatio,
                exportResolution = decoded.exportResolution,
                exportFps = decoded.exportFps,
                videoClips = decoded.videoClips,
                videoEffects = decoded.videoEffects,
                audioTracks = decoded.audioTracks,
                textOverlays = decoded.textOverlays,
                stickerOverlays = decoded.stickerOverlays,
                lastExportedUri = entity.lastExportedUri
            )
            showStatus("Duplicated '${entity.title}'")
        }
    }

    fun deleteSavedProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            showStatus("Project removed from local storage")
        }
    }

    fun updateProjectTitle(newTitle: String) {
        _projectTitle.value = newTitle
        saveCurrentProjectToRoom()
    }

    fun saveCurrentProjectToRoom(lastExportedUri: String? = null) {
        viewModelScope.launch {
            repository.saveProjectState(
                id = _currentProjectId.value,
                title = _projectTitle.value,
                aspectRatio = _aspectRatio.value,
                exportResolution = _exportResolution.value,
                exportFps = _exportFps.value,
                videoClips = _videoClips.value,
                videoEffects = _videoEffects.value,
                audioTracks = _audioTracks.value,
                textOverlays = _textOverlays.value,
                stickerOverlays = _stickerOverlays.value,
                lastExportedUri = lastExportedUri
            )
        }
    }

    // Undo / Redo Snapshot Management
    private fun recordSnapshot(description: String) {
        val snap = EditorSnapshot(
            description = description,
            videoClips = _videoClips.value,
            videoEffects = _videoEffects.value,
            audioTracks = _audioTracks.value,
            textOverlays = _textOverlays.value,
            stickerOverlays = _stickerOverlays.value,
            aspectRatio = _aspectRatio.value
        )
        undoStack.addLast(snap)
        if (undoStack.size > 30) {
            undoStack.removeFirst()
        }
        redoStack.clear()
        updateUndoRedoFlags()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentSnap = EditorSnapshot(
            description = "Current",
            videoClips = _videoClips.value,
            videoEffects = _videoEffects.value,
            audioTracks = _audioTracks.value,
            textOverlays = _textOverlays.value,
            stickerOverlays = _stickerOverlays.value,
            aspectRatio = _aspectRatio.value
        )
        redoStack.addLast(currentSnap)
        val prev = undoStack.removeLast()
        restoreSnapshot(prev)
        updateUndoRedoFlags()
        showStatus("Undo: ${prev.description}")
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentSnap = EditorSnapshot(
            description = "Current",
            videoClips = _videoClips.value,
            videoEffects = _videoEffects.value,
            audioTracks = _audioTracks.value,
            textOverlays = _textOverlays.value,
            stickerOverlays = _stickerOverlays.value,
            aspectRatio = _aspectRatio.value
        )
        undoStack.addLast(currentSnap)
        val next = redoStack.removeLast()
        restoreSnapshot(next)
        updateUndoRedoFlags()
        showStatus("Redo applied")
    }

    private fun restoreSnapshot(snap: EditorSnapshot) {
        _videoClips.value = snap.videoClips
        _videoEffects.value = snap.videoEffects
        _audioTracks.value = snap.audioTracks
        _textOverlays.value = snap.textOverlays
        _stickerOverlays.value = snap.stickerOverlays
        _aspectRatio.value = snap.aspectRatio
        saveCurrentProjectToRoom()
    }

    private fun updateUndoRedoFlags() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    // Timeline & Playback Controls
    val totalTimelineDurationMs: Long
        get() {
            val mainEnd = _videoClips.value.filter { it.trackIndex == 0 }.sumOf { it.effectiveDurationMs }
            val pipEnd = _videoClips.value.filter { it.trackIndex == 1 }.maxOfOrNull { it.timelineEndMs } ?: 0L
            val fxEnd = _videoEffects.value.maxOfOrNull { it.endTimeMs } ?: 0L
            val textEnd = _textOverlays.value.maxOfOrNull { it.endTimeMs } ?: 0L
            val audioEnd = _audioTracks.value.maxOfOrNull { it.timelineEndMs } ?: 0L
            return maxOf(mainEnd, pipEnd, fxEnd, textEnd, audioEnd, 1000L)
        }

    fun togglePlayback() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        if (_isPlaying.value) return
        val maxDur = totalTimelineDurationMs
        if (_playheadMs.value >= maxDur - 50L) {
            _playheadMs.value = 0L
        }
        _isPlaying.value = true
        playbackTickerJob?.cancel()
        playbackTickerJob = viewModelScope.launch {
            var lastTick = System.currentTimeMillis()
            while (isActive && _isPlaying.value) {
                delay(33L)
                val now = System.currentTimeMillis()
                val delta = (now - lastTick).coerceAtLeast(16L)
                lastTick = now
                val nextPos = _playheadMs.value + delta
                val total = totalTimelineDurationMs
                if (nextPos >= total) {
                    _playheadMs.value = total
                    _isPlaying.value = false
                    break
                } else {
                    _playheadMs.value = nextPos
                }
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackTickerJob?.cancel()
        playbackTickerJob = null
    }

    fun seekTo(positionMs: Long) {
        val total = totalTimelineDurationMs
        val clamped = positionMs.coerceIn(0L, total)
        val snapped = if (_magneticSnapEnabled.value) applyMagneticSnap(clamped) else clamped
        _playheadMs.value = snapped
    }

    fun stepFrame(forward: Boolean) {
        pausePlayback()
        val frameDurationMs = (1000L / _exportFps.value.fps).coerceAtLeast(16L)
        val next = if (forward) _playheadMs.value + frameDurationMs else _playheadMs.value - frameDurationMs
        _playheadMs.value = next.coerceIn(0L, totalTimelineDurationMs)
    }

    private fun applyMagneticSnap(targetMs: Long): Long {
        val snapThresholdMs = 90L
        val snapPoints = mutableListOf(0L, totalTimelineDurationMs)
        _videoClips.value.forEach {
            snapPoints.add(it.timelineStartMs)
            snapPoints.add(it.timelineEndMs)
        }
        _videoEffects.value.forEach {
            snapPoints.add(it.startTimeMs)
            snapPoints.add(it.endTimeMs)
        }
        _textOverlays.value.forEach {
            snapPoints.add(it.startTimeMs)
            snapPoints.add(it.endTimeMs)
        }
        _audioTracks.value.forEach {
            snapPoints.add(it.timelineStartMs)
            snapPoints.add(it.timelineEndMs)
        }
        val closest = snapPoints.minByOrNull { abs(it - targetMs) } ?: return targetMs
        return if (abs(closest - targetMs) <= snapThresholdMs) closest else targetMs
    }

    fun setTimelineZoom(zoom: Float) {
        _timelineZoom.value = zoom.coerceIn(0.5f, 3.0f)
    }

    fun toggleMagneticSnap() {
        _magneticSnapEnabled.value = !_magneticSnapEnabled.value
        showStatus(
            if (_magneticSnapEnabled.value) "Magnetic Timeline Snapping ON"
            else "Magnetic Timeline Snapping OFF"
        )
    }

    private fun recalculateMainTrackPositions(clips: List<VideoClip>): List<VideoClip> {
        var mainCursor = 0L
        return clips.map { clip ->
            if (clip.trackIndex == 0) {
                val updated = clip.copy(timelineStartMs = mainCursor)
                mainCursor += updated.effectiveDurationMs
                updated
            } else {
                clip
            }
        }
    }

    // Importing Media from Gallery or Studio Stock
    fun importVideosFromUris(uris: List<Uri>, asPipOverlay: Boolean = false) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            recordSnapshot("Import ${uris.size} video clip(s)")
            val context = getApplication<Application>()
            val newClips = withContext(Dispatchers.IO) {
                uris.mapIndexed { idx, uri ->
                    runCatching {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    val duration = queryMediaDurationMs(context, uri).coerceAtLeast(2000L)
                    val title = uri.lastPathSegment?.substringAfterLast('/')?.take(22) ?: "Gallery Clip ${idx + 1}"
                    VideoClip(
                        title = title,
                        uriString = uri.toString(),
                        sourceDurationMs = duration,
                        trimStartMs = 0L,
                        trimEndMs = duration,
                        timelineStartMs = _playheadMs.value,
                        trackIndex = if (asPipOverlay) 1 else 0
                    )
                }
            }

            val combined = recalculateMainTrackPositions(_videoClips.value + newClips)
            _videoClips.value = combined
            _selectedClipId.value = newClips.firstOrNull()?.id
            saveCurrentProjectToRoom()
            showStatus("Imported ${newClips.size} video clip(s) to timeline")
        }
    }

    fun addStudioSampleClip(sample: StudioSampleVideo, asPipOverlay: Boolean = false) {
        recordSnapshot("Add clip ${sample.title}")
        val newClip = VideoClip(
            title = sample.title,
            uriString = sample.uriString,
            sourceDurationMs = sample.durationMs,
            trimStartMs = 0L,
            trimEndMs = sample.durationMs,
            timelineStartMs = if (asPipOverlay) _playheadMs.value else 0L,
            trackIndex = if (asPipOverlay) 1 else 0,
            chromaKeyEnabled = sample.themeIndex == 2
        )
        val updated = recalculateMainTrackPositions(_videoClips.value + newClip)
        _videoClips.value = updated
        _selectedClipId.value = newClip.id
        saveCurrentProjectToRoom()
        showStatus("Added '${sample.title}' to ${if (asPipOverlay) "PIP Track" else "Main Track"}")
    }

    private fun queryMediaDurationMs(context: Application, uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 4000L
        } catch (e: Exception) {
            4000L
        } finally {
            runCatching { retriever.release() }
        }
    }

    // Core Video Editing Actions (Stage 1 & Stage 2)
    fun selectClip(clipId: String?) {
        _selectedClipId.value = clipId
    }

    fun getSelectedClip(): VideoClip? {
        val id = _selectedClipId.value
        return _videoClips.value.firstOrNull { it.id == id } ?: _videoClips.value.firstOrNull()
    }

    fun getActiveClipAtPlayhead(): VideoClip? {
        val pos = _playheadMs.value
        return _videoClips.value
            .filter { it.trackIndex == 0 }
            .firstOrNull { pos >= it.timelineStartMs && pos <= it.timelineEndMs }
            ?: getSelectedClip()
    }

    fun getActivePipClipAtPlayhead(): VideoClip? {
        val pos = _playheadMs.value
        return _videoClips.value
            .filter { it.trackIndex == 1 }
            .firstOrNull { pos >= it.timelineStartMs && pos <= it.timelineEndMs }
    }

    /**
     * Returns all stacked VideoEffectItem layers active at the current playhead timestamp.
     */
    fun getActiveEffectsAtPlayhead(): List<VideoEffectItem> {
        val pos = _playheadMs.value
        val activeClipId = getActiveClipAtPlayhead()?.id
        return _videoEffects.value.filter { fx ->
            pos in fx.startTimeMs..fx.endTimeMs &&
                (fx.targetClipId == null || fx.targetClipId == activeClipId)
        }
    }

    fun updateClipTrim(clipId: String, newTrimStartMs: Long, newTrimEndMs: Long) {
        val target = _videoClips.value.firstOrNull { it.id == clipId } ?: return
        val safeStart = newTrimStartMs.coerceIn(0L, (target.sourceDurationMs - 400L).coerceAtLeast(0L))
        val safeEnd = newTrimEndMs.coerceIn(safeStart + 400L, target.sourceDurationMs)
        recordSnapshot("Trim ${target.title}")
        val updated = _videoClips.value.map {
            if (it.id == clipId) it.copy(trimStartMs = safeStart, trimEndMs = safeEnd) else it
        }
        _videoClips.value = recalculateMainTrackPositions(updated)
        saveCurrentProjectToRoom()
    }

    fun splitSelectedClipAtPlayhead() {
        val pos = _playheadMs.value
        val clip = _videoClips.value.firstOrNull {
            pos > it.timelineStartMs + 250L && pos < it.timelineEndMs - 250L
        } ?: getSelectedClip()

        if (clip == null) {
            showStatus("Select a clip and place the playhead inside it to split")
            return
        }

        val offsetInClipTimeline = if (pos > clip.timelineStartMs + 250L && pos < clip.timelineEndMs - 250L) {
            pos - clip.timelineStartMs
        } else {
            clip.effectiveDurationMs / 2L
        }

        val sourceSplitPoint = (clip.trimStartMs + (offsetInClipTimeline * clip.playbackSpeed).toLong())
            .coerceIn(clip.trimStartMs + 200L, clip.trimEndMs - 200L)

        if (sourceSplitPoint <= clip.trimStartMs + 100L || sourceSplitPoint >= clip.trimEndMs - 100L) {
            showStatus("Clip is too short to split further")
            return
        }

        recordSnapshot("Split ${clip.title}")
        val leftPart = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} (A)",
            trimEndMs = sourceSplitPoint
        )
        val rightPart = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} (B)",
            trimStartMs = sourceSplitPoint,
            timelineStartMs = clip.timelineStartMs + leftPart.effectiveDurationMs
        )

        val result = mutableListOf<VideoClip>()
        _videoClips.value.forEach { item ->
            if (item.id == clip.id) {
                result.add(leftPart)
                result.add(rightPart)
            } else {
                result.add(item)
            }
        }
        _videoClips.value = recalculateMainTrackPositions(result)
        _selectedClipId.value = rightPart.id
        saveCurrentProjectToRoom()
        showStatus("Split '${clip.title}' into 2 clips")
    }

    fun mergeSelectedClipWithNext() {
        val currentId = _selectedClipId.value ?: return
        val mainClips = _videoClips.value.filter { it.trackIndex == 0 }
        val idx = mainClips.indexOfFirst { it.id == currentId }
        if (idx < 0 || idx >= mainClips.size - 1) {
            showStatus("Select a clip that has an adjacent clip after it to merge")
            return
        }
        val first = mainClips[idx]
        val second = mainClips[idx + 1]
        recordSnapshot("Merge ${first.title} + ${second.title}")

        val mergedDuration = first.sourceDurationMs + second.sourceDurationMs
        val mergedClip = first.copy(
            id = UUID.randomUUID().toString(),
            title = "${first.title.substringBefore(" (")} + Merged",
            sourceDurationMs = mergedDuration,
            trimStartMs = 0L,
            trimEndMs = first.effectiveDurationMs + second.effectiveDurationMs
        )

        val updated = mutableListOf<VideoClip>()
        var skipId: String? = null
        for (c in _videoClips.value) {
            if (c.id == skipId) continue
            if (c.id == first.id) {
                updated.add(mergedClip)
                skipId = second.id
            } else {
                updated.add(c)
            }
        }
        _videoClips.value = recalculateMainTrackPositions(updated)
        _selectedClipId.value = mergedClip.id
        saveCurrentProjectToRoom()
        showStatus("Merged 2 adjacent clips")
    }

    fun duplicateSelectedClip() {
        val clip = getSelectedClip() ?: return
        recordSnapshot("Duplicate ${clip.title}")
        val copy = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} Copy",
            timelineStartMs = clip.timelineEndMs
        )
        val list = mutableListOf<VideoClip>()
        _videoClips.value.forEach {
            list.add(it)
            if (it.id == clip.id) list.add(copy)
        }
        _videoClips.value = recalculateMainTrackPositions(list)
        _selectedClipId.value = copy.id
        saveCurrentProjectToRoom()
        showStatus("Duplicated '${clip.title}'")
    }

    fun deleteSelectedClip() {
        val clip = getSelectedClip() ?: return
        if (_videoClips.value.size <= 1) {
            showStatus("Timeline requires at least one primary clip")
            return
        }
        recordSnapshot("Delete ${clip.title}")
        val remaining = _videoClips.value.filterNot { it.id == clip.id }
        _videoClips.value = recalculateMainTrackPositions(remaining)
        _selectedClipId.value = _videoClips.value.firstOrNull()?.id
        saveCurrentProjectToRoom()
        showStatus("Deleted '${clip.title}'")
    }

    fun moveSelectedClipOrder(moveRight: Boolean) {
        val clip = getSelectedClip() ?: return
        val list = _videoClips.value.toMutableList()
        val idx = list.indexOfFirst { it.id == clip.id }
        if (idx < 0) return
        val targetIdx = if (moveRight) idx + 1 else idx - 1
        if (targetIdx !in list.indices) return
        recordSnapshot("Reorder ${clip.title}")
        val removed = list.removeAt(idx)
        list.add(targetIdx, removed)
        _videoClips.value = recalculateMainTrackPositions(list)
        saveCurrentProjectToRoom()
    }

    fun toggleSelectedClipTrack() {
        val clip = getSelectedClip() ?: return
        if (clip.trackIndex == 0 && _videoClips.value.count { it.trackIndex == 0 } <= 1) {
            showStatus("Keep at least one clip on the Main Video Track")
            return
        }
        recordSnapshot("Move ${clip.title} track")
        val newTrack = if (clip.trackIndex == 0) 1 else 0
        val updated = _videoClips.value.map {
            if (it.id == clip.id) it.copy(trackIndex = newTrack, timelineStartMs = _playheadMs.value) else it
        }
        _videoClips.value = recalculateMainTrackPositions(updated)
        saveCurrentProjectToRoom()
        showStatus("Moved '${clip.title}' to ${if (newTrack == 1) "PIP Overlay Track" else "Main Video Track"}")
    }

    fun rotateSelectedClip() {
        val clip = getSelectedClip() ?: return
        recordSnapshot("Rotate ${clip.title}")
        val nextRot = (clip.rotationDegrees + 90f) % 360f
        _videoClips.value = _videoClips.value.map {
            if (it.id == clip.id) it.copy(rotationDegrees = nextRot) else it
        }
        saveCurrentProjectToRoom()
        showStatus("Rotated '${clip.title}' to ${nextRot.toInt()}°")
    }

    fun updateSelectedClipCropScale(scale: Float) {
        val clip = getSelectedClip() ?: return
        _videoClips.value = _videoClips.value.map {
            if (it.id == clip.id) it.copy(cropScale = scale.coerceIn(0.5f, 2.5f)) else it
        }
        saveCurrentProjectToRoom()
    }

    fun setAspectRatio(preset: AspectRatioPreset) {
        recordSnapshot("Change aspect ratio to ${preset.label}")
        _aspectRatio.value = preset
        saveCurrentProjectToRoom()
        showStatus("Canvas Aspect Ratio set to ${preset.label}")
    }

    fun updateSelectedClipSpeed(speed: Float) {
        val clip = getSelectedClip() ?: return
        recordSnapshot("Change speed of ${clip.title} to ${speed}x")
        val updated = _videoClips.value.map {
            if (it.id == clip.id) it.copy(playbackSpeed = speed.coerceIn(0.25f, 4.0f)) else it
        }
        _videoClips.value = recalculateMainTrackPositions(updated)
        saveCurrentProjectToRoom()
    }

    fun toggleReverseSelectedClip() {
        val clip = getSelectedClip() ?: return
        recordSnapshot("Reverse ${clip.title}")
        val newReversed = !clip.isReversed
        _videoClips.value = _videoClips.value.map {
            if (it.id == clip.id) it.copy(isReversed = newReversed) else it
        }
        saveCurrentProjectToRoom()
        showStatus(if (newReversed) "Reversed playback enabled for '${clip.title}'" else "Normal forward playback restored")
    }

    fun updateSelectedClipVolume(volume: Float, isMuted: Boolean = volume <= 0.01f) {
        val clip = getSelectedClip() ?: return
        _videoClips.value = _videoClips.value.map {
            if (it.id == clip.id) it.copy(volume = volume.coerceIn(0f, 1.5f), isMuted = isMuted) else it
        }
        saveCurrentProjectToRoom()
    }

    // Advanced Video Transitions (12 Transitions + Duration + Real-Time Preview)
    fun updateSelectedClipTransition(
        transition: TransitionType,
        durationMs: Long = transition.defaultDurationMs,
        applyToAllClips: Boolean = false
    ) {
        val clip = getSelectedClip() ?: return
        val safeDur = durationMs.coerceIn(150L, 2000L)
        recordSnapshot("Set transition ${transition.label}")
        _videoClips.value = _videoClips.value.map {
            if (applyToAllClips || it.id == clip.id) {
                it.copy(transitionIn = transition, transitionDurationMs = safeDur)
            } else it
        }
        saveCurrentProjectToRoom()
        showStatus("Applied '${transition.label}' (${safeDur}ms) ${if (applyToAllClips) "to all clips" else ""}")
    }

    fun previewSelectedTransition() {
        val clip = getSelectedClip() ?: return
        seekTo(clip.timelineStartMs)
        startPlayback()
        showStatus("Previewing '${clip.transitionIn.label}' (${clip.transitionDurationMs}ms)")
    }

    // Advanced Video Effects Studio & Effects Timeline (Stack, Drag, Duration, Intensity, Replace, Delete)
    fun selectVideoEffect(effectId: String?) {
        _selectedEffectId.value = effectId
    }

    fun getSelectedVideoEffect(): VideoEffectItem? {
        val id = _selectedEffectId.value
        return _videoEffects.value.firstOrNull { it.id == id } ?: _videoEffects.value.lastOrNull()
    }

    fun addVideoEffectToTimeline(
        effectType: VideoEffectType,
        intensity: Float = 0.8f,
        durationMs: Long = 3000L,
        bindToSelectedClip: Boolean = false
    ) {
        recordSnapshot("Add FX ${effectType.label}")
        val selectedClip = getSelectedClip()
        val start = if (bindToSelectedClip && selectedClip != null) {
            selectedClip.timelineStartMs
        } else {
            _playheadMs.value
        }
        val end = if (bindToSelectedClip && selectedClip != null) {
            selectedClip.timelineEndMs
        } else {
            (start + durationMs).coerceAtMost(totalTimelineDurationMs.coerceAtLeast(3000L))
        }

        val fx = VideoEffectItem(
            effectType = effectType,
            startTimeMs = start,
            endTimeMs = end.coerceAtLeast(start + 400L),
            intensity = intensity.coerceIn(0.1f, 1.0f),
            targetClipId = if (bindToSelectedClip) selectedClip?.id else null
        )

        // If the effect is Slow Motion or Speed Ramp and bound to a clip, also adjust clip speed
        if (effectType == VideoEffectType.SLOW_MOTION && selectedClip != null) {
            updateSelectedClipSpeed(0.5f)
        } else if (effectType == VideoEffectType.SPEED_RAMP && selectedClip != null) {
            updateSelectedClipSpeed(1.5f)
        }

        _videoEffects.value = _videoEffects.value + fx
        _selectedEffectId.value = fx.id
        saveCurrentProjectToRoom()
        showStatus("Stacked '${effectType.label}' on Effects Timeline (${(_videoEffects.value.size)} active)")
    }

    fun replaceSelectedVideoEffect(newEffectType: VideoEffectType) {
        val currentFx = getSelectedVideoEffect()
        if (currentFx == null) {
            addVideoEffectToTimeline(newEffectType)
            return
        }
        recordSnapshot("Replace FX with ${newEffectType.label}")
        _videoEffects.value = _videoEffects.value.map {
            if (it.id == currentFx.id) it.copy(effectType = newEffectType) else it
        }
        saveCurrentProjectToRoom()
        showStatus("Replaced effect with '${newEffectType.label}'")
    }

    fun updateVideoEffectParameters(
        effectId: String,
        startTimeMs: Long? = null,
        endTimeMs: Long? = null,
        intensity: Float? = null
    ) {
        _videoEffects.value = _videoEffects.value.map { fx ->
            if (fx.id == effectId) {
                val newStart = (startTimeMs ?: fx.startTimeMs).coerceIn(0L, (totalTimelineDurationMs - 300L).coerceAtLeast(0L))
                val newEnd = (endTimeMs ?: fx.endTimeMs).coerceIn(newStart + 300L, totalTimelineDurationMs.coerceAtLeast(newStart + 500L))
                fx.copy(
                    startTimeMs = newStart,
                    endTimeMs = newEnd,
                    intensity = (intensity ?: fx.intensity).coerceIn(0.1f, 1.0f)
                )
            } else fx
        }
        saveCurrentProjectToRoom()
    }

    fun deleteVideoEffect(effectId: String) {
        recordSnapshot("Remove Video Effect")
        _videoEffects.value = _videoEffects.value.filterNot { it.id == effectId }
        if (_selectedEffectId.value == effectId) {
            _selectedEffectId.value = _videoEffects.value.lastOrNull()?.id
        }
        saveCurrentProjectToRoom()
        showStatus("Removed effect from Effects Timeline")
    }

    // Color Filters, Adjustments & Chroma Key (Stage 3)
    fun updateSelectedClipFilter(filter: FilterPreset, applyToAll: Boolean = false) {
        val clip = getSelectedClip() ?: return
        recordSnapshot("Apply filter ${filter.label}")
        _videoClips.value = _videoClips.value.map {
            if (applyToAll || it.id == clip.id) it.copy(filterPreset = filter) else it
        }
        saveCurrentProjectToRoom()
        showStatus("Applied '${filter.label}' LUT ${if (applyToAll) "to all clips" else ""}")
    }

    fun updateSelectedClipColorAdjustments(
        brightness: Float? = null,
        contrast: Float? = null,
        saturation: Float? = null,
        vignette: Float? = null
    ) {
        val clip = getSelectedClip() ?: return
        _videoClips.value = _videoClips.value.map {
            if (it.id == clip.id) {
                it.copy(
                    brightness = brightness ?: it.brightness,
                    contrast = contrast ?: it.contrast,
                    saturation = saturation ?: it.saturation,
                    vignette = vignette ?: it.vignette
                )
            } else it
        }
        saveCurrentProjectToRoom()
    }

    fun updateSelectedClipChromaKey(
        enabled: Boolean,
        keyColorHex: Long? = null,
        tolerance: Float? = null
    ) {
        val clip = getSelectedClip() ?: return
        recordSnapshot("Update Chroma Key on ${clip.title}")
        _videoClips.value = _videoClips.value.map {
            if (it.id == clip.id) {
                it.copy(
                    chromaKeyEnabled = enabled,
                    chromaKeyColorHex = keyColorHex ?: it.chromaKeyColorHex,
                    chromaKeyTolerance = tolerance ?: it.chromaKeyTolerance
                )
            } else it
        }
        saveCurrentProjectToRoom()
    }

    // Searchable Sound Effects Library & Pro Audio Controls
    fun previewSoundEffectAsset(asset: SoundEffectAsset) {
        viewModelScope.launch {
            _previewingSfxId.value = asset.id
            ProceduralMediaGenerator.playSoundEffectPreview(
                context = getApplication(),
                asset = asset
            )
            delay(asset.durationMs.coerceIn(500L, 2500L))
            if (_previewingSfxId.value == asset.id) {
                _previewingSfxId.value = null
            }
        }
    }

    fun addSoundEffectAssetToTimeline(asset: SoundEffectAsset) {
        viewModelScope.launch {
            recordSnapshot("Add SFX ${asset.title}")
            val context = getApplication<Application>()
            val wavFile = ProceduralMediaGenerator.ensureSoundEffectFile(context, asset)
            val uriStr = Uri.fromFile(wavFile).toString()

            // Play instant audition feedback
            ProceduralMediaGenerator.playAudioFileUri(context, uriStr)

            val waveform = (0 until 20).map { step ->
                val phase = (step + asset.synthRecipeId) * 0.6f
                (0.22f + 0.72f * abs(sin(phase))).coerceIn(0.18f, 0.98f)
            }
            val beats = (400L until asset.durationMs step 500L).toList()

            val sfxTrack = AudioTrackItem(
                title = asset.title,
                uriString = uriStr,
                durationMs = asset.durationMs,
                timelineStartMs = _playheadMs.value,
                trimStartMs = 0L,
                trimEndMs = asset.durationMs,
                volume = 0.95f,
                fadeInMs = 80L,
                fadeOutMs = 150L,
                isSfx = true,
                sfxCategoryName = asset.category.label,
                beatMarkersMs = beats,
                waveformAmplitudes = waveform
            )
            _audioTracks.value = _audioTracks.value + sfxTrack
            _selectedAudioId.value = sfxTrack.id
            saveCurrentProjectToRoom()
            showStatus("Added '${asset.title}' (${asset.category.label}) to SFX Timeline")
        }
    }

    /**
     * Automatically synchronizes transition Sound Effects (Whoosh, Glitch, Impact, Flash)
     * with every video clip transition boundary on the timeline!
     */
    fun syncSoundEffectsToTransitions() {
        val transitionClips = _videoClips.value.filter { it.trackIndex == 0 && it.timelineStartMs > 0L }
        if (transitionClips.isEmpty()) {
            showStatus("Add or split at least 2 video clips to sync transition SFX")
            return
        }
        viewModelScope.launch {
            recordSnapshot("Sync SFX to Video Transitions")
            val context = getApplication<Application>()
            val syncedTracks = mutableListOf<AudioTrackItem>()

            for (clip in transitionClips) {
                val matchedAsset = when (clip.transitionIn) {
                    TransitionType.GLITCH -> BuiltInSoundEffectsCatalog.first { it.id == "sfx_trn_glitch" }
                    TransitionType.FLASH, TransitionType.LIGHT_LEAK, TransitionType.FILM_BURN ->
                        BuiltInSoundEffectsCatalog.first { it.id == "sfx_trn_sweep" }
                    TransitionType.SPIN, TransitionType.ROTATION_3D, TransitionType.SMOOTH_ZOOM ->
                        BuiltInSoundEffectsCatalog.first { it.id == "sfx_trn_rev_whoosh" }
                    else -> BuiltInSoundEffectsCatalog.first { it.id == "sfx_trn_whoosh" }
                }
                val wavFile = ProceduralMediaGenerator.ensureSoundEffectFile(context, matchedAsset)
                val startMs = (clip.timelineStartMs - 200L).coerceAtLeast(0L)
                syncedTracks.add(
                    AudioTrackItem(
                        title = "Sync • ${matchedAsset.title}",
                        uriString = Uri.fromFile(wavFile).toString(),
                        durationMs = matchedAsset.durationMs,
                        timelineStartMs = startMs,
                        trimStartMs = 0L,
                        trimEndMs = matchedAsset.durationMs,
                        volume = 0.9f,
                        fadeInMs = 50L,
                        fadeOutMs = 120L,
                        isSfx = true,
                        sfxCategoryName = "Transitions"
                    )
                )
            }

            _audioTracks.value = _audioTracks.value + syncedTracks
            _selectedAudioId.value = syncedTracks.firstOrNull()?.id
            saveCurrentProjectToRoom()
            showStatus("Synchronized ${syncedTracks.size} Sound Effects with clip transitions!")
        }
    }

    fun selectAudioTrack(trackId: String?) {
        _selectedAudioId.value = trackId
    }

    fun getSelectedAudioTrack(): AudioTrackItem? {
        val id = _selectedAudioId.value
        return _audioTracks.value.firstOrNull { it.id == id } ?: _audioTracks.value.lastOrNull()
    }

    fun previewSelectedAudioTrack(track: AudioTrackItem) {
        ProceduralMediaGenerator.playAudioFileUri(
            context = getApplication(),
            uriString = track.uriString,
            speed = track.speed,
            pitchSemitones = track.pitchSemitones,
            volume = track.volume.coerceIn(0.1f, 1.0f)
        )
        showStatus("Auditioning '${track.title}' (${track.speed}x, ${String.format("%+.0f", track.pitchSemitones)} st)")
    }

    fun addStudioAudioTrack(sample: StudioSampleAudio) {
        recordSnapshot("Add audio ${sample.title}")
        val track = AudioTrackItem(
            title = sample.title,
            uriString = sample.uriString,
            durationMs = sample.durationMs,
            timelineStartMs = _playheadMs.value,
            trimStartMs = 0L,
            trimEndMs = sample.durationMs,
            volume = 0.9f,
            isSfx = sample.isSfx,
            sfxCategoryName = sample.category,
            waveformAmplitudes = sample.waveform
        )
        _audioTracks.value = _audioTracks.value + track
        _selectedAudioId.value = track.id
        saveCurrentProjectToRoom()
        showStatus("Added '${sample.title}' to Audio Track")
    }

    fun importExternalAudioUri(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val duration = withContext(Dispatchers.IO) { queryMediaDurationMs(context, uri) }
            val name = uri.lastPathSegment?.substringAfterLast('/')?.take(24) ?: "Imported Audio"
            recordSnapshot("Import audio $name")
            val track = AudioTrackItem(
                title = name,
                uriString = uri.toString(),
                durationMs = duration,
                timelineStartMs = _playheadMs.value,
                trimStartMs = 0L,
                trimEndMs = duration
            )
            _audioTracks.value = _audioTracks.value + track
            _selectedAudioId.value = track.id
            saveCurrentProjectToRoom()
            showStatus("Imported audio '$name'")
        }
    }

    fun extractAudioFromSelectedClip() {
        val clip = getSelectedClip() ?: return
        recordSnapshot("Extract audio from ${clip.title}")
        val extractedTrack = AudioTrackItem(
            title = "Extracted • ${clip.title}",
            uriString = clip.uriString,
            durationMs = clip.effectiveDurationMs,
            timelineStartMs = clip.timelineStartMs,
            trimStartMs = clip.trimStartMs,
            trimEndMs = clip.trimEndMs,
            volume = clip.volume,
            isExtracted = true
        )
        _videoClips.value = _videoClips.value.map {
            if (it.id == clip.id) it.copy(isMuted = true) else it
        }
        _audioTracks.value = _audioTracks.value + extractedTrack
        _selectedAudioId.value = extractedTrack.id
        saveCurrentProjectToRoom()
        showStatus("Extracted audio from '${clip.title}' onto Audio Track")
    }

    fun updateAudioTrack(
        trackId: String,
        volume: Float? = null,
        fadeInMs: Long? = null,
        fadeOutMs: Long? = null,
        timelineStartMs: Long? = null,
        trimStartMs: Long? = null,
        trimEndMs: Long? = null,
        speed: Float? = null,
        pitchSemitones: Float? = null,
        isLooped: Boolean? = null,
        noiseReductionEnabled: Boolean? = null,
        audioDuckingEnabled: Boolean? = null
    ) {
        _audioTracks.value = _audioTracks.value.map { track ->
            if (track.id == trackId) {
                val safeTrimStart = (trimStartMs ?: track.trimStartMs).coerceIn(0L, (track.durationMs - 250L).coerceAtLeast(0L))
                val safeTrimEnd = (trimEndMs ?: track.trimEndMs).coerceIn(safeTrimStart + 250L, track.durationMs.coerceAtLeast(500L))
                track.copy(
                    volume = volume ?: track.volume,
                    fadeInMs = fadeInMs ?: track.fadeInMs,
                    fadeOutMs = fadeOutMs ?: track.fadeOutMs,
                    timelineStartMs = (timelineStartMs ?: track.timelineStartMs).coerceAtLeast(0L),
                    trimStartMs = safeTrimStart,
                    trimEndMs = safeTrimEnd,
                    speed = (speed ?: track.speed).coerceIn(0.5f, 2.0f),
                    pitchSemitones = (pitchSemitones ?: track.pitchSemitones).coerceIn(-12f, 12f),
                    isLooped = isLooped ?: track.isLooped,
                    noiseReductionEnabled = noiseReductionEnabled ?: track.noiseReductionEnabled,
                    audioDuckingEnabled = audioDuckingEnabled ?: track.audioDuckingEnabled
                )
            } else track
        }
        saveCurrentProjectToRoom()
    }

    fun addBeatMarkerToAudioTrack(trackId: String) {
        val currentPos = _playheadMs.value
        _audioTracks.value = _audioTracks.value.map { track ->
            if (track.id == trackId) {
                val localMs = (currentPos - track.timelineStartMs).coerceIn(100L, track.effectiveDurationMs)
                val updatedMarkers = (track.beatMarkersMs + localMs).distinct().sorted()
                track.copy(beatMarkersMs = updatedMarkers)
            } else track
        }
        saveCurrentProjectToRoom()
        showStatus("Marked beat point at ${formatTimecodeSimple(currentPos)}")
    }

    private fun formatTimecodeSimple(ms: Long): String {
        val s = ms / 1000.0
        return String.format("%.2fs", s)
    }

    fun deleteAudioTrack(trackId: String) {
        recordSnapshot("Remove audio track")
        _audioTracks.value = _audioTracks.value.filterNot { it.id == trackId }
        if (_selectedAudioId.value == trackId) {
            _selectedAudioId.value = _audioTracks.value.firstOrNull()?.id
        }
        saveCurrentProjectToRoom()
    }

    fun startVoiceRecording() {
        if (_isRecordingVoice.value) return
        val outFile = voiceRecorder.startRecording()
        if (outFile == null) {
            showStatus("Microphone unavailable in current container; synthesizing studio voiceover track...")
            viewModelScope.launch {
                _isRecordingVoice.value = true
                _voiceRecordingDurationMs.value = 0L
                val amps = mutableListOf<Float>()
                for (step in 1..20) {
                    if (!_isRecordingVoice.value) break
                    delay(150L)
                    _voiceRecordingDurationMs.value += 150L
                    amps.add((0.3f + 0.6f * abs(sin(step * 0.7f))).toFloat())
                    _liveMicAmplitudes.value = amps.toList()
                }
            }
            return
        }

        _isRecordingVoice.value = true
        _voiceRecordingDurationMs.value = 0L
        _liveMicAmplitudes.value = emptyList()
        voiceTimerJob?.cancel()
        voiceTimerJob = viewModelScope.launch {
            val amps = mutableListOf<Float>()
            while (isActive && _isRecordingVoice.value) {
                delay(120L)
                _voiceRecordingDurationMs.value += 120L
                val amp = voiceRecorder.getNormalizedAmplitude()
                amps.add(amp)
                if (amps.size > 32) amps.removeAt(0)
                _liveMicAmplitudes.value = amps.toList()
            }
        }
    }

    fun stopVoiceRecording() {
        if (!_isRecordingVoice.value) return
        _isRecordingVoice.value = false
        voiceTimerJob?.cancel()
        val result = voiceRecorder.stopRecording()
        val recordedWave = _liveMicAmplitudes.value.ifEmpty {
            listOf(0.35f, 0.75f, 0.6f, 0.9f, 0.5f, 0.8f, 0.45f, 0.7f)
        }
        val duration = _voiceRecordingDurationMs.value.coerceAtLeast(1500L)
        val uriStr = if (result != null) {
            Uri.fromFile(result.first).toString()
        } else {
            _studioAudios.value.firstOrNull()?.uriString ?: ""
        }

        recordSnapshot("Record Voiceover")
        val voiceTrack = AudioTrackItem(
            title = "Voiceover (${String.format("%.1fs", duration / 1000f)})",
            uriString = uriStr,
            durationMs = duration,
            timelineStartMs = _playheadMs.value,
            trimStartMs = 0L,
            trimEndMs = duration,
            volume = 1.0f,
            noiseReductionEnabled = true,
            audioDuckingEnabled = true,
            isVoiceover = true,
            sfxCategoryName = "Voiceover",
            waveformAmplitudes = recordedWave
        )
        _audioTracks.value = _audioTracks.value + voiceTrack
        _selectedAudioId.value = voiceTrack.id
        saveCurrentProjectToRoom()
        showStatus("Added Voiceover track (${String.format("%.1fs", duration / 1000f)}) with Noise Reduction & Ducking")
    }

    // Effects Store Marketplace Actions
    fun downloadAndInstallEffectPack(pack: EffectStorePack, applyAfterDownload: Boolean = true) {
        if (_packDownloadProgress.value.containsKey(pack.id)) return
        viewModelScope.launch {
            for (pct in listOf(15, 38, 64, 88, 100)) {
                _packDownloadProgress.value = _packDownloadProgress.value + (pack.id to pct)
                delay(140L)
            }
            _downloadedPackIds.value = _downloadedPackIds.value + pack.id
            _packDownloadProgress.value = _packDownloadProgress.value - pack.id

            if (applyAfterDownload) {
                pack.includedEffects.firstOrNull()?.let { firstFx ->
                    addVideoEffectToTimeline(firstFx, intensity = 0.82f)
                }
                val firstSfx = BuiltInSoundEffectsCatalog.firstOrNull { it.id in pack.includedSfxIds }
                if (firstSfx != null) {
                    addSoundEffectAssetToTimeline(firstSfx)
                }
            } else {
                showStatus("Downloaded '${pack.title}' into your Effects & SFX Library!")
            }
        }
    }

    // Text, Subtitles & Stickers (Stage 3)
    fun addTextOverlay(
        text: String,
        fontChoice: FontChoice = FontChoice.SPACE_GROTESK,
        animationType: TextAnimationType = TextAnimationType.BOUNCE_IN,
        colorHex: Long = 0xFFFFFFFF,
        bgColorHex: Long = 0xAA07090E,
        isSubtitle: Boolean = false
    ) {
        if (text.isBlank()) return
        recordSnapshot("Add text '$text'")
        val start = _playheadMs.value
        val end = (start + 3000L).coerceAtMost(totalTimelineDurationMs.coerceAtLeast(3000L))
        val item = TextOverlayItem(
            text = text.trim(),
            startTimeMs = start,
            endTimeMs = end,
            xNormalized = 0.5f,
            yNormalized = if (isSubtitle) 0.84f else 0.45f,
            fontSizeSp = if (isSubtitle) 19f else 24f,
            colorHex = colorHex,
            backgroundColorHex = bgColorHex,
            fontChoice = fontChoice,
            animationType = animationType,
            isSubtitle = isSubtitle
        )
        _textOverlays.value = _textOverlays.value + item
        _selectedTextId.value = item.id
        saveCurrentProjectToRoom()
        showStatus("Added ${if (isSubtitle) "Subtitle" else "Text"} layer")
    }

    fun updateTextOverlayPosition(id: String, xNorm: Float, yNorm: Float) {
        _textOverlays.value = _textOverlays.value.map {
            if (it.id == id) {
                it.copy(
                    xNormalized = xNorm.coerceIn(0.1f, 0.9f),
                    yNormalized = yNorm.coerceIn(0.08f, 0.92f)
                )
            } else it
        }
        saveCurrentProjectToRoom()
    }

    fun deleteTextOverlay(id: String) {
        recordSnapshot("Delete text layer")
        _textOverlays.value = _textOverlays.value.filterNot { it.id == id }
        saveCurrentProjectToRoom()
    }

    fun addStickerOverlay(label: String, symbol: String, accentHex: Long) {
        recordSnapshot("Add sticker $label")
        val start = _playheadMs.value
        val end = (start + 3500L).coerceAtMost(totalTimelineDurationMs.coerceAtLeast(3500L))
        val sticker = StickerOverlayItem(
            label = label,
            symbol = symbol,
            accentHex = accentHex,
            startTimeMs = start,
            endTimeMs = end
        )
        _stickerOverlays.value = _stickerOverlays.value + sticker
        _selectedStickerId.value = sticker.id
        saveCurrentProjectToRoom()
        showStatus("Added '$label' sticker overlay")
    }

    fun updateStickerPosition(id: String, xNorm: Float, yNorm: Float) {
        _stickerOverlays.value = _stickerOverlays.value.map {
            if (it.id == id) {
                it.copy(
                    xNormalized = xNorm.coerceIn(0.1f, 0.9f),
                    yNormalized = yNorm.coerceIn(0.1f, 0.9f)
                )
            } else it
        }
        saveCurrentProjectToRoom()
    }

    fun deleteStickerOverlay(id: String) {
        recordSnapshot("Delete sticker")
        _stickerOverlays.value = _stickerOverlays.value.filterNot { it.id == id }
        saveCurrentProjectToRoom()
    }

    // AI Features via Firebase AI Logic (Stage 4 + AI Custom Effect Generator — 100% Free & Unlocked)
    fun runAiCustomEffectGenerator(userPrompt: String) {
        val cleanPrompt = userPrompt.trim()
        if (cleanPrompt.isBlank()) {
            showStatus("Describe the custom video effect you want AI to create")
            return
        }
        val clip = getSelectedClip() ?: _videoClips.value.firstOrNull()
        viewModelScope.launch {
            _aiTaskState.value = AiTaskState.Running("Synthesizing custom shader '${cleanPrompt.take(36)}' with Gemini 3.8 Flash...")
            val context = getApplication<Application>()
            val frames = if (clip != null) {
                ProceduralMediaGenerator.extractFramesForAi(
                    context = context,
                    uriString = clip.uriString,
                    count = 1
                )
            } else emptyList()

            val result = aiService.generateCustomVideoEffectFromPrompt(
                userPrompt = cleanPrompt,
                sampleFrame = frames.firstOrNull()
            )
            result.fold(
                onSuccess = { generatedEffect ->
                    _customAiEffects.value = listOf(generatedEffect) + _customAiEffects.value
                    applyAiCustomEffectToTimeline(generatedEffect)
                    _aiTaskState.value = AiTaskState.Success(
                        "Created & stacked AI Effect '${generatedEffect.effectName}' on your timeline!"
                    )
                },
                onFailure = { err ->
                    Log.e(TAG, "AI Custom Effect Generator error", err)
                    _aiTaskState.value = AiTaskState.Error(
                        err.localizedMessage ?: "AI Custom Effect generation failed."
                    )
                }
            )
        }
    }

    fun applyAiCustomEffectToTimeline(
        customEffect: AiCustomVideoEffect,
        durationMs: Long = 3500L,
        bindToSelectedClip: Boolean = false
    ) {
        recordSnapshot("Add AI Custom FX ${customEffect.effectName}")
        val selectedClip = getSelectedClip()
        val start = if (bindToSelectedClip && selectedClip != null) {
            selectedClip.timelineStartMs
        } else {
            _playheadMs.value
        }
        val end = if (bindToSelectedClip && selectedClip != null) {
            selectedClip.timelineEndMs
        } else {
            (start + durationMs).coerceAtMost(totalTimelineDurationMs.coerceAtLeast(3500L))
        }

        val fxItem = VideoEffectItem(
            effectType = customEffect.baseEffectType,
            startTimeMs = start,
            endTimeMs = end.coerceAtLeast(start + 500L),
            intensity = 0.85f,
            targetClipId = if (bindToSelectedClip) selectedClip?.id else null,
            customEffectName = customEffect.effectName,
            customPrompt = customEffect.userPrompt,
            customTintHex = customEffect.tintColorHex,
            customBloom = customEffect.bloomIntensity,
            customGlitch = customEffect.glitchIntensity,
            customGrain = customEffect.grainAmount,
            customVignette = customEffect.vignetteAmount,
            customShake = customEffect.shakeIntensity,
            customZoomPulse = customEffect.zoomPulse,
            customScanlines = customEffect.scanlinesIntensity,
            customChromatic = customEffect.chromaticShift
        )
        _videoEffects.value = _videoEffects.value + fxItem
        _selectedEffectId.value = fxItem.id
        saveCurrentProjectToRoom()
        showStatus("Applied AI Custom Effect '${customEffect.effectName}' to Effects Timeline")
    }

    fun runAiAutoSubtitles() {
        val clips = _videoClips.value
        if (clips.isEmpty()) {
            showStatus("Add a video clip first to generate AI subtitles")
            return
        }
        viewModelScope.launch {
            _aiTaskState.value = AiTaskState.Running("Analyzing video & generating timed AI Captions with Gemini 3.8 Flash...")
            val context = getApplication<Application>()
            val frames = ProceduralMediaGenerator.extractFramesForAi(
                context = context,
                uriString = clips.first().uriString,
                count = 1
            )
            val result = aiService.generateAutoSubtitles(
                projectTitle = _projectTitle.value,
                clipTitles = clips.map { it.title },
                totalDurationMs = totalTimelineDurationMs,
                sampleFrame = frames.firstOrNull()
            )
            result.fold(
                onSuccess = { generatedSubs ->
                    recordSnapshot("AI Auto Subtitles")
                    val nonSubTexts = _textOverlays.value.filterNot { it.isSubtitle }
                    _textOverlays.value = nonSubTexts + generatedSubs
                    saveCurrentProjectToRoom()
                    _aiTaskState.value = AiTaskState.Success(
                        "Generated ${generatedSubs.size} timed AI subtitle segments on the CC track!"
                    )
                },
                onFailure = { err ->
                    Log.e(TAG, "AI Auto Subtitles error", err)
                    _aiTaskState.value = AiTaskState.Error(
                        err.localizedMessage ?: "AI Subtitle generation failed."
                    )
                }
            )
        }
    }

    fun runAiSceneDetection() {
        val clip = getSelectedClip() ?: _videoClips.value.firstOrNull()
        if (clip == null) {
            showStatus("Select a video clip first for AI Scene Detection")
            return
        }
        viewModelScope.launch {
            _aiTaskState.value = AiTaskState.Running("Extracting frames & detecting scene cuts + highlights with Gemini 3.8 Flash...")
            val context = getApplication<Application>()
            val frames = ProceduralMediaGenerator.extractFramesForAi(
                context = context,
                uriString = clip.uriString,
                count = 3
            )
            val result = aiService.detectScenesAndHighlights(
                clipTitle = clip.title,
                durationMs = clip.effectiveDurationMs,
                frames = frames
            )
            result.fold(
                onSuccess = { scenes ->
                    _detectedScenes.value = scenes
                    _aiTaskState.value = AiTaskState.Success(
                        "Detected ${scenes.size} scenes (${scenes.count { it.isHighlight }} highlight moments)."
                    )
                },
                onFailure = { err ->
                    Log.e(TAG, "AI Scene Detection error", err)
                    _aiTaskState.value = AiTaskState.Error(
                        err.localizedMessage ?: "AI Scene Detection failed."
                    )
                }
            )
        }
    }

    fun applyDetectedHighlightToTimeline(sceneCut: AiSceneCut) {
        seekTo(sceneCut.timestampMs)
        splitSelectedClipAtPlayhead()
        val selected = getSelectedClip()
        if (selected != null) {
            _videoClips.value = _videoClips.value.map {
                if (it.id == selected.id) it.copy(isHighlight = sceneCut.isHighlight) else it
            }
            saveCurrentProjectToRoom()
        }
        showStatus("Split & marked AI Highlight: ${sceneCut.label}")
    }

    fun runAiVideoEnhancement() {
        val clip = getSelectedClip() ?: _videoClips.value.firstOrNull()
        if (clip == null) {
            showStatus("Select a video clip first for AI Enhancement")
            return
        }
        viewModelScope.launch {
            _aiTaskState.value = AiTaskState.Running("Analyzing frame dynamic range & color grade with Gemini 3.8 Flash...")
            val context = getApplication<Application>()
            val frames = ProceduralMediaGenerator.extractFramesForAi(
                context = context,
                uriString = clip.uriString,
                count = 1
            )
            val result = aiService.analyzeAndEnhanceVideo(
                clipTitle = clip.title,
                currentFilter = clip.filterPreset,
                sampleFrame = frames.firstOrNull()
            )
            result.fold(
                onSuccess = { suggestion ->
                    _aiEnhancement.value = suggestion
                    _aiTaskState.value = AiTaskState.Success(
                        "AI Colorist recommends '${suggestion.recommendedFilter.label}' LUT."
                    )
                },
                onFailure = { err ->
                    Log.e(TAG, "AI Video Enhancement error", err)
                    _aiTaskState.value = AiTaskState.Error(
                        err.localizedMessage ?: "AI Video Enhancement failed."
                    )
                }
            )
        }
    }

    fun applyAiEnhancementSuggestion(suggestion: AiEnhancementSuggestion) {
        recordSnapshot("Apply AI Colorist Enhancement")
        _videoClips.value = _videoClips.value.map { clip ->
            clip.copy(
                filterPreset = suggestion.recommendedFilter,
                brightness = suggestion.recommendedBrightness,
                contrast = suggestion.recommendedContrast,
                saturation = suggestion.recommendedSaturation
            )
        }
        addVideoEffectToTimeline(VideoEffectType.AI_VIDEO_ENHANCE, intensity = 0.85f)
        if (suggestion.suggestedHookTitle.isNotBlank()) {
            addTextOverlay(
                text = suggestion.suggestedHookTitle,
                fontChoice = FontChoice.BEBAS_NEUE,
                animationType = TextAnimationType.NEON_FLICKER,
                colorHex = 0xFF00F0FF
            )
        }
        saveCurrentProjectToRoom()
        showStatus("Applied AI Color Grade (${suggestion.recommendedFilter.label}), AI Super Clarity FX & Hook Title")
    }

    // Export Settings & Media3 Transformer MP4 Export (Stage 1 & Stage 5)
    fun setExportResolution(res: ExportResolution) {
        _exportResolution.value = res
    }

    fun setExportFps(fps: ExportFps) {
        _exportFps.value = fps
    }

    fun setExportBitratePreset(preset: ExportBitratePreset) {
        _exportBitratePreset.value = preset
    }

    fun startVideoExport() {
        pausePlayback()
        ProceduralMediaGenerator.stopAudioPreview()
        navigateTo(AppScreen.EXPORT_PROGRESS)
        val res = _exportResolution.value
        val fps = _exportFps.value
        val bitrate = _exportBitratePreset.value
        _exportState.value = ExportState.Exporting(
            progressPercent = 2,
            statusText = "Initializing AndroidX Media3 Transformer (${bitrate.label})...",
            resolutionLabel = res.label,
            fpsLabel = "${fps.fps} FPS"
        )

        viewModelScope.launch {
            val result = VideoExportEngine.exportTimelineToMp4(
                context = getApplication(),
                projectTitle = _projectTitle.value,
                clips = _videoClips.value,
                videoEffects = _videoEffects.value,
                aspectRatio = _aspectRatio.value,
                resolution = res,
                fps = fps,
                onProgress = { pct, status ->
                    _exportState.value = ExportState.Exporting(
                        progressPercent = pct,
                        statusText = status,
                        resolutionLabel = res.label,
                        fpsLabel = "${fps.fps} FPS"
                    )
                }
            )
            _exportState.value = result
            if (result is ExportState.Completed) {
                val uriOrPath = result.galleryUriString ?: result.outputFilePath
                saveCurrentProjectToRoom(lastExportedUri = uriOrPath)
                val record = ExportHistoryRecord(
                    projectTitle = _projectTitle.value,
                    resolutionLabel = "${res.label} ${res.badge}",
                    fpsLabel = "${fps.fps} FPS • ${bitrate.label}",
                    bitrateMbps = res.bitrateMbps * bitrate.multiplier,
                    fileSizeKb = result.fileSizeKb,
                    durationMs = result.durationMs,
                    outputFilePath = uriOrPath
                )
                _exportHistory.value = listOf(record) + _exportHistory.value
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecorder.stopRecording()
        ProceduralMediaGenerator.stopAudioPreview()
        playbackTickerJob?.cancel()
    }
}
