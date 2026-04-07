# Backlog Prioritization Guide

Use this framework when assigning or justifying the priority of a user story.
Prioritization is a decision, not a guess — it must be explainable.

---

## 7 Prioritization Factors

Score each story against these factors before assigning a priority level.
When using AI to generate a prioritization table, request all 7 dimensions.

| Factor | What to assess |
|---|---|
| **Business value** | Impact on user retention, new user acquisition, and revenue potential |
| **Urgency** | Market timing, competitor moves, or committed stakeholder deadlines |
| **Dependencies** | Does other work block on this? Prioritize stories that unblock others |
| **Implementation cost** | Effort, team resources, and time required — favor best cost/benefit ratio |
| **Risk** | Technical uncertainty, integration complexity, or regulatory exposure |
| **User feedback** | Direct user requests, pain points from research, NPS signals |
| **Tech maturity** | How proven is the underlying technology? Higher risk = consider deferring |

---

## Priority Levels (MoSCoW)

| Level | Meaning |
|---|---|
| **Must have** | Required for the release to be viable — without it, the journey breaks |
| **Should have** | High value, not critical for MVP — include if capacity allows |
| **Could have** | Nice to have — deferred without impacting core value |
| **Won't have** | Explicitly out of scope for this iteration (document why) |

---

## AI Prompt for Prioritization Table

Use this prompt to generate a prioritization table from a set of user stories:

```
Given the following user stories for [product/feature]:

[paste stories]

Generate a prioritization table in markdown with one row per story and these
columns:
- User story ID and title
- Business value (High / Med / Low) with one-line justification
- Urgency (High / Med / Low) with one-line justification
- Implementation complexity (High / Med / Low)
- Key risks or dependencies
- Suggested priority (Must have / Should have / Could have / Won't have)

Base your assessment on typical market context for this type of product.
Flag any story where the justification is uncertain and needs team discussion.
```

> Always review the AI output critically. The table is a starting point for
> the team conversation, not a replacement for it.

---

## Prioritization Process

1. **Stakeholder review**: Involve key stakeholders to ensure the backlog
   reflects business objectives, not just technical preferences.

2. **Team session**: Use Planning Poker to estimate complexity, or the
   Eisenhower matrix (urgent/important quadrants) to sort stories. Engineers'
   input on cost and risk is as important as the PM's view on value.

3. **Reevaluate regularly**: The backlog is dynamic. Reprioritize at the start
   of each sprint or when new data (user feedback, market shifts) arrives.

---

## Output: Prioritization Table

When generating a prioritized backlog, produce this table alongside the stories:

| ID | Title | Business Value | Urgency | Complexity | Risk / Dependencies | Priority |
|---|---|---|---|---|---|---|
| US-001 | [title] | High — [reason] | Med — [reason] | Low | None | Must have |
| US-002 | [title] | Med — [reason] | Low — [reason] | High | Depends on US-001 | Should have |
