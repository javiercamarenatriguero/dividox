package com.akole.dividox.component.dividend.data.mapper

import com.akole.dividox.component.dividend.data.db.DividendPaymentEntity
import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import kotlinx.datetime.LocalDate

/**
 * Maps between [DividendPaymentEntity] (Room) and [DividendPayment] (domain).
 *
 * - `paymentDate` is stored as ISO-8601 string (YYYY-MM-DD) in Room.
 */

/** Converts a Room entity to its domain model equivalent. */
internal fun DividendPaymentEntity.toDomain(): DividendPayment = DividendPayment(
    id = DividendPaymentId(id),
    tickerId = tickerId,
    amount = amount,
    amountPerShare = amountPerShare,
    shares = shares,
    currency = currency,
    paymentDate = LocalDate.parse(paymentDate),
)

/** Converts a domain model to its Room entity equivalent. */
internal fun DividendPayment.toEntity(): DividendPaymentEntity = DividendPaymentEntity(
    id = id.value,
    tickerId = tickerId,
    amount = amount,
    amountPerShare = amountPerShare,
    shares = shares,
    currency = currency,
    paymentDate = paymentDate.toString(),
)
