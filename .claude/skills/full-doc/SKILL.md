---
name: full-doc
description: Orchestrates the full product documentation lifecycle by invoking all documentation skills in sequence across four phases - Description, Planning, Solutioning, and Roadmap. Use when you need to generate complete product documentation from scratch or when starting a new product design project.
---

# Full Product Documentation Generator

Orchestrates the complete product documentation lifecycle by running all documentation skills in the correct sequence. Generates a comprehensive design document with all artifacts organized in a structured folder.

## Usage

```
/full-doc <product name or idea>
```

## Input Requirements

Before starting, gather from the user:
1. **Product name** — the name or working title
2. **Problem domain** — what problem space the product addresses
3. **Target users** — who will use the product (2-4 user roles)
4. **Key differentiators** — what makes this product unique (optional)

## Execution Phases

Execute the following phases in order. Each phase builds on the output of the previous one.

### Phase 1: Description (Discovery)

**Skill:** `/product-description`

Generates the foundational product understanding:
- Brief Description (What is it? Value proposition, competitive analysis)
- Main Functions (prioritized feature table + functional subsections)
- Lean Canvas (9-block business model)
- Main Use Cases (3 use cases with Mermaid diagrams)

**Output:** Sections 1-4 of the main document.

**Checkpoint:** After this phase, present a summary to the user and ask for confirmation before proceeding. Key questions:
- Are the identified competitors correct?
- Is the feature prioritization aligned with their vision?
- Do the use cases cover the most critical user journeys?

---

### Phase 2: Planning

**Skill:** `/generate-prd`

Generates detailed Product Requirements Documents for each main use case identified in Phase 1:
- One PRD per use case (typically 3 PRDs)
- Each PRD includes: overview, problem statement, goals, scope, user stories, functional requirements, NFRs, UI/UX, dependencies, risks, release plan

**Output:** `PRDs/` folder with individual PRD files + index table in main document.

**Checkpoint:** Confirm PRD scope and priorities with the user.

---

### Phase 3: Solutioning

**Skills:** `/design-data-model` → `/design-system` → `/design-c4` → `/generate-adr`

Execute in this order (each builds on the previous):

1. **Data Model** (`/design-data-model`) — ERD with entities derived from use cases and PRDs
2. **System Design** (`/design-system`) — Architecture, services, tech stack, system diagram
3. **C4 Diagrams** (`/design-c4`) — Three-level C4 for the most architecturally significant subsystem
4. **ADRs** (`/generate-adr`) — 4-8 Architecture Decision Records for key technical choices

**Output:** Sections 5-8 of the main document + `ADRs/` folder.

**Checkpoint:** Review architecture decisions with the user before proceeding to roadmap.

---

### Phase 4: Roadmap

**Skill:** `/product-roadmap`

Generates the delivery timeline:
- Gantt chart with all phases
- Detailed phase breakdowns with deliverables
- Key milestones table

**Output:** Section 10 of the main document.

---

## Output Folder Structure

All documentation is generated under the `docs/` directory at the project root.

```
docs/
└── <product-name>/
    ├── <PRODUCT-NAME>.md          # Main design document (all sections)
    ├── prompts.md                 # Log of prompts used (optional)
    ├── ADRs/
    │   ├── ADR-001-*.md
    │   ├── ADR-002-*.md
    │   └── ...
    ├── PRDs/
    │   ├── PRD-001-*.md
    │   ├── PRD-002-*.md
    │   └── ...
    └── resources/
        └── (any generated assets)
```

## Main Document Structure

The main `<PRODUCT-NAME>.md` file assembles all sections with this Table of Contents:

```markdown
# [Product Name] -- [Tagline]

---

## Table of Contents

1. [Brief Description](#1-brief-description)
2. [Main Functions](#2-main-functions)
3. [Lean Canvas](#3-lean-canvas)
4. [Main Use Cases](#4-main-use-cases)
5. [Data Model](#5-data-model)
6. [High-Level System Design](#6-high-level-system-design)
7. [C4 Diagram](#7-c4-diagram)
8. [Architecture Decision Records (ADRs)](#8-architecture-decision-records-adrs)
9. [Product Requirements Documents (PRDs)](#9-product-requirements-documents-prds)
10. [Product Roadmap](#10-product-roadmap)
```

## Rules

- Execute phases sequentially — each phase depends on the previous
- Pause after each phase for user review (checkpoint)
- All diagrams use Mermaid (no external image dependencies)
- All content in English
- Use horizontal rules (`---`) between major sections
- Cross-reference between sections using markdown anchor links
- PRDs and ADRs are separate files linked from the main document via index tables
- Maintain consistent terminology across all documents
- Feature priorities (P0/P1/P2) must be consistent across all phases
- The main document should be self-contained — a reader can understand the product by reading only the main file, with PRDs and ADRs providing additional depth

## Resuming

If the process is interrupted, read the existing documentation to determine which phase was last completed and resume from the next phase. Do not regenerate completed phases unless the user explicitly requests it.
