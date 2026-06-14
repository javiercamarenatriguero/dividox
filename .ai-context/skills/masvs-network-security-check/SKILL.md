---
name: masvs-network-security-check
description: >
  Deep audit of network communication security against OWASP MASVS-NETWORK controls.
  Use when code configures HTTP clients (Ktor, OkHttp), touches TLS, certificates, or network_security_config.xml.
---

# Network Security Check (MASVS-NETWORK)

> **Context**: See `.ai-context/security-instructions.md` for when to invoke this skill
> and the full MASVS coverage map for Dividox.

## Controls Assessed

| Control | Requirement |
|---------|-------------|
| MASVS-NETWORK-1 | All network traffic uses properly configured TLS |
| MASVS-NETWORK-2 | Certificate pinning or equivalent for developer-controlled endpoints |

## Dividox Network Stack

- **KMP shared**: Ktor client configured in `commonMain` with platform-specific engine actuals
- **Android engine**: OkHttp (`androidMain`)
- **iOS engine**: Darwin (`iosMain`)
- **API endpoints**: Yahoo Finance, internal Dividox backend (if any)

## Audit Scope

### MASVS-NETWORK-1 — TLS Configuration

#### Android (`androidMain` + `res/xml/network_security_config.xml`)

| Check | Expected |
|---|---|
| No `cleartextTrafficPermitted="true"` in NSC | ✅ |
| No `<domain-config cleartextTrafficPermitted="true">` for prod domains | ✅ |
| No custom `TrustManager` that accepts all certificates | ✅ FAIL if present |
| No `HostnameVerifier` that returns `true` unconditionally | ✅ FAIL if present |
| `OkHttpClient` does not set `sslSocketFactory` with weak config | ✅ |
| `usesCleartextTraffic` in `AndroidManifest.xml` is `false` or absent | ✅ |

#### iOS (`iosMain` + `Info.plist`)

| Check | Expected |
|---|---|
| No `NSAllowsArbitraryLoads: true` in ATS config | ✅ FAIL if present |
| No `NSExceptionAllowsInsecureHTTPLoads` for prod domains | ✅ |
| `URLSession` delegate does not bypass `ServerTrust` evaluation | ✅ |

#### KMP Common (`commonMain`)

| Check | Expected |
|---|---|
| Ktor `HttpClient` does not disable SSL validation | ✅ |
| No `HttpClient { engine { ... } }` config that disables certificate checks | ✅ |
| All API base URLs use `https://` | ✅ |
| No sensitive data in URL path or query parameters | ✅ (use headers/body) |

### MASVS-NETWORK-2 — Certificate Pinning

| Check | Expected |
|---|---|
| Pinning configured for all developer-controlled API domains | ✅ |
| Pins are public key hashes (not full cert — rotatable) | ✅ |
| Backup pins provided (min 2) | ✅ |
| Pinning failures log a warning and fail the request | ✅ |
| No pinning bypass in debug builds without explicit flag | ⚠️ Warn |

**Dividox note**: Yahoo Finance is a third-party API — pinning is optional (cannot control their cert rotation).
Pinning is required for any first-party Dividox backend endpoints.

## Audit Steps

1. **Locate Ktor client config** — search `HttpClient`, `install(`, `engine {`.
2. **Check Android NSC** — read `res/xml/network_security_config.xml`.
3. **Check iOS ATS** — read `iosApp/iosApp/Info.plist`.
4. **Search for TLS bypasses** — `trustAll`, `ALLOW_ALL_HOSTNAME_VERIFIER`, `checkServerTrusted`.
5. **Verify all URLs** — grep for `http://` in network code (non-localhost).
6. **Check pinning implementation** — find `CertificatePinner` (OkHttp) or `TrustKit` (iOS) setup.

## Output Format

```markdown
## MASVS-NETWORK Audit Report

### NETWORK-1 — TLS Configuration
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### NETWORK-2 — Certificate Pinning
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### Cleartext Inventory
| URL | Context | Risk |
|-----|---------|------|

### Summary
- FAIL: {n} | WARN: {n} | PASS: {n}
- Verdict: APPROVED / BLOCKED
```

## MASTG Test Mappings

- MASTG-TEST-0020: Testing the TLS Settings (Android)
- MASTG-TEST-0021: Testing Endpoint Identity Verification (Android)
- MASTG-TEST-0022: Testing Custom Certificate Stores and Certificate Pinning (Android)
- MASTG-TEST-0066: Testing the TLS Settings (iOS)
- MASTG-TEST-0067: Testing Custom Certificate Stores and Certificate Pinning (iOS)
