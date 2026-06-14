---
name: masvs-checklist
description: >
  Generate a full OWASP MASVS v2 compliance checklist for Dividox with MASTG test mappings.
  Use before a release, for a compliance review, or when assessing overall security posture.
---

# MASVS Checklist (Full Compliance)

> **Context**: See `.ai-context/security-instructions.md` for Dividox security profile
> (Tier 2 fintech — all controls except RESILIENCE are required).

## Purpose

Generate a `MASVS_CHECKLIST.md` for the current state of the Dividox codebase, covering
all 8 MASVS v2 categories with per-control MASTG test mappings and compliance status.

## When to Use

- Before any release to production
- When onboarding a new security reviewer
- When assessing gap between current state and full MASVS compliance
- After a significant architectural change (new auth flow, new storage, new network layer)

## Procedure

For each category below, invoke the corresponding deep-audit skill to populate findings,
then aggregate into the compliance matrix.

| Step | Action | Skill |
|------|--------|-------|
| 1 | Storage audit | `skill: masvs-secure-storage-audit` |
| 2 | Crypto audit | `skill: masvs-crypto-review` |
| 3 | Auth audit | `skill: masvs-auth-assessment` |
| 4 | Network audit | `skill: masvs-network-security-check` |
| 5 | Platform audit | `skill: masvs-platform-interaction-review` |
| 6 | Code quality audit | `skill: masvs-code-quality-scan` |
| 7 | Privacy audit | `skill: masvs-privacy-audit` |
| 8 | Resilience (optional) | `skill: masvs-resilience-assessment` |

## Output: `MASVS_CHECKLIST.md`

```markdown
# MASVS v2 Compliance Checklist — Dividox
**Date**: {date}
**App version**: {version}
**Tier**: 2 (Handles PII and Financial Data)

## Compliance Matrix

| Category | Control | Status | MASTG Test | Notes |
|----------|---------|--------|------------|-------|
| MASVS-STORAGE | STORAGE-1 | ✅ PASS / ⚠️ WARN / ❌ FAIL | MASTG-TEST-0001 | |
| MASVS-STORAGE | STORAGE-2 | | MASTG-TEST-0002 | |
| MASVS-CRYPTO | CRYPTO-1 | | MASTG-TEST-0013 | |
| MASVS-CRYPTO | CRYPTO-2 | | MASTG-TEST-0014 | |
| MASVS-AUTH | AUTH-1 | | MASTG-TEST-0068 | |
| MASVS-AUTH | AUTH-2 | | MASTG-TEST-0016 | |
| MASVS-AUTH | AUTH-3 | | MASTG-TEST-0018 | |
| MASVS-NETWORK | NETWORK-1 | | MASTG-TEST-0020 | |
| MASVS-NETWORK | NETWORK-2 | | MASTG-TEST-0022 | |
| MASVS-PLATFORM | PLATFORM-1 | | MASTG-TEST-0028 | |
| MASVS-PLATFORM | PLATFORM-2 | | MASTG-TEST-0031 | |
| MASVS-PLATFORM | PLATFORM-3 | | MASTG-TEST-0035 | |
| MASVS-CODE | CODE-1 | | MASTG-TEST-0041 | |
| MASVS-CODE | CODE-2 | | MASTG-TEST-0041 | |
| MASVS-CODE | CODE-3 | | MASTG-TEST-0039 | |
| MASVS-CODE | CODE-4 | | MASTG-TEST-0005 | |
| MASVS-PRIVACY | PRIVACY-1 | | MASTG-TEST-0005 | |
| MASVS-PRIVACY | PRIVACY-2 | | MASTG-TEST-0006 | |
| MASVS-PRIVACY | PRIVACY-3 | | MASTG-TEST-0057 | |
| MASVS-PRIVACY | PRIVACY-4 | | MASTG-TEST-0057 | |

## Summary
- ✅ PASS: {n}
- ⚠️ WARN: {n}  
- ❌ FAIL: {n}
- Overall verdict: COMPLIANT / NON-COMPLIANT

## Remediation Roadmap (by priority)
1. [FAIL items — must fix before release]
2. [WARN items — fix within next sprint]
3. [Recommendations for Tier 3 upgrade]
```

## Important

This checklist is **static analysis only**. Dynamic testing (Frida instrumentation,
proxy traffic inspection, device-level testing) is needed for full MASVS certification.
