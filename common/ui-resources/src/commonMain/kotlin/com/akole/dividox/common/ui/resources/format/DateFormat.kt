package com.akole.dividox.common.ui.resources.format

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * Formats a [LocalDate] as DD/MM/YYYY (e.g. "03/05/2026").
 */
fun LocalDate.formatShort(): String =
    "${day.toString().padStart(2, '0')}/${(month.ordinal + 1).toString().padStart(2, '0')}/$year"

/**
 * Returns a 3-letter month abbreviation (e.g. "Jan", "Feb").
 */
fun LocalDate.monthShort(): String = month.shortName()

/**
 * Returns the full English name of the month (e.g. "January").
 */
fun Month.fullName(): String = when (this) {
    Month.JANUARY -> "January"
    Month.FEBRUARY -> "February"
    Month.MARCH -> "March"
    Month.APRIL -> "April"
    Month.MAY -> "May"
    Month.JUNE -> "June"
    Month.JULY -> "July"
    Month.AUGUST -> "August"
    Month.SEPTEMBER -> "September"
    Month.OCTOBER -> "October"
    Month.NOVEMBER -> "November"
    Month.DECEMBER -> "December"
}

/**
 * Returns a 3-letter abbreviated name of the month (e.g. "Jan").
 */
fun Month.shortName(): String = fullName().take(3)
