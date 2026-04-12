# Task: TK-016 · component:watchlist — Scaffold + Domain + Data

## Description

Scaffold `:component:watchlist`, define the watchlist domain and use cases with TDD, then build the Firestore data layer and register the Koin module.

**User Stories:** DVX-US-009 · DVX-US-023 · DVX-US-024 · DVX-US-025
**ADRs:** ADR-005
**Depends on:** TK-013
**Blocks:** TK-017
**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch** `feature/DVX-TK-016-component-watchlist` — `skill: manage-git-flow`

### Phase 2: Scaffold
- [ ] **Scaffold `:component:watchlist`**
  - `component/watchlist/build.gradle.kts` — `dividox.kmp.library` + `dividox.kmp.test`
  - `include(":component:watchlist")` in `settings.gradle.kts`
  - **Verify:** `./gradlew :component:watchlist:compileKotlinJvm`
  - **Commit:** `DVX-TK-016 Scaffold component:watchlist module`

### Phase 3: Domain Layer (TDD)
- [ ] **`WatchlistEntry(tickerId: String, addedAt: Instant)`**
- [ ] **`WatchlistRepository`:** `getWatchlist(): Flow<List<WatchlistEntry>>`, `addToWatchlist`, `removeFromWatchlist`, `isInWatchlist(ticker): Flow<Boolean>`
- [ ] **Use cases + tests:** `GetWatchlistUseCase`, `AddToWatchlistUseCase`, `RemoveFromWatchlistUseCase`, `IsInWatchlistUseCase`
  - **Verify:** `./gradlew :component:watchlist:jvmTest`
  - **Commit:** `DVX-TK-016 Add watchlist domain layer with tests`

### Phase 4: Data Layer
- [ ] **`WatchlistFirestoreDataSource`** — collection `users/{uid}/watchlist`
- [ ] **`WatchlistRepositoryImpl`** (TDD, mock data source)
  - **Verify:** `./gradlew :component:watchlist:jvmTest`
- [ ] **`WatchlistModule.kt`** + add to `App.kt` startKoin
  - **Commit:** `DVX-TK-016 Implement watchlist data layer and Koin module`

### Phase 5: Testing & Quality
- [ ] `./gradlew test` + `./gradlew detekt`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking
**Total Tasks:** 6 **Completed:** 0 **Remaining:** 6

---

## Notes
- `WatchlistRepository` is the single source of truth for favourite state across all screens — never duplicate this state in a ViewModel
