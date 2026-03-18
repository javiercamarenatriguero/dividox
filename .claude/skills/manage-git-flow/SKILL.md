---
name: manage-git-flow
description: Git workflow management for projects. Use when developers need to (1) create properly formatted Git branches following feature/{ticket}-{description} pattern, (2) format commit messages with ticket prefixes, (3) generate Pull Request titles and descriptions, (4) validate branch names or commit messages against project standards, or (5) get step-by-step workflow guidance for feature development or bug fixes.
---

# Git Workflow Manager

## Quick Start

```bash
# Create branch
skill: manage-git-flow create-branch TICKET-123 my-changes
# -> git checkout -b feature/TICKET-123-my-changes

# Format commit
skill: manage-git-flow commit TICKET-123 "my changes"
# -> TICKET-123 My changes

# Generate PR
skill: manage-git-flow pr TICKET-123 "My Changes"
# -> PR title and description template

# Validate
skill: manage-git-flow validate-branch feature/TICKET-123-my-changes
skill: manage-git-flow validate-commit "TICKET-123 My changes"
```

## Patterns

**Branch**: `feature/{TICKET}-{n}-{description}`
**Commit**: `{TICKET}-{n} {Brief description}`
**PR Title**: `{TICKET}-{n} {Title Case}`
**PR Body**: `{TICKET}-{n}\n\n{Description}\n\nChanges include:\n- {items}`

## Workflow Example

```bash
# 1. Create branch
git checkout main && git pull
git checkout -b feature/TICKET-123-my-changes

# 2. Commit incrementally (one logical change each)
git add [files] && git commit -m "TICKET-123 Add domain models"
git add [files] && git commit -m "TICKET-123 Create repository implementation"
git add [files] && git commit -m "TICKET-123 Add ViewModel"
git add [files] && git commit -m "TICKET-123 Create screen UI"
git add [files] && git commit -m "TICKET-123 Wire navigation"
git add [files] && git commit -m "TICKET-123 Add tests"

# 3. Push
git push origin feature/TICKET-123-my-changes

# 4. Create PR with generated template
```

**Commit order**: Dependencies -> Domain/Data -> Presentation -> UI -> Wiring -> Tests

## Best Practices

**One logical change per commit**: Makes reviews easier and enables selective reverts

**Be specific**: "Add UserRepository interface" not "Update files"

**Commit early and often**: Don't wait until everything is done

## Reference Files
- **[workflow-patterns.md](references/workflow-patterns.md)** - Branch, commit, PR patterns and regex
- **[validation-rules.md](references/validation-rules.md)** - Validation algorithms and error messages
- **[examples.md](references/examples.md)** - Real-world commit sequences and anti-patterns
