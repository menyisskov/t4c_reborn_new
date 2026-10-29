# Online NPC engine design (parity step 6)

Status: **design draft, not implemented** (T4C-0284, 2026-10-29). Top-tier architecture lane
(AGENTS.md section 6). Answers open question 3 of [CLIENT_UNIFICATION_DESIGN.md](CLIENT_UNIFICATION_DESIGN.md)
("run NPC classes on the server or extract dialogue to data?") and the "NPCs and economy" line of
[PARITY.md](PARITY.md): every NPC talks, sells, teaches, stores and teleports online exactly as in
single-player. Builds on the merged Kilhiam/Warden Aelric slice ([NPC_INTERACTION_DESIGN.md](NPC_INTERACTION_DESIGN.md)),
the quest service ([QUEST_ABILITY_DESIGN.md](QUEST_ABILITY_DESIGN.md) section 3) and the shop flow of
PR #238 (`ShopRules`, `/shop-offers`, `/shop-buy`, `/shop-sell`, still open when this was written).
Follows the shape of [SPELL_ENGINE_DESIGN.md](SPELL_ENGINE_DESIGN.md): one shared rule set, single-player
delegates to it, the server runs the same code. Facts are read from `origin/main` 117456d4 by parsing every
class under `src/main/java/com/perso/T4C/npc/` and the exported `npc-appearances-v1.json`; re-measure
before relying on a number.

## 1. Inventory: what the NPCs actually do

### 1.1 Where NPC behaviour lives

- **Identity and placement (data).** 510 classes extend an NPC base; 504 are concrete, spawned
  identities (the rest are the abstract story bases). 488 carry `@Spawn` annotations; the 12 Skraug
  classes are placed by monster spawn groups and claimed through `NpcSpawnOwnership`. The appearance
  exporter already lists every placement: **2,574 spawns of 504 identities** (world 0: 1,010, world 1:
  148, world 2: 1,402, world 4: 14). 228 are stationary; 2,346 wander inside a patrol radius.
- **`NpcSpec` (data, already declarative).** Every class builds one: display name key, sprite parts,
  welcome key, `DialogueTopic(keywords, response, actions)`, `CombatProfile`. Topic actions are an
  8-value enum (`ActionType`); only `GIVE_QUEST` (21 uses), `OPEN_SPELL_LEARNING` (10) and `OPEN_SHOP`
  (1) are used.
- **Catalogues (data).** `ShopCatalog` (24 vendors, of which 18 are plain `ShopBehavior` stock and
  already exported as `npc-shops-v2.json`; 6 return `null` because their own script gates the stock)
  and `TrainingCatalog` (46 teachers of spells and skills with level and gold).
- **Behaviour (imperative Java).** `ScriptedNpc.javaBehavior()` returns an `NpcBehavior` with eleven
  hooks. Overrides in the 504 classes: `onConversationStart` 193, `onKeyword` 172, `onYesNo` 112,
  `onInitialise` 41, `onAttacked` 33, `onDeath` 23, `onAttack` 12, `onPopup` 6. The behaviour reaches
  the world almost only through **`NpcBehaviorContext`, a 35-method facade** (flags, karma, global
  flags, say/shout keys, items, gold, XP, heal, teleport, respawn point, yes/no, summon, NPC state,
  spells, open shop/learn screens, end conversation).
- **Escapes from the facade.** 127 classes make 379 direct calls: 266 are gold, level and HP reads or
  gold/heal writes on `c.player()` (`getGold` 71, `addGold` 68, `getLevel` 52, `getMaxHp` 40,
  `getCurrentHp` 24, `applyHeal` 11); 69 are NPC state (`provoke` 35, `setStationary` 34). Real
  outliers: two remort NPCs (`RemortNPC2`, `RemortNPC3`) writing the five attributes, max HP/mana and
  element stats, 11 classes calling `QuestService` (8 directly, 3 via `npc().questService()`), 2 classes
  opening a GUI screen themselves (`SpellMerchant`, `StorageChest`: `GuiManager.open(...)`), 13 more
  that only use the `LearnScreen.TrainingOffer` record as data, and 6 calling other services
  (`MirrorTrials`, `DivinePantheon`, `CollisionManager`, `CompanionManager`, `EchoOfSelf`,
  `InventoryService`).
