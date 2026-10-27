# M3/M4 design: authoritative online quests and starter-role abilities

Status: **design proposal, not implemented.** Drafted 2026-10-27, Asia/Jerusalem. The content scope
(classes, `renewed_wards`, lethal-claimant kill credit) is **PROPOSED, pending owner approval** in
[SCOPE_PROPOSAL.md](SCOPE_PROPOSAL.md). This file designs the mechanics only. It does not shorten the
quest, move its circle, alter rewards, or claim any milestone gate. Facts below were read from the
source at the merge of `origin/main` into `worktree-beta-milestone-work` on 2026-10-27; re-verify them
if `QuestService`, `SpellCastingService`, `ItemUseService` or `OnlineStore.applyReward` change.

Related: [LOOT_DESIGN.md](LOOT_DESIGN.md) (M2; Skeletons are unattackable until its L8 lands),
[NPC_INTERACTION_DESIGN.md](NPC_INTERACTION_DESIGN.md) (the `/interact` path this extends),
[MILESTONES.md](MILESTONES.md) M3/M4, and [PEER_WORK.md](PEER_WORK.md) (reserved files are binding).

## 1. Offline behavior to preserve (verified)

**Quests (`QuestService`, `QuestDef`, `RenewedWards`, `WardenAelric`, `ScriptedNpc`).**

- Flags: `quest.<id>.status` (0 not started, 1 active, 2 completed), `quest.<activationFlag|id>.kills`
  and `killlog.<canonical monster name>`. `statusFor` also treats a non-zero `activationFlag` as active.
- Greeting a giver runs `turnInReadyQuests` first (`ScriptedNpc`, `automaticallyTurnInQuests`). Topic 0
  of Warden Aelric (`GIVE_QUEST renewed_wards`) runs `giveOrReport`: not started -> set status 1 and
  kills 0, reply offer text; completed -> completed text; otherwise a progress line with counts.
- `recordKill(player, monster, world, tileX, tileY)` increments `killlog.*` on **every** kill, then for
  each active quest whose target matches (case-insensitive, or via the canonical registry name) and
  whose circle contains the **monster's** tile (`dx²+dy² <= r²`) sets kills to `min(required, n+1)`.
- `complete`: refuse first if a reward item fails `InventoryService.canAdd`; then consume the required
  item (`requiredItemQty` copies of the normalized `item.<key>`), set status 2 and kills = required,
  add gold, `addXpExact(rewardXp)`, set `unlock.zone.<id>` if any, add the reward item, add the reward
  spell's name if not already known. `minLevel` blocks turn-in only.
- `addXpExact` is `addXp(..., applyMultiplier=false)`, i.e. `PlayerProgression.scaledAward(xp, 0, false)`
  plus `addScaledXp`. `OnlineProgression.award` already computes exactly that, so it is reused.
- `renewed_wards`: 12 `Skeleton`, world 0, circle (2785,1095) r 50, 500 gold, 400 XP, reward spell
  display name `${spell.renew_armor}`, no item gates. Aelric spawns at (2948,1044), **inside** the
  current temple crop (x 2904..2983, y 1029..1088); the kill circle is not.

**Light Heal (`HealLight`, `SpellCastingService.begin`, `MainGameScreen` self-cast).**

- Order of checks in `begin`: sanctuary (attack/self-centred-attack only, so not Light Heal) -> known
  spell -> target type (Light Heal `targetType` 3 accepts SELF) -> PvP -> range/LOS (skipped for SELF)
  -> spell cooldown -> mental exhaustion or stun -> mana (`manaCost "2"` via `DiceFormula`) -> success
  roll (`nextInt(101) > rate` fails; rate "100") -> deduct mana, apply exhaustion
  (mental `1000+max(0,530-(level-3)*20)` ms, physical and attack `750+...` ms), trigger cooldown (0 s).
- Casting does **not** check `minInt/minWis/minLevel` (those gate learning). A full-HP self-cast is
  allowed and spends mana. Heal = first `effectType 1` formula
  `(1d5+8+self.wis/23)*self.light/100)` with effective wisdom and light power (light skill, or 100 if
  unlearned, plus boosts); `applyHeal(d,d)` clamps to max HP with `healingReceivedPercent`.

