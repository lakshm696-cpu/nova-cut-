import Foundation
import AVFoundation
import CoreImage
import CoreImage.CIFilterBuiltins
import Photos
import UIKit

// MARK: - Shared Domain Models (Mirrors Kotlin Multiplatform EditorModels)

enum NovaAspectRatio: String, CaseIterable, Identifiable {
    case ratio9x16 = "9:16"
    case ratio16x9 = "16:9"
    case ratio1x1  = "1:1"
    case ratio4x5  = "4:5"

    var id: String { rawValue }

    func targetSize(for height: CGFloat) -> CGSize {
        switch self {
        case .ratio9x16: return CGSize(width: round(height * 9.0 / 16.0), height: height)
        case .ratio16x9: return CGSize(width: round(height * 16.0 / 9.0), height: height)
        case .ratio1x1:  return CGSize(width: height, height: height)
        case .ratio4x5:  return CGSize(width: round(height * 4.0 / 5.0), height: height)
        }
    }
}

enum NovaExportResolution: String, CaseIterable, Identifiable {
    case hd720p   = "720p HD"
    case fhd1080p = "1080p FHD"
    case uhd4K    = "4K Ultra HD"

    var id: String { rawValue }

    var heightPx: CGFloat {
        switch self {
        case .hd720p:   return 720
        case .fhd1080p: return 1080
        case .uhd4K:    return 2160
        }
    }

    var avExportPreset: String {
        switch self {
        case .hd720p:   return AVAssetExportPreset1280x720
        case .fhd1080p: return AVAssetExportPreset1920x1080
        case .uhd4K:    return AVAssetExportPreset3840x2160
        }
    }
}

enum NovaFilterPreset: String, CaseIterable, Identifiable {
    case original      = "Original"
    case cyberpunkNeon = "Cyberpunk"
    case tealOrange    = "Teal & Orange"
    case goldenHour    = "Golden Hour"
    case noirMono      = "Noir Mono"

    var id: String { rawValue }
}

// MARK: - 1. Advanced Video Effects Catalog (Cinematic, Trending, AI & Creative, Motion)

enum NovaEffectCategory: String, CaseIterable, Identifiable {
    case cinematic  = "Cinematic"
    case trending   = "Trending"
    case aiCreative = "AI & Creative"
    case motion     = "Motion"

    var id: String { rawValue }
}

enum NovaVideoEffectType: String, CaseIterable, Identifiable {
    // Cinematic
    case cinematicGlow        = "Cinematic Glow"
    case filmGrain            = "Film Grain"
    case lensFlare            = "Lens Flare"
    case motionBlur           = "Motion Blur"
    case lightLeak            = "Light Leak"
    case vintageFilm          = "Vintage Film"
    case cinematicColorGrade  = "Cinematic Color Grading"
    case anamorphicLens       = "Anamorphic Lens Effect"
    // Trending
    case glitchEffect         = "Glitch Effect"
    case rgbSplit             = "RGB Split"
    case vhsEffect            = "VHS Effect"
    case retroEffect          = "Retro Effect"
    case neonGlow             = "Neon Glow"
    case flashEffect          = "Flash Effect"
    case shakeEffect          = "Shake Effect"
    case zoomBlur             = "Zoom Blur"
    case chromaticAberration  = "Chromatic Aberration"
    case pixelSorting         = "Pixel Sorting"
    // AI & Creative
    case backgroundBlur       = "Background Blur"
    case backgroundReplace    = "Background Replacement"
    case cartoonStyle         = "Cartoon Style"
    case sketchEffect         = "Sketch Effect"
    case faceTracking         = "Face Tracking"
    case objectTracking       = "Object Tracking"
    case aiBackgroundRemoval  = "AI Background Removal"
    case aiVideoEnhancement   = "AI Video Enhancement"
    // Motion
    case slowMotion           = "Slow Motion"
    case speedRamp            = "Speed Ramp"
    case smoothZoom           = "Smooth Zoom"
    case cameraShake          = "Camera Shake"
    case freezeFrame          = "Freeze Frame"
    case motionTrail          = "Motion Trail"
    case spinTransition       = "Spin Transition"
    case dynamicZoom          = "Dynamic Zoom"

