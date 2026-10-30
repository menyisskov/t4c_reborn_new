# Client unification: the online client runs the single-player screens (T4C-0254)

Design draft, 2026-10-28. **Documentation only; no production code changed, no server, database
or port used.** Source baseline: `origin/main` `68861bef` (PR206, protocol 5; rechecked against `25a4b2ca`). Re-measure the
counts below before relying on them. Owner rule: [PARITY.md](PARITY.md) (online looks and plays
exactly like single-player; the only difference is a client and a server). Engineering rules:
[AGENT.md](../AGENT.md) (camera, rendering, English only). Paths starting `screens/`, `gui/`,
`ui/`, `player/`, ... are under `src/main/java/com/perso/T4C/`; `server/` and `domain/` are under
`multiplayer/`.

**Decision: Option A, a session seam.** `MainGameScreen` and every GUI class stay the only game
screen. They talk to a `GameSession` with two implementations: `LocalGameSession` (today's code,
moved, not rewritten) and `RemoteGameSession` (intents to the server, snapshots back). The view
model is the *real* `Player`, `BaseMonster`, `BaseNPC` and `GroundItem` objects, driven as
puppets from snapshots online. Prediction is limited to the player's own movement.

## 1. How the game screen reads and mutates state today

### 1.1 Who holds authoritative state (single-player)

Everything is in-process and mutable; there is no read-only view layer.

| State | Holder | Evidence |
|---|---|---|
| Character (stats, vitals, gold, XP, inventory `List<String>`, equipment, spells, skills, quest flags, buffs, cooldown timestamps, quickbar, macros) | `player/Player` (1,322 lines, extends `model/Stats`, 140 public methods, 7 callback interfaces) | `MainGameScreen` holds one `@Getter Player player`; 180 `player.` references in the screen |
| Monsters (AI, pursuit, HP, loot, respawn) | `monster/core/MonsterManager` (669) + `BaseMonster` (1,354) and ~480 subclasses | 62 `monsterManager` references in the screen; `updateVisible` runs AI every frame |
| NPCs (dialogue scripts, shops, trainers, portals, chests) | `npc/core/NPCManager` (730) + `BaseNPC` (1,245) and ~600 classes | 48 `npcManager` references; NPC classes open GUI themselves (`ScriptedNpc`, `SpellMerchant`, `StorageChest`) |
| Ground items | `objects/GroundItemManager` (field of the screen) | 10 references |
| Quests | `quest/QuestService` (constructed in loading step 2 with `savePlayerState` as a callback) | 6 references |
| Spell effects, DoTs, buffs ticking | `spell/SpellEffectManager` (screen field) | 19 references |
| Companions, tame/harvest channels, world events, day/night, teleports, music zones | screen fields | `CompanionManager`, `TameChannel`, `HarvestChannel`, `WorldEventRuntime`, `DayNightCycle` |
| Persistence | `helper/PlayerStateStore.save(Player)` (static, file-backed, active-file static) | 20 `savePlayerState` calls in the screen plus a 15 s autosave |

### 1.2 The frame loop

`MainGameScreen.render` (line 1510) is: input → autosave → teleport check → `updateEntities`
→ world events → camera/music/zone banner → day/night → render bounds → ground → entities
with decor occlusion → spells, names, floating damage → overlays → HUD (`GameChat`,
`PlayerHUD`, coords, `RadarHud`, zone banner, cursor, `GuiManager.render`). Only
`updateEntities` (line 4108: `player.update`, `npcManager.updateVisible`,
`monsterManager.updateVisible`, `performAttackTick`, `spellEffectManager.update`, tame and
harvest) and the input/teleport/autosave sections *simulate*. Everything after them only
*reads* `player`, the managers and the effect/render objects. This split is the seam.

### 1.3 How the GUI reaches game logic

There is no event bus or command layer. Each GUI screen receives the `Player` and mutates it:

| Pattern | Count | Examples |
|---|---|---|
| GUI screens whose constructor takes `Player` | 16 | `Inventory(player, hud)`, `SpellBook(player, hud)`, `Statistics`, `QuestScreen`, `MacrosScreen`, `ShopScreen`, `StorageScreen`, `LearnScreen`, `TrainScreen` |
| Direct static rule calls that mutate the player | ~45 sites | `Inventory`: 17 `InventoryService`/`ItemUseService` calls (`equip`, `unequip`, `remove`, `useOnSelf`) plus `player.dropInventoryItem`; `ShopScreen`: `ItemSaleService.sell`, `player.setGold`, `player.setInventory`; `StorageScreen`: 20 service calls |
| GUI classes that write the save file themselves | 10 GUI + 2 input classes, 21 calls | `PlayerStateStore.save` in `Inventory` (5), `MacrosScreen` (4), `ShopScreen` (2), `SpellBook` (2), `ClickToMoveHandler` (2), `ObjectClickHandler` (2), ... |
| Screen-owned callbacks wired into the model | 21 `set*Callback` calls | monster damage/death/loot, NPC damage, player death, level-up, teleport, messages |
| Read-only GUI | most of `PlayerHUD` (17 `player.` reads), `RadarHud` (0), `JournalWindow`, `GuiInventory` (1), `CharacterPanel`, `OptionsScreen`, `ControlsScreen` | |
| Global singletons used by the screen | `CollisionManager` (11), `SpriteLoader`, `GuiManager` (static), `SystemMessage` (shared sink), `SoundManager` | |

Combat and casting live inside the screen itself (`performAttackTick`, `tryCastAttackSpell`,
`castDefensiveSpell`, `applyBowImpact`, `applySpellImpact`, about 1,500 of the 4,964 lines).
Movement is 8-direction input: `GameInputHandler.handleInput` → `player.move(dx, dy, delta)`;
mouse drag is projected to the same 8 directions (`MouseMovementHandler.projectDirection`).

### 1.4 The current online client

`online/OnlineClient` (949 lines) is its own `ApplicationAdapter`. It shares `MapRenderer`,
`PlayerAnimations`, `MonsterAnimations` and `NPCAnimations` but none of the screen, HUD or GUI.
State arrives through `online/OnlineConnection` (498 lines, one network thread): each loop
sends `/input` (`epoch sequence dx dy`), polls `/state` (`Snapshot`: players, creatures, NPCs,
loot, region, character revision) and, when the revision changes, `/character`
(`OnlineCharacterView` with the full `CharacterProgress`). Gameplay commands (`/attack`, `/cast`,
`/use`, `/equip`, `/unequip`, `/pickup`, `/recover`, `/interact`) go one at a time, carry the
character revision as a fence, and are never replayed after a conflict. The server
(`server/PreviewWorld`, 34 Hz) owns movement with its own swept-substep rule, which is **not**
`PlayerMovement`. 13 tools under `multiplayer/tools` and 4 tests drive `OnlineConnection`
directly; its public API is a contract.

## 2. Options

| Criterion | A. Session seam, puppets | B. Headless local simulation as mirror | C. Fork `MainGameScreen` | D. Ship single-player logic to the server and stream frames/state wholesale |
|---|---|---|---|---|
| Fidelity to single-player | Same screen, HUD, GUI classes, renderer, input | Same, but the mirror's simulation diverges from the server's | Diverges from the day of the fork | Same look, but needs a pixel/state streaming client |
| Effort | L–XL total, staged; first slice M | XL (every subsystem needs reconciliation) | M to start, unbounded upkeep | XL |
| Risk to single-player | Mechanical extraction behind characterization tests | Same extraction plus a second running simulation | None at first; parity drift guaranteed | High |
| Latency feel | Own movement predicted with the shared rule; everything else at server pace plus interpolation | Best in theory, rollback artefacts in practice (a predicted kill that the server refuses) | Same as A | Worst |
| Reuse of shared rules (`domain/`) | Server keeps using `domain/`; client uses the same rule only for movement prediction | Client runs all rules, server runs them again; two truths | Same as A | n/a |
| Cheating resistance | Unchanged: client only sends intents; the server never accepts state | Unchanged if the server stays authoritative, but the client holds a full simulation to tamper with | Unchanged | Strong |
| Fit with protocol 5, receipts, fencing | Uses them as they are; refusals become messages | Needs reconciliation for every committed receipt | As A | Discards them |
| Parity rule "no second GUI" | Met | Met | **Violated** | Met |

**Choice: A.** It is the only option that meets the owner rule (one game screen, one GUI), keeps
the server the only source of truth and reuses the merged protocol, fencing and receipts without
change. B is rejected because client-side monster AI, loot and spell resolution would have to be
reconciled against server receipts subsystem by subsystem, which is more work than A and gives
visible rollbacks; its one useful idea, local prediction, is kept for movement only. C is
rejected because a 5,000-line fork drifts and is exactly the "second GUI" the rule forbids. D is
out of proportion for a 2D tile game.

## 3. Target architecture

### 3.1 Interfaces to extract

New package `com.perso.T4C.session` (client-only; nothing here goes into `domain/`).

```java
/** One per played character; chosen at screen construction, never switched while playing. */
public interface GameSession extends AutoCloseable {
  SessionKind kind();                          // LOCAL or REMOTE
  CharacterSource character();                 // where the Player comes from
  WorldDriver world();                         // what updateEntities does
  PlayerCommands commands();                   // every state-changing player intent
  CharacterPersistence persistence();          // save, or deliberately nothing
  SessionEvents events();                      // presentation events (damage, death, messages, VFX, sound)
  Capabilities capabilities();                 // which screens/actions are wired in this session
  ConnectionState connection();                // LOCAL is always CONNECTED
}

public interface CharacterSource {
  MapDefinition initialMap();                  // replaces loadInitialPlayerState() + MapDefinition.fromZ
  void populate(Player player) throws GameException;   // replaces initializePlayer()'s state load
}

public interface WorldDriver {
  void loadEntities(MapDefinition map, MonsterManager monsters, NPCManager npcs,
      GroundItemManager ground);               // replaces initializeWorldEntities()
  void step(float delta, Player player, FrameBounds visible); // replaces updateEntities' simulation
  Iterable<Player> otherPlayers();             // empty offline
  void onMapSwitch(int z);                     // replaces switchMapForZ's entity reload
}

public interface PlayerCommands {
  void move(int dx, int dy);                   // per frame, 8-direction (already the input model)
  CommandTicket attack(TargetRef target);      // melee swing or bow shot
  CommandTicket cast(String spellKey, TargetRef target);   // unit, self or position target
  CommandTicket useItem(int inventoryIndex, String itemKey);
  CommandTicket equip(BodyPart part, int inventoryIndex, String itemKey);
  CommandTicket unequip(BodyPart part);
  CommandTicket dropItem(int inventoryIndex, String itemKey, int quantity);
  CommandTicket pickUp(GroundRef item);
  CommandTicket recoverCorpse(UUID corpse);
  CommandTicket talk(String npcId, OptionalInt topic, String typedText);
  CommandTicket buy(String npcId, List<BasketLine> basket);
  CommandTicket sell(String npcId, List<BasketLine> basket);
  CommandTicket storage(StorageMove move);     // deposit/withdraw item or gold
  CommandTicket learn(String npcId, String spellOrSkillKey);
  CommandTicket allocate(StatOrSkill target, int points);
}

/** Resolves once; the GUI shows pending/accepted/refused. Local tickets complete immediately. */
public interface CommandTicket {
  CompletionStage<CommandOutcome> outcome();
}
public record CommandOutcome(boolean accepted, String messageKey, List<Integer> args) {}

public interface CharacterPersistence { void save(Player player); }   // LOCAL: PlayerStateStore
public interface SessionEvents { void drainInto(EventSink sink); }      // called once per frame
```

`TargetRef` is a sealed type (`Monster(long id)`, `Npc(String id)`, `OtherPlayer(long id)`,
`Self`, `Position(float x, float y)`); locally it wraps the object reference, remotely the
server id. `Capabilities` is a set of enum flags (`INVENTORY`, `SPELLBOOK`, `SHOP`, ...) so a
not-yet-wired screen says "not available online yet" instead of half-working.

### 3.2 What each interface replaces

| Interface | Replaces in `MainGameScreen` / GUI | Local implementation |
|---|---|---|
| `CharacterSource` | `initializeCharacterAndWorld`, `loadInitialPlayerState`, the state part of `initializePlayer` | the same code moved verbatim |
| `WorldDriver` | `initializeWorldEntities`, the simulation half of `updateEntities`, the entity reload in `switchMapForZ`, `worldEvents.tick` | moved verbatim (`npcManager.updateVisible`, `monsterManager.updateVisible`, `performAttackTick`, `spellEffectManager.update`, tame, harvest) |
| `PlayerCommands` | the ~45 GUI mutation sites, `MonsterInputHandler` attack start, `tryCastAttackSpell`/`castDefensiveSpell`/`tryCastPositionSpell` entry points, `GroundItemClickHandler`, NPC talk/shop/storage entry points | a thin class that calls the same static services in the same order, completing the ticket at once |
| `CharacterPersistence` | all 41 `PlayerStateStore.save` and `savePlayerState` calls (screen, 10 GUI classes, 2 input handlers), autosave | `PlayerStateStore.save` unchanged |
| `SessionEvents` | nothing locally: the 21 existing callbacks keep firing; online they are fed from server events | pass-through (empty) |

The GUI screens keep their constructors and gain one argument, the session (or they read it from
the `PlayerHUD` they already receive). A screen calls `session.commands().equip(...)` where it
called `InventoryService.equip(player, ...)` and redraws when the ticket resolves. For the local
session the ticket is already complete, so the frame looks exactly as today.

### 3.3 Snapshots to view-model objects (remote)

The GUI never sees protocol records. `RemoteGameSession` owns an `OnlineConnection` and a
`SnapshotApplier` that writes into the same objects the single-player code reads:

