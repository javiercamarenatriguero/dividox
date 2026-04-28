package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.repository.MarketRepository

class GetDividendInfoUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(ticker: String): Result<DividendInfo> =
        repository.getDividendInfo(ticker)
}
