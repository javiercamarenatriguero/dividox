# TK-017 — Integration Security Module

## Description

Scaffold `:integration:security`, define enriched models (`SecurityHolding`, `PortfolioSummary`, `EnrichedWatchlistEntry`, `SecurityDetail`), implement integration use cases that combine portfolio + market + watchlist component use cases, register Koin module.

**Branch**: `feature/DVX-TK-017-integration-security` (off `feature/DVX-TK-016-component-watchlist`)

---

## Subtasks

- [x] Phase 1 — Git branch created off `feature/DVX-TK-016-component-watchlist`
- [x] Phase 2 — Scaffold `:integration:security` module
  - [x] Create `integration/security/build.gradle.kts`
  - [x] Add `include(":integration:security")` to `settings.gradle.kts`
  - [x] Verify: `./gradlew :integration:security:compileKotlinJvm`
  - [x] Commit: `DVX-TK-017 Scaffold integration:security module`
- [x] Phase 3 — Enriched Models + Use Cases (TDD)
  - [x] Define enriched domain models
  - [x] Write tests (fakes + test cases) before implementation
  - [x] Implement use cases
  - [x] Verify: `./gradlew :integration:security:jvmTest` (27 tests, 0 failures)
  - [x] Commit models: `DVX-TK-017 Add integration:security enriched models`
  - [x] Commit use cases + tests: `DVX-TK-017 Add integration:security use cases with tests`
- [x] Phase 4 — Koin module
  - [x] Create `SecurityIntegrationModule.kt`
  - [x] Add `securityIntegrationModule` to `KoinInitializer.kt`
  - [x] Add `projects.integration.security` to `composeApp/build.gradle.kts`
  - [x] Verify: `./gradlew :composeApp:assembleDebug` — BUILD SUCCESSFUL
  - [x] Commit: `DVX-TK-017 Register security integration Koin module`
- [x] Phase 5 — Quality gate
  - [x] `./gradlew :integration:security:jvmTest` — 27 tests, 0 failures
  - [x] `./gradlew detekt` — BUILD SUCCESSFUL
  - Note: `./gradlew test` shows pre-existing failures in `:component:market:testDebugUnitTest` from TK-016 branch (not introduced by TK-017)
