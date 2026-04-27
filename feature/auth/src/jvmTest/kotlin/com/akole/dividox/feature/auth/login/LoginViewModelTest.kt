package com.akole.dividox.feature.auth.login

import com.akole.dividox.common.auth.data.GoogleSignInLauncher
import com.akole.dividox.common.auth.domain.model.AuthUser
import com.akole.dividox.common.auth.domain.repository.AuthRepository
import com.akole.dividox.common.auth.domain.usecase.SignInWithEmailUseCase
import com.akole.dividox.common.auth.domain.usecase.SignInWithGoogleUseCase
import com.akole.dividox.feature.auth.login.LoginContract.LoginSideEffect
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun teardown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    private fun viewModel(
        repository: AuthRepository = FakeLoginRepository(),
        launcher: GoogleSignInLauncher = FakeGoogleSignInLauncher("token"),
    ) = LoginViewModel(
        signInWithEmail = SignInWithEmailUseCase(repository),
        signInWithGoogle = SignInWithGoogleUseCase(repository),
        googleSignInLauncher = launcher,
    )

    @Test
    fun `initial state has empty fields`() {
        val state = viewModel().viewState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `OnEmailChanged updates email`() {
        val vm = viewModel()
        vm.onViewEvent(LoginViewEvent.OnEmailChanged("user@test.com"))
        assertEquals("user@test.com", vm.viewState.value.email)
    }

    @Test
    fun `OnPasswordChanged updates password`() {
        val vm = viewModel()
        vm.onViewEvent(LoginViewEvent.OnPasswordChanged("secret"))
        assertEquals("secret", vm.viewState.value.password)
    }

    @Test
    fun `OnErrorDismissed clears error`() {
        val vm = viewModel(repository = FailingLoginRepository())
        runTest {
            vm.onViewEvent(LoginViewEvent.OnSignInClicked)
            advanceUntilIdle()
            vm.onViewEvent(LoginViewEvent.OnErrorDismissed)
            assertNull(vm.viewState.value.error)
        }
    }

    @Test
    fun `OnSignInClicked with success stays loading`() = runTest {
        val vm = viewModel()
        vm.onViewEvent(LoginViewEvent.OnSignInClicked)
        advanceUntilIdle()
        assertEquals(true, vm.viewState.value.isLoading)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `OnSignInClicked with failure sets error`() = runTest {
        val vm = viewModel(repository = FailingLoginRepository())
        vm.onViewEvent(LoginViewEvent.OnSignInClicked)
        advanceUntilIdle()
        assertEquals(false, vm.viewState.value.isLoading)
        assertEquals("Sign in failed", vm.viewState.value.error)
    }

    @Test
    fun `OnForgotPasswordClicked emits NavigateToForgotPassword with email`() = runTest {
        val vm = viewModel()
        vm.onViewEvent(LoginViewEvent.OnEmailChanged("user@test.com"))

        val effects = mutableListOf<LoginSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        vm.onViewEvent(LoginViewEvent.OnForgotPasswordClicked)
        advanceUntilIdle()
        job.cancel()

        val effect = assertIs<LoginSideEffect.Navigation.NavigateToForgotPassword>(effects.first())
        assertEquals("user@test.com", effect.email)
    }

    @Test
    fun `OnSignUpClicked emits NavigateToSignUp`() = runTest {
        val vm = viewModel()

        val effects = mutableListOf<LoginSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        vm.onViewEvent(LoginViewEvent.OnSignUpClicked)
        advanceUntilIdle()
        job.cancel()

        assertIs<LoginSideEffect.Navigation.NavigateToSignUp>(effects.first())
    }

    @Test
    fun `OnGoogleSignInClicked with successful launcher calls signInWithGoogle`() = runTest {
        val repository = FakeLoginRepository()
        val vm = viewModel(repository = repository, launcher = FakeGoogleSignInLauncher("id_token"))
        vm.onViewEvent(LoginViewEvent.OnGoogleSignInClicked)
        advanceUntilIdle()
        assertEquals(true, repository.signInWithGoogleCalled)
    }

    @Test
    fun `OnGoogleSignInClicked with null token does nothing`() = runTest {
        val repository = FakeLoginRepository()
        val vm = viewModel(repository = repository, launcher = FakeGoogleSignInLauncher(null))
        vm.onViewEvent(LoginViewEvent.OnGoogleSignInClicked)
        advanceUntilIdle()
        assertEquals(false, repository.signInWithGoogleCalled)
        assertEquals(false, vm.viewState.value.isLoading)
    }

    @Test
    fun `OnGoogleSignInClicked when launcher throws sets error`() = runTest {
        val vm = viewModel(launcher = ThrowingGoogleSignInLauncher())
        vm.onViewEvent(LoginViewEvent.OnGoogleSignInClicked)
        advanceUntilIdle()
        assertEquals(false, vm.viewState.value.isLoading)
        assertEquals("Launcher error", vm.viewState.value.error)
    }
}

private class FakeLoginRepository : AuthRepository {
    var signInWithEmailCalled = false
    var signInWithGoogleCalled = false

    override fun observeAuthState(): Flow<AuthUser?> = flowOf(null)
    override suspend fun signInWithEmail(email: String, password: String) =
        Result.success<Unit>(Unit).also { signInWithEmailCalled = true }
    override suspend fun signUpWithEmail(email: String, password: String) = Result.success(Unit)
    override suspend fun signInWithGoogle(idToken: String) =
        Result.success<Unit>(Unit).also { signInWithGoogleCalled = true }
    override suspend fun sendPasswordResetEmail(email: String) = Result.success(Unit)
    override suspend fun signOut() = Result.success(Unit)
}

private class FailingLoginRepository : AuthRepository {
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

private class FakeGoogleSignInLauncher(private val idToken: String?) : GoogleSignInLauncher() {
    override suspend fun launchSignIn(): String? = idToken
}

private class ThrowingGoogleSignInLauncher : GoogleSignInLauncher() {
    override suspend fun launchSignIn(): String? = throw RuntimeException("Launcher error")
}
