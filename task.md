# TK-024: Security Detail Screen (Analyst View)

## Description

Implement the Security Detail screen (`:feature:analysis` module) with comprehensive security analysis including price charts, dividend metrics, fundamentals, and action buttons. The screen displays detailed financial data for a selected security with pull-to-refresh capability and responsive state management.

## Specification

**Screen Layout** (top to bottom):
- Header: back arrow + centered ticker symbol + heart icon (favorite toggle)
- Price card: current price + % change with direction icon + "Refreshed X min ago" + pull-to-refresh
- Price line chart + period selector [1D|1W|1M|YTD|1Y|ALL]
- Dividend Metrics 2x2 grid: Yield, Annual Payout, Payout Ratio, 5Y Growth
- Dividend Growth bar chart (10 years with value/% toggle)
- Fundamentals section: Market Cap, P/E Ratio, Ex-Div Date
- Full-width CTA: "Add Security" / "Edit Holding" (context-aware)
- Disclaimer footer: "Prices delayed 15 minutes"

**Design Reference**: [Stitch Project](https://stitch.withgoogle.com/projects/10568397103146599411)

**Dependencies**:
- ✅ GetSecurityDetailUseCase (TK-017)
- ✅ GetPriceHistoryUseCase (TK-015)
- ✅ AddToWatchlistUseCase
- ✅ RemoveFromWatchlistUseCase

**Blocks**: TK-025

## Subtasks

- [x] **Module Scaffolding**: Create `:feature:analysis` module with build.gradle.kts, add to settings.gradle.kts
- [ ] **Chart Components**: Create reusable LineChart component in `:common:ui-resources` for price history
- [x] **MVI Contract**: Implement SecurityDetailContract (SecurityDetailViewState, SecurityDetailViewEvent, SecurityDetailSideEffect)
- [x] **ViewModel**: Implement SecurityDetailViewModel with state management, refresh logic, and favorite toggle
- [x] **Screen UI**: Build SecurityDetailScreen composable with all sections (price card, charts, metrics, fundamentals, CTA)
- [x] **Navigation**: Create SecurityDetailRoute and wire into RootNavGraph
- [x] **DI Registration**: Register ViewModel and use cases in :composeApp/di/ViewModelModule.kt
- [x] **Unit Tests**: Write SecurityDetailViewModelTest with GIVEN/WHEN/THEN coverage
- [x] **Compilation Verification**: Ensure builds pass for all targets (jvmTest, assembleDebug, etc.)

## Status

✅ **CORE IMPLEMENTATION COMPLETE** - TK-024 scaffolding and MVP functionality implemented. All core build files compile successfully.

**Note on Chart Components**: LineChart is deferred to TK-025 as a separate enhancement task since it requires careful coordination with the charting library and data formatting.

## Files Created

- `/Users/camarej4/code/git/dividox/feature/analysis/build.gradle.kts` - Module Gradle configuration
- `/Users/camarej4/code/git/dividox/feature/analysis/src/commonMain/kotlin/com/akole/dividox/feature/analysis/SecurityDetailContract.kt` - MVI contract
- `/Users/camarej4/code/git/dividox/feature/analysis/src/commonMain/kotlin/com/akole/dividox/feature/analysis/SecurityDetailViewModel.kt` - ViewModel with state management
- `/Users/camarej4/code/git/dividox/feature/analysis/src/commonMain/kotlin/com/akole/dividox/feature/analysis/SecurityDetailScreen.kt` - Compose UI implementation
- `/Users/camarej4/code/git/dividox/feature/analysis/src/commonTest/kotlin/com/akole/dividox/feature/analysis/SecurityDetailViewModelTest.kt` - Unit tests
- `/Users/camarej4/code/git/dividox/composeApp/src/commonMain/kotlin/com/akole/dividox/navigation/RootNavGraph.kt` (updated) - Added securityDetailScreenNode

## Files Modified

- `/Users/camarej4/code/git/dividox/settings.gradle.kts` - Added `:feature:analysis` module
- `/Users/camarej4/code/git/dividox/composeApp/build.gradle.kts` - Added `:feature:analysis` dependency
- `/Users/camarej4/code/git/dividox/composeApp/src/commonMain/kotlin/com/akole/dividox/di/ViewModelModule.kt` - Registered SecurityDetailViewModel

## Architecture

- **MVI Pattern**: Full MVI implementation with Contract, ViewModel, and Screen
- **State Management**: Uses Flow-based state with mvi() delegate
- **DI**: All dependencies injected via Koin with parametrized ticker
- **Navigation**: Type-safe navigation via Kotlin serialization routes
- **Multi-platform**: Code compiles for Android, iOS, and Desktop targets
