package com.akole.dividox.component.dividend.domain.model

/** Indicates how a dividend payment was received. */
enum class PaymentMethod {
    /** Cash dividend deposited to the account. */
    CASH,

    /** Dividend reinvested into more shares (DRIP). */
    REINVESTED,
}
