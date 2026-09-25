---
name: ship-pr
description: End-to-end delivery of a change in this repo, from a finished working tree to a merged PR and a verified live site. Covers a self-review before opening the PR, task/changelog/guidelines bookkeeping, opening the PR, waiting on CI and the Codex review, answering findings, merging, and post-merge cleanup. Use whenever the user says "open a PR", "ship it", "merge it", "double check and merge", "push this", or a content/balance task is done and needs to land. For the merge policy itself (when self-merge is allowed), this defers to the `steward` skill.
---

# Ship a PR (t4c_reborn_new)

`steward` says **when** a PR may be merged. This skill is the **how**, start to finish, and it
exists because the same avoidable review round-trips kept happening: old saves missed by a new
cap, an off-by-one in odds shown on the website, a doc restating a number that had already
changed. Do the self-review in step 1 *before* Codex does it for you.

The build is **Maven** (`mvn`), not Gradle.

## 1. Self-review the diff before opening anything

Run `git diff origin/main...HEAD` (and `git status` for untracked files) and check each line of
this list. Fix what you find before pushing.

- [ ] **Old saves.** Did you add or lower a cap, limit, range or requirement (level, rebirths,
      stats, item requirements, counts)? Then a save made before this change can already be past
      it. Clamp on load (`helper/PlayerStateMapper.applyToPlayer`), don't silently delete player
      choices, and add a test that loads an over-the-limit save. See the `balance-change` skill.
- [ ] **Numbers shown to players come from code.** Any value on the website, in NPC dialogue or
      in docs that restates game math (odds, damage, caps, requirements, costs) must be computed
      by the game's own helper, not typed by hand. Check probability edges: this codebase often
      rolls `nextInt(101) <= chance`, which is (c+1)/101, not c%.
- [ ] **Docs and skills that quote code.** `grep -rn` the old value across `DESIGN_GUIDELINES.md`,
      `CLAUDE.md`, `.claude/skills/**` and `compendium/app.js`. Where a doc must mention a
      number that a test can check, add or extend the test. Otherwise, point to the code
      instead of copying the number.
- [ ] **Hard-coded counts in tests.** Adding content can break count tests
      (`SpellRegistryParityTest` counts spells, for example). Update the expected count on
      purpose, and say why in the commit.
- [ ] **Registry keys.** JSON items register as `item.<key>` and spells as `${spell.<key>}`. Any
      comparison against a bare key string silently matches nothing.
- [ ] **Unique IDs.** boostIds (generated sets 20000–29999, hand items 30000+), spellIds and
      quest ids must not collide. The item test enforces boostIds, but check spellIds by grep.
- [ ] **Exporter allow-lists.** New spell/NPC/monster/quest classes need adding to
      `CompendiumExporter`'s "new content" sets, or the website won't show them.
- [ ] **i18n.** Every new `${...}` key exists in `assets/i18n/lang.json` (English only).

Then run, locally:

```bash
mvn -q test                       # full suite; CI runs a scoped subset, so don't rely on it alone
mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
java -cp "target/classes:$(cat /tmp/cp.txt)" com.perso.T4C.tools.CompendiumExporter compendium/data
```

Commit the regenerated `compendium/` data if it changed. If you touched `compendium/app.js`,
`app.css` or `index.html`, also do the render check in `verify-website`.

## 2. Bookkeeping, in the same PR

- [ ] `TASKS.md`: claim the next free `T4C-XXXX`, bump the "Next free ID" line, add the row.
- [ ] `CHANGELOG.md`: a player-facing entry (no class names, no tests, no file paths).
- [ ] `DESIGN_GUIDELINES.md`: record every lasting decision from the conversation (section 0
      lists what counts). This is required, not optional polish.
- [ ] Commit message references the task ID and ends with the session's attribution lines.
- [ ] After the main commit, a small follow-up commit records its hash in the `TASKS.md` row.

## 3. Open the PR and wait properly

1. Branch: the session's designated branch. If its previous PR is already merged, restart it from
   `origin/main` (`git checkout -B <branch> origin/main`, bringing uncommitted work across with a
   stash). A normal push is then a fast-forward. **Never force-push.**
2. Open the PR (GitHub MCP), ready for review. The body carries the technical detail the
   changelog leaves out: rules as a table, what changed, how it was tested.
3. `subscribe_pr_activity`, then `send_later` a check-in about 15 minutes out as a fallback,
   because webhooks for CI success and Codex completion don't always arrive.
4. End your turn. Don't sleep or poll in a loop; events and the check-in will wake you.

## 4. Handle Codex findings

For each finding: check it against the code, then decide.
- **Real:** reproduce it as a failing test where practical, fix the root cause (not just the
  instance), run `mvn -q test`, and push. Reply on the thread naming the commit and what it
  changes, then resolve it. If the fix exposes a *class* of mistake, add a test that guards the
  whole class, as the boostId-uniqueness test did.
- **Not worth fixing** (out of scope, pre-existing, a destructive guess): reply once with the
  reason and resolve. You don't need the owner's sign-off.
- A pushed fix triggers a Codex re-review. Wait for it to finish before merging.

## 5. Merge and clean up

Merge only when `steward`'s conditions hold: **Build and test** is green on the current head, there
is no conflict, Codex is not "Running", and every finding has had a decision. Merge with
`merge_pull_request` (`merge_method: merge`, `expectedHeadSha` = current head). Then:

- [ ] Delete your pending `send_later` triggers for this PR (`delete_trigger`).
- [ ] If the user asked whether the site is live, or the PR changed website-visible data, run
      `verify-website` step 3 after the compendium workflow and Vercel finish.
- [ ] Report to the user in plain language: what's merged, what Codex caught and how it was
      handled, and the open follow-ups.
