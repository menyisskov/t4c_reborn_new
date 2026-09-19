#!/usr/bin/env bash
# Decides what this CI run needs to do, based on the diff between $1 (base
# ref/sha) and $2 (head ref/sha, defaults to HEAD): skip entirely (docs-only
# change), run the full test suite, or run a scoped subset of test classes.
#
# Prints GitHub Actions "key=value" output lines (see $GITHUB_OUTPUT docs) to
# stdout. Run it locally with any two refs to see what it would decide, e.g.:
#   scripts/ci-select-tests.sh origin/main HEAD
#
# Design notes (see CI_TEST_SELECTION.md for the full rationale): this
# project is a single Maven module with several registries (items, monsters,
# spells, NPCs, quests) that get scanned by cross-cutting "parity"/
# "integrity"/"coverage" tests living in unrelated packages (e.g. a monster
# change can break com/perso/T4C/npc/LighthavenSamaritanTest.java). Naive
# "only run tests in the changed package" selection would let those regress
# silently, which is exactly the class of bug this repo's CI was originally
# added to catch (see CHANGELOG.md's 2026-09-17 CI entry). So: any change
# that could plausibly ripple out is answered with the full suite; only
# clearly-local changes get a scoped run.

set -euo pipefail

BASE_REF="${1:?usage: ci-select-tests.sh <base-ref> [head-ref]}"
HEAD_REF="${2:-HEAD}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

emit() { printf '%s=%s\n' "$1" "$2"; }

# --- 1. Compute the changed file list -------------------------------------
if ! git rev-parse --verify --quiet "$BASE_REF" >/dev/null; then
  echo "## base ref '$BASE_REF' not found locally; falling back to full suite" >&2
  emit skip false
  emit test_mode full
  emit reason "could not resolve base ref $BASE_REF for a diff"
  exit 0
fi

if ! git rev-parse --verify --quiet "$HEAD_REF" >/dev/null; then
  echo "## head ref '$HEAD_REF' not found locally; falling back to full suite" >&2
  emit skip false
  emit test_mode full
  emit reason "could not resolve head ref $HEAD_REF for a diff"
  exit 0
fi

MERGE_BASE="$(git merge-base "$BASE_REF" "$HEAD_REF" 2>/dev/null || true)"
DIFF_BASE="${MERGE_BASE:-$BASE_REF}"

if ! CHANGED_FILES="$(git diff --name-only "$DIFF_BASE" "$HEAD_REF")"; then
  echo "## git diff between $DIFF_BASE and $HEAD_REF failed; falling back to full suite" >&2
  emit skip false
  emit test_mode full
  emit reason "git diff failed, could not determine changed files"
  exit 0
fi

if [[ -z "$CHANGED_FILES" ]]; then
  emit skip true
  emit test_mode none
  emit reason "no changed files between $DIFF_BASE and $HEAD_REF"
  exit 0
fi

# --- 2. Does anything build-relevant even touch this diff? ----------------
# Anything outside src/, assets/, pom.xml, or this script/workflow is docs/
# process only (CHANGELOG.md, TASKS.md, CLAUDE.md, docs/, *.md, etc.) and
# needs neither a compile nor a test run.
BUILD_RELEVANT="$(grep -E '^(src/|assets/|pom\.xml$|\.github/workflows/ci\.yml$|scripts/ci-select-tests\.sh$)' <<<"$CHANGED_FILES" || true)"

if [[ -z "$BUILD_RELEVANT" ]]; then
  emit skip true
  emit test_mode none
  emit reason "only docs/process files changed, no compile or test needed"
  exit 0
fi

emit skip false

# --- 3. Full-suite fallback triggers ---------------------------------------
FULL_REASON=""

if grep -qE '^pom\.xml$' <<<"$CHANGED_FILES"; then
  FULL_REASON="pom.xml changed"
elif grep -qE '^\.github/workflows/ci\.yml$' <<<"$CHANGED_FILES"; then
  FULL_REASON="CI workflow itself changed"
elif grep -qE '^assets/' <<<"$CHANGED_FILES"; then
  FULL_REASON="assets/ changed (data several tests load directly)"
fi

