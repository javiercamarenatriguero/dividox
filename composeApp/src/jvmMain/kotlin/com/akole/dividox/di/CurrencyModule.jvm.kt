package com.akole.dividox.di

import com.akole.dividox.common.currency.data.datastore.EXCHANGE_RATE_DATASTORE_FILE_NAME

internal actual fun exchangeRatePath(): String =
    System.getProperty("user.home") + "/.dividox/$EXCHANGE_RATE_DATASTORE_FILE_NAME"
