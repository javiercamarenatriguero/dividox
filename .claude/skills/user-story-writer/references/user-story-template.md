# User Story Template

Use this exact structure when writing user stories.

---

# US-NNN: [Title]

**Epic**: [Epic name]
**Priority**: Must have / Should have / Could have / Won't have
**Estimate**: [Story points or T-shirt size — filled during refinement]
**Status**: Draft / Refined / Ready

---

## Story

As a **[persona or user role]**,
I want to **[action]**,
so that **[benefit or value obtained]**.

---

## Context & Notes

[Additional context, business rules, or constraints relevant to the story.
No implementation details here — those belong in Tasks.]

---

## Acceptance Criteria

> Max 3–5 scenarios. More than 5 = story too large, split it (INVEST: S).

**Scenario 1:** [descriptive name — happy path]
- **Given** [precondition]
- **When** [action]
- **Then** [expected result 1], [expected result 2]

**Scenario 2:** [descriptive name — error or validation]
- **Given** [precondition]
- **When** [action]
- **Then** [expected result]

**Scenario 3 — Edge case:** [one-line description of the boundary or non-obvious condition]
- **Given** [boundary or unusual precondition — e.g. empty state, max length, concurrent access]
- **When** [action performed in this boundary context]
- **Then** [expected boundary behaviour]

---

## INVEST Checklist

Before marking this story as Ready, confirm all six criteria:

- [ ] **Independent** — Can be developed without blocking or being blocked by another story
- [ ] **Negotiable** — Implementation details are open to team conversation
- [ ] **Valuable** — Delivers visible, external value to the user or business
- [ ] **Estimable** — Team can estimate effort with reasonable confidence
- [ ] **Small** — Completable as a vertical slice within one sprint (~2–4 days)
- [ ] **Testable** — Acceptance criteria are specific and verifiable

> If any criterion fails, either rewrite the story or split it before adding
> it to the sprint.

---

## Related Stories

- [US-NNN: title]
- [US-NNN: title]

---

## Tasks

> Filled during sprint planning by the development team.

- [ ] Task 1
- [ ] Task 2
