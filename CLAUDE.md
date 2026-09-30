# CLAUDE.md

Instructions for Claude Code (and any other AI agent) working in this repo.
This file is about **process**; for engineering/architecture rules see
`AGENT.md`, and for PR-review protocol see `AGENTS.md`.

## Changelog & task tracking — do this on every content/feature change

This project has no external issue tracker. `CHANGELOG.md` and `TASKS.md`
are it — they exist so a human can see, at a glance and without digging
through `git log`, what's shipped and when, in the direction of a future
"release." Keeping them current is **part of the task**, not a follow-up.

Whenever you add, change, or fix something a player would notice — new
content (zone, monster, item, spell, quest, NPC, map), a systems/economy
change (rebirth, progression curves, storage, UI), or a real bug fix — do
all three before considering the work done:

1. **Claim a task ID.** Open `TASKS.md`, take the next free `T4C-XXXX`, and
   add a row (Status `In Progress` if you're starting, `Done` once shipped).
2. **Write a `CHANGELOG.md` entry** for the pass, dated, under
   `Added`/`Changed`/`Fixed`, referencing the task ID — follow the existing
   entries' format and level of detail (a sentence per item, not a diff
   dump).
3. **Reference the task ID in the commit message** (e.g. `(T4C-0012)`) so
   the commit, the changelog entry, and the task row all cross-link.

Pure process/tooling work (skills, CI, review policy) still gets an ID and
a changelog entry — tag it `Process/Tooling` in `TASKS.md` — but skip it
for things with no player- or repo-visible effect (typo fixes, comment
edits, a failed experiment reverted in the same session).

Small fixes discovered *while* doing a larger pass (e.g. a bugfix found
mid-zone-build) belong in that pass's own task/changelog entry, not a new
one — don't fragment one piece of work into several IDs.

### CHANGELOG.md is for players, not engineers

`CHANGELOG.md` is patch notes — write it for someone who plays the game
and has never opened this repo. Say what was **added, changed, fixed, or
removed**, in plain language a player would use, not what was edited in
the code:

- No class/method/variable names, file paths, registry/flag names, or
  other implementation details (`QuestService`, `hasUnlockedZone`,
  `unlock.zone.*`, `CompendiumExporter` — none of that belongs here).
- No mention of tests, refactors, code review findings, or how a fix
  works internally — just the player-visible result.
- Describe the thing itself: the zone, item, quest, monster, spell, or
  behavior, and what's different about it now. "Reaching a quest's kill
  goal now tells you if you still need to bring back an item" reads fine;
  "recordKill's notification now checks hasRequiredItem before announcing
  message.quest_ready" does not.
- That level of technical detail is exactly right for the **commit
  message** and PR description — put it there instead, not in the
  changelog.

