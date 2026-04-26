package com.akole.dividox.common.auth

import com.akole.dividox.common.auth.data.AuthDataSource

actual fun createAuthDataSource(): AuthDataSource = AuthDataSource()
