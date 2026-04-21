#!/bin/bash

# Script to install git hooks for Signalman project

set -e

echo "Installing git hooks..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOOKS_DIR="$SCRIPT_DIR/../.git/hooks"
PRE_COMMIT_HOOK="$SCRIPT_DIR/pre-commit"

# Check if we're in a git repository
if [ ! -d "$SCRIPT_DIR/../.git" ]; then
    echo "Error: Not a git repository. Please run this script from the project root."
    exit 1
fi

# Create hooks directory if it doesn't exist
mkdir -p "$HOOKS_DIR"

# Copy pre-commit hook
cp "$PRE_COMMIT_HOOK" "$HOOKS_DIR/"
chmod +x "$HOOKS_DIR/pre-commit"

echo "Git hooks installed successfully!"
echo ""
echo "The following hooks are now active:"
echo "  - pre-commit: Runs spotlessApply and detekt before each commit"
echo ""
