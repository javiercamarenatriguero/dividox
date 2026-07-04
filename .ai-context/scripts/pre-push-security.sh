#!/usr/bin/env bash
# Pre-push advisory hook: secret scan + owasp-security-review reminder.
# Registered as a PostToolUse hook on Bash — receives tool input JSON on stdin.
# Advisory only — always exits 0 (never blocks push).

INPUT=$(cat)
CMD=$(echo "$INPUT" | jq -r '.tool_input.command // .tool_input.cmd // ""')

if ! echo "$CMD" | grep -qE '\bgit\b.*\bpush\b'; then
  exit 0
fi

echo ""
echo "=== Security Advisory Scan ==="
echo ""

if command -v gitleaks &>/dev/null; then
  LEAKS_OUTPUT=$(gitleaks detect --no-git --no-banner 2>&1)
  LEAKS_EXIT=$?
  if [ $LEAKS_EXIT -ne 0 ]; then
    echo "⚠ gitleaks found potential secrets:"
    echo "$LEAKS_OUTPUT"
    echo ""
  else
    echo "✓ No secrets detected by gitleaks"
  fi
else
  echo "⚠ gitleaks not installed — install with: brew install gitleaks"
fi

CHANGED=$(git diff --name-only HEAD~1 2>/dev/null || git diff --name-only HEAD)
SECURITY_FILES=$(echo "$CHANGED" | grep -iE '(auth|token|session|keystore|keychain|credential|network|security|crypto|storage|datastore|preferences)' || true)

if [ -n "$SECURITY_FILES" ]; then
  echo ""
  echo "⚠ Security-sensitive files changed:"
  echo "$SECURITY_FILES" | sed 's/^/  - /'
  echo ""
  echo "→ Run /owasp-security-review before creating a PR"
  echo "  MASVS controls: STORAGE · CRYPTO · AUTH · NETWORK · PLATFORM · PRIVACY"
fi

echo ""
