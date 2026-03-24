---
name: implement-navigation
description: Describes and enforces the type-safe KMP navigation pattern. Use when adding new screens, routes, or navigation flows to the app.
---

# Navigation Pattern

## Pattern Overview

Navigation follows a **wiring-layer pattern** where feature modules expose `Screen` composables (which collect their own side effects) and the app module wires them to type-safe routes via `NavGraphBuilder` extensions.

```
Screen (feature module)  ←  ScreenNode (navigation wiring)  ←  RootNavGraph  ←  App.kt
       receives state,          creates ViewModel                 NavHost          rememberNavController
       onEvent, sideEffects,    collects state
       onNavigation             handles onNavigation callback
       collects side effects
```

### Key Concepts

| Concept | Description |
|---------|-------------|
| **Route** | `@Serializable` object/data class. Co-located in its `[Feature]Navigation.kt` |
| **navigateTo Extension** | `NavController.navigateTo*()` extension, co-located with its route |
| **ScreenNode** | `NavGraphBuilder` extension that wires ViewModel → Screen for a given route |
| **RootNavGraph** | Single `NavHost` composable that registers all screen nodes |
| **Side Effect Handling** | Screen collects side effects internally via `CollectSideEffect` and delegates navigation to `onNavigation` callback. ScreenNode only handles `onNavigation`. |

### Principles

1. **Type Safety** — Routes use `@Serializable` via `kotlinx.serialization`, no string-based routing
2. **Co-located Routes** — Route + `navigateTo*()` + ScreenNode live in the same `[Feature]Navigation.kt`
3. **Separation** — Feature modules expose `Screen` composables only; navigation wiring lives in `composeApp/navigation/`
4. **No NavController in ViewModel** — ViewModels emit side effects; Screens collect them and delegate navigation to `onNavigation`
5. **KMP Compatible** — Uses `org.jetbrains.androidx.navigation:navigation-compose`

## Directory Structure

```
composeApp/src/commonMain/kotlin/.../
└── navigation/
    ├── RootNavGraph.kt              → SetupRootNavGraph with NavHost
    ├── HomeNavigation.kt            → HomeRoute + navigateToHome() + homeScreenNode()
    ├── [Feature]Navigation.kt       → [Feature]Route + navigateTo[Feature]() + [feature]ScreenNode()
    └── ...
```

## How to Add a New Screen

### 1. Define Route and Navigation Extension

Each `[Feature]Navigation.kt` file contains its route, `navigateTo*` extension, and screen node:

```kotlin
// [Feature]Navigation.kt
@Serializable
data object FeatureRoute

fun NavController.navigateToFeature(navOptions: NavOptions? = null) {
    this.navigate(FeatureRoute, navOptions)
}

// With arguments
@Serializable
data class ProfileRoute(val userId: String)

fun NavController.navigateToProfile(userId: String, navOptions: NavOptions? = null) {
    this.navigate(ProfileRoute(userId = userId), navOptions)
}
```

### 2. Create ScreenNode

The ScreenNode creates the ViewModel, collects state, and passes everything to the Screen. The Screen handles side effects internally — the ScreenNode only provides the `onNavigation` callback.

```kotlin
// [Feature]Navigation.kt
fun NavGraphBuilder.[feature]ScreenNode(navController: NavController) {
    composable<FeatureRoute> {
        val viewModel = viewModel { FeatureViewModel() }
        val state by collectViewState(viewModel.viewState)

        FeatureScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    FeatureSideEffect.Navigation.NavigateBack ->
                        navController.popBackStack()
                    is FeatureSideEffect.Navigation.GoToDetail ->
                        navController.navigateToDetail(id = navigation.id)
                }
            },
        )
    }
}
```

**With route arguments:**

```kotlin
composable<FeatureRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<FeatureRoute>()
    val viewModel = viewModel { FeatureViewModel(id = route.id) }
    // ...
}
```

### 3. Register in RootNavGraph

```kotlin
NavHost(navController, startDestination = HomeRoute) {
    [feature]ScreenNode(navController)
}
```

### 4. Navigate To

Use the `NavController` extension from the corresponding `[Feature]Navigation.kt`:

```kotlin
navController.navigateToFeature()
navController.navigateToProfile(userId = "123")
```

## Rules

### Do's ✅

- Co-locate route, `navigateTo*()` extension, and screen node in the same `[Feature]Navigation.kt`
- Use `navigateTo*()` extensions instead of raw `navController.navigate(Route)`
- Use `collectViewState()` from `:common:mvi` for state collection in the ScreenNode
- Pass `sideEffects` and `onNavigation` to the Screen — let the Screen collect side effects via `CollectSideEffect`
- Name extensions as `[feature]ScreenNode(navController)`

### Don'ts ❌

- **NEVER** add `composable {}` inside feature modules
- **NEVER** pass `NavController` to ViewModels
- **NEVER** use hardcoded string routes
- **NEVER** navigate from inside a ViewModel — emit a side effect
- **NEVER** collect side effects in the ScreenNode — the Screen does that

## References

- For advanced patterns (Nested Graphs, Dialogs, Results), see [references/navigation-patterns.md](references/navigation-patterns.md).
- For naming and dependency rules, see [references/navigation-rules.md](references/navigation-rules.md).
