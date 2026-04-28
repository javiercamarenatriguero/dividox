package com.akole.dividox.feature.auth.forgotpassword

import com.akole.dividox.component.auth.domain.model.AuthUser
import com.akole.dividox.component.auth.domain.repository.AuthRepository
import com.akole.dividox.component.auth.domain.usecase.ForgotPasswordUseCase
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordSideEffect
import com.akole.dividox.feature.auth.forgotpassword.ForgotPasswordContract.ForgotPasswordViewEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

    @BeforeTest
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun teardown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    private fun viewModel(repository: AuthRepository = FakeForgotPasswordRepository()) =
        ForgotPasswordViewModel(ForgotPasswordUseCase(repository))

    @Test
    fun `initial state has empty fields`() {
        val state = viewModel().viewState.value
        assertEquals("", state.email)
        assertEquals(false, state.isLoading)
        assertEquals(false, state.isSuccess)
        assertNull(state.error)
    }

    @Test
    fun `OnEmailChanged updates email`() {
        val vm = viewModel()
        vm.onViewEvent(ForgotPasswordViewEvent.OnEmailChanged("reset@test.com"))
        assertEquals("reset@test.com", vm.viewState.value.email)
    }

    @Test
    fun `OnSendResetLinkClicked with success sets isSuccess`() = runTest {
        val vm = viewModel()
        vm.onViewEvent(ForgotPasswordViewEvent.OnEmailChanged("user@test.com"))
        vm.onViewEvent(ForgotPasswordViewEvent.OnSendResetLinkClicked)
        advanceUntilIdle()
        assertEquals(false, vm.viewState.value.isLoading)
        assertTrue(vm.viewState.value.isSuccess)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `OnSendResetLinkClicked with failure sets error`() = runTest {
        val vm = viewModel(repository = FailingForgotPasswordRepository())
        vm.onViewEvent(ForgotPasswordViewEvent.OnEmailChanged("user@test.com"))
        vm.onViewEvent(ForgotPasswordViewEvent.OnSendResetLinkClicked)
        advanceUntilIdle()
        assertEquals(false, vm.viewState.value.isLoading)
        assertEquals(false, vm.viewState.value.isSuccess)
        assertEquals("Reset failed", vm.viewState.value.error)
    }

    @Test
    fun `OnBackClicked emits NavigateBack`() = runTest {
        val vm = viewModel()

        val effects = mutableListOf<ForgotPasswordSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        vm.onViewEvent(ForgotPasswordViewEvent.OnBackClicked)
        advanceUntilIdle()
        job.cancel()

        assertIs<ForgotPasswordSideEffect.Navigation.NavigateBack>(effects.first())
    }
}

private class FakeForgotPasswordRepository : AuthRepository {
    override fun observeAuthState(): Flow<AuthUser?> = flowOf(null)
    override suspend fun signInWithEmail(email: String, password: String) = Result.success(Unit)
    override suspend fun signUpWithEmail(email: String, password: String) = Result.success(Unit)
    override suspend fun signInWithGoogle(idToken: String) = Result.success(Unit)
    override suspend fun sendPasswordResetEmail(email: String) = Result.success(Unit)
    override suspend fun signOut() = Result.success(Unit)
    override fun getCurrentUserId(): String? = null
}

private class FailingForgotPasswordRepository : AuthRepository {
    override fun observeAuthState(): Flow<AuthUser?> = flowOf(null)
    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(IllegalStateException("Sign in failed"))
    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(IllegalStateException("Sign up failed"))
    override suspend fun signInWithGoogle(idToken: String): Result<Unit> =
        Result.failure(IllegalStateException("Google sign in failed"))
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        Result.failure(IllegalStateException("Reset failed"))
    override suspend fun signOut(): Result<Unit> =
        Result.failure(IllegalStateException("Sign out failed"))
    override fun getCurrentUserId(): String? = null
}
