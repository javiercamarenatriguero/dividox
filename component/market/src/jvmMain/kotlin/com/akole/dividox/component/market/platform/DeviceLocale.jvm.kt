package com.akole.dividox.component.market.platform

import java.util.Locale

actual fun deviceLanguage(): String = Locale.getDefault().language
actual fun deviceRegion(): String = Locale.getDefault().country.ifEmpty { "US" }
