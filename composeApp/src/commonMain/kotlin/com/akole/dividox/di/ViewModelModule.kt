package com.akole.dividox.di

import com.akole.dividox.feature.home.HomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { params ->
        HomeViewModel(
            greeting = params.get(),
            platformName = params.get(),
        )
    }
}
