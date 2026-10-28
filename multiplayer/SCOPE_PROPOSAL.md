# Two-player checkpoint scope: proposal

> **Superseded in part (2026-10-28).** The owner ruled that the online game must match
> single-player in everything except having a client and a server. The exclusions and the
> "Warrior only" fallback in this proposal are now temporary divergences to close, not a scope
> limit. See [PARITY.md](PARITY.md) for the rule, the checklist and the build order. The
> two-player starter route below remains a useful intermediate checkpoint.

Status: **APPROVED by the owner on 2026-10-27 with the amendments below.** Drafted the same day,
Asia/Jerusalem. [MILESTONES.md](MILESTONES.md) "Scope must be explicit before completion" remains
the authority and records the approval. Sections still say PROPOSED in their headings; read them
as approved unless this block amends them.

Owner amendments (2026-10-27):

- **Paladin starts with a mace and shield** (the Recruit's Mace, not a sword). Section 1's
  "sword and shield" reads "mace and shield". Melee online must therefore support the mace.
- **Every mage starts with a new entry-level spell** (Arcane Spark): slightly weaker than Fire Dart
  and with no school or magic-power orientation, unlike Mana Burst's air-resistance-gated bolt.
  Mage and Battle Mage get it now; Cleric and Healer keep their current starting spells. Online
  casting scope is unchanged: only Light Heal is in the checkpoint.
- **Archer starts with a basic bow and quiver** (the Ashwood Reflex Bow and the new Recruit's
  Quiver, plus arrows). Online archery stays out of the checkpoint (section 1), so this changes
  offline play and the creation kit only.
- The rest of the proposal is approved as written.

Each item below was labelled **PROPOSED**. Rejecting any item did not authorize weakening the
authored content to fit a smaller hosted area. This checkpoint does not replace full Beta; the owner later set its concurrency gate to two real rendered clients, with all other Beta gates unchanged.

Every content fact below comes from the packaged definitions at commit `c11ca7a6`:
`RenewedWards.java`, `WardenAelric.java`, `r215Skeleton.java`, `RenewArmor.java`, `QuestService.java`,
`CharacterClass.java` and the server exports `content/monsters-v1.json`, `items-v1.json`,
`portals-v1.json` and `npc-interactions-v1.json`. Re-verify these facts before approval if any of
those files change.

## 1. Starter classes and required abilities: PROPOSED

There are seven creation classes (`CharacterClass`): Warrior, Archer, Paladin, Cleric, Healer, Mage
and Battle Mage. Online play currently supports only restricted physical melee. It rejects bows,
stun blow, active buffs and spell casting.

| Class | Proposed | Required abilities for the checkpoint | Reason |
| --- | --- | --- | --- |
| Warrior | **Yes** | Melee attack with the starting greatsword; drink potions (`light_healing_potion` from the common kit; `healing_potion`/`light_healing_potion` loot); equip/unequip, including looted Leather gloves. | Has no spells and is closest to the existing melee slice. |
| Paladin | **Yes** | Melee with mace and shield (owner amendment: starts with the Recruit's Mace); cast **Light Heal** (`spell.heal_light`, its only starting spell) on self; drink potions; equip/unequip. | Adds one real cast (mana cost, cooldown and effect) with minimal surface, which satisfies M4's "functional caster" rule for one class. |
| Archer | No | — | Bows/quivers/ammunition are unsupported online. |
| Cleric, Healer, Mage, Battle Mage | No | — | Their offensive starting spells (`stone_shard`, `fire_dart`) need projectile/spell-damage support. |

Fallback for owner choice: **Warrior only**. In that case, no casting is in scope, and M4 must say
the checkpoint has no caster. The other five classes stay creatable offline. Online creation should
refuse them with an explicit "not yet available online" message. They must not be hidden, and they
must not create a character that cannot fight.

Also required for both classes: aggressive monsters acquire targets, pursue, attack and reset
(M4). Death, half-HP return and corpse recovery already exist. Stat/level progression uses
`PlayerProgression`. **Renew Armor** (the quest reward) is *granted and persisted*. Casting it is
**not** in scope, because it recasts buff spells (Barrier, Protection and others) that neither class knows.
Active buffs also make the online combat loadout fail closed.

## 2. Authored quest: `renewed_wards`. PROPOSED, with verified gaps

Verified definition (`RenewedWards.definition()`, `QuestDef` 21-argument constructor):

| Field | Value |
| --- | --- |
| Giver | `WardenAelric`, `@Spawn` world 0 (2948,1044), `stationary=false`, `aggressive=false`; dialogue topic 0 triggers `GIVE_QUEST renewed_wards` |
| Objective | 12 kills of `Skeleton`. The **monster's** tile must be in world 0, within radius 50 of (2785,1095). `MainGameScreen` passes the monster's tile, not the player's. |
| Activation / item / level gates | none (`activationFlag`, `requiredItemKey` and `minLevel` are null/0) |
| Rewards | 500 gold, 400 XP (`addXpExact`), spell `${spell.renew_armor}`; no item reward |
| Flags written | `quest.renewed_wards.status` (1 active, 2 done), `quest.renewed_wards.kills`, and `killlog.Skeleton` (incremented on *every* kill by `recordKill`) |
| Text | `quest.renewed_wards.{title,offer,completion,completed}` in `assets/i18n/lang.json` |

Verified content in the objective circle (`monsters-v1.json`): **13 `Skeleton` spawns**, all
non-stationary, aggressive, at (2767..2802, 1071..1115). Skeleton: level 4, 69 HP, one physical
attack `1d7+3`, 7–22 gold, 59 death XP, 30 s respawn. Loot: Leather gloves 3%, Ring of confidence
1%, Healing potion 2%, Light healing potion 3%, Skeleton bone 0.07%. All five resolve in
`items-v1.json` to `item.leather_gloves`, `item.ring_of_confidence`, `item.healing_potion`,
`item.light_healing_potion` and `item.skeleton_bone`. None is unique or undroppable. The circle also
holds 1 Goblin and 1 Wandering Orc (aggressive, with loot) and 2 Olin Haad Guard 3.

### What is not hosted or implemented online (exact)

1. **Kill area is not hosted.** The starting crop is world 0 x 2904..2983, y 1029..1088. The
   nearest Skeleton (2802,1095) is 102 tiles west of the crop edge, and the circle spans
   x 2735..2835. Same-world adjacent regions do not support seam walking (MAP_TRANSFER_DESIGN.md),
   so the plan needs one larger world-0 region (for example origin (2720,1020), 264×136). A
   rectangle that size contains 20 Olin Haad Guard 3, 13 Skeleton, 4 Goblin, 2 Wandering Orc,
   1 Wild Horse and one **unresolved custom spawn `SUNDIAL` at (2881,1070)** (an NPC class, with
   no monster definition). Walkable connectivity from Aelric to the circle is **unverified**.
   Run a footprint BFS like the one used for portal 16 before committing to a rectangle.
2. **Portals 12, 13 and 14 sit inside the kill circle** at (2770,1094), (2770,1095) and (2771,1094).
   All three target world 1 (134,667), which is unhosted. A larger region must keep them
   explicitly refused.
3. **Warden Aelric is not in the online NPC catalog.** `npc-interactions-v1.json` lists only
   Kilhiam. There is no online quest accept, progress or turn-in service. `OnlineNpcInteractions`
   handles only the letter.
4. **Skeletons cannot be attacked online today.** `OnlineMelee` rejects any definition with non-empty
   `loot` (`unsupported_combat`). General ground loot (LOOT_DESIGN.md) is therefore a hard
   prerequisite of this quest, not an optional extra.
5. **Accepting the quest would disable melee.** `CrimsonscaleLetterRules.hasUnsupportedCombatFlags`
   allows only `ADDON_CRIMSONSCALE_LETTER=1`. `quest.renewed_wards.*` and `killlog.*` flags are
   rejected. This needs an explicit allowlist of supported quest flags.
6. **No kill-credit producer.** The lethal strike commits gold, XP and the world-life claim, but
   no quest or kill-log flag.
7. **No aggressive or pursuit AI.** Online creatures retaliate only after provocation and do not
   chase. Authored Skeletons are aggressive (aggro 100).
8. **Spell reward is not representable as authored.** Offline grants the display-name identity
   `${spell.renew_armor}`. Online `CharacterProgress.spells` accepts only normalized `spell.*` keys
   and rejects that string. There is no headless spell catalog (no `spells-v1.json`) to validate
   the key. A shared, tested name→key mapping and spell export are required.
9. **No online carry-weight engine.** Turn-in itself grants no item. Loot pickup needs the shared
   `InventoryService` weight rule (see LOOT_DESIGN.md).
10. **Prose/data mismatch (flag only, do not fix here).** `quest.renewed_wards.offer` says the
    skeletons are "east of the temple grounds". The objective centre (2785) is about 163 tiles
    *west* of Aelric (2948). Route this to the technical-writer pass. Do not move the objective.

Do not shorten the kill count, move the circle, or drop the spell reward to make online play pass.

## 3. Shared-kill credit: PROPOSED

- **Credit goes to the lethal-strike claimant only.** That is the character whose lethal
  `commitCreatureStrike` wins the existing `online_world_rewards` claim for that life's event UUID.
  This matches offline (one killer) and reuses the existing atomic claim. A losing concurrent strike
  already rolls back completely.
- Quest progress, the kill-log flag, gold, death XP and the loot drop draft commit in the **same**
  transaction as the lethal strike. Progress counts only if, at draft time, the claimant's committed
  `quest.renewed_wards.status` is 1, the creature's authoritative death tile is inside the circle,
  and the definition matches `Skeleton` (canonical name). Kills above 12 are capped, as offline.
- Replay protection: the life event UUID and payload fingerprint make a retried kill idempotent. An
  old creature life cannot be credited twice. Another player's progress is never touched.
- Other damagers keep their per-hit XP only. No assist credit, party split or tagging is offered
  (parties are excluded).
- Both players can finish independently: 13 spawns with a 30 s respawn, and per-character flags.

## 4. Loot-claim rule: PROPOSED

The full design is in [LOOT_DESIGN.md](LOOT_DESIGN.md). In summary:

- Monster gold continues to go straight to the lethal claimant, as offline does. It is never ground
  loot. Rolled items become persistent ground loot at the death tile.
- **Owner window: 60 s** in which only the lethal claimant may pick up. After that, any player in
  the same region and in range may pick up. Loot expires after **15 min** and leaves the world.
  The row is kept as `EXPIRED` for audit and is never brought back. Offline ground items have no
  timer, so this lifetime needs explicit owner approval.
- **Simultaneous pickup:** first committed claim wins. The ground row's state changes from `ON_GROUND` to
  `CLAIMED` inside the claimant's inventory transaction. The loser gets `loot_unavailable` and an
  unchanged inventory.
- **Full inventory:** the shared carry-weight rule refuses the pickup. The item stays on the ground
  under its existing window, and the inventory is unchanged. There is no partial pickup of a
  single instance. Stack quantities are conserved.

## 5. Platform: PROPOSED

- Windows 11 x64 native `OnlineClient` only, using the pinned GLFW build (T4C-0189). No macOS,
  Linux or browser client is in scope.
- Two stages:
  1. Local QA: two rendered clients on one PC against loopback HTTP.
  2. Checkpoint acceptance: **two Windows machines** over the trusted HTTPS endpoint (TLS.md).
     M6 states that one PC is not remote evidence.
- One authoritative server process with PostgreSQL 16, as today.

## 6. Exclusions for the checkpoint: PROPOSED

None of these exclusions removes the feature from full Beta, and none is silently added:

- Public registration (operator-issued keys remain), trading, parties and shared XP, PvP, guilds.
- Archer, Cleric, Healer, Mage and Battle Mage online play (see section 1). Casting Renew Armor.
  Every spell other than Light Heal.
- Player-initiated dropping of inventory items to the ground. Offline supports this
  (`GroundItemManager.dropItem`), so it remains a Beta requirement. Merchants/shops, storage/bank,
  companions/taming, rebirth, world events, crafting.
- Every quest except `renewed_wards`. Kilhiam's remaining storyline beyond the letter.
  The dungeon (world 1) beyond the existing portal 16/17 interior. Portals 12–14 and every other
  portal stay refused.
- Scripted monsters (`sourceEvents`), special-encounter loot and `oneOf` choice tables. The
  Skeleton/Goblin/Wandering Orc tables use plain item entries.

## Approval checklist (owner approved all items on 2026-10-27, with the amendments at the top)

- [x] Classes: Warrior + Paladin (with Light Heal), Paladin on mace and shield.
- [x] Quest `renewed_wards` unchanged, with the larger world-0 region it needs. Server region hosted by T4C-0231 (Q10): `[2735,2984) x [1029,1146)`, 249 x 117; evidence in HOSTED_STARTER_ROUTE_DESIGN.md "Implemented".
- [x] Lethal-claimant-only kill credit.
- [x] Loot owner window of 60 s, expiry of 15 min, first-commit-wins, refusal on full inventory.
- [x] Windows only; acceptance on two machines.
- [x] Exclusion list above.
