package com.akole.dividox

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform