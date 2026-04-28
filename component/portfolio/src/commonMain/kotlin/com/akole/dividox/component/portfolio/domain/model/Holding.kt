package com.akole.dividox.component.portfolio.domain.model

/**
 * User's share holding with purchase details.
 *
 * @property id Unique holding identifier
 * @property tickerId Yahoo Finance ticker symbol (e.g., "AAPL", "MC.PA")
 * @property shares Quantity of shares held
 * @property purchasePrice Price per share at purchase (in [purchaseCurrency])
 * @property purchaseCurrency ISO 4217 currency code
 * @property purchaseDate Unix timestamp (ms) of purchase date
 */
data class Holding(
    val id: HoldingId,
    val tickerId: String,
    val shares: Double,
    val purchasePrice: Double,
    val purchaseCurrency: Currency,
    val purchaseDate: Long,
)
