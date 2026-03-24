---
name: implement-di
description: Expert on Dependency Injection (DI) configuration using Koin for Kotlin Multiplatform projects. Use this skill when configuring modules, registering dependencies, or setting up test overrides.
---

# Dependency Manager Skill

## Module Location

All DI configuration lives in **`:app`** → `commonMain/di/`.

```
app/
└── src/
    └── commonMain/kotlin/com/akole/dividox/
        └── di/
            ├── AppModule.kt          → Dispatchers, CoroutineScope, platform providers
            ├── RepositoryModule.kt   → Repository bindings (:component/* impls)
            ├── DomainModule.kt       → Use Case factories (:component/* use cases)
            └── ViewModelModule.kt    → ViewModel registrations (:feature/* ViewModels)
```

> ❌ Never create Koin modules inside `:feature/*` or `:component/*`.  
> ✅ Components and features use **constructor injection only** — no Koin references inside them.  
> See `skill: module-organization` for full module rules.

## Description
Expert on Dependency Injection (DI) configuration using Koin for the Dividox KMP project. Use this skill when configuring modules, registering dependencies, or setting up test overrides.

## Capabilities
- Configure Koin modules
- Register Repositories, UseCases, ViewModels, and DataSources
- Manage dependency scopes (`single`, `factory`, `viewModel`)
- Set up test DI configurations

## Rules (CRITICAL)
- **Constructor Injection**: All classes must rely solely on constructor injection.
- **Interface Binding**: Always bind interface to implementation, not concrete classes.

## Reference Files
- [DI Patterns and Templates](references/di-patterns.md)

## Common Tasks

### Registering a New ViewModel
```kotlin
viewModel { MyViewModel(useCase = get()) }
```

### Registering a New Repository
```kotlin
single<MyRepository> { MyRepositoryImpl(dataSource = get()) }
```

### Registering a New UseCase
```kotlin
factory { MyUseCase(repository = get()) }
```
