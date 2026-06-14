# Security Instructions — Dividox MASVS Context

## App Security Profile

Dividox is a **fintech investment portfolio app** (stock watchlists, dividends, real-money data).
This places it at **NowSecure Tier 2 — Handles PII and Financial Data**.

Required controls: full STORAGE + CRYPTO + AUTH + NETWORK + PLATFORM + CODE + PRIVACY.
Optional (Tier 3 only): RESILIENCE (anti-tampering, root detection, obfuscation).

---

## When to Invoke Security Skills

Every agent (Developer, Code Reviewer) must consult the table below before implementing
or reviewing code. Invoke the matching skill for the changed area.

| Code area touched                                                        | Required skill                       | MASVS category |
|--------------------------------------------------------------------------|--------------------------------------|----------------|
| SharedPreferences, DataStore, SQLite, Room, files                        | `skill: secure-storage-audit`        | MASVS-STORAGE  |
| Encryption, key generation, Keystore/Keychain                            | `skill: crypto-review`               | MASVS-CRYPTO   |
| Login, token storage, session, biometrics, Google Sign-In                | `skill: auth-assessment`             | MASVS-AUTH     |
| OkHttp, Retrofit, Ktor, TLS config, certificates                         | `skill: network-security-check`      | MASVS-NETWORK  |
| Deep links, WebViews, Intents, Manifest exports                          | `skill: platform-interaction-review` | MASVS-PLATFORM |
| Dependencies (libs.versions.toml), minSdk, R8/ProGuard, input validation | `skill: code-quality-scan`           | MASVS-CODE     |
| Privacy policy, permissions, analytics SDKs, user identifiers            | `skill: privacy-audit`               | MASVS-PRIVACY  |
| Full MASVS compliance report for a release or milestone                  | `skill: masvs-checklist`             | All            |
| New architecture, data flows, threat surface changes                     | `skill: mobile-threat-model`         | All            |

`skill: owasp-security-review` is the **quick PR gate** (PASS/WARN/FAIL per control). Run it on
every PR that touches the areas above. The deeper skills above produce full audit reports.

---

## Agent Responsibilities

### Developer Agent
- Before implementing any data persistence, networking, or auth: consult this file.
- After implementing: run `skill: owasp-security-review` as part of the Verification phase.
- On auth, network, or storage features: run the matching deep-audit skill BEFORE committing.

### Code Reviewer Agent
- `skill: owasp-security-review` is **MANDATORY** for all PRs touching auth, network, storage.
- Run the matching deep-audit skill if the PR scope warrants a full audit.
- Add `[SECURITY-FAIL]` as a Critical Issue that blocks merge.
- Add `[SECURITY-WARN]` as an Improvement requiring justification.

### QA / PO Agent
- Include security acceptance criteria in every user story touching auth, payments, or user data.
- Reference `skill: masvs-checklist` when defining release readiness criteria.

---

## Dividox-Specific Security Rules

These are project-level decisions. Skills enforce them.

1. **No plaintext secrets in source** — API keys via `BuildConfig` (Android) / `xcconfig` (iOS). CI via GitHub Secrets.
2. **Token storage** — OAuth tokens stored in Android Keystore / iOS Keychain only. Never SharedPreferences or NSUserDefaults.
3. **Logging** — No user PII, portfolio data, or tokens in any log call (`Log.d/e/i`, `println`, `Timber`).
4. **TLS** — Ktor client must enforce TLS 1.2+. No `trustAll`. No hostname verifier override.
5. **Backup** — `android:allowBackup="false"` or explicit backup rules excluding sensitive data.
6. **R8** — Release builds must have R8 full-mode enabled. ProGuard rules must not expose sensitive model classes.
7. **minSdk 31** — API 31+ provides strong Keystore attestation. Do not lower minSdk without security review.

---

## MASVS v2 Coverage Map

| Category         | Controls                           | Skill                         | Tier required     |
|------------------|------------------------------------|-------------------------------|-------------------|
| MASVS-STORAGE    | STORAGE-1, STORAGE-2               | `secure-storage-audit`        | Tier 2            |
| MASVS-CRYPTO     | CRYPTO-1, CRYPTO-2                 | `crypto-review`               | Tier 2            |
| MASVS-AUTH       | AUTH-1, AUTH-2, AUTH-3             | `auth-assessment`             | Tier 2            |
| MASVS-NETWORK    | NETWORK-1, NETWORK-2               | `network-security-check`      | Tier 2            |
| MASVS-PLATFORM   | PLATFORM-1, PLATFORM-2, PLATFORM-3 | `platform-interaction-review` | Tier 2            |
| MASVS-CODE       | CODE-1, CODE-2, CODE-3, CODE-4     | `code-quality-scan`           | Tier 2            |
| MASVS-RESILIENCE | RESILIENCE-1 to 4                  | `resilience-assessment`       | Tier 3 (optional) |
| MASVS-PRIVACY    | PRIVACY-1 to 4                     | `privacy-audit`               | Tier 2            |

---

## References

- [OWASP MASVS v2](https://mas.owasp.org/MASVS/)
- [OWASP MASTG](https://mas.owasp.org/MASTG/)
- [NowSecure Secure Mobile Development](https://github.com/nowsecure/secure-mobile-development)
- [Android Security Best Practices](https://developer.android.com/privacy-and-security/security-tips)
- [Apple Platform Security](https://support.apple.com/guide/security/)