| Server data | Local object | Rule |
|---|---|---|
| `CharacterProgress` (on revision change) | the screen's `Player` | `ProgressMirror.toStateDto(progress)` builds a `PlayerStateDto`; the existing `PlayerStateMapper.applyToPlayer` fills the `Player`. Inventory index → item instance UUID is kept in an `ItemInstanceIndex` owned by the session, so GUI intents by index are sent by UUID. Revision is kept and must only increase |
| Own `Actor` (x, y, hp, sequence) | `Player` position and HP | reconciled by the movement predictor (3.4); HP from the actor between character revisions |
| Other `Actor`s | `RemotePlayer` puppets (a `Player` with `PlayerAnimations`, no simulation, no persistence) | added to the render list next to the own player; needs appearance parts in the snapshot (new) |
| `CreatureView` | a `BaseMonster` built by `MonsterRegistry` from `definition` and added to `MonsterManager` with a `remoteControlled` flag that skips AI, pursuit, loot and respawn | position, facing and health set from the snapshot through `OnlineCreatureMotion`-style interpolation; death plays the normal death animation |
| `NpcView` | the real NPC class from `NpcFactoryRegistry` by id, with scripts and dialogue execution disabled (`remoteControlled`) | rendered exactly as single-player; talking sends `talk` |
| `GroundLootView` | `GroundItem` in `GroundItemManager` keyed by loot UUID | owner window shown as a tooltip line |
| `Corpse` | a ground object rendered with the single-player corpse/bag sprite | `recoverCorpse` on click |
| `region` / `locationEpoch` | `switchMapForZ` path with the hosted crop's origin as an offset | the session acknowledges `/region-ready` only after the map, collision and puppets are rebuilt (existing fence) |

All protocol coordinates are region-local; the applier adds the region origin once, so the
screen keeps working in absolute world pixels as today.

### 3.4 Latency and prediction

- **Own movement:** predicted. The client keeps calling `player.move(dx, dy, delta)` at the
  native fps and sends each frame's direction with a sequence number. Each snapshot carries the
  last applied sequence (`Actor.sequence` already exists). The predictor resets to the server
  position and replays unacknowledged inputs; an error under one tile is smoothed over 100 ms,
  more than one tile snaps. **Prerequisite:** server and client must use the same step rule.
  Today they do not (`PreviewWorld.step` sweeps 0.2-tile substeps, `PlayerMovement.move` has its
  own slide and timing rules). The movement step moves into `domain/` as a pure
  `MovementRules.step(position, dx, dy, dt, collision)` used by both; until then (slice 1)
  movement is unpredicted, as today.
- **Other players, monsters, NPCs:** rendered 100 ms behind the newest snapshot and interpolated
  between two snapshots, facing from the delta.
- **Melee:** no outcome prediction. Clicking a monster starts the same auto-attack loop
  (`performAttackTick`), but each swing becomes an `attack` ticket paced by
  `WeaponTimingRules` (already in `domain/`); the swing animation starts at once, damage numbers
  and the hit sound play when the server reports the result. A refused early swing stays silent,
  as T4C-0249 already does.
- **Spells:** the cast cursor and casting animation start at once; mana, cooldown and effects
  come from the server. Cooldown display reads `CharacterProgress.cooldowns` (absolute UTC).
- **Inventory, shop, storage, training:** not optimistic. The affected slot or button shows a
  pending state until the ticket resolves (loopback is a few ms; remote is one round trip).
- **Transport:** HTTP polling (`/input` then `/state` per loop) is kept for slices 1–3. If
  remote play feels slow, a streamed snapshot channel replaces the poll inside
  `OnlineConnection` only; nothing above the session changes.

### 3.5 Events, refusals and messages

The server adds an `events` list to `/state` (bounded, per viewer, ordered by tick): damage
dealt or taken (amount, miss, element), death, level-up, XP gain, item gained, spell cast and
impact (for VFX), sound cue, chat. `SessionEvents` turns them into the calls the existing
callbacks already make (`floatingDamage.add`, `SoundManager`, `spellRenderer`,
`SystemMessage.showShared`), so presentation code is shared. A refused ticket shows its
`messageKey` through `SystemMessage` and the chat log, exactly where single-player shows its own
failure text (for example `ItemUseService.failureMessage`). `OnlineCommandRefusals` maps server
codes to the same `lang.json` keys single-player uses wherever one exists.

### 3.6 Reconnect and lease

- Connection loss: the screen keeps the last frame, blocks input, shows a "Reconnecting"
  banner and lets `OnlineConnection` reconnect with its existing busy backoff. Pending tickets
  fail with "connection lost" and are never replayed (current rule).
- On rejoin the session rebuilds puppets from the first snapshot, resets the movement predictor
  and re-acknowledges the region epoch.
- Lease lost to another login of the same character, or revoked key: back to
  `OnlineLobbyScreen` with the server's message. Escape and the options "switch character"
  return to the lobby online and to character selection offline.

### 3.7 Single-player stays unchanged (non-negotiable)

- `new MainGameScreen(game)` keeps its signature and builds `LocalGameSession`. The online
  launcher calls a new `MainGameScreen(game, session)`.
- `LocalGameSession` is the old code moved, in the same order, with no rule changes. Each move
  is a separate commit with characterization tests written first.
- Save files are untouched: `LocalPersistence` is `PlayerStateStore.save`. `RemotePersistence`
  writes nothing, and `PlayerStateStore.save` refuses a `Player` marked remote (defense in
  depth: a forgotten call cannot overwrite the active single-player save).
- Quickbar, macros and window layout are client preferences. Online they are stored in a
  separate local file per online character id, never in the single-player save.
- Offline start never loads network classes and opens no socket (asserted by a test).

### 3.8 Persistence audit: every write path to the save directory (T4C-0265)

Rule: a remote character (`Player.isRemote()`) must never cause a byte to be written to the
single-player save directory. Pinned by `SavePathAuditTest` (the list of direct
`PlayerStateStore.save` callers cannot change unreviewed) and `RemoteSessionWritesNothingTest`
(a remote character through the GUI mutate-and-save sequences leaves the directory byte-identical).

| Write path | Reachable by a remote session? | Protection |
|---|---|---|
| `PlayerStateStore.save(Player)` / `save(Player, float)` (GUI, input and lifecycle call sites, autosave, dispose) | Yes, by a forgotten call: every one takes the session's `Player` | Refused inside `save(Player, float)` when `player.isRemote()`; nothing is written, not even a temp file. Routed through the session (see below): Inventory 5, MacrosScreen 4, SpellBook 2, ClickToMoveHandler 2, ObjectClickHandler 2, Statistics, RobeWardrobeScreen, AdventureScreen (each 1). Still direct (still guarded, no HUD or session reachable): ShopScreen 2, LearnScreen, TrainScreen, StorageScreen (each 1), `MyGame.dispose` |
| `CharacterPersistence.save(Player)` and `save(Player, float)` (`LocalCharacterPersistence`) | Yes (MainGameScreen autosave and the routed GUI saves) | Delegate to the guarded `PlayerStateStore.save`; a remote session installs a persistence that writes nothing. GUI windows reach it through `PlayerHUD.persistenceOf(hud)` (the HUD is given `session.persistence()` by MainGameScreen; no HUD means the local persistence, as before); `ObjectClickHandler` takes it as a constructor argument |
| `PlayerStateStore.save(String, PlayerStateDto)` / `saveOrThrow` | No, by construction. The only DTO sources are `LocalCharacterDirectory.setStoredRank` (a DTO read back from a local slot file, only the GM rank changed) and `LocalCharacterStore.create` (a brand-new DTO from the creation screen). `PlayerStateMapper.fromPlayer`, the only `Player` to DTO conversion, is called solely inside the guarded `save(Player, float)` | No guard needed and none added: a remote `Player` cannot become a DTO for a local slot. `SavePathAuditTest` fails if a new caller of either overload appears |
| `LocalCharacterStore.create / delete / saveRoster` (roster file, per-slot state files) | No. Only `CharacterSelectionScreen` (offline start flow) calls them; the online flow uses `OnlineLobbyScreen` | Not reachable: a remote session never opens the offline selection screen. Add `RemoteCharacterSource` (stage 2) with no roster writes |
| `LocalCharacterStore.activate` (sets the static active state file) | Only from `CharacterSelectionScreen` and `CharacterLoadingScreen` | Does not write; and a stale active file cannot be written by a remote `Player` anyway (guard) |
| `LocalCharacterDirectory.setStoredRank` (GM `/rank` of a named local slot) | Yes if a remote `MainGameScreen` kept the local directory: it would edit the rank of a local slot with the same name | `GmCommandProcessor` takes a `CharacterDirectory` interface; the remote session must supply its own (server-side or none). Listed for stage 2/3; not a write of the remote character |
| `AccountStorage.save` (`account_storage.json`, the shared account vault, via `AccountStorageService` deposit/withdraw/gold) | Yes: the StorageScreen account tab takes any `Player` and moves its items and gold into the local vault | `AccountStorageService` deposit, withdraw and gold methods now refuse a remote player (this was the one unprotected path); online storage is a server concern |
| `GamePreferencesStore.save` (client preferences file) | Yes, and intended: audio and display options are per client, not per character | Not a character write; unchanged |
| `GameChat.appendLog` (chat log file) | Yes, and intended: optional client-side log | Not a character write; unchanged |
| `I18n` catalogue writer, `BuildPreferences`, `MapStamp`, `MapDraftService`, `MapEditorScreen`, `ItemJavaExporter`, `SpawnJavaExporter`, `SpellJavaExporter`, `BinaryIOUtils`, `SpriteBin*`, `CollisionMapIO`, `DdaExtractor`, `T4CContentStudio`, `OnlineNativeLibrary` | No: editor, content-tool and installer writers; none touch a character or take a `Player` | Out of scope; they write content or cache files, not saves |
| Character switch, dispose, autosave | Autosave: `MainGameScreen.savePlayerState` (already via the session). Dispose: `MyGame.dispose` saves the current screen's player | Both end in the guarded `save(Player, float)` |

### 3.9 PlayerCommands, slice 1: attack the clicked monster (T4C-0267)

`PlayerCommands.attack(BaseMonster)` is the first member. It was chosen because it is the one
combat intent with a single entry point (`MonsterInputHandler` calls one callback after it has
resolved the click to a live, attackable monster, and after an armed targeted spell has had its
chance) and a closed rule set (record the target, then one melee or bow attempt). The other
combat paths stay on the screen for now: the per-frame auto-attack (`performAttackTick`, which is
simulation, not intent), target selection by Tab, the hotbar and spell casts.

- **Local:** `LocalPlayerCommands.attack` is the old `beginAttackTarget` body moved unchanged.
  The attack-target fields, the bow rules and the projectile renderer still live in
  `MainGameScreen`, reached through the small `LocalAttackHost` it implements and binds at build
  time. Range, line of sight, facing, cooldown and damage stay in `MonsterManager.attackMonster`
  and the bow code. `AttackIntentCharacterizationTest` pins the behaviour: target recorded before
  the swing, no player or no monster does nothing, a miss keeps the target without a cooldown, a
  weapon on cooldown or a stunned player only becomes the attacker, bow refusals (range, no
  quiver) drop the target and combat mode, and a launched shot starts the cooldown.
- **Remote:** send the intent (target by server id) and apply nothing locally: no target flag, no
  swing, no cooldown. Range, cooldown and damage are the server's; a refusal arrives as a session
  event (see 3.5). The `LocalAttackHost` is not part of the remote path.
- **Not yet moved:** the auto-attack tick keeps reading the same screen fields; when the remote
  session lands, those fields become the view of the server-held target (follow-up slice).

### 3.10 Movement: single-player versus server, and the shared step (T4C-0269)

Single-player and the online server move a player with two different rules. The pure step is
now in `world/MovementRules` (`Mode.GRID_RESERVATION` = single-player, called by
`PlayerMovement` and pinned bit for bit by `PlayerMovementGoldenTest`; `Mode.SWEPT_SUBSTEP` =
the server rule, copied from `PreviewWorld.step`, not yet called by the server). Prediction
(3.4) needs one rule on both sides, so the differences below are parity debt until the owner
picks the surviving rule; single-player keeps its own.

| Aspect | Single-player (`Player.move` -> `PlayerMovement.move`) | Server (`PreviewWorld.step`) |
|---|---|---|
| Input | 8 directions from keys (dy scaled by the 0.5 tile aspect, only the sign matters), mouse drag, click-to-move path | `/input epoch sequence dx dy`, dx and dy in -1..1, latest wins, expires after 12 ticks |
| Time base | render frame `delta` (variable) | fixed tick, 34 per second, server-owned; repeated packets add nothing |
| Speed | 300 px/s x `getEffectiveSpeedMultiplier()` (SPEED buffs, GM 0.1..10) | 300 px/s fixed, no buffs: 300 / 32 / 34 = 0.2757 tiles per tick (the maximum the tamper probe measures) |
| Units and origin | world pixels (tile 32 x 16), position is a tile top-left corner (anchor) | tile units (double) in the hosted crop, spawn on a tile centre (+0.5), so actors rest half a tile right and down of single-player anchors |
| Step model | reserves a whole tile, then glides to it; the reservation survives released input and cannot reverse mid-tile; a mid-tile load snaps to the next anchor in the input direction | no target: moves a fixed distance each tick in substeps of at most 0.2 tile, stops at once when input stops |
| Diagonal | both axes advance at the same tiles/s (x pixels at 300, y at 150) | both axes advance 0.2757 tiles/tick, same tiles/s |
| Collision query | `CollisionManager`: static map via shared `WorldCollision` (wall-foot rule) + dynamic blockers (monsters, NPCs) + teleport-passable tiles + sampled path check + map pixel bounds | precomputed grid from the shared `WorldCollision` per 80x60 crop; tile and tile above blocked; no dynamic blockers, no teleport tiles |
| Footprint | destination tile and the tile above, plus the diagonal corner rule against the origin tile | tile and tile above only; diagonal needs both axis-only targets standable |
| Blocked | slides: tries the four neighbouring directions in the order -1, +1, -2, +2 around the requested one (so it slides along walls) | stops; no sliding |
| Gates | stunned, exhausted, attacking stop movement; GM can switch collisions off | frozen (transfer) or dead stop movement |
| Facing | derived from the reserved direction (client animation) | none |

Equal today: collision codes (`CollisionRules` equals the desktop `CollisionType` for all 256
codes) and the distance covered per second in open ground (9.375 tiles/s). `MovementRulesTest`
and `MovementRulesRealMapTest` pin both facts and the server loop.

Decided by the owner parity rule: the server adopts the grid-reservation rule (3.10.1).

#### 3.10.1 Decision and server adoption (T4C-0277)

