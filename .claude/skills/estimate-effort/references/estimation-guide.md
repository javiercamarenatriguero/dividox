# Estimation Calibration Guide

Reference examples for estimating each technology layer using Fibonacci
Planning Poker. Use these to anchor team discussion and resolve disagreements.

---

## Table of Contents

1. [Fibonacci Scale Reference](#fibonacci-scale-reference)
2. [Layer-by-Layer Calibration Examples](#layer-by-layer-calibration-examples)
3. [Planning Poker Facilitation](#planning-poker-facilitation)
4. [MoSCoW × Layer Decision Matrix](#moscow--layer-decision-matrix)
5. [Common Estimation Traps](#common-estimation-traps)

---

## Fibonacci Scale Reference

| Points | Effort signal | Max duration (1 dev) | Split threshold |
|--------|---------------|----------------------|-----------------|
| 1 | Trivial — near-zero risk | < 2 hours | Never |
| 2 | Small — well-understood | Half day | Never |
| 3 | Medium — some complexity | 1 day | Consider if 2 unknowns |
| 5 | Large — multiple concerns | 2 days | If touching > 1 sub-system |
| 8 | Oversized | 3–4 days | Always split before committing |
| 13 | Epic — story too large | > 1 week | Split the story, not the ticket |

> **Rule**: If two team members disagree by more than one Fibonacci step
> (e.g., one votes 2, another votes 5), surface the assumption and re-vote.
> Disagreement = hidden complexity or hidden simplicity — both are valuable.

---

## Layer-by-Layer Calibration Examples

### BackendAPI

| Points | Example |
|--------|---------|
| 1 | Add a field to an existing response payload |
| 2 | New read-only GET endpoint with simple query |
| 3 | POST endpoint with validation, business rule, and error responses |
| 5 | Multi-step orchestration across two services with rollback logic |
| 8 | Stateful workflow engine or complex aggregation — split first |

Key questions: Does it need transactions? Does it call external APIs?
Does it require a new service class or can it extend an existing one?

---

### Database

| Points | Example |
|--------|---------|
| 1 | Add a nullable column to an existing table |
| 2 | New index; or rename a column with a safe migration |
| 3 | New table with FK relationships; or backfill migration on a small table |
| 5 | Schema change requiring data migration on a large table (> 1M rows) |
| 8 | Cross-table restructuring or sharding concern — flag as Spike candidate |

Key questions: How large is the affected table? Is zero-downtime migration
required? Does the change affect existing queries?

---

### Frontend

| Points | Example |
|--------|---------|
| 1 | Update label text or color in an existing component |
| 2 | New read-only display component wired to existing data |
| 3 | Interactive form with validation, loading state, and error handling |
| 5 | Full page/view with multiple components, client-side filtering, and pagination |
| 8 | Complex real-time UI (WebSocket, live updates, optimistic updates) — split |

Key questions: Is it a new route or extending an existing one? Does it
require new shared components? Is mobile responsiveness a requirement?

---

### Auth

| Points | Example |
|--------|---------|
| 1 | Add a role check to an existing guard/middleware |
| 2 | New permission flag with UI visibility toggle |
| 3 | New role with a custom permission set applied across multiple endpoints |
| 5 | Multi-tenant isolation logic or OAuth provider integration |
| 8 | Full auth provider swap or zero-trust architecture change — Spike first |

Key questions: Does it require changes to the token/session model?
Does it affect existing users' access? Is it org-scoped or global?

---

### Async

| Points | Example |
|--------|---------|
| 1 | Add a field to an existing job payload |
| 2 | New simple background job with no external dependencies |
| 3 | Queue consumer with retry logic and dead-letter handling |
| 5 | Event-driven workflow across multiple consumers with ordering guarantees |
| 8 | Distributed saga or compensating transactions — Spike first |

Key questions: Does it need exactly-once delivery? What is the failure
mode? Is there a monitoring/alerting requirement?

---

### Infra

| Points | Example |
|--------|---------|
| 1 | Add an environment variable to existing config |
| 2 | New feature flag in LaunchDarkly/equivalent |
| 3 | New cloud resource (S3 bucket, SQS queue) with IAM policy |
| 5 | Multi-region resource setup or VPC peering |
| 8 | New cluster, service mesh, or compliance boundary — Spike first |

Key questions: Does it require a new cloud account or region?
Is it gated by a security review?

---

### CI/CD

| Points | Example |
|--------|---------|
| 1 | Add an env variable to the pipeline |
| 2 | Add a new build step to an existing pipeline |
| 3 | New pipeline stage (e.g., staging deploy, smoke test gate) |
| 5 | Multi-environment promotion workflow with approval gates |
| 8 | Full pipeline rewrite or migration to a new CI platform — Spike first |

Key questions: Does it block other teams' deploys? Does it require
new secrets management? Is a rollback mechanism needed?

---

### Testing

| Points | Example |
|--------|---------|
| 1 | Add an assertion to an existing test |
| 2 | Unit tests for a new service method or utility |
| 3 | Integration test for a new endpoint (real DB, no mocks) |
| 5 | E2E test covering a full user flow across multiple screens |
| 8 | Full QA regression suite for a new epic — split by scenario group |

Key questions: Is it unit, integration, or E2E? Does it need test data
seeding? Is there a performance or load testing requirement?

---

## Planning Poker Facilitation

Use this flow when running an async or synchronous estimation session:

1. **Present the story**: Read the story title, the two-line summary, and
   the acceptance criteria aloud (or share in chat). Do not share estimates yet.

2. **Identify layers**: Agree as a team which of the 8 layers are in scope
   for this story before estimating. Remove layers everyone agrees are N/A.

3. **Vote simultaneously**: Each participant reveals their estimate for one
   layer at the same time (cards, emoji, or async poll). No anchoring.

4. **Surface disagreements**: If votes differ by more than one step:
   - The highest voter explains what complexity they see
   - The lowest voter explains their simplifying assumption
   - Re-vote after the discussion (usually converges in one round)

5. **Record and move on**: Accept the consensus estimate. If stuck after
   two rounds, take the higher estimate and add a risk note.

6. **Time-box**: Allocate 3–5 minutes per layer per story. A 10-story sprint
   with 4 layers average = ~2–3 hours total. Use async voting tools
   (e.g., PlanningPoker.com, Jira poker) for distributed teams.

---

## MoSCoW × Layer Decision Matrix

Use this matrix when sprint capacity is tight and the team must cut scope:

| Story priority | Layer priority | Decision |
|----------------|----------------|----------|
| Must have | Must have | Commit — non-negotiable |
| Must have | Could have | Cut from sprint; add to backlog |
| Should have | Must have | Include if capacity allows; defer story if not |
| Should have | Could have | First to cut when capacity is tight |
| Could have | Any | High-level estimate only; skip layer breakdown |
| Won't have | Any | Do not estimate; record deferral reason |

**Practical rule**: When total sprint estimate exceeds velocity by > 20%,
cut Could-have layers from Should-have stories first, then defer entire
Should-have stories before touching Must-have scope.

---

## Common Estimation Traps

| Trap | Symptom | Fix |
|------|---------|-----|
| **Anchoring** | First voter's number pulls the group | Always vote simultaneously |
| **Gold-plating** | Estimates inflate with nice-to-haves | Enforce "what does Done require, not what would be nice" |
| **Ignoring testing** | Testing layer always gets 1 | Ask "what scenarios need an integration test?" before voting |
| **Auth blindspot** | Auth layer marked N/A when it isn't | For every new endpoint, ask "who can call this?" |
| **Infra assumed free** | New cloud resource not estimated | Check: does this story introduce any new infrastructure dependency? |
| **Splitting too late** | 8-point tickets enter the sprint | Flag 8s during estimation, not during standup |
