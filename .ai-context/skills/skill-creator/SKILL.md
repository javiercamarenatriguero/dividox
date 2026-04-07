---
name: skill-creator
description: Guide for creating effective skills. This skill should be used when users want to create a new skill (or update an existing skill) that extends Claude's capabilities with specialized knowledge, workflows, or tool integrations.
---

# Skill Creator

This skill provides guidance for creating effective skills.

## About Skills

Skills are modular, self-contained packages that extend Claude's capabilities by providing
specialized knowledge, workflows, and tools.

### What Skills Provide

1. Specialized workflows - Multi-step procedures for specific domains
2. Tool integrations - Instructions for working with specific file formats or APIs
3. Domain expertise - Project-specific knowledge, schemas, business logic
4. Bundled resources - Scripts, references, and assets for complex and repetitive tasks

## Core Principles

### Concise is Key

**Default assumption: Claude is already very smart.** Only add context Claude doesn't already have. Prefer concise examples over verbose explanations.

### Set Appropriate Degrees of Freedom

**High freedom**: When multiple approaches are valid.
**Medium freedom**: When a preferred pattern exists with acceptable variation.
**Low freedom**: When operations are fragile and consistency is critical.

### Anatomy of a Skill

```
skill-name/
├── SKILL.md (required)
│   ├── YAML frontmatter (name + description)
│   └── Markdown instructions
└── Bundled Resources (optional)
    ├── scripts/          - Executable code
    ├── references/       - Documentation loaded as needed
    └── assets/           - Files used in output
```

### Progressive Disclosure

1. **Metadata (name + description)** - Always in context (~100 words)
2. **SKILL.md body** - When skill triggers (<5k words)
3. **Bundled resources** - As needed by Claude

Keep SKILL.md body under 500 lines. Split content into separate files when approaching this limit.

## Skill Creation Process

1. Understand the skill with concrete examples
2. Plan reusable skill contents (scripts, references, assets)
3. Create the skill directory structure
4. Edit the skill (implement resources and write SKILL.md)
5. Iterate based on real usage

## Reference Files
- **[workflows.md](references/workflows.md)** - Sequential and conditional workflow patterns
- **[output-patterns.md](references/output-patterns.md)** - Template and example output patterns

## SKILL.md Writing Guidelines

### Frontmatter
- `name`: The skill name
- `description`: What the skill does AND when to use it. Include all trigger information here.

### Body
Write instructions for using the skill and its bundled resources. Use imperative/infinitive form.

### What NOT to Include
- README.md, CHANGELOG.md, or auxiliary documentation
- Setup and testing procedures
- User-facing documentation beyond SKILL.md
