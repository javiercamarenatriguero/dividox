# KMP-Specific Context Patterns

## Architecture Context

### MVI Pattern
```markdown
**Architecture Context**:
- Pattern: MVI (Model-View-Intent)
- Components:
  - ViewState: Immutable UI state snapshot
  - ViewEvent: User actions/UI events
  - SideEffect: One-off effects (navigation, toasts)
  - ViewModel: State management with unidirectional data flow
```

### Clean Architecture
```markdown
**Architecture Context**:
- Layer: [Domain/Data/Presentation]
- Dependencies:
  - Domain: Pure Kotlin, no platform dependencies
  - Data: Repository implementations, data sources
  - Presentation: ViewModels, Composables
```

## Technology Stack Context

### Kotlin Coroutines
```markdown
**Coroutines Context**:
- Dispatchers: IO for network/database, Default for CPU, Main for UI
- Scope: ViewModels use `viewModelScope`
- Repositories: Inject `CoroutineDispatcher`
```

### Compose Multiplatform
```markdown
**Compose Context**:
- Design System: Material 3
- State: Use `remember`, `derivedStateOf` for performance
- Side Effects: Use `LaunchedEffect` for one-time effects
- Recomposition: Optimize with stable parameters
- Platforms: Android, iOS, Desktop (JVM)
```

## Testing Context

### Unit Testing
```markdown
**Testing Context**:
- Framework: kotlin.test + MockK
- Naming: GIVEN/WHEN/THEN format
- Coverage: Target >80% for UseCases, >75% for ViewModels
- Location: `composeApp/src/commonTest/`
```

## Quality Standards

```markdown
**Code Style Requirements**:
- Follow Kotlin coding conventions
- Use meaningful variable names
- Keep functions small and focused
- Avoid hardcoded values
- Use Material 3 design system
- Run detekt for static analysis
```

## Context Selection Guide

| Task Type | Include Context |
|-----------|-----------------|
| New Feature | MVI, Clean Architecture, Compose, DI, Navigation |
| Domain Logic | Clean Architecture, Coroutines, DI |
| UI Work | Compose, Material 3, Performance |
| Testing | Unit Testing, Coverage targets |
| Bug Fix | Relevant tech stack, Error Handling |
| Refactoring | Architecture, Code Style, Performance |
