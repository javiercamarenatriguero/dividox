# Navigation Patterns

## Simple Screen (No Arguments)

```kotlin
fun NavGraphBuilder.featureScreenNode(navController: NavController) {
    composable<FeatureRoute> {
        val viewModel = viewModel { FeatureViewModel() }
        val state by collectViewState(viewModel.viewState)

        FeatureScreen(state = state, onEvent = viewModel::onViewEvent)
    }
}
```

## Screen with Route Arguments

```kotlin
composable<FeatureRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<FeatureRoute>()
    val viewModel = viewModel { FeatureViewModel(id = route.id) }
    // ...
}
```

## Side Effect Navigation

```kotlin
private fun handleNavigation(
    effect: FeatureSideEffect.Navigation,
    navController: NavController,
) {
    when (effect) {
        FeatureSideEffect.Navigation.NavigateBack ->
            navController.popBackStack()
        is FeatureSideEffect.Navigation.GoToDetail ->
            navController.navigateToDetail(id = effect.id)
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
