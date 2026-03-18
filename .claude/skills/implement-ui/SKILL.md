---
name: implement-ui
description: Scaffolds the UI Layer (Presentation) including MVI Contract, ViewModel, Composable Screen, Navigation, and DI wiring. Use when creating new screens, adding UI features, or implementing presentation logic with MVI pattern.
---

# UI Layer Architect

Scaffold the complete UI/Presentation layer following the MVI pattern and Clean Architecture guidelines.

## 1. Analyze the Request
- Identify the `feature-name` (e.g., `user-profile`, `settings`).
- If not provided, ask the user for it.

## 2. Execution Steps

### Step 1: Create Contract
```kotlin
package com.akole.dividox.feature.[package_name]

interface [FeatureName]Contract {
    data class [FeatureName]ViewState(
        val isLoading: Boolean = false,
    )

    sealed interface [FeatureName]ViewEvent {
        data object OnLoad : [FeatureName]ViewEvent
    }

    sealed interface [FeatureName]SideEffect {
        sealed interface Navigation : [FeatureName]SideEffect {
            data object NavigateBack : Navigation
        }
    }
}
```

### Step 2: Create ViewModel
```kotlin
package com.akole.dividox.feature.[package_name]

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class [FeatureName]ViewModel : ViewModel() {
    // MVI state management
    // Handle events and emit state/effects
}
```

### Step 3: Create Screen
```kotlin
package com.akole.dividox.feature.[package_name]

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

@Composable
fun [FeatureName]Screen(
    state: [FeatureName]ViewState,
    onEvent: ([FeatureName]ViewEvent) -> Unit,
) {
    // UI Content using Material 3
}

@Preview
@Composable
private fun [FeatureName]ScreenPreview() {
    MaterialTheme {
        [FeatureName]Screen(
            state = [FeatureName]ViewState(),
            onEvent = {},
        )
    }
}
```

### Step 4: Register ViewModel in DI

## 3. Verification
- Ensure the package structure is correct.
- Confirm the Screen has the correct signature: `(state, onEvent)`.
- Verify the ViewModel uses proper state management.

## 4. Next Steps
- Wire up navigation using `skill: implement-navigation`.
- Register dependencies in DI using `skill: implement-di`.

## 5. References
- For common patterns (Loading, Forms) and anti-patterns, see [references/mvi-patterns.md](references/mvi-patterns.md).
