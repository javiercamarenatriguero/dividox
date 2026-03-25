---
name: generate-adr
description: Generates Architecture Decision Records (ADRs) documenting key technical decisions with context, alternatives, and consequences. Use when documenting architectural choices such as technology selection, patterns, infrastructure, or design trade-offs. Part of the Solutioning phase of product documentation.
---

# ADR Generator

Generates Architecture Decision Records following a standardized format. Each ADR documents a single architectural decision.

## Usage

```
/generate-adr <decision topic>
```

## Input Requirements

Read existing product documentation (system design, data model, PRDs) to identify:
1. **Key architectural decisions** — technology choices, patterns, trade-offs
2. **Context** — why this decision needs to be made
3. **Constraints** — team size, budget, timeline, existing infrastructure

If no existing documentation is found, ask the user for the decision context.

## Output Structure

Each ADR is saved as a separate file under `docs/<product-name>/ADRs/ADR-NNN-kebab-case-title.md`

### ADR Template

```markdown
# ADR-NNN: [Decision Title]

**Date:** YYYY-MM-DD
**Status:** Proposed | Accepted | Deprecated | Superseded

## Context

[2-3 paragraphs explaining WHY this decision needs to be made. What is the problem or requirement that drives this decision? What constraints exist?]

## Decision

[1-2 paragraphs describing WHAT was decided. Be specific about the technology, pattern, or approach chosen. Include version numbers or specific configurations when relevant.]

## Alternatives Considered

### Alternative 1: [Name]
- **Pros:** [Benefits of this approach]
- **Cons:** [Drawbacks of this approach]

### Alternative 2: [Name]
- **Pros:** [Benefits]
- **Cons:** [Drawbacks]

[Include 2-3 alternatives minimum]

## Consequences

### Positive
- [Benefit 1]
- [Benefit 2]
- [Benefit 3]

### Negative
- [Trade-off 1]
- [Trade-off 2]
- [Trade-off 3]

## Related ADRs

- [ADR-NNN (brief description of relationship)]
```

## Common ADR Topics

When generating ADRs for a new product, consider these common decisions:

1. **Architecture style** — Microservices vs monolith vs modular monolith
2. **Communication patterns** — REST vs gRPC vs GraphQL; sync vs async
3. **API gateway** — dedicated gateway vs service mesh vs direct calls
4. **Real-time communication** — WebSockets vs SSE vs polling
5. **AI/ML separation** — dedicated AI service vs embedded in business logic
6. **Multi-tenancy** — shared DB vs DB-per-tenant vs schema-per-tenant
7. **Authentication** — JWT vs sessions vs OAuth provider
8. **Database** — SQL vs NoSQL vs polyglot persistence
9. **Event handling** — event sourcing vs simple events vs CQRS
10. **Deployment** — Kubernetes vs serverless vs PaaS

## Rules

- Each ADR documents exactly ONE decision
- Context must explain WHY, not just WHAT
- Always include at least 2 alternatives considered with honest pros/cons
- Consequences must include both positive AND negative outcomes
- Status should be "Accepted" for decisions that are finalized
- Related ADRs should cross-reference each other
- File naming: `ADR-NNN-kebab-case-title.md` (e.g., `ADR-001-microservices.md`)
- Number ADRs sequentially starting from 001
- Date should be the generation date
- All content in English

## Index Table

After generating all ADRs, create an index table in the main document:

```markdown
## Architecture Decision Records (ADRs)

| ADR | Decision | Status |
|---|---|---|
| [ADR-001](ADRs/ADR-001-title.md) | Decision summary | Accepted |
```

## Batch Generation

When asked to generate multiple ADRs, identify 4-8 key architectural decisions from the system design and generate them in sequence. Ensure cross-references between related ADRs are consistent.
