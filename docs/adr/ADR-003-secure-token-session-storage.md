# ADR-003: Secure Token and Session Storage Strategy

## Status
Accepted

## Context
Firebase Auth SDKs handle token refresh automatically on Android and iOS. However, the app needs a way to:
1. Observe auth state changes across the app (navigation guard)
2. Persist session across cold starts
3. Handle token storage on Desktop JVM (where Firebase SDK is not available)

## Decision
**Delegate session persistence to the Firebase SDK on Android and iOS.** Do not re-implement token storage manually. Use `FirebaseAuth.currentUser` / `authStateDidChange` for observability.

For **Desktop JVM**: store the Firebase ID token in an encrypted file using `java.security.KeyStore` (AES/GCM). Refresh token via REST `securetoken.googleapis.com/v1/token`.

Expose auth state to the app via `ObserveAuthStateUseCase` which returns `Flow<AuthUser?>`:
- `null` → unauthenticated → navigate to Login
- `AuthUser` → authenticated → navigate to Home

## Alternatives Considered

### Custom encrypted SharedPreferences (Android only)
- Redundant; Firebase already manages this

### Multiplatform Settings (russhwolf/multiplatform-settings)
- Suitable for non-sensitive preferences; **not appropriate for auth tokens** (stored in plain SharedPreferences on Android)

### SQLDelight with encryption
- Overkill for a session token

## Consequences
- **Positive**: Zero manual token management on Android/iOS; secure by default; Firebase handles rotation
- **Negative**: Desktop requires custom implementation; coupling to Firebase session model
- **Security note**: Desktop keystore implementation must be reviewed before production release (OWASP MASVS-STORAGE-1)

## Implementation Notes
- Platform `AuthDataSource` actuals decide storage strategy
- `ObserveAuthStateUseCase` wraps platform callback as `callbackFlow`
- Navigation guard in `RootNavGraph` subscribes to this flow

## Related
- [ADR-002](ADR-002-clean-architecture-auth-module-split.md): Module Split
- DVX-12: Auth Data Layer
- DVX-17: Session Guard in RootNavGraph
