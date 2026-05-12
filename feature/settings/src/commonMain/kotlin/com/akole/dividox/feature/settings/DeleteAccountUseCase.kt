package com.akole.dividox.feature.settings

import com.akole.dividox.component.auth.domain.repository.AuthRepository
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import com.akole.dividox.component.watchlist.domain.repository.WatchlistRepository

class DeleteAccountUseCase(
    private val portfolioRepository: PortfolioRepository,
    private val watchlistRepository: WatchlistRepository,
    private val authRepository: AuthRepository,
    private val dividendRepository: DividendRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        portfolioRepository.clearAll().getOrElse { return Result.failure(it) }
        watchlistRepository.clearAll().getOrElse { return Result.failure(it) }
        authRepository.deleteAccount().getOrElse { return Result.failure(it) }
        runCatching { dividendRepository.clearAll() }.getOrElse { return Result.failure(it) }
        return Result.success(Unit)
    }
}
