# Navigation Patterns

## Simple Screen (No Arguments)

```kotlin
fun NavGraphBuilder.featureScreenNode(navController: NavController) {
    composable<FeatureRoute> {
        val viewModel = viewModel { FeatureViewModel() }
        val state by collectViewState(viewModel.viewState)

        FeatureScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navigation ->
                when (navigation) {
                    // handle navigation side effects
                }
            },
        )
    }
}
```

## Screen with Route Arguments

```kotlin
composable<FeatureRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<FeatureRoute>()
    val viewModel = viewModel { FeatureViewModel(id = route.id) }
    val state by collectViewState(viewModel.viewState)

    FeatureScreen(
        state = state,
        onEvent = viewModel::onViewEvent,
        sideEffects = viewModel.sideEffect,
        onNavigation = { navigation ->
            when (navigation) {
                FeatureSideEffect.Navigation.NavigateBack ->
                    navController.popBackStack()
            }
        },
    )
}
```

## Side Effect Flow

Side effects flow through the Screen, not the ScreenNode:

```
ViewModel → emitSideEffect() → Screen (CollectSideEffect) → onNavigation callback → ScreenNode → navController
```

The Screen collects side effects internally and delegates navigation ones to `onNavigation`:

```kotlin
// Inside Screen composable
CollectSideEffect(sideEffects) { effect ->
    when (effect) {
        is FeatureSideEffect.Navigation -> onNavigation(effect)
        // Handle non-navigation side effects internally (e.g., haptics, toasts)
    }
}
```

## Nested Navigation (Flows)

```kotlin
fun NavGraphBuilder.onboardingFlow(navController: NavController) {
    navigation<OnboardingGraphRoute>(startDestination = StepOneRoute) {
        composable<StepOneRoute> { /* ... */ }
        composable<StepTwoRoute> { /* ... */ }
    }
}
```

## Dialog Navigation

```kotlin
fun NavGraphBuilder.errorDialogNode(navController: NavController) {
    dialog<ErrorDialogRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ErrorDialogRoute>()
        ErrorDialog(
            message = route.message,
            onDismiss = { navController.popBackStack() },
        )
    }
}
```

## Navigation with Result

```kotlin
// Receive result
composable<SettingsRoute> { backStackEntry ->
    LaunchedEffect(Unit) {
        backStackEntry.savedStateHandle
            .getStateFlow<String?>("result", null)
            .filterNotNull()
            .collect { result -> /* handle */ }
    }
}

// Send result before navigating back
navController.previousBackStackEntry?.savedStateHandle?.set("result", "value")
navController.popBackStack()
```