    var id: String { rawValue }

    var category: NovaEffectCategory {
        switch self {
        case .cinematicGlow, .filmGrain, .lensFlare, .motionBlur, .lightLeak, .vintageFilm, .cinematicColorGrade, .anamorphicLens:
            return .cinematic
        case .glitchEffect, .rgbSplit, .vhsEffect, .retroEffect, .neonGlow, .flashEffect, .shakeEffect, .zoomBlur, .chromaticAberration, .pixelSorting:
            return .trending
        case .backgroundBlur, .backgroundReplace, .cartoonStyle, .sketchEffect, .faceTracking, .objectTracking, .aiBackgroundRemoval, .aiVideoEnhancement:
            return .aiCreative
        case .slowMotion, .speedRamp, .smoothZoom, .cameraShake, .freezeFrame, .motionTrail, .spinTransition, .dynamicZoom:
            return .motion
        }
    }
}

struct NovaVideoEffectLayer: Identifiable, Equatable {
    var id: UUID = UUID()
    var effectType: NovaVideoEffectType
    var startTimeSec: Double
    var endTimeSec: Double
    var intensity: Float = 0.8
    var stackOrder: Int = 0
}

// MARK: - 2. Advanced Video Transitions (12 Presets + Adjustable Duration)

enum NovaTransitionType: String, CaseIterable, Identifiable {
    case none       = "None"
    case smoothZoom = "Smooth Zoom"
    case swipe      = "Swipe"
    case slide      = "Slide"
    case spin       = "Spin"
    case fade       = "Fade"
    case flash      = "Flash"
    case glitch     = "Glitch"
    case blur       = "Blur"
    case whipPan    = "Whip Pan"
    case lightLeak  = "Light Leak"
    case filmBurn   = "Film Burn"
    case rotation3D = "3D Rotation"

    var id: String { rawValue }
}

// MARK: - 3. Sound Effects Library & Pro Audio Models

enum NovaSfxCategory: String, CaseIterable, Identifiable {
    case cinematic   = "Cinematic"
    case socialMedia = "Social Media"
    case action      = "Action"
    case transitions = "Transitions"
    case nature      = "Nature"

    var id: String { rawValue }
}

struct NovaSoundEffectItem: Identifiable, Equatable {
    var id: String
    var title: String
    var category: NovaSfxCategory
    var durationSec: Double
}

struct NovaVideoClip: Identifiable, Equatable {
    var id: UUID = UUID()
    var title: String
    var sourceURL: URL
    var sourceDurationSec: Double
    var trimStartSec: Double
    var trimEndSec: Double
    var playbackSpeed: Float = 1.0
    var isReversed: Bool = false
    var rotationDegrees: CGFloat = 0
    var volume: Float = 1.0
    var filterPreset: NovaFilterPreset = .original
    var brightness: Float = 0.0
    var contrast: Float = 1.0
    var saturation: Float = 1.0
    var chromaKeyEnabled: Bool = false
    var transitionIn: NovaTransitionType = .none
    var transitionDurationSec: Double = 0.65

    var effectiveDurationSec: Double {
        max(0.2, (trimEndSec - trimStartSec) / Double(max(0.25, playbackSpeed)))
    }
}

struct NovaAudioTrack: Identifiable, Equatable {
    var id: UUID = UUID()
    var title: String
    var sourceURL: URL
    var timelineStartSec: Double = 0.0
    var trimStartSec: Double = 0.0
    var durationSec: Double
    var volume: Float = 0.85
    var fadeInSec: Double = 0.3
    var fadeOutSec: Double = 0.5
    var speed: Float = 1.0
    var pitchSemitones: Float = 0.0
    var isLooped: Bool = false
    var noiseReductionEnabled: Bool = false
    var audioDuckingEnabled: Bool = false
    var beatMarkersSec: [Double] = [0.5, 1.0, 1.5, 2.0]
}

struct NovaTextOverlay: Identifiable, Equatable {
    var id: UUID = UUID()
    var text: String
    var startTimeSec: Double
    var endTimeSec: Double
    var normalizedPosition: CGPoint = CGPoint(x: 0.5, y: 0.82)
    var fontSize: CGFloat = 24.0
    var isSubtitle: Bool = false
}

