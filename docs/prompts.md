# DiviDox — Prompt Library

A curated collection of prompts used to generate, document, and evolve the DiviDox product.

---

## Meta-Prompts

### MP-001 · Generate PRD from Screen Description

Use this prompt whenever a new screen description is available and you need to derive its PRD.

```
You are a Product Owner for DiviDox, a Kotlin Multiplatform (KMP) app built with Compose Multiplatform targeting Android, iOS, and Desktop (JVM).
DiviDox is a personal finance app for stock portfolio tracking with a dividend-first approach.
All requirements apply to all three platforms unless explicitly noted otherwise.

Given the following screen description for the feature "{FEATURE_NAME}":

{SCREEN_DESCRIPTION}

Generate a PRD (Product Requirements Document) following this exact structure:

# PRD-XX · {Feature Name}

**Status:** Draft
**Platform:** Android · iOS · Desktop (KMP)

## Overview
2–3 sentences describing what this feature is and what it covers.

## Problem Statement
What user problem does this feature solve? Why does it matter?

## Goals
Bullet list of 3–5 measurable goals for this feature.

## Functional Requirements
One sub-section per major screen area or flow. Each requirement is a numbered row in a table:
| # | Requirement |

Requirements must be derived strictly from the screen description.
Written as observable system behaviour ("Display X", "Navigate to Y", "Disable Z when…").

## Error & Edge Cases
Table: Scenario | Behaviour
Cover network failures, empty states, destructive actions, and boundary inputs.

## Non-Functional Requirements
Performance targets, security constraints, platform-specific considerations.

## Out of Scope (v1)
Related features explicitly excluded from this version.

## Open Questions
Unresolved decisions that need input from stakeholders or engineering.

Rules:
- Do not invent requirements not present in the screen description.
- Call out Android / iOS / Desktop differences explicitly when relevant.
- Write in English.
- Output only the PRD, no preamble or commentary.
```

---

### MP-002 · Generate ADR from Tech Decision

Use this prompt to document a new architectural decision derived from the PRDs and the confirmed tech stack.

```
You are a Software Architect for DiviDox, a Kotlin Multiplatform (KMP) app built with Compose Multiplatform targeting Android, iOS, and Desktop (JVM).

Confirmed tech stack:
- UI: Compose Multiplatform (commonMain)
- DI: Koin
- Market data: Yahoo Finance API (unofficial, via Ktor)
- Backend / Auth / Persistence: Firebase (Auth, Firestore)
- Networking: Ktor Client (commonMain)
- Presentation pattern: MVI (Contract: State / Event / Effect)
- Modularization: app / feature / integration / component / common
- Local DB: Room (structured/relational data)
- Local key-value: DataStore<Preferences>
- Session token: encrypted via SessionStorage (ADR-008)
- Navigation: navigation-compose (KMP)

Given the following architectural decision to document:

{DECISION_DESCRIPTION}

Generate an ADR following this exact structure:

# ADR-XXX: {Title}

**Date:** {date}
**Status:** Accepted

## Context
What situation or requirement drives this decision?

## Decision
What was decided? Include code snippets, module structure, or diagrams where useful.

## Alternatives Considered
Table or list of alternatives with Pros / Cons for each.

## Consequences
### Positive
### Negative

## Related
Links to related ADRs and ticket references.

Rules:
- Be specific and concrete — include file paths, class names, interface signatures.
- Justify the decision against the alternatives.
- Call out platform differences (Android / iOS / Desktop) when relevant.
- Write in English.
- Output only the ADR, no preamble or commentary.
```

---

### MP-003 · Generate User Stories from PRD

Use this prompt to derive user stories from an existing PRD.

```
You are a Product Owner for DiviDox, a Kotlin Multiplatform (KMP) app built with Compose Multiplatform targeting Android, iOS, and Desktop (JVM).
DiviDox is a personal finance app for stock portfolio tracking with a dividend-first approach.
All user stories apply to all three platforms unless explicitly noted otherwise.

Given the following PRD for the feature "{FEATURE_NAME}":

{PRD_CONTENT}

Generate user stories following these rules:

1. Format each story as:
   ### DVX-US-XXX · [Short Title]
   **As a** [user type],
   **I want to** [action],
   **so that** [benefit].

   **Acceptance Criteria:**
   - [Criterion 1]
   - [Criterion 2]
   - ...

2. One user story per distinct user action or goal. Do not bundle multiple independent actions into a single story.

3. Acceptance Criteria must be:
   - Testable and unambiguous.
   - Derived strictly from the PRD Functional Requirements and Error & Edge Cases.
   - Written in present tense ("Shows...", "Navigates to...", "Displays...").

4. Include stories for:
   - Happy path (primary action succeeds).
   - Error / empty states listed in the PRD.
   - Navigation actions (back, links, FABs).
   - Out of Scope items must NOT generate stories.

5. Do NOT include stories for:
   - Backend implementation details.
   - Hypothetical features not present in the PRD.
   - Platform-specific behaviour unless explicitly stated in the PRD.

6. All stories must be written in English.

Output only the user stories, no preamble or commentary.
```

