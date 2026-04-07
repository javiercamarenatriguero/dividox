# Dependency Injection Patterns (Koin)

## KoinInitializer

### commonMain

```kotlin
object KoinInitializer {
    fun init(config: KoinApplication.() -> Unit = {}) {
        startKoin {
            config()
            modules(
                appModule,
                viewModelModule,
                // add new modules here
            )
        }
    }
}
```

### androidMain — extension with Android context

```kotlin
fun KoinInitializer.init(application: Application) {
    init {
        androidLogger()
        androidContext(application)
    }
}
```

### Android entry point (`DividoxApplication.kt`)

```kotlin
class DividoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KoinInitializer.init(this)
    }
}
```

### Desktop entry point (`main.kt`)

```kotlin
fun main() {
    KoinInitializer.init()
    application {
        Window(...) { App() }
    }
}
```

### iOS entry point (`MainViewController.kt`)

```kotlin
fun MainViewController() = run {
    KoinInitializer.init()
    ComposeUIViewController { App() }
}
```

> ⚠️ iOS: use `run { }` so `init()` is called once when the controller is created,
> not inside the Compose lambda (which recomposes).

---

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
    // No constructor params — injected via get()
    viewModel {
        FeatureViewModel(useCase = get())
    }

    // With call-site params — injected via parametersOf()
    viewModel { params ->
        HomeViewModel(
            greeting = params.get(),
            platformName = params.get(),
        )
    }
}
```

### Consuming a parameterized ViewModel in Compose

```kotlin
val viewModel = koinViewModel<HomeViewModel>(
    parameters = { parametersOf(greeting, platformName) }
)
```

---

## Best Practices

### Interface → Implementation
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

---

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
