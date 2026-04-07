# Validation Rules Reference

## Branch Name Validation

### Rules
1. Must start with `feature/`
2. Must include ticket prefix
3. Must include numeric ticket number
4. Must include branch description after ticket number separated by `-`
5. Description must be lowercase and hyphen-separated

### Examples

**Valid**:
```bash
feature/TICKET-123-my-changes
feature/DIV-42-settings-screen
```

**Invalid**:
```bash
feature/TICKET-123/my-changes    # Slash not allowed after ticket number
feature/TICKET-123_my-changes    # Underscore not allowed
feature/TICKET123-my-changes     # Missing hyphen after prefix
```

---

## Commit Message Validation

### Rules
1. Must start with ticket prefix
2. Must have numeric ticket number
3. Must have one space after ticket number
4. Must have capitalized description
5. Must not end with period

### Examples

**Valid**:
```bash
TICKET-123 My changes
DIV-42 Add settings screen
```

**Invalid**:
```bash
TICKET-123: My changes     # Colon not allowed
TICKET-123 my changes      # Not capitalized
TICKET-123 My changes.     # Trailing period
```

---

## PR Title Validation

### Rules
1. Must start with ticket prefix
2. Must have numeric ticket number
3. Must have title case
4. Total length must be <= 72 characters

### Examples

**Valid**:
```bash
TICKET-123 My Changes
DIV-42 Add Settings Screen
```

**Invalid**:
```bash
TICKET-123: My Changes    # Colon not allowed
TICKET-123 my changes     # Not title case
```
