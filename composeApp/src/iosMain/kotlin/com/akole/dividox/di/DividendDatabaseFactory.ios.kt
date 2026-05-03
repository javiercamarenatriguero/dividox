package com.akole.dividox.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akole.dividox.component.dividend.data.db.DividendDatabase
import platform.Foundation.NSHomeDirectory

/**
 * iOS implementation: creates a file-backed [DividendDatabase] in the app's Library directory.
 *
 * Steps:
 * 1. Obtains the app sandbox home directory via [NSHomeDirectory].
 * 2. Appends the Library subdirectory (standard iOS location for DB files).
 * 3. Configures the builder with [BundledSQLiteDriver].
 */
internal actual fun createDividendDatabaseBuilder(): RoomDatabase.Builder<DividendDatabase> {
    val dbPath = NSHomeDirectory() + "/Library/dividox_dividends.db"
    return Room.databaseBuilder<DividendDatabase>(name = dbPath)
        .setDriver(BundledSQLiteDriver())
}
