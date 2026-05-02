package com.akole.dividox.component.dividend.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for local dividend payment cache.
 *
 * Version history:
 * - 1: Initial schema with [DividendPaymentEntity].
 *
 * Instances are created platform-specifically via [createDividendDatabaseBuilder].
 */
@Database(entities = [DividendPaymentEntity::class], version = 1)
@androidx.room.ConstructedBy(DividendDatabaseConstructor::class)
abstract class DividendDatabase : RoomDatabase() {
    /** Returns the DAO for dividend payment access. */
    abstract fun dividendDao(): DividendDao
}
