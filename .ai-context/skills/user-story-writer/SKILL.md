---
name: user-story-writer
description: >
  Write well-formed user stories following INVEST criteria and BDD acceptance
  criteria. Use when asked to write user stories, create backlog items, break
  down an epic into stories, or refine existing stories. Triggers on phrases
  like "write user story", "create user stories", "backlog item", "break down
  this epic", "refine this story", "evaluate INVEST".
metadata:
  author: Javier Camarena
  version: 1.0.0
---

# User Story Writer

Write individual user stories that are well-structured, INVEST-compliant, and
ready for sprint refinement. Stories follow the same format and ID convention
(US-NNN) used in the PRD generator skill, so they can be directly embedded in
a PRD or exported to a story map.

## Input / Output

**Input**: A feature description, epic, or set of requirements in any format (prose, bullet list, one-liner).

**Output**: One or more files saved as `user-stories/US-NNN-slug.md`. Each file is a self-contained story with BDD acceptance criteria, INVEST checklist, and an empty `## Tasks` section.

## Process

1. **Read context**: If a design document or PRD exists in the project, read it
   to understand the data model, existing stories, and architecture.

2. **Ask before writing**: Gather the information needed to write a quality
   story. Do not assume. Ask at minimum:
   - Who is the user or persona performing this action?
   - What do they want to do?
   - What value or benefit do they get from it?
   - Which epic does this story belong to?
   - Are there known constraints, business rules, or edge cases to consider?
   - What does "done" look like for this story? (helps define acceptance criteria)
   - Is there a next ID to use, or should you auto-increment from existing stories?

3. **Evaluate INVEST**: Before writing the final story, verify it passes all six
   criteria. If it fails any, adjust scope or split the story.

4. **Assign priority**: Use the 7 prioritization factors in
   [references/prioritization.md](references/prioritization.md) to justify the
   MoSCoW level. If writing multiple stories at once, offer to generate a
   prioritization table using the AI prompt defined there.

5. **Write the story**: Follow the template in
   [references/user-story-template.md](references/user-story-template.md).

6. **Save or embed**: Save the story as `user-stories/US-NNN-slug.md`, or embed
   it in the relevant PRD if one exists. Inform the user of the path.

## Writing Guidelines

1. **Persona over role**: "As Marta, senior recruiter" is stronger than
   "As a recruiter". Use personas when the project has them defined.
2. **Value must be external and visible**: The benefit in "so that..." must be
   something the user experiences, not a technical improvement.
3. **One story, one need**: If the story serves two different goals, split it.
4. **Acceptance criteria = testable scenarios**: Use the BDD format defined
   below. Each scenario covers one distinct case (happy path, error, edge case).
   **Scenario 3 is always the edge case** — use the dedicated inline format
   (not a code block) to visually distinguish boundary conditions:

   ```
   **Scenario 3 — Edge case:** [one-line description of the boundary or non-obvious condition]
   - **Given** [boundary or unusual precondition — e.g. empty state, max length, concurrent access]
   - **When** [action performed in this boundary context]
   - **Then** [expected boundary behaviour]
   ```
5. **No implementation in the story**: The story describes WHAT and WHY.
   Implementation details belong in tasks, not in the story or ACs.
6. **Small means vertical slice**: A story should cut through all layers
   (frontend, backend, data) and deliver end-to-end value — like a thin slice
   of a cake that includes all layers, not just the top.

## Acceptance Criteria Format

Use a two-level hierarchy. Top-level groups (e.g., `Scenario 1.`) name the
theme. Sub-scenarios use bold headings and bullet keywords. Use "shall" for
expected outcomes and capitalize NOT for negations. Merge all `And` clauses
into the `- **Then**` line, separated by commas. Omit `- **When**` when the
precondition alone fully implies the trigger.

```
Scenario N. [Group title describing what the sub-scenarios share]

**Scenario N.1:** [Specific sub-scenario title]
- **Given** [precondition]
- **When** [triggering action]
- **Then** [actor/system] shall [primary outcome], [actor/system] shall [additional outcome]

**Scenario N.2:** [Specific sub-scenario title]
- **Given** [precondition]
- **When** [triggering action]
- **Then** [actor/system] shall [expected outcome], [actor/system] shall NOT [negated outcome]
```

The edge case always uses the dedicated label:

```
**Scenario 3 — Edge case:** [one-line description of the boundary or non-obvious condition]
- **Given** [boundary or unusual precondition — e.g. empty state, max length, concurrent access]
- **When** [action performed in this boundary context]
- **Then** [expected boundary behaviour]
```

---

Group related scenarios logically (e.g., "Happy Path and Notifications",
"Validation and Access Control"). Aim for 2 groups × 2 sub-scenarios = 4
total. **Do not exceed 6 sub-scenarios per story** — more is a signal the
story is too large and should be split (INVEST: S).
