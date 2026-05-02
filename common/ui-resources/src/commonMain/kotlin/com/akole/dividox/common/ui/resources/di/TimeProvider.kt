package com.akole.dividox.common.ui.resources.di

import kotlin.time.Clock

fun getCurrentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
