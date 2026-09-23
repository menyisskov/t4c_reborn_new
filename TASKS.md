# Task Ledger

A lightweight, in-repo task tracker. This project doesn't use an external
issue tracker, so every unit of content/feature work gets a sequential ID
here (`T4C-XXXX`) purely for visibility and cross-referencing — from a
commit message, a `CHANGELOG.md` entry, or a PR description back to "what
was this work and why."

**Next free ID: `T4C-0024`.** When starting new work, claim the next ID,
add a row below with status `Planned`/`In Progress`, and flip it to `Done`
(with a commit range and a `CHANGELOG.md` entry) when it ships. See
`CLAUDE.md` for the full policy this file is part of.

| ID | Title | Type | Status | Commits | Changelog |
|---|---|---|---|---|---|
| T4C-0001 | JSON content pipeline, armor sets, level cap 500 | Content/Systems | Done | `a7e7247` | [2026-09-16](CHANGELOG.md#2026-09-16--json-content-pipeline-armor-sets-level-cap-500-t4c-0001) |
| T4C-0002 | Rebirth economy, storage, spell vendor, macros | Content/Systems | Done | `0404411` | [2026-09-17](CHANGELOG.md#2026-09-17--rebirth-economy-storage-and-quality-of-life-pass-t4c-0002) |
| T4C-0003 | Content-authoring Claude skills | Process/Tooling | Done | `0f2cba7`, `2bfcd38`, `99e6f39`, `1758ed7` | [2026-09-17](CHANGELOG.md#2026-09-17--content-authoring-skills-t4c-0003-processtooling) |
| T4C-0004 | Sunken Chancel & Cinderreach Hills zones | Content | Done | `8f67c16` | [2026-09-17](CHANGELOG.md#2026-09-17--sunken-chancel--cinderreach-hills-zones-t4c-0004) |
| T4C-0005 | Leveling overhaul (XP overflow fix) + 4 endgame zones 100–500 | Content/Fix | Done | `a8c3f55` | [2026-09-17](CHANGELOG.md#2026-09-17--leveling-overhaul--4-endgame-zones-levels-100500-t4c-0005) |
| T4C-0006 | Fix new spells unlearnable via TrainingCatalog | Fix | Done | `52db68e` | [2026-09-17](CHANGELOG.md#2026-09-17--spell-learnability-fix-t4c-0006) |
| T4C-0007 | English-only localization pass | Fix/Process | Done | `a8d76b1` | [2026-09-18](CHANGELOG.md#2026-09-18--english-only-localization-pass-t4c-0007) |
| T4C-0008 | Canon-verified content pass (Goblin Slayer, Arch Drake, Deep Ones Cave, Sentinel) | Content | Done | `67d940e` | [2026-09-18](CHANGELOG.md#2026-09-18--canon-verified-content-pass-t4c-0008) |
| T4C-0009 | Avalon island expansion | Content | Done | `09ad82d`, `40e0f19`, `2127376`, `a3c968a`, `cd7bdf3`, `d607f66` | [2026-09-18–19](CHANGELOG.md#2026-09-1819--avalon-island-expansion-t4c-0009) |
| T4C-0010 | CI: skip on docs-only changes, scoped test selection | Process/Tooling | Done | `d29b421` | [2026-09-19](CHANGELOG.md#2026-09-19--smarter-ci-skip-docs-only-changes-scope-test-runs-t4c-0010) |
| T4C-0011 | MMO server groundwork: Java 21, libGDX purity guard | Process/Tooling | Done | `8aef3a8`, `148752b` | [2026-09-21](CHANGELOG.md#2026-09-21--mmo-server-groundwork-java-21-libgdx-purity-guard-t4c-0011) |
| T4C-0012 | Local compendium website (items/spells/monsters/NPCs/quests/zones) | Content/Tooling | Done | `e0170f1` | [2026-09-22](CHANGELOG.md#2026-09-22--local-compendium-website-t4c-0012) |
| T4C-0013 | CI auto-regenerates compendium data on push to main | Process/Tooling | Done | `c0c15e7` | [2026-09-22](CHANGELOG.md#2026-09-22--ci-auto-regenerates-compendium-data-on-push-to-main-t4c-0013) |
| T4C-0014 | Deterministic NPC ordering in compendium exporter | Process/Tooling | Done | `0282e7c` | [2026-09-22](CHANGELOG.md#2026-09-22--deterministic-npc-ordering-in-compendium-exporter-t4c-0014) |
| T4C-0015 | Monster balance report generator (audit new bosses vs. legacy curve) | Process/Tooling | Done | `6757374` | [2026-09-22](CHANGELOG.md#2026-09-22--monster-balance-report-generator-t4c-0015) |
| T4C-0016 | Fix mislabeled attack "hit chance" on compendium monster pages | Fix | Done | `3f7fd65` | [2026-09-22](CHANGELOG.md#2026-09-22--fix-mislabeled-attack-hit-chance-on-compendium-monster-pages-t4c-0016) |
| T4C-0017 | XP-per-level chart on the compendium Systems page | Content/Tooling | Done | `dbc3add` | [2026-09-22](CHANGELOG.md#2026-09-22--xp-per-level-chart-on-the-compendium-systems-page-t4c-0017) |
| T4C-0018 | Rename t4cfantasy-colliding spells, add Apex-tier spells, +4/+5 weapons/shields, fix warrior/archer armor AC | Content/Fix | Done | `c982e26` | [2026-09-22](CHANGELOG.md#2026-09-22--spell-renames-apex-tier-spells-enchanted-gear-and-warriorarcher-armor-fix-t4c-0018) |
| T4C-0019 | Zone access quests, Avalon dead-end fix, dynamic fast travel | Content/Systems | Done | `43e9890` | [2026-09-22](CHANGELOG.md#2026-09-22--zone-access-quests-avalon-dead-end-fix-dynamic-fast-travel-t4c-0019) |
| T4C-0020 | CLAUDE.md: CHANGELOG.md must be written for players, not engineers | Process/Tooling | Done | `ce722be` | [2026-09-23](CHANGELOG.md#2026-09-23--changelog-writing-policy-player-facing-not-technical-t4c-0020) |
| T4C-0021 | Spell class balance pass, armor set loot wiring, compendium data/display fixes | Content/Fix | Done | `148c76f`, `97151ea` | [2026-09-23](CHANGELOG.md#2026-09-23--spell-balance-lost-armor-recovered-and-honest-site-data-t4c-0021) |
| T4C-0022 | Standalone town-side access quests for Sunken Chancel and Cinderreach Hills | Content | Done | `960509f` | [2026-09-23](CHANGELOG.md#2026-09-23--town-side-access-quests-for-sunken-chancel-and-cinderreach-hills-t4c-0022) |
| T4C-0023 | Zone map images on the compendium site (real tile-art coloring, NPC/boss pins) | Content/Tooling | Done | (pending) | [2026-09-23](CHANGELOG.md#2026-09-23--zone-map-images-on-the-compendium-site-t4c-0023) |

## Type legend

- **Content** — new/changed in-world content (zones, monsters, items,
  spells, quests, NPCs, maps).
- **Systems** — new or changed game mechanics/economy (rebirth, storage,
  progression curves, UI infra).
- **Fix** — bug fix with no new content.
- **Process/Tooling** — repo/dev workflow (skills, CI, review policy) with
  no in-game effect.

## Backfill note

T4C-0001 through T4C-0009 were assigned retroactively on 2026-09-19 while
setting up this ledger and `CHANGELOG.md`, by walking `git log` for the
project's content/feature passes. Earlier history (companion/taming system,
HUD rework, original NPC/quest content, etc.) predates this tracking effort
and was not backfilled with IDs — only the passes covered by
`CHANGELOG.md` are. Everything from T4C-0010 onward is tracked live, as the
work happens.
