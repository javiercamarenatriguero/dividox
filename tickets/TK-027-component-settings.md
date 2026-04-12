# Task: TK-027 · component:settings — Scaffold + DataStore + Domain + Data

## Description

Scaffold `:component:settings`, add DataStore dependencies and create the `DataStore<Preferences>` expect/actual factory, define the `UserSettings` domain model and `SettingsRepository` with TDD, and implement `SettingsLocalDataSource`, `SettingsRepositoryImpl`, and the Koin module.

**User Stories:** DVX-US-027 · DVX-US-028 · DVX-US-029
**ADRs:** ADR-005, ADR-012
**Depends on:** TK-026
**Blocks:** TK-028
**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch** `feature/DVX-TK-027-component-settings` — `skill: manage-git-flow`

### Phase 2: Scaffold + DataStore Setup
- [ ] **Scaffold `:component:settings`**
  - `component/settings/build.gradle.kts` — `dividox.kmp.library` + `dividox.kmp.test`
  - `include(":component:settings")` in `settings.gradle.kts`
  - **Commit:** `DVX-TK-027 Scaffold component:settings module`

- [ ] **Add DataStore dependency** — `androidx.datastore:datastore-preferences-core` (KMP), add to `libs.versions.toml`
  - **Commit:** `DVX-TK-027 Add DataStore dependency to component:settings`

- [ ] **Create DataStore expect/actual factory**
  - `commonMain`: `expect fun createDataStore(producePath: () -> String): DataStore<Preferences>`
  - `androidMain`: context-based path `context.filesDir.resolve("settings.preferences_pb")`
  - `iosMain`: `NSHomeDirectory() + "/settings.preferences_pb"`
  - `jvmMain`: `System.getProperty("user.home") + "/.dividox/settings.preferences_pb"`
  - Location: `component/settings/src/*/kotlin/.../data/datasource/local/DataStoreFactory.kt`
  - **Verify:** `./gradlew :component:settings:compileKotlinJvm`
  - **Commit:** `DVX-TK-027 Add DataStore expect/actual factory`

### Phase 3: Domain Layer (TDD)
- [ ] **`UserSettings(baseCurrency: String = "USD", biometricEnabled: Boolean = true, notificationsEnabled: Boolean = true)`**
- [ ] **`SettingsRepository`:** `getSettings(): Flow<UserSettings>`, `updateCurrency`, `updateBiometricEnabled`, `updateNotificationsEnabled`
- [ ] **Use cases + tests:** `GetSettingsUseCase`, `UpdateCurrencyUseCase`, `UpdateBiometricLockUseCase`, `UpdateNotificationsUseCase`
  - **Verify:** `./gradlew :component:settings:jvmTest`
  - **Commit:** `DVX-TK-027 Add settings domain layer with tests`

### Phase 4: Data Layer
- [ ] **`SettingsLocalDataSource`** — keys: `BASE_CURRENCY`, `BIOMETRIC_ENABLED`, `NOTIFICATIONS_ENABLED`; maps `Preferences` → `UserSettings` with defaults
- [ ] **`SettingsRepositoryImpl`** (TDD, mock DataStore)
  - **Verify:** `./gradlew :component:settings:jvmTest`
- [ ] **`SettingsModule.kt`** + add to `App.kt` startKoin
  - **Commit:** `DVX-TK-027 Implement settings data layer and Koin module`

### Phase 5: Testing & Quality
- [ ] `./gradlew test` + `./gradlew detekt`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking
**Total Tasks:** 9 **Completed:** 0 **Remaining:** 9
