# TK-039 — Fix MASVS Audit Prompt YAML Structure

**Type:** Technical Task
**Layer:** CI/CD
**Points:** 2
**Priority:** High
**Branch:** `feature/DVX-TK-039-fix-masvs-prompt-yaml`
**File:** `.github/masvs-audit.prompt.yml`
**Parent:** TK-038

---

## Context

The MASVS audit prompt file introduced in TK-038 has structural defects that prevent
GitHub Copilot / GitHub Models from parsing and executing the prompt correctly.

---

## Problems

### 1. `temperature` at wrong indentation level

Current (broken):
```yaml
model: openai/gpt-4o
  temperature: 0.2
```

`model` is a scalar — `temperature` cannot be a child key. Must live under `modelParameters:`.

Fix:
```yaml
model: openai/gpt-4o
modelParameters:
  temperature: 0.2
```

### 2. System prompt has no YAML key

Prompt text appears as orphaned scalar without a key. GitHub prompt files require
it as a `role: system` entry inside `messages:`.

Current (broken):
```yaml
      You are a senior mobile app security engineer...
```

Fix:
```yaml
messages:
  - role: system
    content: |
      You are a senior mobile app security engineer...
```

### 3. `testData` and `evaluators` entries lack proper YAML keys

Inline examples and evaluator checks are orphaned scalars, not valid YAML mappings.

---

## Acceptance Criteria

- [x] `temperature: 0.2` lives under `modelParameters:` at root level.
- [x] System prompt wrapped in `messages[0]` with `role: system`.
- [x] User message examples in `messages[1]` with correct `role: user` + `content:`.
- [x] `testData` entries use proper YAML list/map structure.
- [x] `evaluators` entries use proper YAML list/map structure.
- [x] `yamllint .github/masvs-audit.prompt.yml` passes with no errors.
- [x] Prompt loads in GitHub Copilot without parse errors.

---

## References

- `.github/masvs-audit.prompt.yml`
- `tickets/TK-038-masvs-ci-security-scan.md`
- [GitHub Copilot prompt file schema docs](https://docs.github.com/en/copilot/customizing-copilot/reusing-prompts-and-instructions-in-github-copilot)