The owner rule decides the open question above: single-player is the reference, so the server
adopts `Mode.GRID_RESERVATION` (option 1). `PreviewWorld.step` now calls `ServerMovement.step`,
which runs `MovementRules.Grid.reserve` and `advance` with the server's fixed delta (budget 300 / 34
= 8.82 px, horizontal-equivalent, per tick). The previous loop stays in the file as
`Mode.SWEPT_SUBSTEP`, selected by `-Dt4c.server.movement=swept` or `PreviewWorld.movementMode(...)`;
it is the rollback and the reference the tests compare against.

**Convention (the half-tile offset).** The wire, snapshots, persistence and every consumer keep
tile-centre doubles in the hosted crop: a player resting on tile (3,5) is at (3.5, 5.5), exactly
what spawns, respawns, corpses and transfers already write. The shared rule works in world pixels
where a position is a tile's top-left anchor. `ServerMovement` converts with one fixed, exact
offset: `pixelX = (x - 0.5) * 32`, `pixelY = (y - 0.5) * 16` and back. Resting positions are
therefore identical to before and nothing about stored positions changes. Only a position taken
mid-glide (a checkpoint can now fall between tiles) is off the centre.

**Impact analysis: who consumes server positions.**

| Consumer | Effect of the grid rule | Action |
|---|---|---|
| Snapshots (`/state`) and the native client | Same wire (tile-centre doubles, protocol 6 unchanged). The client mirrors snapshots and has no prediction, so it simply sees tile-to-tile glides; a released key now finishes its tile (at most one tile, 3.6 ticks) | none |
| Persistence checkpoints and `PersistentSessions` | A checkpoint may store a mid-glide position. Load goes through `PreviewWorld.joinAt`, which snaps a position between tiles to the centre of the tile it stands on (legal under both rules); exact centres load untouched. Old saves (positions the swept loop left anywhere) load the same way | `joinAt` snap, test of old-format positions |
| Hosted transfers, respawn, corpse recovery, `relocate`, `transferTo` | Server-written positions; they clear any glide in progress, then the player rests where it was put (a mid-tile placement still works: the rule snaps toward the next anchor in the input direction, as offline does) | glide cleared |
| Creature pursuit, aggro, retaliation range, NPC talk range, loot pickup range | All are distance checks on fractional doubles; none assumed a stop-anywhere position. A player rests on a centre more often, which cannot move a range boundary by more than the half tile they already tolerated | none |
| Tamper probe (`SPEED-spam`, `SPEED-walls`) | The per-tick maximum stays 0.2757 tiles per axis (same tick distance; leftover budget after an arrival is spent on the next reservation, never added). Samples stay standable: while gliding the foot tile is the origin or the destination, both legal | assertion unchanged; probe not re-run |
| QA tools (`NativeAdapterSoak`, `NativeLootProbe`, `NativePursuitProbe`, `NativeTransferProbe`) | They steer by tile-centre BFS and assume an instant stop; they now overshoot a released tile by up to one tile | re-validate at the next live window (not run here) |
| Preview web client (`client.js`) | Draws snapshots only | none |

**What equals the old loop and what differs on purpose** (pinned by `ServerMovementParityTest`):

| Behaviour | Result |
|---|---|
| Open ground, held input, eight directions | positions equal the old loop every tick (within 2e-4 tile; float versus double) |
| Speed | at most 0.2757 tiles per axis per tick, any input sequence |
| Cardinal collision decisions (enter the next tile or not) | identical to the old precomputed grid on a random map built the way production builds it |
| Diagonal the old loop allowed | always allowed; the extra diagonals the grid allows are exactly the documented ones (a blocked side tile, or the wall-adjacent exception) |
| Blocked step | slides in the order -1, +1, -2, +2 around the requested direction instead of stopping |
| Released key | the reserved tile is finished, then the player rests on a centre (no instant stop mid-tile) |
| Reverse mid-tile | impossible until arrival |
| Saved mid-tile position | snaps to the centre of its tile on load; illegal tiles are still refused |
| Never | standing on a blocked tile, under random input over five random maps |

**Remaining, not done here.** Dynamic blockers (monsters, NPCs, other players), speed buffs and GM
speed (the server holds no player speed state), the stun/exhausted/attacking gates, teleport-passable
tiles beyond hosted portal sources, the wall-foot difference between `PreviewMap.load` (applies it)
and `RegionMapLoader` (does not), and client-side prediction on top of the now shared rule.

### 3.11 Remote session, step R1: the character (T4C-0287)

`RemoteGameSession` (kind `REMOTE`, headless, not wired to any screen) exists with
`RemoteCharacterSource`, a persistence that writes nothing, and placeholders for the world driver
and the commands that throw `UnsupportedOperationException("not available online yet: <member>")`.
`ProgressMirror.toStateDto(OnlineConnection.View)` is the design's `ProgressMirror`: it turns the
committed `CharacterProgress` plus the own actor and region into the `PlayerStateDto` the existing
`PlayerStateMapper.applyToPlayer` fills the `Player` from (the mapper was reused in the forward
direction; no mapper code changed). `applyTo` sets `Player.remote` before the mapper runs. The
conventions that differ between the two models (worn items leave the inventory, a two-slot item
fills both slots, experience split with the server's own curve, base health without buff health,
region-local tile centres to absolute tile anchors) are documented on `ProgressMirror`; the item
instance ids are kept beside the state for the later `ItemInstanceIndex`. A view that is not ready
fails with `IllegalStateException` rather than starting a new character. Parity debt list: PARITY.md.
Next, R2: the world driver applies snapshots to the monster, NPC and ground-item objects.

### 3.12 Remote session, step R2: the world as puppets (T4C-0289)

