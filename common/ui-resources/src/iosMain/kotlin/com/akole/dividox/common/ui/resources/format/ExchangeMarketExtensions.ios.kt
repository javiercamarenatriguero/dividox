package com.akole.dividox.common.ui.resources.format

import com.akole.dividox.common.ui.resources.components.ExchangeMarket

// Flag emojis (Regional Indicator sequences) don't render in Skia on iOS.
// Return empty string — the label already identifies the market (US, UK, DE…).
actual fun ExchangeMarket.flag(): String = ""
