package com.akole.dividox.di

import com.akole.dividox.feature.auth.login.LoginViewModel
import com.akole.dividox.feature.auth.register.SignUpViewModel
import com.akole.dividox.feature.home.HomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { params ->
        HomeViewModel(
            greeting = params.get(),
            platformName = params.get(),
        )
    }
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignUpViewModel)
}
