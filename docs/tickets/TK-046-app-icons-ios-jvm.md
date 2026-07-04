# TK-046 — Add Custom App Icons for iOS and JVM (Desktop)

**Type:** Technical Task
**Layer:** Platform / Resources
**Points:** 3
**Priority:** Medium
**Branch:** `feature/DVX-TK-046-app-icons-ios-jvm`

## Context

Android already has a full custom icon set with adaptive icons (foreground + background)
across all mipmap densities (`mdpi` through `xxxhdpi`) plus the `anydpi-v26` XML descriptor.

iOS and JVM/Desktop still use default placeholders:
- **iOS:** `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/` contains a single
  `app-icon-1024.png` — likely the default Xcode/Compose placeholder, not the branded
  Dividox icon.
- **JVM/Desktop:** `compose.desktop.nativeDistributions` block in `composeApp/build.gradle.kts`
  has no `iconFile` configured, so macOS `.dmg`, Windows `.msi`, and Linux `.deb` packages
  ship with the generic Compose Multiplatform default icon.

## Scope

### iOS
- [x] Generate the branded Dividox icon at 1024×1024 for the App Store.
- [x] Replace `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png`.
- [x] Update `Contents.json` if additional sizes are needed for Spotlight, Settings, etc.
- [x] Verify icon renders correctly in Xcode preview and on simulator.

### JVM / Desktop
- [x] Export branded icon in required formats:
  - macOS: `.icns` (512×512 @2x minimum)
  - Windows: `.ico` (256×256, 48×48, 32×32, 16×16)
  - Linux: `.png` (512×512)
- [x] Place icon files under `composeApp/src/jvmMain/resources/` (or similar).
- [x] Configure `nativeDistributions` in `composeApp/build.gradle.kts`:
  ```kotlin
  nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.akole.dividox"
      packageVersion = "1.0.0"

      macOS {
          iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
      }
      windows {
          iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
      }
      linux {
          iconFile.set(project.file("src/jvmMain/resources/icon.png"))
      }
  }
  ```
- [x] Verify icon shows in macOS Dock, Windows taskbar, and Linux launcher.

## Acceptance Criteria

- [x] iOS app displays the branded Dividox icon (not default) on home screen and App Store.
- [x] Desktop app displays the branded Dividox icon in Dock/taskbar/launcher.
- [x] All icon files are committed to source control (no build-time generation).
- [x] No regressions on Android icon.

## References

- Android icon source: `composeApp/src/androidMain/res/mipmap-*/`
- iOS icon: `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`
- Desktop config: `composeApp/build.gradle.kts` lines 130-140
- [Compose Desktop packaging docs](https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution)
- [Apple Human Interface Guidelines — App Icons](https://developer.apple.com/design/human-interface-guidelines/app-icons)
