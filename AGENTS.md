# AGENTS.md

## Project context

Dividox is a **Kotlin Multiplatform (KMP)** investment portfolio app built with Compose Multiplatform, targeting Android, iOS, and Desktop.

Architectural decisions are documented in `docs/adr/` and are the **single source of truth** for technology choices, integration patterns, and constraints. Before generating user stories, tickets, or implementation tasks, ground work in the relevant ADRs — especially authentication (ADR-001 to ADR-004), data architecture (ADR-005 to ADR-008), and theming (ADR-009).

Key constraints that affect scope:
- All shared logic lives in `commonMain`; platform-specific code uses `expect/actual`
- Auth backend: Firebase (ADR-001), Clean Architecture split (ADR-002)
- Token storage contract defined in ADR-008 — do not deviate
- UI: Compose Multiplatform + Material3 (ADR-009)

---

## Skills

All recurring workflows are handled by skills bundled locally under `.claude/skills/`.
**Skills are the single source of truth for output structure and process.** Do not duplicate template content or workflow steps anywhere else.

### Product & Requirements

| Skill | Trigger |
|---|---|
| `write-meta-prompt` | Transform vague ideas into structured feature prompts |
| `generate-prd` | Write a full Product Requirements Document |
| `product-description` | Write a product overview or elevator pitch |
| `product-roadmap` | Plan releases and milestones |
| `user-story-writer` | Write INVEST-compliant user stories → `user-stories/US-NNN-slug.md` |
| `story-map-generator` | Generate a User Story Map by activity → `story-maps/SM-NNN-slug.md` |
| `ticket-writer` | Decompose user stories into sprint tickets → `tickets/TK-NNN-slug.md` |
| `estimate-effort` | Size stories/tickets using Fibonacci + MoSCoW |
| `task-planner` | Break features into ordered, layered subtasks |

### Architecture & Design

| Skill | Trigger |
|---|---|
| `generate-adr` | Document an architecture decision → `docs/adr/ADR-NNN-slug.md` |
| `design-c4` | Produce C4 architecture diagrams |
| `design-data-model` | Define domain entities and relationships |
| `design-md` | Write design documentation in markdown |
| `design-system` | Define or update the visual design system |
| `stitch-design` | Generate UI screens via Stitch |
| `module-organization` | Understand and plan KMP module structure |

### Implementation

| Skill | Trigger |
|---|---|
| `implement-domain` | Scaffold domain layer (entities, use cases, repository interfaces) |
| `implement-ui` | Build Compose Multiplatform screens with MVI pattern |
| `implement-di` | Wire Koin dependency injection modules |
| `implement-navigation` | Set up Compose Navigation routes and graphs |
| `write-unit-test` | Write `commonTest` unit tests for domain and UI logic |
| `audit-compose-performance` | Review Compose recomposition and performance |

### Quality & Operations

| Skill | Trigger |
|---|---|
| `owasp-security-review` | Run OWASP Top 10 review on changed code |
| `manage-git-flow` | Validate branch names, commit messages, and PR conventions |
| `full-doc` | Generate comprehensive documentation for a module or feature |
| `skill-creator` | Codify a new recurring workflow as a skill |

---

## Agent orchestration

```
.claude/agents/
  po.agent.md             → Product Owner: user stories, tickets, estimation, roadmap
  developer.agent.md      → KMP Engineer: domain, UI, DI, navigation, tests
  code-reviewer.agent.md  → Senior Reviewer: code quality, architecture, security, Compose perf
```

### Typical feature workflow

```
1. PO agent          — write-meta-prompt → user-story-writer → story-map-generator
2. PO agent          — ticket-writer → estimate-effort
3. Developer agent   — implement-domain → implement-ui → implement-di → implement-navigation → write-unit-test
4. Code Reviewer     — owasp-security-review → audit-compose-performance → manage-git-flow
```

### ADR workflow

```
Developer / PO agent — generate-adr (when a significant architectural decision is made)
```

Each ADR goes to `docs/adr/ADR-NNN-slug.md` and must reference the affected modules and any superseded decisions.

---

## Conventions

- **Branch naming**: `feature/DVX-NNN-slug`, `fix/DVX-NNN-slug`, `chore/DVX-NNN-slug`
- **Commit format**: `DVX-NNN Short imperative description`
- **User story IDs**: `US-NNN` (auto-increment from existing files in `user-stories/`)
- **Ticket IDs**: `TK-NNN` (auto-increment from existing files in `tickets/`)
- **ADR IDs**: `ADR-NNN` (auto-increment from existing files in `docs/adr/`)
- **Platform targets**: Always note Android / iOS / Desktop divergence in ACs when behavior differs
