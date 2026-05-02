package com.akole.dividox.di

import com.akole.dividox.common.currency.data.datastore.EXCHANGE_RATE_DATASTORE_FILE_NAME
import org.koin.mp.KoinPlatform
import android.content.Context

internal actual fun exchangeRatePath(): String =
    KoinPlatform.getKoin().get<Context>().filesDir.path + "/$EXCHANGE_RATE_DATASTORE_FILE_NAME"
