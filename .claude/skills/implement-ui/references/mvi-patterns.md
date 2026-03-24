# MVI Patterns and Best Practices

## Common MVI Patterns

> All patterns use the `mvi()` delegate from `:common:mvi`.  
> Import: `com.akole.dividox.common.mvi.viewmodel.{MVI, mvi}`

### Loading State Pattern
```kotlin
data class ViewState(
    val isLoading: Boolean = false,
    val data: Data? = null,
    val error: String? = null,
) : com.akole.dividox.common.mvi.ViewState

// In ViewModel — use updateViewState { copy(...) }
private fun loadData() {
    viewModelScope.launch {
        updateViewState { copy(isLoading = true, error = null) }

        repository.getData()
            .onSuccess { data ->
                updateViewState { copy(isLoading = false, data = data) }
            }
            .onFailure { error ->
                updateViewState { copy(isLoading = false, error = error.message) }
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
    val isSubmitting: Boolean = false,
) : com.akole.dividox.common.mvi.ViewState

sealed interface FormViewEvent : com.akole.dividox.common.mvi.ViewEvent {
    data class OnNameChanged(val value: String) : FormViewEvent
    data class OnEmailChanged(val value: String) : FormViewEvent
    data object OnSubmitClicked : FormViewEvent
}
```

### Side Effect Emission
```kotlin
// Always use viewModelScope.emitSideEffect — never launch a separate coroutine manually
private fun navigateToDetails(id: String) {
    viewModelScope.emitSideEffect(FeatureSideEffect.Navigation.GoToDetails(id))
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

// BAD — raw MutableStateFlow bypasses the MVI contract
class FeatureViewModel : ViewModel() {
    private val _state = MutableStateFlow(FeatureViewState())
    val state: StateFlow<FeatureViewState> = _state.asStateFlow()
}

// GOOD — use the mvi() delegate from :common:mvi
class FeatureViewModel : ViewModel(),
    MVI<FeatureViewState, FeatureViewEvent, FeatureSideEffect>
    by mvi(FeatureViewState()) {

    override fun onViewEvent(viewEvent: FeatureViewEvent) { ... }
}
```

### Don't navigate directly from ViewModel
```kotlin
// BAD
private fun handleAction() {
    navController.navigate("route")
}

// GOOD — emit side effect via viewModelScope.emitSideEffect
private fun handleAction() {
    viewModelScope.emitSideEffect(FeatureSideEffect.Navigation.NavigateToDetails)
}
```
