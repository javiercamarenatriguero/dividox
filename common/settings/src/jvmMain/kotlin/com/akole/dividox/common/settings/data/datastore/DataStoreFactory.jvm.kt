package com.akole.dividox.common.settings.data.datastore

internal actual fun dataStorePath(): String =
    System.getProperty("user.home") + "/.dividox/$DATASTORE_FILE_NAME"
