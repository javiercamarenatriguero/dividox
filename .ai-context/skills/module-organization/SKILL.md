---
name: module-organization
description: Defines the multi-module KMP architecture for Dividox (app/feature/integration/component/common). Use when creating new modules, deciding where code belongs, or onboarding to the project structure.
---

# Module Organization — Dividox KMP

Dividox follows a **multi-module Clean Architecture** adapted for Kotlin Multiplatform. Every module is a KMP module with source sets (`commonMain`, `androidMain`, `iosMain`, `jvmMain`).

## Module Map

```
dividox/
├── app/                          → Entry point, DI, Navigation
├── feature/
│   └── [name]/                   → Presentation layer (Screen, ViewModel, Contract)
├── component/
│   └── [name]/                   → Domain + Data layer (UseCases, Repositories)
├── integration/
│   └── [name]/                   → Bridges between components
└── common/
    └── [name]/                   → Shared utilities, UI components, formatters
```

---

## Module Types

### `:app`
**Purpose**: Application entry point. DI initialization, root NavHost, platform-specific launchers.  
**Convention plugins**:
```kotlin
alias(libs.plugins.dividox.kmp.application)
alias(libs.plugins.dividox.compose.multiplatform)
alias(libs.plugins.dividox.kmp.ios)
alias(libs.plugins.dividox.kmp.test)
```
**Source sets**:
| Source set | Content |
|---|---|
| `commonMain` | `App.kt`, `NavHost`, `di/` modules (Koin), `navigation/` wiring |
| `androidMain` | `MainActivity.kt` |
| `iosMain` | `MainViewController.kt` |
| `jvmMain` | `main.kt` |

---

### `:feature:[name]`
**Purpose**: Presentation layer for a single user-facing feature. Contains MVI Contract, ViewModel, and Compose Multiplatform Screen.  
**Convention plugins**:
```kotlin
alias(libs.plugins.dividox.kmp.library)
alias(libs.plugins.dividox.compose.multiplatform)
alias(libs.plugins.dividox.kmp.ios)
alias(libs.plugins.dividox.kmp.test)
```
**Source sets**:
| Source set | Content |
|---|---|
| `commonMain` | `[Name]Contract.kt`, `[Name]ViewModel.kt`, `[Name]Screen.kt` |
| `commonTest` | `[Name]ViewModelTest.kt` |

> ✅ Feature modules are **platform-agnostic** — all code in `commonMain`.  
> ❌ No `androidMain`/`iosMain` in feature modules.

---

### `:component:[name]`
**Purpose**: Domain + Data layer for a bounded context. Pure Kotlin in `commonMain`; platform-specific implementations via `expect/actual`.  
**Convention plugins**:
```kotlin
alias(libs.plugins.dividox.kmp.library)
alias(libs.plugins.dividox.kmp.ios)
alias(libs.plugins.dividox.kmp.test)
```
**Source sets**:
| Source set | Content |
|---|---|
| `commonMain` | `domain/model/`, `domain/repository/`, `domain/usecase/`, `data/repository/`, `data/datasource/` |
| `androidMain` | Platform `actual` implementations (e.g., Room DAO, DataStore) |
| `iosMain` | Platform `actual` implementations |
| `jvmMain` | Platform `actual` implementations (Desktop) |
| `commonTest` | Use case and repository tests |

> ✅ Repository **interfaces** in `domain/` (commonMain).  
> ✅ Repository **implementations** in `data/` (commonMain or platform actuals).  
> ❌ No Compose or ViewModel in component modules.

---

### `:integration:[name]`
**Purpose**: Coordinates multiple components that need to communicate without creating circular dependencies.  
**Convention plugins**:
```kotlin
alias(libs.plugins.dividox.kmp.library)
alias(libs.plugins.dividox.kmp.ios)
alias(libs.plugins.dividox.kmp.test)
```
**When to create**: When `component/A` needs data from `component/B` — never import B directly from A; create `integration/A-B` instead.

---

### `:common:[name]`
**Purpose**: Shared, reusable utilities with no business logic.  
**Convention plugins** (base):
```kotlin
alias(libs.plugins.dividox.kmp.library)
alias(libs.plugins.dividox.kmp.ios)
```
Add `dividox.compose.multiplatform` only for modules with Compose UI (e.g., `common/ui`).

**Typical modules**:
| Module | Content |
|---|---|
| `common/mvi` | **MVI foundation** — `ViewState`, `ViewEvent`, `SideEffect` interfaces; `MVI<VS,VE,SE>` contract; `mvi()` delegate; `CollectSideEffect`, `collectViewState` composables |
| `common/ui` | Shared Compose components, theme, typography |
| `common/utils` | Extensions, formatters, helpers |
| `common/coroutines` | Dispatcher providers, scope utilities |

> ✅ Common modules are **leaf nodes** — they depend on nothing in the project.  
> ✅ All feature ViewModels **must** use `MVI` + `mvi()` from `:common:mvi` — never raw `MutableStateFlow`.  
> ✅ All feature Screens must accept `sideEffects: Flow<SE>` and use `CollectSideEffect` from `:common:mvi`.

---

## Dependency Rules

```
:app              → :feature/*, :component/*, :integration/*, :common/*
:feature/[name]   → :component/*, :integration/*, :common/*
:integration/[name] → :component/*, :common/*
:component/[name] → :common/*
:common/[name]    → (nothing)
```

### FORBIDDEN
| Rule | Why |
|---|---|
| `:feature/A` → `:feature/B` | Features must be independent |
| `:component/A` → `:component/B` | Components must be isolated → use `:integration/A-B` |
| Any module → `:app` | App is the composition root |

---

## Code Placement Quick Reference

| What | Where |
|---|---|
| Screen (Composable) | `:feature/[name]` → `commonMain` |
| ViewModel | `:feature/[name]` → `commonMain` |
| MVI Contract | `:feature/[name]` → `commonMain` |
| Use Case | `:component/[name]` → `commonMain/domain/usecase/` |
| Repository interface | `:component/[name]` → `commonMain/domain/repository/` |
| Repository implementation | `:component/[name]` → `commonMain/data/repository/` (or platform actual) |
| Domain model | `:component/[name]` → `commonMain/domain/model/` |
| DI modules | `:app` → `commonMain/di/` |
| NavHost + navigation wiring | `:app` → `commonMain/navigation/` |
| Platform entry points | `:app` → `androidMain/`, `iosMain/`, `jvmMain/` |
| Shared UI components | `:common/ui` → `commonMain` |
| Utilities / extensions | `:common/utils` → `commonMain` |

---

## References
- [Detailed module rules and anti-patterns](references/module-rules.md)
