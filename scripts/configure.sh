#!/bin/bash
# Points the project at your own Apple Developer account.
#
#   ./scripts/configure.sh com.yourname ABCDE12345
#
# - Replaces the bundle ID prefix everywhere it's used: both targets, the App Group
#   in both entitlements files, and Shared/AppGroup.swift.
# - Saves your Team ID in Config/Signing.local.xcconfig, which Git ignores.
#
# Safe to run again with a different prefix or team.
set -euo pipefail

cd "$(dirname "$0")/.."

usage() {
    echo "Usage: $0 <bundle-id-prefix> [team-id]"
    echo "Example: $0 com.janedoe ABCDE12345"
    echo "Your Team ID is in Xcode › Settings › Accounts (select your team), or at developer.apple.com/account."
    exit 1
}

[[ $# -ge 1 && $# -le 2 ]] || usage
new_prefix="$1"
team="${2:-}"

if ! [[ "$new_prefix" =~ ^[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)+$ ]]; then
    echo "Error: '$new_prefix' isn't a reverse-DNS prefix like com.yourname."
    exit 1
fi
if [[ -n "$team" ]] && ! [[ "$team" =~ ^[A-Z0-9]{10}$ ]]; then
    echo "Error: '$team' doesn't look like a Team ID (10 capital letters and digits, like ABCDE12345)."
    exit 1
fi

project="StepCounter.xcodeproj/project.pbxproj"
old_prefix=$(sed -nE 's/.*PRODUCT_BUNDLE_IDENTIFIER = (.+)\.StepCounter;.*/\1/p' "$project" | head -n 1)
if [[ -z "$old_prefix" ]]; then
    echo "Error: couldn't find the app's bundle identifier in $project."
    exit 1
fi

files=(
    "$project"
    StepCounter/StepCounter.entitlements
    StepCounterWidget/StepCounterWidget.entitlements
    Shared/AppGroup.swift
)

if [[ "$old_prefix" != "$new_prefix" ]]; then
    old_escaped=$(printf '%s' "$old_prefix" | sed 's/\./\\./g')
    sed -i '' "s/${old_escaped}\.StepCounter/${new_prefix}.StepCounter/g" "${files[@]}"
fi
echo "Bundle IDs: ${new_prefix}.StepCounter, ${new_prefix}.StepCounter.Widget"
echo "App Group:  group.${new_prefix}.StepCounter"

if [[ -n "$team" ]]; then
    printf '// Your signing settings. Git ignores this file.\nDEVELOPMENT_TEAM = %s\n' "$team" > Config/Signing.local.xcconfig
    echo "Team:       $team (saved in Config/Signing.local.xcconfig)"
else
    echo "Team:       not set. Choose one in Xcode under Signing & Capabilities, or rerun with your Team ID."
fi
