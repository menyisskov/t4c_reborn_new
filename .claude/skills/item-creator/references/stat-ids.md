# statId reference for item `boosts[]`

Every entry in an item's `boosts[]` array is `{boostId, statId, expression}`. `statId` picks
*what* the boost affects; `EquipmentBonusRules.bonus(player, statId)` sums the evaluated
`expression` of every currently-active boost with that `statId` across all equipped items
(see `item/EquipmentBonusRules.java`), and `player/Player.java` reads those sums when computing
derived stats/combat numbers.

## Core attributes

| statId | Effect |
|---|---|
| 1  | Intelligence |
| 2  | Endurance |
| 3  | Strength |
| 4  | Wisdom |
| 6  | Dexterity / Agility |

`5`, `7`, `11` appear unused/reserved in the current codebase — don't use them for a new item
without first grepping to confirm they're still free.

## Combat

| statId | Effect |
|---|---|
| 8  | Attack skill |
| 9  | Dodge skill |
| 10 | Weapon/melee damage bonus |
| 10012 | Attack (alternate/boost-flavored variant of 8) |
| 10011 | Dodge (alternate/boost-flavored variant of 9) |
| 10001 | stun_blow skill |
| 10002 | powerful_blow skill |
| 10008 | parry skill |
| 10027 | armor_penetration |
| 10035 | archery skill |
| 10036 | two_weapons skill |

## Armor / defense

| statId | Effect |
|---|---|
| 20 | Armor Class |

## Elemental resist (damage taken reduction) — one per school, no others exist

| statId | Element |
|---|---|
| 12 | Air resist |
| 13 | Fire resist |
| 14 | Water resist |
| 15 | Earth resist |
| 21 | Light resist |
| 22 | Dark resist |

There is **no separate "physical" or "disease" resist statId** — only these six school-based
resists exist as item boosts. Don't invent one.

## Elemental power (spell damage dealt with that school)

| statId | Element |
|---|---|
| 16 | Air power |
| 17 | Fire power |
| 18 | Water power |
| 19 | Earth power |
| 23 | Light power |
| 24 | Dark power |

Resist and power share the same six-element set (Air/Fire/Water/Earth/Light/Dark) but use
**different statIds** — e.g. Fire resist is 13, Fire power is 17. Don't mix them up.

## Utility / thief skills

| statId | Effect |
|---|---|
| 10014 | hide |
| 10015 | rob |
| 10016 | sneak |
| 10026 | picklock |
| 10028 | peek |
| 10029 | rapid_healing |

## boostId — must be globally unique across the *entire* item catalog

`boostId` is not just unique within one item's file. At runtime, `EquipmentBonusRules.bonus()`
collects every equipped item's active boosts into a single `Map<Integer boostId, ActiveBoost>`
keyed by `boostId` (see `item/EquipmentBonusRules.java`), then sums by `statId`. If two
*different* items a player has equipped at the same time happen to share a `boostId`, the map
`put` silently drops one of them instead of adding both — a real, silent correctness bug, not
just a lint issue.

**How to find a free block of ids:**

```bash
# highest boostId currently used anywhere under assets/items/
grep -rhoE '"boostId": [0-9]+' assets/items/*.json | awk '{print $2}' | sort -n | tail -1
```

As of this skill being written, JSON-authored items use two ranges:
- `20000`–`20701`: sequentially assigned by `tools/ArmorSetGenerator.java` for the Ancient
  Celestial / Empyrean sets (one counter, incremented per boost across all 96 files).
- `30000`–`30001`: `ring_of_the_archer.json`.

Legacy Java items (`item/definition/*.java`) use small, low integers (e.g. `708`, `982`-`988`,
`304`) assigned ad hoc per file — these are numerous (thousands of legacy files) and not worth
enumerating by hand.

**Rule of thumb:** take the highest id found by the grep above, round up, and claim a fresh
block comfortably clear of it (e.g. if the max is `30001`, start your new item's boosts at
`31000`+). If you're generating a multi-piece set, reserve a big enough contiguous block up
front (the existing generator uses a single incrementing counter across the whole run) so a
second batch you add later won't collide with this one. Always re-run the grep right before
writing new ids — another item may have been added since you last checked.

## BodyPart enum (`player/BodyPart.java`)

`LEGS`, `FEET`, `BODY`, `HEAD`, `BELT`, `NECK`, `BRACER`, `BACK`, `RING1`, `RING2`, `LEFT_ARM`,
`RIGHT_ARM`, `LEFT_HAND`, `RIGHT_HAND`, `SHIELD`, `WEAPON`, `WEAPON2`, `BOOT`, `ROBELEGS`,
`HAIR`, `HAT`, `MASK`, `CAPE`.

Use `BACK` for capes and mantles — `CAPE` has no inventory slot (see `SKILL.md`).

Two-slot pieces (e.g. gauntlets/gloves) use `bodyPart` for one hand and `secondaryBodyPart` for
the other (`LEFT_HAND` + `RIGHT_HAND`), each with its own
`appearanceEquippedPrimary`/`appearanceEquippedSecondary` sprite — see
`tools/ArmorSetGenerator.java`'s gauntlets `Piece` entry.
