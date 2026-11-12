# NOVACUT — Native iOS (SwiftUI + AVFoundation) & KMP Setup Guide

This directory contains the native iOS implementation of **NOVACUT**, matching the Android (`Jetpack Compose` + `AndroidX Media3 Transformer`) studio architecture:

- `NovaCutVideoEngine.swift`: Native `AVFoundation` (`AVMutableComposition`, `AVMutableVideoComposition`, `CoreImage` `CIFilter` LUTs & Chroma Key, `AVMutableAudioMix`, and `AVAssetExportSession` 720p/1080p/4K MP4 export + `PHPhotoLibrary` gallery persistence).
- `NovaCutStudioView.swift`: Native `SwiftUI` dark studio interface with `PhotosPicker` gallery import, upper-screen video preview, frame-by-frame controls, multi-track timeline, and export sheet.

## Xcode Project Setup Instructions

1. Open **Xcode 16+** and select **File -> New -> Project -> iOS -> App**.
2. Set Product Name to `NovaCut`, Interface to **SwiftUI**, and Language to **Swift**.
3. Drag `NovaCutVideoEngine.swift` and `NovaCutStudioView.swift` into your Xcode target.
4. Add the following keys to `Info.plist` for scoped media access and voiceover recording:
   - `NSPhotoLibraryAddUsageDescription`: `"NovaCut saves your exported 4K MP4 videos to your Photos library."`
   - `NSMicrophoneUsageDescription`: `"NovaCut records voiceovers directly onto your multi-track timeline."`
5. In your `@main` App struct, set `NovaCutStudioView()` as the root window view.
