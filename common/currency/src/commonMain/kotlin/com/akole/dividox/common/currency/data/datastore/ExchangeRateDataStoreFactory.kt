package com.akole.dividox.common.currency.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/** Filename used for the DataStore Preferences file across all platforms. */
const val EXCHANGE_RATE_DATASTORE_FILE_NAME = "dividox_exchange_rates.preferences_pb"

/**
 * Creates the [DataStore] instance for exchange rate persistence.
 *
 * Uses [PreferenceDataStoreFactory.createWithPath] so the file location can be
 * supplied by each platform's actual implementation.
 *
 * @param producePath Lambda that returns the absolute file path for the DataStore file.
 *                    Called lazily on first access.
 * @return A [DataStore] backed by [EXCHANGE_RATE_DATASTORE_FILE_NAME] at the given path.
 */
fun createExchangeRateDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath {
        producePath().toPath()
    }