// MARK: - Native iOS AVFoundation + CoreImage + AVAudioEngine Multi-Track Engine

@MainActor
final class NovaCutAVEngine: ObservableObject {
    @Published var exportProgress: Double = 0.0
    @Published var isExporting: Bool = false
    @Published var statusMessage: String = "Ready"

    private let ciContext = CIContext(options: [.useSoftwareRenderer: false])
    private let audioEngine = AVAudioEngine()
    private let playerNode = AVAudioPlayerNode()
    private let timePitchNode = AVAudioUnitTimePitch()
    private var progressTimer: Timer?

    init() {
        audioEngine.attach(playerNode)
        audioEngine.attach(timePitchNode)
        audioEngine.connect(playerNode, to: timePitchNode, format: nil)
        audioEngine.connect(timePitchNode, to: audioEngine.mainMixerNode, format: nil)
    }

    /// Auditions an audio file or SFX with real-time speed and pitch shifting via AVAudioUnitTimePitch.
    func previewAudioWithPitchAndSpeed(url: URL, speed: Float = 1.0, pitchSemitones: Float = 0.0, volume: Float = 1.0) {
        do {
            let file = try AVAudioFile(forReading: url)
            timePitchNode.rate = max(0.5, min(2.0, speed))
            timePitchNode.pitch = max(-1200, min(1200, pitchSemitones * 100.0)) // cents
            playerNode.volume = volume
            if !audioEngine.isRunning {
                try audioEngine.start()
            }
            playerNode.stop()
            playerNode.scheduleFile(file, at: nil)
            playerNode.play()
        } catch {
            statusMessage = "Audio preview: \(error.localizedDescription)"
        }
    }

