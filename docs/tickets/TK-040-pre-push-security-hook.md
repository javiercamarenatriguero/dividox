# TK-040 — CI/CD: Pre-Push Security Hook + Node.js Deprecation Fix

**Type:** Technical Task
**Layer:** CI/CD + Developer Experience
**Points:** 2
**Priority:** Medium
**Branch:** `feature/DVX-TK-040-security-hooks-ci`

## Description

Add a pre-push advisory hook that runs secret scanning locally and reminds developers
to invoke the `owasp-security-review` skill before pushing security-sensitive changes.
The hook lives in `.ai-context/scripts/` (shared by Claude Code and GitHub Copilot via symlinks).

Additionally, fix two CI warnings:
1. **Node.js 20 deprecation** — upgrade all GitHub Actions to latest stable versions.
2. **Detekt artifact path** — suppress "no files found" warning on report upload.

## Tasks

### Phase 1: Security hook

- [ ] Create `.ai-context/scripts/pre-push-security.sh`:
  - Runs `gitleaks detect --no-git` (warn only, exit 0 — advisory, never blocks push)
  - Prints reminder to run `owasp-security-review` skill for auth/network/storage changes
- [ ] Symlink `.claude/scripts/pre-push-security.sh` → `../../.ai-context/scripts/pre-push-security.sh`
- [ ] Register `PostToolUse` hook in `.claude/settings.json` matching `git push` commands

### Phase 2: GitHub Actions upgrade (Node.js 20 → 24)

- [ ] Upgrade across all 5 workflow files:
  - `actions/checkout@v4` → `@v7`
  - `actions/setup-java@v4` → `@v5`
  - `actions/upload-artifact@v4` → `@v7`
  - `actions/download-artifact@v4` → `@v8`
  - `gradle/actions/setup-gradle@v4` → `@v6`

### Phase 3: Fix detekt artifact warning

- [ ] Add `if_no_files_found: ignore` to detekt upload steps in `on-pull-request.yml` and `on-merge.yml`

## Acceptance Criteria

- [ ] Pre-push hook runs `gitleaks` scan and prints advisory (never blocks push)
- [ ] Hook script lives in `.ai-context/scripts/` with symlink in `.claude/scripts/`
- [ ] No Node.js 20 deprecation warnings in GitHub Actions
- [ ] No "No files found" warning for detekt artifact upload
- [ ] No regressions in existing CI workflows

## Related

- TK-038 (CI security job)
- TK-039 (Detekt security ruleset)
- Issue #75
