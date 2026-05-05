package com.akole.dividox.common.ui.resources.format

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

actual fun LocalDate.monthShort(): String =
    DateTimeFormatter.ofPattern("MMM", Locale.getDefault()).format(toJavaLocalDate())

actual fun LocalDate.monthFull(): String =
    DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()).format(toJavaLocalDate())
