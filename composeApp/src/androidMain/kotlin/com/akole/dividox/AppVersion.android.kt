package com.akole.dividox

import android.content.pm.PackageManager
import org.koin.java.KoinJavaComponent.getKoin
import android.app.Application

actual fun getAppVersion(): String = try {
    val app = getKoin().get<Application>()
    app.packageManager.getPackageInfo(app.packageName, PackageManager.GET_ACTIVITIES).versionName ?: "1.0"
} catch (e: Exception) {
    "1.0"
}
