# Navigation Routing Rules

## Core Principles

1. **Type Safety**: All routes use `@Serializable` via `kotlinx.serialization`.
2. **Co-located**: Route, `navigateTo*()` extension, and ScreenNode live together in `[Feature]Navigation.kt`.
3. **Separation**: Feature modules expose stateless `Screen` composables only — navigation wiring lives in `composeApp/navigation/`.
4. **No NavController in ViewModel**: ViewModels emit side effects; navigation files translate them to `navController` calls.
5. **KMP Compatible**: Uses `org.jetbrains.androidx.navigation:navigation-compose` (not the Android-only variant).

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Route (no args) | `@Serializable data object [Name]Route` | `HomeRoute` |
| Route (with args) | `@Serializable data class [Name]Route(val arg: Type)` | `DetailRoute(val id: String)` |
| Navigate extension | `fun NavController.navigateTo[Name](navOptions?)` | `navigateToDetail(id, navOptions)` |
| Nav file | `[Feature]Navigation.kt` | `DetailNavigation.kt` |
| Nav extension | `fun NavGraphBuilder.[feature]ScreenNode(navController)` | `detailScreenNode(navController)` |
| Nav handler | `private fun handle[Feature]Navigation(effect, navController)` | `handleDetailNavigation(...)` |

## Rules

### Do's ✅

- Use `@Serializable` route objects/data classes
- Create `NavController.navigateTo*()` extension per route, co-located in `[Feature]Navigation.kt`
- Use `navigateTo*()` extensions instead of raw `navController.navigate(Route)`
- Co-locate route + extension + screenNode in the same `[Feature]Navigation.kt`
- Use `collectViewState()` from `:common:mvi` for state collection in ScreenNode
- Pass `sideEffects` and `onNavigation` to the Screen — let the Screen call `CollectSideEffect`
- Handle navigation via `onNavigation` callback in the ScreenNode
- Keep all navigation logic in `composeApp/navigation/`
- Use `backStackEntry.toRoute<Route>()` to extract route arguments

### Don'ts ❌

- **NEVER** use `composable { }` inside feature modules
- **NEVER** pass `NavController` to ViewModels
- **NEVER** use hardcoded string routes like `"home/details/{id}"`
- **NEVER** navigate from inside a ViewModel — emit a side effect instead
- **NEVER** define routes in feature modules — co-locate in `[Feature]Navigation.kt`
- **NEVER** collect side effects in the ScreenNode — the Screen does that via `CollectSideEffect`

## Dependencies

```toml
# gradle/libs.versions.toml
[libraries]
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "androidx-navigation" }
kotlinx-serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "kotlinx-serialization" }
```

```kotlin
// composeApp/build.gradle.kts
plugins {
    alias(libs.plugins.kotlinxSerialization)
}
commonMain.dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.core)
}
```
