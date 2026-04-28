package com.akole.dividox.component.market.domain.model

import kotlin.time.Instant

data class PricePoint(
    val timestamp: Instant,
    val close: Double,
)
