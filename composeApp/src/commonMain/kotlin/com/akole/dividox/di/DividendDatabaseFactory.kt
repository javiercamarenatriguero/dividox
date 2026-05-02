package com.akole.dividox.di

import androidx.room.RoomDatabase
import com.akole.dividox.component.dividend.data.db.DividendDatabase

/**
 * Platform-specific factory for the [DividendDatabase] Room builder.
 *
 * Each platform provides the correct file path and driver configuration:
 * - **Android**: uses [android.content.Context.getDatabasePath] + [BundledSQLiteDriver]
 * - **JVM**: uses `user.home` system property + [BundledSQLiteDriver]
 * - **iOS**: uses [platform.Foundation.NSHomeDirectory] Library dir + [BundledSQLiteDriver]
 */
internal expect fun createDividendDatabaseBuilder(): RoomDatabase.Builder<DividendDatabase>
