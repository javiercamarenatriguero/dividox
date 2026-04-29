---
name: GitHub PR creation blocked by EMU
description: gh pr create fails with Enterprise Managed User restriction; must create PRs manually
type: feedback
---

`gh pr create` fails with `GraphQL: Unauthorized: As an Enterprise Managed User, you cannot access this content (createPullRequest)`.

**Why:** The GitHub account is an Enterprise Managed User (EMU), which restricts certain API operations including programmatic PR creation via the CLI.

**How to apply:** After pushing the branch, provide the PR URL from the `git push` output and instruct the user to open it in the browser to create the PR manually. Do not retry `gh pr create` — it will always fail.
