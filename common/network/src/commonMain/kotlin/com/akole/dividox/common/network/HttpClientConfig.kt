package com.akole.dividox.common.network

data class HttpClientConfig(
    val timeoutMs: Long = 10_000,
    val defaultHeaders: Map<String, String> = emptyMap(),
)
