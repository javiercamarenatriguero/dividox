package com.akole.dividox.feature.auth

import com.akole.dividox.component.auth.domain.model.AuthError
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.auth_error_account_not_found
import dividox.common.ui_resources.generated.resources.auth_error_email_in_use
import dividox.common.ui_resources.generated.resources.auth_error_invalid_credentials
import dividox.common.ui_resources.generated.resources.auth_error_invalid_email
import dividox.common.ui_resources.generated.resources.auth_error_network
import dividox.common.ui_resources.generated.resources.auth_error_recent_login_required
import dividox.common.ui_resources.generated.resources.auth_error_terms_required
import dividox.common.ui_resources.generated.resources.auth_error_too_many_attempts
import dividox.common.ui_resources.generated.resources.auth_error_unknown
import dividox.common.ui_resources.generated.resources.auth_error_weak_password
import org.jetbrains.compose.resources.StringResource

internal fun AuthError.messageRes(): StringResource = when (this) {
    AuthError.InvalidCredentials -> Res.string.auth_error_invalid_credentials
    AuthError.AccountNotFound -> Res.string.auth_error_account_not_found
    AuthError.EmailAlreadyInUse -> Res.string.auth_error_email_in_use
    AuthError.WeakPassword -> Res.string.auth_error_weak_password
    AuthError.InvalidEmail -> Res.string.auth_error_invalid_email
    AuthError.NetworkError -> Res.string.auth_error_network
    AuthError.TooManyAttempts -> Res.string.auth_error_too_many_attempts
    AuthError.RecentLoginRequired -> Res.string.auth_error_recent_login_required
    AuthError.TermsNotAccepted -> Res.string.auth_error_terms_required
    is AuthError.Unknown -> Res.string.auth_error_unknown
}
