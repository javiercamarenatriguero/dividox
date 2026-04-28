package com.akole.dividox.component.portfolio.data.datasource

import com.akole.dividox.component.portfolio.data.dto.HoldingDto
import com.akole.dividox.component.portfolio.data.mapper.toDomain
import com.akole.dividox.component.portfolio.data.mapper.toDto
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Firestore KMP implementation of portfolio data source via dev.gitlive:firebase-firestore.
 * Persists holdings at: `users/{userId}/portfolio/{holdingId}`
 *
 * @property userId Current authenticated user ID—determines collection path
 */
class FirestorePortfolioDataSource(
    private val userId: String,
) : PortfolioDataSource {

    private val collectionRef
        get() = Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("portfolio")

    /**
     * Observe portfolio as a reactive stream.
     * Emits immediately on subscription and on every Firestore change.
     *
     * @return Flow emitting Result<List<Holding>> on each change or error
     */
    override fun observePortfolio(): Flow<Result<List<Holding>>> =
        collectionRef.snapshots()
            .map { snapshot ->
                val holdings = snapshot.documents.map { doc ->
                    doc.data<HoldingDto>().copy(id = doc.id).toDomain()
                }
                Result.success(holdings)
            }
            .catch { e -> emit(Result.failure(e)) }

    /**
     * Fetch current portfolio snapshot (one-shot read).
     *
     * @return Result.success with holdings list or Result.failure on error
     */
    override suspend fun getPortfolio(): Result<List<Holding>> = try {
        val snapshot = collectionRef.get()
        val holdings = snapshot.documents.map { doc ->
            doc.data<HoldingDto>().copy(id = doc.id).toDomain()
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
        val docRef = collectionRef.add(holding.toDto())
        Result.success(HoldingId(docRef.id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update existing holding (full document replace).
     *
     * @param holding updated holding (id field identifies document)
     * @return Result.success on update or Result.failure
     */
    override suspend fun updateHolding(holding: Holding): Result<Unit> = try {
        collectionRef.document(holding.id.value).set(holding.toDto())
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
        collectionRef.document(holdingId.value).delete()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
