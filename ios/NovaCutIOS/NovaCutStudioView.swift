import SwiftUI
import AVKit
import PhotosUI

// MARK: - Native SwiftUI NOVACUT Luxury Dark Studio Interface (100% Free Unlocked Edition)

struct NovaCutStudioView: View {
    @StateObject private var avEngine = NovaCutAVEngine()
    @State private var projectTitle: String = "NOVACUT • Luxury Reel"
    @State private var aiCustomPrompt: String = "24K gold cinema lens flare with royal purple halation"
    @State private var aspectRatio: NovaAspectRatio = .ratio9x16
    @State private var exportResolution: NovaExportResolution = .fhd1080p
    @State private var clips: [NovaVideoClip] = []
    @State private var effectLayers: [NovaVideoEffectLayer] = [
        NovaVideoEffectLayer(effectType: .cinematicGlow, startTimeSec: 0.0, endTimeSec: 2.5, intensity: 0.8),
        NovaVideoEffectLayer(effectType: .glitchEffect, startTimeSec: 1.8, endTimeSec: 4.0, intensity: 0.75, stackOrder: 1)
    ]
    @State private var audioTracks: [NovaAudioTrack] = []
    @State private var textOverlays: [NovaTextOverlay] = [
        NovaTextOverlay(text: "NOVACUT STUDIO", startTimeSec: 0.2, endTimeSec: 3.5)
    ]
    @State private var selectedClipID: UUID?
    @State private var selectedEffectCategory: NovaEffectCategory = .cinematic
    @State private var selectedSfxCategory: NovaSfxCategory = .cinematic
    @State private var sfxSearchQuery: String = ""
    @State private var playheadSec: Double = 0.0
    @State private var isPlaying: Bool = false
    @State private var showExportSheet: Bool = false
    @State private var showEffectsStoreSheet: Bool = false
    @State private var selectedPhotoItem: PhotosPickerItem?

    // NOVACUT Luxury Palette (#08090D, #12141B, #D6B46A Gold, #9B75FF Purple)
    private let novaObsidian = Color(red: 0.031, green: 0.035, blue: 0.051)
    private let novaCharcoal = Color(red: 0.071, green: 0.078, blue: 0.106)
    private let novaCyan = Color(red: 0.839, green: 0.706, blue: 0.416) // NovaGold #D6B46A
    private let novaMagenta = Color(red: 0.608, green: 0.459, blue: 1.0) // NovaPurple #9B75FF
    private let novaAmber = Color(red: 0.922, green: 0.824, blue: 0.596)

