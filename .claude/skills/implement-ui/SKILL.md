---
name: implement-ui
description: Scaffolds the UI Layer (Presentation) including MVI Contract, ViewModel, Composable Screen, Navigation, and DI wiring. Use when creating new screens, adding UI features, or implementing presentation logic with MVI pattern.
---

# UI Layer Architect

Scaffold the complete UI/Presentation layer following the MVI pattern and Clean Architecture guidelines.

## Module Location

All code lives in **`:feature:[name]`** → `commonMain`.

```
feature/
└── [name]/
    ├── build.gradle.kts
    └── src/
        ├── commonMain/kotlin/com/akole/dividox/feature/[name]/
        │   ├── [Name]Contract.kt
        │   ├── [Name]ViewModel.kt
        │   └── [Name]Screen.kt
        └── commonTest/kotlin/com/akole/dividox/feature/[name]/
            └── [Name]ViewModelTest.kt
```

> ❌ No `androidMain`/`iosMain` in feature modules — all Compose Multiplatform code is in `commonMain`.  
> ❌ No navigation wiring here — that goes in `:app` → `commonMain/navigation/`.  
> See `skill: module-organization` for full module rules.

## 1. Analyze the Request
- Identify the `feature-name` (e.g., `user-profile`, `settings`).
- If not provided, ask the user for it.
- Check that the `:feature:[name]` module exists in `settings.gradle.kts`; create it if not.

## 2. Execution Steps

### Step 1: Create Contract

All marker interfaces extend from `:common:mvi` (`com.akole.dividox.common.mvi`).

```kotlin
package com.akole.dividox.feature.[package_name]

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState

interface [FeatureName]Contract {
    data class [FeatureName]ViewState(
        val isLoading: Boolean = false,
    ) : ViewState

    sealed interface [FeatureName]ViewEvent : ViewEvent {
        data object OnLoad : [FeatureName]ViewEvent
    }

    sealed interface [FeatureName]SideEffect : SideEffect {
        sealed interface Navigation : [FeatureName]SideEffect {
            data object NavigateBack : Navigation
        }
    }
}
```

### Step 2: Create ViewModel

Use the `mvi()` delegate from `:common:mvi` — never use raw `MutableStateFlow` for state management.

```kotlin
package com.akole.dividox.feature.[package_name]

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi

class [FeatureName]ViewModel(
    private val someUseCase: SomeUseCase,
) : ViewModel(),
    MVI<[FeatureName]ViewState, [FeatureName]ViewEvent, [FeatureName]SideEffect>
    by mvi([FeatureName]ViewState()) {

    override fun onViewEvent(viewEvent: [FeatureName]ViewEvent) {
        when (viewEvent) {
            [FeatureName]ViewEvent.OnLoad -> onLoad()
        }
    }

    private fun onLoad() {
        viewModelScope.launch {
            updateViewState { copy(isLoading = true) }
            someUseCase()
                .onSuccess { data -> updateViewState { copy(isLoading = false, data = data) } }
                .onFailure { updateViewState { copy(isLoading = false) } }
        }
    }

    private fun navigateBack() {
        viewModelScope.emitSideEffect([FeatureName]SideEffect.Navigation.NavigateBack)
    }
}
```

### Step 3: Create Screen

Use `collectViewState` and `CollectSideEffect` from `:common:mvi`.

```kotlin
package com.akole.dividox.feature.[package_name]

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import com.akole.dividox.common.mvi.CollectSideEffect
import kotlinx.coroutines.flow.Flow

@Composable
fun [FeatureName]Screen(
    state: [FeatureName]ViewState,
    onEvent: ([FeatureName]ViewEvent) -> Unit,
    sideEffects: Flow<[FeatureName]SideEffect>,
    onNavigation: ([FeatureName]SideEffect.Navigation) -> Unit,
) {
    CollectSideEffect(sideEffects) { effect ->
        when (effect) {
            is [FeatureName]SideEffect.Navigation -> onNavigation(effect)
        }
    }

    // UI Content using Material 3
}

@Preview
@Composable
private fun [FeatureName]ScreenPreview() {
    MaterialTheme {
        [FeatureName]Screen(
            state = [FeatureName]ViewState(),
            onEvent = {},
            sideEffects = kotlinx.coroutines.flow.emptyFlow(),
            onNavigation = {},
        )
    }
}
```

### Step 4: Register ViewModel in DI

Add to `:app/di/ViewModelModule.kt`:

```kotlin
viewModel { [FeatureName]ViewModel(get()) }
```

Add `:common:mvi` to the feature module's dependencies if not already present:

```kotlin
// feature/[name]/build.gradle.kts
commonMain.dependencies {
    implementation(projects.common.mvi)
}
```

## 3. Verification
- Ensure the package structure is correct.
- Confirm the Screen has the correct signature: `(state, onEvent)`.
- Verify the ViewModel uses proper state management.

## 4. Next Steps
- Wire up navigation using `skill: implement-navigation`.
- Register dependencies in DI using `skill: implement-di`.

## 5. References
- For common patterns (Loading, Forms) and anti-patterns, see [references/mvi-patterns.md](references/mvi-patterns.md).
