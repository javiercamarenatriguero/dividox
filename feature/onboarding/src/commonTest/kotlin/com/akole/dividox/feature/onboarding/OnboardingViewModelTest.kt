package com.akole.dividox.feature.onboarding

import com.akole.dividox.common.settings.domain.usecase.SetOnboardingCompletedUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingViewModelTest {

    private val mockSetOnboardingCompleted = mockk<SetOnboardingCompletedUseCase>()

    private fun buildViewModel(): OnboardingViewModel {
        coEvery { mockSetOnboardingCompleted() } returns Unit
        return OnboardingViewModel(setOnboardingCompleted = mockSetOnboardingCompleted)
    }

    @Test
    fun onNextClicked_whenNotLastPage_advancesPage() = runTest {
        // GIVEN
        val viewModel = buildViewModel()

        // WHEN
        viewModel.onViewEvent(OnboardingEvent.OnNextClicked)

        // THEN
        assertEquals(1, viewModel.viewState.value.currentPage)
    }

    @Test
    fun onNextClicked_onLastPage_persistsFlagAndNavigates() = runTest {
        // GIVEN
        val viewModel = buildViewModel()
        repeat(4) { viewModel.onViewEvent(OnboardingEvent.OnNextClicked) }

        // WHEN
        viewModel.onViewEvent(OnboardingEvent.OnNextClicked)

        // THEN
        coVerify(exactly = 1) { mockSetOnboardingCompleted() }
        val effect = viewModel.sideEffect.first()
        assertEquals(OnboardingSideEffect.NavigateToDashboard, effect)
    }

    @Test
    fun onSkipClicked_persistsFlagAndNavigates() = runTest {
        // GIVEN
        val viewModel = buildViewModel()

        // WHEN
        viewModel.onViewEvent(OnboardingEvent.OnSkipClicked)

        // THEN
        coVerify(exactly = 1) { mockSetOnboardingCompleted() }
        val effect = viewModel.sideEffect.first()
        assertEquals(OnboardingSideEffect.NavigateToDashboard, effect)
    }

    @Test
    fun onPageChanged_updatesCurrentPage() = runTest {
        // GIVEN
        val viewModel = buildViewModel()

        // WHEN
        viewModel.onViewEvent(OnboardingEvent.OnPageChanged(3))

        // THEN
        assertEquals(3, viewModel.viewState.value.currentPage)
    }
}
