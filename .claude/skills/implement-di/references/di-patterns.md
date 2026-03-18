# Dependency Injection Patterns (Koin)

## Module Templates

### AppModule (Framework)
**Purpose**: Cross-cutting, platform and framework providers.

```kotlin
val appModule = module {
    // Dispatchers
    single(named(Dispatcher.IO)) { Dispatchers.IO }
    single(named(Dispatcher.DEFAULT)) { Dispatchers.Default }
    single(named(Dispatcher.MAIN)) { Dispatchers.Main }

    // Scopes
    single(named("ApplicationScope")) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(named(Dispatcher.DEFAULT)))
    }
}
```

### RepositoryModule (Data Layer)
**Purpose**: Concrete repository implementations.

```kotlin
val repositoryModule = module {
    single<SessionRepository> {
        SessionRepositoryImpl(
            ioDispatcher = get(named(Dispatcher.IO))
        )
    }
}
```

### ViewModelModule (Presentation)
**Purpose**: ViewModel registrations.

```kotlin
val viewModelModule = module {
    viewModel {
        FeatureViewModel(
            useCase = get()
        )
    }
}
```

## Best Practices

### Interface -> Implementation
Always bind the interface, not the implementation class.

```kotlin
// Good
single<MyRepository> { MyRepositoryImpl(get()) }

// Bad
single { MyRepositoryImpl(get()) }
```

### Named Qualifiers
Always use named qualifiers for dispatchers.

```kotlin
single(named(Dispatcher.IO)) { Dispatchers.IO }
```

### Scopes
| Dependency Type | Scope       | Reason                            |
|-----------------|-------------|-----------------------------------|
| Repository      | `single`    | Shared state, expensive to create |
| Handler         | `single`    | Maintains connections/state       |
| Use Case        | `factory`   | Stateless, cheap to create        |
| ViewModel       | `viewModel` | Lifecycle-aware                   |
| Dispatcher      | `single`    | Platform resource                 |

## Testing with DI

### KoinTest Rule

```kotlin
class MyUseCaseTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single<MyRepository> { FakeMyRepository() }
                single(named(Dispatcher.IO)) { Dispatchers.Unconfined }
            }
        )
    }

    @Test
    fun `test use case with DI`() {
        val useCase: MyUseCase = get()
        // Test implementation
    }
}
```
