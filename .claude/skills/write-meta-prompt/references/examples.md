# Prompt Examples: Before and After

## Example 1: Feature Request

### Before (Vague)
```
"I need a new screen for settings"
```

### After (Structured)
```markdown
# KMP Engineer: Create Settings Feature

**Objective**: Implement a Settings screen for user preferences

**Context**:
- Module: `composeApp` (commonMain)
- Architecture: MVI pattern, Clean Architecture
- Platform: KMP (Android, iOS, Desktop)

**Requirements**:

### Domain Layer
1. Create `SettingsViewState` with preference fields
2. Create `SettingsViewEvent` for user actions
3. Create `SettingsSideEffect` for navigation

### Presentation Layer
4. Create `SettingsViewModel` following MVI pattern
5. Create `SettingsScreen` using Material 3

**Constraints**:
- Must use Material 3 components
- Settings must persist across app restarts
- Shared code in commonMain

**Expected Output**:
- Complete implementation with unit tests
- Navigation wired
- All tests passing
```

## Example 2: Bug Fix

### Before (Vague)
```
"The app crashes sometimes when loading data"
```

### After (Structured)
```markdown
# KMP Engineer: Fix Crash in Data Loading

**Objective**: Fix NullPointerException in data loading

**Current Behavior**:
1. User opens screen
2. App attempts to load data
3. Crash occurs if source is unavailable

**Expected Behavior**:
1. Show "No data available" message
2. No crash occurs

**Fix Requirements**:
1. Add null-check before accessing properties
2. Return `Result.failure()` with appropriate error
3. Update UI to handle error state
4. Add unit test to reproduce and verify fix
```

## Example 3: Performance

### Before (Vague)
```
"The list is slow and laggy"
```

### After (Structured)
```markdown
# KMP Engineer: Optimize List Performance

**Objective**: Reduce recompositions and improve rendering

**Optimization Requirements**:
1. Use `remember` for expensive computations
2. Add stable keys to LazyColumn items
3. Move data processing to ViewModel
4. Use `derivedStateOf` for computed values

**Constraints**:
- Maintain existing visual design
- No breaking changes to API
```

## Pattern Recognition

| User Says | Task Type | Focus |
|-----------|-----------|-------|
| "Create/Add new..." | Feature | Full stack implementation |
| "Fix/Bug/Crash..." | Bug Fix | Root cause + regression test |
| "Test/Add tests..." | Testing | Coverage + standards |
| "Optimize/Speed up..." | Performance | Metrics + optimization |
| "Refactor/Clean up..." | Refactoring | Architecture compliance |
