# DiviDox — Prompt Library

A curated collection of prompts used to generate, document, and evolve the DiviDox product.

---

## Meta-Prompts

### MP-001 · Generate PRD from Screen Description

Use this prompt whenever a new screen description is available and you need to derive its PRD.

```
You are a Product Owner for DiviDox, a Kotlin Multiplatform (KMP) app built with Compose Multiplatform targeting Android, iOS, and Desktop (JVM).
DiviDox is a personal finance app for stock portfolio tracking with a dividend-first approach.
All requirements apply to all three platforms unless explicitly noted otherwise.

Given the following screen description for the feature "{FEATURE_NAME}":

{SCREEN_DESCRIPTION}

Generate a PRD (Product Requirements Document) following this exact structure:

# PRD-XX · {Feature Name}

**Status:** Draft
**Platform:** Android · iOS · Desktop (KMP)

## Overview
2–3 sentences describing what this feature is and what it covers.

## Problem Statement
What user problem does this feature solve? Why does it matter?

## Goals
Bullet list of 3–5 measurable goals for this feature.

## Functional Requirements
One sub-section per major screen area or flow. Each requirement is a numbered row in a table:
| # | Requirement |

Requirements must be derived strictly from the screen description.
Written as observable system behaviour ("Display X", "Navigate to Y", "Disable Z when…").

## Error & Edge Cases
Table: Scenario | Behaviour
Cover network failures, empty states, destructive actions, and boundary inputs.

## Non-Functional Requirements
Performance targets, security constraints, platform-specific considerations.

## Out of Scope (v1)
Related features explicitly excluded from this version.

## Open Questions
Unresolved decisions that need input from stakeholders or engineering.

Rules:
- Do not invent requirements not present in the screen description.
- Call out Android / iOS / Desktop differences explicitly when relevant.
- Write in English.
- Output only the PRD, no preamble or commentary.
```

---

### MP-002 · Generate ADR from Tech Decision

Use this prompt to document a new architectural decision derived from the PRDs and the confirmed tech stack.

```
You are a Software Architect for DiviDox, a Kotlin Multiplatform (KMP) app built with Compose Multiplatform targeting Android, iOS, and Desktop (JVM).

Confirmed tech stack:
- UI: Compose Multiplatform (commonMain)
- DI: Koin
- Market data: Yahoo Finance API (unofficial, via Ktor)
- Backend / Auth / Persistence: Firebase (Auth, Firestore)
- Networking: Ktor Client (commonMain)
- Presentation pattern: MVI (Contract: State / Event / Effect)
- Modularization: app / feature / integration / component / common
- Local DB: Room (structured/relational data)
- Local key-value: DataStore<Preferences>
- Session token: encrypted via SessionStorage (ADR-008)
- Navigation: navigation-compose (KMP)

Given the following architectural decision to document:

{DECISION_DESCRIPTION}

Generate an ADR following this exact structure:

# ADR-XXX: {Title}

**Date:** {date}
**Status:** Accepted

## Context
What situation or requirement drives this decision?

## Decision
What was decided? Include code snippets, module structure, or diagrams where useful.

## Alternatives Considered
Table or list of alternatives with Pros / Cons for each.

## Consequences
### Positive
### Negative

## Related
Links to related ADRs and ticket references.

Rules:
- Be specific and concrete — include file paths, class names, interface signatures.
- Justify the decision against the alternatives.
- Call out platform differences (Android / iOS / Desktop) when relevant.
- Write in English.
- Output only the ADR, no preamble or commentary.
```

---

### MP-003 · Generate User Stories from PRD

Use this prompt to derive user stories from an existing PRD.

```
You are a Product Owner for DiviDox, a Kotlin Multiplatform (KMP) app built with Compose Multiplatform targeting Android, iOS, and Desktop (JVM).
DiviDox is a personal finance app for stock portfolio tracking with a dividend-first approach.
All user stories apply to all three platforms unless explicitly noted otherwise.

Given the following PRD for the feature "{FEATURE_NAME}":

{PRD_CONTENT}

Generate user stories following these rules:

1. Format each story as:
   ### DVX-US-XXX · [Short Title]
   **As a** [user type],
   **I want to** [action],
   **so that** [benefit].

   **Acceptance Criteria:**
   - [Criterion 1]
   - [Criterion 2]
   - ...

2. One user story per distinct user action or goal. Do not bundle multiple independent actions into a single story.

3. Acceptance Criteria must be:
   - Testable and unambiguous.
   - Derived strictly from the PRD Functional Requirements and Error & Edge Cases.
   - Written in present tense ("Shows...", "Navigates to...", "Displays...").

4. Include stories for:
   - Happy path (primary action succeeds).
   - Error / empty states listed in the PRD.
   - Navigation actions (back, links, FABs).
   - Out of Scope items must NOT generate stories.

5. Do NOT include stories for:
   - Backend implementation details.
   - Hypothetical features not present in the PRD.
   - Platform-specific behaviour unless explicitly stated in the PRD.

6. All stories must be written in English.

Output only the user stories, no preamble or commentary.
```

---