# Root-level main classes (no subpackage: MyGame, T4CContentStudio,
# MapEditor) are entry points / shared tooling - treat as foundational.
if [[ -z "$FULL_REASON" ]] && grep -qE '^src/main/java/com/perso/T4C/[^/]+\.java$' <<<"$CHANGED_FILES"; then
  FULL_REASON="a root-level entry-point/tooling class changed"
fi

# Packages nearly everything else depends on transitively - no reliable way
# to scope tests to "everyone who might be affected", so play it safe.
FOUNDATIONAL_PACKAGES="helper entity model world mapping config content exception"
TOUCHED_PACKAGES="$(grep -oE '^src/(main|test)/java/com/perso/T4C/[^/]+/' <<<"$CHANGED_FILES" | sed -E 's#^src/(main|test)/java/com/perso/T4C/##;s#/$##' | sort -u)"

if [[ -z "$FULL_REASON" ]]; then
  for pkg in $TOUCHED_PACKAGES; do
    for f in $FOUNDATIONAL_PACKAGES; do
      if [[ "$pkg" == "$f" ]]; then
        FULL_REASON="foundational package '$pkg' changed"
        break 2
      fi
    done
  done
fi

# A broad change touching many packages at once isn't worth narrowing -
# the bookkeeping cost isn't buying much of a speedup at that point.
TOUCHED_PACKAGE_COUNT="$(wc -w <<<"$TOUCHED_PACKAGES" | tr -d ' ')"
if [[ -z "$FULL_REASON" && "$TOUCHED_PACKAGE_COUNT" -gt 6 ]]; then
  FULL_REASON="$TOUCHED_PACKAGE_COUNT packages touched at once, running everything"
fi

if [[ -n "$FULL_REASON" ]]; then
  emit test_mode full
  emit reason "$FULL_REASON"
  exit 0
fi

# --- 4. Scoped selection -----------------------------------------------
# Registry-sensitive test classes: any test file that imports one of the
# five content registries. These get pulled in whenever a change touches a
# package that feeds one of those registries, regardless of which package
# the test itself lives in - see the module docstring above.
REGISTRY_IMPORT_PATTERN='^import com\.perso\.T4C\.(item\.ItemRegistry|monster\.core\.MonsterRegistry|spell\.SpellRegistry|npc\.core\.NpcFactoryRegistry|quest\.QuestRegistry);'
CONTENT_PACKAGES="item monster spell npc quest tools"

NEEDS_REGISTRY_TESTS=false
for pkg in $TOUCHED_PACKAGES; do
  for c in $CONTENT_PACKAGES; do
    if [[ "$pkg" == "$c" ]]; then
      NEEDS_REGISTRY_TESTS=true
      break 2
    fi
  done
done

TEST_CLASSES=""

for pkg in $TOUCHED_PACKAGES; do
  if [[ -d "src/test/java/com/perso/T4C/$pkg" ]]; then
    while IFS= read -r f; do
      TEST_CLASSES="$TEST_CLASSES $(basename "$f" .java)"
    done < <(find "src/test/java/com/perso/T4C/$pkg" -name '*.java')
  fi
done

if [[ "$NEEDS_REGISTRY_TESTS" == true ]]; then
  while IFS= read -r f; do
    TEST_CLASSES="$TEST_CLASSES $(basename "$f" .java)"
  done < <(grep -rlE "$REGISTRY_IMPORT_PATTERN" src/test/java --include='*.java')
fi

# Any test file changed directly always runs, even if its package wasn't
# otherwise implicated (e.g. a brand-new test with no matching main change).
while IFS= read -r f; do
  [[ -n "$f" ]] && TEST_CLASSES="$TEST_CLASSES $(basename "$f" .java)"
done < <(grep -E '^src/test/java/.*\.java$' <<<"$CHANGED_FILES" || true)

TEST_CLASSES="$(tr ' ' '\n' <<<"$TEST_CLASSES" | sed '/^$/d' | sort -u | paste -sd, -)"

if [[ -z "$TEST_CLASSES" ]]; then
  emit test_mode full
  emit reason "changed packages ($TOUCHED_PACKAGES) matched no test class, running everything to be safe"
  exit 0
fi

emit test_mode scoped
emit test_classes "$TEST_CLASSES"
emit reason "scoped to packages: $(tr '\n' ' ' <<<"$TOUCHED_PACKAGES")"
