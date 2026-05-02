package com.akole.dividox.component.dividend

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.model.PaymentMethod
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class FakeDividendRepository : DividendRepository {

    private val payments = MutableStateFlow<List<DividendPayment>>(emptyList())

    /** Replaces all stored payments with [list], triggering downstream observers. */
    fun setPayments(list: List<DividendPayment>) {
        payments.value = list
    }

    override fun getDividendHistory(): Flow<List<DividendPayment>> = payments

    override fun getLifetimeDividends(): Flow<Double> = flow {
        payments.collect { list ->
            emit(list.filter { it.method == PaymentMethod.CASH }.sumOf { it.amount })
        }
    }

    override fun getYtdDividends(): Flow<Double> = flow {
        val currentYear = Clock.System.todayIn(TimeZone.UTC).year
        payments.collect { list ->
            emit(
                list.filter { it.method == PaymentMethod.CASH && it.paymentDate.year == currentYear }
                    .sumOf { it.amount },
            )
        }
    }

    override fun getUpcomingPayments(): Flow<List<DividendPayment>> = flow {
        val today = Clock.System.todayIn(TimeZone.UTC)
        payments.collect { list -> emit(list.filter { it.paymentDate > today }) }
    }

    override suspend fun addDividendPayment(payment: DividendPayment) {
        payments.update { it + payment }
    }

    companion object {
        /**
         * Constructs a sample [DividendPayment] with sensible defaults for use in tests.
         *
         * @param id Unique payment identifier.
         * @param ticker Ticker symbol.
         * @param amount Gross dividend amount.
         * @param currency ISO 4217 currency code.
         * @param paymentDate Date the dividend was paid.
         * @param method Cash or reinvested.
         */
        fun samplePayment(
            id: String = "p1",
            ticker: String = "AAPL",
            amount: Double = 100.0,
            currency: String = "USD",
            paymentDate: LocalDate = LocalDate(2025, 3, 15),
            method: PaymentMethod = PaymentMethod.CASH,
        ): DividendPayment = DividendPayment(
            id = DividendPaymentId(id),
            tickerId = ticker,
            amount = amount,
            currency = currency,
            paymentDate = paymentDate,
            method = method,
        )
    }
}
