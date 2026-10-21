#!/usr/bin/env bash
# Build a signed release APK locally and publish it to a GitHub Release.
#
# Mirrors the motorcycle-maintenance pattern: no CI secrets, no key leaves
# this machine. Expects keystore.properties at the repo root (storeFile,
# storePassword, keyAlias, keyPassword — see app/build.gradle.kts).
#
# Usage:
#   scripts/publish-release.sh <version>          # e.g. 1.0.0 (must match versionName)
#   scripts/publish-release.sh <version> --draft  # don't publish yet
#   scripts/publish-release.sh <version> --prerelease
#
# Env overrides:
#   NOTES_FILE=path/to/notes.md   release-notes body; else prompts $EDITOR
#   APK_NAME=CountingEdge.apk     renamed asset on the release

set -euo pipefail

VERSION="${1:-}"
if [[ -z "$VERSION" ]]; then
  echo "Usage: $0 <version> [--draft|--prerelease]" >&2
  exit 2
fi
shift || true

if [[ "$VERSION" == v* ]]; then
  echo "ERROR: pass the version without the leading 'v' (e.g. ${VERSION#v})." >&2
  exit 2
fi

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

TAG="v${VERSION}"
APK_NAME="${APK_NAME:-CountingEdge-${VERSION}.apk}"

if [[ ! -f keystore.properties ]]; then
  echo "ERROR: keystore.properties not found at repo root." >&2
  echo "Create one with:" >&2
  echo "  storeFile=/absolute/path/to/your.jks" >&2
  echo "  storePassword=..." >&2
  echo "  keyAlias=..." >&2
  echo "  keyPassword=..." >&2
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "ERROR: GitHub CLI (gh) not found. Install it: https://cli.github.com" >&2
  exit 1
fi
if ! gh auth status >/dev/null 2>&1; then
  echo "ERROR: gh is not authenticated. Run 'gh auth login' first." >&2
  exit 1
fi

if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "ERROR: working tree has uncommitted changes. Commit or stash first." >&2
  git status --short >&2
  exit 1
fi

# The tag must describe what the APK actually reports, and versionCode must
# go up, or sideloaded installs can't upgrade over the previous release.
GRADLE_FILE="app/build.gradle.kts"
read_gradle_value() { # <key> <file contents>
  sed -nE "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*\"?([^\"]+)\"?[[:space:]]*$/\1/p" <<<"$2" | head -n1
}
GRADLE_SRC="$(cat "$GRADLE_FILE")"
VERSION_NAME="$(read_gradle_value versionName "$GRADLE_SRC")"
VERSION_CODE="$(read_gradle_value versionCode "$GRADLE_SRC")"
if [[ -z "$VERSION_NAME" || ! "$VERSION_CODE" =~ ^[0-9]+$ ]]; then
  echo "ERROR: could not read versionName/versionCode from $GRADLE_FILE." >&2
  exit 1
fi
if [[ "$VERSION_NAME" != "$VERSION" ]]; then
  echo "ERROR: version argument $VERSION does not match versionName \"$VERSION_NAME\" in $GRADLE_FILE." >&2
  echo "Bump versionName (and versionCode), commit, then rerun." >&2
  exit 1
fi

git fetch --quiet --tags origin || echo "WARN: could not fetch tags from origin; checking local tags only." >&2

if git rev-parse -q --verify "refs/tags/$TAG" >/dev/null || git ls-remote --exit-code --tags origin "refs/tags/$TAG" >/dev/null 2>&1; then
  echo "ERROR: tag $TAG already exists." >&2
  exit 1
fi

PREV_TAG="$(git tag --list 'v[0-9]*' --sort=-v:refname | head -n1)"
if [[ -n "$PREV_TAG" ]]; then
  PREV_CODE="$(read_gradle_value versionCode "$(git show "$PREV_TAG:$GRADLE_FILE" 2>/dev/null || true)")"
  if [[ "$PREV_CODE" =~ ^[0-9]+$ ]] && (( VERSION_CODE <= PREV_CODE )); then
    echo "ERROR: versionCode $VERSION_CODE is not greater than $PREV_CODE (shipped in $PREV_TAG)." >&2
    echo "Bump versionCode in $GRADLE_FILE, commit, then rerun." >&2
    exit 1
  fi
fi
echo "==> Releasing $VERSION_NAME (versionCode $VERSION_CODE)${PREV_TAG:+, previous $PREV_TAG}"

echo "==> Running unit tests, then building release APK + AAB"
./gradlew clean testDebugUnitTest
./gradlew assembleRelease bundleRelease

APK_SRC="app/build/outputs/apk/release/app-release.apk"
AAB_SRC="app/build/outputs/bundle/release/app-release.aab"

if [[ ! -f "$APK_SRC" ]]; then
  echo "ERROR: $APK_SRC not found (build did not produce a signed APK)." >&2
  exit 1
fi

STAGE="$(mktemp -d)"
trap 'rm -rf "$STAGE"' EXIT
cp "$APK_SRC" "$STAGE/$APK_NAME"
ASSETS=("$STAGE/$APK_NAME")
if [[ -f "$AAB_SRC" ]]; then
  cp "$AAB_SRC" "$STAGE/${APK_NAME%.apk}.aab"
  ASSETS+=("$STAGE/${APK_NAME%.apk}.aab")
else
  echo "WARN: $AAB_SRC not found; publishing the APK only." >&2
fi

SHA256="$(shasum -a 256 "$STAGE/$APK_NAME" | awk '{print $1}')"
echo "==> APK SHA-256: $SHA256"

NOTES_PATH="${NOTES_FILE:-}"
if [[ -n "$NOTES_PATH" ]]; then
  # Substitute placeholders in a user-supplied notes file.
  RENDERED="$STAGE/notes.md"
  sed -e "s|__SHA256__|$SHA256|g" -e "s|__APK_NAME__|$APK_NAME|g" -e "s|__VERSION__|$VERSION|g" "$NOTES_PATH" > "$RENDERED"
  NOTES_PATH="$RENDERED"
else
  NOTES_PATH="$STAGE/notes.md"
  cat > "$NOTES_PATH" <<EOF
## What's new

- (describe changes)

## Install

Download \`$APK_NAME\` below and open it on a device running Android 7.0 or later. Installing it over a previous release preserves app data.

APK SHA-256: \`$SHA256\`
EOF
  if [[ -n "${EDITOR:-}" ]]; then
    "$EDITOR" "$NOTES_PATH"
  else
    echo "No \$EDITOR set — review/edit $NOTES_PATH then press Enter."
    read -r
  fi
fi

echo "==> Tagging $TAG"
git tag "$TAG"

echo "==> Pushing tag"
git push origin "$TAG"

echo "==> Creating GitHub Release $TAG"
gh release create "$TAG" \
  "${ASSETS[@]}" \
  --title "Counting Edge $VERSION" \
  --notes-file "$NOTES_PATH" \
  "$@"

echo "==> Done. https://github.com/vectorlogic-dev/card-counter/releases/tag/$TAG"
