package com.akole.dividox.di

import com.akole.dividox.common.auth.createAuthRepository
import com.akole.dividox.common.auth.domain.repository.AuthRepository
import com.akole.dividox.common.auth.domain.usecase.ForgotPasswordUseCase
import com.akole.dividox.common.auth.domain.usecase.ObserveSessionUseCase
import com.akole.dividox.common.auth.domain.usecase.SignInWithEmailUseCase
import com.akole.dividox.common.auth.domain.usecase.SignInWithGoogleUseCase
import com.akole.dividox.common.auth.domain.usecase.SignOutUseCase
import com.akole.dividox.common.auth.domain.usecase.SignUpWithEmailUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule: Module = module {
    single<AuthRepository> { createAuthRepository() }
    factoryOf(::ObserveSessionUseCase)
    factoryOf(::SignInWithEmailUseCase)
    factoryOf(::SignUpWithEmailUseCase)
    factoryOf(::SignInWithGoogleUseCase)
    factoryOf(::ForgotPasswordUseCase)
    factoryOf(::SignOutUseCase)
}
