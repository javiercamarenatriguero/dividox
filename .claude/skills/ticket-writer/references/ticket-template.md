# Ticket Templates

Use the appropriate template based on ticket type. All tickets share the same
header structure; the body varies by type.

---

## Common Header (all types)

```markdown
# TK-NNN: [Clear, action-verb title — e.g., "Implement JWT refresh token endpoint"]

**Type**: Feature | Technical Task | Bug | Improvement | Spike
**Source**: [US-NNN: Story title] | N/A (for standalone technical tasks)
**Sprint**: [Sprint number or name, e.g., Sprint 10]
**Priority**: Critical | High | Medium | Low
**Estimate**: [1 | 2 | 3 | 5 | 8] points
**Assigned to**: [Team or person, e.g., Backend Team / @username]
**Labels**: [e.g., backend, auth, security, sprint-10]
**Status**: To Do
```

---

## Feature Ticket

Use for user-facing functionality that implements part of a user story.

```markdown
# TK-NNN: [Title]

**Type**: Feature
**Source**: US-NNN: [Story title]
**Sprint**: [Sprint]
**Priority**: [Level]
**Estimate**: [Points]
**Assigned to**: [Team/person]
**Labels**: [tags]
**Status**: To Do

---

## Description

**Purpose**: [Why this ticket exists — what user need or story criterion it fulfills.]

**Scope**: [What exactly must be built. Be specific: which endpoint, which UI component,
which data field. Include constraints or technical requirements agreed during refinement.]

**Out of scope**: [What explicitly is NOT part of this ticket, to prevent scope creep.]

---

## Acceptance Criteria

> Derived from the source user story's scenarios. Each criterion must be testable.

- [ ] [Criterion 1 — specific and verifiable]
- [ ] [Criterion 2]
- [ ] [Criterion 3]

---

## Links & References

- **User Story**: [US-NNN file path or link]
- **PRD**: [PRD-NNN file path, if applicable]
- **Design**: [Figma or design file link, if applicable]
- **Related tickets**: [TK-NNN, TK-NNN]
- **ADR**: [ADR-NNN, if an architectural decision was made for this ticket]

---

## Notes & Comments

[Space for team discussion, open questions, implementation hints, or spike results
that informed this ticket. Do not write implementation code here.]

---

## Change History

| Date | Author | Change |
|------|--------|--------|
| YYYY-MM-DD | [name] | Created |
```

---

## Technical Task Ticket

Use for infrastructure, refactoring, CI/CD, migrations, configuration, or any
work that has no direct user-visible output but enables features.

```markdown
# TK-NNN: [Title — e.g., "Configure Redis cache for session storage"]

**Type**: Technical Task
**Source**: US-NNN: [Story it enables] | [Feature ticket TK-NNN it enables]
**Sprint**: [Sprint]
**Priority**: [Level]
**Estimate**: [Points]
**Assigned to**: [Team/person]
**Labels**: [e.g., infrastructure, devops, refactoring]
**Status**: To Do

---

## Description

**Purpose**: [What technical problem this solves and why it is needed now.]

**Work to be done**:
1. [Step or sub-task 1]
2. [Step or sub-task 2]
3. [Step or sub-task 3]

**Definition of Done**:
- [ ] [Verifiable outcome 1 — e.g., "All existing tests pass after refactor"]
- [ ] [Verifiable outcome 2 — e.g., "CI pipeline runs in < 3 minutes"]
- [ ] [Verifiable outcome 3]

---

## Links & References

- **Enables**: [TK-NNN Feature tickets that depend on this task]
- **Related**: [ADR-NNN if a decision was made, PRD-NNN for requirements context]
- **Documentation**: [Links to relevant docs or runbooks]

---

## Notes & Comments

[Open questions, risks, or known constraints for this task.]

---

## Change History

| Date | Author | Change |
|------|--------|--------|
| YYYY-MM-DD | [name] | Created |
```

---

## Bug Ticket

Use for known defects or regressions. Always requires reproduction steps.

```markdown
# TK-NNN: [Title — describe the broken behaviour, e.g., "Login fails for users with '+' in email"]

**Type**: Bug
**Source**: [US-NNN if linked to a story] | [Reported by: user/QA/monitoring]
**Sprint**: [Sprint]
**Priority**: [Level — bugs blocking users are usually Critical or High]
**Estimate**: [Points]
**Assigned to**: [Team/person]
**Labels**: [e.g., bug, auth, regression, sprint-10]
**Status**: To Do

---

## Description

**Expected behaviour**: [What should happen.]

**Actual behaviour**: [What actually happens — be specific, include error messages if any.]

**Impact**: [Who is affected and how severely. e.g., "All users attempting login with special characters in email receive a 500 error."]

---

## Steps to Reproduce

1. [Step 1]
2. [Step 2]
3. [Step 3]
4. Observe: [what you see]

**Environment**: [Browser, OS, version, staging/prod, etc.]

---

## Acceptance Criteria

- [ ] The bug no longer occurs following the reproduction steps above
- [ ] [Additional regression test criterion]
- [ ] No existing tests were broken by the fix

---

## Links & References

- **Related story**: [US-NNN if applicable]
- **Error logs**: [Link to logs, Sentry, Datadog, etc.]
- **Related tickets**: [TK-NNN]

---

## Notes & Comments

[Root cause hypothesis, relevant code area, or known workaround.]

---

## Change History

| Date | Author | Change |
|------|--------|--------|
| YYYY-MM-DD | [name] | Created |
```

