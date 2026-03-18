---
name: implement-navigation
description: Manages navigation wiring for features. Use when adding new screens, routes, or navigation flows to the app.
---

# Navigation Manager

Wire up feature navigation following type-safe navigation patterns.

## 1. Analyze the Request
- Identify the `feature-name` (e.g., `user-profile`).
- Identify if it's a top-level route or a nested route.

## 2. Execution Steps

### Step 1: Create/Update Feature Navigation File
```kotlin
package com.akole.dividox.navigation.[feature_name]

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object [FeatureName]Route

fun NavGraphBuilder.[featureName]Navigation(
    navController: NavController,
) {
    composable<[FeatureName]Route> {
        val viewModel: [FeatureName]ViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        [FeatureName]Screen(
            state = state,
            onEvent = viewModel::onViewEvent,
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
