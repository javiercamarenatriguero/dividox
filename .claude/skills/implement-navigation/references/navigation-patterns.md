# Navigation Patterns

## Type-Safe Navigation

Use Kotlin Serialization for type-safe routes:

```kotlin
@Serializable
object WelcomeRoute

@Serializable
data class DetailsRoute(val itemId: String)

// Navigation with arguments
navController.navigate(DetailsRoute(itemId = "123"))

// Composable with arguments
composable<DetailsRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<DetailsRoute>()
    DetailsScreen(itemId = route.itemId)
}
```

## Common Scenarios

### Simple Navigation

```kotlin
fun NavGraphBuilder.featureNavigation(navController: NavController) {
    composable<FeatureRoute> {
        val viewModel: FeatureViewModel = koinViewModel()
        val state by collectViewState(viewModel.viewState)   // from :common:mvi

        FeatureScreen(
            state = state,
            onEvent = viewModel::onViewEvent,
            sideEffects = viewModel.sideEffect,
            onNavigation = { navController.popBackStack() },
        )
    }
}
```

### Nested Navigation (Flows)

```kotlin
fun NavGraphBuilder.onboardingNavigation(navController: NavController) {
    navigation<OnboardingGraphRoute>(startDestination = StepOneRoute) {
        composable<StepOneRoute> { /* ... */ }
        composable<StepTwoRoute> { /* ... */ }
        composable<StepThreeRoute> { /* ... */ }
    }
}
```

### Dialog Navigation

```kotlin
composable<ErrorDialogRoute> {
    val route = it.toRoute<ErrorDialogRoute>()
    ErrorDialog(
        message = route.message,
        onDismiss = { navController.popBackStack() },
    )
}
```

### Deep Links

```kotlin
composable<FeatureRoute>(
    deepLinks = listOf(
        navDeepLink<FeatureRoute>(basePath = "dividox://feature")
    )
) {
    FeatureScreen(/* ... */)
}
```

### Navigation with Result

```kotlin
composable<DetailsRoute> { backStackEntry ->
    val savedStateHandle = backStackEntry.savedStateHandle

    LaunchedEffect(Unit) {
        savedStateHandle.getStateFlow<String?>("result", null)
            .filterNotNull()
            .collect { result -> /* Handle result */ }
    }
}

// Set result before navigating back
savedStateHandle["result"] = "some_result"
navController.popBackStack()
```
