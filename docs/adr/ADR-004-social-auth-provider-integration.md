# ADR-004: Social Auth Provider Integration (Google + Apple)

## Status
Proposed

## Context
The app must support Google Sign-In and Sign in with Apple alongside Email/Password. Both social providers require platform-native UI flows (consent screens, OS sheets) that cannot be fully abstracted in commonMain.

## Decision
Use **expect/actual** for the platform-specific launch of each social auth flow:

```kotlin
// commonMain
expect class GoogleSignInLauncher {
    suspend fun launch(): String // idToken
}

expect class AppleSignInLauncher {
    suspend fun launch(): AppleCredential
}
```

The resulting credential is passed to Firebase Auth in `AuthRepositoryImpl` (common code), which exchanges it for a Firebase session.

### Google Sign-In
- **Android**: `Credential Manager API` (Android 14+) with `GoogleSignInClient` fallback
- **iOS**: `GoogleSignIn-iOS` CocoaPods SDK via Kotlin/Native interop
- **Desktop**: Not supported in MVP — throws `UnsupportedOperationException`

### Sign in with Apple
- **iOS**: `AuthenticationServices.ASAuthorizationController` (native; **required** by App Store guidelines)
- **Android**: Firebase `OAuthProvider("apple.com")` via WebView redirect
- **Desktop**: Not supported in MVP — throws `UnsupportedOperationException`

## Consequences
- **Positive**: Native UX for each platform; App Store compliance for Sign in with Apple
- **Negative**: Significant platform-specific boilerplate; Google Sign-In on iOS requires CocoaPods config
- **Requirement**: Sign in with Apple is **mandatory on iOS** if any social login is offered (App Store Review Guideline 4.8)
- **Desktop MVP scope**: Email/Password only

## Related
- [ADR-001](ADR-001-firebase-authentication-backend.md): Firebase as Authentication Backend
- DVX-13: Google Sign-In Launchers
- DVX-14: Apple Sign-In Launchers
