#!/usr/bin/env bash
# Pre-commit hook for Claude Code: runs detekt + jvmTest before git commit commands.
# Called as a PreToolUse hook on Bash — receives tool input JSON on stdin.

set -euo pipefail

CMD=$(jq -r '.tool_input.command // ""')

# Only intercept git commit commands
if ! echo "$CMD" | grep -qE '\bgit\b.*\bcommit\b'; then
  exit 0
fi

ERRORS=""

if ! ./gradlew detekt --no-daemon --console=plain -q 2>&1; then
  ERRORS="Detekt static analysis failed."
fi

if ! ./gradlew jvmTest --no-daemon --console=plain -q 2>&1; then
  ERRORS="${ERRORS:+$ERRORS }JVM tests failed."
fi

if [ -n "$ERRORS" ]; then
  jq -n --arg reason "$ERRORS" '{"continue": false, "stopReason": $reason}'
  exit 0
fi
