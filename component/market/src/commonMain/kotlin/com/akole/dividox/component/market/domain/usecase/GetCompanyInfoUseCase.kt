package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.repository.MarketRepository

class GetCompanyInfoUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(ticker: String): Result<CompanyInfo> =
        repository.getCompanyInfo(ticker)
}
