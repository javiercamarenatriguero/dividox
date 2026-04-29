# TK-017 — Integration Security Module

## Description

Scaffold `:integration:security`, define enriched models (`SecurityHolding`, `PortfolioSummary`, `EnrichedWatchlistEntry`, `SecurityDetail`), implement integration use cases that combine portfolio + market + watchlist component use cases, register Koin module.

**Branch**: `feature/DVX-TK-017-integration-security` (off `feature/DVX-TK-016-component-watchlist`)

---

## Subtasks

- [x] Phase 1 — Git branch created off `feature/DVX-TK-016-component-watchlist`
- [ ] Phase 2 — Scaffold `:integration:security` module
  - [ ] Create `integration/security/build.gradle.kts`
  - [ ] Add `include(":integration:security")` to `settings.gradle.kts`
  - [ ] Verify: `./gradlew :integration:security:compileKotlinJvm`
  - [ ] Commit: `DVX-TK-017 Scaffold integration:security module`
- [ ] Phase 3 — Enriched Models + Use Cases (TDD)
  - [ ] Define enriched domain models
  - [ ] Write tests (fakes + test cases) before implementation
  - [ ] Implement use cases
  - [ ] Verify: `./gradlew :integration:security:jvmTest`
  - [ ] Commit models: `DVX-TK-017 Add integration:security enriched models`
  - [ ] Commit use cases + tests: `DVX-TK-017 Add integration:security use cases with tests`
- [ ] Phase 4 — Koin module
  - [ ] Create `SecurityIntegrationModule.kt`
  - [ ] Add `securityIntegrationModule` to `KoinInitializer.kt`
  - [ ] Add `projects.integration.security` to `composeApp/build.gradle.kts`
  - [ ] Commit: `DVX-TK-017 Register security integration Koin module`
- [ ] Phase 5 — Quality gate
  - [ ] `./gradlew :integration:security:jvmTest`
  - [ ] `./gradlew detekt`
