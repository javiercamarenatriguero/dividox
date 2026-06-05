package com.akole.dividox.component.market.platform

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.countryCode

actual fun deviceLanguage(): String = NSLocale.currentLocale.languageCode ?: "en"
actual fun deviceRegion(): String = NSLocale.currentLocale.countryCode ?: "US"
