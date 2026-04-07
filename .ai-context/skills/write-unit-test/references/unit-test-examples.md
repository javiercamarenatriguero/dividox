# Unit Test Examples

## ViewModel Test Template

ViewModels implement the `MVI` interface from `:common:mvi`.
Access state via `sut.viewState.value` and side effects via `sut.sideEffect`.

```kotlin
class FeatureViewModelTest {

    private lateinit var sut: FeatureViewModel

    // Mocked dependencies
    private val mockUseCase = mockk<MyUseCase>(relaxed = true)
    private val mockRepository = mockk<MyRepository>(relaxed = true)

    @Before
    fun setup() {
        sut = FeatureViewModel(
            myUseCase = mockUseCase,
            myRepository = mockRepository,
        )
    }

    @Test
    fun `SHOULD navigate to details WHEN OnActionClicked is called`() = runTest {
        // WHEN
        sut.onViewEvent(FeatureViewEvent.OnActionClicked)

        sut.sideEffect.test {
            // THEN
            assertEquals(FeatureSideEffect.Navigation.GoToDetails, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SHOULD update state WHEN OnLoad is called GIVEN repository has data`() = runTest {
        // GIVEN
        val expectedData = "Test Data"
        every { mockRepository.getData() } returns flow { emit(expectedData) }

        // WHEN
        sut.onViewEvent(FeatureViewEvent.OnLoad)

        // THEN
        assertEquals(
            FeatureViewState(data = expectedData),
            sut.viewState.value   // ← viewState, not state (MVI interface property)
        )
    }
}
```

## Use Case Test Template

```kotlin
class GetDataUseCaseTest {

    private lateinit var sut: GetDataUseCase
    private val mockRepository = mockk<DataRepository>(relaxed = true)

    @Before
    fun setup() {
        sut = GetDataUseCase(mockRepository)
    }

    @Test
    fun `SHOULD return data WHEN repository has valid data`() = runTest {
        // GIVEN
        val expected = listOf(DataItem("1"), DataItem("2"))
        coEvery { mockRepository.getData() } returns Result.success(expected)

        // WHEN
        val result = sut()

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `SHOULD return failure WHEN repository fails`() = runTest {
        // GIVEN
        coEvery { mockRepository.getData() } returns Result.failure(Exception("Error"))

        // WHEN
        val result = sut()

        // THEN
        assertTrue(result.isFailure)
    }
}
```

## Common MockK Patterns

```kotlin
// Return value
every { mockRepository.getData() } returns "data"

// Suspend function
coEvery { mockRepository.fetchData() } returns Result.success("data")

// Verify called
verify { mockRepository.getData() }

// Verify suspend function
coVerify { mockRepository.fetchData() }
```
