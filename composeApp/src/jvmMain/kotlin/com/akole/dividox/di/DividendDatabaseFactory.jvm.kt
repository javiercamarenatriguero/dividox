package com.akole.dividox.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akole.dividox.component.dividend.data.db.DividendDatabase

/**
 * JVM (Desktop) implementation: creates a file-backed [DividendDatabase] in the user's home dir.
 *
 * Steps:
 * 1. Resolves the database path from the system user.home property.
 * 2. Configures the builder with [BundledSQLiteDriver].
 */
internal actual fun createDividendDatabaseBuilder(): RoomDatabase.Builder<DividendDatabase> {
    val dbPath = System.getProperty("user.home") + "/dividox_dividends.db"
    return Room.databaseBuilder<DividendDatabase>(name = dbPath)
        .setDriver(BundledSQLiteDriver())
}
