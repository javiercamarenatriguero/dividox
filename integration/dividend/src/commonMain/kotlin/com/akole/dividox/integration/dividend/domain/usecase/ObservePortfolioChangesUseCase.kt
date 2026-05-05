package com.akole.dividox.integration.dividend.domain.usecase

import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

/**
 * Emits the current portfolio holdings whenever they change.
 * Uses fingerprint-based [distinctUntilChanged] to suppress spurious Firestore refreshes
 * that return identical data.
 *
 * The fingerprint includes ticker, shares, and purchase price so that updates to an existing
 * holding (e.g. changing share count) are detected and trigger a downstream sync — not just
 * additions and removals.
 *
 * Emitting the full [Holding] list (instead of fingerprints) lets callers pass the data directly
 * to sync use cases, avoiding a redundant second Firestore read.
 */
class ObservePortfolioChangesUseCase(
    private val portfolioRepository: PortfolioRepository,
) {
    operator fun invoke(): Flow<List<Holding>> =
        portfolioRepository
            .observePortfolio()
            .mapNotNull { result -> result.getOrNull() }
            .distinctUntilChanged { a, b -> a.toFingerprint() == b.toFingerprint() }

    private fun List<Holding>.toFingerprint(): List<String> =
        map { "${it.tickerId}:${it.shares}:${it.purchasePrice}" }.sorted()
}
