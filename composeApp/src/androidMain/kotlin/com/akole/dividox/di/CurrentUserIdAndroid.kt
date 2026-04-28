package com.akole.dividox.di

import com.google.firebase.auth.FirebaseAuth

internal actual fun getCurrentUserId(): String {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    return uid ?: throw IllegalStateException("No authenticated user found. User must sign in first.")
}
