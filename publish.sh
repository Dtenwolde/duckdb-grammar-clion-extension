#!/usr/bin/env bash
set -euo pipefail

PROPERTIES_FILE="gradle.properties"

usage() {
    echo "Usage: $0 [--version <X.Y.Z>]"
    echo
    echo "Options:"
    echo "  --version <X.Y.Z>   Bump plugin version before publishing"
    echo
    echo "Required environment variable:"
    echo "  JETBRAINS_MARKETPLACE_TOKEN   Your JetBrains Marketplace token"
    exit 1
}

# Parse arguments
NEW_VERSION=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --version)
            [[ -z "${2:-}" ]] && { echo "Error: --version requires a value"; usage; }
            NEW_VERSION="$2"
            shift 2
            ;;
        -h|--help) usage ;;
        *) echo "Unknown option: $1"; usage ;;
    esac
done

# Validate token
if [[ -z "${JETBRAINS_MARKETPLACE_TOKEN:-}" ]]; then
    echo "Error: JETBRAINS_MARKETPLACE_TOKEN is not set."
    echo "Export it before running this script:"
    echo "  export JETBRAINS_MARKETPLACE_TOKEN=<your_token>"
    exit 1
fi

# Optionally bump version
if [[ -n "$NEW_VERSION" ]]; then
    CURRENT_VERSION=$(grep '^pluginVersion=' "$PROPERTIES_FILE" | cut -d'=' -f2)
    echo "Bumping version: $CURRENT_VERSION → $NEW_VERSION"
    # macOS-compatible in-place sed
    sed -i '' "s/^pluginVersion=.*/pluginVersion=$NEW_VERSION/" "$PROPERTIES_FILE"
fi

PLUGIN_VERSION=$(grep '^pluginVersion=' "$PROPERTIES_FILE" | cut -d'=' -f2)
echo "Publishing version $PLUGIN_VERSION to JetBrains Marketplace..."

./gradlew clean buildPlugin verifyPlugin publishPlugin

echo
echo "Successfully published version $PLUGIN_VERSION."