# Changelog

All notable game-content and systems changes are documented here, newest first.
Format is loosely based on [Keep a Changelog](https://keepachangelog.com/),
grouped by content pass rather than by individual commit. Every entry
references a task ID from [`TASKS.md`](TASKS.md) — see that file for status,
commit links, and finer-grained notes.

This file was backfilled on 2026-09-19 by walking the project's git history;
dates are the commit dates of the work, not retroactively invented. From now
on, every content/feature pass adds its own entry here as part of the work
(see `CLAUDE.md`), not after the fact.

## [Unreleased]

_Nothing pending._

## 2026-09-22 — Spell renames, Apex-tier spells, enchanted gear, and warrior/archer armor fix (T4C-0018)

### Fixed
- Five spells added by earlier passes (T4C-0004/T4C-0009) were literally named after real
  t4cfantasy.com/Addon "Ancient tier" spells (Sentinel, Divine Veil, Clemancy, Undead
  Annihilation, Omega Planetoids) — a fork adding new content shouldn't reuse the real game's own
  spell names. Renamed to invented names with identical mechanics/`spellId`s: Sentinel → Leyward
  Bastion, Divine Veil → Veilstone Aegis, Clemancy → Wellspring Mercy, Undead Annihilation →
  Sunscour, Omega Planetoids → Gravebreaker. Archmage Thalindra and Skywatch Ilvara's dialogue/
  training wiring, and the compendium exporter's allow-list, updated to match.
- `tools/ArmorSetGenerator.java`'s warrior/archer armor flavors (Ancient Celestial and Empyrean
  tiers) were mechanically identical to their mage/elemental siblings — same Armor Class, and the
  same intelligence/wisdom requirement stacked *on top of* the strength/agility gate, so a
  "warrior" set demanded mage stats too. Now genuinely physical-class gear: ~65% of the mage
  tier's AC, no intelligence/wisdom requirement at all, and a flat endurance boost the mage
  flavors don't get, on top of the existing strength/agility + attack/archery skill split. All 24
  warrior/archer armor pieces regenerated; the 72 mage/elemental pieces are unaffected (Empyrean's
  36 elemental files show only cosmetic `boostId` renumbering from the shared counter).

### Added
- Five new "Apex tier" player spells (levels 320-900) filling the gap between the existing
  "Elder tier" (40-260) and the level-1000 curve cap / Avalon boss band (550-650): Voidreave
  Lance (320, dark bolt), Stormcaller's Judgment (480, air AOE), Sanctum Ward (550, defensive
  group ward), Emberqueen's Wrath (650, fire AOE), Cataclysm's Herald (900, water AOE capstone).
  Also taught by Archmage Thalindra at the Avalon Sanctuary Spell Trainer's Tower.
- Six new enchanted weapons extending two existing top-tier legacy lines one/two ranks past their
  previous +3 ceiling: Adamantite Two-Handed Sword +4/+5, Mithril Two-Handed Sword +4/+5, Black
  Locust Composite Bow +4/+5 — each continuing that line's own established damage-dice/flat-bonus/
  skill-boost growth curve (drop-only, `price: 0`, matching the existing +1→+2→+3 progression's
  own convention).
- Three new Adamantite Shield tiers (+3/+4/+5) — this codebase had no shield enchant-tier line at
  all before now; adds one on top of the existing unenchanted Adamantite Shield, with AC, parry
  skill, and endurance scaling per tier.

### Notes
- `item/json/ItemJsonDef.java` already supports `dmgFormula`/`atkDelay` pass-through (four
  existing JSON items — `goblin_slayer`, `bow_of_centaur_slaying`, `caradocs_sundered_blade`,
  `ignaroks_emberfang_claw` — already use it) — the `item-creator` skill's documented "JSON
  weapons can't have real damage" limitation is stale and no longer applies; no engine change was
  needed for the new weapons' real, scaled damage.

## 2026-09-22 — XP-per-level chart on the compendium Systems page (T4C-0017)

### Added
- `tools/CompendiumExporter.java` now exports `xpcurve.json` (and bundles it into `data.js` as
  `xpCurve`): every `XpCurveDefinitions` entry (levels 1-1000, `xpToNextLevel`/`totalXp`) plus
  `GameConstants.SERVER_XP_RATE`, the flat multiplier the server applies to monster XP grants
  before it's checked against this curve.
