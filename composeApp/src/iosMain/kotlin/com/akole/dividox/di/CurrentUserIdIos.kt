package com.akole.dividox.di

internal actual fun getCurrentUserId(): String {
    throw NotImplementedError("Firebase Auth not configured for iOS in MVP. Use stub user-id for testing.")
}