- **Text (data).** `lang.json` holds 19,720 `npc.*` keys (7,171 topic keywords, 3,562 topic replies,
  5,861 original-script lines, 476 welcomes). 1,410 speech calls pass a `${key}` or `sayKey`; only 12
  build a sentence by concatenation and about 7 contain a literal phrase. Single-player resolves the
  key, formats arguments and substitutes the player's name at display time.

### 1.2 Classification of the 504 identities and 2,574 spawns

"Tier" says how the behaviour is expressed today: **data** = `NpcSpec` plus catalogue entries only
(no behaviour code, or `StaticDialogueBehavior`/`StationaryBehavior`); **param** = a shared, reusable
behaviour class configured by constructor arguments (`PortalBehavior`, `BankBehavior`, `TrainingBehavior`,
`TrainingAndFleeBehavior`, `EmptyChestBehavior`, `NexusIndexBehavior`, `DeadBrotherBehavior`,
`GateBehavior`, `DaranBehavior`, random shard chests, the Skraug shouts); **script** = a hand-written
`NpcBehavior` (280 anonymous classes, plus the story and Oracle guardian bases). Roles come from a
static scan of each class (catalogue membership, facade and service calls), so a few borderline
classes may sit one row off (for example `RemortNPC3` scans as dialogue); totals are exact.

| Kind (primary role) | Classes | Spawns | data | param | script | Examples |
|---|---:|---:|---:|---:|---:|---|
| Pure dialogue / greeting | 247 | 348 | 54 | 19 | 174 | BrotherNiuss, Sundial (68 spawns), townsfolk |
| Teleporter, portal, door | 96 | 104 | 0 | 68 | 28 | 66 `PortalBehavior`, Chaos/Doppelganger portals, ColosseumClerk |
| Shop (sells/buys) | 24 | 24 | 5 | 0 | 19 | Fali, Karahn, Sigfried; Boreas's stock depends on `__QUEST_ISLAND_ACCESS` |
| Quest giver / turn-in / story | 24 | 24 | 11 | 0 | 13 | WardenAelric, HarbormasterRangor, Concord and Campaign NPCs |
| Spell teacher (spell merchant) | 23 | 23 | 3 | 0 | 20 | Kederic, Zhakar, SpellMerchant |
| Skill trainer | 22 | 23 | 9 | 2 | 11 | Asarr, ArganorIargh |
| Scripted event / object (no conversation) | 36 | 47 | 0 | 23 | 13 | Nexus stones, Oracle guardians, random chests, spawners |
| Storage / bank | 5 | 6 | 0 | 4 | 1 | 4 `BankBehavior`, StorageChest (2 spawns) |
| Rebirth (remort) | 2 | 2 | 0 | 0 | 2 | RemortNPC2, TrialWardenOsric |
| Hybrid (two or more roles) | 13 | 13 | 4 | 0 | 9 | Araknor (shop+spells), OldCorrin (quest+rebirth), MirrorwardenYsmera (rebirth+companion) |
| Ambient combatant (Skraug) | 12 | 1,960 | 0 | 12 | 0 | invulnerable Skraug townsfolk; behaviour is three random shout keys |
| **Total** | **504** | **2,574** | **86** | **128** | **290** | |

