# Task: DVX-TK-042 — Map Authentication Error Messages to User-Friendly Text

## Problem

When authentication fails (wrong password, invalid email, network error, etc.), the app displays
raw HTTP status codes or exception messages (e.g. `FirebaseAuthInvalidCredentialsException`,
`HTTP 401 Unauthorized`). Users see technical gibberish instead of helpful guidance.

## Affected Files

- `feature/auth/src/commonMain/.../LoginViewModel.kt` — uses `it.message` directly from exception
- `feature/auth/src/commonMain/.../ForgotPasswordViewModel.kt` — same pattern
- `feature/auth/src/commonMain/.../SignUpScreen.kt` — same pattern
- `common/ui-resources/src/commonMain/composeResources/values/strings.xml` — needs new string entries

## Current Behavior

```kotlin
// LoginViewModel.kt
.onFailure { updateViewState { copy(isLoading = false, error = it.message) } }
```

`it.message` passes raw exception text to UI — no mapping, no localization.

## Expected Behavior

- Map known exception types to user-friendly messages from `strings.xml`
- Fallback to a generic "Something went wrong. Please try again." for unknown errors
- Common cases to handle:
  - Invalid credentials → "Incorrect email or password"
  - Account not found → "No account found with this email"
  - Email already in use → "An account with this email already exists"
  - Weak password → "Password must be at least 6 characters"
  - Network error → "No internet connection. Check your network"
  - Too many attempts → "Too many failed attempts. Try again later"
  - Invalid email format → "Please enter a valid email address"

## Implementation Notes

- Create an `AuthErrorMapper` in `feature/auth` that maps exception types/messages to string resource keys
- All error strings must go in `strings.xml` per project convention
- Consider Firebase Auth error codes if using Firebase (check `component/auth/`)
- Keep the mapper testable — write unit tests for each mapping

## Priority

**High** — directly impacts user experience on first interaction with the app.

## Labels

`bug`, `ux`, `auth`
