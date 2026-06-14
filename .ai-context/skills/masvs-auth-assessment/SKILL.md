---
name: masvs-auth-assessment
description: >
  Deep audit of authentication and session management against OWASP MASVS-AUTH controls.
  Use when code touches login, logout, token storage, token refresh, biometrics, or Google Sign-In.
---

# Authentication & Authorization Assessment (MASVS-AUTH)

> **Context**: See `.ai-context/security-instructions.md` for when to invoke this skill
> and the full MASVS coverage map for Dividox.

## Controls Assessed

| Control | Requirement |
|---------|-------------|
| MASVS-AUTH-1 | Remote authentication follows current best practices |
| MASVS-AUTH-2 | Sensitive local operations protected by local authentication |
| MASVS-AUTH-3 | App enforces step-up authentication for sensitive actions |

## Dividox Auth Architecture

- **Remote auth**: Google Sign-In (OAuth 2.0 + OIDC)
- **Token storage**: ID token + refresh token must be in Android Keystore / iOS Keychain
- **Session**: Managed via Firebase Auth or custom token refresh logic
- **Sensitive actions** (Tier 2): exporting portfolio, deleting account, changing settings

## Audit Scope

### MASVS-AUTH-1 — Remote Authentication

| Check | Expected |
|---|---|
| OAuth 2.0 PKCE used for authorization code flow | ✅ |
| `state` parameter validated to prevent CSRF | ✅ |
| `nonce` included and validated for OIDC | ✅ |
| ID token validated (signature, expiry, audience) | ✅ |
| Refresh token stored in Keystore/Keychain, not SharedPreferences | ✅ |
| Refresh token rotation on each use | ✅ |
| Token expiry respected — no indefinite sessions | ✅ |
| Logout clears ALL tokens (local + server-side revocation) | ✅ |

### MASVS-AUTH-2 — Local Authentication

| Check | Expected |
|---|---|
| Biometric uses `BiometricPrompt` (Android) / `LAContext` (iOS) | ✅ |
| Biometric result bound to a Keystore-protected key operation | ✅ |
| No custom biometric implementations | ✅ |
| No biometric bypass via backup PIN/password that skips key unwrap | ⚠️ Warn |

### MASVS-AUTH-3 — Step-Up Authentication

| Check | Expected |
|---|---|
| Delete account requires re-authentication | ✅ |
| Export portfolio requires re-authentication | ✅ |
| Step-up triggers fresh auth (not just PIN check) | ✅ |

## Audit Steps

1. **Trace the full auth flow** — from `GoogleSignIn` / `FirebaseAuth` call to token persistence.
2. **Verify token storage** — search `SharedPreferences`, `DataStore`, `UserDefaults` for any token key.
3. **Check token refresh logic** — verify expiry is checked before each API call.
4. **Check logout** — verify all tokens removed from storage AND server-side revocation called.
5. **Review biometric integration** — confirm `BiometricPrompt`/`LAContext` usage.
6. **Check step-up flows** — verify delete account and export require re-auth.

## Output Format

```markdown
## MASVS-AUTH Audit Report

### AUTH-1 — Remote Authentication
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### AUTH-2 — Local Authentication
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### AUTH-3 — Step-Up Authentication
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### Summary
- FAIL: {n} | WARN: {n} | PASS: {n}
- Verdict: APPROVED / BLOCKED
```

## MASTG Test Mappings

- MASTG-TEST-0016: Testing Confirm Credentials (Android)
- MASTG-TEST-0018: Testing Biometric Authentication (Android)
- MASTG-TEST-0063: Testing Local Authentication (iOS)
- MASTG-TEST-0068: Testing OAuth 2.0 Flows (Android + iOS)
