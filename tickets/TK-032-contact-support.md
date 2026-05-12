# Task: TK-032 · feature:settings — Contact Support & Notifications Fix

## Description

Fix two broken items in Settings:

1. **Contact Support bug**: "Contact Support" row incorrectly fires `NotificationsClicked` → opens help URL. Should have its own `ContactSupportClicked` event that opens a native email compose intent pre-filled with app version, device info, and `support@dividox.app` as the recipient.

2. **Notifications row**: Re-add the "Notifications" row to the preferences section. Currently the `NotificationsClicked` event exists in the contract but no row renders it. Wire it properly — on Android redirect to system notification settings for the app; on iOS/JVM open the help URL (push notifications not supported on those platforms in v1).

**Depends on:** TK-031
**Blocks:** —
**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch** `feature/DVX-TK-032-contact-support` — `skill: manage-git-flow`

### Phase 2: Contract & ViewModel
- [ ] **Add `ContactSupportClicked` to `SettingsViewEvent`**
- [ ] **Handle `ContactSupportClicked` in `SettingsViewModel`**
  - Emit `OpenEmailCompose(to, subject, body)` side effect
  - Body pre-filled: app version + platform + user UID (last 6 chars) for support context
- [ ] **Add `OpenEmailCompose` to `SettingsViewSideEffect`**
  - `data class OpenEmailCompose(val to: String, val subject: String, val body: String)`
  - **Commit:** `DVX-TK-032 Add ContactSupportClicked event and OpenEmailCompose side effect`

### Phase 3: Email compose platform impl
- [ ] **`EmailComposeService` expect/actual** in `common/settings/data/`
  - Android: `Intent.ACTION_SENDTO` with `mailto:` URI + extras
  - iOS: `MFMailComposeViewController` (fall back to `mailto:` URL if unavailable)
  - JVM: `Desktop.getDesktop().mail(URI)` or log if unavailable
  - Register in Koin `SettingsModule`
  - **Commit:** `DVX-TK-032 Add EmailComposeService expect/actual`

### Phase 4: Notifications row
- [ ] **Fix `SettingsScreen`**: change "Contact Support" `onClick` from `NotificationsClicked` to `ContactSupportClicked`
- [ ] **Add "Notifications" row** in the Preferences section (Bell icon)
  - Android: redirect to `Settings.ACTION_APP_NOTIFICATION_SETTINGS` via `NotificationsClickedHandler`
  - iOS/JVM: keep existing URL behaviour
  - **Commit:** `DVX-TK-032 Fix Contact Support row and add Notifications row`

### Phase 5: Navigation wiring
- [ ] **Handle `OpenEmailCompose` in `settingsScreenNode`** — call `emailComposeService.compose(...)`
  - **Commit:** `DVX-TK-032 Wire OpenEmailCompose in navigation`

### Phase 6: Testing & Quality
- [ ] `./gradlew :feature:settings:jvmTest :feature:settings:detekt`
- [ ] Create Pull Request — `skill: manage-git-flow`

---

## Progress Tracking
**Total Tasks:** 7 **Completed:** 0 **Remaining:** 7

---

## Notes
- Contact Support subject: `"[DiviDox Support] v{version} {platform}"`
- Don't include full UID in the email body for privacy — last 6 chars only
- iOS `MFMailComposeViewController` requires a device with Mail configured; fall back to `mailto:` URL if `canSendMail()` returns false