**Potions (`ItemUseService.useOnSelf`, `ItemPotionOfLightHealing` id 10176).**

- Ownership by key; pick the first item spell whose `nextInt(100) < chance` (100 here); refuse
  `NO_HEALING_NEEDED` at full HP / `NO_MANA_NEEDED` at full mana; `beginItemUse` (no known-spell check,
  no interruption) still refuses on spell cooldown, **mental exhaustion** and stun; potion cost and
  exhaustion are "0"; heal 25; `useCharge` decrements a per-key pooled charge count.

**Regeneration.** Offline `Player.tickNaturalRegeneration` applies `RegenerationRules.regenerateHp/
regenerateMana` every 2 s while alive. **The online server has no regeneration at all** (no match for
`regen` under `multiplayer/server`). Without it a Paladin cannot sustain Light Heal; see A5.

## 2. Transaction primitive: reuse, do not add schema

`OnlineStore.commitGameplayEvent(session, revision, eventId, gold, xp, progress)` (and the
`commitCreatureStrike` variant) already gives what M3/M4 need, with **no schema change**:

- character row `FOR UPDATE`, receipt lookup before the revision check, payload fingerprint over the
  whole encoded `CharacterProgress` (flags, inventory, spells, cooldowns, vitals), revision CAS, gold/XP
  column increments, `revision+1`, one commit.
- Retrying the **same** prepared draft with the same event UUID is idempotent; a different payload
  under that UUID is a `Conflict`; a stale revision is refused. A lost response is resolved by the
  existing rule: no automatic replay, disconnect on ambiguity, the committed state is visible on rejoin.

Every quest and ability mutation below is therefore "prepare one immutable draft on the simulation
owner from committed state, commit it once through the session worker, publish only after commit".
The quest/ability state lives entirely in `CharacterProgress` (`questFlags`, `spells`, `inventory`,
`cooldowns`, `vitals`), which is already durable, versioned and validated (`spell.*`/`item.*` keys).

## 3. M3 quest service

### 3.1 Shared rules (extracted, not copied)

New pure `com.perso.T4C.quest.QuestRules` (domain build) takes `Map<String,Integer>` flags and plain
values instead of `Player`, and `QuestService` delegates to it. It owns: `statusFlag`, `killsFlag`,
`killLogFlag(canonicalName)`, `status(flags, def)`, `kills(flags, def)`, `inObjectiveArea(def, world,
x, y)`, `accept(flags, def)`, `creditKill(flags, def, canonicalName, world, x, y)` (returns new flags,
including the unconditional kill-log increment), `readiness(flags, def, requiredItemCount, level)`
(`NOT_STARTED | COMPLETED | NEEDS_KILLS | NEEDS_ITEM | NEEDS_LEVEL | READY`) and
`completionFlags(flags, def)`. Canonical monster lookup stays in `QuestService` (it needs
`MonsterRegistry`); the server passes `MonsterContent`'s definition name, which is the canonical name.

New pure `com.perso.T4C.spell.SpellKeys.normalize` maps a display identity (`${spell.renew_armor}`) to
the normalized key (`spell.renew_armor`, collapsing `spell.spell_` like `SpellData.getKey`).
`SpellData.getKey` delegates. This resolves gap 8 of SCOPE_PROPOSAL.md without changing offline saves,
which keep storing display names.

### 3.2 Content export