---

## Improvement Ticket

Use for enhancements to existing, working features based on user feedback,
metrics, or product review. Not a bug — the feature works, but can be better.

```markdown
# TK-NNN: [Title — e.g., "Improve candidate search response time from 2s to <500ms"]

**Type**: Improvement
**Source**: [US-NNN if linked] | [Origin: user feedback / analytics / product review]
**Sprint**: [Sprint]
**Priority**: [Level]
**Estimate**: [Points]
**Assigned to**: [Team/person]
**Labels**: [e.g., performance, ux, improvement]
**Status**: To Do

---

## Description

**Current behaviour**: [How the feature works today.]

**Desired improvement**: [What should change and why. Include metrics or user
feedback that motivate the change.]

**Success metric**: [How we will know the improvement is done. Use a measurable
target: e.g., "p95 response time < 500ms", "task completion rate > 80%".]

---

## Acceptance Criteria

- [ ] [Criterion 1 — measurable]
- [ ] [Criterion 2]
- [ ] Existing functionality is not degraded

---

## Links & References

- **Original story**: [US-NNN]
- **User feedback source**: [Support ticket, survey, session recording, etc.]
- **Analytics**: [Link to dashboard or metric]

---

## Notes & Comments

[Ideas for implementation approach, constraints to be aware of.]

---

## Change History

| Date | Author | Change |
|------|--------|--------|
| YYYY-MM-DD | [name] | Created |
```

---

## Spike Ticket

Use for time-boxed research or experimentation where the implementation path
is unknown. A Spike never produces production code — only a documented
finding and a recommendation.

**Always create two tickets**: the Spike (research) and the follow-up Feature
or Technical Task (implementation). The follow-up ticket is created but
blocked by the Spike until it is resolved.

```markdown
# TK-NNN: [Title — e.g., "Spike: Evaluate SMS providers for 2FA (Twilio vs AWS SNS)"]

**Type**: Spike
**Source**: US-NNN: [Story that requires this research]
**Sprint**: [Sprint]
**Priority**: [Level — usually High, since follow-up work is blocked]
**Estimate**: [1 | 2 | 3] points (Spikes are time-boxed; max 2–3 days)
**Timebox**: [e.g., 2 days — if no conclusion by then, present best available info]
**Assigned to**: [Team/person]
**Labels**: [e.g., spike, research, auth]
**Status**: To Do
**Blocks**: [TK-NNN: follow-up implementation ticket]

---

## Description

**Question to answer**: [The specific question or unknown this Spike must resolve.
Be precise — a Spike with a vague question produces a vague answer.]

**Why we don't know yet**: [What makes this uncertain: new technology, competing
options, unknown performance characteristics, compliance requirements, etc.]

**Timebox**: [Maximum duration. If the answer is not found, the team decides
with whatever information is available at the end of the timebox.]

---

## Research Scope

What to investigate:
- [ ] [Specific aspect 1 to evaluate]
- [ ] [Specific aspect 2 to evaluate]
- [ ] [Specific aspect 3 to evaluate]

What NOT to investigate (out of scope for this Spike):
- [Explicitly excluded item — prevents scope creep]

---

## Expected Output

By the end of this Spike, the assignee must produce:
- [ ] A written summary of findings (saved to `spikes/TK-NNN-findings.md`)
- [ ] A recommendation with rationale
- [ ] An updated estimate for the follow-up implementation ticket (TK-NNN)
- [ ] An ADR if an architectural decision was reached (ADR-NNN)

---

## Links & References

- **Source story**: [US-NNN]
- **Follow-up ticket**: [TK-NNN — blocked until Spike is resolved]
- **Reference material**: [Relevant docs, vendor pages, prior art]

---

## Notes & Comments

[Known constraints that should inform the research direction.]

---

## Change History

| Date | Author | Change |
|------|--------|--------|
| YYYY-MM-DD | [name] | Created |
| YYYY-MM-DD | [name] | Spike resolved — findings in spikes/TK-NNN-findings.md |
```

---

## Summary Table (generate after creating all tickets)

Include this at the end of your output to give the user a quick overview:

```markdown
## Sprint Ticket Summary — US-NNN: [Story Title]

| Ticket | Type | Title | Points | Priority | Assigned to |
|--------|------|-------|--------|----------|-------------|
| TK-001 | Feature | [title] | 3 | High | Backend Team |
| TK-002 | Feature | [title] | 2 | High | Frontend Team |
| TK-003 | Technical Task | [title] | 1 | Critical | DevOps |
| TK-004 | Spike | [title] | 2 | High | Backend Team |
| TK-005 | Feature | [title] | 3 | Medium | Backend Team |
| **Total** | | | **11** | | |
```
