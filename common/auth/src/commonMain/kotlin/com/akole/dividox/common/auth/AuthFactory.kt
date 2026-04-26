package com.akole.dividox.common.auth

import com.akole.dividox.common.auth.data.AuthDataSource
import com.akole.dividox.common.auth.data.AuthRepositoryImpl
import com.akole.dividox.common.auth.domain.repository.AuthRepository

/**
 * Factory for platform-specific [AuthDataSource] implementation.
 * Each platform creates its own data source instance (Firebase on Android/iOS, stub on Desktop).
 * @return Platform-specific [AuthDataSource]
 */
expect fun createAuthDataSource(): AuthDataSource

/**
 * Factory for creating the default [AuthRepository] implementation.
 * Wired as singleton in Koin DI container.
 * @return [AuthRepositoryImpl] with platform-specific [AuthDataSource]
 */
fun createAuthRepository(): AuthRepository = AuthRepositoryImpl(createAuthDataSource())
