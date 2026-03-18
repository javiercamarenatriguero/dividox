# Workflow Patterns

## Sequential Workflows

For complex tasks, break operations into clear, sequential steps:

```markdown
Processing involves these steps:

1. Analyze the input (run analyze.py)
2. Create mapping (edit config.json)
3. Validate mapping (run validate.py)
4. Process (run process.py)
5. Verify output (run verify.py)
```

## Conditional Workflows

For tasks with branching logic, guide Claude through decision points:

```markdown
1. Determine the modification type:
   **Creating new content?** -> Follow "Creation workflow" below
   **Editing existing content?** -> Follow "Editing workflow" below

2. Creation workflow: [steps]
3. Editing workflow: [steps]
```
