package com.akole.dividox.component.portfolio.data.mapper

import com.akole.dividox.component.portfolio.data.dto.HoldingDto
import com.akole.dividox.common.ui.resources.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId

/**
 * Map Firestore DTO to domain model.
 * Currency code string is parsed to Currency enum.
 * HoldingId value is wrapped from string.
 *
 * @return Domain Holding with parsed currency
 * @throws IllegalArgumentException if purchaseCurrency code not in Currency enum
 */
fun HoldingDto.toDomain(): Holding = Holding(
    id = HoldingId(id),
    tickerId = tickerId,
    shares = shares,
    purchasePrice = purchasePrice,
    purchaseCurrency = Currency.valueOf(purchaseCurrency),
    purchaseDate = purchaseDate,
)

/**
 * Map domain model to Firestore DTO.
 * Currency enum is serialized to ISO 4217 code string.
 * HoldingId value is unwrapped to string.
 *
 * @return DTO ready for Firestore persistence
 */
fun Holding.toDto(): HoldingDto = HoldingDto(
    id = id.value,
    tickerId = tickerId,
    shares = shares,
    purchasePrice = purchasePrice,
    purchaseCurrency = purchaseCurrency.code,
    purchaseDate = purchaseDate,
)
