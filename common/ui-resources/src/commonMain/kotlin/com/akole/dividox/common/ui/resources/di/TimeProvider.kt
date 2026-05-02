package com.akole.dividox.common.ui.resources.di

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Returns the current epoch time in milliseconds. */
fun getCurrentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

/**
 * Returns today's [LocalDate] in the given [timeZone].
 *
 * Defaults to the device's current system time zone. Pass [TimeZone.UTC] when
 * you need a timezone-independent reference (e.g. for exchange-rate freshness checks).
 *
 * @param timeZone The time zone used to determine "today". Defaults to [TimeZone.currentSystemDefault].
 */
fun todayIn(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Clock.System.todayIn(timeZone)
