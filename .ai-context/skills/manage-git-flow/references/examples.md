# Workflow Examples Reference

## End-to-End Example

### Branch Creation
```bash
git checkout main
git pull origin main
git checkout -b feature/DIV-42-settings-screen
```

### Commit Sequence
```bash
git add composeApp/src/commonMain/.../domain/
git commit -m "DIV-42 Add settings domain models"

git add composeApp/src/commonMain/.../data/
git commit -m "DIV-42 Create settings repository"

git add composeApp/src/commonMain/.../ui/
git commit -m "DIV-42 Add settings screen UI"

git add composeApp/src/commonMain/.../navigation/
git commit -m "DIV-42 Wire navigation"
```

### Push Branch
```bash
git push origin feature/DIV-42-settings-screen
```

### Pull Request

**Title**:
```
DIV-42 Add Settings Screen
```

**Description**:
```markdown
DIV-42

Implements the settings screen.

Changes include:
- Added settings domain models
- Created settings repository
- Implemented settings screen UI
- Wired navigation
```

---

## Anti-Patterns

### Bad Branch Names
```bash
feature/DIV-42/my-changes      # Slash after ticket
feature/DIV-42_my-changes       # Underscore in description
feature/DIV42-my-changes        # Missing hyphen after prefix
```

### Bad Commit Messages
```bash
DIV-42: My changes              # Colon after ticket
DIV-42 My changes.              # Trailing period
DIV-42 update files             # Not capitalized, vague
```

### Bad PR Titles
```bash
DIV-42: My Changes              # Colon after ticket
DIV-42 my changes               # Not title case
My Changes                      # Missing ticket prefix
```
