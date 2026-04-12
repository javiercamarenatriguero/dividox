# Task: TK-018 · feature:dashboard — Scaffold + MVI + Navigation

## Description

Scaffold `:feature:dashboard`, implement the full Dashboard MVI (DashboardContract, DashboardViewModel, DashboardScreen), and wire `DashboardRoute` in `mainGraph`.

**User Stories:** DVX-US-005 · DVX-US-006 · DVX-US-007 · DVX-US-008 · DVX-US-009 · DVX-US-010
**PRD:** PRD-02
**ADRs:** ADR-005, ADR-006, ADR-007, ADR-010, ADR-011
**Depends on:** TK-017
**Blocks:** TK-019
**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch** `feature/DVX-TK-018-feature-dashboard` — `skill: manage-git-flow`

### Phase 2: Scaffold
- [ ] **Scaffold `:feature:dashboard`**
  - `feature/dashboard/build.gradle.kts` — `dividox.kmp.library` + `dividox.compose.multiplatform` + `dividox.kmp.ios` + `dividox.kmp.test`
  - `include(":feature:dashboard")` in `settings.gradle.kts`
  - **Verify:** `./gradlew :feature:dashboard:compileKotlinJvm`
  - **Commit:** `DVX-TK-018 Scaffold feature:dashboard module`

### Phase 3: MVI
- [ ] **`DashboardContract`**
  - State: `isLoading, summary: PortfolioSummary?, gainers, losers, events, watchlist, discovery, currency, selectedPeriod, error`
  - Event: `PeriodSelected, CurrencyToggled, FavouriteToggled(ticker), SecurityClicked(ticker), AddSecurityClicked(ticker), ViewAllFavouritesClicked`
  - Effect: `NavigateToSecurity(ticker), NavigateToFavorites, NavigateToAddHolding(ticker)`
  - **Commit:** `DVX-TK-018 Add DashboardContract`

- [ ] **`DashboardViewModel`** + unit tests
  - `GetPortfolioSummaryUseCase`, `GetWatchlistUseCase` on init; `RemoveFromWatchlistUseCase` on FavouriteToggled
  - Currency conversion using spot rate from `MarketRepository`
  - **Verify:** `./gradlew :feature:dashboard:jvmTest`
  - **Commit:** `DVX-TK-018 Add DashboardViewModel with unit tests`

- [ ] **`DashboardScreen`**
  - Header + USD/EUR toggle · Period selector `[1D|1W|1M|1Y|YTD|ALL]` + 4 metric cards
  - Top 3 Gainers / Losers (hide if portfolio empty → "Add your first holding" CTA)
  - Upcoming Events feed · Favorites Monitor (2 entries + VIEW ALL) · Market Intelligence carousel (+ per card)
  - "Prices delayed 15 minutes" disclaimer
  - **Commit:** `DVX-TK-018 Add DashboardScreen UI`

### Phase 4: Navigation + Koin
- [ ] **Wire `DashboardRoute` in `mainGraph`**
  - `onSecurityClick` → `SecurityDetailRoute`, `onViewAllFavorites` → `FavoritesRoute`, `onAddSecurity` → `AddHoldingRoute`
- [ ] **Register `:feature:dashboard` Koin module** in `App.kt` startKoin
  - **Verify:** `./gradlew :composeApp:assembleDebug`
  - **Commit:** `DVX-TK-018 Wire DashboardRoute and register dashboard Koin module`

### Phase 5: Testing & Quality
- [ ] `./gradlew test` + `./gradlew detekt`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking
**Total Tasks:** 7 **Completed:** 0 **Remaining:** 7

---

## Notes
- Period selector recalculates client-side — no network call on period change
- Currency toggle will call `UpdateCurrencyUseCase` after TK-029 is done; use local state until then