`RemoteGameSession.world()` is now `RemoteWorldDriver` (headless, not wired to any screen).
`loadEntities` builds a `MonsterManager` and an `NPCManager` in **puppet mode**: no spawn is queued
from the local map files, and they hold only what the snapshot says. `tickEntities` applies the
newest `OnlineConnection.View` (only when its tick or region changed, and never a snapshot of
another region epoch than the map's) and then eases and animates the puppets; no local AI starts.
Creatures are built with `MonsterRegistry.create` from the definition name, NPCs with
`NpcFactoryRegistry.create` from the public id (an id with no class, an appearance-only NPC, becomes
a `PuppetNpc` from the published sprite or parts), loot as `GroundItem` in the screen's own
`GroundItemManager` (`addRemoteItem` / `removeRemoteItem`), and every other actor as a remote
`Player`. Positions: creatures, NPCs and loot are authored tile anchors (`(origin + x) * tile`);
players are tile centres (half a tile less, like the own player in `ProgressMirror`).

**Puppet switch points** (every one defaults to off, so single-player is unchanged;
`PuppetSwitchCharacterizationTest` pins the default behaviour, `PuppetSwitchTest` the puppet one):

| Class | Switch | Inert in puppet mode |
|---|---|---|
| `BaseMonster` | `setPuppet` | `update` (AI, pathing, aggro, attacks, patrol), `takeDamage`, `heal`, `applyPlayerDamage` (experience), `applyClanDamage`, `aggroOn`, `aggroOnCompanion`, `die` (death callback), `respawn`, `shouldRespawn`, `shouldRemoveAfterDeath`; state arrives through `puppetPlace` and `puppetHealth` |
| `MonsterAnimations` | `setMuted` (set by `setPuppet`) | the attack and death sounds that a state change would play |
| `MonsterManager` | `setPuppetMode` | `addMonster` (script spawn event, hunting respawn, callbacks), `update`, `updateVisible`, `attackMonster`, `notifyKilledByPlayer` (loot and quest credit), script events, `spawnMonster` (summons) |
| `BaseNPC` | `setPuppet` | `update` (patrol, hostility), `onClick` (conversation), `onAttacked`/`onHit`/`onDeath` sounds, `provoke`, `fleeFrom`; state arrives through `puppetPlace` |
| `ScriptedNpc` | via `BaseNPC.isPuppet` | `resolvedBehavior()` returns nothing, so every script hook (initialise, popup, attack, death, destroy, conversation, shop, training) is skipped |
| `NPCManager` | `setPuppetMode` | `addNPC` flags puppets, `triggerPopupEvents`, `spawnNPC`, `update`, `updateVisible`, `onClick`, `attackNpc`, `damageNpc`, `onNpcAttacked`, `talkToActiveNpc` |

**Sweep test** (`PuppetSweepTest`, section 5 of this design): a death snapshot on every registered
monster class (484 definitions, Java classes plus the JSON monsters) and the combat, conversation
and removal paths on every registered NPC class (503 of 504 ids are publishable; one id breaks the
public id rule) fire no death, loot, kill, script, damage, quest, save or sound callback and give
the witness player nothing.

**What the snapshot lacks** (parity debt; each is a server or later-step item, none is invented here):

| Needed to look and play like single-player | Snapshot today | Effect now |
|---|---|---|
| Creature attack, cast, hit and miss events | none (`CreatureView` has health and position only) | creatures never swing or flinch; no damage numbers, no hit or miss feedback |
| Sounds and spell effects | none (stage 7 `events` feed) | puppets are silent by design |
| Creature stun, status, aggro | none | not shown |
| NPC stationary, idle, health, hostility | `NpcView` has id, name, position, sprite | stationary NPCs do not play their idle loop; no NPC combat |
| Corpses | `View.corpses` is not applied | no corpse sprite or recover target yet |
| Loot owner window, charges | `ownerRemainingMs` exists; charges absent | no owner tooltip; charges unknown |
| Other players: name, class, appearance parts, equipment, stealth, actions | `Actor` carries id, position, hp and, since T4C-0314, the public look: name, gender and the worn items that draw something (all derived server-side from committed state); class, stealth and actions are still not sent | dressed through `PlayerAppearanceDefaults` like the own character (default look for an older server); name and health-colour outline on hover; walk animation and `MainGameScreen` drawing from R3; exposed by `RemoteWorldDriver.otherPlayers()` |
| Interpolation buffer (render 100 ms behind) | n/a | puppets ease 60 ms toward the newest snapshot instead |
| Server id to object | held by the driver (`creatureId`, `npcId`) | step R3 sends intents by these ids |

Preview image (a real `MainGameScreen` drawing the puppets of a hand-made snapshot, not a golden):
`multiplayer/design/remote-world-puppets.png`, produced by `RemoteWorldPuppetsPreview`.
Next, R3: `PlayerCommands` over the connection (attack, move, pick up, interact) with tickets, using
the ids above.

### 3.13 Remote session, step R3: the commands (T4C-0292)

`RemoteGameSession.commands()` is `RemotePlayerCommands` (headless, not wired to the launcher).
The screen binds it once with a `Host` (the player, and where a message goes: the same
`SystemMessage` single-player uses); the click hooks added in `GroundItemManager` and `NPCManager`
are installed by the driver (`bindIntents`) and are inert unless it does.

| Intent | Entry point (unchanged) | Request | Locally |
|---|---|---|---|
| attack the clicked monster | `PlayerCommands.attack` from `MonsterInputHandler` | `/attack <rev> <epoch> <creature id>` (`RemoteWorldDriver.creatureId`) | nothing: no target flag, swing, cooldown or damage |
| walk | `PlayerCommands.move` every frame from `GameInputHandler` | `/input <epoch> <seq> <dx> <dy>`, the connection's own sequence, tagged with the client frame | `Player.move` at once (shared rule), then reconciled |
| pick up loot | `GroundItemManager.pickUpAt` with a hook: status `REQUESTED` | `/pickup <rev> <epoch> <loot uuid>` (`lootId`) | nothing: no inventory change, the item stays until a snapshot drops it |
| talk to an NPC | `NPCManager.onClick` in puppet mode with a hook | `/interact <rev> <epoch> <public id>` (`npcId`) | nothing: no conversation, quest or script starts |

Single-player is the same code: with no hook set, `pickUpAt` and `onClick` are byte-for-byte what
they were (`GroundItemPickupCharacterizationTest`, `PuppetSwitchCharacterizationTest`,
`MainGameScreenGoldenTest`). A reply (accepted or refused) is queued on the network thread and
shown on the next frame through the host: the refusal key from `OnlineCommandRefusals`, or the
reply's own message key with its arguments. The routine swing-cooldown refusal is silent. A second
click while one is in flight, or a link that is down, says "busy" (the keys the refusal table
already has) instead of dropping the click without a word.

**Prediction and reconciliation.** `OnlineConnection.direction(dx, dy, frame)` holds the direction
as before and remembers the client frame; each `/input` it sends stores which frame was newest, and
`acknowledgedFrame(sequence)` maps the snapshot's last applied sequence back to a frame. The
commands record every frame in a `MovementReconciler` whose rule is the shared grid rule
(`RemoteMovementRule`, stateless: a glide cut at a frame boundary re-reserves from mid-tile, the
same target in the held direction). On each new snapshot: drop acknowledged frames, replay the rest
on the server position, compare with the player. Tolerance: under 0.5 tile nothing happens (the
replay also counts the time the server already spent on the last input, so a steady error of about
speed times snapshot age, 9.4 tiles per second, is normal); 0.5 to 1 tile eases over 0.1 s; one tile
or more snaps (a wall the server saw, a teleport, a stop the server crossed a tile boundary on
first); a new region epoch restarts from the server. The sent direction is the input's sign, so
both sides finish a reserved tile after a released key; while stunned, exhausted or mid-swing it is
none (the local gates, which the server does not know yet); with only a click path moving it is the
direction the path step took. At 100 ms round trip the steady error approaches half a tile, the
reason for the dead zone; a streamed snapshot channel or a server-side sequence echo would
tighten it (not needed at loopback).

**Not available online yet** (no `PlayerCommands` member, so no throwing stub either): equip,
unequip, use item, cast (the connection already has `equip`, `unequip`, `useItem` and `cast`),
corpse recovery, shop and storage windows (`shopOffers`, `shopBuy`, `shopSell`), training, dropping
items, chat, the repeating auto-attack and the hotbar. Their screens still act locally and must be
routed one by one (each with its characterization test first) in the later stages.

Other players are drawn by the screen's own render slot for a `RemoteWorldDriver` world and walk
(`PlayerMovement.puppetMoving`); a local session draws nothing extra. Preview image (the real
`MainGameScreen` on a scripted link: the click on a far Skeleton goes out as `/attack`, the scripted
refusal shows as "Your target is out of reach." in the real chat window, the other players stand
in the world): `multiplayer/design/remote-click-attack-refusal.png`, produced by
`RemoteClickAttackPreview` (the attack callback is invoked directly, not through a mouse event).
Next, R4: the launcher opens the real screen from the lobby with a `RemoteGameSession`; the screen
builds `GameInputHandler` with the session's commands, binds the host, calls `confirmRegionReady`
after each map load and shows the reconnect banner.

### 3.14 Remote session, step R4: launcher and the first live rendered run (T4C-0297)

**Scope change.** While R4 was being built, the lobby-to-real-screen launcher landed on `main`
(T4C-0293 to 0296: `MyGame.launchOnlineLobby`, `MainGameScreen(MyGame, OnlineConnection)`, the region
acknowledgement, the disconnect text with "R" back to the lobby, remote shops, inventory and
abilities). The R4 launcher written in parallel (`OnlineGame`, `OnlineGameFlow`) duplicated it and was
dropped, not merged. What this step adds on top of `main`: Switch character (Options) on a remote
session saves nothing and returns to the lobby instead of the local roster, and the return records
the 35 s rejoin hold (`MyGame.returnToOnlineLobby`), and the live evidence below. Launch is still the
opt-in `--lobby` / `-Lobby`; the default launch is unchanged.

**Live run (2026-10-29, local evidence only).** Disposable PG16 cluster `t4c_acc_f393573926df`
(acceptance-cluster.ps1, loopback 57311), one `ServerMain --online` child on 18811 (game socket
18812), two operator-provisioned characters on two accounts (`--provision-online`, profiles in TEMP,
deleted afterwards). One rendered client window (real LibGDX window, no special JVM options, audio
disabled) running the real launcher `MyGame` and the real `MainGameScreen`; the second player was a
headless `OnlineConnection` adapter in the same process. Everything was torn down (cluster
`DESTROY_OK`, server stopped, profiles deleted); no protected port, QA process or existing database was
touched. Driven by `OnlineLiveRunDriver` (test scope): the lobby's Enter key delivered to the lobby's
own input processor; movement keys fed through a wrapper around `Gdx.input.isKeyPressed` (the game
polls keys, so this is the screen's own handler running on scripted key state, not OS-level key
events); the Switch character action invoked on the screen (the Options button click itself was not
driven). Screenshots are the real back buffer.

| Image (`multiplayer/design/`) | What it shows |
|---|---|
| `live-1-lobby.png` | the account lobby listing the provisioned warrior |
| `live-2-temple-hud.png` | the real game screen in the Lighthaven temple: HUD, hotbar, chat, radar, health orb, temple NPCs from the server |
| `live-3-own-character-moving.png` | 0.9 s into a held "right": the camera has scrolled with the character; the second player stands in view on the right |
| `live-4-after-move-other-player.png` | after the walk: a different part of the temple, the other player and NPCs in the world |
| `live-5-back-in-lobby.png` | after Switch character: the lobby shows "still being released, you can play again in 33 s" (lease hold) |

Worked: lobby to world in about 3 s after Enter (screen built in one frame, 9.8 s after launch
including asset loading), no reconnect or snapshot error in the client log, own movement visibly
tracks the held key, the adapter stays connected and the other player is drawn, Switch character
returns to the lobby with the hold. Not driven or not shown: attack, pickup, NPC click (covered by
R3 previews and tests), reconnect after a server restart (stage 1 exit criterion, still open; the
current behaviour is the disconnect text and "R" back to the lobby), a real mouse or OS key event.

**Findings** (nothing was fixed beyond the Switch character path; each is parity debt):

1. The online screen is very dark. The remote screen starts a fresh day/night cycle (default constructor, whose 7 * 3600 default is passed to the hour parameter in seconds, so the starting time is unclear) and not the server
   world time of day; the cause of the darkness was not isolated here. The own character
   is hard to find in the screenshots. A server clock (or a fixed daylight for online) is needed.
2. Other players have no name, health bar or appearance (known, 3.12); in the images the second
   player is a small default figure.
3. While joining, the lobby shows nothing (no "connecting" text) before the game screen appears.
4. `ServerMain --online` kept running a listener entry for the game socket briefly after stop (OS
   TIME_WAIT, no owning process); nothing to fix.

Single-player is unchanged: `MainGameScreenGoldenTest` (3 tests, not skipped) and every pin in the
stage 0 and seam sets pass with the golden images untouched. Next: the reconnect-after-restart
check and a longer two-rendered-client run (stage 1 exit), then stage 2.

### 3.15 Live rendered two-client runs: sight, talk, restart, fight, loot (T4C-0306)

**Evidence class: rendered clients, local only.** Two real LibGDX windows (separate processes, each the
real `MyGame` launcher and the real `MainGameScreen`, 1280x768, audio off) against one real
`ServerMain --online` on a disposable PostgreSQL 16 cluster (`acceptance-cluster.ps1`, cluster
`t4c_acc_ede1df959c39`, loopback 57411; server HTTP 18851, game socket 18852; protocol 7 and the
schema-13 migrations of `main` e8c062c0 plus this branch). No protected port, QA process, owner window
or existing database was touched; the cluster was destroyed (`DESTROY_OK`), the server stopped
and the private profiles deleted afterwards. Nothing here is remote or clean-machine evidence.

**How it is driven (honest scope).** `OnlineLiveRunDriver` (test scope, one process per player, marker
files in a shared directory to line the two up) feeds the *screen's own handlers*: Enter and R go to the
lobby's/screen's own input processor; walking is scripted key state behind `Gdx.input.isKeyPressed`
(path found over the client's own collision map); attack, NPC talk and loot pickup are left clicks
(`touchDown`/`touchUp`) delivered to the screen's input processor at the pixel where the target is
drawn, so `MonsterInputHandler`, the NPC click handler and `GroundItemClickHandler` run unchanged and
send their intent to the server. Not real OS mouse/key events. Not driven: character creation, equip,
inventory/spell panels, spells, shops, quests, portals/travel, the Options button, a region-separated
observer, fatal-return/corpse recovery, an inventory-refusal pickup, the claim race *inside* the owner
window, any run longer than a few minutes, any lost-response fault. Operator tool
`multiplayer/tools/LiveRunProvision.java` created the accounts and characters (level-1 warriors in the
temple, level-8 warriors beside a Skeleton anchor, the same headroom rule as `NativeLootProbe`); the
operator stopped and restarted the server with the private graceful `/admin/shutdown`.

Reproduce (PowerShell, repo root, after `mvn -q -DskipTests package dependency:build-classpath
-Dmdep.outputFile=target/classpath.txt` and `mvn -q -f multiplayer/pom.xml -DskipTests package`): create the cluster
(`-Action Create -Port <free>`), start `t4c-server.jar --online worldmap.colbin <port>` through `-Action RunEnv` (set
`T4C_CONTROL_FILE`, `T4C_BIND_ADDRESS=127.0.0.1`), run `LiveRunProvision COLLISION temple|skeletons OUTDIR [SUFFIX]`
through `RunEnv`, then start two `OnlineLiveRunDriver` processes with arguments
`http://127.0.0.1:<port> <profile> <shots> <tag ending A or B> temple|skeletons <syncdir> <windowX> 20`
(classpath `target/classes;target/test-classes;<classpath.txt>`, working directory the repository root). The
operator creates `server-restarted` in the sync directory after the restart.

| Scene and result (images in `multiplayer/design/`) | Observed |
|---|---|
| Lobby selection, `live2-01` | each account's lobby lists its own warrior as Ready; Enter selects it; the real screen is built about 3 s later (11 to 12 s after launch). |
| Mutual sight, `live2-04` | both windows drew the other player at once (`otherPlayers` = 1 on each side); both stood a few tiles apart at the authored spawn. |
| Talk out of range, `live2-06` | the first click (driver still stuck behind a bench, 208 px away) was refused by the server: "That person cannot talk to you right now." |
| Kilhiam, first greeting and repeat | after a collision-aware walk (10 to 12 s) each client clicked Kilhiam; each got exactly one parchment letter (0 to 1), a repeat click gave no second one. |
| Server stop and restart, `live2-09`, `live2-10` | graceful stop (HTTP 200); within about 5 s both clients showed "Disconnected. Press R to reconnect."; R (the screen's own key) returned each to the lobby, which showed "still being released, you can play again in 33 s"; Enter retried every 2.5 s rejoined both after 33.1 s. |
| After the restart, `live2-12` | position equal to the committed tile (a few pixels from the pre-stop spot), inventory identical, the other player drawn again; a greeting after the restart gave the repeat text and no second letter for either player. |
| Attack, `live2-13` | two level-8 warriors clicked Skeletons through the monster click handler; about 38 attack clicks each; both gained committed experience; the first drop (Leather Gloves) appeared after about 34 s (`live2-14`, both beside it). |
| Death and respawn, `live2-15` | player A kept standing among Skeletons while waiting and died once (one `online_corpses` row for that character in the database); the screenshot shows A back in the temple after the respawn. The death was incidental, not a scripted death scenario. |
| Pickup race, `live2-16` | the drop of the previous run (killer's 60 s window long over) was clicked by both clients at the same moment: the database shows one claimant (`CLAIMED`, one claim receipt) and the other player's HUD showed "The pickup was refused. The ground may have changed."; the winner's inventory went from 1 to 2 Leather Gloves, the loser's stayed the same; both snapshots then listed no loot. |
| Greeting with the own name, `live2-07`, `live2-08` | after the fix below the parchment greeting reads "Hello Live Bravo V, a messenger brought this parchment for you." When both players clicked Kilhiam in the same instant, one of them got "The conversation was refused. Try again." and succeeded on the second click (one letter each in the end). |

**Findings.**

1. **The online screen is very dark** (`live2-02`) and does hinder screenshots: the own character and NPCs are hard
   to see. The driver set the screen's day/night cycle to hour 12 through reflection (`live2-03`); that is a driver
   convenience, not an in-game toggle. `DayNightCycle` was not touched (owner decision pending).
2. **Fixed here: a literal `%s` in NPC greetings.** Server replies carry integer arguments only, so
   "Hello %s, a messenger brought this parchment" arrived with the placeholder. `RemotePlayerCommands` now fills a
   text placeholder with the client's own character name (test `RemotePlayerCommandsTest`, verified live).
3. **Not edited, for the loot/server lane: the public loot view never counts the owner window down.**
   `ownerRemainingMs` stayed at 60000 for the 90 s the drop sat there (`RegionGroundLoot` keeps the value read at
   publication); `ownerPlayerId` stays set. The claim itself uses database time and was correct (the race above).
   A client cannot show when a drop becomes free. *Fixed in T4C-0307:* the public view now counts the window down from the monotonic time the figure was read and clears the owner when it ends (claims unchanged).
4. **Not edited: simultaneous talk to one NPC** refused one player with the generic "conversation was refused" (the retry
   worked). Probably single-flight on the NPC; the key does not say "busy". *Resolved in T4C-0307:* NPC talk is single-flight per character (a pending event or a revision change), not per NPC; `busy` and `revision` now map to `online.npc.busy`. The pickup race loser now gets `loot_taken` / `online.loot.taken` when the claim row shows another character took it.
5. **Not edited: the pickup-race loser sees the generic refusal text** (`online.loot.refused`), not "someone else took it".
6. After a server restart the lobby still holds the client 33 s (the 35 s lease hold is applied by the client whatever
   the cause). Safe, but a restart needs no hold; a UX point for the owner.
8. (Resolved in T4C-0358, 3.21: the restarts were late polls, not the route shape.) Other players' legs: these runs used a build from before PR #266 (walk hold for other players), and the screenshots are single frames, so whether the other player's legs animate while walking was not observed and is not claimed. A later run on a build with #266 would show it.
7. A game run rewrites the tracked `t4c-chat.log`; it must be reverted before committing.

### 3.16 Live rendered gameplay panels: inventory, spells, potions, shop, walking frames, quest (T4C-0312)

**Evidence class: rendered clients, local only, same harness and limits as 3.15.** Disposable PostgreSQL 16 cluster
`t4c_acc_c5f1ec67edd1` (loopback 57721), one `ServerMain --online` (HTTP 18881, game socket 18882), protocol 7, `main`
f3d4ed62 plus this branch; the cluster was destroyed and the server stopped afterwards, no protected port, QA process or
owner window was touched. Two real `MainGameScreen` windows per scene (separate processes, `OnlineLiveRunDriver`).

**How the new controls are driven.** Only through the screen's own input processor: `I` (inventory), `C` (spells), `U`
(potions), the digit keys, `Tab` and `T` (shop), and left clicks on the shopkeeper. The driver reads the screen's panels by
reflection only to decide which digit to press and to log the list, and it reads the *committed* state from the connection
(`OnlineConnection.view().character()`: revision, gold, vitals, worn slots, inventory, cooldowns). Not real OS key or mouse
events. Operator tool `LiveRunProvision` gained scenes `wares` (a badly wounded level-1 Healer and a level-1 Warrior
carrying an over-weight stack, both beside Sigfried), `quest` (a level-30 warrior and a level-1 observer beside
WardenAelric) and `move-skeletons` / `move-giver` (a plain position update on the quester's row while the server is
stopped: the Skeleton ground is about 160 tiles from the giver behind cave mouths, and walking it is the soak's job).
Reproduce as in 3.15 with `LiveRunProvision COLLISION wares|quest OUT [SUFFIX]` and the driver's scene argument
`wares` or `quest`; the operator stops and restarts the server at each `stop-ready` marker (the quest scene has two cycles,
`to-skeletons` and `to-giver`).

| Scene and result (images `multiplayer/design/live3-*`) | Observed |
|---|---|
| Inventory panel, `live3-03`, `live3-04`, `live3-05` | `I` opened the panel on the committed items (worn slots first, then carried). Digit 2 unequipped the staff, digit 2 equipped it again, digit 1 unequipped the robe; after each press the committed worn slots changed as expected (revisions 10 to 12, 12 to 13, 13 to 15; the revision also moves on background regeneration checkpoints). |
| Equipment after a server restart, `live3-15` | with the robe off and the server stopped and restarted, the character rejoined (33 s hold) with the robe still off and identical inventory; digit 2 equipped the robe again and the commit showed it worn. |
| Spell, `live3-06`, `live3-07` | `C` listed the two learned spells the server can resolve (Heal Light, Stone Shard, each Mana 2); digit 1 cast Light Heal on the wounded Healer: HP 5 to 21 of 28 and the log line "Health +14 \| Mana -2"; the mental, attack and physical exhaustion cooldowns appeared in the committed state. |
| Potion, `live3-08`, `live3-09` | `U` listed only the three Light Healing Potions; the Potion of Mana the character also carries is **not offered** (not in the server's item-effect list). Digit 1 used one: HP 21 to 28, the stack went from three to two, committed. |
| Shop, `live3-10` to `live3-13` | a click on Sigfried opened the buy list with the server's prices and "Your gold: 212"; digit 1 bought a Rusted Short Sword (212 to 203 gold, the item in the inventory); digit 3 (Rusted Hand Axe, 787 gold) was refused with "You do not have enough gold." and gold and inventory were unchanged; Tab then digit 2 sold a Torch (203 to 205). |
| Over-weight refusal, `live3-14` | the Warrior carrying an over-weight stack pressed digit 1 (a Rusted Short Sword, 9 gold): "You cannot carry that much."; gold 207 and the inventory unchanged. |
| Other player: appearance, `live3-01` | drawn at once in the other window, in the **default naked look** (not the warrior's equipment), with **no name and no health bar** above the figure. |
| Other player: walking frames, `live3-02` | the Warrior walked about eight tiles out and back; a strip of 12 crops (still, then walking) taken from the Healer's real back buffer shows the legs change from frame to frame, and the puppet's own walking flag and animation timer were logged for 148 samples. The animation runs, but it restarts about every third sample (about 0.35 s): 51 of the 148 samples show the standing pose while the position advances (in the earlier run, over a straight stretch, the timer ran continuously up to 2.6 s). Cause not isolated; the driver's stair-stepped route is a candidate. |
| Quest accept, `live3-16`, `live3-17` | the first greeting click on WardenAelric landed on **Moonrock**, an NPC drawn in front of him (the screen's click handler takes the first NPC under the cursor): "That person cannot talk to you right now." The screen has no topic control in any case, so the offer (topic 0) was sent through the same `OnlineConnection` the screen uses and **logged as not driven through the screen**: reply accepted, the committed quest status went from 0 to 1. |
| Server stop, move, restart (operator) | the operator stopped the server, moved the quester's saved position 160 tiles to the Skeleton ground (plain row update), restarted; the client returned to the lobby, waited the 33 s hold and rejoined at the Skeleton anchor with the quest status 1 and kills 0. The same again in the other direction later. Walking the route was not tested here. |
| 12 Skeleton kills, `live3-18` | the level-30 warrior clicked Skeletons through the monster click handler: 230 attack clicks in about 200 s; the committed kill counter rose from 0 to 12 (status 1), experience +3072 and gold +171 from the monsters. HP fell to 57 of 259 at the worst; the driver drank the three Light Healing Potions through `U` and did not die. A first attempt with a level-12 warrior (119 HP) was aborted at 10 HP after only three kills: the known lethal starter ground (T4C-0248, owner decision: unchanged). |
| Turn-in, `live3-19`, `live3-20` | back at the Warden (kills 12, status 1 still committed after the second restart) the first click on the Warden alone turned the quest in: status 2, gold 391 to 891 (+500), experience +400, `spell.renew_armor` learned, one commit (revision 257 to 259); the message reads "...here's how to renew your own wards in a single breath." A second greeting changed nothing (gold, experience, spells and status the same). The quest has no required item (`requiredItemKey` is null), so the required-item path was not exercised. |

**Findings.**

1. **Fixed by T4C-0313 (see below): the online lists and the shop panel were drawn upside down in the real screen** (`live3-04`,
   `live3-06`, `live3-08`, `live3-10`): the title is the lowest line, rows run 9 up to 1 and the block sits at the bottom
   right on top of the radar. Repro: online window, press `I`, `C` or `U`, or click a shopkeeper. `MainGameScreen` draws
   them from `hudCamera.viewportHeight - 16` downwards, which is the top edge only on a y-up camera.
2. **Fixed by T4C-0313: the shop title showed the raw NPC key** (`${npc.sigfried}: buying`): `showShop` passes
   `NpcView.displayName` unresolved to the panel; `I18n.resolve` is not applied.
3. **Fixed here (Claude lane, tests `RemotePlayerCommandsTest`): the first click on a shopkeeper after walking up was
   often refused** with "They are busy for a moment. Try again." (refused as busy or stale revision: a position or
   regeneration checkpoint had just moved the committed revision; this is the likely cause, the server's reason was not
   logged). It happened on three of the four first clicks in the first two runs and once for a trade ("The shop is busy.
   Try again." instead of the too-poor refusal). A greeting or a
   trade refused with the busy text is now sent once more with the refreshed revision (a refusal changed nothing, so
   nothing is ever sent twice); any other refusal is shown at once. In the final run both clients opened the shop on the
   first click and the too-poor refusal showed its own text.
4. **Gap: no mana potion online.** The U list holds only healing potions; Potion of Mana carries an item effect the server
   does not yet allow. Also only Light Heal and Stone Shard (needs a creature target, not driven) are castable by the
   starters, and no starter spell is a buff, so **buff persistence across a rejoin was not exercised**; mana (21 of 22
   at the stop, 22 of 22 after) and the exhaustion cooldowns were present and consistent.
5. **Gap: other players have no appearance parts, no name and no health bar** (the snapshot carries none; the puppet uses
   the default naked parts; `renderNameOverlay` covers monsters, NPCs and objects only).
6. **Fixed by T4C-0313: the screen had no NPC topic control.** A click sends the greeting only; the quest offer is topic 0.
   A greeting turns a finished quest in but never offers it.
7. The committed revision rises constantly (regeneration and position checkpoints), which is why a command can race one.
8. **Fixed by T4C-0313: overlapping NPC click boxes.** Moonrock stands in front of WardenAelric; a click
   where both boxes overlap goes to the first NPC under the cursor and is refused ("That person cannot talk to you right now."),
   both at the first greeting and at the first turn-in attempt (`live3-16`). The driver now aims at a point where only the
   wanted NPC is under the cursor. A player has to click the visible part of the Warden.
9. **Known, owner-accepted: the Skeleton ground is lethal** (T4C-0248): the quest was only completable at level 30 with the
   three starter healing potions; a level-12 warrior died away at 10 of 119 HP after three kills.

**T4C-0313 follow-up (Claude, owner rule: online looks and plays exactly like single-player).** The four defects above are
fixed in the screen, not in the legacy text client:

- *Lists and shop:* the I, C, U and shop lists draw in `OnlineListWindow`, a `JournalWindow` (the chrome and centring
  every single-player service screen uses): title in the gold display font, body rows top to bottom in a well, key hint at
  the foot, X button. `OnlineListWindowLayoutTest` pins top-to-bottom row order and that the window never covers the radar
  disc on 1280x720 to 2560x1440. The full graphical single-player `Inventory`, `SpellBook` and `ShopScreen` are not reused:
  they mutate a local `Player` and basket; online equip/cast/trade still go through the server intents (design stage 3
  and 5 replace the lists with those screens). Evidence: `multiplayer/design/online-list-*.png` (preview with sample
  lines, `OnlineListWindowPreview`; not a live run).
- *Shop title:* the panel is opened with `I18n.resolve(displayName)`.
- *Topics:* single-player has no topic list, the player types a highlighted keyword. Online sends a topic index, so a
  greeting that opens no shop now opens the same window listing the NPC's authored keywords (`npc.topic_keyword.*.0`);
  digit keys send `interact(npc, topic)`, Esc, I, C, U, walking a tile or a disconnect close it. A reply to a topic never
  reopens the list.
- *Overlapping boxes:* in puppet (online) mode `NPCManager.findNpcAt` picks, among the boxes under the
  cursor, the one whose box centre is nearest, the one lower on screen on a tie. Single-player keeps the first NPC in
  list order, unchanged (pinned by `RemoteClickHooksTest`).
- *Not verified:* a live two-window run on a server; the changes are covered by unit tests and the rendered preview only.

### 3.17 Live rendered session: topic panel, equipment seen by the other window, loot races, portals, corpse recovery, 39 minutes (T4C-0318)

**Evidence class: rendered clients, local only, same harness and limits as 3.15 and 3.16, but nothing relocated by the operator.**
Base `d4eff350` (main at the time) plus this branch; protocol 7; disposable PostgreSQL 16 cluster `t4c_acc_d53a6def51e0`
(loopback 57901) and one `ServerMain --online` (HTTP 18931, game socket 18932), all destroyed or stopped afterwards (cluster
`DESTROY_OK`, server `/admin/shutdown` HTTP 200 and exit); no protected port, owner window or peer process was touched. Two level-30
warriors (`LiveRunProvision beta`) start beside WardenAelric; the second carries a filler stack that leaves exactly 2 weight units
free. Driven only through the screens' own handlers (keys and clicks delivered to the input processor, walking by scripted key state
over the client's own collision map, 165 tiles from the temple to the Skeleton ground included); the pointer for the hover outline is
injected through the driver's `Gdx.input` wrapper (the screen's own hover code runs); **through the connection instead of the screen:
only the corpse recovery** (the screen has no corpse control, see finding 4). Reproduce as in 3.15 with scene `beta`
(`LiveRunProvision COLLISION beta OUT [SUFFIX]`, driver arguments `... beta <sync> <x> 20 <goalTileX> <goalTileY>`; the provisioner
prints `skeletonGround=`); `-Dbeta.scale`, `-Dbeta.from=ground|death` and `-Dbeta.until=equip` shorten or split the script.

| Scene and result | Observed |
|---|---|
| NPC topic panel, `live4-01` | a click on Warden Aelric opened the topic window ("1. Quest, 2. Grave"); digit 1 (topic 0) on the screen's own key handler accepted `renewed_wards` in both windows (committed status 0 to 1, revision 2 to 4 and 4 to 5); the chat showed "Put down twelve of the skeletons ...". The window stays open after a topic; Esc was pressed afterwards. Nothing was sent around the screen (3.16 sent the offer through the connection). |
| Equipment seen by the other window, `live4-02` | one window pressed `I`, took the leather pants off and put them on again; the other window's snapshot actor and drawn model changed with it (LEGS leaves the equipped map and returns) and, with the pointer on the figure, drew the name above it and the green health-colour outline (T4C-0314 live). Then the roles swapped. The strip shows before, legs off, legs on. |
| Portal trips, `live4-04` | Lighthaven door 16 and return door 17, then cave mouth 13 and exit 15, each character in turn with the other window watching: the observer's drawn players and snapshot players dropped to 0 within 2.6 to 39.8 s of the walker starting (the walk is part of that time), the walker's screen switched region (`start` to `lighthaven-interior` to `start`, `skeleton-cave`; location epoch 1 to 4) and the other window drew the arrival in the new region with the right name and gear; both windows were then in the same region and saw each other (8 hops, 0 timeouts). Players in separate regions saw nothing of each other. Targeting, damage or recovery across the boundary was not tried. |
| Owner-window countdown | for all three drops both windows' committed views held the owner window and counted it down in real time (for example 57441, 47351, 37379, 27318, 17325 ms at 10 s steps); only the owner's view carries its own player id as owner (by design), the other window sees `null` and the same remaining time. The screen draws no countdown (finding 3). |
| Claim race inside the window, `live4-03` | drop 1 (Light Healing Potion, killed by Alpha): both clicked at the same instant, 52.9 s left: Alpha's carried count 3 to 4, Bravo "Another player has the first claim on that item for now." and 3 to 3. |
| Claim race after the window, carry weight, `live4-03` | drop 2 (Leather Gloves, weight 3, both windows past the window, `ownerRemainingMs` 0): Alpha 1 to 2 gloves; Bravo, with 2 units free, "You cannot carry that much weight." and 1 to 1, the drop stayed on the ground until Alpha's claim (the capacity refusal preserved loot and inventory). |
| Race that lost to a committed claim, `live4-03` | drop 3 (Healing Potion, inside the window): Alpha 24 to 25; Bravo's request arrived after the commit and got "Another player picked that up first." (`loot_taken`, T4C-0307) with 23 to 23. In an earlier run of this session a click 167 px away got "That item is too far away to pick up." |
| Database after all runs | `online_ground_loot`: 3 CLAIMED, 6 EXPIRED, 3 ON_GROUND; no loot id with two receipts; every CLAIMED row has exactly one receipt and the claimant equals the receipt's character. |
| Death and corpse recovery, `live4-05` | Bravo stood among the Skeletons without swinging or drinking until a corpse was listed (the server respawns the character in the temple at once), walked back through the temple door and 165 tiles to the Skeleton ground with the keys (fighting on the way) and, within reach, `/recover` through the connection was accepted on the first try: the corpse's three stacks (3 permits, 1 Healing Potion, 1 Torch) returned to the inventory (permits 45 to 48, Healing Potions 19 to 20, Torches 2 to 3), worn equipment unchanged, the corpse row recovered with its item list empty. Out-of-range at 96 px was refused first in an earlier run (the reach is the melee range, 64 px). Individual single-stack recovery (the fourth `/recover` token) and a second recovery of the same corpse were not driven. |
| 39-minute session, run `beta1` | 39.4 min (2363 s) from lobby to the last script step, of which 19.0 min were timed fight, idle (40 s of every 300 s) and loot segments (540 s and 600 s, with the cave trip between) and about 1.6 min the walk to the ground: Alpha 727 attack clicks, +8,862 experience and Bravo 776 clicks, +6,365 experience in the logged part, lowest hit points 9 and 2 of about 270, both still connected at the end. Client logs: 0 `ERROR`, 0 `WARN`, 0 exceptions in 500 and 565 lines; server: nothing on stderr, 0 "Online persistence failed" lines, graceful stop with HTTP 200. Client heap (no `-Xmx`) swung between 2.4 and 3.9 GB in a sawtooth with no upward drift over the 19 minutes. No server restart, forced kill, lost response or reconnect was part of it. |

**Findings.**

1. **Fixed here (Claude lane, tests `RemotePlayerCommandsTest`, `OnlineCommandRefusalsTest`): an equipment change refused as busy or
   stale failed with only "The equipment change was refused."** The second window's first unequip was lost this way in two
   equipment passes (a regeneration or position checkpoint had just moved the committed revision, the same race as the shop and greeting
   refusals of 3.16). The equip and unequip refusals now map `busy` and `revision` to their own key (`online.equip.busy`), and the
   command is sent once more with the refreshed revision; any other refusal (requirements, conflict) is shown at once and never
   repeated. Live: the new code ran in the later equipment pass (accepted first time; the refusal itself did not recur, so the retry
   path is covered by the tests only).
2. **Fixed here, not re-measured live: about one attack click in five was answered "Your character is busy. Try again in a moment."**
   in the chat (148 of 727 clicks for Alpha, 147 of 776 for Bravo in `beta1`, `live4-03` shows the chat). The same single retry is
   now applied to attacks (the first busy refusal is silent, a second one is shown); `RemotePlayerCommandsTest` pins it. The
   routine swing-cooldown refusal stays silent as before.
3. **Gap: the screen draws nothing for the owner window** (only the refusal text when someone else clicks); the countdown that
   T4C-0307 made correct is visible in the connection's view only.
4. **Gap, M2: the screen has no corpse. Fixed in T4C-0321 (not re-run live).** At the time of the run a server corpse was neither
   drawn nor offered for recovery (the view lists it; the legacy text client has a `recover` command), so the recovery above went
   through the connection. The screen now draws the single-player corpse marker (the name "Corpse" where the character fell;
   single-player has no corpse sprite or key) from the connection's own corpse list, and a click on it sends `/recover` and shows the
   server's refusal texts. Still open: individual single-stack recovery from the screen.
5. **Not a game gap: the Healing Potion list was empty because the provisioner gave the stack zero charges.** With 19 in the bag the
   `U` list said "You carry nothing you can use online". `LiveRunProvision beta` created that stack with zero charges, which the
   server refuses by design (single-charge consumables only); a real Skeleton drop carries the item's one charge and is offered
   (pinned by `OnlineAbilityChoicesTest`). The Healing Potion spell was already allowlisted. The genuine gap was the Potion of Mana
   (3.16), fixed in T4C-0321: the allowlist now admits every offline consumable that only restores health or mana. The level-30
   characters came within 2 and 9 hit points of dying on the known lethal ground (T4C-0248).
6. **Driver, not game:** the route search oscillated (A, S, A, S) for minutes at the temple respawn tile in three runs while the
   keys alone moved freely in all four directions (`mortal` diagnostic scene); the script now walks south out of the room first.
   Whether a human can get stuck there was not established. The earlier diagonal walk into the Lighthaven exit door corner is also
   fixed in the driver (one axis at a time).
7. The Lighthaven interior and the skeleton cave stay almost black whatever the driver does with the hour (`DayNightCycle` untouched,
   owner decision pending): the arrival screenshots of those regions are unusable, the logs are the evidence.
8. A live check of the new T4C-0313 windows: the topic window, the inventory list and the weight and refusal texts rendered and
   worked on the real screen. No screenshot of the inventory window itself was kept.

**Not driven:** the lost-response and forced-kill cases, a server restart in the session, the quest kills and turn-in for the second
window, mana potion, creature-target casting, spell merchants, character creation, the Options button, targeting or damage across
a region boundary, individual single-stack corpse recovery, and a mixed session on the packaged candidate.

### 3.18 Rendered lobby error cases and local HTTPS (T4C-0325, milestone M1)

**Evidence class: rendered lobby windows, local only.** Same harness idea as 3.15 but a purpose-built test-scope driver,
`OnlineLobbyCasesDriver` (one process per case; Enter, Down/Up and C are delivered to the lobby screen's own input processor;
the only injected value is the typed creation name). Operator tool: `LiveRunProvision` scenes `lobby`, `lobby-leases`,
`lobby-stale-move`, `lobby-created`. Two disposable PG16 clusters (loopback 57931 and 57932, `DESTROY_OK`), one
`ServerMain --online` each (HTTP 18941, HTTPS 18961, game socket one above), both stopped. Reproduce: build as in 3.15, create the
cluster, start the server through `RunEnv`, run `LiveRunProvision COLLISION lobby OUTDIR`, then one driver per scene
(`plain`, `roster`, `inuse`, `stale`, `create`, `tls`) with the endpoint, a profile from OUTDIR and a sync directory. For the
version case put a copy of `OnlineProtocol`, `OnlineAccountClient` and `OnlineConnection` compiled with `VERSION = 8` first on
the client classpath (the constant is inlined, so all three must be recompiled). For HTTPS give the server
`T4C_TLS_KEYSTORE`, `T4C_TLS_PASSWORD_FILE`, `T4C_PUBLIC_ORIGIN=https://localhost:<port>` and the client a truststore holding only
the public certificate.

| Case (image in `multiplayer/design/`) | Observed |
|---|---|
| Invalid key, `m1-lobby-invalid-key` | roster read refused (401); text "The server did not accept this account. Check your online profile."; Enter opens no connection. |
| Empty roster, `m1-lobby-empty-roster` | "This account has no characters yet. Press Create to make your first one."; Enter does nothing; no connection. |
| Incompatible version, `m1-lobby-incompatible-version` | client with protocol 8 against server protocol 7: "This client and the server use different versions. Update the client and the server together." (before the fix: "Could not reach the online server"). |
| Pagination, `m1-lobby-roster-page-1`, `m1-lobby-roster-page-last` | 25 characters (one Ready, one Region offline, one Respawning, 22 New) in two server pages, all listed in server order, `truncated=false`, scrolls to the last row. |
| Unavailable, `m1-lobby-unavailable-region` | Play on a Region offline character and on a Respawning one: "That character cannot be played right now (Region offline|Respawning). Choose another character or try again later."; no lease left. |
| In use, `m1-lobby-in-use` | window B (same account) shows the character In use; Play: "That character is already in use, in another window or in a session that has not timed out yet. Wait about 30 seconds, then press R." Window A kept playing; lease released when A left. |
| Foreign, `m1-lobby-foreign` | the character was moved to another account while the lobby was open; Play: "That character could not be entered. It may be in use in another window (wait about 30 seconds, then press R), or it is not on this account."; the next roster no longer lists it. |
| Selection alone | database lease count 0 after 8 s on a 25-character roster and after every refused Play; 1 only while the ready character was in play. |
| Creation (extra), `m1-lobby-create-form` | C opened the form; typed name, Down chose Archer; Enter created one character, the roster listed it, Play entered the game with 46 inventory stacks and 204 gold from the server-generated kit. |
| HTTPS (`m1-tls-lobby-trusted`, `m1-tls-hostname-mismatch`) | trusted `localhost` certificate: roster and join worked; default trust and `127.0.0.1` host name: "Could not reach the online server", no roster. |

Finding fixed here: the server answers an unknown key, another account's character, a missing one and one in use with one
refusal (409 `account_or_session`, on purpose: no account oracle), and `OnlineConnection` only logged the failure, so
`OnlineHandoffScreen` returned to the lobby without a word. `OnlineConnection.failureText()` now carries the reason (recorded
before the status flips to disconnected), `OnlineJoinFailure` maps refusals to messages, and the lobby refines the general
refusal with the freshly read row of the selected character (in use, Region offline, Respawning). Not changed: the lobby still
offers Play on a row that says In use (the roster is advisory; the server decides). Two notes written here at the time are
settled by T4C-0352 (section 3.20): PARITY.md's Archer line was stale (online archery exists since T4C-0273), and the owner
ruled on 2026-10-30 that in-client creation is in Beta scope, so the empty-roster text that points to Create is correct.

### 3.18 Live rendered combat and abilities with two different roles: contention, spells, buff and rejoin, refusals, death seen from the other window (T4C-0331)

**Evidence class: rendered clients, local only, same harness and limits as 3.15 to 3.17.** Build: `main` 155c03a5 plus this branch,
protocol 8 (the first passes of the session ran on `main` b32587ff, protocol 7, and found the defects below; every case in the table was re-run on the merged build, except the Stone Shard cast under an unsupported quest flag, seen only on the first build), disposable PostgreSQL 16 cluster `t4c_acc_6937b64ed7ff` (loopback 57941), one `ServerMain --online` (HTTP 18951, game socket
18952), all destroyed or stopped afterwards (`DESTROY_OK`, `/admin/shutdown` HTTP 200); no protected port, QA process, owner window or
peer process was touched. Two real `MainGameScreen` windows per run (separate processes). Every run is a different pair of roles at level
18 (levels through the server's own XP award path): **Warrior + Healer** (the full script), **Archer + Healer** (refusals) and
**Paladin + Mage** (a second melee role, a fire caster and an area spell). Operator edits written by `LiveRunProvision m4`: the Healer
also knows Protection (a timed buff) and starts at 40 percent health, the Mage also knows Fireball, the Warrior and Archer carry ten Healing
Potions with the item's one charge, the Healer of the Archer run carries a quest flag the online game does not run, and the death run
starts the Warrior at 25 hit points. Reproduce as in 3.15 with scene `m4` (`LiveRunProvision COLLISION m4 OUT SUFFIX`, `-Dm4.pair=warrior-healer|archer-healer|paladin-mage`,
`-Dm4.level`, `-Dm4.alphaHp`, `-Dm4.alphaStack`; driver `-Dm4.chapters=census,buff,contention,refusal,area,reset,disconnect,restart,death,corpse`, `-Dm4.lives`, `-Dm4.spell`);
the operator stops and restarts the server at the `stop-ready-m4` markers; `LiveRunProvision m4-report` prints the non-secret database rows used below.

**Driven only through the screens' own handlers:** `C` and digit (spell list, cast), `U` and digit (potion), `I` and digit (equip, unequip), clicks on
creatures (selection plus the melee swing the monster click handler sends) and on the drawn corpse, scripted walking, the screen's `R` after a server stop.
**Scripted around the screen (logged as such):** the leaving of the world for the "disconnect" case calls `MainGameScreen.switchCharacter()` by reflection (the
Options button was not clicked); the committed state, creature hit points and the other player's public hit points are read from the connection's view; the daylight
toggle the other runs use was left as it was and `DayNightCycle` was not touched.

| Scene and result | Observed |
|---|---|
| Two roles, different abilities | Warrior list: "You have no spells you can cast online." Healer list: Heal Light (Mana 2), Protection (Mana 9), Stone Shard (Mana 2). Mage list: Arcane Spark (1), Fire Dart (1), Fireball (7). Paladin list: Heal Light (2). The potion list of every role offers the Light Healing, Healing and Mana potions. |
| Heal on self through `C` | Healer at 71 of 166: digit 1 "Health +13 \| Mana -2", health 71 to 84, mana 84 to 82, the mental, physical and attack exhaustion expiries committed (about 2 to 4 s). Later casts at 85 to 101 and 103 to 121 healed 16 to 18. |
| Timed buff through `C` | Protection (digit 2): "Mana -9", the buff is committed with 1,197,625 ms left (20 minutes). |
| Buff across a rejoin | Server graceful stop and restart (the lobby holds the character for about 33 s). Before the stop Protection had 1,053,736 ms left, after the rejoin 1,017,706 ms and 2.5 s later 1,015,182 ms: about 36 s used although about 85 s passed, because logged-out time does not run a buff down (as `OnlineBuffsTest` pins); the cooldown anchors, mana and vitals were the committed ones, position and inventory identical. Both windows rejoined after 33 s. |
| Creature-target spell | A click on the Skeleton selects it (and sends a melee swing that is refused out of reach: "Your target is out of reach."), `C` then digit 3 casts Stone Shard through the persistent connection, each cast "Mana -2", and the creature's health fell in both windows with them. A cast with no live target said "There is nothing there to target."; one behind an obstacle "You cannot see the target." |
| Splash spell | The Mage's Fireball (target type 17) was refused ("That spell cannot be cast on that target.") before the fix below. After it: accepted, "Mana -7", the 69-hit-point Skeleton died to the one cast. No second creature stood within 7 tiles, so the splash itself is covered by the PostgreSQL test only (`OnlineSpellStrikesTest.aFireballKillsTwoCreaturesAndWoundsAThirdInOneAtomicCommit`). |
| Contention, one creature life at a time (3 lives) | Both windows attacked the same creature id (announced by file). Both windows logged the same creature health sequence (life 1: 69, 48, 26, 11, dead, identical in both). Experience: life 1 Warrior +226 and Healer +30, life 2 Warrior +50 and Healer +146, life 3 Warrior +48 and Healer +208. The extra amount is the death experience: only the lethal blow's owner gets it, the other keeps per-hit experience, as SCOPE_PROPOSAL section 3 declares. Database: `online_world_rewards` lists the Warrior for life 1 and the Healer for lives 2 and 3. A second pair (Paladin and Mage) did the same on two lives. |
| Retaliation on the screen | After a hit the Skeleton hits back: 28 committed drops of 1 to 7 points for the Warrior in the whole run (103 points), each logged from the committed hit points; the crop `multiplayer/design/live5-retaliation.png` shows "Skeleton - Level 4" beside the Warrior with the health-colour outline. The Healer, who stood back, took none. (First build: a Skeleton did not attack an unprovoked player standing 25 px from it for 45 s; health rose by regeneration.) |
| Leaving reach: portal, no heal | The Warrior wounded a Skeleton to 48 of 69, walked 9 tiles and through the cave mouth and back. The creature stayed at its tile (it did not follow the fleeing player), kept 48 hit points when the player returned and across a server restart (committed life identity); it never healed. The database showed such wounded creatures without a target after the player left in the earlier passes (PATROL or RETURN, 47 and 19 hit points kept) and, at the end of the final pass, back in NATURAL mode on the nearby Healer at 48. |
| Disconnect leaves no immortal target | The Warrior wounded creature 29 to 41, then left the world (reflection, see above). The Healer then killed it from 41 to dead in 10 s and got its death experience (+200); the Warrior rejoined after 40 s. |
| Death seen from the other window | The Warrior (25 hit points) provoked one Skeleton, then did nothing. The Healer's window showed the Warrior's public hit points fall (27, 17, 9, 7, 4 of 173) in its own snapshot, then the figure was at the temple tile (217,10) with 86 of 173 (half health, the death rule). One database row for the corpse (1 stack), no ground-loot row from the death. |
| Corpse recovery on the screen | The Warrior walked back (131 s) and clicked the drawn "Corpse": "You gather your belongings from your corpse."; the carried list gained the stack back, the corpse listed empty and flagged recovered. (First build only: with an over-weight extra stack of the provisioner the same click was refused 6 times with "You cannot carry that much weight." and the corpse kept its contents.) |
| Unsupported combat | An Archer took its quiver off (WEAPON2): the bow alone is refused, twice, "You cannot attack that yet."; with the quiver back on the same swing hit and gave experience. A Healer carrying a flag of a quest the online game does not run was refused every attack ("You cannot attack that yet."); on the first build of the session its Stone Shard cast was refused too ("A creature in the way cannot be fought online yet."), the cast was not reached in the final-build pass. A Warrior could not wear the Archer's bow: "You do not meet the requirements for that item." |

**Defects found and fixed here (Claude lane, with tests):**

1. **Taking off a quiver dropped the whole online connection.** The `/unequip` and `/equip` body pattern allowed only capital letters and underscores, but the
   quiver sits in `WEAPON2`; the server answered 400 and the client treats a 400 as fatal ("Online connection stopped: Server rejected request: 400"). Now every body part name
   is valid (`PreviewServer.equipmentBodyValid`, `EquipmentSlotBodyTest` walks every `BodyPart`).
2. **Raw message keys in the chat.** A cast, potion or player-targeted cast sent while the previous command was in flight showed `online.spell.refused` / `online.item.use.refused`;
   neither key existed. The failure texts now use the existing "busy" text, and `RemoteCommandMessageKeysTest` fails when the command class names a key the catalogue lacks
   (it caught a third occurrence, merged in from `main` the same day).
3. **A cast or potion answered "busy" was lost.** The Protection cast right after a Light Heal was refused as busy (a regeneration checkpoint had moved the revision), the same race attacks, greetings and equipment already
   retry once. Casts, potions and player-targeted casts now retry once the same way (`RemotePlayerCommandsTest`).
4. **Fireball and its two siblings could not be cast from the spell list.** Area spells aimed at a creature (target type 17) were listed as needing no target, so the server refused them. The list now asks for the selected creature
   (`OnlineAbilityChoicesTest`). Type 19 areas (aimed at a tile, 20 spells) and area heals are still listed and still refused when cast from this list: they need the aiming and friend-target UI of the real spell book.

**Findings not fixed (report, owner or Codex lane):**

- A cast's reply text for an attack spell reads "Health +0 \| Mana -2": the committed reply carries no damage, so the player sees no number (events are stage 7).
- The retaliating Skeleton did not pursue a player walking away (12 tiles). **Root-caused in T4C-0334: a parity defect, not the authored hunting-ground rule** (the plain Skeleton has none): the server judged creatures and the chased player by the player's above-foot fold where single-player uses the static foot collision; fixed and pinned (MILESTONES.md, T4C-0334 note). Original wording follows. A full-health Skeleton 25 px from an idle player did nothing for 45 s, while a Skeleton wounded earlier was later in NATURAL mode on a nearby player (database); pursuit and natural acquisition are otherwise covered by `OnlineMonsterPursuitTest` and the earlier two-adapter probe only.
- There is no voluntary "return to lobby" key: `R` works only after a disconnect; the voluntary route is Options, Switch character (not clicked here).
- The death penalty roll can leave nothing on the corpse (the Warrior of one run died with no corpse row at all); whether a death with no drops should still leave a corpse marker is a product question.
- `PARITY.md` still said bows and stun blow were refused; `OnlineMeleeTest` shows both are accepted (bow only with a quiver). Corrected there.

**Not driven:** the Paladin's own Light Heal cast (its list offers it; the Healer cast the same spell), Battle Mage and Cleric, a splash hit on a second creature, any spell with a tile aim (type 19) or friend target on the real spell book, stat allocation (no online control), multi-charge items, creature spell attackers and clan combat on the screen, a lost-response or forced-kill fault, and anything on a packaged candidate or a second machine.

### 3.19 Live rendered inventory, equip, the whole authored adventure for both players, region boundary and loot/recovery fault drill (T4C-0342, milestones M2 and M3)

**Evidence class: rendered clients and native adapters, local only, same harness and limits as 3.15 to 3.18.** Build: `origin/main` 73f46063 and later merges (protocol 8) plus this branch;
disposable PostgreSQL 16 clusters `t4c_acc_cebf2600e307` (loopback 57971) for both parts, one `ServerMain --online` (HTTP 18981 for the rendered runs, 18972 for the drill, 38 s
apart), all stopped (`/admin/shutdown` HTTP 200, exit 0) and destroyed (`DESTROY_OK`); no protected port, owner window or peer process was touched. Two level-30 Warriors (`LiveRunProvision beta`
with `-Dprovision.extra=item.concord_oath_ring_warrior,item.recruits_mace -Dbeta.nofiller=true`) start beside Warden Aelric. Reproduce the rendered part as in 3.15 with scene `accept`
(`-Daccept.chapters=equip,topic,hops,ground,kills,boundary,turnin,reaccept,restart`, driver arguments `... accept <sync> <x> 20 2767 1086`; `LiveRunProvision COLLISION accept-report OUT` prints the committed rows used below); the drill
as `NativeLootFaultDrill RUNTIME_DIR JAVA_EXE SERVER_PORT NEW_OUTPUT_DIR` through `acceptance-cluster.ps1 -Action RunEnv` (see `multiplayer/tools/NATIVE_FAULT_DRILL.md`). The run was assembled from eight
two-window runs (a chapter a run, because early runs found driver problems listed below); the quest part ran as one continuous session with the restart.

**Driven only through the screens' own handlers:** `I`, digits, Left/Right and Esc (inventory window), clicks on the NPC, on creatures and on loot, scripted walking, the topic panel, `R` and Enter after a server stop.
**Scripted around the screen (logged as such):** the cross-region commands (`attack`, `pickUp` through the connection: the screen has no way to aim at another region); the character sheet opened by running the HUD's own
`characterAction` callback, because **the HUD buttons are drawn but not clickable on the online screen** (finding 3); committed state and own-player numbers read from the connection and the screen's `Player`.

| Scene and result | Observed |
|---|---|
| Inventory window, `live6-inventory-window-page1/2.png` | `I` opens a centred window: "[1] Unequip Leather Pants (LEGS) ... [6] Unequip Recruit's Mace (WEAPON) (1) [7] Equip Recruit's Greatsword ..." and on page 2 "Equip Potion of Mana (1)", "Equip Healing Potion x20", "Equip Ring of Remembered Names (Warrior) (1)": identities, equipped slots, quantities and charges. |
| Equip numbers on the own screen | body armour off: Armor Class 3.10 to 1.65 (the sheet shows 1); weapon off: Strength 43 to 40; everything back on: 43 and 3.10; the committed look of the character lost and regained BODY and WEAPON in the same moments; the own figure lost and regained the armour (`live6-own-model-before/body-armour-off.png`, `live6-character-sheet-body-armour-off.png`, Strength green with its bonus). The Strength-100 ring changed nothing (requirement); the mace replaced the greatsword in the weapon slot. |
| Quest, both windows | topic digit 1 accepted in both; Lighthaven door pair with the map switching and the other window seeing leave and arrival; walk to the ground; kills until `quest.renewed_wards.kills` read 12 in each (Alpha reached 6 at 325 s, Bravo reached 12 at 481 s; both kept fighting until both had); walk back and a greeting: status 1 to 2, gold +500, experience +400, `spell.renew_armor` (Alpha 386/5,800,378 to 886/5,800,778; Bravo 400/5,800,322 to 900/5,800,722). The first click on the way (the walk is longer than the helper's patience) was refused "That person cannot talk to you right now." (out of range, as designed). |
| Repeat turn-in and re-accept | greetings after completion changed nothing for either window: two in the same session, one after the restart; topic 0 pressed on the completed quest left status 2, kills 12, gold and experience unchanged. The Warden's greeting after completion reads like the offer ("Keep the dead from stirring ... I'll see you rewarded"); see finding 4. |
| Server restart, both windows | graceful stop and restart; both windows rejoined after 33 s with the same status, kills, gold, experience, inventory (carried list identical) and position (a few pixels from the pre-stop spot). |
| Database | `online_characters`: both status 2, kills 12, knows `spell.renew_armor`; one turn-in-style reward row each (500 gold, 400 experience); no loot id with two receipts; creature kill rewards Alpha 17 and Bravo 14 (the lethal blow's owner, two windows on the same creatures). |
| Region boundary, run A | Bravo walked into the cave mouth while Alpha stayed out: Alpha's snapshot showed 1 player (itself), 0 drawn others; Bravo's cave snapshot 30 creatures. Bravo's `attack` on Alpha's target creature 32: "no target" (the cave has no creature 32); Alpha's `attack` on the cave window's creature 30: "out of reach" (a different creature 30 stands in the outside region); the outside target stayed at 69 and the cave creature at 27 hit points; Bravo's `pickUp` of Alpha's ground drop (a Light Healing Potion) was refused ("The pickup was refused") with the drop still on the ground. In a second run the attack answer was "busy" (a command was in flight), the same effect. |
| Loot claim and corpse recovery under kills (`NativeLootFaultDrill`, 18 rounds) | claim, delay between handing the request to the adapter and the hard kill, reply, database right after the kill: 0, 10, 25, 50, 80, 120, 200 ms: reply lost, not applied (drop on the ground, 0 receipts, potions 3 to 3); 350 ms: reply lost, **applied** (potions 3 to 4, 1 receipt); 600 ms: acknowledged, applied. Recovery: 0 to 120 ms: lost, not applied (corpse 2 stacks, gold unchanged); 200 and 350 ms: lost, **applied** (potions 3 to 5, gold +50, corpse empty and recovered); 600 ms: acknowledged. After a restart and 38 s of lease expiry the same request through a new adapter: accepted for all 14 unapplied rounds, refused "unavailable" for the 4 applied ones; final state for all 18: item(s) exactly once, one receipt per claimed drop with the claimant as `claimed_by`, corpse recovered, no duplicate item id in any inventory, last server stopped gracefully (exit 0). Both outcomes of the lost reply occurred for both request kinds. |

**Findings.**

1. **Fixed here (Claude lane, test `RemoteOwnLookTest`): the own screen kept join-time gear.** The own `Player` is built once at join and only the vitals were refreshed, so after an equip or unequip the character sheet, the equipment bonuses
   (Strength, Armor Class, resistances) and the drawn own model stayed those of the first frame while the committed character and every other window changed. `RemoteOwnLook.refresh` (called with the vitals sync) now sets the worn items from the committed progress
   and rebuilds the look when they differ. Live before and after: Armor Class 3.10 all through; then 1.65 and back.
2. **Not a defect, how the evidence reads:** creature ids are region-local (the cave and the starter region both have a creature 30), so a command naming another region's id is read in the sender's own region (refused or "no target"); the epoch fence of the stale-intent battery (T4C-0251) is what stops delayed intents, not re-run here.
3. **Gap, report (owner or peer lane): the online screen draws the six HUD buttons (Character, Backpack, Spell book, Quest, Map, Options) but its input chain does not route clicks to them** (`touchDown` returns false at the button's own pixels; the remote multiplexer has no HUD adapter). The character sheet, and the stat-point spending of T4C-0336 that lives in it, are therefore not reachable by mouse in the online client. The driver opened the sheet through the HUD's own callback and says so above. Wiring the buttons and the open panel's input into the remote multiplexer is a client task that touches the input chain Codex's rendered-client work uses, so it was not done here.
4. **Gap, small: after completion the Warden's greeting repeats the offer text** ("I'll see you rewarded with something worth knowing") although the quest is done (status stays 2, nothing re-accepts). Single-player parity of that line was not checked.
5. **Unhosted route:** the one authored portal source in the starter region whose route is not enabled (947 at 2964,1038 toward world 1) is in a walkable pocket no hosted tile reaches (flood from the region spawn: 19,541 reachable tiles, the source not among them), so nothing in play can walk into an unhosted destination; the server's silent no-op on a refused source is therefore unobservable. The explicit refusals are join and respawn ones (lobby "Region offline", T4C-0325).
6. **Driver, not game:** a window left inside the cave by an aborted run starts the next run there (the script now takes the exit first); a window that starts hurt dies among the Skeletons (the script now waits for 75 percent health); the respawn-room route oscillation of 3.17 finding 6 recurs (the script now walks south first); one command at a time per connection ("Gameplay command pending" killed a run that sent an attack and a pickup together). A killed run's character is held for 35 s, so a rerun started at once cannot join (it times out in the lobby).
7. After 12 kills the quest counter stopped at 12 while the creature kill rewards went on (17 and 14); the counter caps at the goal.

**Not driven:** a lost response or kill during the quest turn-in itself; a rendered corpse recovery and individual single-stack recovery (T4C-0321/0339 merged separately); the cave's own residents' combat (30 creatures in the cave snapshot, none fought); a package, remote or second-machine run; the stale-epoch battery on this build.

### 3.20 Character creation: two rendered windows, an interrupted-request drill and all seven classes (T4C-0352, milestone M1)

**Evidence class: rendered clients plus native adapter, local only.** Base `origin/main` acb7f2ce plus this branch, protocol 8.
Owner decision 2026-10-30: in-client creation is in Beta scope (SCOPE_PROPOSAL.md, DESIGN_GUIDELINES.md). Two disposable PG16 clusters
(`t4c_acc_179d2129a573` on loopback 57951 for the drill, `t4c_acc_703d46930f8c` on 57952 for the windows), one `ServerMain --online` each
(18971 and 18981, game socket one above), all destroyed or stopped afterwards; no protected port, owner window or peer process.

**Rendered run (new `OnlineLiveRunDriver` scenes `create` and `reenter`; `LiveRunProvision COLLISION empty OUT` makes two accounts with no
characters, `create-report` prints the committed rows).** Driven through the screens' own input processors: `C` (open the form), the name
as key-typed events, Down to the class row, `Enter` (create), `Enter` (play), the walk to Kilhiam, the NPC click, and for the close the
screen's `switchCharacter` callback by reflection (the Options button was not clicked), then `Enter` in the lobby again. Arguments:
`-Dcreate.name=<name> -Dcreate.classIndex=<index in the form's list>` before the class name; tags end in A and B as in 3.15.

| Step | Observed (both windows, a Paladin "Aurelia" and an Archer "Borin") |
|---|---|
| Empty account, `create-1` | the lobby shows no rows and the text that points to Create ("This account has no characters yet. Press Create ..."); the roster read finished, no message. |
| Form, `create-2` (`m1-create-form-seven-classes`) | seven class rows (Warrior, Archer, Paladin, Cleric, Healer, Mage, Battle Mage), gender and alignment buttons, the typed name; the form's state read back matched the typed name, class, male and good. |
| Create, `create-3` | `Enter` created the character; the roster listed it as Ready with its class (one row on each account). |
| In the world | the committed view showed the class kit: Paladin 216 gold, 17 stacks (seven worn: back, body, feet, head, legs, shield, weapon; Light Heal known), Archer 227 gold, 46 stacks (bow, quiver, arrows; no spells); each saw the other window's character. |
| Kilhiam | 47 px away after a collision-aware walk of about 8 s: the first click gave one parchment letter, the repeat click none (`talk-first-greeting`, `talk-repeat-greeting`). |
| Close and reconnect | `switchCharacter` returned to the lobby; `Enter` retried every 2.5 s rejoined after about 40 s (the lobby waits for the old lease); record identical, one letter; the third click gave no second letter (`m1-create-reconnect-kilhiam`). |
| Window closed and reopened (`reenter`) | both processes exited, two new ones showed the character on the roster, entered, and a fourth click on Kilhiam gave no second letter. |
| Database (`create-report`) | one character and one creation receipt per account; Aurelia paladin, level 1, 216 gold, 17 stacks, one letter, equipped slots as above; Borin archer, 227 gold, 47 stacks, one letter. Client logs showed no exception. |

**Interrupted request (`multiplayer/tools/NativeCreateFaultDrill.java`, new).** The same real `OnlineAccountClient` that the lobby form
uses sends the creation request; the server child is killed hard a fixed number of milliseconds later (0, 10, 35, 60, 80, 85 to 120 in
steps of 5, 150, 200, 350, 600, 900, 0 and 20 ms; the class cycles through all seven). Judged against the committed database right after
the kill and again after the restart: at most one character per account and one receipt, a character always carries its complete kit
(class items, torches, potions, skills, spells, karma, gold range, distinct item ids), and after resending the same request id the
record is byte-identical to the pre-retry one. Result of the passing run: 5 rounds had nothing committed (kills at 0 to 35 ms, and the
two last), 15 had committed; one of those (round 16, 350 ms, Archer) had lost its reply to the client yet was already committed, and
the retry returned that character without touching it. A replay changed nothing; the same name under a new request id came back
`name_taken`; the same id with the other alignment came back `creation_conflict`; neither added a character. Two earlier runs failed on
the drill's own pacing only (every client call costs two requests and the loopback peer budget is 40 refilled at one per two seconds, so
the refusal probes were answered 429 `account_rate`); no record changed in them, and in the second of them round 4 (60 ms) also lost its
reply and was already committed. Reproduce as `NativeLootFaultDrill` in NATIVE_FAULT_DRILL.md: `javac ... NativeCreateFaultDrill.java`,
then `acceptance-cluster.ps1 -Action RunEnv -RunId t4c_acc_<id> -Command java ... NativeCreateFaultDrill <runtime> <java> <free-port> <new-output-dir>`.

**Classes and text.** Every one of the seven classes was created in the drill (every kit compared with `CharacterStartingKit.itemCounts` and the
class's equipment slots, spells and skills), and two of them rendered. The form, the server's class resolution and offline creation read the same
seven classes (`CharacterKitParityTest` fails if the lists differ). PARITY.md's "Archer awaits online ranged combat" was stale: online archery exists
since T4C-0273 (a bow with a worn quiver, `OnlineMeleeTest.archerBowAttackUsesRangedReach...`) and a rendered Archer was created and played here.
The empty-roster text ("Press Create to make your first one") is correct now that creation is in scope.

**Not done.** No remote or HTTPS creation, no class other than Paladin and Archer rendered, no fight with a created character (3.18 covers a Warrior, an
Archer, a Paladin, a Healer and a Mage fighting), no click on the Options or window-close buttons, no creation of a fourth character against the
slot limit in a window (`OnlineStoreTest` covers `roster_full`), and no screenshot of the in-game stat sheet.

### 3.21 Other players' walking: root cause and replay buffer (T4C-0358)

Owner report (twice): other players "glide, not walk". T4C-0305 (PR #266) held the walk flag 0.15 s after each position change; the live runs in 3.16 still showed the walk cycle restarting about every 0.35 s.

Root cause, reproduced headlessly (`RemotePlayerWalkParityTest`: 34 Hz ticks at `PLAYER_SPEED`, polls returning the latest tick, 60 fps frames):

1. The walking flag followed how recently a poll moved the target, and `PlayerAnimations.update` zeroes the walk timer whenever the flag is false. The real poll loop is two HTTP calls plus a 75 ms sleep, so gaps of 100 to 350 ms are normal on a loaded client; any gap over the 0.15 s hold dropped the legs to the standing pose and restarted the cycle. Slow-client run (polls every 100 to 300 ms), old code: 156 of 498 walking frames showed the standing pose and the cycle restarted 33 times; a single 0.25 s late poll gave 7 standing frames and a restart.
2. The position chased the newest poll with a 60 ms exponential ease: jump, then slow down, in every poll interval. Even with ideal polls (50 to 110 ms) the largest per-frame step was 2.53 px against a nominal 1.25 px (2x bursts) and the 0.25 s speed was off the server pace by up to 20%. That is the "glide".
3. Facing was recomputed from each poll's step, so a poll spanning a corner gave a diagonal glance.

Fix (client only; wire and protocol unchanged): `RemotePlayerPlayback` records every received position as a sample on the server's own time line (tick / 34) and draws the puppet 0.1 to 0.4 s behind the newest sample (kept just above the recent longest poll gap by playing back at most 12% faster or slower), moving it along straight lines between samples. The walking flag is "the drawn position is moving right now" (50 ms flicker hold), so the legs run exactly while the picture moves; facing is the drawn segment's direction with a 25% dead zone for the minor axis (tile units); a jump over 4 tiles at over 900 px/s (portal, respawn) places the puppet at once. There is no extrapolation, so a stopping player is never drawn past its stop. After: 0 standing frames and 0 restarts in all 20 harness runs (straight, zig-zag, slow client, late poll), per-frame step at most 1.40 px (nominal 1.25), 0.25 s speed within 7 to 16% of the server pace in the worst windows (slow-client seeds, while the lag converges), facing changes only at path turns.

Cost: other players are drawn about 0.2 s behind the server (before about 0.1 s). Not verified: a rendered two-window run (the sandbox refused WSL, so no disposable PostgreSQL cluster was started). The next live run should log the observed puppet's `isMoving`, walk timer and angle per frame.

## 4. Staged plan

Sizes: S under a day, M a few days, L about a week, XL more. Every stage ends with the full
`mvn -q package` test suite green, the single-player golden screenshots unchanged and a
self-reviewed PR.

| Stage | Content | Exit criteria | Tests | Size |
|---|---|---|---|---|
| 0. Harness | `MainGameScreenPreview` (like `OnlineLobbyPreview`): opens the real screen with a fixture save, no network, saves PNGs of map+HUD, inventory, spell book, statistics. Characterization tests around the code to be moved: autosave, equip/unequip, shop buy/sell, melee tick | Golden PNGs committed; tests pass on `main` unchanged | headless unit tests + preview PNGs | S |
| **1. First slice: real screen, movement and melee online** | `GameSession`, `CharacterSource`, `WorldDriver`, `CharacterPersistence`, `Capabilities`; `LocalGameSession` (moved code); `RemoteGameSession` over the unchanged `OnlineConnection`; `ProgressMirror`; remote creatures and other players as puppets; `MonsterInputHandler` attack → `attack` ticket; `PlayerHUD` bars, radar, chat (messages only), zone banner. Unwired buttons say "not available online yet". Online launcher opens the real screen after the lobby | Two clients on the acceptance cluster: both see the same map, HUD and each other; move; kill a Skeleton; HP, XP and gold update on the HUD; a refusal shows as a message; reconnect after server restart. Single-player golden PNGs identical; no write to the save directory during an online run | unit: `ProgressMirror` round trip, puppet applier, `PlayerStateStore` refuses remote players; preview PNGs of the online screen from a recorded snapshot fixture; live: `multiplayer/tools/acceptance-cluster.ps1` disposable WSL PostgreSQL + `NativePursuitProbe`-style two-adapter run | M |
| 2. Predicted movement | `domain/` `MovementRules` used by `PreviewWorld` and `PlayerMovement`; predictor with sequence replay; server snapshot keeps last applied sequence | No visible rubber-banding at loopback; under 150 ms injected delay the own character moves at once and settles within one tile; server speed and collision unchanged (tamper battery still passes) | unit: predictor replay, shared-rule parity test (same inputs, same path in both callers); live: tamper probe speed cases + two clients | M |
| 3. Items | `Inventory`, equipment, `Statistics` (read-only), ground pickup and player drop, item use, corpse recovery, weight; `ItemInstanceIndex`; server `/drop` | Every inventory action works through the single-player GUI; no text panels used | unit: index mapping, ticket pending/refused rendering; preview PNG of the online inventory equals single-player layout; live two-client loot run | M |
| 4. Spells | `SpellBook`, quickbar, macros (client-local store), targeted/self/area casting, buff bar, cooldowns; depends on the spell engine | All spells the server supports cast through the normal UI; casting VFX from events | unit + live; grows with the spell engine | L |
| 5. NPCs and economy | talk, shops, storage, trainers, spell merchants through the real screens; depends on the NPC engine decision | Shop buy/sell and storage over the server with the single-player screens | unit + live | L |
| 6. World flow | portals and map switch via `onMapSwitch`, death and respawn screens, quest journal, kill log, map screen, options, switch character | A full session from lobby to logout without the text client | live long run | M |
| 7. Events and audio | `events` feed for damage numbers, misses, VFX, sounds | Online combat looks and sounds like single-player | preview PNGs + live | M |
| 8. Retire the text client | see section 6 | text panels deleted | build + probes | S |

Stage 1 is deliberately small: it reuses `OnlineConnection` as is, needs no protocol bump and no
server change (other players keep default appearances until appearance parts are added), and
proves the whole seam end to end.

## 5. Risks, open questions and parallel work

**Biggest risk: extracting the session seam out of a 4,964-line screen without changing
single-player.** Simulation, combat and casting are interleaved with rendering (about 1,500
lines of combat/cast code and 21 callbacks). Mitigation: stage 0 golden PNGs and
characterization tests first; move code in small mechanical commits; no rule edits in the same
commit as a move; review each move against the diff of the moved block.

Other risks:

- **Mirror save corruption.** A GUI path that still calls `PlayerStateStore.save` would write an
  online character over the local save. Guarded by the remote-player refusal and a test that runs
  an online fixture session and asserts no file write.
- **Puppet side effects.** `BaseMonster`/`BaseNPC` subclasses run scripts, sounds and loot in
  update and death paths. The `remoteControlled` flag must short-circuit every one; a test sweeps
  all registered monster and NPC classes, applies a death snapshot and asserts no loot, XP, quest
  or sound callback fires.
- **Movement rule divergence** (server substep rule versus `PlayerMovement`). Resolved for the
  step itself by T4C-0277 (3.10.1): the server runs the single-player grid-reservation rule.
  What remains is dynamic blockers, speed state and the client-side prediction.
- **Static singletons** (`GuiManager`, `SystemMessage`, `CollisionManager`) assume one game per
  process. Acceptable: one session per client process.
- **Polling transport** latency on a real network; replaceable inside `OnlineConnection`.
- **Probe contract.** 13 tools use `OnlineConnection`; its API stays, the session wraps it.

Open questions for the owner or reviewers:

1. Chat: single-player chat is local talk text plus GM commands. Online needs a server chat
   route; GM commands must stay offline-only. Confirm.
2. Where online quickbar/macros live: client-local file (proposed) or server-side per character.
3. NPC dialogue: run NPC classes on the server or extract dialogue to data (PARITY step 6).
   Stage 5 depends on it; stages 1–4 do not.
4. Whether the ground-loot owner window tooltip text is wanted.

Parallel work: the spell engine (server and `domain/`, PARITY step 3) and the NPC engine (step 6)
can proceed during stages 0–3 because the session only forwards intents and mirrors results.
Small server tasks that can be done by a peer at any time: `events` in `/state`, appearance parts
in `Actor`, `/drop`, last-applied sequence (already present), shared `MovementRules`. Stage 1 must
not run concurrently with any other edit to `MainGameScreen`, `Inventory`, `PlayerStateStore` or
`MonsterManager`; claim those files in [PEER_WORK.md](PEER_WORK.md) first.

## 6. The current text-panel client

| Files | Fate |
|---|---|
| `OnlineConnection`, `OnlineEndpoint`, `OnlineBusyBackoff`, `OnlineAccountClient`, `OnlineAccountProfile`, `OnlineNativeLibrary`, `model/*` | **Kept.** Transport and wire model under `RemoteGameSession`; probes keep using them |
| `OnlineLobbyModel`, `OnlineLobbyLaunch`, `OnlineCreateForm`, `screens/OnlineLobbyScreen`, `OnlineLobbyLayout`, `OnlineCreateLayout` | **Kept** until character creation moves to the single-player creation screen (PARITY characters item); then the create form is replaced |
| `OnlineCommandRefusals`, `OnlineCreatureMotion` | **Kept and reused** (refusal messages, puppet interpolation) |
| `OnlineClient` | Kept as a fallback behind a `--text-client` flag from stage 1 to stage 6, then reduced to a launcher that opens the lobby and the real screen |
| `OnlineHudText`, `OnlineInventoryRows`, `OnlineAbilityMenu`, `OnlineAbilityChoices`, `OnlineNpcTopics`, `OnlineSpellAllowlist` | **Deleted in stage 8**, with their tests, once the matching single-player screens are wired (allowlist goes when the spell engine serves all spells) |
