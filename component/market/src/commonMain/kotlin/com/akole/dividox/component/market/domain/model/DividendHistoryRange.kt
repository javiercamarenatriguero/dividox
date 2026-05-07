package com.akole.dividox.component.market.domain.model

/**
 * Historical range used when fetching dividend event history from the market API.
 *
 * @property apiValue The range string accepted by Yahoo Finance chart endpoint.
 * @property label Short display label for UI chips.
 */
enum class DividendHistoryRange(val apiValue: String, val label: String) {
    /** From January 1st of the current year to today. */
    YTD("ytd", "YTD"),
    /** Last year. */
    ONE_YEAR("1y", "1Y"),
    /** Last 5 years. */
    FIVE_YEARS("5y", "5Y"),
    /** All historical data. */
    MAX("max", "Max"),
}