- Compendium Systems page: a log-scale line chart of XP required per level (the curve spans
  100 XP at level 1 to ~4.9T at level 999, so linear would flatten the entire early game to a
  single pixel), with a hover/keyboard crosshair + tooltip (level, XP to next level, total XP
  so far) and a collapsible milestone table for the same data without hovering.

## 2026-09-22 — Fix mislabeled attack "hit chance" on compendium monster pages (T4C-0016)

### Fixed
- The compendium's monster detail pages labeled a monster attack's `value2` field as "hit chance
  %", and never showed `value1` at all. Per `BaseMonster#rollDamage`/`pickAttack`, `value2` is
  only a selection weight used to pick among several eligible attacks at the same range — never a
  to-hit percentage — while `value1` is the real combat-attack/hit-power stat fed into hit
  resolution. Same class of bug Codex flagged in `MonsterBalanceReportGenerator.java` on PR #12
  (T4C-0015); found here independently while cross-checking the live site against that fix.
  `tools/CompendiumExporter.java` now exports `combatAttack` (value1) and `selectionWeight`
  (value2) instead of the old bare `value2`, and `compendium/app.js` displays "attack N" (plus
  "· weight N" only when a monster has more than one attack to choose among).

## 2026-09-22 — Monster balance report generator (T4C-0015)

### Added
- `tools/MonsterBalanceReportGenerator.java`: dumps every registered monster (Java + JSON, 427
  total) with full combat/resist stats to JSON, for auditing new-content bosses against the
  existing monster/level curve. Used to cross-check every new/activated monster's HP, damage,
  and `xpOnDeath` against real legacy monsters at comparable levels and against the live XP
  curve's documented pacing target (~9 kills/level for trash, 3-4 for a boss, accounting for the
  5x server XP rate). Findings (Mordrenn/Ignarok badly XP-overtuned from before the curve
  replacement in T4C-0005; four Avalon monsters badly XP-undertuned) reported separately, not
  yet corrected in this pass.

## 2026-09-22 — Deterministic NPC ordering in compendium exporter (T4C-0014)

### Fixed
- `tools/CompendiumExporter.java`'s NPC export followed `NpcFactoryRegistry.registrations()`'s
  classpath-scan order, which is not guaranteed stable across machines/filesystems — running the
  exporter locally and via the `compendium.yml` CI workflow (T4C-0013) on GitHub's runners
  produced the same NPC records in different orders, showing up as pure-reorder noise in
  `compendium/data/npcs.json`/`data.js` diffs with no actual content change. Now sorted by NPC id
  before export.

## 2026-09-22 — CI auto-regenerates compendium data on push to main (T4C-0013)

### Added
- `.github/workflows/compendium.yml`: on every push to `main`, recompiles, re-runs
  `tools/CompendiumExporter.java`, and pushes a `chore: regenerate compendium data` commit
  straight to `main` if `compendium/data.js`/`compendium/data/*.json` drifted (stat tweaks,
  reworded quest/dialogue text, new loot entries, etc. on already-tracked content). Guards
  against re-triggering itself on its own push via a `[skip compendium]` tag in its commit
  message.

### Notes
- This does not make brand-new content (a new zone/monster/spell/NPC/quest) appear on the
  site automatically — the exporter's "is this new" allow-lists are hand-curated (the game
  data has no `isNew`/rarity field to key off), so adding a genuinely new class still needs a
  source change to `CompendiumExporter.java` (and, for a new zone, a `compendium/data/zones.json`
  row) before this workflow's regeneration has anything new to pick up. See
  `compendium/README.md`'s "Regenerating the data" section for exactly when this workflow
  covers you and when it doesn't.
- Pushes straight to `main` with no review step, per explicit request — if `main` ever gets
  branch protection requiring PRs, this workflow's push will start failing and will need to
  switch to opening a PR instead.

## 2026-09-22 — Local compendium website (T4C-0012)

A static, searchable stat-sheet site (`compendium/`) documenting everything this fork added
on top of the original game — zones, monsters, items, spells, NPCs, quests — styled after
the classic `t4cfantasy.com` stat sheet but modern, cross-linked, and colored by item
rarity/spell element. Opens directly from the filesystem (`compendium/index.html`), no
server or build step required.

