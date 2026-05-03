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
    ONE_YEAR("1y", "1Y"),
    TWO_YEARS("2y", "2Y"),
    FIVE_YEARS("5y", "5Y"),
    MAX("max", "Max"),
}
