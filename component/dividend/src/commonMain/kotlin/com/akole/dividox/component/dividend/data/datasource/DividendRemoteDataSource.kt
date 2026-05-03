package com.akole.dividox.component.dividend.data.datasource

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

private const val COLLECTION_DIVIDENDS = "dividends"
private const val FIELD_TICKER_ID = "tickerId"
private const val FIELD_AMOUNT = "amount"
private const val FIELD_CURRENCY = "currency"
private const val FIELD_PAYMENT_DATE = "paymentDate"

/**
 * Remote data source backed by Firestore.
 *
 * Collection path: `users/{uid}/dividends`
 *
 * Each document stores a dividend payment with fields:
 * - `tickerId` (String)
 * - `amount` (Double)
 * - `currency` (String)
 * - `paymentDate` (String, ISO-8601)
 *
 * @property userId Authenticated user ID used to scope the collection.
 */
class DividendRemoteDataSource(
    private val userId: String,
) {

    private val collection
        get() = Firebase.firestore.collection("users/$userId/$COLLECTION_DIVIDENDS")

    /**
     * Observes the Firestore dividend collection as a stream of domain models.
     */
    fun observeAll(): Flow<List<DividendPayment>> =
        collection.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                runCatching { doc.toDividendPayment() }.getOrNull()
            }
        }

    /**
     * Writes a new dividend payment document to Firestore.
     *
     * @param payment The payment to persist.
     */
    suspend fun add(payment: DividendPayment) {
        collection.document(payment.id.value).set(
            mapOf(
                FIELD_TICKER_ID to payment.tickerId,
                FIELD_AMOUNT to payment.amount,
                FIELD_CURRENCY to payment.currency,
                FIELD_PAYMENT_DATE to payment.paymentDate.toString(),
            ),
        )
    }

    private fun dev.gitlive.firebase.firestore.DocumentSnapshot.toDividendPayment(): DividendPayment =
        DividendPayment(
            id = DividendPaymentId(id),
            tickerId = get(FIELD_TICKER_ID),
            amount = get(FIELD_AMOUNT),
            currency = get(FIELD_CURRENCY),
            paymentDate = LocalDate.parse(get(FIELD_PAYMENT_DATE)),
        )
}

