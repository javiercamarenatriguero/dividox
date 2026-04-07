# Story Map Template

Use this exact structure when generating story maps.

---

# Story Map: [Product / Feature Name]

**Goal**: [What user journey does this map cover? One sentence.]
**Primary users**: [Personas or user roles]
**Releases covered**: MVP / v1.1 / v2.0 (adjust as needed)
**Generated**: YYYY-MM-DD

---

## Backbone

The high-level activities the user performs, in chronological order.

| # | Activity | Description |
|---|---|---|
| 1 | [Activity name] | [What the user is trying to accomplish at this stage] |
| 2 | [Activity name] | [What the user is trying to accomplish at this stage] |
| 3 | [Activity name] | [What the user is trying to accomplish at this stage] |
| 4 | [Activity name] | [What the user is trying to accomplish at this stage] |

---

## Story Map

> Read each column as "stories that support this activity".
> Read each row band as "stories in this release".
> Top of each column = most essential; bottom = deferred value.

### MVP — minimum to complete the journey end-to-end

| [Activity 1] | [Activity 2] | [Activity 3] | [Activity 4] |
|---|---|---|---|
| **US-001**: [title] | **US-004**: [title] | **US-007**: [title] | **US-010**: [title] |
| **US-002**: [title] | **US-005**: [title] | **US-008**: [title] | |
| **US-003**: [title] | | **US-009**: [title] | |

---

### v1.1 — depth and usability improvements

| [Activity 1] | [Activity 2] | [Activity 3] | [Activity 4] |
|---|---|---|---|
| **US-011**: [title] | **US-013**: [title] | **US-015**: [title] | **US-017**: [title] |
| **US-012**: [title] | **US-014**: [title] | **US-016**: [title] | |

---

### v2.0 — scale and advanced features

| [Activity 1] | [Activity 2] | [Activity 3] | [Activity 4] |
|---|---|---|---|
| **US-018**: [title] | **US-020**: [title] | **US-022**: [title] | **US-024**: [title] |
| **US-019**: [title] | **US-021**: [title] | **US-023**: [title] | |

---

## Story Index

Quick reference for all stories in the map. Use the user-story-writer skill to
flesh out each story individually.

| ID | Title | Activity | Release | Priority |
|---|---|---|---|---|
| US-001 | [title] | [activity] | MVP | Must have |
| US-002 | [title] | [activity] | MVP | Must have |
| US-003 | [title] | [activity] | MVP | Should have |

---

## Gaps & Open Questions

> Gaps in the map often signal missing requirements or scope decisions.

- [ ] [Gap or question 1]
- [ ] [Gap or question 2]

---

## Next Steps

- [ ] Flesh out individual stories using the **user-story-writer** skill
- [ ] Validate MVP scope with stakeholders
- [ ] Generate a PRD for the MVP using the **prd-generator** skill
