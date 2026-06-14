---
name: masvs-resilience-assessment
description: >
  Audit of anti-tampering and reverse engineering resilience against OWASP MASVS-RESILIENCE controls.
  Tier 3 only — optional for Dividox unless regulatory or contractual requirements apply.
  Use when implementing root/jailbreak detection, obfuscation, or anti-debug measures.
---

# Resilience Assessment (MASVS-RESILIENCE)

> **Context**: See `.ai-context/security-instructions.md` for when to invoke this skill
> and the full MASVS coverage map for Dividox.
>
> **Tier note**: Dividox is Tier 2. RESILIENCE controls are Tier 3 (optional).
> Implement only if required by financial regulations or app store policies.

## Controls Assessed

| Control | Requirement |
|---------|-------------|
| MASVS-RESILIENCE-1 | App validates platform integrity (root/jailbreak detection) |
| MASVS-RESILIENCE-2 | App detects and responds to tampering |
| MASVS-RESILIENCE-3 | App applies obfuscation to sensitive logic |
| MASVS-RESILIENCE-4 | App detects and responds to dynamic analysis tools |

## When to Implement for Dividox

Implement RESILIENCE controls if:
- Regulatory requirement (PCI-DSS, FFIEC) mandates it
- App store review requires it
- Users access real brokerage accounts through the app (Tier 3 trigger)

## Audit Scope

### MASVS-RESILIENCE-1 — Platform Integrity

| Check | Expected |
|---|---|
| Root detection (Android) — multiple signals: `su` binary, build tags, test keys | ✅ |
| Jailbreak detection (iOS) — Cydia, sandbox escape, dylib injection | ✅ |
| Detection distributed across multiple locations (not one `isRooted()` function) | ✅ |
| Detection failure triggers graceful degradation, not crash | ✅ |

Android: Consider `SafetyNet Attestation` (deprecated) → `Play Integrity API`.
iOS: No system API — implement heuristics or use `DCAppAttestService`.

### MASVS-RESILIENCE-2 — Anti-Tampering

| Check | Expected |
|---|---|
| APK signature verified at runtime | ✅ |
| Dex integrity check implemented | ⚠️ Optional |
| Play Integrity API `appIntegrity.appRecognitionVerdict` checked | ✅ |
| Tampering detected → session terminated | ✅ |

### MASVS-RESILIENCE-3 — Obfuscation

| Check | Expected |
|---|---|
| R8 full mode enabled in release builds | ✅ |
| Sensitive class names obfuscated (not kept via `-keep`) | ✅ |
| No overly broad `-keep class com.akole.dividox.**` rules | ✅ |
| Native code (if any) compiled with symbol stripping | ✅ |

### MASVS-RESILIENCE-4 — Anti-Debug / Anti-Instrumentation

| Check | Expected |
|---|---|
| `android:debuggable="false"` in release manifest | ✅ |
| Debugger detection via `Debug.isDebuggerConnected()` | ⚠️ Optional |
| Emulator detection heuristics | ⚠️ Optional |
| Frida detection (for high-value apps) | ⚠️ Tier 3 only |

## Audit Steps

1. **Check R8 config** — verify `minifyEnabled = true` and `shrinkResources = true` in release.
2. **Review ProGuard rules** — flag overly broad `-keep` rules.
3. **Check release manifest** — `debuggable="false"`, `allowBackup="false"`.
4. **Search for integrity checks** — `PackageManager.getPackageInfo`, `Play Integrity API`, `DCAppAttestService`.
5. **Verify detection responses** — detection must not be a single point of failure.

## Output Format

```markdown
## MASVS-RESILIENCE Audit Report

### Hardening Posture: None / Basic / Moderate / Advanced

### RESILIENCE-1 — Platform Integrity
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### RESILIENCE-2 — Anti-Tampering
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### RESILIENCE-3 — Obfuscation
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### RESILIENCE-4 — Anti-Debug
- [FAIL/WARN/PASS] {finding} — {file}:{line}

### Summary
- FAIL: {n} | WARN: {n} | PASS: {n}
- Verdict: APPROVED / BLOCKED (Tier 3 only if controls required)
```

## MASTG Test Mappings

- MASTG-TEST-0041: Testing Root Detection (Android)
- MASTG-TEST-0042: Testing Anti-Debugging (Android)
- MASTG-TEST-0043: Testing File Integrity Checks (Android)
- MASTG-TEST-0082: Testing Jailbreak Detection (iOS)
