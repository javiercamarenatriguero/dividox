package com.akole.dividox.component.dividend.domain.model

import kotlin.jvm.JvmInline

/**
 * Type-safe wrapper for a dividend payment identifier.
 *
 * @property value The underlying string identifier (e.g., Firestore document ID).
 */
@JvmInline
value class DividendPaymentId(val value: String)
