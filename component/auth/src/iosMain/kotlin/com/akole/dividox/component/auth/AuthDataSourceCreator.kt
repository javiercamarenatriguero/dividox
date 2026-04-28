package com.akole.dividox.component.auth

import com.akole.dividox.component.auth.data.AuthDataSource

actual fun createAuthDataSource(): AuthDataSource = AuthDataSource()
