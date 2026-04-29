---
name: Kotlin stdlib Clock vs kotlinx.datetime
description: In Kotlin 2.x KMP commonMain, use kotlin.time.Clock/Instant (stdlib) not kotlinx.datetime.Clock
type: feedback
---

Use `kotlin.time.Clock` and `kotlin.time.Instant` from the Kotlin stdlib, not `kotlinx.datetime.Clock` or `kotlinx.datetime.Instant`, for timestamp operations in commonMain.

**Why:** In Kotlin 2.x, `Clock` and `Instant` moved into the Kotlin stdlib (`kotlin.time` package). `kotlinx.datetime.Clock` no longer exists as a top-level class — `Clock.System` under `kotlinx.datetime` import causes `Unresolved reference 'System'` at compile time. The market component already uses `kotlin.time.Clock` — that is the project standard.

**How to apply:** Import `kotlin.time.Clock` and `kotlin.time.Instant`. Never import `kotlinx.datetime.Clock` or `kotlinx.datetime.Instant` for new code. `kotlinx-datetime` library is still used for calendar/timezone operations (e.g. `Clock.System.todayIn(TimeZone.currentSystemDefault())`), but raw `Instant` and `Clock.System.now()` should come from stdlib.
