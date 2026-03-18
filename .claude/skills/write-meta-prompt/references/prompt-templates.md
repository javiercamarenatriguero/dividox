# Prompt Templates

## Template 1: Development Task

```markdown
# [Role]: [Task Type] - [Feature/Component Name]

**Objective**: [One-line clear goal]

**Context**:
- Current State: [What exists now]
- Problem: [What needs fixing/improving]
- Architecture: [Relevant patterns]
- Dependencies: [Required modules, libraries]

**Requirements**:
1. [Functional requirement 1]
2. [Functional requirement 2]

**Constraints**:
- [What not to change]
- [What not to use]

**Expected Output**:
- [File 1]: [Description]
- [File 2]: [Description]

**Acceptance Criteria**:
- [ ] [Criterion 1]
- [ ] All tests pass
- [ ] Detekt checks pass
```

## Template 2: Testing Task

```markdown
# SDET Task: Write Tests for [Component/Feature]

**Objective**: Create comprehensive test coverage

**Context**:
- Component: `[path/to/file]`
- Type: [ViewModel/UseCase/Repository]
- Target Coverage: [Y%]

**Test Requirements**:
- [ ] Happy path scenarios
- [ ] Error handling
- [ ] Edge cases
- [ ] Boundary conditions

**Expected Output**:
- `[ComponentName]Test.kt` in `src/commonTest/`
- GIVEN/WHEN/THEN naming
```

## Template 3: Refactoring Task

```markdown
# Senior Engineer: Refactor [Component/Feature]

**Objective**: Refactor to [improve X/follow Y pattern]

**Context**:
- Current File(s): `[path/to/files]`
- Current Issues: [list]
- Target Pattern: [MVI/Clean Architecture/etc.]

**Constraints**:
- Maintain existing public API
- Preserve existing behavior
- Maintain test coverage

**Acceptance Criteria**:
- [ ] All existing tests still pass
- [ ] Follows architecture guidelines
```

## Template 4: Bug Fix Task

```markdown
# Engineer: Fix Bug - [Bug Title]

**Objective**: Fix [specific bug description]

**Bug Details**:
- **Severity**: [Critical/High/Medium/Low]
- **Affected Component**: `[file/module]`

**Current Behavior**: [What happens now]
**Expected Behavior**: [What should happen]

**Fix Requirements**:
1. [Fix specific issue]
2. [Add regression test]

**Acceptance Criteria**:
- [ ] Bug no longer reproducible
- [ ] Regression test added
- [ ] All tests pass
```
