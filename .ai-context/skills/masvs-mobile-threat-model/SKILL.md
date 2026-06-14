---
name: masvs-mobile-threat-model
description: >
  Generate a STRIDE threat model for Dividox mapped to MASVS controls with NowSecure risk tiering.
  Use when designing new features with a significant security surface, or for periodic threat reviews.
---

# Mobile Threat Model (STRIDE + MASVS)

> **Context**: See `.ai-context/security-instructions.md` for Dividox security profile
> (Tier 2 fintech, NowSecure tiering).

## Purpose

Produce a `THREAT_MODEL.md` documenting the Dividox threat landscape using STRIDE methodology,
mapped to MASVS v2 controls, with NowSecure Tier 2 risk assessment.

## When to Use

- New feature with significant security surface (new auth flow, payment integration, data export)
- Quarterly security review
- Before external penetration test
- Onboarding a security partner

## Dividox Application Profile

| Attribute | Value |
|---|---|
| Platform | Android + iOS + Desktop (KMP) |
| Architecture | Compose Multiplatform, MVI, Clean Architecture |
| Data sensitivity | PII (email) + Financial (portfolio, dividends) |
| NowSecure tier | **Tier 2** |
| Backend | Firebase Auth + external market APIs (Yahoo Finance) |
| Auth | Google Sign-In (OAuth 2.0 / OIDC) |
| Storage | Android Keystore / iOS Keychain + Room/SQLite |

## Trust Boundaries

| Boundary | Description |
|---|---|
| App ↔ OS | Keystore/Keychain, permissions, IPC |
| App ↔ Network | Ktor/OkHttp → Yahoo Finance API, Firebase |
| App ↔ Backend | Firebase Auth, Firestore (if used) |
| App ↔ User | UI inputs, deep links, clipboard |
| App ↔ Third-party SDKs | Firebase Analytics, Crashlytics |

## STRIDE Analysis

### Spoofing
| Threat | MASVS Control | Risk | Mitigation |
|---|---|---|---|
| Fake app clone impersonates Dividox | RESILIENCE-2 | Medium | Play Integrity API, app signing |
| OAuth token stolen and replayed | AUTH-1 | High | PKCE, short-lived tokens, rotation |
| Phishing via deep link spoofing | PLATFORM-1 | Medium | App Links (verified domains only) |

### Tampering
| Threat | MASVS Control | Risk | Mitigation |
|---|---|---|---|
| Portfolio data modified in local DB | STORAGE-1 | Medium | SQLCipher encryption |
| APK repackaged with malicious code | RESILIENCE-2 | Low (Tier 2) | R8 obfuscation, Play Integrity |
| Man-in-the-middle modifies API response | NETWORK-1 | High | TLS 1.3, certificate pinning |

### Repudiation
| Threat | MASVS Control | Risk | Mitigation |
|---|---|---|---|
| User denies portfolio action | AUTH-3 | Low | Step-up auth + audit log |
| No trace of auth event | AUTH-1 | Medium | Firebase Auth event logging |

### Information Disclosure
| Threat | MASVS Control | Risk | Mitigation |
|---|---|---|---|
| Token leaked via logcat | STORAGE-2 | High | No PII/tokens in logs |
| Portfolio data in backup | STORAGE-2 | High | `android:allowBackup=false` |
| Sensitive data in task switcher | PLATFORM-3 | Medium | `FLAG_SECURE` on financial screens |
| PII in analytics events | PRIVACY-1 | High | Sanitize event parameters |
| API key hardcoded | CRYPTO-2 | Critical | BuildConfig injection + CI secrets |

### Denial of Service
| Threat | MASVS Control | Risk | Mitigation |
|---|---|---|---|
| API rate limiting exhausted | NETWORK-1 | Low | Retry with backoff |
| Storage filled with cache | CODE-4 | Low | Cache eviction policy |

### Elevation of Privilege
| Threat | MASVS Control | Risk | Mitigation |
|---|---|---|---|
| Exported Activity accessed by malicious app | PLATFORM-1 | High | `android:exported=false` |
| WebView JavaScript bridge exploited | PLATFORM-2 | Medium | Disable JS by default |
| Root/jailbreak bypasses Keystore | RESILIENCE-1 | Medium (Tier 3) | Play Integrity, graceful degradation |

## Output: `THREAT_MODEL.md`

The generated document must include:
1. Application profile and data flow diagram (Mermaid)
2. Asset inventory (tokens, portfolio data, PII)
3. Trust boundary map
4. STRIDE threat catalog (from table above + discovered threats)
5. Risk matrix (likelihood × impact)
6. MASVS control mapping per threat
7. Security requirements derived from threats
8. Residual risk after controls applied
9. Recommended penetration test areas

## NowSecure Tier 2 Minimum Controls

- STORAGE-1, STORAGE-2 ✅
- CRYPTO-1, CRYPTO-2 ✅
- AUTH-1, AUTH-2, AUTH-3 ✅
- NETWORK-1, NETWORK-2 ✅
- PLATFORM-1, PLATFORM-2, PLATFORM-3 ✅
- CODE-1 through CODE-4 ✅
- PRIVACY-1 through PRIVACY-4 ✅
- RESILIENCE-1 through RESILIENCE-4 ⚠️ Optional (Tier 3)
