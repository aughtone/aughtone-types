#!/bin/bash

# Gemini Git Safety Guardrail
# This script is a secondary safety layer to prevent destructive Git operations.

COMMAND="$@"
if [ -z "$COMMAND" ]; then
  # Fallback to stdin if no args provided
  COMMAND=$(cat)
fi

DANGEROUS_PATTERNS=(
  "git push"
  "git reset"
  "git clean"
  "git branch -D"
  "git branch -d"
  "git checkout \."
  "git restore \."
  "git commit"
  "git merge"
  "git rebase"
  "git tag"
)

for pattern in "${DANGEROUS_PATTERNS[@]}"; do
  if echo "$COMMAND" | grep -qE "$pattern"; then
    echo "🛑 BLOCKED: '$COMMAND' matches dangerous pattern '$pattern'." >&2
    echo "Gemini is not authorized to perform destructive Git operations." >&2
    exit 2
  fi
done

exit 0
