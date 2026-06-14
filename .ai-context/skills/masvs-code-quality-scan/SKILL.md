---
name: masvs-code-quality-scan
description: >
  Deep audit of code quality and supply chain security against OWASP MASVS-CODE controls.
  Use when modifying libs.versions.toml dependencies, minSdk, R8/ProGuard rules, or input validation logic.
  This is the primary skill for Detekt-detectable issues.
---

# Code Quality & Security Scan (MASVS-CODE)

> **Context**: See `.ai-context/security-instructions.md` for when to invoke this skill
> and the full MASVS coverage map for Dividox.
>
> **Detekt note**: `./gradlew detekt` covers parts of MASVS-CODE (unsafe calls, code patterns).
> This skill covers the full control set including dependency auditing and build config that Detekt cannot assess.

## Controls Assessed

| Control | Requirement |
|---------|-------------|
| MASVS-CODE-1 | App targets current, supported platform versions |
| MASVS-CODE-2 | App has an update mechanism for security patches |
| MASVS-CODE-3 | App uses no vulnerable or unmaintained third-party dependencies |
| MASVS-CODE-4 | App validates and sanitizes all external input |

## Audit Scope for Dividox

### MASVS-CODE-1 — Platform Version Requirements

| Check | Expected | Current |
|---|---|---|
| `minSdk` | ≥ 31 (Android 12) | 31 ✅ |
| `targetSdk` | Latest stable | 36 ✅ |
| iOS deployment target | ≥ iOS 16 | Check `iosApp` |
| Kotlin version | ≥ 2.x | 2.3.20 ✅ |
| AGP version | ≥ 9.x | 9.1.0 ✅ |

### MASVS-CODE-2 — Update Mechanism

| Check | Expected |
|---|---|
| In-app update flow implemented (Android `AppUpdateManager`) | ⚠️ Warn if missing |
| Minimum version enforcement on app startup | ⚠️ Warn if missing |
| Firebase Remote Config for kill-switch / forced update flag | Recommended |

### MASVS-CODE-3 — Third-Party Dependencies

Audit `gradle/libs.versions.toml` for:

| Category | Check |
|---|---|
| Outdated versions | Compare against Maven Central latest |
| Known CVEs | Search OSS Index / GitHub Advisory DB |
| Abandoned libraries | Last commit > 2 years, no maintainer |
| Transitive deps | Check for vulnerable transitive pulls |
| No debug-only libs in release | `debugImplementation` scope correct |

Key Dividox dependencies to verify: Ktor, OkHttp, Firebase, Koin, Room/SQLDelight, Compose.

### MASVS-CODE-4 — Input Validation

| Input source | Check |
|---|---|
| Deep link parameters | Validated and typed before use |
| API response data | No direct deserialization into DB without validation |
| User search input | Sanitized before passing to queries |
| Stock ticker symbols | Regex-validated (alphanumeric only) |
| External URLs in WebViews | Allowlist-based validation |

## Detekt-Detectable Issues (MASVS-CODE overlap)

The following are caught by `./gradlew detekt` with the security ruleset:

- `UnsafeCallOnNullableType` (`!!`) in non-test code
- `LateinitUsage` in public APIs
- `GlobalCoroutineUsage` (`GlobalScope.launch`)
- `Log.d/e/i` with sensitive parameter patterns (custom rule)

Run `./gradlew detekt` first; this skill covers what Detekt cannot.

## Audit Steps

1. **Check `libs.versions.toml`** — identify all direct dependencies and their versions.
2. **Cross-reference against CVE databases** — use `./gradlew dependencyUpdates` if available.
3. **Verify build config** — `minSdk`, `targetSdk`, R8 rules in `build-logic/convention/`.
4. **Review ProGuard/R8 rules** — ensure release build does not expose sensitive model classes.
5. **Audit input validation** — trace all external inputs (deep links, API, user forms).
6. **Check debug-only dependencies** — no `debugImplementation` leaks to release.

## Output Format

```markdown
## MASVS-CODE Audit Report

### CODE-1 — Platform Versions
- [FAIL/WARN/PASS] {finding}

### CODE-2 — Update Mechanism
- [FAIL/WARN/PASS] {finding}

### CODE-3 — Dependencies
| Library | Version | Latest | CVE | Status |
|---------|---------|--------|-----|--------|

### CODE-4 — Input Validation
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### Summary
- FAIL: {n} | WARN: {n} | PASS: {n}
- Verdict: APPROVED / BLOCKED
```

## MASTG Test Mappings

- MASTG-TEST-0005: Testing App Permissions (Android)
- MASTG-TEST-0039: Checking for Weaknesses in Third Party Libraries (Android)
- MASTG-TEST-0041: Testing Enforced Updating (Android)
