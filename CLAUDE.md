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

## Format reference

- `TASKS.md`: one markdown table, one row per ID, with a short type/status/
  commit/changelog-link. Keep the "Next free ID" line at the top current.
- `CHANGELOG.md`: reverse-chronological, one `##` section per pass (not per
  commit), each tagged with its task ID and using `Added`/`Changed`/`Fixed`
  subsections as needed, written in player-facing language (see above). See
  the file's own entries for the expected level of detail.

## Other project docs worth knowing about

- `AGENT.md` — engineering charter (rendering/camera constraints,
  English-only rule, general coding standards).
- `AGENTS.md` — automated PR review protocol.
- `.claude/skills/` — `game-director`, `item-creator`, `spell-creator`,
  `npc-monster-creator`, `quest-creator`, `graphic-designer`: use these for
  actually building new content; this file only covers tracking it
  afterward.
- `docs/content-ideas/` — write-ups from past content passes (research
  notes, what shipped, backlog ideas). Worth reading before starting a new
  content pass — several already-identified, canon-grounded ideas are
  sitting there unbuilt.
