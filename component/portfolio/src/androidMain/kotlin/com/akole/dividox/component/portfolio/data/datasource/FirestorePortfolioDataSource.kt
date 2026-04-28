package com.akole.dividox.component.portfolio.data.datasource

import com.akole.dividox.component.portfolio.data.dto.HoldingDto
import com.akole.dividox.component.portfolio.data.mapper.toDomain
import com.akole.dividox.component.portfolio.data.mapper.toDto
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore implementation of portfolio data source.
 * Persists holdings at: `users/{userId}/portfolio/{holdingId}`
 *
 * @property firestore Firebase Firestore instance (singleton from Firebase SDK)
 * @property userId Current authenticated user ID—determines collection path
 */
class FirestorePortfolioDataSource(
    private val firestore: FirebaseFirestore,
    private val userId: String,
) : PortfolioDataSource {
    private val collectionRef
        get() = firestore.collection("users").document(userId).collection("portfolio")

    /**
     * Observe portfolio holdings as a reactive stream.
     * Emits immediately on subscription and on every Firestore change.
     * Listener cleanup handled by awaitClose.
     *
     * @return Flow emitting Result<List<Holding>> on each change or error
     */
    override fun observePortfolio(): Flow<Result<List<Holding>>> = callbackFlow {
        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val holdings = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(HoldingDto::class.java)?.copy(id = doc.id)?.toDomain()
                }
                trySend(Result.success(holdings))
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Fetch current portfolio snapshot (one-shot read).
     *
     * @return Result.success with holdings list or Result.failure on error
     */
    override suspend fun getPortfolio(): Result<List<Holding>> = try {
        val snapshot = collectionRef.get().await()
        val holdings = snapshot.documents.mapNotNull { doc ->
            doc.toObject(HoldingDto::class.java)?.copy(id = doc.id)?.toDomain()
        }
        Result.success(holdings)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Add new holding to portfolio.
     * Firestore auto-generates document ID.
     *
     * @param holding share holding to persist
     * @return Result.success with generated HoldingId or Result.failure
     */
    override suspend fun addHolding(holding: Holding): Result<HoldingId> = try {
        val dto = holding.toDto().copy(id = "")
        val docRef = collectionRef.add(dto).await()
        Result.success(HoldingId(docRef.id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update existing holding.
     * Replaces entire document at `{holdingId}`.
     *
     * @param holding updated holding (id field identifies record)
     * @return Result.success on update or Result.failure
     */
    override suspend fun updateHolding(holding: Holding): Result<Unit> = try {
        collectionRef.document(holding.id.value).set(holding.toDto()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Remove holding from portfolio.
     *
     * @param holdingId ID of holding to delete
     * @return Result.success on delete or Result.failure
     */
    override suspend fun removeHolding(holdingId: HoldingId): Result<Unit> = try {
        collectionRef.document(holdingId.value).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
