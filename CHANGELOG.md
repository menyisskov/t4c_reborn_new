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
