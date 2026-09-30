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
 * - 4: Added [StockQuoteEntity] for persistent quote cache across sessions.
 * - 5: Added [CompanyInfoEntity] and [NewsCacheEntity] for persistent company info and news caches.
 * - 6: Added [DividendInfoEntity] for persistent dividend info cache (yield, annual payout, etc.).
 *
 * Instances are created platform-specifically via [createDividendDatabaseBuilder].
 */
@Database(
    entities = [
        DividendPaymentEntity::class,
        StockQuoteEntity::class,
        CompanyInfoEntity::class,
        NewsCacheEntity::class,
        DividendInfoEntity::class,
    ],
    version = 6,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = DividendDatabase.Migration1To2::class),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
    ],
)
@androidx.room.ConstructedBy(DividendDatabaseConstructor::class)
abstract class DividendDatabase : RoomDatabase() {

    @DeleteColumn(tableName = "dividend_payments", columnName = "method")
    class Migration1To2 : androidx.room.migration.AutoMigrationSpec

    /** Returns the DAO for dividend payment access. */
    abstract fun dividendDao(): DividendDao

    /** Returns the DAO for persistent stock quote cache. */
    abstract fun stockQuoteDao(): StockQuoteDao

    /** Returns the DAO for persistent company info cache. */
    abstract fun companyInfoDao(): CompanyInfoDao

    /** Returns the DAO for persistent news cache. */
    abstract fun newsDao(): NewsDao

    /** Returns the DAO for persistent dividend info cache. */
    abstract fun dividendInfoDao(): DividendInfoDao
}
