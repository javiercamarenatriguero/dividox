package com.akole.dividox.feature.dividends

import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until

internal fun projectUpcomingPayments(
    history: List<EnrichedPayment>,
    today: LocalDate,
    endOfYear: LocalDate,
): List<EnrichedPayment> {
    val projected = mutableListOf<EnrichedPayment>()
    history.groupBy { it.payment.tickerId }.forEach { (_, payments) ->
        val sorted = payments.sortedBy { it.payment.paymentDate }
        if (sorted.size < 2) return@forEach
        val last = sorted.last()
        val secondLast = sorted[sorted.size - 2]
        val intervalDays = secondLast.payment.paymentDate.until(last.payment.paymentDate, DateTimeUnit.DAY)
        if (intervalDays <= 0) return@forEach
        var nextDate = last.payment.paymentDate.plus(intervalDays, DateTimeUnit.DAY)
        while (nextDate < today) nextDate = nextDate.plus(intervalDays, DateTimeUnit.DAY)
        var count = 0
        while (nextDate <= endOfYear && count < 3) {
            projected += EnrichedPayment(
                payment = last.payment.copy(
                    id = DividendPaymentId("${last.payment.tickerId}-proj-$nextDate"),
                    paymentDate = nextDate,
                ),
                companyInfo = last.companyInfo,
            )
            nextDate = nextDate.plus(intervalDays, DateTimeUnit.DAY)
            count++
        }
    }
    return projected
}
