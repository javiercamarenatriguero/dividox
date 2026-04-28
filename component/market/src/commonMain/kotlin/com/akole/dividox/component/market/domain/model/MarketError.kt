package com.akole.dividox.component.market.domain.model

sealed class MarketError : Exception() {
    data object NetworkError : MarketError()
    data class NotFound(val ticker: String) : MarketError()
    data object RateLimited : MarketError()
    data class Unknown(override val message: String) : MarketError()
}