---

### MP-004 · Split User Stories into Implementation Tasks

Use this prompt to turn a set of user stories into granular, sequentially-numbered implementation task tickets. Each ticket bundles scaffold + domain + data (for components) or scaffold + MVI + nav wiring (for features) into a single ticket — never standalone scaffold-only or nav-only tickets.

```
You are a KMP Staff Engineer for DiviDox, a Kotlin Multiplatform app built with Compose Multiplatform targeting Android, iOS, and Desktop (JVM).

Tech stack: KMP + CMP · Koin · Firebase (Auth, Firestore) · Ktor · Yahoo Finance (unofficial) · MVI · Room · DataStore · navigation-compose (typed routes) · Detekt

Module layers:
- :common       → shared utilities, theme, navigation routes (no Compose screens)
- :component    → domain + data (no Compose, no ViewModel)
- :integration  → orchestration of multiple components (no Compose)
- :feature      → screens + MVI (Compose, ViewModel, Contract)
- :composeApp   → app shell, RootNavGraph, Koin root

Given the following user stories:

{USER_STORIES}

Split them into granular implementation task tickets following these rules:

1. NUMBERING & ORDER
   - Number tickets TK-NNN sequentially in the order they should be implemented.
   - No ticket should reference a higher-numbered ticket as a dependency.
   - Order: Foundation → Component Domain → Component Data → Integration → Feature MVI → Navigation/DI.

2. TICKET GRANULARITY — BUNDLE RULES
   - **Component ticket** = scaffold + domain + data (+ Koin) all in one ticket. Never split them.
   - **Feature ticket** = scaffold + MVI Contract/ViewModel/Screen + nav wiring (+ Koin) all in one ticket. Never split them.
   - **Integration ticket** = scaffold + all orchestration use cases + Koin all in one ticket.
   - Do NOT create a standalone scaffold-only ticket. The scaffold is always Phase 1 of its component/feature ticket.
   - Do NOT create a standalone nav-wiring-only ticket. Nav wiring is always the final phase of its feature ticket.
   - Do NOT bundle multiple unrelated screens into one ticket.
   - A ticket should be completable in one focused session (half day max).
   - Target total ticket count ≤ 20 for a full v1 app scope. If your count exceeds 20, merge further by applying the bundle rules above.

3. FOUNDATION FIRST (per ticket)
   - Phase 1 of every component/feature/integration ticket MUST scaffold its Gradle module:
     create build.gradle.kts (apply correct convention plugins), add include() to settings.gradle.kts, verify empty module compiles.
   - Convention plugins: dividox.kmp.library + dividox.kmp.test for :component/:integration; add dividox.compose.multiplatform + dividox.kmp.ios for :feature.
   - If a module needs infrastructure (Room, Ktor, DataStore), add it in Phase 1 of that same ticket BEFORE domain phases.

4. EACH TICKET FORMAT
   Output each ticket as a markdown file named TK-NNN-slug.md with this structure:

   # Task: TK-NNN · {Title}

   ## Description
   One paragraph: what this ticket does and why.

   **User Stories:** (if applicable)
   **PRD:** (if applicable)
   **ADRs:** (relevant ADRs)
   **Depends on:** TK-NNN
   **Blocks:** TK-NNN
   **Status:** Backlog

   ---

   ## Subtasks

   ### Phase 1: Architecture & Setup
   - [ ] Create Git Branch: `feature/DVX-TK-NNN-slug` — Action: `skill: manage-git-flow`

   ### Phase N: {Phase Name}
   - [ ] {Subtask description}
     - Details, file locations, interface signatures
     - **Verify:** `./gradlew :{module}:compileKotlinJvm` or `jvmTest`
     - **Commit:** `DVX-TK-NNN Commit message`

   ### Last Phase: Testing & Quality
   - [ ] `./gradlew test` + `./gradlew detekt`
   - [ ] Create Pull Request — Skill: `skill: manage-git-flow`

   ---

   ## Progress Tracking
   **Total Tasks:** N  (count of checkboxes)
   **Completed:** 0
   **Remaining:** N

   ---

   ## Notes
   Implementation notes, platform caveats, non-obvious constraints.

5. COMMIT MESSAGES
   Every logical step must produce a commit: `DVX-TK-NNN Short description`
   Use present tense imperative: "Add", "Implement", "Scaffold", "Wire", "Register"

6. KOIN WIRING
   Every component/feature that introduces a Koin module must also add it to App.kt startKoin{} in the same ticket.

7. NAVIGATION WIRING
   Wire each route (add composable { FeatureScreen(...) } to mainGraph/authGraph) in the LAST phase of the feature's MVI ticket — never in a separate ticket.
   The Koin module registration and nav wiring must always be committed together with the screen/ViewModel they belong to.

8. USER SESSION
   Any feature that depends on the authenticated user must depend (directly or transitively) on the session lifecycle ticket (ObserveSessionUseCase + SessionState).

Output one markdown block per ticket. Do not output any preamble or commentary outside the ticket blocks.
```

