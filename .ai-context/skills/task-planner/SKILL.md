---
name: task-planner
description: Plan tasks for implementation, creating subtasks in a task-specific `task.md` file. Use when breaking down features into actionable implementation steps.
---

# Task Planner Skill

**Purpose**: Plan tasks for implementation, creating subtasks in a task-specific `task.md` file.

## Capabilities

### 1. General Task Planning
Creates a task-specific `task.md` file with a breakdown of subtasks for any given implementation request.

### 2. External Resource Integration
If a Figma file key or external reference is provided, use available tools to fetch the content and enrich the `task.md` with specific implementation details.

## Usage

```bash
skill: task-planner plan "Implement feature X"
```

## task.md Format

### Template & Rules

1. **Description**: Must include detailed context from the user prompt or external sources.
2. **Subtasks**:
   - Break down into: Module setup, Domain, Data, UI, Navigation, Testing.
   - **UI Tasks**: Include the specific Figma link if available.
   - **Compilation**: Every task must result in compiling code.
   - **Commits**: Every logical step must be committed to git.
3. **Progress Tracking**: Update the `task.md` file to mark tasks as completed (`[x]`) after finishing each one.

### Example `task.md`

See [task.md](references/task.md) for a complete example.
