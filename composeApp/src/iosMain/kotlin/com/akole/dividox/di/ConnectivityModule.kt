package com.akole.dividox.di

import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import org.koin.dsl.module

actual val connectivityModule = module {
    single<NetworkConnectivityManager> {
        NetworkConnectivityManager()
    }
}
