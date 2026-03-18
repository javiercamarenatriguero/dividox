# Navigation Routing Rules

## Core Principles

1. **Type Safety**: Use Kotlin Serialization for all routes.
2. **Separation**: Features expose screens only; navigation is wired externally.
3. **No NavController in ViewModel**: ViewModels should be unaware of the navigation framework.

## Rules

### Do's
- Use `@Serializable` route objects/data classes.
- Handle navigation via side effects or callbacks.
- Keep navigation logic in the app/navigation layer.

### Don'ts
- Never use `composable { }` inside feature modules.
- Never pass NavController to ViewModels.
- Avoid hardcoded string routes like `"home/details/{id}"`.
