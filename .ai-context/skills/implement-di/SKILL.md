---
name: implement-di
description: Expert on Dependency Injection (DI) configuration using Koin for Kotlin Multiplatform projects. Use this skill when configuring modules, registering dependencies, or setting up test overrides.
---

# Dependency Manager Skill

## Module Location

All DI configuration lives in **`:composeApp`** → `commonMain/di/`.

```
composeApp/
└── src/
    ├── commonMain/kotlin/com/akole/dividox/
    │   └── di/
    │       ├── KoinInitializer.kt    → object KoinInitializer with fun init(config)
    │       ├── AppModule.kt          → Dispatchers, CoroutineScope, platform providers
    │       ├── ViewModelModule.kt    → ViewModel registrations (:feature/* ViewModels)
    │       ├── RepositoryModule.kt   → Repository bindings (:component/* impls)
    │       └── DomainModule.kt       → Use Case factories (:component/* use cases)
    └── androidMain/kotlin/com/akole/dividox/
        └── di/
            └── KoinInitializer.android.kt → extension fun init(Application)
```

> ❌ Never create Koin modules inside `:feature/*` or `:component/*`.
> ✅ Components and features use **constructor injection only** — no Koin references inside them.
> See `skill: module-organization` for full module rules.

## Convention Plugin

Koin dependencies are added via the `dividox-kmp-di` convention plugin (`KmpDiConventionPlugin`).

Apply it to any module that hosts DI configuration:

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.dividox.kmp.di)
}
```

The plugin adds automatically:
- `commonMain`: `koin-core`, `koin-compose`, `koin-compose-viewmodel`
- `androidMain`: `koin-android`

## Initialization Pattern

### KoinInitializer (commonMain)

```kotlin
object KoinInitializer {
    fun init(config: KoinApplication.() -> Unit = {}) {
        startKoin {
            config()
            modules(appModule, viewModelModule)
        }
    }
}
```

### Android extension (androidMain)

```kotlin
fun KoinInitializer.init(application: Application) {
    init {
        androidLogger()
        androidContext(application)
    }
}
```

### Per-platform entry points

| Platform | File | Call |
|----------|------|------|
| Android  | `DividoxApplication.kt` | `KoinInitializer.init(this)` |
| Desktop  | `main.kt` | `KoinInitializer.init()` (before `application { }`) |
| iOS      | `MainViewController.kt` | `KoinInitializer.init()` (inside `run { }`, before `ComposeUIViewController`) |

## Capabilities
- Configure Koin modules
- Register Repositories, UseCases, ViewModels, and DataSources
- Manage dependency scopes (`single`, `factory`, `viewModel`)
- Set up test DI configurations

## Rules (CRITICAL)
- **Constructor Injection**: All classes must rely solely on constructor injection.
- **Interface Binding**: Always bind interface to implementation, not concrete classes.
- **No Koin in features**: Never import Koin inside `:feature/*` or `:component/*` modules.

## Reference Files
- [DI Patterns and Templates](references/di-patterns.md)

## Common Tasks

### Registering a New ViewModel (no constructor params)
```kotlin
viewModel { MyViewModel(useCase = get()) }
```

### Registering a New ViewModel (with constructor params from call site)
```kotlin
// Module
viewModel { params -> MyViewModel(value = params.get(), useCase = get()) }

// Call site (Compose)
val viewModel = koinViewModel<MyViewModel>(
    parameters = { parametersOf(value) }
)
```

### Registering a New Repository
```kotlin
single<MyRepository> { MyRepositoryImpl(dataSource = get()) }
```

### Registering a New UseCase
```kotlin
factory { MyUseCase(repository = get()) }
```