    private let sfxCatalog: [NovaSoundEffectItem] = [
        NovaSoundEffectItem(id: "sfx_impact", title: "Cinematic Impact", category: .cinematic, durationSec: 2.4),
        NovaSoundEffectItem(id: "sfx_boom", title: "Deep Boom", category: .cinematic, durationSec: 2.8),
        NovaSoundEffectItem(id: "sfx_whoosh", title: "Cinematic Whoosh", category: .transitions, durationSec: 1.4),
        NovaSoundEffectItem(id: "sfx_pop", title: "Pop", category: .socialMedia, durationSec: 0.6),
        NovaSoundEffectItem(id: "sfx_shutter", title: "Camera Shutter", category: .socialMedia, durationSec: 0.7),
        NovaSoundEffectItem(id: "sfx_thunder", title: "Thunder", category: .action, durationSec: 3.2),
        NovaSoundEffectItem(id: "sfx_ocean", title: "Ocean Waves", category: .nature, durationSec: 4.0)
    ]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // 1. Top Studio Header with 100% Free Unlocked Badge, Effects Store & Export
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(projectTitle)
                            .font(.headline.bold())
                            .foregroundColor(.white)
                        Text("100% FREE UNLOCKED • \(aspectRatio.rawValue) • \(exportResolution.rawValue) • \(effectLayers.count) FX")
                            .font(.caption2)
                            .foregroundColor(novaCyan)
                    }
                    Spacer()

                    Text("ALL FREE")
                        .font(.caption2.bold())
                        .padding(.horizontal, 8)
                        .padding(.vertical, 5)
                        .background(novaCharcoal)
                        .foregroundColor(novaCyan)
                        .clipShape(Capsule())

                    Button {
                        showEffectsStoreSheet = true
                    } label: {
                        Label("FX Store", systemImage: "sparkles")
                            .font(.caption.bold())
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(novaCharcoal)
                            .foregroundColor(novaAmber)
                            .clipShape(Capsule())
                    }

                    PhotosPicker(selection: $selectedPhotoItem, matching: .videos) {
                        Label("Import", systemImage: "photo.badge.plus")
                            .font(.caption.bold())
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(novaCharcoal)
                            .foregroundColor(novaCyan)
                            .clipShape(Capsule())
                    }

                    Button {
                        showExportSheet = true
                    } label: {
                        Label("Export", systemImage: "square.and.arrow.up")
                            .font(.caption.bold())
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(novaCyan)
                            .foregroundColor(.black)
                            .clipShape(Capsule())
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(novaCharcoal)

                // 2. Upper Screen Video Preview Stage with Live Stacked FX Badge
                ZStack {
                    novaObsidian
                    RoundedRectangle(cornerRadius: 16)
                        .fill(
                            LinearGradient(
                                colors: [Color(red: 0.06, green: 0.09, blue: 0.22), Color(red: 0.22, green: 0.05, blue: 0.32)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(maxWidth: 230, maxHeight: 300)
                        .overlay(
                            VStack {
                                HStack {
                                    ForEach(effectLayers.filter { playheadSec >= $0.startTimeSec && playheadSec <= $0.endTimeSec }) { fx in
                                        Text(fx.effectType.rawValue)
                                            .font(.caption2.bold())
                                            .foregroundColor(novaMagenta)
                                            .padding(.horizontal, 8)
                                            .padding(.vertical, 3)
                                            .background(Color.black.opacity(0.75))
                                            .cornerRadius(6)
                                    }
                                    Spacer()
                                }
                                .padding(8)
                                Spacer()
                                ForEach(textOverlays.filter { playheadSec >= $0.startTimeSec && playheadSec <= $0.endTimeSec }) { item in
                                    Text(item.text)
                                        .font(.system(size: 16, weight: .heavy, design: .rounded))
                                        .foregroundColor(novaCyan)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 4)
                                        .background(Color.black.opacity(0.75))
                                        .cornerRadius(8)
                                }
                                .padding(.bottom, 16)
                            }
                        )
                }
                .frame(maxHeight: 310)

                // 3. Multi-Track Timeline (Video, FX Stack, Text, SFX/Audio)
                ScrollView(.horizontal, showsIndicators: true) {
                    VStack(alignment: .leading, spacing: 6) {
                        // FX Timeline Track
                        HStack(spacing: 4) {
                            Text("FX")
                                .font(.caption2.bold())
                                .foregroundColor(novaMagenta)
                                .frame(width: 32)
                            ForEach(effectLayers) { fx in
                                Text("\(fx.effectType.rawValue) (\(Int(fx.intensity * 100))%)")
                                    .font(.caption2.bold())
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 8)
                                    .frame(height: 24)
                                    .background(novaMagenta.opacity(0.5))
                                    .cornerRadius(6)
                            }
                        }
                        // Video Track
                        HStack(spacing: 4) {
                            Text("VID")
                                .font(.caption2.bold())
                                .foregroundColor(novaCyan)
                                .frame(width: 32)
                            ForEach(clips) { clip in
                                Text("\(clip.title) • \(clip.transitionIn.rawValue)")
                                    .font(.caption2.bold())
                                    .foregroundColor(.white)
                                    .frame(width: CGFloat(clip.effectiveDurationSec * 45.0), height: 32)
                                    .background(selectedClipID == clip.id ? novaCyan.opacity(0.45) : Color.blue.opacity(0.35))
                                    .cornerRadius(8)
                                    .onTapGesture { selectedClipID = clip.id }
                            }
                        }
                        // SFX & Audio Track
                        HStack(spacing: 4) {
                            Text("SFX")
                                .font(.caption2.bold())
                                .foregroundColor(novaAmber)
                                .frame(width: 32)
                            ForEach(audioTracks) { track in
                                Text("\(track.title) (\(String(format: "%.1fx", track.speed)))")
                                    .font(.caption2)
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 8)
                                    .frame(height: 24)
                                    .background(novaAmber.opacity(0.5))
                                    .cornerRadius(6)
                            }
                        }
                    }
                    .padding(12)
                }
                .frame(height: 115)
                .background(novaObsidian)

                // 4. Video Effects & SFX Studio Browser
                VStack(alignment: .leading, spacing: 8) {
                    Picker("Category", selection: $selectedEffectCategory) {
                        ForEach(NovaEffectCategory.allCases) { cat in
                            Text(cat.rawValue).tag(cat)
                        }
                    }
                    .pickerStyle(.segmented)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(NovaVideoEffectType.allCases.filter { $0.category == selectedEffectCategory }) { effect in
                                Button {
                                    effectLayers.append(
                                        NovaVideoEffectLayer(
                                            effectType: effect,
                                            startTimeSec: playheadSec,
                                            endTimeSec: playheadSec + 2.5,
                                            intensity: 0.85,
                                            stackOrder: effectLayers.count
                                        )
                                    )
                                } label: {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(effect.category.rawValue)
                                            .font(.caption2)
                                            .foregroundColor(novaMagenta)
                                        Text(effect.rawValue)
                                            .font(.caption.bold())
                                            .foregroundColor(.white)
                                    }
                                    .padding(10)
                                    .background(novaCharcoal)
                                    .cornerRadius(10)
                                }
                            }
                        }
                    }
                }
                .padding(12)
                .background(novaCharcoal)

                Spacer()
            }
            .background(novaObsidian.ignoresSafeArea())
            .sheet(isPresented: $showExportSheet) {
                VStack(spacing: 20) {
                    Text("Export Master MP4 (AVFoundation)")
                        .font(.title3.bold())
                    Picker("Resolution", selection: $exportResolution) {
                        ForEach(NovaExportResolution.allCases) { res in
                            Text(res.rawValue).tag(res)
                        }
                    }
                    .pickerStyle(.segmented)

                    if avEngine.isExporting {
                        ProgressView(value: avEngine.exportProgress)
                        Text(avEngine.statusMessage).font(.caption)
                    } else {
                        Button("Start Hardware Export") {
                            Task {
                                _ = try? await avEngine.exportProject(
                                    clips: clips,
                                    effectLayers: effectLayers,
                                    audioTracks: audioTracks,
                                    aspectRatio: aspectRatio,
                                    resolution: exportResolution
                                )
                            }
                        }
                        .buttonStyle(.borderedProminent)
                    }
                }
                .padding(24)
                .presentationDetents([.medium])
            }
        }
        .preferredColorScheme(.dark)
    }
}