### Added
- `tools/CompendiumExporter.java`: a runnable dumper that reads the live game registries
  (`MonsterRegistry`, `SpellDefinitions`, `QuestDefinitions`, `NpcFactoryRegistry`,
  `ShopCatalog`, `assets/items/*.json`) and every i18n string they reference, filters to a
  curated "new since fork" allow-list cross-referenced against `CHANGELOG.md`/
  `docs/content-ideas/`, and writes both per-category JSON files and a single bundled
  `compendium/data.js` (`window.T4C_DATA`).
- `compendium/index.html` + `app.css` + `app.js`: a vanilla-JS, dependency-free, hash-routed
  single-page site — global fuzzy search, sortable/filterable tables for monsters/items/
  spells/NPCs, full monster characteristic pages (stats, resists, attacks, loot tables with
  drop-chance bars), item pages with `boosts[]` decoded into human-readable stat names
  (`statId` reference table) and a derived rarity tier (Legendary/Set/Rare/Common) used for
  color-coding, spell pages with element-colored tags, NPC dialogue-tree pages, and full
  quest walkthrough pages (offer/completion/completed text, objective geofence shown on a
  schematic minimap, rewards).
- `compendium/data/{zones,statids,meta}.json`: hand-curated reference data the exporter
  can't derive from code — zone name/level-range/biome/world-placement/summary (sourced from
  `docs/content-ideas/*.md`'s exact coordinates), the item `statId` → label table (from
  `.claude/skills/item-creator/references/stat-ids.md`), and non-zone systems/economy passes
  plus the armor-set and Colosseum-ladder collections for the Systems page.
- `compendium/README.md`: how to open the site and how to regenerate its data after a new
  content pass.

### Notes
- Scope is new-since-fork content only (10 zones, 33 monsters, 120 items, 10 new spells + the
  full player spellbook for context, 16 NPCs, 10 quests) rather than the entire legacy game
  database — see the README's "Scope" section.
- The Ancient Celestial / Empyrean armor sets (96 of the 120 items) have no shop or monster
  drop source in the current codebase (`price: 0`, no loot-table reference anywhere) — the
  Systems page documents this rather than inventing a fake source.

## 2026-09-21 — MMO server groundwork: Java 21, libGDX purity guard (T4C-0011)

First step of the client/server split. The original T4C was an MMO and this
recreation's single-player-only shape is an artifact of how it was rebuilt,
not a design goal — so the desktop client is being moved toward rendering a
world the server owns. Nothing player-visible changes in this pass.

### Added
- `ServerPurityTest` guards the rule packages the future headless server
  will load (`combat`, `spell`, `item`, `quest`, `skill`, `death`,
  `monster/loot`, `model`) against libGDX references. All 5,794 files across
  them are gdx-free today apart from four presentation classes
  (`CombatGeometry`, `CompanionCastVfxHook`, `NpcCastVfxHook`,
  `TameChannel`), recorded as known violations so the list doubles as the
  remaining client/server split work. A companion test fails once a recorded
  violation is cleaned up, so the list can't rot. A server has no GL
  context, so this class of mistake otherwise surfaces as a crash on a
  headless box rather than a compile error on a developer's desktop.

### Changed
- Toolchain moves to Java 21 LTS (`maven.compiler` 17 → 21, CI JDK 17 → 21).
  The server wants virtual threads for thread-per-connection socket
  handling; bumping the whole project keeps one toolchain rather than
  splitting versions per module.
- `scripts/ci-select-tests.sh` always appends `ServerPurityTest` in scoped
  mode. The guard asserts properties of packages other than its own, and
  `combat`/`skill`/`death` are neither foundational nor
  content-registry-defining — so they scope, and a gdx import added there
  would previously have run only that package's own tests and never been
  checked. Appended after the existing "matched no test class" check so it
  cannot mask that full-suite fallback.

### Notes
- `mvn spotless:apply` currently reformats 526 pre-existing files: CI runs
  compile and test but never `spotless:check`, so formatting has drifted.
  Left alone deliberately rather than bundling an unrelated 526-file diff
  into this pass.

## 2026-09-19 — Smarter CI: skip docs-only changes, scope test runs (T4C-0010)

### Added
- `scripts/ci-select-tests.sh` decides, from the diff between the base and
  head commit, whether CI has anything to do at all (skip), needs to run
  every test (full), or can run a scoped subset (scoped) — runnable and
  testable locally against any two refs.

