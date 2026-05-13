# Task: DVX-TK-032 · feature:settings — Native About / Terms / Privacy Screens

## Description

Replace the old `OpenUrl` side effects for About DiviDox, Terms & Conditions, and Privacy Policy with proper native Compose screens. The `SettingsViewModel` already emits `NavigateToAbout`, `NavigateToTerms`, `NavigateToPrivacy` side effects but `settingsScreenNode` in `MainNavigation.kt` does not yet handle them.

**Goal:** Three native Compose screens, registered at the root nav level (full-screen, no bottom bar), navigated to from `rootNavController` inside `settingsScreenNode`.

**Screens:**
- **About DiviDox** — app name, version, tagline, brief description
- **Terms & Conditions** — scrollable legal text
- **Privacy Policy** — scrollable privacy text

**Depends on:** DVX-TK-031 (merged)
**Status:** In Progress

---

## Architecture Notes

- Screens live in `feature/settings/src/commonMain/.../feature/settings/`
- No ViewModel needed — static content only
- Routes: `AboutRoute`, `TermsRoute`, `PrivacyRoute` added to `MainNavigation.kt`
- Nodes registered in `RootNavGraph.kt` (outside `mainGraphNode` — no bottom bar)
- `settingsScreenNode` receives `rootNavController` — use it to `navigate(AboutRoute)`, etc.
- All text in `strings.xml` — no hardcoded strings in composables
- Spacing via `MaterialTheme.spacing.*` — no hardcoded dp values
- Back navigation: each screen node calls `navController.popBackStack()` on back event

---

## Subtasks

### Phase 1: Branch setup
- [ ] **Create Git branch** `feature/DVX-TK-032-native-about-terms-privacy` from `main`
  - **Commit:** *(branch creation, no commit needed)*

### Phase 2: Ticket & GitHub update
- [ ] **Update `tickets/TK-032-contact-support.md`** — rename file to `TK-032-native-about-terms-privacy.md`, rewrite content to match new scope
- [ ] **Update GitHub issue #63** — new title "DVX-TK-032 Native About / Terms / Privacy Screens", update body
  - **Commit:** `DVX-TK-032 Update TK-032 ticket scope to native screens`

### Phase 3: Strings
- [ ] **Add all content strings** to `common/ui-resources/src/commonMain/composeResources/values/strings.xml`:
  - Screen titles (reuse existing `settings_about`, `settings_terms`, `settings_privacy`)
  - About screen: tagline, version label, description paragraph, credits label
  - Terms: section headers + body text (or single `terms_body` block string)
  - Privacy: section headers + body text (or single `privacy_body` block string)
  - Common: `label_back` if not already present
  - **Commit:** `DVX-TK-032 Add About/Terms/Privacy screen strings`

### Phase 4: Static screens
- [ ] **`AboutScreen.kt`** in `feature/settings/src/commonMain/.../feature/settings/`
  - Top bar with back button and title "About DiviDox"
  - App logo (reuse existing drawable if available, else placeholder)
  - App name, version (passed as param from state or nav argument), tagline, description
  - `onBack: () -> Unit` callback
- [ ] **`TermsScreen.kt`** — top bar + `LazyColumn` with scrollable terms content
- [ ] **`PrivacyScreen.kt`** — top bar + `LazyColumn` with scrollable privacy content
  - **Commit:** `DVX-TK-032 Add AboutScreen, TermsScreen, PrivacyScreen`

### Phase 5: Routes & navigation nodes
- [ ] **Add routes** in `MainNavigation.kt`:
  ```kotlin
  @Serializable data object AboutRoute
  @Serializable data object TermsRoute
  @Serializable data object PrivacyRoute
  ```
- [ ] **Add navigation extension fns** (`navigateToAbout`, `navigateToTerms`, `navigateToPrivacy`)
- [ ] **Add screen nodes** (`aboutScreenNode`, `termsScreenNode`, `privacyScreenNode`) in `MainNavigation.kt`
  - **Commit:** `DVX-TK-032 Add About/Terms/Privacy routes and nav nodes`

### Phase 6: Wire navigation
- [ ] **Register nodes** in `RootNavGraph.kt`:
  ```kotlin
  aboutScreenNode(navController)
  termsScreenNode(navController)
  privacyScreenNode(navController)
  ```
- [ ] **Handle side effects** in `settingsScreenNode` (`MainNavigation.kt`):
  ```kotlin
  is SettingsViewSideEffect.Navigation.NavigateToAbout -> rootNavController.navigateToAbout()
  is SettingsViewSideEffect.Navigation.NavigateToTerms -> rootNavController.navigateToTerms()
  is SettingsViewSideEffect.Navigation.NavigateToPrivacy -> rootNavController.navigateToPrivacy()
  ```
  - **Commit:** `DVX-TK-032 Wire About/Terms/Privacy navigation from Settings`

### Phase 7: Quality & PR
- [ ] `./gradlew :feature:settings:jvmTest :feature:settings:detekt`
- [ ] Create Pull Request — `skill: manage-git-flow pr DVX-TK-032 "Native About Terms Privacy Screens"`

---

## Progress Tracking

**Total Tasks:** 9 · **Completed:** 0 · **Remaining:** 9

---

## Implementation Notes

- `AboutScreen` receives `appVersion: String` from the SettingsViewState (already present) — pass it via nav argument or inject via the node composable reading from a shared ViewModel
- Simplest approach: pass `appVersion` as a route argument in `AboutRoute(val appVersion: String)`
- Terms/Privacy are fully static — no params needed
- Existing `OpenUrl` side effect stays in contract (may be reused elsewhere), but no longer emitted for these three actions
