---
name: masvs-privacy-audit
description: >
  Deep audit of privacy controls against OWASP MASVS-PRIVACY controls.
  Use when code touches permissions, analytics SDKs, user identifiers, consent flows, or data export/deletion.
---

# Privacy Audit (MASVS-PRIVACY)

> **Context**: See `.ai-context/security-instructions.md` for when to invoke this skill
> and the full MASVS coverage map for Dividox.

## Controls Assessed

| Control | Requirement |
|---------|-------------|
| MASVS-PRIVACY-1 | App collects only necessary data and resources |
| MASVS-PRIVACY-2 | App prevents user identification beyond app-specific needs |
| MASVS-PRIVACY-3 | App is transparent about data collection |
| MASVS-PRIVACY-4 | App provides users control over their data |

## Dividox Data Inventory

| Data type | Sensitivity | Purpose |
|---|---|---|
| Email address | High | Google Sign-In identity |
| Portfolio positions | High | Core feature |
| Watchlist tickers | Medium | Core feature |
| Dividend history | High | Core feature |
| Device analytics events | Low | App improvement |
| Crash reports | Low | Stability |

## Audit Scope

### MASVS-PRIVACY-1 — Data Minimization

| Check | Expected |
|---|---|
| Only permissions needed for core features declared | ✅ |
| No `CAMERA`, `MICROPHONE`, `CONTACTS` unless explicitly required | ✅ |
| Analytics events contain no PII (email, name, device fingerprint) | ✅ |
| Crash reports anonymized before upload | ✅ |
| No background location access | ✅ |

Android permissions to audit in `AndroidManifest.xml`:
- `INTERNET` — required ✅
- `ACCESS_NETWORK_STATE` — required ✅
- Flag any `READ_CONTACTS`, `READ_PHONE_STATE`, `ACCESS_FINE_LOCATION` as FAIL.

### MASVS-PRIVACY-2 — User Identification Prevention

| Check | Expected |
|---|---|
| No persistent hardware IDs (`ANDROID_ID` raw, `IMEI`, `MAC`) | ✅ FAIL if present |
| Advertising ID (`GAID`) only used with user consent | ✅ |
| No device fingerprinting across sessions | ✅ |
| Firebase `Installation ID` used instead of device ID | ✅ |

### MASVS-PRIVACY-3 — Transparency

| Check | Expected |
|---|---|
| Privacy policy accessible in-app (About / Settings screen) | ✅ |
| Data collection disclosed before collection begins | ✅ |
| Analytics SDK purpose disclosed in privacy policy | ✅ |
| Consent obtained before optional analytics | ⚠️ Warn if missing |

### MASVS-PRIVACY-4 — User Control

| Check | Expected |
|---|---|
| Account deletion removes all user data (DVX-TK-031 implemented) | ✅ |
| User can export portfolio data (DVX-TK-030) | ✅ |
| Analytics opt-out available in settings | ⚠️ Warn if missing |
| Data deletion request sent to backend on account delete | ✅ |

## Audit Steps

1. **Read `AndroidManifest.xml`** — list all permissions, flag dangerous ones.
2. **Search analytics SDK integrations** — Firebase Analytics, Crashlytics, etc.
3. **Check event payloads** — grep for analytics `logEvent` / `log` calls; verify no PII in parameters.
4. **Search for hardware IDs** — `ANDROID_ID`, `getDeviceId`, `getImei`, `macAddress`.
5. **Verify delete account flow** — confirm all local data purged + backend deletion triggered.
6. **Check in-app privacy policy link** — Settings → Privacy Policy screen exists.

## Output Format

```markdown
## MASVS-PRIVACY Audit Report

### Permission Inventory
| Permission | Declared | Justified | Risk |
|------------|----------|-----------|------|

### PRIVACY-1 — Data Minimization
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### PRIVACY-2 — User Identification
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### PRIVACY-3 — Transparency
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### PRIVACY-4 — User Control
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### GDPR / CCPA Implications
- {any notable regulatory exposure}

### Summary
- FAIL: {n} | WARN: {n} | PASS: {n}
- Verdict: APPROVED / BLOCKED
```

## MASTG Test Mappings

- MASTG-TEST-0005: Testing App Permissions (Android)
- MASTG-TEST-0006: Testing Whether Sensitive Data Is Sent to Third Parties (Android)
- MASTG-TEST-0057: Testing App Permissions (iOS)
