---
name: write-meta-prompt
description: Expert in transforming vague ideas into professional, structured prompts optimized for Kotlin Multiplatform Engineering. Use this skill when you need to create clear, actionable prompts for AI agents or team members.
---

# Prompt Writer Skill

## Description
Transforms vague, high-level, or unstructured requests into precise, comprehensive, and actionable prompts for specialized AI agents or engineering teams.

## Usage
```bash
skill: write-meta-prompt [your vague task description]
```

## Capabilities

### 1. Intent Clarification
- Identifies core technical goals
- Extracts implicit requirements
- Determines scope (File, Module, Project)

### 2. Prompt Structuring
- **Role Definition**: Who should execute the task
- **Context**: Necessary background information
- **Task**: Specific action items
- **Constraints**: What NOT to do
- **Output Format**: Expected deliverable format

### 3. KMP-Specific Enhancement
Automatically adds relevant context for:
- **Architecture**: MVI/MVVM, Clean Architecture
- **Tech Stack**: Kotlin, Coroutines, Flow, Compose Multiplatform
- **Testing**: kotlin.test, MockK
- **Performance**: Threading, memory, recomposition
- **Platforms**: Android, iOS, Desktop (JVM)

## Reference Files

- [prompt-templates.md](references/prompt-templates.md) - Standard prompt templates
- [kmp-context.md](references/kmp-context.md) - KMP-specific context patterns
- [examples.md](references/examples.md) - Before/after prompt examples

## Output Format

```markdown
# [Role Name] Task

**Objective**: [Clear statement of the goal]

**Context**:
[Relevant background information, current state, dependencies]

**Requirements**:
1. [Requirement 1]
2. [Requirement 2]

**Constraints**:
- [Constraint 1]
- [Constraint 2]

**Expected Output**:
[Description of desired artifact]
```

## Common Transformations

| Vague Request | Structured Prompt |
|---------------|-------------------|
| "Fix the bug" | "Fix NullPointerException in ViewModel.onLogin() caused by..." |
| "Make it faster" | "Optimize Chart recomposition by using remember/derivedStateOf..." |
| "Add tests" | "Create unit tests for UseCase covering happy path, errors, edge cases..." |
| "Refactor this" | "Refactor SettingsScreen to follow MVI pattern: extract ViewState, ViewEvent, SideEffect..." |

## Best Practices

1. **Be Specific**: Transform "make it better" into measurable goals
2. **Add Context**: Include relevant architecture patterns and constraints
3. **Target Audience**: Identify who will execute (Dev, SDET, Reviewer)
4. **Break Down**: Complex tasks should be split into multiple focused prompts
5. **Include Examples**: When possible, provide code examples or patterns to follow

## Related Skills

- **write-unit-test**: For creating test implementations
- **implement-ui**: For scaffolding UI/Presentation layer
- **implement-domain**: For creating Logic layer
