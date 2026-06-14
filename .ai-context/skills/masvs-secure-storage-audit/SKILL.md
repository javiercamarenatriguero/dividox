---
name: masvs-secure-storage-audit
description: >
  Deep audit of data-at-rest security against OWASP MASVS-STORAGE controls.
  Use when code writes sensitive data to disk: SharedPreferences, DataStore, Room/SQLite, files, logs, clipboard, or backups.
---

# Secure Storage Audit (MASVS-STORAGE)

> **Context**: See `.ai-context/security-instructions.md` for when to invoke this skill
> and the full MASVS coverage map for Dividox.

## Controls Assessed

| Control | Requirement |
|---------|-------------|
| MASVS-STORAGE-1 | Intentionally stored sensitive data is protected |
| MASVS-STORAGE-2 | No sensitive data leaked via system APIs, logs, backups, or side channels |

## Audit Scope for Dividox (KMP)

Dividox stores: OAuth tokens, user profile data, portfolio positions, watchlist, dividend history.
All of the above are **sensitive** — apply STORAGE-1 and STORAGE-2.

### Android (`androidMain`)

| Storage mechanism | Sensitive data allowed? | Notes |
|---|---|---|
| `AndroidKeyStore` + `EncryptedSharedPreferences` | ✅ Yes | Only valid option for tokens |
| Plain `SharedPreferences` | ❌ No | Readable on rooted devices |
| `DataStore<Preferences>` unencrypted | ❌ No | Same risk as plain SharedPreferences |
| Room/SQLite with `SQLCipher` | ✅ Yes | Requires encryption key in Keystore |
| Room/SQLite unencrypted | ⚠️ Warn | OK for non-sensitive cached market data |
| External storage | ❌ No | World-readable |
| `Log.d/e/i` with user data | ❌ No | Strips to logcat — FAIL |
| `android:allowBackup` | Must be `false` or backup rules | Check AndroidManifest |

### iOS (`iosMain`)

| Storage mechanism | Sensitive data allowed? | Notes |
|---|---|---|
| Keychain with `kSecAttrAccessibleWhenUnlockedThisDeviceOnly` | ✅ Yes | Required for tokens |
| `NSUserDefaults` | ❌ No | Unencrypted plist |
| `UserDefaults` via Kotlin/Native | ❌ No | Same risk |
| Core Data without encryption | ⚠️ Warn | OK for non-sensitive data |
| `print()` / `NSLog()` with user data | ❌ No | FAIL |

### KMP Common (`commonMain`)

- `multiplatform-settings` library: verify actual platform implementation stores to Keychain/Keystore for sensitive keys.
- No logging of `SecurityModel`, `DividendInfo`, `PortfolioPosition`, or any class containing financial data.

## Audit Steps

1. **Identify sensitive data classes** — search for: `token`, `auth`, `password`, `email`, `portfolio`, `dividend`, `position`, `watchlist`.
2. **Trace storage paths** — follow each class to its persistence call.
3. **Verify Android Keystore / iOS Keychain usage** for token and credential storage.
4. **Scan all `Log.*`, `println`, `Timber.*` calls** — flag any that include sensitive field values.
5. **Check `AndroidManifest.xml`** for `allowBackup` and backup rules.
6. **Check `Info.plist`** for data protection entitlements.
7. **Verify clipboard access** — `ClipboardManager` / `UIPasteboard` must not copy sensitive data.

## Output Format

```markdown
## MASVS-STORAGE Audit Report

### STORAGE-1 — Secure Storage of Intentional Data
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### STORAGE-2 — No Unintentional Data Leakage
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### Summary
- FAIL: {n} | WARN: {n} | PASS: {n}
- Verdict: APPROVED / BLOCKED
```

## MASTG Test Mappings

- MASTG-TEST-0001: Testing Local Data Storage for Sensitive Data (Android)
- MASTG-TEST-0002: Testing Logs for Sensitive Data (Android)
- MASTG-TEST-0052: Testing Local Data Storage for Sensitive Data (iOS)
- MASTG-TEST-0053: Testing Logs for Sensitive Data (iOS)
