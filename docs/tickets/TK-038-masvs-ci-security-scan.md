# Task: TK-038 · CI/CD — Automated Security Gate (MASVS-CODE + Secrets)

## Description

Add a dedicated **security** job to the PR workflow with two automated checks:
1. **Detekt security profile** — covers **MASVS-CODE only** (unsafe API usage, coroutines misuse,
   potential bugs). Cannot audit STORAGE, CRYPTO, AUTH, NETWORK, PLATFORM, or PRIVACY.
2. **gitleaks secret scan** — detects hardcoded API keys and credentials (MASVS-CRYPTO-2 partial).

**Full MASVS coverage** (STORAGE, CRYPTO, AUTH, NETWORK, PLATFORM, PRIVACY, RESILIENCE)
is provided by AI-assisted skills (`masvs-*`) in `.claude/skills/`, not by this CI job.
This job is a fast automated safety net; the skills are the comprehensive review layer.

**Goal:** Every PR gets an automated MASVS-CODE + secrets gate before merge. Hard fail blocks merge.

**Depends on:** TK-039 (`detekt-security.yml` must exist first)

**Status:** Backlog

---

### Phase 1: Branch setup

- [ ] Branch: `feature/DVX-TK-038-masvs-security-tooling` (already created)

### Phase 2: Add security job to on-pull-request.yml

- [ ] Add `security` job in `.github/workflows/on-pull-request.yml`:
  - Trigger: `pull_request` to `main` / `develop`
  - Permissions: `security-events: write`, `contents: read`
  - Steps: checkout → JDK 17 → Gradle setup
  - Run: `./gradlew detekt --config config/detekt/detekt-security.yml`
  - Upload SARIF via `github/codeql-action/upload-sarif@v3`
  - **Commit:** `DVX-TK-038 Add MASVS-CODE security scan job to PR workflow`

### Phase 3: Add gitleaks secret scanning step

- [ ] Add `gitleaks/gitleaks-action@v2` step to the security job:
  - Fail job on any hardcoded secret or API key found
  - **Commit:** `DVX-TK-038 Add gitleaks secret scanning to security job`

### Phase 4: Verification

- [ ] Push branch — verify security job appears on PR
- [ ] SARIF report visible in GitHub Security → Code Scanning tab
- [ ] `./gradlew detekt --config config/detekt/detekt-security.yml` passes locally
- [ ] gitleaks finds no secrets in current codebase

## Progress Tracking

**Total Tasks:** 4 · **Completed:** 0 · **Remaining:** 4

**Scope boundary (important):**
- ✅ This job covers: MASVS-CODE (Detekt) + secrets (gitleaks)
- ❌ This job does NOT cover: MASVS-STORAGE, MASVS-CRYPTO, MASVS-AUTH, MASVS-NETWORK, MASVS-PLATFORM, MASVS-PRIVACY, MASVS-RESILIENCE
- 🤖 Full MASVS coverage: use `masvs-*` AI skills per `.ai-context/security-instructions.md`

Technical notes:
- SARIF upload requires `security-events: write` job permission
- Do NOT reuse the lint job Gradle cache key for the security job
- Detekt SARIF path: `'**/build/reports/detekt/*.sarif'`
