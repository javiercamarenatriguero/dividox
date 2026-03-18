# MVI Patterns and Best Practices

## Common MVI Patterns

### Loading State Pattern
```kotlin
data class ViewState(
    val isLoading: Boolean = false,
    val data: Data? = null,
    val error: String? = null
)

// In ViewModel
private fun loadData() {
    viewModelScope.launch {
        updateState { copy(isLoading = true, error = null) }

        repository.getData()
            .onSuccess { data ->
                updateState { copy(isLoading = false, data = data) }
            }
            .onFailure { error ->
                updateState { copy(isLoading = false, error = error.message) }
            }
    }
}
```

### Form State Pattern
```kotlin
data class FormViewState(
    val name: String = "",
    val email: String = "",
    val isValid: Boolean = false,
    val isSubmitting: Boolean = false
)

sealed interface FormViewEvent {
    data class OnNameChanged(val value: String) : FormViewEvent
    data class OnEmailChanged(val value: String) : FormViewEvent
    data object OnSubmitClicked : FormViewEvent
}
```

## Anti-Patterns to Avoid

### Don't access ViewModel directly from Screen
```kotlin
// BAD
@Composable
fun FeatureScreen(viewModel: FeatureViewModel) {
    val state = viewModel.state.collectAsState()
}

// GOOD
@Composable
fun FeatureScreen(
    state: FeatureViewState,
    onEvent: (FeatureViewEvent) -> Unit
)
```

### Don't use mutable state in ViewModel
```kotlin
// BAD
class FeatureViewModel : ViewModel() {
    var data by mutableStateOf("")
}

// GOOD - Use StateFlow or MVI delegate
class FeatureViewModel : ViewModel() {
    private val _state = MutableStateFlow(FeatureViewState())
    val state: StateFlow<FeatureViewState> = _state.asStateFlow()
}
```

### Don't navigate directly from ViewModel
```kotlin
// BAD
private fun handleAction() {
    navController.navigate("route")
}

// GOOD - Emit side effect
private fun handleAction() {
    emitSideEffect(FeatureSideEffect.Navigation.NavigateToDetails)
}
```
