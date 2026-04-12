package com.akole.dividox.di

import com.akole.dividox.common.auth.domain.repository.AuthRepository
import com.akole.dividox.common.auth.domain.repository.StubAuthRepository
import com.akole.dividox.common.auth.domain.usecase.ObserveSessionUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule: Module = module {
    single<AuthRepository> { StubAuthRepository() }
    factoryOf(::ObserveSessionUseCase)
}
