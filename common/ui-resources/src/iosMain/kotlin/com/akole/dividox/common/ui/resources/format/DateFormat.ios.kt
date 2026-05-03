package com.akole.dividox.common.ui.resources.format

import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun LocalDate.monthShort(): String = formatWithPattern("MMM")

actual fun LocalDate.monthFull(): String = formatWithPattern("MMMM")

private fun LocalDate.formatWithPattern(pattern: String): String {
    val components = NSDateComponents().apply {
        setYear(this@formatWithPattern.year.toLong())
        setMonth(this@formatWithPattern.monthNumber.toLong())
        setDay(this@formatWithPattern.dayOfMonth.toLong())
    }
    val calendar = NSCalendar.currentCalendar
    val nsDate = calendar.dateFromComponents(components) ?: return ""
    val formatter = NSDateFormatter().apply {
        dateFormat = pattern
        locale = NSLocale.currentLocale
    }
    return formatter.stringFromDate(nsDate)
}
