package com.akole.dividox.component.dividend.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase

/**
 * Room database for local dividend payment cache.
 *
 * Version history:
 * - 1: Initial schema with [DividendPaymentEntity].
 * - 2: Removed `method` column (PaymentMethod cannot be determined automatically from APIs).
 * - 3: Added `amount_per_share` and `shares` columns for per-share breakdown display.
 *
 * Instances are created platform-specifically via [createDividendDatabaseBuilder].
 */
@Database(
    entities = [DividendPaymentEntity::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = DividendDatabase.Migration1To2::class),
        AutoMigration(from = 2, to = 3),
    ],
)
@androidx.room.ConstructedBy(DividendDatabaseConstructor::class)
abstract class DividendDatabase : RoomDatabase() {

    @DeleteColumn(tableName = "dividend_payments", columnName = "method")
    class Migration1To2 : androidx.room.migration.AutoMigrationSpec

    /** Returns the DAO for dividend payment access. */
    abstract fun dividendDao(): DividendDao
}
