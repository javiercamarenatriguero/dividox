package com.akole.dividox.component.dividend.data.db

import com.akole.dividox.component.dividend.data.datasource.DividendLocalDataSource

/**
 * Convenience factory that creates a [DividendLocalDataSource] from a given [DividendDao].
 *
 * The [DividendDao] is obtained by the caller (typically the DI module in `:composeApp`)
 * from a platform-specific [DividendDatabase] builder.
 *
 * @param dao The Room DAO to wrap.
 */
fun buildDividendLocalDataSource(dao: DividendDao): DividendLocalDataSource =
    DividendLocalDataSource(dao)
