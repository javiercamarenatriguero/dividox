package com.akole.dividox

import android.app.Application
import com.akole.dividox.di.KoinInitializer
import com.akole.dividox.di.init

class DividoxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initGoogleSignIn(this)
        KoinInitializer.init(this)
    }
}
