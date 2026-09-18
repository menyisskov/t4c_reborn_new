# Leveling overhaul + four endgame zones (100–500)

## The bug

`Stats.currentXp` / `Stats.xpToNextLevel` were `int`, and so was `XpCurve.Entry`'s
`totalXp`/`xpToNextLevel`. The curve generator that produced the old
`XpCurveDefinitions.java` (`XpCurveExtender.java`, now removed) accumulated `totalXp` in a
plain `int`, which **silently overflowed into negative numbers starting at level 541**
(`new XpCurve.Entry(541, 11388415, -2138910889)` — visible directly in the old file's git
history) and wrapped a second time back into small positive numbers by level ~689. On top of
that, the curve's own growth rate (`xpToNextLevel(level) ≈ 42 * level^1.9`, fitted by the old
generator) grew far more slowly than the XP some high-level monsters award — the `ArenaMobXP`
family in particular scales roughly as `level^4`. The combination meant a single kill of a
top-tier monster (`ArenaMobXP500`, 15.2M XP, ×5 with `GameConstants.SERVER_XP_RATE`) could
grant more than 7 levels' worth of XP in one hit at level 500, and the overflowed `totalXp`
values made anything reading that field beyond level 541 nonsensical.

## The fix

1. **Widened every XP-carrying field to `long`**: `Stats.currentXp`/`xpToNextLevel`,
   `XpCurve.Entry`'s two fields and its getters, `PlayerStateDto`'s save-file fields (Gson
   round-trips old `int`-valued saves into these `long` fields with no migration needed),
   `DeathPenaltyService.Result.xpLost` (with a new `percentageLong` helper — the old
   `percentage(int,int)` silently clamped at `Integer.MAX_VALUE`), the `.setxp` GM command, and
   `T4CContentStudio`'s XP-curve JSON API.
2. **New curve** (`src/main/java/com/perso/T4C/tools/XpCurveHardener.java`, replacing the
   removed `XpCurveExtender.java`): levels 1–99 are untouched (no discontinuity for existing
   pacing). From level 100 on, `xpToNextLevel(level) = round(BASE * x^p(level))` where
   `x = level/100` and, critically, **the exponent itself grows with level**
   (`p(level) = 2.5 + (level-100)/200`, from 2.5 at level 100 up to 7.0 at the new level-1000
   cap). This means the curve's steepness compounds rather than staying a fixed power law, so it
   structurally outgrows *any* fixed-exponent monster-XP curve — including the `level^4`-ish
   `ArenaMobXP` family — well before level 500, by a wide margin by level 1000. `BASE` is read
   from the existing level-100 entry, so it's continuous with the unchanged 1–99 range.
3. Verified against every monster in the codebase with a resolvable `level`/`xpOnDeath`: the
   worst remaining single-kill level-skip anywhere is **~3.7 levels**, from three named,
   presumably one-time "world boss" encounters (`Jormungand`, `Fenrir`, `AnthorTheMad`, all
   level 100) — left untouched as likely-intentional legendary-kill payouts rather than farmable
   content. Every ordinary farmable monster, `ArenaMobXP` included, now takes multiple kills per
   level at its own level band, and that ratio gets *stricter*, not looser, as level increases.

## Fixed along the way: Mirak Nira's dead "100 goblins" quest

`MirakNira.java` already had a fully-implemented "kill 100 goblins to earn my trust" branch
(reads `__GOBLINS_KILLED_BY_HERO`, rewards `ring_of_trust`, adjusts karma) but **nothing in the
codebase ever incremented that flag** — `OriginalNpcScriptMacros.java` maps it to a legacy
numeric flag id (30056) that would have been bumped by an original-game monster `OnDeath`
script, and that script-execution path was never carried over. The quest was silently
unwinnable. Fixed by incrementing the flag from the existing generic player-kill callback in
`MainGameScreen.java` whenever the killed monster's clan resolves to `GOBLIN` — works for every
existing goblin spawn in the world, not just new content.

## Four new zones, levels 100→500

Two are genuinely new monsters; two "activate" fully-stat'd but never-placed legacy bosses
(`LesserDrake`/`GreaterDrake` had real, varied stats — resists, attack formulas, attributes —
but `xpOnDeath: 0` and zero `@Spawn` annotations, so they were unreachable and worthless to
fight even if found). All four follow the same shape as the previous content pass: trash +
boss, calibrated against the *new* curve (not the old one), 2 items each, 1 kill-quest + 1 new
quest-giver NPC.

| Zone | Levels | Trash | Boss | Center (worldmap) |
|---|---|---|---|---|
| Windhowl Marches | 100–150 | Centaur Warrior (activated) | Centaur King (activated) | (1650, 1550) |
| The Hollow March | 150–200 | Barrow Wight (new) | The Hollow King (new) | (1600, 1800) |
| Lesser Drake's Aerie | 200–260 | Kraanian Wyrmling (new) | Lesser Drake (activated) | (1900, 1950) |
| Greater Drake's Bastion | 400–500 | Bastion Warden (new) | Greater Drake (activated) | (2000, 2300) |

`CentaurWarrior`/`CentaurKing` existed as barely-authored placeholder stubs (health 1000,
every attribute a flat 100, `xpOnDeath: 0`, `aggro: -100` i.e. permanently passive) — not
"real, tuned legacy content" the way the Drakes were, so those two got a full stat rewrite
rather than a light activation; only the class files, sprite names, and sound constants were
kept.

Reward-per-quest gold/XP and monster `xpOnDeath` were calibrated directly against the new
curve's actual `xpToNextLevel` at each zone's level band — targeting roughly 9 kills/level for
trash and 3–4 kills/level for a zone boss — rather than extrapolated from lower-level data
points, since a naive linear extrapolation undershoots badly once the curve's exponent starts
climbing past level ~200 (an error caught and corrected during this pass for the Bastion
Warden specifically: a first attempt at ~125k xpOnDeath would have needed over 300 kills to
gain one level at level 430; the calibrated value is 4.75M).

## What this pass didn't do

- No new spells this round, to keep four zones' worth of new content in scope.
- The three legendary-tier "instant multi-level" bosses noted above weren't retuned — flagged,
  not silently left broken.
- Spawn placement is grep-verified against every other `@Spawn` coordinate (zero collisions in
  each zone's radius), not visually verified in the map editor — same caveat as the previous
  content pass, for the same reason (no display in this environment).

## Update: canon cross-check (see `2026-09-canon-verified-additions.md`)

Written without access to `t4cfantasy.com`/`t4cbible.com` (egress was blocked at the time — see
the previous doc's research note for the same limitation). A later pass got real site access and
confirmed `LesserDrake` (level 250, health 15,385) and `GreaterDrake` (level 500, health 54,943)
are **exact matches** for the real T4C "GM"-category Drake ladder, not just plausible numbers —
and that the real ladder has one more rung above them: **Arch Drake, level 1000, health 206,623**.
The newer pass adds a fifth zone, Drake's Lair, that activates that missing top rung — a natural
capstone for this pass's level-1000 curve cap, since Arch Drake sits exactly there. It also
surfaced a real, previously-dormant limitation this pass didn't hit: `MonsterDef.xpOnDeath` and
`QuestDef.rewardXp` are both still `int`, which the level-1000 curve's own math can overflow —
worth knowing if a future pass calibrates a boss above roughly level 800. See the newer doc for
specifics.
