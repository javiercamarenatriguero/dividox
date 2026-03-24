---
name: implement-navigation
description: Manages navigation wiring for features. Use when adding new screens, routes, or navigation flows to the app.
---

# Navigation Manager

Wire up feature navigation following type-safe navigation patterns.

## Module Location

All navigation wiring lives in **`:app`** → `commonMain/navigation/`.

```
app/
└── src/
    └── commonMain/kotlin/com/akole/dividox/
        └── navigation/
            ├── RootNavGraph.kt               → Root NavHost
            └── [feature]/
                └── [Feature]Navigation.kt    → Feature-specific nav extension
```

> ❌ Never add `composable { }` or `NavGraphBuilder` extensions inside `:feature/*`.  
> ✅ Feature modules only expose `Screen` composables — `:app` wires them to routes.  
> ✅ Routes (`@Serializable` objects) can live in `:feature/*` or `:app/navigation/`.  
> See `skill: module-organization` for full module rules.

## 1. Analyze the Request
- Identify the `feature-name` (e.g., `user-profile`).
- Identify if it's a top-level route or a nested route.

## 2. Execution Steps

### Step 1: Create/Update Feature Navigation File

Use `collectViewState` and `CollectSideEffect` from `:common:mvi` to wire state and side effects.

```kotlin
package com.akole.dividox.navigation.[feature_name]

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.akole.dividox.common.mvi.CollectSideEffect
import com.akole.dividox.common.mvi.collectViewState
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object [FeatureName]Route

fun NavGraphBuilder.[featureName]Navigation(
    navController: NavController,
) {
    composable<[FeatureName]Route> {
        val viewModel: [FeatureName]ViewModel = koinViewModel()
        val state by collectViewState(viewModel.viewState)

        [FeatureName]Screen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    [FeatureName]SideEffect.Navigation.NavigateBack ->
                        navController.popBackStack()
                }
            },
        )
    }
}
```

### Step 2: Add to Graph
- Add the function call: `[featureName]Navigation(navController)` to the appropriate NavHost.

## 3. Verification
- Ensure Route is `@Serializable`.
- Ensure ViewModel is resolved correctly.
- Ensure Screen signature matches the one in the feature module.

## 4. References
- For core principles, see [references/navigation-rules.md](references/navigation-rules.md).
- For specific navigation patterns (Nested, Dialogs, Results), see [references/navigation-patterns.md](references/navigation-patterns.md).
