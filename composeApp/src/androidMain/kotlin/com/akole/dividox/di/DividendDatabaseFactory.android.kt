package com.akole.dividox.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akole.dividox.component.dividend.data.db.DividendDatabase
import org.koin.mp.KoinPlatform

/**
 * Android implementation: creates a file-backed [DividendDatabase] in the app's database dir.
 *
 * Steps:
 * 1. Obtains the Android application context from Koin's global context.
 * 2. Resolves the database file path using [Context.getDatabasePath].
 * 3. Configures the builder with [BundledSQLiteDriver] for KMP compatibility.
 */
internal actual fun createDividendDatabaseBuilder(): RoomDatabase.Builder<DividendDatabase> {
    val context: Context = KoinPlatform.getKoin().get()
    val dbFile = context.getDatabasePath("dividox_dividends.db")
    return Room.databaseBuilder<DividendDatabase>(context, dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
}
