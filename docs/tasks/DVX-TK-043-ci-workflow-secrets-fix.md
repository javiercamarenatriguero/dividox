# Task: DVX-TK-043 — Fix CI Workflow File Errors and Secret Mismatches

## Problem

`on-distribute.yml` fails on every run with: *"This run likely failed because of a workflow file issue."*
Additionally, desktop distribution secrets reference names that don't exist in the repository.

## Secret Name Mismatch

The workflows reference `FIREBASE_DESKTOP_*` secrets, but the repo only has non-prefixed versions:

| Referenced in YAML | Actual repo secret |
|---|---|
| `FIREBASE_DESKTOP_APPLICATION_ID` | `FIREBASE_APPLICATION_ID` |
| `FIREBASE_DESKTOP_API_KEY` | `FIREBASE_API_KEY` |
| `FIREBASE_DESKTOP_PROJECT_ID` | `FIREBASE_PROJECT_ID` |
| `FIREBASE_DESKTOP_GCM_SENDER_ID` | `FIREBASE_GCM_SENDER_ID` |
| `FIREBASE_DESKTOP_STORAGE_BUCKET` | `FIREBASE_STORAGE_BUCKET` |

Affected files:
- `.github/workflows/on-distribute.yml` (lines 149-153)
- `.github/workflows/on-merge.yml` (lines ~148-152)

## Resolution Options

**Option A** — Rename secrets in workflow files to match existing repo secrets (no GitHub admin needed):
```yaml
FIREBASE_APPLICATION_ID: ${{ secrets.FIREBASE_APPLICATION_ID }}
```

**Option B** — Create `FIREBASE_DESKTOP_*` secrets in repo settings (if desktop needs different Firebase project):
This would require GitHub admin access to add the secrets.

## Workflow File Issue

The "workflow file issue" error needs investigation. Possible causes:
- YAML syntax validation failure during push
- Reusable workflow reference issue with `permissions:` block

Steps to debug:
1. Run `actionlint` locally on all workflow files
2. Check if `permissions` is valid on reusable workflow caller jobs
3. Validate all `${{ }}` expressions

## Affected Workflows

- `.github/workflows/on-distribute.yml`
- `.github/workflows/on-merge.yml`
- `.github/workflows/distribute-android-action.yml`
- `.github/workflows/distribute-desktop-action.yml`

## Priority

**High** — CI/CD completely broken, no distributions possible.

## Labels

`bug`, `ci/cd`, `infrastructure`
