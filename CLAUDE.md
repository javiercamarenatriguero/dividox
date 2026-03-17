# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Dividox is a Kotlin Multiplatform (KMP) application built with Compose Multiplatform, targeting Android, iOS, and Desktop (JVM). Package: `com.akole.dividox`.

## Build Commands

```bash
# Desktop
./gradlew :composeApp:run                  # Run desktop app

# Android
./gradlew :composeApp:assembleDebug        # Build debug APK

# iOS - open iosApp/ in Xcode and run from there

# Tests
./gradlew test                             # Run all tests
./gradlew :composeApp:jvmTest              # Run JVM tests only

# Checks
./gradlew build                            # Full build (all targets)
```

## Architecture

Single-module structure with all platform code in `:composeApp`.

- **`composeApp/src/commonMain/`** — Shared Kotlin code and Compose UI (App.kt is the main composable)
- **`composeApp/src/androidMain/`** — Android platform implementation (MainActivity entry point)
- **`composeApp/src/jvmMain/`** — Desktop platform implementation (main.kt entry point)
- **`composeApp/src/iosMain/`** — iOS platform implementation (MainViewController)
- **`composeApp/src/commonTest/`** — Shared tests using kotlin.test
- **`iosApp/`** — Native iOS project (SwiftUI shell wrapping the Compose framework)

Platform-specific code uses the Kotlin **expect/actual** pattern (see `Platform.kt` in commonMain and corresponding actuals in each platform source set).

## Key Tech

- Kotlin 2.3.20, Compose Multiplatform 1.10.2, AGP 9.1.0, Gradle 9.3.1
- Material Design 3 theming
- Version catalog at `gradle/libs.versions.toml`
- Android: minSdk 31, targetSdk 36
