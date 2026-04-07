---
name: ticket-writer
description: >
  Generate Agile work tickets (Features, Technical Tasks, Bugs, Improvements,
  Spikes) from user stories. Use when asked to create tickets, break down a
  user story into tasks, generate sprint tickets, create work items, decompose
  a story into technical tasks, or populate a sprint backlog. Triggers on
  phrases like "generate tickets", "create tasks from this story", "break down
  into tickets", "sprint tickets for US-NNN", "decompose this story", "create
  work items", "technical tasks for this story", "what tickets do we need".
metadata:
  author: Javier Camarena
  version: 1.0.0
---

# Ticket Generator

Generate detailed, actionable work tickets from user stories for sprint
planning. Tickets follow the TK-NNN format and are traceable back to their
source US-NNN user story.

Tickets are the atomic unit of work in a sprint: each one should be completable
by one person in one to two days. They translate the "what and why" of a user
story into the "how" that the development team actually executes.

## Input / Output

**Input**: One or more user stories (`user-stories/US-NNN-slug.md`) with acceptance criteria. Optionally accepts an effort estimation table (layer points per story) to pre-populate ticket sizes.

**Output**: One file per ticket saved as `tickets/TK-NNN-slug.md`. The `## Tasks` section of each source US-NNN story is updated with checkboxes linking to the generated tickets.

## Ticket Types

| Type | When to use |
|------|-------------|
| **Feature** | Implements user-facing functionality. Linked to a US-NNN. |
| **Technical Task** | Infrastructure work, refactoring, CI/CD, migrations. No direct user value but enables features. |
| **Bug** | A known defect that needs fixing. References the broken behaviour. |
| **Improvement** | Enhances an existing feature based on user feedback or metrics. |
| **Spike** | Time-boxed research or experimentation. Always produces a document or decision, never production code. **Split into two tickets**: Spike (research) + Feature/Task (implementation). |

## Process

1. **Read the source story**: Open the US-NNN file. Read the acceptance
   criteria and context notes carefully — they define the scope of the tickets.

2. **Read related context**: If a PRD or story map exists, read them to
   understand technical constraints, non-functional requirements, and sprint
   priority.

3. **Ask clarifying questions** before generating:
   - Which sprint or iteration is this for?
   - What is the team's tech stack? (affects how to split frontend/backend)
   - Is there a known assignee or team per layer? (backend, frontend, QA, DevOps)
   - Are there known technical risks or unknowns that require a Spike?
   - Should QA validation be a separate ticket or part of each Feature ticket?
   - What is the ID of the last existing ticket? (to auto-increment TK-NNN)

4. **Identify ticket types needed**: Not every story needs all types. A simple
   UI story may only need one Feature + one QA ticket. A story touching auth
   may need a Spike first.

5. **Generate tickets**: Follow the template in
   [references/ticket-template.md](references/ticket-template.md). Use the
   right template per ticket type.

6. **Check coverage**: Every acceptance criterion in the source story must be
   covered by at least one ticket's acceptance criteria. If a criterion has no
   ticket, add one.

7. **Save tickets**: Save each ticket as `tickets/TK-NNN-slug.md`. Update the
   `## Tasks` section of the source US-NNN story file with a checklist
   referencing each generated ticket.

8. **Suggest next steps**: If a Spike was generated, remind the user to
   schedule the implementation ticket only after the Spike is resolved.

## Decomposition Guidelines

1. **One layer per ticket**: Each ticket covers exactly one of the 8 canonical
   layers. A ticket touching multiple layers is too large. Only generate
   tickets for layers the story actually requires.

   | Layer | Estimate when the story requires… |
   |-------|-----------------------------------|
   | **BackendAPI** | New or modified REST/GraphQL endpoints, service logic |
   | **Database** | Schema changes, migrations, new indexes, query changes |
   | **Frontend** | New UI components, views, client-side state |
   | **Auth** | New permissions, role checks, token handling |
   | **Async** | Background jobs, queues, event consumers, webhooks |
   | **Infra** | New env vars, cloud resources, secrets, feature flags |
   | **CI/CD** | Pipeline changes, build steps, deployment config |
   | **Testing** | Integration/E2E tests, QA validation, test data setup |

2. **Spikes have a timebox**: Always specify a maximum duration (e.g., 2 days).
   If the answer is not found in time, the team decides with available info.
3. **Bugs need reproduction steps**: A bug ticket without steps to reproduce
   is not actionable. Do not generate a bug ticket without them.
4. **Technical Tasks are not optional**: Infrastructure, migrations, and
   configuration tickets must exist if the feature depends on them. Do not
   embed them silently inside a Feature ticket.
5. **QA is explicit**: If acceptance testing is expected, create a dedicated
   Testing-layer ticket. Do not assume it is implicit.
6. **Effort via Planning Poker (Fibonacci)**: Estimate each ticket
   independently using the Fibonacci scale. Estimate per layer, not per story;
   sum across layers for the story total. Any implementation ticket at 8+
   points must be split before sprint commitment.

   | Points | Signal | Typical example |
   |--------|--------|-----------------|
   | **1** | Trivial | Config flag, copy change, single-column migration |
   | **2** | Small | Simple CRUD endpoint, basic read-only component |
   | **3** | Medium | Multi-step validation, form with state, auth middleware |
   | **5** | Large | Complex business rule, multi-table migration, full screen |
   | **8** | Must split | Spike max; flag for decomposition before sprint commit |

7. **Traceability is mandatory**: Every Feature ticket must reference its
   source US-NNN. Every Technical Task must reference the Feature or US-NNN
   it enables.

## Priority Scale

| Level | Meaning |
|-------|---------|
| **Critical** | Blocks the sprint goal or other tickets. Must be done first. |
| **High** | Core to the story's acceptance criteria. Sprint fails without it. |
| **Medium** | Improves quality or coverage but story can ship without it. |
| **Low** | Nice-to-have. Defer if time pressure arises. |

## Output

After generating all tickets:
1. List all tickets in a summary table: `TK-NNN | Type | Title | Points | Priority`
2. Update the `## Tasks` section of the source US-NNN story file with checkboxes linking to each ticket
3. Inform the user of all saved file paths
