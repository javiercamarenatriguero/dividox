package com.akole.dividox.component.dividend.data.db

import com.akole.dividox.component.dividend.data.datasource.DividendLocalDataSource

/**
 * Convenience factory that builds the [DividendLocalDataSource] using the platform-specific
 * Room database builder.
 *
 * Encapsulates [DividendDatabase] and [DividendDao] creation so that callers (e.g., the Koin
 * DI module in `:composeApp`) do not need Room on their direct classpath.
 */
fun buildDividendLocalDataSource(): DividendLocalDataSource =
    DividendLocalDataSource(createDividendDatabaseBuilder().build().dividendDao())
