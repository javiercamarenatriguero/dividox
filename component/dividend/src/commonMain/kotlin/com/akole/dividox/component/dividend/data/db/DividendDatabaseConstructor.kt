package com.akole.dividox.component.dividend.data.db

import androidx.room.RoomDatabaseConstructor

/**
 * Expect declaration for the Room-generated database constructor.
 *
 * Room KMP generates the `actual` implementation automatically via KSP —
 * **do not write manual actuals**.
 */
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object DividendDatabaseConstructor : RoomDatabaseConstructor<DividendDatabase> {
    override fun initialize(): DividendDatabase
}
