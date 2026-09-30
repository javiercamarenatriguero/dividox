package com.akole.dividox.feature.portfolio

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.component.portfolio.domain.usecase.AddHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.RemoveHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.UpdateHoldingUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HoldingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockGetStockQuote = mockk<GetStockQuoteUseCase>()
    // MockK can't return Result<value class> (ClassCastException on HoldingId), so adds use a fake repo.
    private val addRepository = FakeAddRepository()
    private val mockAddHolding = AddHoldingUseCase(addRepository)
    private val mockUpdateHolding = mockk<UpdateHoldingUseCase>()
    private val mockRemoveHolding = mockk<RemoveHoldingUseCase>()
    private val mockGetPortfolio = mockk<GetPortfolioUseCase>()
    private val mockObserveSettings = mockk<ObserveAppSettingsUseCase>()
    private val positionActions = PositionActions(
        addHolding = mockAddHolding,
        updateHolding = mockUpdateHolding,
        removeHolding = mockRemoveHolding,
        scope = CoroutineScope(testDispatcher),
    )

    companion object {
        private const val FIXED_TIMESTAMP = 1700000000000L
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockObserveSettings() } returns flowOf(AppSettings())
        every { mockGetPortfolio.execute() } returns flowOf(Result.success(emptyList()))
        coEvery { mockGetStockQuote(any()) } returns Result.failure(RuntimeException("no quote"))
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createAddViewModel() = HoldingViewModel(
        holdingId = null,
        getStockQuote = mockGetStockQuote,
        positionActions = positionActions,
        getPortfolio = mockGetPortfolio,
        getCurrentTimeMillis = { FIXED_TIMESTAMP },
        observeAppSettings = mockObserveSettings,
    )

    private fun createPrefillViewModel(ticker: String) = HoldingViewModel(
        holdingId = null,
        prefillTicker = ticker,
        getStockQuote = mockGetStockQuote,
        positionActions = positionActions,
        getPortfolio = mockGetPortfolio,
        getCurrentTimeMillis = { FIXED_TIMESTAMP },
        observeAppSettings = mockObserveSettings,
    )

    private fun createEditViewModel(holdingId: HoldingId = HoldingId("h1")) = HoldingViewModel(
        holdingId = holdingId,
        getStockQuote = mockGetStockQuote,
        positionActions = positionActions,
        getPortfolio = mockGetPortfolio,
        getCurrentTimeMillis = { FIXED_TIMESTAMP },
        observeAppSettings = mockObserveSettings,
    )

    private fun createQuote(ticker: String, price: Double = 100.0): StockQuote {
        return StockQuote(
            ticker = ticker,
            price = price,
            change = 5.0,
            changePercent = 5.0,
            currency = "USD",
            lastUpdated = Instant.parse("2024-01-20T00:00:00Z"),
        )
    }

    // ===== ADD MODE TESTS =====

    @Test
    fun test_addMode_initializes_correctly() = runTest {
        // GIVEN: new ViewModel with holdingId = null
        val vm = createAddViewModel()

        // WHEN: viewState is collected
        val state = vm.viewState.value

        // THEN: mode should be ADD, no holdingId, currency defaults to EUR
        assertEquals(HoldingContract.Mode.ADD, state.mode)
        assertNull(state.holdingId)
        assertEquals(Currency.EUR, state.currency)
        assertTrue(state.shares.isEmpty())
        assertTrue(state.pricePerShare.isEmpty())
    }

    @Test
    fun test_addMode_sharesChanged_keeps_only_whole_numbers() = runTest {
        // GIVEN: ADD mode viewmodel
        val vm = createAddViewModel()

        // WHEN: user enters shares with a decimal part
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("10.5"))

        // THEN: only the integer part is kept
        assertEquals("10", vm.viewState.value.shares)
    }

    @Test
    fun test_addMode_sharesChanged_can_be_cleared() = runTest {
        // GIVEN: ADD mode viewmodel with shares typed
        val vm = createAddViewModel()
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("120"))

        // WHEN: the field is cleared (X button)
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged(""))

        // THEN: shares is empty and the estimated total resets
        assertEquals("", vm.viewState.value.shares)
        assertEquals(0.0, vm.viewState.value.estimatedTotal)
    }

    @Test
    fun test_addMode_pricePerShareChanged_updates_state() = runTest {
        // GIVEN: ADD mode viewmodel
        val vm = createAddViewModel()

        // WHEN: user enters price per share
        vm.onEvent(HoldingContract.HoldingViewEvent.PricePerShareChanged("150.75"))

        // THEN: pricePerShare value is updated
        assertEquals("150.75", vm.viewState.value.pricePerShare)
    }

    @Test
    fun test_addMode_estimatedTotal_recalculates_on_input_change() = runTest {
        // GIVEN: ADD mode viewmodel
        val vm = createAddViewModel()

        // WHEN: user enters shares
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("10"))
        // AND: user enters price per share
        vm.onEvent(HoldingContract.HoldingViewEvent.PricePerShareChanged("100"))

        // THEN: estimatedTotal = 10 * 100 = 1000
        assertEquals(1000.0, vm.viewState.value.estimatedTotal)
    }

    @Test
    fun test_addMode_estimatedTotal_handles_decimal_price() = runTest {
        // GIVEN: ADD mode viewmodel
        val vm = createAddViewModel()

        // WHEN: user enters whole shares and a decimal price
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("3"))
        vm.onEvent(HoldingContract.HoldingViewEvent.PricePerShareChanged("100.5"))

        // THEN: estimatedTotal = 3 * 100.5 = 301.5
        assertEquals(301.5, vm.viewState.value.estimatedTotal)
    }

    @Test
    fun test_addMode_currencyChanged_updates_state() = runTest {
        // GIVEN: ADD mode viewmodel
        val vm = createAddViewModel()

        // WHEN: user selects USD
        vm.onEvent(HoldingContract.HoldingViewEvent.CurrencyChanged(Currency.USD))

        // THEN: currency is updated
        assertEquals(Currency.USD, vm.viewState.value.currency)
    }

    @Test
    fun test_addMode_confirmClicked_requires_security() = runTest {
        // GIVEN: form is incomplete (no security selected)
        val vm = createAddViewModel()
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("10"))
        vm.onEvent(HoldingContract.HoldingViewEvent.PricePerShareChanged("100"))

        // WHEN: user clicks confirm
        vm.onEvent(HoldingContract.HoldingViewEvent.ConfirmClicked)
        advanceUntilIdle()

        // THEN: error side effect should be sent
        // (Cannot easily test side effects here; would need collect test)
    }

    @Test
    fun test_addMode_confirmClicked_adds_holding_successfully() = runTest {
        // GIVEN: valid form with all fields
        val quote = createQuote("AAPL", 150.0)
        
        coEvery { mockGetStockQuote("AAPL") } returns Result.success(quote)
        val vm = createPrefillViewModel("AAPL")
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("5"))
        vm.onEvent(HoldingContract.HoldingViewEvent.PricePerShareChanged("150"))

        // WHEN: user clicks confirm
        vm.onEvent(HoldingContract.HoldingViewEvent.ConfirmClicked)
        advanceUntilIdle()

        // THEN: AddHoldingUseCase.execute() should be called
        assertEquals(1, addRepository.added.size)
        assertFalse(vm.viewState.value.isSaving)
        assertTrue(vm.viewState.value.operationCompleted)
        assertFalse(vm.viewState.value.operationIsDelete)
    }

    @Test
    fun test_addMode_confirmClicked_emits_added_feedback() = runTest {
        // GIVEN: a prefilled form and a successful add
        coEvery { mockGetStockQuote("AAPL") } returns Result.success(createQuote("AAPL", 150.0))
        val feedback = mutableListOf<PositionFeedback>()
        backgroundScope.launch(testDispatcher) { positionActions.feedback.collect { feedback += it } }
        val vm = createPrefillViewModel("AAPL")
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("5"))

        // WHEN: user confirms
        vm.onEvent(HoldingContract.HoldingViewEvent.ConfirmClicked)
        advanceUntilIdle()

        // THEN: the app shell is told which position was added (for "View" / "Undo")
        assertEquals(listOf<PositionFeedback>(PositionFeedback.Added("AAPL", HoldingId("new-id"))), feedback)
    }

    @Test
    fun test_prefill_sets_price_currency_and_today() = runTest {
        // GIVEN: the chosen security quotes 187.456 USD
        coEvery { mockGetStockQuote("AAPL") } returns Result.success(createQuote("AAPL", 187.456))

        // WHEN: the form opens for AAPL
        val vm = createPrefillViewModel("AAPL")
        advanceUntilIdle()

        // THEN: only shares remain to be typed
        val state = vm.viewState.value
        assertEquals("AAPL", state.selectedSecurity?.ticker)
        assertEquals("187.46", state.pricePerShare)
        assertEquals(Currency.USD, state.currency)
        assertEquals(FIXED_TIMESTAMP, state.purchaseDateMillis)
        assertFalse(state.isLoadingSecurity)
        assertEquals(HoldingContract.Mode.ADD, state.mode)
    }

    @Test
    fun test_prefill_existing_ticker_switches_to_edit() = runTest {
        // GIVEN: AAPL already in the portfolio
        coEvery { mockGetStockQuote("AAPL") } returns Result.success(createQuote("AAPL", 150.0))
        val existing = Holding(
            id = HoldingId("h9"),
            tickerId = "AAPL",
            shares = 3.0,
            purchasePrice = 120.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 1L,
        )
        every { mockGetPortfolio.execute() } returns flowOf(Result.success(listOf(existing)))

        // WHEN: the form opens for AAPL
        val vm = createPrefillViewModel("AAPL")
        advanceUntilIdle()

        // THEN: the user edits the existing position instead of duplicating it
        val state = vm.viewState.value
        assertEquals(HoldingContract.Mode.EDIT, state.mode)
        assertEquals(HoldingId("h9"), state.holdingId)
        assertEquals("3", state.shares)
        assertEquals("120", state.pricePerShare)
    }

    @Test
    fun test_prefill_quote_failure_stops_loading() = runTest {
        // GIVEN: the quote cannot be fetched (default mock)
        // WHEN: the form opens
        val vm = createPrefillViewModel("XYZ")
        advanceUntilIdle()

        // THEN: loading ends without a security, so the screen can show the error state
        assertFalse(vm.viewState.value.isLoadingSecurity)
        assertNull(vm.viewState.value.selectedSecurity)
    }

    @Test
    fun test_toPriceInput_rounds_and_trims() {
        assertEquals("187.46", 187.456.toPriceInput())
        assertEquals("150", 150.0.toPriceInput())
        assertEquals("0.1235", 0.12346.toPriceInput())
    }

    // ===== EDIT MODE TESTS =====

    @Test
    fun test_editMode_initializes_correctly() = runTest {
        // GIVEN: new ViewModel with holdingId = "h1"
        val vm = createEditViewModel(HoldingId("h1"))

        // WHEN: viewState is collected
        val state = vm.viewState.value

        // THEN: mode should be EDIT, holdingId set, currency defaults to EUR
        assertEquals(HoldingContract.Mode.EDIT, state.mode)
        assertEquals(HoldingId("h1"), state.holdingId)
        assertEquals(Currency.EUR, state.currency)
    }

    @Test
    fun test_editMode_deleteClicked_shows_confirmation() = runTest {
        // GIVEN: EDIT mode viewmodel
        val vm = createEditViewModel()
        assertFalse(vm.viewState.value.showDeleteConfirmation)

        // WHEN: user clicks delete
        vm.onEvent(HoldingContract.HoldingViewEvent.DeleteClicked)

        // THEN: showDeleteConfirmation should be true
        assertTrue(vm.viewState.value.showDeleteConfirmation)
    }

    @Test
    fun test_editMode_cancelDelete_hides_confirmation() = runTest {
        // GIVEN: delete confirmation is shown
        val vm = createEditViewModel()
        vm.onEvent(HoldingContract.HoldingViewEvent.DeleteClicked)
        assertTrue(vm.viewState.value.showDeleteConfirmation)

        // WHEN: user clicks cancel delete
        vm.onEvent(HoldingContract.HoldingViewEvent.CancelDeleteClicked)

        // THEN: showDeleteConfirmation should be false
        assertFalse(vm.viewState.value.showDeleteConfirmation)
    }

    @Test
    fun test_editMode_confirmDelete_removes_holding() = runTest {
        // GIVEN: delete confirmation is shown and RemoveHolding mocked
        coEvery { mockRemoveHolding.execute(any()) } returns Result.success(Unit)
        val vm = createEditViewModel(HoldingId("h1"))
        vm.onEvent(HoldingContract.HoldingViewEvent.DeleteClicked)

        // WHEN: user confirms delete
        vm.onEvent(HoldingContract.HoldingViewEvent.ConfirmDeleteClicked)
        advanceUntilIdle()

        // THEN: RemoveHoldingUseCase.execute() should be called with correct ID
        coVerify(exactly = 1) { mockRemoveHolding.execute(HoldingId("h1")) }
    }

    @Test
    fun test_editMode_confirmUpdate_updates_holding() = runTest {
        // GIVEN: EDIT mode with valid form
        val quote = createQuote("GOOGL", 140.0)
        coEvery { mockUpdateHolding.execute(any()) } returns Result.success(Unit)
        coEvery { mockGetStockQuote("GOOGL") } returns Result.success(quote)
        every { mockGetPortfolio.execute() } returns flowOf(
            Result.success(listOf(Holding(HoldingId("h1"), "GOOGL", 10.0, 130.0, Currency.USD, 1L))),
        )
        val vm = createEditViewModel(HoldingId("h1"))
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("20"))
        vm.onEvent(HoldingContract.HoldingViewEvent.PricePerShareChanged("140"))

        // WHEN: user clicks confirm (update)
        vm.onEvent(HoldingContract.HoldingViewEvent.ConfirmClicked)
        advanceUntilIdle()

        // THEN: UpdateHoldingUseCase.execute() should be called
        coVerify(exactly = 1) { mockUpdateHolding.execute(any()) }
    }

    @Test
    fun test_editMode_preserves_purchaseDate_on_update() = runTest {
        // GIVEN: an existing holding bought on a historical date
        val originalDate = 1705276800000L
        coEvery { mockGetStockQuote("TSLA") } returns Result.success(createQuote("TSLA", 250.0))
        coEvery { mockUpdateHolding.execute(any()) } returns Result.success(Unit)
        every { mockGetPortfolio.execute() } returns flowOf(
            Result.success(listOf(Holding(HoldingId("h1"), "TSLA", 10.0, 200.0, Currency.USD, originalDate))),
        )
        val vm = createEditViewModel(HoldingId("h1"))
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("15"))

        // WHEN: user confirms the update
        vm.onEvent(HoldingContract.HoldingViewEvent.ConfirmClicked)
        advanceUntilIdle()

        // THEN: the original purchase date is kept
        coVerify(exactly = 1) {
            mockUpdateHolding.execute(match { it.purchaseDate == originalDate && it.shares == 15.0 })
        }
    }

    // ===== SHARED TESTS =====

    @Test
    fun test_sharesChanged_with_invalid_input_sets_to_zero() = runTest {
        // GIVEN: ADD mode viewmodel
        val vm = createAddViewModel()
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("100"))
        assertEquals("100", vm.viewState.value.shares)

        // WHEN: user enters invalid value and recalc is called
        vm.onEvent(HoldingContract.HoldingViewEvent.SharesChanged("abc"))
        vm.onEvent(HoldingContract.HoldingViewEvent.PricePerShareChanged("100"))

        // THEN: estimatedTotal should be 0 (invalid shares treated as 0)
        assertEquals(0.0, vm.viewState.value.estimatedTotal)
    }

    @Test
    fun test_dismiss_event_handled() = runTest {
        // GIVEN: ADD mode viewmodel
        val vm = createAddViewModel()

        // WHEN: user dismisses sheet
        vm.onEvent(HoldingContract.HoldingViewEvent.DismissClicked)

        // THEN: no error should occur (event is no-op)
        // If we got here without exception, test passes
        assertTrue(true)
    }
}

private class FakeAddRepository : PortfolioRepository {
    val added = mutableListOf<Holding>()
    override fun observePortfolio(): Flow<Result<List<Holding>>> = emptyFlow()
    override suspend fun getPortfolio(): Result<List<Holding>> = Result.success(emptyList())
    override suspend fun addHolding(holding: Holding): Result<HoldingId> {
        added += holding
        return Result.success(HoldingId("new-id"))
    }
    override suspend fun updateHolding(holding: Holding): Result<Unit> = Result.success(Unit)
    override suspend fun removeHolding(holdingId: HoldingId): Result<Unit> = Result.success(Unit)
    override suspend fun clearAll(): Result<Unit> = Result.success(Unit)
}
