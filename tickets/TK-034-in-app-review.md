# Task: TK-034 · feature:settings — In-App Review (Rate the App)

## Description

Prompt users to rate DiviDox using the native in-app review flows: Google Play In-App Review API on Android and `SKStoreReviewController` on iOS. The prompt is triggered automatically (not from a Settings row) after the user views a Security Detail screen for the 5th time, ensuring they have experienced value before being asked. Desktop JVM is a no-op.

**Depends on:** TK-031
**Blocks:** —
**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch** `feature/DVX-TK-034-in-app-review` — `skill: manage-git-flow`

### Phase 2: Review trigger tracking
- [ ] **`InAppReviewTracker`** in `common/settings/data/`
  - DataStore key `security_detail_view_count: Int`
  - `fun recordSecurityDetailView(): Boolean` — increments count; returns `true` when count reaches 5 (trigger threshold) or 20 (re-trigger after first review)
  - **Commit:** `DVX-TK-034 Add InAppReviewTracker with DataStore persistence`

### Phase 3: Platform review service
- [ ] **`InAppReviewService` expect/actual**
  - Android: `ReviewManagerFactory.create(context)`, call `manager.requestReviewFlow()` then `manager.launchReviewFlow()`
  - iOS: `SKStoreReviewController.requestReview()` (iOS 14+: `requestReview(in: windowScene)`)
  - JVM: no-op
  - **Commit:** `DVX-TK-034 Add InAppReviewService expect/actual`

### Phase 4: Wire into SecurityDetailViewModel
- [ ] **Call `InAppReviewTracker.recordSecurityDetailView()`** in `SecurityDetailViewModel.init` or on first data load
- [ ] **Emit `TriggerInAppReview` side effect** if tracker returns `true`
  - **Commit:** `DVX-TK-034 Trigger in-app review from SecurityDetailViewModel`

### Phase 5: Navigation wiring
- [ ] **Handle `TriggerInAppReview`** in `securityDetailScreenNode` — call `inAppReviewService.requestReview()`
  - **Commit:** `DVX-TK-034 Wire TriggerInAppReview in navigation`

### Phase 6: Testing & Quality
- [ ] `./gradlew :feature:analysis:jvmTest :feature:analysis:detekt`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking
**Total Tasks:** 6 **Completed:** 0 **Remaining:** 6

---

## Notes
- Google Play review API may silently do nothing if the user already reviewed or the quota is exceeded — this is by design
- Never ask for a review immediately; ensure the user has experienced value first (5 security detail views is the threshold)
- Do NOT add a "Rate the App" row in Settings — the native APIs work best when triggered contextually and only on Android/iOS
