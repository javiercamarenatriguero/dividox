# Task: TK-025 · feature:favorites — SecurityCard + Scaffold + MVI + Navigation

## Description

Add the shared `SecurityCard` composable to `:common:ui-resources`, scaffold `:feature:favorites`, implement the Favorites screen MVI, and wire `FavoritesRoute` in `mainGraph`.

**User Stories:** DVX-US-023 · DVX-US-024 · DVX-US-025
**PRD:** PRD-06
**ADRs:** ADR-010, ADR-011
**Depends on:** TK-024
**Blocks:** TK-026, TK-029
**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch** `feature/DVX-TK-025-feature-favorites` — `skill: manage-git-flow`

### Phase 2: Shared SecurityCard Component
- [ ] **Create `SecurityCard` composable** in `:common:ui-resources`
  - Props: `logo, ticker, companyName, price, dailyChange, dailyChangePercent: Double, isFavourite, onFavouriteToggle, onClick, isInPortfolio: Boolean`
  - Semantic colour: green for positive `dailyChangePercent`, red for negative
  - Heart icon: solid (favourited) / outlined (not favourited); optional briefcase badge when `isInPortfolio`
  - Location: `common/ui-resources/src/commonMain/kotlin/.../components/SecurityCard.kt`
  - **Verify:** `./gradlew :common:ui-resources:compileKotlinJvm`
  - **Commit:** `DVX-TK-025 Add SecurityCard shared component`

### Phase 3: Scaffold
- [ ] **Scaffold `:feature:favorites`**
  - `feature/favorites/build.gradle.kts` — `dividox.kmp.library` + `dividox.compose.multiplatform` + `dividox.kmp.ios` + `dividox.kmp.test`
  - `include(":feature:favorites")` in `settings.gradle.kts`
  - **Verify:** `./gradlew :feature:favorites:compileKotlinJvm`
  - **Commit:** `DVX-TK-025 Scaffold feature:favorites module`

### Phase 4: MVI
- [ ] **`FavoritesContract`** — State: `entries: List<EnrichedWatchlistEntry>, searchQuery, isLoading, error` · Event: `SearchQueryChanged, FavouriteToggled(ticker), SecurityClicked(ticker), BackClicked` · Effect: `NavigateToSecurity, NavigateBack`
- [ ] **`FavoritesViewModel`** + unit tests — `GetEnrichedWatchlistUseCase` (live), `RemoveFromWatchlistUseCase` on toggle, client-side search (ticker + name, case-insensitive)
  - **Verify:** `./gradlew :feature:favorites:jvmTest`
  - **Commit:** `DVX-TK-025 Add FavoritesViewModel with unit tests`

- [ ] **`FavoritesScreen`** — back + "Favorites" title · search bar · `SecurityCard` list · empty state · "Prices delayed 15 min" disclaimer
  - **Commit:** `DVX-TK-025 Add FavoritesScreen UI`

### Phase 5: Navigation + Koin
- [ ] **Wire `FavoritesRoute` in `mainGraph`** — entry point is `onFavoritesClicked` from `SettingsScreen` (not a bottom nav tab) · `onSecurityClick` → `SecurityDetailRoute` · `onBack` → `popBackStack` (returns to Settings)
- [ ] **Register `:feature:favorites` Koin module** in `App.kt` startKoin
  - **Verify:** `./gradlew :composeApp:assembleDebug`
  - **Commit:** `DVX-TK-025 Wire FavoritesRoute (entry from Settings) and register favorites Koin module`

### Phase 6: Testing & Quality
- [ ] `./gradlew test` + `./gradlew detekt`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking
**Total Tasks:** 8 **Completed:** 0 **Remaining:** 8

---

## Notes
- `SecurityCard` in `:common:ui-resources` is reusable by Dashboard, Favorites, and Search without cross-feature imports
