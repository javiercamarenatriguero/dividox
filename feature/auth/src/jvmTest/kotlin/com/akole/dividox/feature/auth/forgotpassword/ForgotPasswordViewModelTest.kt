package com.akole.dividox.feature.auth.forgotpassword

import com.akole.dividox.component.auth.domain.usecase.ForgotPasswordUseCase
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordSideEffect
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordViewEvent
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val forgotPassword: ForgotPasswordUseCase = mockk()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { forgotPassword(any()) } returns Result.success(Unit)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ForgotPasswordViewModel(forgotPassword)

    @Test
    fun `initial state has empty fields`() {
        // GIVEN / WHEN
        val state = viewModel().viewState.value

        // THEN
        assertEquals("", state.email)
        assertEquals(false, state.isLoading)
        assertEquals(false, state.isSuccess)
        assertNull(state.error)
    }

    @Test
    fun `OnEmailChanged updates email`() {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(ForgotPasswordViewEvent.OnEmailChanged("reset@test.com"))

        // THEN
        assertEquals("reset@test.com", vm.viewState.value.email)
    }

    @Test
    fun `OnSendResetLinkClicked with success sets isSuccess`() = runTest {
        // GIVEN
        val vm = viewModel()
        vm.onViewEvent(ForgotPasswordViewEvent.OnEmailChanged("user@test.com"))

        // WHEN
        vm.onViewEvent(ForgotPasswordViewEvent.OnSendResetLinkClicked)
        advanceUntilIdle()

        // THEN
        assertEquals(false, vm.viewState.value.isLoading)
        assertTrue(vm.viewState.value.isSuccess)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `OnSendResetLinkClicked with failure sets error`() = runTest {
        // GIVEN
        coEvery { forgotPassword(any()) } returns Result.failure(IllegalStateException("Reset failed"))
        val vm = viewModel()
        vm.onViewEvent(ForgotPasswordViewEvent.OnEmailChanged("user@test.com"))

        // WHEN
        vm.onViewEvent(ForgotPasswordViewEvent.OnSendResetLinkClicked)
        advanceUntilIdle()

        // THEN
        assertEquals(false, vm.viewState.value.isLoading)
        assertEquals(false, vm.viewState.value.isSuccess)
        assertEquals("Reset failed", vm.viewState.value.error)
    }

    @Test
    fun `OnBackClicked emits NavigateBack`() = runTest {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<ForgotPasswordSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(ForgotPasswordViewEvent.OnBackClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        assertIs<ForgotPasswordSideEffect.Navigation.NavigateBack>(effects.first())
    }
}
