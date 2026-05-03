package com.akole.dividox.di

import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import org.koin.dsl.module

/**
 * Koin module for network connectivity services.
 *
 * Registers [NetworkConnectivityManager] as a singleton.
 * Platform-specific implementations are injected via expect/actual mechanism
 * in :common:network module.
 */
expect val connectivityModule: org.koin.core.module.Module
