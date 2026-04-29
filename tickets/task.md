---
ticket: TK-018
feature: Dashboard
branch: feature/DVX-TK-018-feature-dashboard
---

# TK-018 Dashboard — Task Tracker

## Phase 1: Git branch
- [x] Create branch `feature/DVX-TK-018-feature-dashboard`

## Phase 2: Scaffold
- [ ] Create `feature/dashboard/build.gradle.kts`
- [ ] Add `include(":feature:dashboard")` to `settings.gradle.kts`
- [ ] Verify: `./gradlew :feature:dashboard:compileKotlinJvm`
- [ ] Commit: `DVX-TK-018 Scaffold feature:dashboard module`

## Phase 3: MVI
- [ ] Write `DashboardContract.kt` (State / Event / Effect)
- [ ] Write `DashboardViewModelTest.kt` (TDD fakes + test cases)
- [ ] Write `DashboardViewModel.kt`
- [ ] Write `DashboardScreen.kt`
- [ ] Verify: `./gradlew :feature:dashboard:jvmTest`
- [ ] Commit: `DVX-TK-018 Add DashboardContract`
- [ ] Commit: `DVX-TK-018 Add DashboardViewModel with unit tests`
- [ ] Commit: `DVX-TK-018 Add DashboardScreen UI`

## Phase 4: Navigation + Koin
- [ ] Create `DashboardNavigation.kt` with `DashboardRoute`
- [ ] Wire `dashboardScreenNode` in `RootNavGraph`
- [ ] Add `DashboardViewModel` to `ViewModelModule`
- [ ] Add `projects.feature.dashboard` dep to `composeApp/build.gradle.kts`
- [ ] Verify: `./gradlew :composeApp:assembleDebug`
- [ ] Commit: `DVX-TK-018 Wire DashboardRoute and register dashboard Koin module`

## Phase 5: Quality gate
- [ ] `./gradlew :feature:dashboard:jvmTest`
- [ ] `./gradlew test`
- [ ] `./gradlew detekt`
- [ ] Fix all issues
