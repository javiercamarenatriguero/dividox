package com.akole.dividox.feature.auth.register

import com.akole.dividox.common.auth.domain.model.AuthUser
import com.akole.dividox.common.auth.domain.repository.AuthRepository
import com.akole.dividox.common.auth.domain.usecase.SignUpWithEmailUseCase
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpSideEffect
import com.akole.dividox.feature.auth.register.SignUpContract.SignUpViewEvent
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun teardown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    private fun viewModel(repository: AuthRepository = FakeSignUpRepository()) =
        SignUpViewModel(SignUpWithEmailUseCase(repository))

    @Test
    fun `initial state has empty fields`() {
        val state = viewModel().viewState.value
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(false, state.termsAccepted)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `OnNameChanged updates name`() {
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnNameChanged("Alice"))
        assertEquals("Alice", vm.viewState.value.name)
    }

    @Test
    fun `OnEmailChanged updates email`() {
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnEmailChanged("alice@test.com"))
        assertEquals("alice@test.com", vm.viewState.value.email)
    }

    @Test
    fun `OnPasswordChanged updates password`() {
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnPasswordChanged("pass123"))
        assertEquals("pass123", vm.viewState.value.password)
    }

    @Test
    fun `OnTermsChanged updates termsAccepted`() {
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnTermsChanged(true))
        assertEquals(true, vm.viewState.value.termsAccepted)
    }

    @Test
    fun `OnErrorDismissed clears error`() = runTest {
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnCreateAccountClicked)
        advanceUntilIdle()
        assertNotNull(vm.viewState.value.error)

        vm.onViewEvent(SignUpViewEvent.OnErrorDismissed)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `OnCreateAccountClicked without terms accepted sets error`() = runTest {
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnCreateAccountClicked)
        advanceUntilIdle()
        assertNotNull(vm.viewState.value.error)
        assertEquals(false, vm.viewState.value.isLoading)
    }

    @Test
    fun `OnCreateAccountClicked with terms and success stays loading`() = runTest {
        val vm = viewModel()
        vm.onViewEvent(SignUpViewEvent.OnTermsChanged(true))
        vm.onViewEvent(SignUpViewEvent.OnCreateAccountClicked)
        advanceUntilIdle()
        assertEquals(true, vm.viewState.value.isLoading)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `OnCreateAccountClicked with terms and failure sets error`() = runTest {
        val vm = viewModel(repository = FailingSignUpRepository())
        vm.onViewEvent(SignUpViewEvent.OnTermsChanged(true))
        vm.onViewEvent(SignUpViewEvent.OnCreateAccountClicked)
        advanceUntilIdle()
        assertEquals(false, vm.viewState.value.isLoading)
        assertEquals("Sign up failed", vm.viewState.value.error)
    }

    @Test
    fun `OnSignInClicked emits NavigateToLogin`() = runTest {
        val vm = viewModel()

        val effects = mutableListOf<SignUpSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        vm.onViewEvent(SignUpViewEvent.OnSignInClicked)
        advanceUntilIdle()
        job.cancel()

        assertIs<SignUpSideEffect.Navigation.NavigateToLogin>(effects.first())
    }
}

private class FakeSignUpRepository : AuthRepository {
    override fun observeAuthState(): Flow<AuthUser?> = flowOf(null)
    override suspend fun signInWithEmail(email: String, password: String) = Result.success(Unit)
    override suspend fun signUpWithEmail(email: String, password: String) = Result.success(Unit)
    override suspend fun signInWithGoogle(idToken: String) = Result.success(Unit)
    override suspend fun sendPasswordResetEmail(email: String) = Result.success(Unit)
    override suspend fun signOut() = Result.success(Unit)
}

private class FailingSignUpRepository : AuthRepository {
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
}
