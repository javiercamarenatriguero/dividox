package com.akole.dividox.common.settings.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal const val DATASTORE_FILE_NAME = "dividox_app_settings.preferences_pb"

internal expect fun dataStorePath(): String

internal fun createDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath {
        dataStorePath().toPath()
    }
