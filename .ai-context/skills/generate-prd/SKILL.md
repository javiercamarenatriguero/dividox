---
name: generate-prd
description: Generates Product Requirements Documents (PRDs) for specific features or use cases. Use when planning features that need detailed requirements, user stories, acceptance criteria, and release plans. Part of the Planning phase of product documentation.
---

# PRD Generator

Generates comprehensive Product Requirements Documents following a standardized structure. Each PRD covers a single feature or capability.

## Usage

```
/generate-prd <feature name or use case>
```

## Input Requirements

Ask the user for:
1. **Feature name** — clear, descriptive title
2. **Product context** — the product this feature belongs to (or read from existing product-description doc)
3. **Priority** — P0/P1/P2
4. **Number of PRDs** — how many PRDs to generate (default: 3, one per main use case)

If a product description document already exists in the project, read it to extract context, use cases, and feature priorities.

## Output Structure

Each PRD is saved as a separate file under `docs/<product-name>/PRDs/PRD-NNN-kebab-case-title.md`

### PRD Template

```markdown
# PRD: [Feature Title]

## 1. Overview
[2-3 paragraph description of the feature, its importance, and how it fits into the product]

---

## 2. Problem Statement
- **What pain point does this solve?** [Description]
- **Who is affected?** [Primary, secondary, tertiary users]
- **What happens today without this feature?** [Current state description]

---

## 3. Goals & Success Metrics
| Goal | Metric | Target |
|---|---|---|
| ... | ... | ... |

---

## 4. Scope

### In Scope
- [Bullet list of what is included]

### Out of Scope
- **[Item]** -- [reason for exclusion and when it might be addressed]

---

## 5. User Stories
| ID | As a... | I want to... | So that... | Priority |
|---|---|---|---|---|
| US-001 | ... | ... | ... | Must have |

---

## 6. Functional Requirements

### FR-001: [Requirement Title]
- **Description**: [What the system does]
- **Acceptance Criteria**:
  - [ ] Given [context], when [action], then [expected result].
- **Business Rules**:
  - [Rule 1]

[Repeat for each functional requirement: FR-002, FR-003, etc.]

---

## 7. Non-Functional Requirements
- **Performance**: [Latency, throughput targets with percentiles]
- **Security**: [Auth, data protection, compliance]
- **Scalability**: [Concurrent users, data volume targets]
- **Accessibility**: [WCAG level, specific requirements]

---

## 8. UI/UX Considerations

### Key Screens
1. **[Screen Name]** — [Description with layout details]

### User Flow
1. [Step-by-step flow through the feature]

---

## 9. Dependencies
| Dependency | Type | Status | Notes |
|---|---|---|---|
| ... | Internal/External | ... | ... |

---

## 10. Risks & Mitigations
| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| ... | High/Medium/Low | High/Medium/Low | ... |

---

## 11. Release Plan

### Phase 1 -- MVP (Weeks 1-N)
- [Deliverables]

### Phase 2 -- Enhancement (Weeks N-M)
- [Deliverables]

### Phase 3 -- Scale (Weeks M-P)
- [Deliverables]

---

## 12. Open Questions
- [ ] [Question 1]
- [ ] [Question 2]
```

## Rules

- Each PRD is a standalone document — it must be understandable without reading other PRDs
- User stories use the format: "As a [role], I want to [action], so that [benefit]"
- Acceptance criteria use Given/When/Then format
- Functional requirements are numbered sequentially (FR-001, FR-002, ...)
- User stories are numbered sequentially (US-001, US-002, ...)
- Non-functional requirements include specific, measurable targets (not vague statements)
- Risks must include concrete mitigations, not just "monitor"
- Release plan phases should be realistic and incremental
- File naming: `PRD-NNN-kebab-case-title.md` (e.g., `PRD-001-ai-screening.md`)
- All content in English
- Create the PRDs/ directory under `docs/<product-name>/`

## Index Table

After generating all PRDs, create an index table in the main document:

```markdown
## Product Requirements Documents (PRDs)

| PRD | Feature | Priority | Status |
|---|---|---|---|
| [PRD-001](PRDs/PRD-001-feature-name.md) | Feature Name | P0 | Draft |
```
