package com.akole.dividox.integration.dividend.domain.model

import kotlinx.datetime.LocalDate

/**
 * Represents a single bar in the dividend projection chart.
 *
 * The [yearMonth] field uses the first day of the represented month as a
 * stable, sortable key (e.g. `2024-01-01` for January 2024).
 *
 * @property yearMonth First day of the represented month.
 * @property amount Total dividend amount for the month (base currency).
 * @property isProjected `true` for future months derived from upcoming
 *   scheduled payments; `false` for historical months from confirmed records.
 */
data class MonthBar(
    val yearMonth: LocalDate,
    val amount: Double,
    val isProjected: Boolean,
)