`quests-v1.json` (new exporter, allowlist `{renewed_wards}`) carries the full `QuestDef` objective and
reward fields. Validation at export **and** at server load (`QuestCatalog`): target monster exists in
`monsters-v1.json`; `requiredItemKey`, if any, is already `item.<key>` and exists in `items-v1.json`
(CLAUDE.md's recorded bare-key failure); `rewardItemKey` resolves; `rewardSpellKey` normalizes through
`SpellKeys` and exists in `spells-v1.json` (A1). Unsupported fields (`unlockZoneId`, multi-master
completion, crafting completion) fail closed until needed. Text stays as i18n keys; the client resolves.

### 3.3 Dialogue protocol (extends `/interact`)

Body becomes `REVISION EPOCH NPC_ID [TOPIC]` where `TOPIC` is a 0-2 digit authored topic index. The
server keeps the existing checks (session, rate, `admit(token,epoch)`, not busy, revision, region, range,
line of sight) and then dispatches by NPC: Kilhiam -> existing letter rule; Warden Aelric -> quest rule.

| Request | Committed state | Effect | Reply |
| --- | --- | --- | --- |
| greet | status 1 and READY | turn-in draft (3.5) | completion text, `changed=true` |
| greet | otherwise | none | welcome text, `changed=false` |
| topic 0 | status 0 | status 1, kills 0 | offer text, `changed=true` |
| topic 0 | status 1 (any kill count) | none | progress key + `[kills, required, remaining]`; offline `giveOrReport` reports even at 12/12 and leaves the turn-in to the greeting |
| topic 0 | status 2 | none | completed text |
| topic 1 | any | none | topic text |

`NpcInteractionReply` gains `List<Integer> args` and accepts `npc.`, `quest.` and `message.` keys. No
reply ever contains another player's data, balances, or client-supplied values. A no-op reply may keep
the request revision, exactly as the letter's repeat greeting does.

### 3.4 Kill credit (lethal claimant only, per SCOPE_PROPOSAL.md section 3)

In `OnlineMelee`, when `after == 0`, apply `QuestRules.creditKill` for **every** cataloged quest to the
claimant's draft flags **before** `commitCreatureStrike`. Inputs are server-only: the definition's
canonical name and the creature's authoritative absolute death tile (today the spawn tile, since
online creatures do not move; after M4 AI, the life's current tile). Consequences:

- Flags commit in the same transaction as damage, gold, XP, loot drafts and the `online_world_rewards`
  claim for `life.eventId()`. A losing concurrent strike rolls back entirely, so its kill never counts.
- A retried lethal strike reuses the same draft (same flags), so the fingerprint matches and nothing is
  counted twice. An old life cannot be claimed again (`online_world_rewards` primary key).
- Only the claimant's own progress is in the draft; other damagers keep per-hit XP only.
- Kills above `required` are capped (offline). Kill log always increments (offline).

`CrimsonscaleLetterRules.hasUnsupportedCombatFlags` currently disables melee for any flag except the
letter (gap 5). Replace its use in `OnlineMelee` with `OnlineQuestFlags.supported(flags, catalog)`:
letter = 1, `quest.<cataloged>.status` in {0,1,2}, `quest.<cataloged>.kills` in [0, required], `killlog.<name>` >= 0.
Anything else stays `unsupported_combat`.

### 3.5 Turn-in (atomic, exactly once)

`OnlineQuests.prepareTurnIn(CharacterState saved, QuestDef def, ItemContent items, long now, Random r)`:

1. Require `readiness == READY` against **committed** progress (status 1, kills, level, item count).
2. Required items: count only unequipped `inventory` instances whose `key` equals the normalized
   `item.<key>` (sum `quantity`). Consume exactly `requiredItemQty` units, oldest instance first,
   splitting a stack's quantity rather than destroying identities. Never consume an equipped or storage
   instance; if only equipped copies would satisfy the count, refuse `NEEDS_ITEM` (fail closed, since
   offline `remove(-1,key)` semantics with equipped copies are not proven here).
3. Reward item, if any: `CarryCapacityRules.canAdd` on the post-consumption inventory and effective
   strength; refuse the whole turn-in with `message.quest_reward_item_blocked` (offline order), no
   state change. A new instance gets a server UUID and `nbCharges` as in `OnlineNpcInteractions`.
4. `completionFlags` (status 2, kills = required), spell set gains `SpellKeys.normalize(rewardSpellKey)`
   (set semantics = offline "if not already known"), XP via `OnlineProgression.award(saved,
   rewardXp, now, r)` (level-ups and their random HP/mana gains are rolled once in the draft).
5. Commit `commitGameplayEvent(token, revision, UUID, rewardGold, awardedXp, draft)` once. Gold and XP
   use the existing column increments, so all rewards and the completion state share one receipt.

Exactly-once argument: a repeat turn-in reads committed status 2 and prepares no draft; two concurrent
turn-ins cannot both pass the revision CAS; a retry of the same draft is deduplicated by receipt; a
lost acknowledgement is resolved on rejoin by reading status 2 and the spell/gold already present.

### 3.6 Remaining M3 prerequisites outside this file's code

Hosting the kill circle (larger world-0 region, portals 12-14 refused, `SUNDIAL` resolved, BFS
connectivity), aggressive/pursuit AI (M4) and ground loot (M2 L6-L9) are required before a real
playthrough. They stay as listed in SCOPE_PROPOSAL.md; this design does not replace them.

## 4. M4 server-validated abilities

### 4.1 Shared rules (extracted)

- New pure `com.perso.T4C.spell.SpellCastRules.evaluate(CastInput)` holds `begin`'s checks in the same
  order, with a `CastInput` snapshot (spell fields, known flag, target kind, distance, LOS, PvP,
  `casterProtected`, `now`, cooldown/mental/stun expiry, mana, unlimited resources, formula `Context`,
  success roll). It returns `failure`, `manaCost`, the three exhaustion durations and the cooldown.
  `SpellCastingService.begin` builds the snapshot from `Player` and applies the result; its behavior and
  messages are unchanged.
- New pure `SpellHealRules.healthDelta(spell, context, random)` extracted from
  `SpellEffectManager.resolvePlayerHealthDelta` (including the min/max-damage fallback).
- `DiceFormula` rolls with `ThreadLocalRandom`; melee already accepts that. Each draft is rolled once
  and persisted, never re-rolled on retry, so non-injectable dice are acceptable. Tests use bounds.

### 4.2 Spell content

`spells-v1.json` (new exporter, allowlist): `spell.heal_light` (castable), the potion spells referenced
by in-scope potion items (10176 and the healing potion's id; the exporter derives these from
`items-v1.json` item spells rather than a typed list), and `spell.renew_armor` marked **grant-only**.
Fields: key, numeric id, cost/success/exhaustion formulas, cooldown, target type, LOS, attack flag,
`t4cEffects`. The server `SpellCatalog` refuses any spell whose effects are not a plain effect-1 heal or
effect-2 mana restore, so buffs, summons, teleports and dispels fail closed (`unsupported_spell`).

### 4.3 Cast: `POST /cast` body `REVISION EPOCH SPELL_KEY` (self target only)

`OnlineCasting.prepareSelfCast(saved, spellKey, now, random, items, spells)` on the simulation owner:
alive; not busy; revision; `progress.spells` contains the key; spell is castable in the catalog; active
buffs fail closed (as `OnlineCombatLoadout`); context from committed attributes + `OnlineEquipmentBonuses`
(effective wis/int/etc.) and light power from `skills["light"]` (100 if 0) + boosts; then
`SpellCastRules.evaluate` with expiries from `cooldowns`. Persisted cooldown keys, all absolute UTC
millis so reconnect cannot shorten them: `spell.<key>` (spell cooldown), `mental_exhaustion`,
`physical_exhaustion`, `attack_exhaustion` (already read by `OnlineMelee`), `stun` (existing).
Draft: mana minus cost, HP plus `applyHeal` semantics (minimum 1, clamp; `healingReceivedPercent` is 0
because buffs fail closed), the new expiries. Commit with gold 0, XP 0. A failed success roll offline
interrupts actions and spends nothing; online it returns `409 spell_failed` with no commit.
Physical exhaustion stops movement offline; the movement input handler must refuse moves until
`physical_exhaustion` (A7).

### 4.4 Potion: `POST /use` body `REVISION EPOCH ITEM_UUID`

`OnlineItemUse.prepareUse`: the instance is in `inventory`, not equipped and not in storage; item spell
selection with `random.nextInt(100) < chance`; full-HP/full-mana refusals; `SpellCastRules` with
known-spell not required (mental exhaustion and stun still block, as offline); apply the delta;
decrement **that instance's** `charges`, removing it at 0 unless `unlimitedUse`. Offline pools charges
per key; for `nbCharges == 1` both models are identical. Items with `nbCharges > 1` fail closed online
until the owner accepts per-instance charges.

### 4.5 Regeneration

`OnlineRegeneration.settle(progress, now, random, bonuses)`: anchor `cooldowns["regen_anchor"]`; apply
`floor((now-anchor)/2000)` `RegenerationRules` ticks (bounded, stop when full), advance the anchor by
whole ticks; dead characters regenerate nothing and re-anchor at `now`. Every vitals-changing draft
(cast, potion, melee, retaliation, turn-in) settles first, so the committed state is always current.
**Owner decision:** whether time logged out regenerates. Proposed: no; re-anchor at join.

### 4.6 Class gate

Online creation accepts only the approved classes (Warrior, Paladin) and refuses others with
`class_unavailable_online`, without hiding them. Approval-dependent.

## 5. Implementation tasks

Model routing: all are cheap-tier coding (AGENTS.md section 6). Tests follow AGENTS.md section 5: one
focused case per behavior plus real boundaries; integration only for consequential joins. Claim a TASKS
ID under the coordination lock when starting each one. "Reserved" means a file listed in PEER_WORK.md.

### Can start now (new files or unreserved delegating edits only)

| # | Work | New files | Delegating edits | Tests | Depends | Approval risk |
| --- | --- | --- | --- | --- | --- | --- |
| Q1 | `QuestRules` extraction | `src/main/java/com/perso/T4C/quest/QuestRules.java` | `quest/QuestService.java`; `multiplayer/domain/pom.xml` (add `QuestDef`, `QuestRules`; serialize pom edits with Q2/A2) | New `QuestRulesTest` (domain testInclude): accept idempotent, kill cap, circle edge `r` in / `r+1` out, activation-flag kills key, kill log always increments. Existing `QuestService*Test` pass unchanged | none | None (offline parity) |
| Q2 | `SpellKeys.normalize` | `spell/SpellKeys.java` | `spell/SpellData.java` (`getKey`); domain pom | Extend nothing; one `SpellKeysTest` (placeholder, `spell.spell_` collapse, already-normalized). `SpellRegistryParityTest` passes | none | None |
| A1 | Spell content export + catalog | `online/model/SpellContent.java`; `tools/OnlineSpellContentExporter.java`; `multiplayer/server/src/main/resources/content/spells-v1.json`; server `SpellCatalog.java` | none | Freshness test modelled on `OnlineMonsterContentExporterTest`; `SpellCatalogTest`: buff/teleport spell refused, grant-only not castable | Q2 | Low |
| Q3 | Quest content export + catalog | `online/model/QuestContent.java`; `tools/OnlineQuestContentExporter.java`; `content/quests-v1.json`; server `QuestCatalog.java` | none | Freshness test; `QuestCatalogTest`: bare `requiredItemKey`, unknown monster and unknown spell are rejected | Q1, Q2, A1 | Low |
| A2 | `SpellCastRules`, `SpellHealRules` extraction | `spell/SpellCastRules.java`, `spell/SpellHealRules.java` | `spell/SpellCastingService.java`, `spell/SpellEffectManager.java`; domain pom | `SpellCastRulesTest`: failure order, cooldown boundary (`now == until` ready), mana exactly equal allowed, roll boundary `nextInt(101)` edge; existing spell/item-use tests pass | none | None |
| S1 | Progress copy helper | server `ProgressEdits.java` (`withFlags/Vitals/Spells/Inventory/Cooldowns`) | optional; do not refactor reserved callers | Covered by Q5/A3 tests | none | None |
| Q4 | Quest flag allowlist | server `OnlineQuestFlags.java` | `OnlineMelee.java` (replace letter-only check) | `OnlineQuestFlagsTest` (allowed set, kills above required refused); move `OnlineNpcInteractionsTest` lines 32-33 expectations | Q3 | Low |
| Q5 | Quest drafts | server `OnlineQuests.java` (greet/topic/turn-in per 3.3/3.5) | none | `OnlineQuestsTest`: accept twice; 11/12 progress reply; 12/12 grants 500 gold, `award(400)`, `spell.renew_armor`, status 2 exactly once; completed is a no-op; fixture quest with normalized item consumes N units and refuses equipped-only; too-heavy reward refuses with no change | Q1, Q3, S1 | Low |
| Q6 | Kill credit in melee | none | `OnlineMelee.java`, `OnlineMeleeTest.java` (serialize with loot L8, same Claude lane) | Lethal in circle with active quest increments kills and kill log; outside circle or inactive quest increments kill log only; non-lethal and capped cases unchanged. Use a loot-free fixture until L8 | Q4, Q5 | **Medium**: lethal-claimant rule pending |
| Q7 | Aelric in NPC catalog and dispatch | none | `tools/OnlineNpcContentExporter.java`, `content/npc-interactions-v1.json`, `online/model/NpcInteractionContent.java`, `online/model/NpcInteractionReply.java` (args, key prefixes), `NpcInteractionCatalog.java` (allowlist), `RegionNpcs.java`, `OnlineNpcInteractions.java` | `RegionNpcsTest` (Aelric visible in temple region only); `OnlineNpcInteractionsTest` (dispatch, Kilhiam unchanged); exporter freshness | Q5 | Low |
| A3 | Self-cast drafts | server `OnlineCasting.java` | none | `OnlineCastingTest`: unknown spell, cooldown/mental-exhaustion boundary, not enough mana, mana and HP deltas, full-HP cast allowed, persisted expiries, `renew_armor` refused | A1, A2, S1 | **Medium**: Paladin + Light Heal pending |
| A4 | Potion drafts | server `OnlineItemUse.java` | none | `OnlineItemUseTest`: foreign/equipped instance refused, full HP refused, mental exhaustion blocks, charge 1 removes instance, `nbCharges > 1` refused | A1, A2, S1 | Low; per-instance charges need owner view if ever enabled |
| A5 | Regeneration settle | server `OnlineRegeneration.java` | none | `OnlineRegenerationTest`: whole ticks only, cap at max, dead re-anchors, huge gap bounded | none | **Medium**: logged-out regen decision |
| A6 | Online class gate | none | `OnlineCharacterCreator.java`, `OnlineCharacterCreatorTest.java` (unreserved; its caller `AccountRosterService` is reserved and is not touched) | Unsupported class refused, Warrior/Paladin unchanged | approval | **High** until classes approved |

### Wait for a reserved-file handoff

| # | Work | Reserved files | Tests | Depends |
| --- | --- | --- | --- | --- |
| Q8 | `interact` passes draft gold/XP (today `0,0`), optional topic, single flight | `PersistentSessions.java`, `PersistentSessionsTest` | Turn-in commits gold/XP/flags/spell once; repeat and concurrent requests; ambiguous outcome disconnects without replay | Q5, Q7 |
| Q9 | `/interact` topic parsing (body cap unchanged at 128), reply args; protocol bump shared with loot L9 | `PreviewServer.java`, `PreviewServerTest`; `OnlineProtocol.java` (shared model; coordinate) | Extend `OnlineNpcHttpTest`: accept, progress, turn-in, repeat turn-in, reconnect, foreign region | Q8 |
| Q10 | Host the kill circle: larger world-0 region, portals 12-14 refused, `SUNDIAL` resolved, BFS connectivity | `HostedWorlds.java` (plus unreserved `HostedRegionPlan`) | `HostedTransferTest`-style region test | owner approval of region |
| A7 | `/cast`, `/use` routes and session wrappers; regen settle at join/checkpoint and before retaliation; movement refused during physical exhaustion | `PreviewServer.java`, `PersistentSessions.java`, `PersistentCreatures.java` (retaliation caller) | HTTP: cast/use happy path, cooldown and mana refusals, reconnect keeps expiries; PostgreSQL restart keeps mana/cooldowns | A3, A4, A5 |
| Q11/A8 | Native dialogue topics, quest progress text, cast/potion controls, HUD from committed state | Native files under `src/main/java/com/perso/T4C/online/` (check register: T4C-0196 adapter ownership) | Adapter tests; rendered visuals need the owner's preview approval (AGENT.md) | Q9, A7 |
| Q12/A9 | `NativeQuestProbe`, `NativeAbilityProbe` (new tools) | none, but needs the integrated server | Two actual adapters: accept, kill, turn-in, repeat turn-in, lost response, restart; cast/potion costs persist. Not rendered evidence | all above |

Suggested start order: Q1, Q2 and A2 (serialize the domain pom), then A1, S1, A5, then Q3, Q4, Q5,
A3, A4, then Q7 and Q6 (after loot L8 on `OnlineMelee`). Q8-Q12 and A7-A9 begin only after Codex
records an explicit release of the reserved files in PEER_WORK.md.
