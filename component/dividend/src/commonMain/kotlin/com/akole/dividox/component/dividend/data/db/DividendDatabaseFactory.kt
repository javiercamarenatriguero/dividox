package com.akole.dividox.component.dividend.data.db

import androidx.room.RoomDatabase

/**
 * Creates a platform-specific Room database builder for [DividendDatabase].
 *
 * Each platform provides its own database file path or context.
 * The caller is responsible for calling [RoomDatabase.Builder.build].
 */
expect fun createDividendDatabaseBuilder(): RoomDatabase.Builder<DividendDatabase>
