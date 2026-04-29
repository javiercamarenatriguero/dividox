package com.akole.dividox.feature.auth.register

import com.akole.dividox.component.auth.domain.usecase.SignUpWithEmailUseCase
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpSideEffect
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val signUpWithEmail: SignUpWithEmailUseCase = mockk()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { signUpWithEmail(any(), any()) } returns Result.success(Unit)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SignUpViewModel(signUpWithEmail)

    @Test
    fun `initial state has empty fields`() {
        // GIVEN / WHEN
        val state = viewModel().viewState.value

        // THEN
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(false, state.termsAccepted)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `OnNameChanged updates name`() {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SignUpViewEvent.OnNameChanged("Alice"))

        // THEN
        assertEquals("Alice", vm.viewState.value.name)
    }

    @Test
    fun `OnEmailChanged updates email`() {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SignUpViewEvent.OnEmailChanged("alice@test.com"))

        // THEN
        assertEquals("alice@test.com", vm.viewState.value.email)
    }

    @Test
    fun `OnPasswordChanged updates password`() {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SignUpViewEvent.OnPasswordChanged("pass123"))

        // THEN
        assertEquals("pass123", vm.viewState.value.password)
    }

    @Test
    fun `OnTermsChanged updates termsAccepted`() {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SignUpViewEvent.OnTermsChanged(true))

        // THEN
        assertEquals(true, vm.viewState.value.termsAccepted)
    }

    @Test
    fun `OnErrorDismissed clears error`() = runTest {
        // GIVEN
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnCreateAccountClicked)
        advanceUntilIdle()
        assertNotNull(vm.viewState.value.error)

        // WHEN
        vm.onViewEvent(SignUpViewEvent.OnErrorDismissed)

        // THEN
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `OnCreateAccountClicked without terms accepted sets error`() = runTest {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SignUpViewEvent.OnCreateAccountClicked)
        advanceUntilIdle()

        // THEN
        assertNotNull(vm.viewState.value.error)
        assertEquals(false, vm.viewState.value.isLoading)
    }

    @Test
    fun `OnCreateAccountClicked with terms and success stays loading`() = runTest {
        // GIVEN
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnTermsChanged(true))

        // WHEN
        vm.onViewEvent(SignUpViewEvent.OnCreateAccountClicked)
        advanceUntilIdle()

        // THEN
        assertEquals(true, vm.viewState.value.isLoading)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `OnCreateAccountClicked with terms and failure sets error`() = runTest {
        // GIVEN
        coEvery { signUpWithEmail(any(), any()) } returns Result.failure(IllegalStateException("Sign up failed"))
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnTermsChanged(true))

        // WHEN
        vm.onViewEvent(SignUpViewEvent.OnCreateAccountClicked)
        advanceUntilIdle()

        // THEN
        assertEquals(false, vm.viewState.value.isLoading)
        assertEquals("Sign up failed", vm.viewState.value.error)
    }

    @Test
    fun `OnSignInClicked emits NavigateToLogin`() = runTest {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<SignUpSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(SignUpViewEvent.OnSignInClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        assertIs<SignUpSideEffect.Navigation.NavigateToLogin>(effects.first())
    }
}
