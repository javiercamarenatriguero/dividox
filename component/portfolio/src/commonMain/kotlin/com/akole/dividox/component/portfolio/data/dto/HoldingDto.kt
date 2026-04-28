package com.akole.dividox.component.portfolio.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Firestore DTO for holdings. Internal serialization format—never exposed to domain.
 *
 * @property id Document ID in Firestore (transient—set from doc.id on read)
 * @property tickerId Yahoo Finance ticker
 * @property shares Quantity held
 * @property purchasePrice Price per share at purchase
 * @property purchaseCurrency ISO 4217 code
 * @property purchaseDate Unix timestamp (ms)
 */
@Serializable
data class HoldingDto(
    @Transient val id: String = "",
    val tickerId: String = "",
    val shares: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val purchaseCurrency: String = "",
    val purchaseDate: Long = 0L,
)
