package com.akole.dividox

import platform.Foundation.NSBundle

actual fun getAppVersion(): String =
    NSBundle.mainBundle().infoDictionary?.get("CFBundleShortVersionString") as? String ?: "1.0"
