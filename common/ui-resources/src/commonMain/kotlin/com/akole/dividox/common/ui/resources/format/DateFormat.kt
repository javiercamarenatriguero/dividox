package com.akole.dividox.common.ui.resources.format

import kotlinx.datetime.LocalDate

/**
 * Formats a [LocalDate] as DD/MM/YYYY (e.g. "03/05/2026").
 */
fun LocalDate.formatShort(): String =
    "${day.toString().padStart(2, '0')}/${(month.ordinal + 1).toString().padStart(2, '0')}/$year"

/**
 * Returns a 3-letter localized month abbreviation using the system locale (e.g. "Jan", "ene.").
 */
expect fun LocalDate.monthShort(): String

/**
 * Returns the localized full month name using the system locale (e.g. "January", "enero").
 */
expect fun LocalDate.monthFull(): String

/**
 * Returns a compact label combining the 3-letter month abbreviation and a 2-digit year
 * (e.g. "Jan '24"). Suitable for chart X-axis labels when multiple years are displayed.
 */
fun LocalDate.monthShortWithYear(): String = "${monthShort()} '${year.toString().takeLast(2)}"

/**
 * Formats a bar chart popup label as "{formattedAmount} - {barLabel}".
 * Example: "$12.50 - Jan" or "12,50 € - Jan"
 *
 * Intended to be passed as [BarChart.popupLabelFormatter]. Placing it in common allows
 * any feature using [BarChart] to produce a consistent popup format.
 *
 * @param amount The bar's numeric value.
 * @param currencyCode ISO 4217 currency code (e.g. "USD", "EUR").
 * @param barLabel The X-axis label of the bar (e.g. "Jan", "Jan '24", "2024").
 */
fun formatBarChartPopupLabel(amount: Float, currencyCode: String, barLabel: String): String =
    "${amount.toDouble().formatPrice(currencyCode)} - $barLabel"
