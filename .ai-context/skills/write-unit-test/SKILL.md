---
name: write-unit-test
description: Guide for writing unit tests for ViewModels, UseCases, and Extensions. Use this skill when you need to create a new unit test, following the project's standard patterns like GIVEN/WHEN/THEN naming.
---

# Unit Test Writer

This skill provides guidance and templates for writing unit tests in the Dividox KMP project.

## Test Source Sets

| Source set | When to use | Mocking strategy |
|---|---|---|
| `jvmTest` | ViewModels, UseCases, DataStore implementations | **MockK** (`mockk`, `coEvery`, `verify`) |
| `commonTest` | Pure domain logic that must run on all platforms | **Fakes** (manual implementations of interfaces) |

> ⚠️ `io.mockk:mockk` is **JVM-only**. It cannot be used in `commonTest`. Always put MockK-based tests in `jvmTest`.

To add MockK to a module's `jvmTest`:
```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}
```

### When to use Fakes instead of MockK
Use a hand-written Fake when the test must live in `commonTest` (shared across platforms) or when the fake needs stateful behaviour (e.g. a `MutableStateFlow` that reacts to calls):

```kotlin
class FakeAppSettingsDataStore : AppSettingsDataStore {
    private val _settings = MutableStateFlow(AppSettings())

    override fun observe(): Flow<AppSettings> = _settings

    override suspend fun setCurrency(currency: Currency) {
        _settings.update { it.copy(currency = currency) }
    }
}
```

---

## Standard Patterns

### Naming Convention
Tests must follow the **GIVEN/WHEN/THEN** naming pattern:
`SHOULD [expected behavior] WHEN [action] GIVEN [condition]`

Example:
`SHOULD update state to loading WHEN OnLoad event is received GIVEN initial state`

### Structure
Tests should be structured with comments separating the phases:
```kotlin
@Test
fun `SHOULD ... WHEN ... GIVEN ...`() = runTest {
    // GIVEN
    val expected = "value"

    // WHEN
    val result = sut.doSomething()

    // THEN
    assertEquals(expected, result)
}
```

### Key Components
1. **MockK**: Use `mockk`, `every`, `coEvery`, `verify`, `coVerify` for mocking dependencies.
2. **Coroutines**: Use `runTest` for suspend functions.
3. **Turbine**: Use `.test { ... }` for testing Flows.
4. **kotlin.test**: Use `assertEquals`, `assertTrue`, `assertFalse` for assertions.

## Creating a New Test

### 1. ViewModels
**Location**: `composeApp/src/commonTest/kotlin/com/akole/dividox/[feature]/[Name]ViewModelTest.kt`

**Key Steps**:
- Mock dependencies using `mockk(relaxed = true)`.
- Initialize `sut` in setup.
- Use `UnconfinedTestDispatcher()` for ViewModels if needed.

### 2. Use Cases and Pure Functions
**Location**: `composeApp/src/commonTest/kotlin/com/akole/dividox/[module]/[Name]Test.kt`

**Key Steps**:
- Test pure functions directly.
- Use standard assertions (`assertEquals`, `assertTrue`).

## Best Practices
- **Flows**: Use `turbine` to test state emissions. Always `cancelAndIgnoreRemainingEvents()` or consume all events.
- **Naming**: Be descriptive. The test name should read like a requirement.
- **One assertion per test**: Keep tests focused on a single behavior.

## References
- For detailed templates and examples (ViewModel, UseCase), see [references/unit-test-examples.md](references/unit-test-examples.md).