### Changed
- `.github/workflows/ci.yml`: a docs/process-only change (no `src/`,
  `assets/`, or `pom.xml` touched) now skips compile and test entirely.
  Compile always runs, unconditionally, whenever the job doesn't skip.
  Content-registry-defining packages (`item`/`monster`/`spell`/`npc`/
  `quest`/`tools`), `assets/`, `pom.xml`, foundational packages
  (`helper`/`entity`/`model`/`world`/`mapping`/`config`/`content`/
  `exception`), root-level entry classes, and >6 touched packages at once
  all fall back to the full suite — this codebase has several
  cross-cutting "registry parity" tests (e.g. `SpellRegistryParityTest`,
  `LighthavenSamaritanTest`) that live in a different package than the
  content that can break them, so naive per-package test scoping would let
  those regress silently. A narrower change (e.g. `render`/`audio`/`gui`
  only) now runs only the touched package's own tests.

### Fixed
- Codex's review of this pass caught two real issues before merge, both
  fixed in the same pass rather than as follow-ups: selector outputs
  (including test-file-derived class names, which a PR's own diff
  controls) are now passed through `env:` and expanded as `"$VAR"` instead
  of being interpolated with `${{ }}` directly into `run:` script text —
  the latter is a shell-injection vector, since a maliciously-named test
  file could execute arbitrary commands in the CI runner. A first version
  of the content-package handling above tried a narrower heuristic (only
  pull in tests that import one of the five registry classes) instead of
  a blanket full-suite fallback; that heuristic missed real cross-package
  dependencies with no such import (e.g. a `NpcScripts` change breaking
  `SelfDestructSpellRegistryTest`), so it was dropped in favor of the
  simpler, safer full-suite fallback described above.

## 2026-09-18–19 — Avalon island expansion (T4C-0009)

### Added
- New island **Avalon**, reached via the `AvalonGateway` spell / `scroll_of_avalon`
  consumable — two sub-zones, **The Avalon Wilds** (lush) and **The Fading Veil**
  (corrupted), plus the **Avalon Sanctuary** settlement (temple, inn, weapons
  merchant, scroll/travel merchant, spell trainer's tower).
- Trash monsters: Fey Warden, Moonlit Stalker (lvl 300–420), Veilbound Wraith,
  Sundered Sentinel (lvl 450–600).
- Bosses (lvl 550–650): Sir Caradoc the Sundered Knight, Ysolde the Veiled
  Matriarch, The Verdant Warden — each with guard adds and a unique drop.
- Items: Caradoc's Sundered Blade, Ysolde's Veiled Circlet, Verdant Warden's
  Bulwark.
- NPCs: Elder Ophira (quest-giver), Quartermaster Elenna (weapons shop),
  Wayfarer Bryndis (sells `scroll_of_avalon`), Archmage Thalindra (spell
  trainer), Sister Ilyndra (temple priest).
- Quests: *Avalon Wilds Vigil*, *The Fading Veil's Reckoning* (both from Elder
  Ophira, geofenced kill-counts).

### Changed
- Avalon trash monster spawn density increased ~2.5x (8→20 spawn points per
  type) after the initial pass felt sparse.

## 2026-09-18 — Canon-verified content pass (T4C-0008)

### Added
- Items: Bow of Centaur Slaying, Goblin Slayer, Archdrake's Molten Heart
  (belt), Dragonguard's Scale Bracer, Barnacled Gauntlets, Depths Warden's
  Talisman.
- Monster: Kraanian Dragonguard ("Drake's Lair" — guards Arch Drake).
- Quests: *Drake's Lair Vigil*, *Deep Ones Cave Purge*.
- Spell: Sentinel (level-200 "Ancient tier" group-support).
- Activated pre-existing, previously-unplaced legacy content found already
  authored in the codebase: **Arch Drake** (level 1000, completes the Drake
  ladder), full loot for the existing Deep Ones Cave, and a real "Goblin
  Slayer" bounty on the existing Rhodar Heatforge NPC.

### Fixed
- JSON-authored weapons can now deal real, stat-scaled damage —
  `ItemJsonDef` previously always defaulted `dmgFormula`/`atkDelay` to a flat
  1–4 roll regardless of stats.

## 2026-09-18 — English-only localization pass (T4C-0007)

### Fixed
- Documented the English-only rule in `AGENT.md` (no locale switcher, no
  second language — translate legacy French remnants on sight).
