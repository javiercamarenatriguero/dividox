---
name: estimate-effort
description: >
  Estimate development effort for user stories and sprint backlogs using
  Planning Poker (Fibonacci) broken down by technology layer, combined with
  MoSCoW prioritization. Use when asked to estimate stories, size a sprint,
  produce a capacity plan, or run a planning session. Triggers on: "estimate
  this story", "how many points", "size the backlog", "sprint capacity",
  "planning poker", "effort for US-NNN", "capacity planning", "estimate the
  sprint", "story points", "t-shirt size", "planning session".
---

# Planning Poker

Produce structured effort estimates for user stories and sprint backlogs.
Estimation is broken down by technology layer (what needs to change) and
sized using the Fibonacci Planning Poker scale. MoSCoW priority determines
the order in which work should be estimated and scheduled.

## Input / Output

**Input**: One or more user stories (US-NNN files or inline descriptions), or a full sprint backlog. Accepts a single story, a list of IDs, or a story map band.

**Output**: Estimation tables — layer breakdown per story and a sprint summary table — produced inline in the conversation or saved alongside the story files.

## Process

1. **Gather input**: Identify the stories to estimate. Accept a single US-NNN,
   a list, or a sprint backlog. If none provided, ask the user.

2. **Read each story**: Open the US-NNN file. Read acceptance criteria,
   Context & Notes, and Related Stories to understand full scope.

3. **Apply MoSCoW to scope estimation**: Before sizing, classify each story:

   | Level | Estimation action |
   |-------|-------------------|
   | **Must have** | Estimate all layers in full — these ship regardless |
   | **Should have** | Estimate all layers — include if capacity allows |
   | **Could have** | High-level estimate only (total points, no layer breakdown) |
   | **Won't have** | Skip estimation — record as deferred with reason |

4. **Identify layers touched**: For each Must/Should story, determine which
   of the 8 canonical layers are required. Only estimate layers the story
   actually touches — not all stories need all layers.

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

5. **Estimate each layer (Planning Poker)**:

   Use the Fibonacci scale. Estimate each layer independently — do not
   anchor on the total. See [references/estimation-guide.md](references/estimation-guide.md)
   for calibration examples per layer.

   | Points | Signal | Typical example |
   |--------|--------|-----------------|
   | **1** | Trivial | Config flag, copy change, single-column migration |
   | **2** | Small | Simple CRUD endpoint, basic read-only component |
   | **3** | Medium | Multi-step validation, form with state, auth middleware |
   | **5** | Large | Complex business rule, multi-table migration, full screen |
   | **8** | Must split | Spike max; flag for decomposition before sprint commit |
   | **13** | Epic-level | Story itself is too large — split the story first |

   If disagreement arises between estimates for the same layer, surface the
   assumption gap (e.g., "Do we cache this?" or "Is SSR required?") and
   re-estimate after resolving it.

6. **Calculate totals**: Sum layer estimates per story. Sum story totals for
   the sprint. Compare against team velocity (if known).

7. **Flag risks**: Mark any layer estimate of 8+ as a split candidate.
   Mark any story where Auth or Infra layers appear unexpectedly — these
   often indicate undiscovered cross-cutting concerns.

8. **Output the estimation table**: See Output section below.

## MoSCoW Integration

MoSCoW applies at two levels:

**Story level** (from the US-NNN `Priority` field):
- Drives which stories get full layer breakdowns vs. high-level estimates.
- Must-have stories are estimated first; schedule won't-have stories last.

**Layer level** (assigned during estimation):
- Within a story, some layers are must-have (e.g., BackendAPI for an API
  story) and some are could-have (e.g., CI/CD if pipelines already handle it).
- Label each layer ticket with its MoSCoW level so the team can cut scope
  per layer if velocity is lower than expected.

## Output

Produce the estimation in three parts:

### Part 1 — Layer Breakdown per Story

For each Must/Should story, one table:

```
## US-NNN: [Story Title] · Must have · [Total] pts

| Layer | Estimate | MoSCoW | Notes |
|-------|----------|--------|-------|
| BackendAPI | 3 | Must have | Scoring endpoint + explanation payload |
| Database | 2 | Must have | Add score_history JSONB column |
| Frontend | 3 | Must have | Side panel component + breakdown table |
| Auth | — | — | No new permission required |
| Async | — | — | Scoring runs sync for now |
| Infra | 1 | Could have | Feature flag for beta rollout |
| CI/CD | — | — | No pipeline changes |
| Testing | 2 | Must have | Integration test for score API |
| **Total** | **11** | | |
```

### Part 2 — Sprint Summary Table

```
## Sprint Estimation Summary

| ID | Title | Priority | BackendAPI | Database | Frontend | Auth | Async | Infra | CI/CD | Testing | Total |
|----|-------|----------|-----------|----------|----------|------|-------|-------|-------|---------|-------|
| US-001 | Ranked list | Must have | 3 | 2 | 3 | — | — | 1 | — | 2 | **11** |
| US-002 | Weight config | Must have | 3 | 1 | 2 | — | 2 | — | — | 2 | **10** |
| US-005 | HM view | Should have | — | — | 3 | 1 | — | — | — | 1 | **5** |
| **Total** | | | | | | | | | | | **26** |

Team velocity: [N] pts/sprint · Capacity delta: [+N / −N]
```

### Part 3 — Risks and Recommendations

List any stories or layers flagged for splitting, any Auth/Infra surprises,
and a go/no-go recommendation per story against sprint capacity.
