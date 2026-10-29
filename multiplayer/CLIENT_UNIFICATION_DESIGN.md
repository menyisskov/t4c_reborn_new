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

### 3.12 Remote session, step R2: the world as puppets (T4C-0288)

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
| Other players: name, class, appearance parts, equipment, stealth, actions | only an opaque `appearance` id and hp | default look; no name; no walk animation (the player movement object has no puppet switch); not drawn by `MainGameScreen` yet, exposed by `RemoteWorldDriver.otherPlayers()` |
| Interpolation buffer (render 100 ms behind) | n/a | puppets ease 60 ms toward the newest snapshot instead |
| Server id to object | held by the driver (`creatureId`, `npcId`) | step R3 sends intents by these ids |

Preview image (a real `MainGameScreen` drawing the puppets of a hand-made snapshot, not a golden):
`multiplayer/design/remote-world-puppets.png`, produced by `RemoteWorldPuppetsPreview`.
Next, R3: `PlayerCommands` over the connection (attack, move, pick up, interact) with tickets, using
the ids above.

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
