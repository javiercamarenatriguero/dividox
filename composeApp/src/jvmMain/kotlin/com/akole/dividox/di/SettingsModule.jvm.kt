package com.akole.dividox.di

import com.akole.dividox.common.settings.data.datastore.DATASTORE_FILE_NAME

internal actual fun dataStorePath(): String =
    System.getProperty("user.home") + "/.dividox/$DATASTORE_FILE_NAME"
