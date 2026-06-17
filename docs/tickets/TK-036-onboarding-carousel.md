# Task: TK-036 · feature:onboarding — First-Login Onboarding Carousel

## Description

Add a full-screen onboarding carousel shown **only once**, the first time a user logs in. After the last page the user lands on Dashboard. The experience is a horizontal drag carousel (HorizontalPager) with 5 pages, a dots indicator, a "Skip" shortcut, and a primary CTA button. Once completed (or skipped), the flag is persisted and the onboarding never appears again.

**Depends on:** TK-013 (feature:auth), TK-018 (feature:dashboard)
**Status:** Backlog

---

## Screens

| # | Title | Subtitle | Visual |
|---|-------|----------|--------|
| 1 | Tu portfolio, en un lugar | Añade holdings y sigue su valor en tiempo real | Lottie: gráfica de barras creciendo con monedas |
| 2 | No pierdas ningún dividendo | Calendario de cobros y proyección anual de ingresos | Lottie: calendario con monedas cayendo |
| 3 | El mercado de un vistazo | Índices mundiales actualizados al instante | Lottie: pantalla de trading con líneas animadas |
| 4 | Busca y sigue valores | Crea tu watchlist sin necesidad de comprar | Lottie: lupa escaneando una lista de acciones |
| 5 | ¡Todo listo! | Empieza a construir tu futuro financiero | Lottie: cohete despegando o confetti |

---

## UI Layout (per page)

```
┌────────────────────────────┐
│  [Skip]              ×/5   │  ← top bar (hidden on last page)
│                            │
│   🎨 Lottie / Image        │  ← ~55% of screen height
│   (fullwidth, centered)    │
│                            │
│   Título (H4, bold)        │
│   Subtítulo (Body, muted)  │
│                            │
│   ● ● ○ ○ ○               │  ← dots indicator
│                            │
│   [    Siguiente   ]       │  ← primary button
│   (last page: "Empezar")   │
└────────────────────────────┘
```

- Drag between pages is free via `HorizontalPager` (Compose Foundation).
- "Siguiente" advances pager programmatically.
- "Empezar" (last page) + "Skip" (any page) both: persist flag → navigate to `MainGraphRoute` (popUpTo 0 inclusive).

---

## Architecture

### Prompt for Lottie assets (supply to image/animation AI)

> *"Flat design animation loop, financial app style, blue and white color palette, no text. Scene: [scene description per table above]. Clean lines, minimalist, 2–3 second loop, transparent background."*

Export as `.lottie` (Lottie JSON). Use `io.github.alexzhirkevich:compottie` for KMP playback.

---

## Implementation Plan

### Phase 1: Persistence — `common/settings`

- [ ] Add `onboardingCompleted: Boolean = false` to `AppSettings`
- [ ] Add `suspend fun setOnboardingCompleted()` to `AppSettingsDataStore` interface and `AppSettingsDataStoreImpl`
- [ ] Add `SetOnboardingCompletedUseCase` in `common/settings/domain/usecase/`
- [ ] **Commit:** `DVX-TK-036 Add onboardingCompleted flag to AppSettings`

### Phase 2: New module `feature/onboarding`

- [ ] Create `feature/onboarding/` module following `dividox.kmp.library` + `dividox.compose.multiplatform` + `dividox.kmp.ios` + `dividox.kmp.test` convention plugins
- [ ] Add `compottie` dependency to `libs.versions.toml` and module `build.gradle.kts`
- [ ] Define MVI contract:
  - `OnboardingState(currentPage: Int, totalPages: Int)`
  - `OnboardingEvent`: `OnNextClicked`, `OnSkipClicked`, `OnPageChanged(page: Int)`
  - `OnboardingSideEffect.NavigateToDashboard`
- [ ] `OnboardingViewModel`: inject `SetOnboardingCompletedUseCase`; on skip/finish call use case then emit `NavigateToDashboard`
- [ ] `OnboardingScreen`: `HorizontalPager` with `PagerState`, dots indicator, skip/next/start buttons
- [ ] `OnboardingPageContent`: composable for single page (Lottie + title + subtitle)
- [ ] Add all UI strings to `common/ui-resources/.../strings.xml`
- [ ] **Commit:** `DVX-TK-036 Add feature:onboarding module with MVI and HorizontalPager`

### Phase 3: Navigation — `composeApp`

- [ ] Create `composeApp/src/commonMain/kotlin/com/akole/dividox/navigation/OnboardingNavigation.kt`
  - `data object OnboardingRoute`
  - `NavGraphBuilder.onboardingScreenNode(navController)`
- [ ] Register `onboardingScreenNode` in `RootNavGraph.kt`
- [ ] **Commit:** `DVX-TK-036 Add OnboardingRoute and navigation node`

### Phase 4: RootNavGraph — routing logic

- [ ] Inject `ObserveAppSettingsUseCase` (already exists) in `SetupRootNavGraph`
- [ ] On `SessionState.Authenticated`: read `onboardingCompleted`; route to `OnboardingRoute` if false, else `MainGraphRoute`
- [ ] **Commit:** `DVX-TK-036 Route to onboarding on first login`

### Phase 5: Lottie assets

- [ ] Generate 5 Lottie files using prompt above (external AI tool)
- [ ] Place in `feature/onboarding/src/commonMain/composeResources/files/`
- [ ] Wire each file to its `OnboardingPageContent`
- [ ] **Commit:** `DVX-TK-036 Add Lottie assets for onboarding pages`

### Phase 6: Testing & Quality

- [ ] Unit tests for `OnboardingViewModel`: skip flow, next-to-last-page flow, page change event
- [ ] Verify `./gradlew :feature:onboarding:jvmTest`
- [ ] Run `./gradlew :feature:onboarding:detekt :composeApp:assembleDebug`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking

**Total Tasks:** 14 · **Completed:** 0 · **Remaining:** 14

---

## Notes

- `HorizontalPager` is in `androidx.compose.foundation` — no extra dep needed beyond existing Compose setup.
- `compottie` (`io.github.alexzhirkevich:compottie`) supports KMP (Android + iOS + Desktop). Pin version in `libs.versions.toml`.
- Lottie files go under `composeResources/files/` and are loaded via `Compottie.rememberLottieComposition`.
- No Firestore or remote schema change needed — `onboardingCompleted` lives only in local DataStore.
- Skip and "Empezar" must both mark the flag; never show onboarding twice regardless of how user exits.
- Use `MaterialTheme.spacing.*` for all padding/sizes; never hardcode values.
