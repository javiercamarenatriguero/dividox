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

# Static analysis
./gradlew detekt                           # Run Detekt on all modules

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

## Gradle Convention Plugins

Shared Gradle configuration lives in `build-logic/convention/`. **Never configure SDK versions, compile options, Compose dependencies, iOS targets, or test dependencies directly in a module's `build.gradle.kts`.** Use the convention plugins instead.

### Available plugins

| Plugin alias | ID | What it configures |
|---|---|---|
| `libs.plugins.dividox.kmp.application` | `com.akole.dividox.kmp.application` | `kotlinMultiplatform` + `androidApplication`, SDK versions, JVM 11, packaging, buildTypes |
| `libs.plugins.dividox.kmp.library` | `com.akole.dividox.kmp.library` | `kotlinMultiplatform` + `androidLibrary`, SDK versions, JVM 11, packaging, buildTypes |
| `libs.plugins.dividox.compose.multiplatform` | `com.akole.dividox.compose.multiplatform` | Compose Multiplatform + Compiler + HotReload, common Compose dependencies (runtime, foundation, material3, ui, resources, lifecycle, activity-compose) |
| `libs.plugins.dividox.kmp.ios` | `com.akole.dividox.kmp.ios` | iOS targets (iosArm64 + iosSimulatorArm64) with static framework |
| `libs.plugins.dividox.kmp.test` | `com.akole.dividox.kmp.test` | `kotlin-test` in commonTest |
| `libs.plugins.dividox.detekt` | `com.akole.dividox.detekt` | Detekt static analysis with HTML+SARIF reports, config from `config/detekt/detekt.yml` |

### Rules

- **New app module**: apply `dividox.kmp.application` + `dividox.compose.multiplatform` + `dividox.kmp.ios` + `dividox.kmp.test`. Only set `namespace`, `applicationId`, `versionCode`, `versionName` in the module.
- **New library/feature module**: apply `dividox.kmp.library` + `dividox.compose.multiplatform` + `dividox.kmp.ios` + `dividox.kmp.test`. Only set `namespace` in the module.
- **Plugin order matters**: `dividox.kmp.application` or `dividox.kmp.library` must come before `dividox.compose.multiplatform` (it depends on `KotlinMultiplatformExtension` being already configured).
- **Adding shared dependencies**: Add them to the appropriate convention plugin, not to individual modules. Module `build.gradle.kts` should only contain module-specific config (namespace, applicationId, desktop config, module-specific dependencies).
- **Version management**: All versions go in `gradle/libs.versions.toml`. Convention plugins read versions from the catalog via `libs.findVersion()` / `libs.findLibrary()`.
- **New convention plugin**: Create the class in `build-logic/convention/src/main/kotlin/`, register it in `build-logic/convention/build.gradle.kts`, and add a `dividox-*` entry in `gradle/libs.versions.toml` `[plugins]` section.

## Key Tech

- Kotlin 2.3.20, Compose Multiplatform 1.10.2, AGP 9.1.0, Gradle 9.3.1
- Material Design 3 theming
- Version catalog at `gradle/libs.versions.toml`
- Convention plugins at `build-logic/convention/`
- Android: minSdk 31, targetSdk 36
