---
name: masvs-platform-interaction-review
description: >
  Deep audit of platform interaction security against OWASP MASVS-PLATFORM controls.
  Use when code touches deep links, Intents, exported components, WebViews, or AndroidManifest/Info.plist.
---

# Platform Interaction Review (MASVS-PLATFORM)

> **Context**: See `.ai-context/security-instructions.md` for when to invoke this skill
> and the full MASVS coverage map for Dividox.

## Controls Assessed

| Control | Requirement |
|---------|-------------|
| MASVS-PLATFORM-1 | App uses IPC mechanisms securely |
| MASVS-PLATFORM-2 | App uses WebViews securely |
| MASVS-PLATFORM-3 | App uses UI components securely (overlays, screenshots, task switcher) |

## Dividox Platform Surface

- **Deep links**: stock detail navigation (`dividox://security/{ticker}`)
- **Intent filters**: any exported Activity
- **WebViews**: news feed, external URLs (if any)
- **Screen content**: portfolio values, dividend amounts — sensitive in task switcher

## Audit Scope

### MASVS-PLATFORM-1 — IPC Mechanisms

#### Android (`androidMain` + `AndroidManifest.xml`)

| Check | Expected |
|---|---|
| All Activities/Services/Receivers set `android:exported="false"` unless required | ✅ |
| Exported components validate callers with permissions | ✅ |
| Deep link parameters validated and sanitized before use | ✅ |
| PendingIntents use `FLAG_IMMUTABLE` | ✅ |
| No implicit Intents for sensitive operations | ✅ |

#### iOS (`iosMain` + `Info.plist`)

| Check | Expected |
|---|---|
| URL schemes validated — no open redirects | ✅ |
| Universal Links (AASA) used instead of custom schemes for sensitive flows | ✅ |
| App Extension data sharing scoped to necessary data only | ✅ |

### MASVS-PLATFORM-2 — WebView Security

| Check | Expected |
|---|---|
| `setJavaScriptEnabled(false)` unless explicitly required | ✅ |
| No `addJavascriptInterface` unless necessary and receiver validated | ✅ |
| `setAllowFileAccess(false)` | ✅ |
| `setAllowUniversalAccessFromFileURLs(false)` | ✅ |
| WebView does not load arbitrary user-provided URLs | ✅ |
| SSL errors not silently ignored in `onReceivedSslError` | ✅ FAIL if `handler.proceed()` unconditional |
| iOS `WKWebView` used (not deprecated `UIWebView`) | ✅ |

### MASVS-PLATFORM-3 — UI Security

| Check | Expected |
|---|---|
| `FLAG_SECURE` set on Activities showing portfolio/financial data (Android) | ✅ |
| Task switcher snapshot obscured for screens with sensitive data | ✅ |
| iOS `applicationWillResignActive` sets blur overlay on sensitive screens | ✅ |
| No clipboard access to copy portfolio values without user intent | ⚠️ Warn |

## Audit Steps

1. **Read `AndroidManifest.xml`** — check all exported components and intent filters.
2. **Search deep link handlers** — find `NavDeepLink`, `handleDeepLink`, `onNewIntent`.
3. **Search WebView usage** — grep `WebView`, `loadUrl`, `WKWebView`, `evaluateJavaScript`.
4. **Check `FLAG_SECURE`** — search in `MainActivity` and screen-level Window config.
5. **Review iOS `applicationWillResignActive`** — check for screen obscuring.

## Output Format

```markdown
## MASVS-PLATFORM Audit Report

### PLATFORM-1 — IPC & Deep Links
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### PLATFORM-2 — WebView Security
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### PLATFORM-3 — UI Security
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### Exported Component Inventory
| Component | Exported | Permission | Risk |
|-----------|----------|------------|------|

### Summary
- FAIL: {n} | WARN: {n} | PASS: {n}
- Verdict: APPROVED / BLOCKED
```

## MASTG Test Mappings

- MASTG-TEST-0028: Testing Deep Links (Android)
- MASTG-TEST-0031: Testing JavaScript Execution in WebViews (Android)
- MASTG-TEST-0035: Testing Object Persistence (Android)
- MASTG-TEST-0075: Testing Custom URL Schemes (iOS)
- MASTG-TEST-0078: Testing UIActivity Sharing (iOS)
