package com.akole.dividox.common.settings.data.datastore

import android.content.Context
import org.koin.mp.KoinPlatform

internal actual fun dataStorePath(): String {
    val context: Context = KoinPlatform.getKoin().get()
    return context.filesDir.path + "/$DATASTORE_FILE_NAME"
}
