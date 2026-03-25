package com.akole.dividox.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

fun KoinInitializer.init(application: Application) {
    init {
        androidLogger()
        androidContext(application)
    }
}
