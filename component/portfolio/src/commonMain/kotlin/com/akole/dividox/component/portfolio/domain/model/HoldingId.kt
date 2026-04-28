package com.akole.dividox.component.portfolio.domain.model

import kotlin.jvm.JvmInline

/**
 * Type-safe wrapper for holding IDs. Ensures non-blank string values.
 *
 * @property value Unique holding identifier
 */
@JvmInline
value class HoldingId(val value: String) {
    init {
        require(value.isNotBlank()) { "HoldingId cannot be blank" }
    }
}
