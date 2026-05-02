package com.akole.dividox.component.dividend.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/**
 * Android implementation: creates a file-backed [DividendDatabase] in the app's database dir.
 *
 * Steps:
 * 1. Obtains the Android application context via Koin.
 * 2. Resolves the database file path using [Context.getDatabasePath].
 * 3. Configures the builder with [BundledSQLiteDriver] for KMP compatibility.
 */
actual fun createDividendDatabaseBuilder(): RoomDatabase.Builder<DividendDatabase> {
    val context: Context = org.koin.core.context.GlobalContext.get().get()
    val dbFile = context.getDatabasePath("dividox_dividends.db")
    return Room.databaseBuilder<DividendDatabase>(context, dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
}