Counting each role wherever it appears (hybrids included): **27 NPCs open a shop** (18 plain catalogue
vendors, 6 script-gated catalogue vendors, 3 script-only: Araknor, Dafyd, ArchmageThalindra; the "21
shops" in PARITY.md was an estimate), 29 teach spells, 23 train skills, 30 give or take quests, 8 rebirth,
5 store, 100 teleport, **1 companion** (MirrorwardenYsmera). **Wardrobe is not an NPC role**: the robe
wardrobe opens from the inventory (`RobeWardrobeScreen`), and taming is the `tame_beast` spell; both
belong to client-unification stage 3 and spell stage 4, not this engine.

**Data versus code.** By spawn count the world is overwhelmingly data: 2,183 of 2,574 spawns (85%)
are data or parameterised (the Skraug alone are 1,960). By identity it is the opposite: 290 of 504
identities (58%) carry a hand-written script, 46,463 of the 64,331 NPC source lines. The scripts are
mechanical translations of the original T4C scripts with a small vocabulary: of the 280 anonymous
behaviours, 81 use nothing but the facade, 63 add only combat hooks, random rolls or world effects,
and 136 also use loops (62), random numbers (80), global flags (51), the wall clock (30, e.g. day/night
answers) or one of the escapes above.

## 2. Options and decision

| | (A) Declarative model for everything | (B) Run the NPC classes on the server against a headless `Player` | (C) Hybrid by tier: data where it is data, the same Java where it is code |
|---|---|---|---|
| Parity fidelity | Re-authoring 290 scripts (46k lines with loops, random, clock, global flags) into a DSL is a translation; every one can drift. The DSL would have to become a language | Same code, but `ScriptedNpc` extends `BaseNPC` (1,245 lines, LibGDX animations and speech rendering) and scripts hold `Player` (1,308 lines); headless shims would differ in the corners | Same behaviour objects, same hook order, same facade semantics; only the facade implementation differs, and it is tested for equivalence |
| Effort | XL+ (DSL, interpreter, 290 rewrites, review of each) | XL (headless `Player` and `BaseNPC`, LibGDX on the server, side effects to silence) | L to XL in batches: a port interface, a mechanical move of 290 behaviours to the domain module, server port implementation |
| Cheating resistance | Server-side | Server-side, but a large mutable `Player` mirror is a new surface | Server-side; the port exposes only typed effects on a `CharacterProgress` draft |
| Receipts, idempotency, lost responses | Fits `commitGameplayEvent` | A mutated `Player` must be diffed back into a draft: error-prone | Effects are collected into one draft and committed once, exactly like the Kilhiam letter, the quest turn-in and the shop trade |
| Fit with merged pieces | Replaces `OnlineNpcInteractions` | Bypasses the `CharacterProgress` model | `OnlineNpcInteractions` becomes the engine's server host; `OnlineQuests`, `ShopRules` and the shop routes are reused |

**Decision: (C).** Five lines:

1. Data that is already data is exported and served by a generic engine: `NpcSpec` dialogue, the shop and
   training catalogues, portal/bank/chest parameters and spawns (214 identities, 2,183 spawns).
2. Code that is code is not rewritten: the 290 `NpcBehavior` scripts move, unchanged in logic, out of
   their LibGDX-bound class shells into the domain module, where client and server compile the same bytes.
3. Scripts talk to the world through one port, `NpcConversation`, extracted from `NpcBehaviorContext`;
   single-player keeps today's implementation, the server implements it as a recorder of typed effects.
4. The server commits every interaction's effects once through the existing receipt-bound paths
   (`commitGameplayEvent`, `commitShopTrade`, `commitTransfer`), so retries, receipts and lost responses
   behave like every merged command.
5. Parity is proven per NPC by golden transcripts recorded from the unmodified single-player classes before
   anything moves, replayed against both port implementations.

## 3. Target architecture

### 3.1 One engine, three tiers, in the domain module

New package `com.perso.T4C.npc.engine` (domain build, no LibGDX, no `Player`):

- `NpcDefinition` (record): id, display name key, welcome key, topics (keyword keys, response key,
  actions), combat profile, flags such as talk-through-walls, and an optional `scriptId`. Built from
  `NpcSpec` (`NpcSpec` stays the single-player authoring form; a pure converter does the mapping).
- `NpcSpeechRules`: `normalize`, `matches(topic, text)`, `isConversationEnd`, keyword-quote stripping;
  moved from `BaseNPC`/`ScriptedNpc` verbatim, both delegate.
- `NpcBehavior` (moved interface, unchanged hooks) and `NpcBehaviorFactory` (`scriptId -> NpcBehavior`):
  the 290 scripts as `npc/script/<Id>Script.java` classes, plus the shared parameterised behaviours.
  `TrainingOffer` moves out of `LearnScreen` into the domain (`LearnScreen` keeps a type alias).
- `NpcConversation` (the port; section 3.2) and `NpcEngine`, which reproduces `ScriptedNpc`'s
  dispatch exactly: auto quest turn-in, behaviour `onConversationStart`, welcome fallback when nothing was
  said, pending yes/no, `onKeyword`, static topic match and its actions, the generic end words.
  `ScriptedNpc.onInteractStart`/`talk` delegate to `NpcEngine` with a `LocalNpcConversation`.

### 3.2 The port

`NpcConversation` is `NpcBehaviorContext`'s 35 methods turned into an interface, plus nine methods
that replace the escapes so no script holds a `Player` or `ScriptedNpc`:

| Group | Methods | Server implementation |
|---|---|---|
| Character reads | `flag`, `hasFlag`, `hasFlagAtLeast`, `viewFlag`, `karma`, `hasItem`, `itemCount`, `isWounded`, new `gold`, `level`, `hp`, `maxHp`, `mana`, `maxMana`, `skill(name)`, `attribute(name)` | read the draft `CharacterProgress` (inventory counts use the normalised `item.<key>` form) |
| Character effects | `flag(name, v)`, `viewFlag(name, v)`, `karma(v)`, `giveItem`, `takeItem`, `giveGold`, new `addGold(signed)`, `giveXp`, `healToHalf`, new `heal(amount)`, `setRespawnPoint`, `teleport` | edit the draft; XP through `OnlineProgression.award`; weight through `CarryCapacityRules`; teleport records a destination (3.4) |
| Speech | `say`, `sayKey(key, args)`, `shout`, `shoutKey`, `systemMessage(Key)` | append an `NpcLine(kind, key, args)`; never resolved on the server |
| Conversation | `askYesNo`, `pendingYesNo`, `endConversation`, `openShop(items)`, `openSellShop`, `openSpellLearning(keys)`, `openSkillLearning(offers)`, new `openStorage(kind)` | stored in the per-player conversation (3.5); replies carry the offer |
| Shared world | `globalFlag(...)`, `npcFlag`, `npcCurrentHp/MaxHp`, `damageNpc`, `fleeFromPlayer`, `selfDestructNpc`, new `provoke`, `setStationary`, `summon`, `castSelfSpell`, `castTargetSpell` | queued as world effects applied after the commit (3.6) |
| Environment | new `random()` and `clock()` | seeded per operation; server wall clock in the server's configured zone |

The 379 escape calls and the 80 `Math.random`/30 `LocalTime.now` uses are rewritten mechanically onto
these methods during the move (one rule per pattern, reviewed as a diff). The handful of real outliers
(the remort attribute writes, `QuestService`, `MirrorTrials`, `DivinePantheon`) get one named port
method each (`rebirth(...)`, `quests()`, `trial(...)`, `pantheon()`), backed locally by today's service and on
the server by its domain rule (`QuestRules`, `OnlineQuests`) or by a fail-closed refusal until ported.
`LocalNpcConversation` is today's `NpcBehaviorContext` body; `NpcBehaviorContext` becomes that class.

### 3.3 The interaction model on the wire

One route stays the entry point: `POST /interact`, body `REVISION EPOCH NPC_ID [INPUT]` with
`INPUT` = nothing (greet), `t:<text>` (typed or clicked keyword, UTF-8, 64-byte cap, the same text
single-player passes to `talk`), `y:1`/`y:0` (yes/no) or the legacy topic index (kept for the text
panel). The server runs `NpcEngine` against a `ServerNpcConversation` over the committed progress and
replies `{revision, changed, lines:[{kind, key, args}], offer?, ended}`. `kind` is say/shout/system;
`offer` is a shop, sell, learn, skill or storage descriptor (3.5). Protocol bumps to 7 with the reply
shape; `messageKey` is kept as the first line for one version so older probes keep working.

Conditions are not a new language: they are the scripts' own Java, reading the draft through the port.
Effects are the typed calls recorded by the port. Data-tier NPCs run through the same `NpcEngine`
(topic match, response key, actions), so there is one dispatcher, not two.

### 3.4 Effects become one atomic server commit

`OnlineNpcInteractions.prepare` (the existing server host, generalised) collects the run into a
`Draft(progress, gold, xp, lines, offer, worldEffects, destination)`:

- **No character change** (pure dialogue, a refusal line, an offer): no commit, `changed=false`, the
  revision may stay, exactly as Kilhiam's repeat greeting.
- **Character change** (flags, karma, items, gold, XP, HP, respawn point): one
  `commitGameplayEvent(token, revision, operationId, gold, xp, draft.progress())`. Revision CAS and the
  receipt make it exactly-once; prepare once and reuse the same draft and operation id on an uncertain
  retry (the current rule). Lines and offer are published only after the commit.
- **Teleport**: the destination joins the same transaction through a `commitTransfer` variant that also
  carries the draft (one receipt for "pay 50 gold, set flag, move"). A destination outside the hosted
  world refuses the whole interaction before commit with `online.npc.unavailable` and the line
  single-player would have shown is not shown; nothing is half-applied.
- **Unsupported effect** (a port method the server has not implemented yet, an unknown item, a spell
  the server cannot cast): the run throws a typed `UnsupportedNpcEffect`; the interaction is refused with
  no state change. This is the fail-closed rule every merged slice already follows.
- **Lost response**: the client does not replay. On reconnect the committed progress shows the effect;
  talking again re-runs the script against committed state, which in single-player is also what a second
  conversation does (e.g. "I already gave you the letter").

Randomness: the draft is prepared once with a per-operation `RandomGenerator`; a retry reuses the
draft, so a random reward is never re-rolled into a different one.

### 3.5 Conversation state and offers

The server keeps one ephemeral `Conversation(npcId, pendingYesNo, offer, openedAt)` per session,
replacing `isInteracting`/`pendingYesNoState` on the single-player NPC instance (which is per NPC there,
since there is one player). It ends on `ended`, on moving out of range, on region change, on logout or
after 5 minutes idle. It is never persisted.

Offers bind the economy routes to the conversation, which is what the 6 script-gated shops and the 3
script-only shops need: `/shop-offers`, `/shop-buy` and `/shop-sell` (PR #238) accept a purchase only for
an item in the conversation's current offer, priced by `ShopRules` from the item registry; with no
conversation they fall back to the static `npc-shops-v2.json` stock, as today. `learn` and `storage`
routes follow the same rule (`TrainingOffer` level and gold checks move to a shared `TrainingRules`,
storage moves to a shared `StorageRules`; both delegated to by the single-player screens, the
`ShopRules` pattern).

### 3.6 Shared world state

Single-player keeps NPC-instance state (`npcFlag`, HP, provoke, stationary, self-destruct) on the NPC
object and global flags in the in-memory `NpcWorldFlags`; neither is saved. Online, the hosted NPC
entity in `HostedWorlds` owns the instance state and a per-server `WorldFlags` map owns global flags,
also in memory, shared by all players (the multiplayer meaning of "global"). World effects are applied
on the simulation thread **after** the character commit succeeds; a refused or failed commit discards
them. Summons and NPC spell casts go through the creature and spell engines and stay refused until
those support the summoned definition.

### 3.7 Client rendering and i18n

- **Target (client-unification stage 5).** `NpcView` is the real NPC class built from
  `NpcFactoryRegistry` with `remoteControlled` set (CLIENT_UNIFICATION_DESIGN.md 3.3), so sprite,
  name, speech bubble and clickable keywords are single-player's. A click or typed line becomes
  `session.commands().talk(npcId, text)`; each reply line is shown through the puppet's existing
  `showDialog`/`shout` and `SystemMessage` path; an offer opens the real `ShopScreen`, `LearnScreen` or
  `StorageScreen` bound to remote tickets. No second dialogue UI is built.
- **Interim (today's text panel).** `OnlineNpcTopics` stops probing contiguous `npc.topic.<id>.<n>` keys
  and lists the NPC's exported keywords; replies render the same lines. It is deleted in unification
  stage 8 as already planned.
- **i18n.** The server sends keys and arguments only. One shared `NpcLineFormat.render(resolver,
  playerName, line)` does what `sayKey`/`resolvePlayerName` do today, used by both the local port and
  the remote client, so a line reads identically. The 12 concatenated and ~7 literal lines become keys
  in the move (the only `lang.json` additions). Keyword matching runs on the server against the
  exported, resolved keyword strings (the game is English-only per AGENT.md, so there is one catalogue).

### 3.8 Single-player keeps working unchanged

- `ScriptedNpc` keeps its constructor, spec, combat profile and rendering; `javaBehavior()` returns
  `NpcBehaviorFactory.create(scriptId)` instead of an inline anonymous class. Same object, same hooks.
- `NpcBehaviorContext` becomes `LocalNpcConversation`, with the same method bodies.
- The moved scripts change only in the mechanical escape rewrites of 3.2; each batch PR proves its NPCs
  still produce the stage-0 golden transcripts byte for byte. No save format, flag name or text changes.
- `MainGameScreen` and the GUI screens are untouched until client-unification stage 5.

### 3.9 Hosting the 2,574 spawns

- **Placement and identity.** `npc-appearances-v1.json` already gives every spawn a stable `spawnKey`.
  It becomes the hosting source for all identities, and `interactionSupported` is replaced by "has an
  exported definition" (the `NpcInteractionCatalog.SUPPORTED` two-NPC allowlist goes away in stage 1).
  The 53 spawns at tile (0, 0) are not hosted (single-player already skips the Sundial one; the
  others need a decision, section 5).
- **Skraug.** The 1,960 Skraug placements are ambient, aggressive, invulnerable combatants whose only
  behaviour is shout keys. They are hosted by the monster/creature pipeline (PARITY step 5) with the
  shout lines as data, not by the dialogue engine.
- **Movement.** 386 of the 614 non-Skraug spawns wander. The patrol rule moves from `BaseNPC.updatePatrol`
  into a domain `NpcPatrolRules` used by both sides; the server advances NPC positions on the simulation
  tick, publishes them in `NpcView`, and range and line-of-sight checks use the current server position.
  Until that stage, online NPCs stand at their spawn tile (today's behaviour) and talk range is measured
  from there.
- **Range.** Unchanged rule: `NPC_INTERACTION_RANGE_TILES_SQUARED` (120) from the authoritative player
  position, raw line of sight unless the NPC talks through walls (`canTalkThroughWalls`), same region.
- **Appearance.** 1,975 spawns are flagged `required_frame_name_missing` (all 1,960 Skraug plus 15
  others): the exporter cannot find their frames. This is a rendering prerequisite tracked by the
  unification puppet work, not by the engine.
- **Scale with whole-world hosting.** Definitions and scripts load once per server; per-NPC runtime
  state is a position, HP and a small flag map. Only NPCs in an active region tick; a region with no
  player keeps NPCs at their last position. Interactions run on demand on the simulation thread
  (one script run is microseconds; no per-tick scripting except `onInitialise` once per NPC load).

## 4. Staged plan

Sizes: S under a day, M a few days, L about a week, XL more. Every stage: full root and multiplayer
suites green, single-player golden screenshots and golden transcripts unchanged, PostgreSQL contract
tests in CI and one live run on the disposable WSL PostgreSQL of `multiplayer/tools/acceptance-cluster.ps1`
before merge, self-reviewed docs-plus-code PR.

| Stage | Content | Exit criteria | Tests | Size |
|---|---|---|---|---|
| 0. Golden transcripts | Root test harness that builds every NPC class headless, drives greet, every keyword, every yes/no state and each flag value the script reads (scanned from string literals passed to `flag`/`hasFlag`), with a fixed clock and seeded random, and records lines, offers and the `Player` diff. Committed as `src/test/resources/npc/golden/*.txt` | Transcripts for all 504 identities committed from unmodified `main`; reruns are byte-identical | the harness itself; determinism check (two runs, same bytes) | M |
| **1. First slice: data-tier dialogue served generically** | `NpcSpeechRules`, `NpcDefinition`, `NpcEngine` (data path only) in the domain module; exporter `npc-dialogue-v1.json` for the 86 data-tier identities and the welcome/topics of all others; `/interact` with `t:` input and the lines reply (protocol 7); `SUPPORTED` replaced by the export; text panel lists exported keywords | Every data-tier NPC inside a hosted region greets and answers each keyword online with the same keys as its golden transcript; scripted NPCs still refuse as unavailable; Kilhiam and Warden Aelric unchanged | unit: engine replay of all 86 golden transcripts; exporter freshness; HTTP test (range, sight, region, unknown input, 64-byte cap); live: native NPC probe on the acceptance cluster | M |
| 2. Port and the parameterised tier | `NpcConversation`, `LocalNpcConversation`, `ServerNpcConversation`; move `PortalBehavior`, `BankBehavior`, chests, `NexusIndex`, `DeadBrother`, `Gate`, `Daran`, Training behaviours; effects to draft; teleport through the transfer commit; world effects after commit | 128 parameterised identities match golden; a portal NPC moves a player between hosted regions in one receipt; refusal for unhosted destinations writes nothing | unit: both ports replay goldens; PostgreSQL contract test for draft+transfer in one transaction and replay by receipt; live two-client run | L |
| 3. Shops: all 27 | Conversation offers; `/shop-*` validate against the offer; the 6 gated and 3 script-only shops' scripts moved | All 27 shop NPCs buy and sell online with the single-player stock for the player's flags and `ShopRules` prices | unit: offer binding (item outside offer refused); `OnlineShopStoreTest` extended; live buy/sell at a gated shop before and after the flag | M |
| 4. Script move, batch by batch | the remaining scripts moved with the escape rewrites, in batches of about 40 grouped by world; each batch flips its NPCs to supported | Each batch: its transcripts byte-identical on both ports; single-player suite and goldens green | unit replay; one live spot check per batch | XL (6-7 PRs of M) |
| 5. Trainers and spell teachers | `TrainingRules` shared with `LearnScreen`/`TrainScreen`; `learn` route bound to the offer; spell keys normalised through `SpellKeys` | All 29 spell teachers and 23 trainers teach online with single-player costs and level gates | unit: rule equivalence over every `TrainingCatalog` offer; PostgreSQL learn receipt test; live learn | M |
| 6. Storage and bank | `StorageRules` shared with `StorageScreen`; character storage (already in `CharacterProgress`); account vault per section 5 | 5 storage NPCs work online; deposit/withdraw exactly once | unit + PostgreSQL contract + live | M (L with the account vault) |
| 7. Quests | port `quests()` backed by `QuestRules`/`OnlineQuests`; auto turn-in on greet; `QuestCatalog` grows with PARITY step 7 | The 30 quest NPCs give, report and turn in every quest the server catalogues; uncatalogued quests refuse | golden + the existing quest PostgreSQL tests | L (paced by step 7) |
| 8. Rebirth, NPC patrol, the rest | `RebirthRules` shared; `NpcPatrolRules` and server NPC movement; summons and NPC casts once the creature and spell engines support them; MirrorwardenYsmera's companion with the companion work | Every identity either works or is listed in PARITY.md debt with the blocking dependency | unit + live long run | L |

Stage 1 is the smallest end-to-end proof: no port, no script moves, no new commit path (dialogue
changes no state), yet it replaces the two-NPC allowlist with exported content for 86 identities and
fixes the wire shape every later stage reuses.

## 5. Risks, open questions, dependencies

**Biggest risk: the mechanical move of 290 scripts silently changing single-player behaviour.** The
scripts are 46k lines, many with branches no player hits often. Mitigation: stage 0 transcripts are
recorded from untouched `main` before any move; every batch must reproduce them on the local port and
the server port; moves and escape rewrites are separate commits; no rule edits in a move PR. Coverage
gap: transcripts only explore flag values the scanner finds; scripts computing flag names at runtime
(the `ADDON_NEXUS_<i>` loops) get hand-listed inputs.

Other risks:

- **Global flags change meaning.** Single-player global flags are one player's world; online they are
  shared (one player robbing the bank affects everyone until restart). That is the natural multiplayer
  reading, but it is a gameplay difference and needs the owner's confirmation per flag (51 classes).
- **Wall-clock answers.** 30 scripts answer by `LocalTime.now()`. Online uses the server clock; players
  in other time zones see "night" answers at their noon.
- **Conversation exclusivity.** Single-player has one talker per NPC; online, several players may talk
  to one NPC at once. Per-session conversations make that safe; NPC-instance effects (provoke, flee,
  self-destruct) apply to everyone.
- **Speech bubble ownership.** A puppet showing player A's reply must not show it to player B: lines are
  private replies; only `shout` lines are public world events (needs the `events` feed).
- **Content drift.** A new NPC authored after stage 1 must appear in the export; the exporter freshness
  test and an "every `@Spawn` identity has a definition or a recorded refusal" test guard it.

Open questions for the owner:

1. Global NPC flags: shared per server (proposed) or per player?
2. The 53 spawns at tile (0, 0): placeholders to skip (proposed) or script-placed objects to host?
3. Account vault: single-player's `AccountStorage` is a local file shared by a player's characters. Online
   needs an account-level storage table (schema change) or the vault stays offline-only.
4. Time-of-day answers: server clock (proposed) or the client's reported local hour?
5. Is the 64-byte typed-input cap acceptable (single-player has no cap; all keywords are shorter)?

Dependencies:

- **Client unification stage 5** renders the real dialogue bubble and screens; until then the text
  panel shows the same lines. The engine does not wait for it.
- **Quests (PARITY step 7)** paces stage 7; quest-giver NPCs refuse uncatalogued quests until then.
- **Whole-world hosting (PARITY step 8)** decides how many NPCs are reachable live; the engine is
  region-agnostic and unit-tested over all identities, but live evidence is limited to hosted regions,
  and teleport destinations outside them refuse.
- **Monster parity (step 5)** hosts the 1,960 Skraug and NPC-summoned creatures; **spell engine stages
  3-5** cover NPC spell casts and teleport spells.
- **PR #238** (shop flow) must merge before stage 3; this design only extends its routes.
