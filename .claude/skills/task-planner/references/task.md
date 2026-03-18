# Task: DIV-XYZ Feature Name

## Description

Implement the feature allowing users to [description]. Users can navigate from [origin] to the new screen.

**Key Requirements:**
- Navigation flow: [Origin] -> [Feature Screen] (Init/Loading/Success/Failure states)
- [Key requirement 1]
- [Key requirement 2]

**Status:** Backlog

---

## Subtasks

### Phase 1: Architecture & Setup
- [ ] **Create Git Branch**
  - Branch: `feature/DIV-XYZ-feature-name`
  - **Action:** Use `skill: manage-git-flow`

### Phase 2: Domain Layer
- [ ] **Define Domain Models**
  - Location: `composeApp/src/commonMain/kotlin/com/akole/dividox/domain/model/`
  - **Verify:** `./gradlew :composeApp:jvmTest`
  - **Commit:** `DIV-XYZ Add domain models`

- [ ] **Define Repository Interface**
  - Location: `composeApp/src/commonMain/kotlin/com/akole/dividox/domain/repository/`
  - **Commit:** `DIV-XYZ Add repository interface`

- [ ] **Implement Use Cases**
  - Write unit tests first (TDD)
  - **Commit:** `DIV-XYZ Add use cases with tests`

### Phase 3: Data Layer
- [ ] **Implement Repository**
  - Write unit tests first (TDD)
  - **Commit:** `DIV-XYZ Add repository implementation with tests`

### Phase 4: Presentation Layer
- [ ] **Define MVI Contract**
  - ViewState, ViewEvent, SideEffect
  - **Commit:** `DIV-XYZ Add MVI contract`

- [ ] **Implement ViewModel**
  - Write unit tests first (TDD)
  - **Commit:** `DIV-XYZ Add ViewModel with tests`

- [ ] **Implement Screen**
  - **Commit:** `DIV-XYZ Add screen UI`

### Phase 5: Navigation & DI
- [ ] **Wire Navigation**
  - **Commit:** `DIV-XYZ Wire navigation`

- [ ] **Register in DI**
  - **Commit:** `DIV-XYZ Register dependencies in DI`

### Phase 6: Testing & Quality
- [ ] **Run Full Test Suite**
  - `./gradlew test`
  - `./gradlew detekt`

- [ ] **Create Pull Request**
  - **Skill:** `skill: manage-git-flow`

---

## Progress Tracking

**Total Tasks:** 11
**Completed:** 0
**Remaining:** 11

---

## Notes

- **TDD Mandatory:** Follow RED-GREEN-REFACTOR cycle for all business logic
- **Incremental Commits:** Commit after each logical step
- **Compilation:** Verify code compiles after each phase
- **KMP:** Shared code goes in `commonMain`, platform-specific in respective source sets