---

### MP-005 · Implement Foundation — SessionState & RootNavGraph Guard

Use this prompt to scaffold the `:common:auth` module, define the session state model, and wire the cold-start splash guard in `RootNavGraph`. This is a stub-first implementation: real Firebase Auth wiring comes later in the `:component:auth` ticket.

```
You are a KMP Staff Engineer for DiviDox, a Kotlin Multiplatform app built with Compose Multiplatform targeting Android, iOS, and Desktop (JVM).

Tech stack: KMP + CMP · Koin · MVI · navigation-compose (typed routes)
Convention plugins: dividox.kmp.library · dividox.kmp.ios · dividox.kmp.di · dividox.kmp.test · dividox.detekt

Task: Implement the Foundation — SessionState & RootNavGraph Guard (TK-010).

## What to build

### 1. Gradle module: `:common:auth`
- Create `common/auth/build.gradle.kts` applying:
  `dividox.kmp.library`, `dividox.kmp.ios`, `dividox.kmp.di`, `dividox.kmp.test`, `dividox.detekt`
- Add `jvm()` target explicitly.
- `commonMain` dependency: `kotlinx-coroutines-core`
- `commonTest` dependency: `kotlinx-coroutines-test`
- Add `include(":common:auth")` to `settings.gradle.kts`.
- Add `implementation(projects.common.auth)` to `:composeApp` `commonMain` dependencies.

### 2. Domain models (commonMain)
Package: `com.akole.dividox.common.auth.domain.model`

- `AuthProvider.kt` — `enum class AuthProvider { GOOGLE, EMAIL }`
- `AuthUser.kt` — `data class AuthUser(uid: String, email: String?, displayName: String?, provider: AuthProvider)`
- `SessionState.kt` — sealed interface:
  ```kotlin
  sealed interface SessionState {
      data object Loading : SessionState
      data class Authenticated(val user: AuthUser) : SessionState
      data object Unauthenticated : SessionState
  }
  ```

### 3. Repository interface + stub
Package: `com.akole.dividox.common.auth.domain.repository`

- `AuthRepository.kt` — interface: `fun observeAuthState(): Flow<AuthUser?>`
- `StubAuthRepository.kt` (internal) — always emits `flowOf(null)` (→ Unauthenticated always).

### 4. Use case
Package: `com.akole.dividox.common.auth.domain.usecase`

- `ObserveSessionUseCase(repository: AuthRepository)`:
  ```kotlin
  operator fun invoke(): Flow<SessionState> =
      repository.observeAuthState()
          .map { user -> if (user != null) Authenticated(user) else Unauthenticated }
          .onStart { emit(Loading) }
  ```

### 5. Koin module
Package: `com.akole.dividox.common.auth.di`

- `authModule`: bind `StubAuthRepository` as `AuthRepository`; factory `ObserveSessionUseCase`.
- Register `authModule` in `KoinInitializer.init()` **before** `appModule`.

### 6. SplashScreen composable
`composeApp/.../navigation/SplashScreen.kt` — centered DiviDox wordmark using `MaterialTheme.typography.displayMedium`. Non-dismissable (no back handler).

### 7. RootNavGraph guard
Replace `SetupRootNavGraph` body with:
```kotlin
val observeSession: ObserveSessionUseCase = koinInject()
val sessionState by observeSession().collectAsState(initial = SessionState.Loading)

when (sessionState) {
    SessionState.Loading        -> SplashScreen()
    SessionState.Unauthenticated,
    is SessionState.Authenticated -> NavHost(...) { /* existing nodes */ }
}
```
Note: Until TK-011 adds real auth screens, both Unauthenticated and Authenticated show the main graph.

### 8. Unit tests
File: `common/auth/src/commonTest/.../ObserveSessionUseCaseTest.kt`

Cover:
- `emits Loading then Unauthenticated when no user is signed in`
- `emits Loading then Authenticated when a user is signed in` (verify the `AuthUser` is propagated)
- `first emission is always Loading`

Use a `FakeAuthRepository` inner class; use `runTest` + `flow.toList()`.

## Rules
- Do NOT commit; leave all changes unstaged for review.
- Do NOT add real Firebase Auth — keep StubAuthRepository.
- Do NOT add a "Save" button or confirmation for session changes.
- All new files go in `commonMain` (no platform-specific source sets needed).
- Follow the existing package structure: `com.akole.dividox.common.auth.*`
```

---
