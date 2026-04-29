---
ticket: TK-027
feature: common:settings
branch: feature/TK-027-common-settings
---

# TK-027 common:settings — DataStore Preferences + Dashboard Integration

## Description

Create `:common:settings` KMP module with DataStore Preferences for persisting
app-level settings (currency preference). Integrate with `:feature:dashboard` to
replace the ephemeral `showInEur` toggle with a durable `currency: Currency`
value backed by DataStore.

**Key Requirements:**
- DataStore KMP expect/actual factory (Android, iOS, JVM)
- `AppSettings` domain model with `currency: Currency`
- `ObserveAppSettingsUseCase` / `SetCurrencyUseCase`
- `AppSettingsDataStoreImpl` mapping Preferences ↔ domain model
- Koin `settingsModule` co-located in `common/settings/di/`
- Dashboard ViewModel observes settings, writes on toggle

**Status:** In Progress

---

## Subtasks

### Phase 1: Architecture & Setup
- [x] **Create Git Branch** `feature/TK-027-common-settings`

### Phase 2: Version Catalog + Module Scaffold
- [ ] **Add DataStore to `libs.versions.toml`**
  - `datastore = "1.1.4"` version + `datastore-preferences-core` library
  - **Commit:** `DVX-TK-027 Add DataStore to version catalog`

- [ ] **Scaffold `common/settings/build.gradle.kts`**
  - Plugins: `dividox.kmp.library`, `dividox.kmp.ios`, `dividox.kmp.test`, `dividox.detekt`
  - `include(":common:settings")` in `settings.gradle.kts`
  - **Verify:** `./gradlew :common:settings:compileKotlinJvm`
  - **Commit:** `DVX-TK-027 Scaffold common:settings module`

### Phase 3: Domain Layer
- [ ] **`AppSettings.kt`** — `data class AppSettings(val currency: Currency = Currency.EUR)`
- [ ] **`AppSettingsDataStore.kt`** — interface with `observe(): Flow<AppSettings>` and `setCurrency()`
- [ ] **`ObserveAppSettingsUseCase.kt`** — delegates to datastore
- [ ] **`SetCurrencyUseCase.kt`** — delegates to datastore
  - **Commit:** `DVX-TK-027 Add settings domain layer`

### Phase 4: Data Layer
- [ ] **`DataStoreFactory.kt`** — `expect fun dataStorePath()` + `createDataStore()` in commonMain
- [ ] **`DataStoreFactory.android.kt`** — actual uses `KoinPlatform` to get `Context`
- [ ] **`DataStoreFactory.ios.kt`** — actual uses NSDocumentDirectory
- [ ] **`DataStoreFactory.jvm.kt`** — actual uses `user.home`
- [ ] **`AppSettingsDataStoreImpl.kt`** — maps `Preferences` ↔ `AppSettings`
  - **Verify:** `./gradlew :common:settings:compileKotlinJvm`
  - **Commit:** `DVX-TK-027 Add settings data layer with DataStore impl`

### Phase 5: DI
- [ ] **`SettingsModule.kt`** in `common/settings/di/`
- [ ] **Wire `settingsModule` in `KoinInitializer.kt`**
  - **Commit:** `DVX-TK-027 Add settings Koin module`

### Phase 6: Dashboard Integration
- [ ] **`DashboardContract.kt`** — replace `showInEur: Boolean` with `currency: Currency`
- [ ] **`DashboardViewModel.kt`** — inject `ObserveAppSettingsUseCase` + `SetCurrencyUseCase`, observe settings
- [ ] **`DashboardScreen.kt`** — update `CurrencyToggleButton` and `MetricsBlock` to use `currency`
- [ ] **`feature/dashboard/build.gradle.kts`** — add `:common:settings` dependency
  - **Verify:** `./gradlew :feature:dashboard:compileKotlinJvm`
  - **Commit:** `DVX-TK-027 Integrate settings into Dashboard feature`

### Phase 7: Testing & Quality
- [ ] `./gradlew :common:settings:jvmTest`
- [ ] `./gradlew :feature:dashboard:jvmTest`
- [ ] `./gradlew detekt`

---

## Progress Tracking
**Total Tasks:** 14 **Completed:** 1 **Remaining:** 13
