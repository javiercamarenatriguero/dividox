package com.akole.dividox.di

import android.content.Context
import com.akole.dividox.common.settings.data.datastore.DATASTORE_FILE_NAME
import org.koin.mp.KoinPlatform

internal actual fun dataStorePath(): String =
    KoinPlatform.getKoin().get<Context>().filesDir.path + "/$DATASTORE_FILE_NAME"
