# TK-016: Component Watchlist

## Description

Scaffold `:component:watchlist`, define watchlist domain and use cases (TDD), build Firestore data layer, register Koin module.

Package: `com.akole.dividox.watchlist`

## Subtasks

### Phase 1 — Git Branch
- [x] Create branch `feature/DVX-TK-016-component-watchlist`

### Phase 2 — Scaffold Module
- [x] Create `component/watchlist/build.gradle.kts`
- [x] Add `include(":component:watchlist")` in `settings.gradle.kts`
- [ ] Verify: `./gradlew :component:watchlist:compileKotlinJvm`
- [ ] Commit: `DVX-TK-016 Scaffold component:watchlist module`

### Phase 3 — Domain Layer (TDD first)
- [ ] Write failing tests for all use cases
- [ ] Implement `WatchlistEntry` domain model
- [ ] Implement `WatchlistRepository` interface
- [ ] Implement `GetWatchlistUseCase`
- [ ] Implement `AddToWatchlistUseCase`
- [ ] Implement `RemoveFromWatchlistUseCase`
- [ ] Implement `IsInWatchlistUseCase`
- [ ] Verify: `./gradlew :component:watchlist:jvmTest`
- [ ] Commit: `DVX-TK-016 Add watchlist domain layer with tests`

### Phase 4 — Data Layer
- [ ] Implement `WatchlistDataSource` interface
- [ ] Implement `WatchlistFirestoreDataSource`
- [ ] Implement `WatchlistRepositoryImpl`
- [ ] Write TDD tests for `WatchlistRepositoryImpl`
- [ ] Create `WatchlistModule.kt` in `composeApp/di/`
- [ ] Register `WatchlistModule` in `KoinInitializer`
- [ ] Verify: `./gradlew :component:watchlist:jvmTest`
- [ ] Commit: `DVX-TK-016 Implement watchlist data layer and Koin module`

### Phase 5 — Quality Gate
- [ ] `./gradlew test`
- [ ] `./gradlew detekt`
- [ ] Fix any issues
- [ ] Create PR
