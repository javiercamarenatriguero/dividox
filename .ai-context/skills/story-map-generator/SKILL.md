---
name: story-map-generator
description: >
  Generate a User Story Map organizing user stories visually by activity flow
  and priority, with release cut lines. Use when asked to create a story map,
  organize existing user stories, plan releases or MVP scope, or visualize the
  full user journey. Triggers on phrases like "story map", "user story map",
  "map the user journey", "plan the MVP", "organize backlog", "what goes in
  the MVP".
metadata:
  author: Javier Camarena
  version: 1.0.0
---

# Story Map Generator

Generate a User Story Map following Jeff Patton's technique: a two-dimensional
view of user stories organized by the activities users perform (horizontal
backbone) and by priority within each activity (vertical axis), with horizontal
cut lines defining releases.

The story map answers the question the flat backlog cannot: "What is the
minimum we need to build so the user can complete their journey end-to-end?"

## What a Story Map Is

```
BACKBONE    [Activity 1]   [Activity 2]   [Activity 3]   [Activity 4]
            ─────────────────────────────────────────────────────────
MVP         US-001         US-003         US-005         US-007
            US-002                        US-006
            ─────────────────────────────────────────────────────────
v1.1        US-008         US-009         US-010
            ─────────────────────────────────────────────────────────
v2.0        US-011         US-012         US-013         US-014
```

- **Backbone** (horizontal): The high-level activities a user performs, in
  chronological order. These are often epic-level actions, not stories.
- **Stories** (vertical under each activity): Ordered top-to-bottom by priority.
  Higher rows = more essential to the user journey.
- **Release cuts** (horizontal lines): Each band represents a release or
  iteration. Stories above the first cut are in the MVP.

## Input / Output

**Input**: A product idea, feature description, existing epics, or a list of user stories in any format.

**Output**: A story map saved as `story-maps/SM-NNN-slug.md` — backbone activities, stories per activity ordered by priority, and horizontal release cut lines defining the MVP and subsequent releases.

## Process

1. **Read context**: If a design document, PRD, or existing user stories exist,
   read them first. The map should not contradict or duplicate existing work.

2. **Ask before mapping**: Do not generate a map with missing information. Ask:
   - What product or feature are we mapping? What is its goal?
   - Who are the primary users or personas?
   - What is the main user journey — from first touch to completing their goal?
   - Do you have existing epics or stories to organize, or do we generate them
     from scratch?
   - What is the scope of the MVP? Is there a known constraint (timeline,
     budget, team size)?
   - How many releases should the map cover?

3. **Build the backbone first**: Identify 4–8 backbone activities that cover
   the full user journey chronologically. Present them to the user for
   validation before writing stories. Activities should be verb phrases
   (e.g., "Search for candidates", "Schedule interview", "Send offer").

4. **Generate stories**: Under each activity, write user stories from most to
   least essential. Use the US-NNN format. Write just enough detail
   (title + one-liner) for the map; the user-story-writer skill handles full
   story development.

5. **Draw release cuts**: Group stories into releases using the 7 prioritization
   factors (business value, urgency, dependencies, implementation cost, risk,
   user feedback, tech maturity) — see
   [references/prioritization.md](references/prioritization.md).
   The MVP cut should contain only the stories that enable a user to complete
   the journey end-to-end — even if imperfectly. Subsequent releases add depth
   and polish. If the user needs a prioritization table for the full story
   index, generate one using the AI prompt in that reference file.

6. **Save the map**: Save as `story-maps/SM-NNN-feature-slug.md`. Inform the
   user of the path and suggest next steps (flesh out stories, generate PRD).

## Writing Guidelines

1. **Backbone = user vocabulary**: Activities should use words the user would
   use, not technical terms. "Pay for order" not "process transaction".
2. **MVP = thin slice, not thin product**: The MVP cut must enable an
   end-to-end journey, even if stripped down. A map where the MVP only covers
   the first two activities is not a valid MVP — it is a partial feature.
3. **Depth = value, not complexity**: Stories lower on the vertical axis add
   value but are not blocking. They are not less important — they are deferred.
4. **Gaps are insights**: If an activity has very few stories or a release cut
   feels unbalanced, that is a signal to investigate, not paper over.
5. **Keep stories atomic**: Each cell in the map should represent one US-NNN
   story, not a bundle. If a cell needs multiple stories, add rows.
