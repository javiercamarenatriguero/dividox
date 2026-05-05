package com.akole.dividox.di

import com.akole.dividox.component.auth.createAuthRepository
import com.akole.dividox.component.auth.data.GoogleSignInLauncher
import com.akole.dividox.component.auth.domain.model.AuthUser
import com.akole.dividox.component.auth.domain.repository.AuthRepository
import com.akole.dividox.component.auth.domain.usecase.ForgotPasswordUseCase
import com.akole.dividox.component.auth.domain.usecase.GetCurrentUserIdUseCase
import com.akole.dividox.component.auth.domain.usecase.ObserveSessionUseCase
import com.akole.dividox.component.auth.domain.usecase.SignInWithEmailUseCase
import com.akole.dividox.component.auth.domain.usecase.SignInWithGoogleUseCase
import com.akole.dividox.component.auth.domain.usecase.SignOutUseCase
import com.akole.dividox.component.auth.domain.usecase.SignUpWithEmailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule: Module = module {
    single<AuthRepository> { createAuthRepository() }
    single { GoogleSignInLauncher() }

    // Hot shared StateFlow of current authenticated userId (null when signed out).
    // Started eagerly so it's always up-to-date for Firestore data sources.
    single<StateFlow<String?>> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        get<AuthRepository>().observeAuthState()
            .map { user: AuthUser? -> user?.uid }
            .stateIn(scope, SharingStarted.Eagerly, null)
    }

    factoryOf(::GetCurrentUserIdUseCase)
    factoryOf(::ObserveSessionUseCase)
    factoryOf(::SignInWithEmailUseCase)
    factoryOf(::SignUpWithEmailUseCase)
    factoryOf(::SignInWithGoogleUseCase)
    factoryOf(::ForgotPasswordUseCase)
    factoryOf(::SignOutUseCase)
}