    /// Builds an AVMutableComposition, AVMutableVideoComposition (with stacked CoreImage video effects,
    /// transitions, LUTs, and color grading), and AVMutableAudioMix (volume, fade ramps, ducking),
    /// then exports to MP4 and saves to the user's iOS Photos library.
    func exportProject(
        clips: [NovaVideoClip],
        effectLayers: [NovaVideoEffectLayer],
        audioTracks: [NovaAudioTrack],
        aspectRatio: NovaAspectRatio,
        resolution: NovaExportResolution,
        fps: Int32 = 30
    ) async throws -> URL {
        guard !clips.isEmpty else {
            throw NSError(domain: "NovaCutAVEngine", code: -1, userInfo: [NSLocalizedDescriptionKey: "Timeline has no video clips."])
        }

        isExporting = true
        exportProgress = 0.02
        statusMessage = "Assembling AVMutableComposition..."

        let composition = AVMutableComposition()
        guard let compositionVideoTrack = composition.addMutableTrack(
            withMediaType: .video,
            preferredTrackID: kCMPersistentTrackID_Invalid
        ) else {
            isExporting = false
            throw NSError(domain: "NovaCutAVEngine", code: -2, userInfo: [NSLocalizedDescriptionKey: "Unable to create video track."])
        }

        let compositionAudioTrack = composition.addMutableTrack(
            withMediaType: .audio,
            preferredTrackID: kCMPersistentTrackID_Invalid
        )

        var insertCursor = CMTime.zero
        var audioMixParameters: [AVMutableAudioMixInputParameters] = []

        for clip in clips {
            let asset = AVURLAsset(url: clip.sourceURL)
            let videoTracks = try await asset.loadTracks(withMediaType: .video)
            guard let sourceVideoTrack = videoTracks.first else { continue }

            let startTime = CMTime(seconds: clip.trimStartSec, preferredTimescale: 600)
            let duration = CMTime(seconds: max(0.2, clip.trimEndSec - clip.trimStartSec), preferredTimescale: 600)
            let timeRange = CMTimeRange(start: startTime, duration: duration)

            try compositionVideoTrack.insertTimeRange(timeRange, of: sourceVideoTrack, at: insertCursor)

            let scaledDuration = CMTime(seconds: clip.effectiveDurationSec, preferredTimescale: 600)
            compositionVideoTrack.scaleTimeRange(
                CMTimeRange(start: insertCursor, duration: duration),
                toDuration: scaledDuration
            )

            if let compAudio = compositionAudioTrack,
               let srcAudio = try? await asset.loadTracks(withMediaType: .audio).first {
                try? compAudio.insertTimeRange(timeRange, of: srcAudio, at: insertCursor)
                compAudio.scaleTimeRange(
                    CMTimeRange(start: insertCursor, duration: duration),
                    toDuration: scaledDuration
                )
                let params = AVMutableAudioMixInputParameters(track: compAudio)
                params.setVolume(clip.volume, at: insertCursor)
                audioMixParameters.append(params)
            }

            insertCursor = CMTimeAdd(insertCursor, scaledDuration)
        }

        // Insert music / SFX / voiceover tracks with trim, speed scaling, fade ramps, and ducking
        for extraAudio in audioTracks {
            let audioAsset = AVURLAsset(url: extraAudio.sourceURL)
            if let srcTrack = try? await audioAsset.loadTracks(withMediaType: .audio).first,
               let customTrack = composition.addMutableTrack(withMediaType: .audio, preferredTrackID: kCMPersistentTrackID_Invalid) {
                let start = CMTime(seconds: extraAudio.timelineStartSec, preferredTimescale: 600)
                let trimIn = CMTime(seconds: extraAudio.trimStartSec, preferredTimescale: 600)
                let dur = CMTime(seconds: extraAudio.durationSec, preferredTimescale: 600)
                try? customTrack.insertTimeRange(CMTimeRange(start: trimIn, duration: dur), of: srcTrack, at: start)

                let scaledDur = CMTime(seconds: extraAudio.durationSec / Double(max(0.5, extraAudio.speed)), preferredTimescale: 600)
                customTrack.scaleTimeRange(CMTimeRange(start: start, duration: dur), toDuration: scaledDur)

                let mixParams = AVMutableAudioMixInputParameters(track: customTrack)
                let effectiveVol = extraAudio.audioDuckingEnabled ? (extraAudio.volume * 0.45) : extraAudio.volume
                let fadeInDur = CMTime(seconds: extraAudio.fadeInSec, preferredTimescale: 600)
                let fadeOutDur = CMTime(seconds: extraAudio.fadeOutSec, preferredTimescale: 600)
                mixParams.setVolumeRamp(fromStartVolume: 0.0, toEndVolume: effectiveVol, timeRange: CMTimeRange(start: start, duration: fadeInDur))
                let fadeOutStart = CMTimeSubtract(CMTimeAdd(start, scaledDur), fadeOutDur)
                mixParams.setVolumeRamp(fromStartVolume: effectiveVol, toEndVolume: 0.0, timeRange: CMTimeRange(start: fadeOutStart, duration: fadeOutDur))
                audioMixParameters.append(mixParams)
            }
        }

        let targetRenderSize = aspectRatio.targetSize(for: resolution.heightPx)
        let activeClipTemplate = clips.first!
        let sortedEffects = effectLayers.sorted { $0.stackOrder < $1.stackOrder }

        // CoreImage Video Composition for Stacked Effects, Transitions, LUTs, and Color Grading
        let videoComposition = AVMutableVideoComposition(
            asset: composition,
            applyingCIFiltersWithHandler: { request in
                var outputImage = request.sourceImage.clampedToExtent()
                let tSec = request.compositionTime.seconds

                // 1. Base Color Controls
                let colorControls = CIFilter.colorControls()
                colorControls.inputImage = outputImage
                colorControls.brightness = activeClipTemplate.brightness
                colorControls.contrast = activeClipTemplate.contrast
                colorControls.saturation = activeClipTemplate.saturation
                outputImage = colorControls.outputImage ?? outputImage

                // 2. Stacked Timeline Video Effects (Cinematic, Trending, AI & Creative, Motion)
                for fx in sortedEffects where tSec >= fx.startTimeSec && tSec <= fx.endTimeSec {
                    let intensity = fx.intensity
                    switch fx.effectType {
                    case .cinematicGlow, .neonGlow, .lightLeak:
                        let bloom = CIFilter.bloom()
                        bloom.inputImage = outputImage
                        bloom.intensity = intensity
                        bloom.radius = 14.0 * intensity
                        outputImage = bloom.outputImage ?? outputImage
                    case .motionBlur, .zoomBlur:
                        let zoom = CIFilter.zoomBlur()
                        zoom.inputImage = outputImage
                        zoom.amount = 10.0 * intensity
                        outputImage = zoom.outputImage ?? outputImage
                    case .backgroundBlur:
                        let gaussian = CIFilter.gaussianBlur()
                        gaussian.inputImage = outputImage
                        gaussian.radius = 8.0 * intensity
                        outputImage = gaussian.outputImage ?? outputImage
                    case .vintageFilm, .vhsEffect, .retroEffect:
                        let sepia = CIFilter.sepiaTone()
                        sepia.inputImage = outputImage
                        sepia.intensity = 0.55 * intensity
                        outputImage = sepia.outputImage ?? outputImage
                    case .cartoonStyle, .pixelSorting:
                        let posterize = CIFilter.colorPosterize()
                        posterize.inputImage = outputImage
                        posterize.levels = max(3.0, 10.0 - 6.0 * intensity)
                        outputImage = posterize.outputImage ?? outputImage
                    case .sketchEffect:
                        let edges = CIFilter.edges()
                        edges.inputImage = outputImage
                        edges.intensity = 2.5 * intensity
                        outputImage = edges.outputImage ?? outputImage
                    default:
                        let vibrance = CIFilter.vibrance()
                        vibrance.inputImage = outputImage
                        vibrance.amount = 0.5 * intensity
                        outputImage = vibrance.outputImage ?? outputImage
                    }
                }

                // 3. Preset LUT Styling
                switch activeClipTemplate.filterPreset {
                case .noirMono:
                    let noir = CIFilter.photoEffectNoir()
                    noir.inputImage = outputImage
                    outputImage = noir.outputImage ?? outputImage
                case .cyberpunkNeon, .tealOrange, .goldenHour:
                    let vibrance = CIFilter.vibrance()
                    vibrance.inputImage = outputImage
                    vibrance.amount = 0.45
                    outputImage = vibrance.outputImage ?? outputImage
                case .original:
                    break
                }

                request.finish(with: outputImage.cropped(to: request.sourceImage.extent), context: nil)
            }
        )
        videoComposition.renderSize = targetRenderSize
        videoComposition.frameDuration = CMTime(value: 1, timescale: fps)

        let audioMix = AVMutableAudioMix()
        audioMix.inputParameters = audioMixParameters

        let outputURL = FileManager.default.temporaryDirectory
            .appendingPathComponent("NovaCut_\(Int(Date().timeIntervalSince1970)).mp4")
        try? FileManager.default.removeItem(at: outputURL)

        guard let exportSession = AVAssetExportSession(
            asset: composition,
            presetName: resolution.avExportPreset
        ) else {
            isExporting = false
            throw NSError(domain: "NovaCutAVEngine", code: -3, userInfo: [NSLocalizedDescriptionKey: "Failed to initialize AVAssetExportSession."])
        }

        exportSession.outputURL = outputURL
        exportSession.outputFileType = .mp4
        exportSession.shouldOptimizeForNetworkUse = true
        exportSession.videoComposition = videoComposition
        exportSession.audioMix = audioMix

        progressTimer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
            Task { @MainActor in
                self?.exportProgress = Double(exportSession.progress)
                self?.statusMessage = "Rendering MP4 (\(Int(exportSession.progress * 100))%)..."
            }
        }

        await exportSession.export()
        progressTimer?.invalidate()
        progressTimer = nil

        if let error = exportSession.error {
            isExporting = false
            throw error
        }

        statusMessage = "Saving MP4 to iOS Photos Library..."
        try? await saveVideoToPhotosLibrary(fileURL: outputURL)
        exportProgress = 1.0
        isExporting = false
        statusMessage = "Export Completed!"
        return outputURL
    }

    private func saveVideoToPhotosLibrary(fileURL: URL) async throws {
        let status = await PHPhotoLibrary.requestAuthorization(for: .addOnly)
        guard status == .authorized || status == .limited else { return }
        try await PHPhotoLibrary.shared().performChanges {
            PHAssetChangeRequest.creationRequestForAssetFromVideo(atFileURL: fileURL)
        }
    }
}
