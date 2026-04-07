# Workflow Patterns Reference

## Branch Naming Patterns

### Feature Branches

**Pattern**:
```
feature/{TICKET}-{ticket-number}-{brief-description}
```

**Rules**:
- Use lowercase letters only
- Separate words with hyphens (`-`)
- Keep descriptions concise but meaningful
- Always include ticket number with hyphen

---

## Commit Message Patterns

**Pattern**:
```
{TICKET}-{ticket-number} {Brief description of the change}
```

**Rules**:
- Start with ticket number and one space
- Start description with capital letter
- Keep concise and specific
- One logical change per commit
- No colon after ticket number
- No period at end

---

## Pull Request Patterns

### PR Title
**Pattern**:
```
{TICKET}-{ticket-number} {Title Case Description}
```

**Rules**:
- Start with ticket number and one space
- Use title case for description
- Keep under 72 characters

### PR Description
**Pattern**:
```markdown
{TICKET}-{ticket-number}

{Detailed description of the changes}

Changes include:
- {Change 1}
- {Change 2}
```

---

## Validation Patterns

### Branch Name Validation
**Regex**: `^feature/[A-Z]+-\d+-[a-z0-9-]+$`

### Commit Message Validation
**Regex**: `^[A-Z]+-\d+ [A-Z].+[^.]$`

### PR Title Validation
**Regex**: `^[A-Z]+-\d+ [A-Z].{3,68}$`
