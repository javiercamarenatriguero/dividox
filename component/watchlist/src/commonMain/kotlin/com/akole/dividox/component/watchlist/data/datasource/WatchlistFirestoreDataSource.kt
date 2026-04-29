package com.akole.dividox.component.watchlist.data.datasource

import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Firestore KMP implementation of watchlist data source via dev.gitlive:firebase-firestore.
 * Persists watchlist entries at: `users/{userId}/watchlist/{tickerId}`
 * Document ID is the tickerId for simple existence checks.
 *
 * @property userId Current authenticated user ID—determines collection path
 */
class WatchlistFirestoreDataSource(
    private val userId: String,
) : WatchlistDataSource {

    private val collectionRef
        get() = Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("watchlist")

    override fun observeWatchlist(): Flow<List<WatchlistEntry>> =
        collectionRef.snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    val dto = doc.data<WatchlistEntryDto>().copy(tickerId = doc.id)
                    dto.toDomain()
                }
            }
            .catch { emit(emptyList()) }

    override suspend fun addToWatchlist(tickerId: String) {
        val dto = WatchlistEntryDto(
            tickerId = tickerId,
            addedAtMillis = Clock.System.now().toEpochMilliseconds(),
        )
        collectionRef.document(tickerId).set(dto)
    }

    override suspend fun removeFromWatchlist(tickerId: String) {
        collectionRef.document(tickerId).delete()
    }
}

@Serializable
private data class WatchlistEntryDto(
    @Transient val tickerId: String = "",
    val addedAtMillis: Long = 0L,
) {
    fun toDomain(): WatchlistEntry = WatchlistEntry(
        tickerId = tickerId,
        addedAt = Instant.fromEpochMilliseconds(addedAtMillis),
    )
}
