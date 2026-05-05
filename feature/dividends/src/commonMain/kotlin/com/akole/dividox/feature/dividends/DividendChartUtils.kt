package com.akole.dividox.feature.dividends

import com.akole.dividox.common.ui.resources.format.monthShort
import com.akole.dividox.common.ui.resources.format.monthShortWithYear
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import kotlinx.datetime.LocalDate

internal fun LocalDate.toBarLabel(range: DividendHistoryRange): String = when (range) {
    DividendHistoryRange.YTD,
    DividendHistoryRange.ONE_YEAR -> monthShort()
    DividendHistoryRange.TWO_YEARS -> monthShortWithYear()
    DividendHistoryRange.FIVE_YEARS,
    DividendHistoryRange.MAX -> year.toString()
}
