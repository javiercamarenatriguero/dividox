package com.akole.dividox.feature.search

enum class ExchangeMarket(
    val emoji: String,
    val label: String,
    val region: String?,
    private val keywords: Set<String>,
) {
    ALL("🌍", "All", null, emptySet()),
    US("🇺🇸", "US", "US", setOf("NASDAQ", "NYSE", "AMEX", "OTC", "ARCA", "PCX")),
    UK("🇬🇧", "UK", "GB", setOf("LSE", "LONDON", "LON")),
    DE("🇩🇪", "DE", "DE", setOf("XETRA", "FSX", "FRA", "GER")),
    ES("🇪🇸", "ES", "ES", setOf("BME", "MCE", "MAD", "MADRID")),
    FR("🇫🇷", "FR", "FR", setOf("PAR", "EPA", "ENX", "PARIS")),
    IT("🇮🇹", "IT", "IT", setOf("MIL", "BIT", "MILAN")),
    CA("🇨🇦", "CA", "CA", setOf("TSX", "CVE", "TORONTO")),
    AU("🇦🇺", "AU", "AU", setOf("ASX", "SYDNEY")),
    JP("🇯🇵", "JP", "JP", setOf("TSE", "OSE", "TOKYO")),
    ;

    fun matches(exchange: String?): Boolean {
        if (this == ALL) return true
        val upper = exchange?.uppercase() ?: return false
        return keywords.any { upper.contains(it) }
    }
}