- Translated remaining French strings: `ShopScreen`'s hardcoded error text,
  several `assets/i18n/lang.json` entries, dialogue keywords on 3 NPCs, the
  1650-line `T4C_168_BEHAVIOR_AUDIT.md`, and French test fixtures.

## 2026-09-17 — Spell learnability fix (T4C-0006)

### Fixed
- Spells added in the leveling-overhaul/zone passes were unlearnable via
  `TrainingCatalog`.

## 2026-09-17 — Leveling overhaul + 4 endgame zones, levels 100–500 (T4C-0005)

### Fixed
- XP-carrying fields (`Stats`, `XpCurve.Entry`, save data, death-penalty
  math) widened `int`→`long`, fixing a silent overflow/wraparound in the old
  curve starting at level 541.
- Mirak Nira's "kill 100 goblins" quest was unwinnable — nothing incremented
  its tracking flag. Now increments on any goblin kill.

### Changed
- New `XpCurveHardener` curve (levels 100–1000) with a level-scaling exponent
  that structurally outgrows monster XP awards at every level band, replacing
  the old fixed-exponent `XpCurveExtender` curve.

### Added
- 4 new zones, levels 100→500: **Windhowl Marches** (Centaur Warrior/King),
  **The Hollow March** (Barrow Wight/The Hollow King), **Lesser Drake's
  Aerie** (Kraanian Wyrmling/Lesser Drake), **Greater Drake's Bastion**
  (Bastion Warden/Greater Drake) — two are new monsters, two activate
  fully-stat'd legacy bosses that had never been placed in the world.

## 2026-09-17 — Sunken Chancel & Cinderreach Hills zones (T4C-0004)

### Added
- Zone **The Sunken Chancel** (water/undead, lvl 38–50): Drowned Acolyte,
  Tideclaw Crab, boss Mordrenn the Drowned Inquisitor; items; spells Riptide
  Surge / Drowned Ward; quest *Tide Warden's Plea*.
- Zone **Cinderreach Hills** (fire, lvl 58–70): Cinder Whelp, Ashfang
  Stalker, boss Ignarok the Emberfang; items; spells Cinderburst / Emberheart
  Resolve; quest *The Emberfang Hunt*.

## 2026-09-17 — Content-authoring skills (T4C-0003, process/tooling)

### Added
- `game-director`, `item-creator`, `quest-creator`, `spell-creator`,
  `npc-monster-creator`, `graphic-designer` Claude Code skills for
  structured, cross-checked content authoring going forward.

## 2026-09-17 — Rebirth economy, storage, and quality-of-life pass (T4C-0002)

### Added
- `SpellMerchant` NPC (sells every player-castable spell); `StorageChest`
  NPC at Lighthaven + Windhouse with a full deposit/withdraw UI (categories,
  search, drag-and-drop, quantity picker).
- New spells: Clemancy, Divine Veil, Undead Annihilation, Omega Planetoids.
- New fast-travel destinations: Stonecrest, Tarantula Pond, Skraug Camp,
  Timeprotectors.
- New GM commands: `.rebirth`, `.setpower`.
- New "Elemental Stats" tab on the character sheet; macro keybinds now
  support Ctrl/Shift/Alt modifiers.

### Changed
- Remort points now scale per rebirth (10 + 5/prior remort) instead of a
  flat 10; monster spawn density doubled; server XP rate raised 5x;
  `REBIRTH_MAX_REMORTS` raised to 100, level cap to 700, base light
  resistance to 5000.

### Fixed
- Alphan's post-rebirth teleport (was calling a spell that didn't exist).
- Null-safe regalia check in `RebirthBehavior` that could crash on rebirth.

## 2026-09-16 — JSON content pipeline, armor sets, level cap 500 (T4C-0001)

### Added
- JSON-driven authoring pipeline for monsters/items (`MonsterJsonDef`/
  `MonsterJsonLoader`, `ItemJsonDef`/`ItemJsonLoader`) merged into the
  existing registries — content no longer requires hand-written Java classes.
- Two new 6-piece, 8-flavor armor sets: Ancient Celestial, Empyrean.
- Ring of the Archer.
- Locations fast-travel panel (`Ctrl+L`), open to all players.
- `run.bat` / `run.sh` launcher scripts.

### Changed
- Colosseum arena ladder extended from level 500 to 750.
- Player level cap extended from 200 to 500 (continuing the real XP curve).