This applies to every entry, `Process/Tooling` ones included — even a
repo-process change has a plain-language version ("the project's rules for
X changed" reads fine; naming the specific doc/file does not).

## Format reference

- `TASKS.md`: one markdown table, one row per ID, with a short type/status/
  commit/changelog-link. Keep the "Next free ID" line at the top current.
- `CHANGELOG.md`: reverse-chronological, one `##` section per pass (not per
  commit), each tagged with its task ID and using `Added`/`Changed`/`Fixed`
  subsections as needed, written in player-facing language (see above). See
  the file's own entries for the expected level of detail.

## Design guidelines — read before any content or balance work

`DESIGN_GUIDELINES.md` holds the owner's balance rules: the level cap, rebirths, the spell
ladder, item classes/AC/bonus budgets, element colors, and how the reference website must
present numbers. Follow it when adding or changing spells, items, monsters or progression. When
the owner states a new rule in chat, add it to that file in the same pass.

**Keeping it current is a standing duty, not something the owner has to request.** At the end
of every task, work out which decisions from the conversation should outlive it: stated rules,
corrections ("this doesn't make sense, I'd expect…"), design calls you made to carry out an
instruction, workflow preferences, accepted open gaps. Record them in `DESIGN_GUIDELINES.md` in
the same PR as the work. That file's section 0 has the full criteria.

## Before opening a PR — self-review first

Nothing reviews these PRs automatically (Codex review was switched off on 2026-09-26), so
this self-review is the only one they get. Use the `ship-pr` skill.
Its step 1 is the full checklist; at minimum:

- **Rebuild the local game after every fix** with `mvn -q '-DskipTests' package`, then rebuild the
  user's main checkout after merging so its launcher runs the latest code. Run focused or full
  tests when they help verify a concrete risk; tests are not a prerequisite for every fix.
- **Old saves:** any new or lowered cap or limit needs a load-time clamp and a test that loads an
  over-the-limit save. Clamp derived values; never claw back what the player chose or spent.
  See the `balance-change` skill.
- **Numbers players see** (website, dialogue, docs) are computed by the game's own helpers,
  never re-typed. Check probability edges: `nextInt(101) <= c` is (c+1)/101, not c%.
- **Docs that quote code go stale.** Grep for the old value in `DESIGN_GUIDELINES.md`, the
  skills and `compendium/app.js`, and prefer a test-backed rule over a copied number.
- **Plan PR boundaries up front.** When a request bundles several goals, say which goals land
  in which PR before starting. Finish, merge and re-branch from `main` between them, so one
  PR's churn doesn't leave another's docs stale.

## Mistakes past reviews caught (don't repeat them)

Add a line here whenever a review finds a new *class* of mistake, along with the test or rule
that now guards against it.

- A new rebirth cap didn't clamp saves already above it. Now guarded by
  `PlayerStateMapperTest`, and the "old saves" rule above.
- The website showed aura odds as c% while the game rolls (c+1)/101. Now guarded by
  `SeraphAuraService.effectivePercent` and `RebirthBehaviorTableTest`.
- A skill doc still reserved an old boost-ID range after a generator re-run. Now guarded by
  `ItemBalanceGuidelinesTest.boostIdsAreUniqueAndInTheirReservedRange`.
- Hard-coded counts in tests (`SpellRegistryParityTest`) break when content is added. Update
  them on purpose, with the reason in the commit.
- JSON items register as `item.<key>`. Comparing against the bare key silently matches
  nothing.
- A skill's example script hard-coded this container's checkout path (`/home/user/...`), which
  breaks in any other checkout. Docs and skills derive paths
  (`git rev-parse --show-toplevel`) instead of copying them.
- Player-facing prose drifted from the code it described: ten quest walkthroughs called a
  *required* item (consumed on turn-in) a reward, and named the boss carrying it as optional.
  Prose is now fact-checked against the definition, not against neighbouring prose — see the
  `technical-writer` skill's fact-check pass.
- A field existing on a definition but never exported means the site cannot show it, and the
  prose starts compensating with something untrue (`rewardItemKey`). When a page can't state a
  fact, fix the exporter rather than writing around it.
- Fixing a wrong fact on one surface and leaving it on the parallel one: all 23 quest
  walkthroughs were corrected while four zone summaries kept the same "that boss is optional"
  claim, and the review caught it. When a fact lives in two places, check both. Now guarded by
  the zone-summary/forced-boss cross-check in the `technical-writer` skill.
- When wiring a missing item grant (a "quest checks for X but nothing gives X" fix), a free
  no-cost grant was added for the Key of Artherk instead of reading that same NPC's own existing
  dialogue text first — which already spelled out the real recipe (Will + Heart + Soul of
  Artherk, parallel to the Key of Ogrimar's Skull + Blood + Bones, both already-sourced items).
  Before inventing a grant condition for an item that already has flavor text describing how
  it's obtained, grep `lang.json` for that item's name - the requirement is very often already
  written down, just not yet wired into code.
- Two JSON items shipped with `appearanceInventory: "64kInvNecklace 4"` — a plausible-looking
  name one increment past the real range (`1`-`3`), so nothing caught it until a player saw a
  blank inventory icon. The `item-creator` skill already documents how to verify a sprite name
  against the packed bin before using it; this is what happens when that step is skipped for a
  name that merely *looks* consistent with ones seen elsewhere. Verify, don't pattern-match.
- 11 quests (`passage_to_avalon` and 10 others) passed their `requiredItemKey` as the bare item
  key (e.g. `"tideworn_avalon_chart"`) instead of the normalized form inventory entries actually
  use (`"item.tideworn_avalon_chart"`). `QuestService.hasRequiredItem`/the progress dialog check
  it with a raw `Collections.frequency()` against the player's inventory list, which never
  normalizes either side — so the quest silently never registered the item, no matter how many
  the player carried. `ItemRegistry.findByKey` is prefix-tolerant so this was invisible in
  isolation; only a real inventory-list comparison catches it. Now guarded by
  `QuestServiceItemObjectiveTest.everyRealQuestsRequiredItemKeyIsAlreadyNormalized`, which sweeps
  every real `QuestDef` and asserts `requiredItemKey` is already normalized. When writing a new
  item-turn-in quest, use the normalized `"item.<key>"` form (see any existing quest that grants
  the same item for the exact string), not the JSON file's bare `key` field.
- Two agents working in parallel from stale, pre-fix worktrees independently rediscovered and
  re-fixed the exact bug above (T4C-0098 duplicating T4C-0095) before either was merged. When
  spawning parallel agents to work on a large, related request, expect this kind of overlap if
  their branch points predate a fix landed earlier in the same session, and check for it at merge
  time rather than assuming independent branches found independent bugs.

- An editor mode consumed every key to protect its own selection and accidentally disabled
  fullscreen and the NPC/monster pickers. Only consume shortcuts owned by the mode; pass
  unrelated keys to the global handlers. `BuildWorkspaceInputSmoke` checks this routing.

## Other project docs worth knowing about

- `AGENT.md` — engineering charter (rendering/camera constraints,
  English-only rule, general coding standards).
- `AGENTS.md` — automated PR review protocol.
- `.claude/skills/` — content skills (`game-director`, `item-creator`,
  `spell-creator`, `npc-monster-creator`, `quest-creator`,
  `graphic-designer`) for building new content, and workflow skills:
  `balance-change` (changing caps/formulas/displayed numbers safely),
  `ship-pr` (self-review → PR → CI → merge → cleanup), `verify-website`
  (regenerate, render-check and confirm the live compendium),
  `technical-writer` (the editorial standard and review gate for every word
  a player reads), and `steward` (the merge policy `ship-pr` defers to).
- `docs/content-ideas/` — write-ups from past content passes (research
  notes, what shipped, backlog ideas). Worth reading before starting a new
  content pass — several already-identified, canon-grounded ideas are
  sitting there unbuilt.
