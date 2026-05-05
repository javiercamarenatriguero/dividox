package com.akole.dividox.component.portfolio.data.datasource

import com.akole.dividox.component.portfolio.data.dto.HoldingDto
import com.akole.dividox.component.portfolio.data.mapper.toDomain
import com.akole.dividox.component.portfolio.data.mapper.toDto
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen

/**
 * Firestore KMP implementation of portfolio data source via dev.gitlive:firebase-firestore.
 * Persists holdings at: `users/{userId}/portfolio/{holdingId}`
 *
 * @property userIdProvider Provides the current authenticated user ID for write operations.
 * @property authUserIdFlow Emits the authenticated user ID (or null when signed out).
 *   [observePortfolio] reacts to this flow so that the Firestore subscription is automatically
 *   created on login and cancelled on logout — eliminating the race condition between
 *   Firebase Auth token propagation and the first Firestore read on a fresh install.
 */
class FirestorePortfolioDataSource(
    private val userIdProvider: () -> String,
    private val authUserIdFlow: Flow<String?>,
) : PortfolioDataSource {

    private val collectionRef: CollectionReference
        get() = collectionRefFor(userIdProvider())

    private fun collectionRefFor(userId: String): CollectionReference =
        Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("portfolio")

    /**
     * Observe portfolio as a resilient reactive stream.
     *
     * Subscribes to [authUserIdFlow] so that:
     * - On login the Firestore subscription is started with the correct authenticated userId.
     * - On logout the Firestore listener is immediately cancelled and an empty result is emitted.
     * - On auth token changes (same uid) nothing changes (distinctUntilChanged).
     *
     * Uses Firestore's real-time snapshot listener which automatically serves from local
     * disk cache when offline (persistence is enabled by default on Android/iOS).
     *
     * @return Flow emitting Result<List<Holding>> on each change or error.
     */
    override fun observePortfolio(): Flow<Result<List<Holding>>> =
        authUserIdFlow
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                if (userId == null) {
                    flowOf(Result.success(emptyList()))
                } else {
                    observePortfolioForUser(userId)
                }
            }

    /**
     * Observes holdings for a specific user using Firestore's real-time snapshot listener.
     *
     * On Android/iOS, Firestore persistence is enabled by default, so `snapshots()`:
     * - Offline with cache: immediately emits cached data (no network needed).
     * - Online: emits from server (with automatic local cache update).
     * - Fresh install + online: emits once the auth token propagates and the first server
     *   read completes. If a PERMISSION_DENIED error occurs (auth token race), `retryWhen`
     *   retries the listener with exponential back-off.
     */
    private fun observePortfolioForUser(userId: String): Flow<Result<List<Holding>>> {
        val collRef = collectionRefFor(userId)
        return collRef.snapshots()
            .map { snap ->
                Result.success(
                    snap.documents.map { doc ->
                        doc.data<HoldingDto>().copy(id = doc.id).toDomain()
                    },
                )
            }
            .retryWhen { cause, attempt ->
                // Emit failure so downstream can react (e.g. show stale state) rather than
                // silently blocking the combine while we wait for the retry.
                emit(Result.failure(cause))
                delay(minOf(2_000L * (attempt + 1), 10_000L))
                true
            }
    }

    /**
     * Fetch current portfolio snapshot (one-shot read). Used by callers that need a
     * suspend result rather than a flow (e.g. pull-to-refresh).
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
