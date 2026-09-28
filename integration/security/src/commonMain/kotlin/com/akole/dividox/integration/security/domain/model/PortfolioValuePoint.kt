package com.akole.dividox.integration.security.domain.model

import kotlinx.datetime.LocalDate

/** Value of the portfolio on [date], expressed in the requested target currency. */
data class PortfolioValuePoint(
    val date: LocalDate,
    val value: Double,
)
