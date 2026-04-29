package com.akole.dividox.feature.auth.login

import com.akole.dividox.component.auth.data.GoogleSignInLauncher
import com.akole.dividox.component.auth.domain.usecase.SignInWithEmailUseCase
import com.akole.dividox.component.auth.domain.usecase.SignInWithGoogleUseCase
import com.akole.dividox.feature.auth.login.LoginContract.LoginSideEffect
import com.akole.dividox.feature.auth.login.LoginContract.LoginViewEvent
import io.mockk.coEvery
import io.mockk.coVerify
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

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val signInWithEmail: SignInWithEmailUseCase = mockk()
    private val signInWithGoogle: SignInWithGoogleUseCase = mockk()
    private val googleSignInLauncher: GoogleSignInLauncher = mockk()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { signInWithEmail(any(), any()) } returns Result.success(Unit)
        coEvery { signInWithGoogle(any()) } returns Result.success(Unit)
        coEvery { googleSignInLauncher.launchSignIn() } returns "token"
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = LoginViewModel(
        signInWithEmail = signInWithEmail,
        signInWithGoogle = signInWithGoogle,
        googleSignInLauncher = googleSignInLauncher,
    )

    @Test
    fun `initial state has empty fields`() {
        // GIVEN / WHEN
        val state = viewModel().viewState.value

        // THEN
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(false, state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `OnEmailChanged updates email`() {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnEmailChanged("user@test.com"))

        // THEN
        assertEquals("user@test.com", vm.viewState.value.email)
    }

    @Test
    fun `OnPasswordChanged updates password`() {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnPasswordChanged("secret"))

        // THEN
        assertEquals("secret", vm.viewState.value.password)
    }

    @Test
    fun `OnErrorDismissed clears error`() = runTest {
        // GIVEN
        coEvery { signInWithEmail(any(), any()) } returns Result.failure(IllegalStateException("Sign in failed"))
        val vm = viewModel()
        vm.onViewEvent(LoginViewEvent.OnSignInClicked)
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnErrorDismissed)

        // THEN
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `OnSignInClicked with success stays loading`() = runTest {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnSignInClicked)
        advanceUntilIdle()

        // THEN
        assertEquals(true, vm.viewState.value.isLoading)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `OnSignInClicked with failure sets error`() = runTest {
        // GIVEN
        coEvery { signInWithEmail(any(), any()) } returns Result.failure(IllegalStateException("Sign in failed"))
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnSignInClicked)
        advanceUntilIdle()

        // THEN
        assertEquals(false, vm.viewState.value.isLoading)
        assertEquals("Sign in failed", vm.viewState.value.error)
    }

    @Test
    fun `OnForgotPasswordClicked emits NavigateToForgotPassword with email`() = runTest {
        // GIVEN
        val vm = viewModel()
        vm.onViewEvent(LoginViewEvent.OnEmailChanged("user@test.com"))
        val effects = mutableListOf<LoginSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnForgotPasswordClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        val effect = assertIs<LoginSideEffect.Navigation.NavigateToForgotPassword>(effects.first())
        assertEquals("user@test.com", effect.email)
    }

    @Test
    fun `OnSignUpClicked emits NavigateToSignUp`() = runTest {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<LoginSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnSignUpClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        assertIs<LoginSideEffect.Navigation.NavigateToSignUp>(effects.first())
    }

    @Test
    fun `OnGoogleSignInClicked with successful launcher calls signInWithGoogle`() = runTest {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnGoogleSignInClicked)
        advanceUntilIdle()

        // THEN
        coVerify { signInWithGoogle("token") }
    }

    @Test
    fun `OnGoogleSignInClicked with null token does nothing`() = runTest {
        // GIVEN
        coEvery { googleSignInLauncher.launchSignIn() } returns null
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnGoogleSignInClicked)
        advanceUntilIdle()

        // THEN
        coVerify(exactly = 0) { signInWithGoogle(any()) }
        assertEquals(false, vm.viewState.value.isLoading)
    }

    @Test
    fun `OnGoogleSignInClicked when launcher throws sets error`() = runTest {
        // GIVEN
        coEvery { googleSignInLauncher.launchSignIn() } throws RuntimeException("Launcher error")
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(LoginViewEvent.OnGoogleSignInClicked)
        advanceUntilIdle()

        // THEN
        assertEquals(false, vm.viewState.value.isLoading)
        assertEquals("Launcher error", vm.viewState.value.error)
    }
}
