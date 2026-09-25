---
name: balance-change
description: How to safely change a game rule or number in T4C Reborn - caps (level, rebirths, stats), formulas (damage, odds, AC, costs), requirement curves, or any value that existing saves, the website, docs or other content depend on. Use whenever the user asks to cap, rebalance, tune, raise/lower a limit, "make X make sense", or changes a number that players see; use it together with spell-creator / item-creator when the change is to spells or items.
---

# Balance changes without regressions

A balance change is rarely one number. It has a **source** (the constant or formula), several
**consumers** (game logic, saves, the website exporter, docs, NPC dialogue, tests) and a
**history** (players whose saves predate it). The bugs Codex has caught here were all missed
consumers or history. Work through the four passes below in order.

## 1. One source of truth

- Put the rule in exactly one place: a constant in `config/GameConstants`, or a small helper
  class with static methods that says what the number means. Existing examples:
  `HighTierSpellCurve`, `ItemBalance`, `RebirthBehavior.requiredLevelFor`,
  `SeraphAuraService.healingChance` / `effectivePercent`.
- If the same formula is written inline in two places (an NPC and the exporter, say), extract
  it first, then change it once.
- Read `DESIGN_GUIDELINES.md` for the rule's current definition, and update it in the same
  pass. The owner's stat budget (5 points per level, cap 400, 50 rebirths, starting attributes
  20 + 5n) is the yardstick for any requirement.

## 2. Find every consumer

```bash
grep -rn "<CONSTANT_OR_METHOD>" src/ compendium/app.js DESIGN_GUIDELINES.md .claude/skills CLAUDE.md
grep -rn "<old literal value>" src/ assets/ compendium/app.js DESIGN_GUIDELINES.md .claude/skills
```

Common consumers people miss:
- **Loading saves:** `helper/PlayerStateMapper.applyToPlayer`.
- **GM commands:** `input/GmCommandProcessor` (for example, `.level` or `.rebirth` bypassing a cap).
- **NPC scripts** that re-implement the rule inline, such as the Oracle's level gate.
- **The website exporter:** `tools/CompendiumExporter`, and whether it copies the number or
  calls the helper.
- **Generators:** `tools/ArmorSetGenerator`. Re-run it; never hand-edit generated files.
- **Tests with hard-coded expectations.**
- **i18n dialogue** that states the number in prose.

## 3. Existing players (history)

For every new or tightened limit, decide what happens to a save that is already past it:
- **Clamp display/scale values** that the game derives on the fly (level to the cap, a
  rebirth count used for aura strength).
- **Don't claw back things the player chose or spent.** Allocated stat points and bought
  upgrades stay. If another system derives numbers from a saved flag (like `RemortNPC2` using
  `__FLAG_NUMBER_OF_REMORTS`), leave that flag alone.
- Write a test that builds an over-the-limit `PlayerStateDto`, loads it, and asserts the
  outcome (see `PlayerStateMapperTest.clampsSavesFromBeforeTheLevelAndRebirthCaps`).
- Say in the PR which save fields you clamped and which you deliberately kept.

## 4. What players see

- The website must publish **the value the game actually uses**. Export it from the helper,
  not by re-typing it in the exporter or in `app.js`.
- Check probability edges by hand for one small and one capped value. `nextInt(101) <= c` is
  (c+1)/101. `nextInt(100) < c` is c/100.
- Turn the rule into an invariant test so the next change can't silently break it. For
  example: every school has one spell per tier; every item's AC matches its formula; no
  requirement is out of reach at its level; IDs are unique; displayed chance equals computed
  chance.
- Regenerate compendium data and, for page changes, run `verify-website`.

When done, continue with `ship-pr`.
