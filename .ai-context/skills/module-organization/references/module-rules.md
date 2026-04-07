# Module Rules & Anti-patterns — Dividox KMP

## Dependency Flow (Clean Architecture)

```
:app
 └── :feature/[name]       (Presentation)
       └── :component/[name]   (Domain + Data)
             └── :common/[name]    (Shared utils)
```

Dependencies always flow **inward**. Inner layers never know about outer layers.

---

## Creating a New Module

### Gradle setup for `:feature:[name]`
```kotlin
// feature/[name]/build.gradle.kts
plugins {
    alias(libs.plugins.dividox.kmp.library)
    alias(libs.plugins.dividox.compose.multiplatform)
    alias(libs.plugins.dividox.kmp.ios)
    alias(libs.plugins.dividox.kmp.test)
}

android {
    namespace = "com.akole.dividox.feature.[name]"
}
```

### Gradle setup for `:component:[name]`
```kotlin
// component/[name]/build.gradle.kts
plugins {
    alias(libs.plugins.dividox.kmp.library)
    alias(libs.plugins.dividox.kmp.ios)
    alias(libs.plugins.dividox.kmp.test)
}

android {
    namespace = "com.akole.dividox.component.[name]"
}
```

### Register in `settings.gradle.kts`
```kotlin
include(":feature:[name]")
include(":component:[name]")
```

---

## Package Naming Convention

```
com.akole.dividox.[module-type].[name].[layer].[sublayer]
```

Examples:
- `com.akole.dividox.feature.expenses.ExpensesContract`
- `com.akole.dividox.feature.expenses.ExpensesViewModel`
- `com.akole.dividox.component.expenses.domain.usecase.GetExpensesUseCase`
- `com.akole.dividox.component.expenses.data.repository.ExpensesRepositoryImpl`
- `com.akole.dividox.common.ui.components.DividoxButton`

---

## KMP Source Set Rules

### What goes in `commonMain`
- All business logic (Use Cases, Repository interfaces, Domain models)
- Compose Multiplatform screens and ViewModels
- Navigation wiring (in `:app`)
- DI configuration (in `:app`)

### What goes in platform source sets
- `androidMain`: Android-specific implementations (Room, DataStore, WorkManager)
- `iosMain`: iOS-specific implementations (NSUserDefaults, CoreData)
- `jvmMain`: Desktop-specific implementations
- Use `expect/actual` to declare the contract in `commonMain`

### expect/actual pattern
```kotlin
// commonMain
expect class PlatformPreferences {
    fun getString(key: String): String?
    fun setString(key: String, value: String)
}

// androidMain
actual class PlatformPreferences(private val context: Context) {
    actual fun getString(key: String) = /* SharedPreferences impl */
    actual fun setString(key: String, value: String) = /* impl */
}

// iosMain
actual class PlatformPreferences {
    actual fun getString(key: String) = /* NSUserDefaults impl */
    actual fun setString(key: String, value: String) = /* impl */
}
```

---

## Anti-patterns

### ❌ Feature importing Feature
```kotlin
// feature/expenses/build.gradle.kts
dependencies {
    implementation(project(":feature:groups"))  // FORBIDDEN
}
```
**Fix**: Extract shared data to `:component/[shared]` and import it from both features.

### ❌ Component importing Component
```kotlin
// component/expenses/build.gradle.kts
dependencies {
    implementation(project(":component:groups"))  // FORBIDDEN
}
```
**Fix**: Create `:integration:expenses-groups` that coordinates both.

### ❌ Business logic in `:app`
```kotlin
// app/.../di/AppModule.kt
val appModule = module {
    factory { calculateSplit(members) }  // FORBIDDEN - belongs in UseCase
}
```

### ❌ Android imports in `commonMain`
```kotlin
// commonMain - FORBIDDEN
import android.content.Context
import androidx.room.Room
```
**Fix**: Use `expect/actual` or move to `androidMain`.

### ❌ Compose in `:component`
```kotlin
// component/expenses/... - FORBIDDEN
@Composable
fun ExpensesWidget() { ... }
```
**Fix**: Move to `:feature/expenses` or `:common/ui`.

---

## When to Create an Integration Module

Create `:integration:[A]-[B]` when:
- A Use Case in `component/A` needs data produced by `component/B`
- Two components need to react to each other's events
- A cross-cutting flow (e.g., authentication + user-preferences) requires orchestration

```
:integration:expenses-groups
  ├── depends on :component:expenses
  ├── depends on :component:groups
  └── exposes: SplitExpenseWithGroupUseCase
```
