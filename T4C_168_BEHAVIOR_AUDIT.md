# T4C 1.68 behavioral audit / Java implementation

Reference sources analyzed:

- Native 1.68 RC14h client: `elestranobaron/Client`, commit `7fa6abf`.
- Associated native server: `elestranobaron/Server`, commit `ea0a8b5`.
- Compared implementation: current Java workspace.

This document lists only the discrepancies already verified in the code. `Absent` means that no corresponding Java entry point or service was found; `Partial` means that a local part exists but the full 1.68 behavior is not reproduced.

## 1. Network architecture and authority

| Domain | 1.68 client/server | Current Java | State |
|---|---|---|---|
| Transport | UDP with header, checksum, fragmentation, packet queues, ACK and timeouts | No network package, packet or socket in `src/main/java` | Absent |
| Movement | Eight directional `RQ_Move*` requests, sent to the server; the movement queue is capped at 25 | Continuous local movement with a per-frame distance budget and local pathing in `PlayerMovement` | Partial / different behavior |
| State authority | The client receives positions, units, HP, XP, gold, mana, weight and effects via packets | State mostly local in `Player` and the Java services | Partial |
| Anti-flood | Movement packets are dropped past 25 queued items | No network equivalent | Absent |
| Synchronization | Existence/missing-unit packets, peripheral units, server position and state | No equivalent mechanism | Absent |

Native evidence: `ComPacketHeader.h`, `Comm.cpp`, `Packet.cpp`, `PacketTypes.h`.

## 2. Verified client timings

The 1.68 client explicitly configures the following values in `Comm.cpp`:

| Action | Max delay | Max ACK |
|---|---:|---:|
| Movement | 0 ms | 0 |
| Attack | 500 ms | 3 |
| Spell | 1000 ms | 5 |
| Skill | 1000 ms | 3 |
| Teleport | 750 ms | 5 |
| Rob | 1000 ms | 3 |
| Arrow hit/miss | 500 ms | 3 |
| Chest | 500 ms | 5 |
| Trade | 500 ms | 5 |

The Java implementation contains local attack/spell/skill cooldowns, but not the ACK, retransmission, rejection and queueing layer that gives them their semantics in the original client. The values therefore cannot be considered equivalent to 1.68 behavior.

## 3. Missing network features

The following packets exist in the original client but no corresponding Java service was found:

- account and connection cycle: `RQ_RegisterAccount`, `RQ_PutPlayerInGame`, `RQ_DeletePlayer`, `RQ_CreatePlayer`, `RQ_ReturnToMenu`, `RQ_AuthenticateServerVersion`;
- player synchronization: `RQ_GetPlayerPos`, `RQ_GetStatus`, `RQ_GetOnlinePlayerList`, `RQ_GetUnitName`, `RQ_GetNearItems`;
- effects and resources: `RQ_HPchanged`, `RQ_XPchanged`, `RQ_ManaChanged`, `RQ_UpdateWeight`, `RQ_CreateEffectStatus`, `RQ_DispellEffectStatus`;
- world: `RQ_WeatherMsg`, `RQ_GetTime`, `RQ_OpenURL`;
- rob and shooting: `RQ_Rob`, `RQ_DispellRob`, `RQ_ArrowHit`, `RQ_ArrowMiss`;
- guilds: `RQ_GuildInvite`, `RQ_GuildKick`, `RQ_GuildLeave`, `RQ_GuildAlterRights`, `RQ_GuildRename`, `RQ_GuildInviteAnswer`.

The Java code contains items, spells or skills that sometimes carry a similar name, but not the protocol or the corresponding multiplayer state transitions.

## 4. Player-to-player trading

The original server implements `TradeMgr2` in `Trade.h`/`Trade.cpp` with:

- trade states `Invalid`, `Inviting`, `Trading`;
- individual states `EditingItems`, `Ready`, `Confirmed`;
- validation of distance, availability of both players and available weight;
- a global operation lock;
- adding/removing items from an intermediate container;
- automatic return of items to the backpack on cancellation or disruption;
- confirmation from both parties before transfer;
- separate client notifications for content, status, start, cancellation and end.

The Java code has neither a `trade` package, nor a `TradeService`, nor a state machine or atomic transfer between two players. Gap: **feature absent**.

## 5. Groups

The original server's `Group.h`/`Group.cpp` provides: invitations, leader, kicking, leaving, member range, automatic sharing, kill-XP distribution, gold distribution, member list updates and member HP.

The Java code has neither a `group` package, nor a group service, nor a member/leader/invitation model. The `SpawnGroup` classes are only about spawn groups and do not reproduce this system. Gap: **feature absent**.

## 6. Guilds

The client contains `GuildUI` and the guild-management requests; the server contains `Guilds` and the associated handling. The Java code has no multiplayer guild service or model. Item names like `GuildChest` do not constitute this feature. Gap: **feature absent**.

## 7. Combat rules already equivalent or nearly so

The following should not be counted as absent:

- the original accuracy formula `rnd(attackSkill) + rnd(attackAgi / 3) - rnd(dodgeSkill) - rnd(targetAgi / 3)` is reproduced in `CombatResolver`;
- the original raw damage is reproduced in `CombatMath` for melee and bow;
- the Java code has attack/dodge profiles, resistance, parry and equipment penalties.

The remaining combat gaps concern server integration, result transmission, network timers and remote-unit client behavior, not this basic accuracy formula.

## 8. Movement

The original client uses discrete T4C directions (8 directions) and sends server requests. The Java code uses local interpolation with step reservation, sliding toward adjacent directions and local pathing in `PlayerMovement`.

Verified differences:

- no server validation or correction of a received position;
- no request queue or 25-move limit;
- the Java code can execute several steps in one frame depending on the movement budget;
- blocking and sliding are decided locally, whereas the original client waits for the server's result;
- other units' movement events do not come from a network stream.

## 9. Inventory, items and containers

The original server represents each entry as an object with an ID, appearance, static reference, quantity and charges. `ItemContainer::Put` refuses to add an item when the weight of the full quantity exceeds the free weight, then stacks compatible items according to their uniqueness. Serialization sends quantity and charges separately (`ItemContainer.cpp`).

The Java code represents `Player`'s inventory as a `List<String>` and keeps charges in a separate map. `InventoryService` reproduces several useful validations (weight, uniqueness, requirements, slot, durability), but there is no item-instance ID or quantity carried by an instance.

Verified differences:

- original quantities are object fields and are transmitted with the ID; the Java code models stacked quantities by repeating keys in a list;
- original items can be targeted by instance ID in requests; the Java code mostly targets a key and sometimes an index;
- the original client serialization distinguishes appearance, ID, static reference, quantity and charges; the Java code has no equivalent packet serializer;
- the original container's atomic operations are locked server-side; Java operations are local and not transactional between two actors.

The Java weight calculation exists, but it does not reproduce the original server container's multi-instance, multi-client semantics.

## 10. Chests and interactive objects

The original client receives chest contents via `RQ_ChestContents`, then uses separate operations `RQ_ChestAddItemFromBackpack`, `RQ_ChestRemoveItemToBackpack`, `RQ_ShowChest` and `RQ_HideChest`. The chest is therefore a persistent container presented to the client, with explicit transfers in both directions.

The Java code has `ChestService`, but its current behavior is different: opening it rolls local loot, drops it on the ground and applies a local respawn; there is no manipulable chest container, no chest/backpack transfer, no open/close packets and no per-player persistent content.

Gap: **the Java code implements local loot chests, not the 1.68 container chests**.

## 11. Buying and selling from NPCs

The original client distinguishes buy and sell lists (`RQ_SendBuyItemList`, `RQ_SendSellItemList`) and item requests. The server has item containers, quantities, weight and instance IDs to validate operations.

The Java code has `ShopScreen` and computes a sell price equal to half the buy price. The transaction is applied directly to the local `Player`. There is no vendor stock, offer ID, server check, quantity reservation or atomic transaction.

Verified gaps:

- a local UI is present, but the protocol and server authority are absent;
- the sell price is hardcoded as `price / 2`, whereas the original server leaves the rule to content/server and can distinguish buy and sell lists;
- no persistent vendor stock or offer quantity carried per instance;
- no network feedback to confirm or refuse the buy/sell.

## 12. Use and equipment

The Java code covers equipping, requirements, charges and durability in `InventoryService`/`ItemUseService`. The remaining gap is architectural: the original client sends `RQ_UseObject`, `RQ_EquipObject` and `RQ_UnequipObject` and waits for the server to update inventory, equipment, weight and charges. The Java code applies the local mutation directly and cannot reproduce concurrent rejections or server state corrections.

## Intermediate summary — spells

## 13. Spells, effects and resources

The Java code has substantial coverage: `SpellCastingService`, `SpellEffectManager`, `SpellRenderer`, persistent effects, cooldowns, mana cost, range, line of sight, projectiles and visual effects. The original client/server has the same broad families via `RQ_CastSpell`, `RQ_SpellEffect`, `RQ_CreateEffectStatus`, `RQ_DispellEffectStatus`, `RQ_ManaChanged` and the native spell/effect handlers.

The functional differences remain verified, however:

- the original client does not decide a spell's outcome locally: it sends the ID and parameters, then receives the effect, mana changes and statuses; the Java code applies cost, exhaustion, cooldown and several effects locally;
- the original client separately synchronizes the visual impact (`RQ_SpellEffect`) and the persistent status (`RQ_CreateEffectStatus`/`RQ_DispellEffectStatus`); the Java code can trigger rendering and mutation in the same local flow;
- effects applied to remote units, mana corrections and expirations received from the server have no Java network equivalent;
- the Java code hardcodes `MAXIMUM_CAST_RANGE_TILES` to 20 in `SpellCastingService`, whereas the original client is not the authority on this limit and receives it from server/spell behavior; this constant can therefore diverge per spell;
- Java projectile callbacks (`Runnable onImpact`) execute the consequence locally after the animation, whereas the original client receives the outcome event from the server.

State: **partial local feature, not multiplayer-equivalent and potentially divergent for timings/results**.

## 14. Resources and regeneration

The original server exposes `HPregen`, `ManaRegen` and `FaithRegen` in `GAME_RULES`, and the client receives HP/mana changes via packets. The Java code has periodic local HP/mana regeneration in `Player.update` and `RegenerationRules`, but:

- no playable `faith` resource was found in the Java model;
- resource changes are not broadcast to other clients;
- a server correction after desync is impossible without network transport;
- the local tick frequency (2 seconds in `RegenerationRules`) is not proof of identity with all of the original server's regeneration paths.

State: **HP/mana partial, faith absent, synchronization absent**.

## Intermediate summary — chat

## 15. Chat and channels

The original client has a server-side chat system with direct messages, shout, page, indirect discussion, channels, channel lists, a channel's user list and joining/leaving a channel. The packets are handled in `Packet.cpp` and the UI emits them from `ChatterUI.cpp`.

The Java code has `GameChat`, but this component is a local console: `addLocalMessage`, `addNpcMessage` and `addSystemMessage` feed the display list directly, and the submit handler is local. No transport, server channel, user list, private message, shout/page or acknowledgment was found.

State: **chat display present, 1.68 communication absent**.

## 16. Death, penalties and resurrection

- The original distinguishes deaths against a monster from deaths against a player, with separate parameters for XP, backpack, equipment, gold lost and gold dropped. The special values 900–999 represent a progressive, level-dependent penalty.
- The original applies XP loss above the current level's threshold. The Java code uses `DeathPenaltyService` on `currentXp`; equivalence therefore depends on the exact semantics of `currentXp` and must be locked down with a per-level test.
- The original explicitly excludes non-droppable items and manipulates instances with quantity/charges. The Java code passes items/charges to `spawnCorpse`, but uses hardcoded default settings and a less rich inventory representation.
- Java resurrection puts the player back at half of max HP, like `GAME_RULES::DeathPenalties`. The battle-mode handling, teams, kill rewards and karma present in the original server are not reproduced by any identifiable team logic in the Java code.

## 17. NPCs, dialogues and quests

- The 1.68 client sends separate requests for indirect conversation, directed conversation and paging; server responses control text, choices and effects. The Java code goes directly from `NPCInputHandler` to `NPCManager`/`NpcScriptEngine`, with no server request or confirmation.
- The Java code has substantial NPC script coverage and a functional `QuestService`: flags, monster objectives within a zone, XP/gold rewards and persistence. This does not guarantee equivalence with the original scripts: the server commands still need to be compared handler by handler.
- Generic Java progression is limited to monster name, world and a circular zone. Original objectives based on specific items, variables, groups, PvP, timers, conditional dialogue or map events require a dedicated handler.
- The Java quest log (`QuestScreen`) is local; it does not replace the state synchronization and server events of the 1.68 client.

## 18. Weather, lighting and map rendering

- The original client contains an explicit weather system (`weather.cpp/.h`) with rain, snow, intensity, an on/off state and particle rendering. No Java rain/snow manager, no synchronized weather state and no handling of the 1.68 weather packet was found.

  The native path's detail is also functional: `RQ_WeatherMsg` (packet 104) receives an effect of `1`, `2` or `3` for rain, snow or fog, and an `OFF`/`ON` value. Rain keeps random drop positions and can display lightning at high intensity; snow keeps its flakes and their sprite variant between draws. `bShowWeatherEffects` can additionally hide these effects client-side, without necessarily cancelling the received state.

  The Java code has neither the decoding of these three effects, nor a persistent intensity/particle state, nor equivalent weather-display filtering. Any weather represented by static map elements therefore cannot reproduce server activation, particle evolution, lightning and the 1.68 client's separation between received state and display option.
- The original client also contains `LightMap`, with lightmap creation/merging and per-zone lighting effects. The Java code has a `DayNightCycle` that applies a global ambience level, but no equivalent was found for a locally merged, tile-by-tile lightmap.

  `LightMap::MakeBaseLightMap` initializes a half-resolution light surface, then `MergeLightMap` merges local sources (including the main torch) before `MakeLightingFX` multiplies each source pixel's RGB channels by the light value. The high-quality path (`bLightHightGraph`) additionally changes the merge and application algorithm. The `LIGHT` value received for the player and other units feeds these sources; this is therefore not just a global tint picked at day change.

  Java's `DayNightCycle` does not reproduce this merge chain, the half-screen resolution, per-pixel RGB processing or the native high/low quality selection. A torch, a light-emitting unit or an object whose `LIGHT` value changes can therefore light a different area, without altering neighboring pixels with the same gradient as the 1.68 client.
- Java collision and pathfinding exist and are rather more explicit than plain rendering: collision maps, line of sight, diagonals and clearance are handled. The remaining gap is the absence of server/network validation, so a local move can be accepted where the 1.68 client would have received a corrected position.

## 19. Current state of the audit

The network, packets, server rules, movement, combat, inventory, items, chests, shops, spells, resources, groups, guilds, chat, death, NPCs, quests, weather, lighting and collision domains have been reviewed. The critical differences are now documented; the only remaining deep-dives are exhaustive content comparisons (each script/animation/item) and scenario tests to quantify the gaps already identified.

## 20. Account, characters and selection

- The 1.68 client provides a server cycle for account registration, character deletion, character creation, version authentication, seraph arrival and the maximum number of characters (`RQ_RegisterAccount`, `RQ_DeletePlayer`, `RQ_CreatePlayer`, `RQ_AuthenticateServerVersion`, `RQ_SeraphArrival`, `RQ_MaxCharactersPerAccountInfo`).
- The Java code uses `LocalCharacterStore`, `characters.json` and one JSON file per character. Creation, deletion, activation, name, sex, stats, starting inventory, starting gold and starting location are all done locally, with no account, server ID, remote validation, name reservation or network error feedback.
- The Java code locally caps the roster at three characters. This value is not negotiated with a server the way the original client's capability packet provides for.
- Local persistence is atomic for the JSON files, but it does not provide the guarantees of shared account storage: two clients can create the same name, overwrite a state, or keep a deleted character in another local copy.

## 21. Remote units and visible multiplayer

- The original client protocol has unit updates (`RQ_UnitUpdate`), peripheral units, group updates and member states. The client must therefore create, refresh and remove remote characters/monsters according to server notifications.
- In the Java code, the identified character classes concern the local player (`Player`, `PlayerAnimations`, `PlayerHUD`); the monster/NPC managers handle local entities coming from spawns, not a registry of remote players fed by packets.
- The following observable functions are therefore missing: another player appearing/disappearing, interpolation or correction of their position, a received remote appearance, remote HP/mana/effects, remote attack animation and removal on disconnect. Java groups are absent, which also prevents synchronized display of members.

## 22. Teleportation, fast mode and seraph

- The 1.68 client treats teleportation as a dedicated request with a delay/ACK (`RQ_TeleportPlayer`, 750 ms, 5 ACK), and also has a fast-mode state (`RQ_PlayerFastMode`). The Java code teleports the player directly via items, scripts or local movement; no server response, destination refusal, cost, cooldown or correction is available.
- The Java code contains teleport registries and seraph animations, but these are local data/effects. They are not the equivalent of the `RQ_Seraph`, `RQ_SeraphArrival` server cycle and rebirth validations.
- A local teleport can therefore bypass collision, safe zone, combat, weight, an active spell cast, or map restrictions that were validated by the original server.

## 23. Sounds, music and animations

- The native client loads sounds via an ID database (`DatabaseLoadVSB`, `GameSounds`, `SoundFX`) and has dedicated sounds for UI controls, items, combat, zones, dungeons and bosses. The Java code has a `SoundManager` based on file names and covers several UI, spell and monster sounds, but does not reproduce the native client's ID-based routing.
- The Java code plays NPC attack sounds when the pose starts and hit/death sounds in local callbacks. The 1.68 client receives the unit's state and orchestrates rendering/sound client-side; without remote units, the Java code cannot play other players' action sounds.
- `PlayerAnimations` sets a frame's duration to `0.05f` and ends an attack by holding the last pose. The native client delegates sequences to its sprite/animation systems and resource data. The exact identity of durations, frame counts, final pose and sound triggering is therefore not proven equivalent; it must be validated animation by animation.
- Java music is selected by local rectangular zones (`musicZones`). The native client additionally has region/dungeon/boss music-change logic (`GameMusic.cpp`); there is no proof that all of these priorities and transitions are preserved in the Java data.

## 24. Actual coverage of NPC script commands

Reading `NpcScriptEngine`'s execution path reveals gaps more precise than the mere presence of NPC classes:

- `SendBuyItemList`, `SendTeachSkillList`, `SendTrainSkillList`, `SendTeachFormuleList` and `CreateFormuleList` are recognized by empty branches. They therefore produce no packet and no list opening by themselves; the Java UI has to rebuild the offer from the local result.
- `HealPlayer`/`Heal` do not reproduce a server-side heal: the engine sets a `heal` flag, then the consequence depends on the local calling code. There is no distance validation, cost, remote target or network correction.
- `CastSpellTarget` and `CastSpellSelf` collect IDs and execute them locally. The original server could apply resistances, targeting, zone, persistent effects, cooldowns and broadcasts to several units before responding to the client.
- `SUMMON`/`SUMMON2` create local spawn requests. They do not reproduce server unit reservation, peripheral visibility, population limits, or broadcasting to other clients.
- The Java engine, by construction, ignores any command not covered by its parsing branches; the presence of an imported script therefore does not prove that every C++ macro is functional. Scripts involving server variables, trade, formulas, groups, guilds, timers or map effects are especially prone to this silent loss.

## 25. Skills, statistics and progression

- The original client distinguishes skill list, training list, buy list, using a skill, status, skill/stat points, XP, level, gold, HP, mana and weight (`RQ_GetSkillList`, `RQ_GetTrainSkillList`, `RQ_GetBuySkillList`, `RQ_UseSkill`, `RQ_GetStatus`, `RQ_SkillStatPoints`, `RQ_XPchanged`, `RQ_LevelUp`, `RQ_GoldChange`, `RQ_HPchanged`, `RQ_ManaChanged`, `RQ_UpdateWeight`).
- The Java code has screens and local mutations: `Statistics` modifies stat points, `TrainScreen` spends gold directly and raises a skill, and `PlayerProgression` directly grants XP, level, points, HP and mana. There is no server response, no remote refusal, no training transaction and no broadcast to other units.
- Java progression grants five stat points and fifteen skill points per level-up. These numbers and the random HP/mana gains are Java rules; they have not yet been shown to be identical to the 1.68 server's rules/configuration.
- The Java UI only exposes the combat skills `attack`, `dodge` and `archery` in the stats table, whereas the original protocol provides for a dynamic skill list. Scripted, active, passive or server-list-dependent skills can therefore be missing from the UI and the use cycle.
- The original client receives resource and XP changes as separate events. The Java code mutates state and then the display locally; a rollback, a server cap, a concurrent spend or a lost packet has no equivalent.

## 26. Client options and configurable graphics effects

- The native options include, in addition to music/sound volume and brightness: page sound, lighting quality, effects quality, animated water, dithering, UI alpha, seraph animation, status display, XP bar text, gold display and 32 FPS mode (`SaveGame.h`).
- The Java preferences cover volume, brightness, fullscreen, VSync, HUD values, GUI transparency, seraph animation, XP text, font quality and logging. The native options for animated water, high-level lighting, graphics effects, dithering, page sound, gold display and the 32 FPS rate are not represented as equivalent Java settings.
- This is not just a UI difference: these flags change the rendering, animation and some audio feedback of the 1.68 client. Their absence forces a single Java behavior regardless of the player's historical configuration.

## 27. Macros, shortcuts, cursors and targeting

- The native client has `MacroHandler`: binding a `VKey` combination to a callback, replacing/removing a macro, global enable/disable and locking macros during certain states. The Java code has no comparable user macro manager; its shortcuts are hardcoded directly in the screens and handlers.
- The native client notably distinguishes attack cursor, ranged-attack cursor and spell cursor (`CombatCursor.h`), with target selection and behavior depending on the attack mode. The Java code has local cursors and targets, but selection can only target its locally loaded monsters/NPCs and receives no remote target/unit.
- The original client also keeps ignore-list mechanisms and accelerator keys in its macro/localization layer. No Java storage of a player ignore-list or message filtering by name was found.
- Java cancellations (`clearCurrentAttackTarget`, spell cancellation, closing a screen) interrupt local actions. They do not reproduce the 1.68 client/server's cancellation requests, intermediate statuses and ACKs.

## 28. Transport integrity and security

- The native client computes CRC16 in `CommCenter`, encrypts/decrypts packets (`TFCCrypt::EncryptS/DecryptS`, `EncryptC/DecryptC`) and rejects a packet when the check or decryption fails. Secured packets also use dedicated delays/ACKs.
- The Java code contains no network transport, CRC, encryption or packet authentication. All local mutations (gold, XP, items, teleportation, skills and spells) are therefore callable without the 1.68 client's integrity layer.
- The native client additionally has server version authentication (`RQ_AuthenticateServerVersion`). The Java code does not perform a version negotiation before launching the local world; a data incompatibility therefore cannot be refused at the same point.
- This gap is both functional and security-related: it affects message validation, detection of tampered/replayed packets, and cross-version compatibility, not just the technical network implementation.

## 29. Worlds, transitions and map changes

- Both implementations declare four 3072×3072 worlds: main world, dungeon, cavern and underworld. The basic file correspondence is therefore present on both the Java side (`MapDefinition`) and the native client side (`V2_WorldMap.Map`, `V2_DungeonMap.Map`, `V2_CavernMap.Map`, `V2_Underworld.Map`).
- The native client, on a teleport/world change, locks the world, validates coordinates within `[0,3072]`, loads the zone map, changes the position, then plays a fade transition before unlocking. The Java code changes the map/position directly in `MainGameScreen` and its teleport registries; no equivalent network locking cycle is present.
- The native client applies world- and position-dependent ambience processing: `NTime.cpp` notably forces specific tints for caverns, dungeons and certain underworld zones. The Java code uses `DayNightCycle` with a global ambience, with no confirmed equivalent for these per-world/per-zone tints.
- Java rendering preloads its four maps and keeps drops per world, but the native dynamic states (received items, peripheral units, weather, lightmap, fade and music triggered during the transition) are not carried over as a single atomic world-change state.

## 30. Character fields lost or simplified on save

The native `TFCPlayer` structure contains fields that `PlayerStateDto` does not have as first-class data:

- charisma and luck (`Cha`, `Lck`);
- faith and max faith (`Faith`, `MaxFaith`);
- armor class and weight/max weight;
- structured elemental powers and resistances for earth/fire/water/air/darkness/light;
- true stats and separate bonuses (`bStr`, `bEnd`, etc.);
- death counters, kills, kill streaks and PvP points;
- server state flags like `CanRunScripts` and `CanSlayUsers`.

The Java code encodes some bonuses/resistances in quest flags and has equipment calculations, but this indirect storage is not equivalent to serialized native fields. It guarantees neither full value retention, nor the base-value/effective-value distinction, nor compatibility with a 1.68 character.

Saved Java buffs mainly contain a name and a duration. The native client/server synchronizes statuses along with their effect data, icon, power and target; after a Java reload, a complex effect can therefore be displayed or recalculated differently, and effects on remote units are absent regardless.

## 31. Specialized packets and rare functions

- The native protocol has distinct events for robbery (`RQ_Rob`, `RQ_DispelRob`), arrow hit/miss (`RQ_ArrowHit`, `RQ_ArrowMiss`), weather (`RQ_WeatherMsg`), opening a URL (`RQ_OpenURL`), server information (`RQ_InfoMessage`) and GM-flag updates (`RQ_GodFlagUpdate`).
- The Java code has a bow projectile and ranged combat rules, but no separate network hit/miss result for an arrow. The local projectile therefore does not prove that arrow consumption, damage timing, the miss message and the target's state follow the 1.68 client.
- No Java robbery service with synchronization, rob cancellation, victim notification or state restoration was found. The `rob` icon in the stats screen is not an implementation of the native mechanism.
- Weather features, a server-received URL, information messages and GM flags are absent as dedicated Java handling. The seraph elements present in the Java code cover a local animation/aura, but not the full set of specialized server events.

## 32. Equipment slots and appearance

- The native client stores 36 item appearances (`Object[36]`) and defines 16 historical slots: body, feet, gloves, helmet, legs, rings, bracelet, necklace, right/left weapons, both hands, belt and sleeves. The Java code exposes 23 `BodyPart` values, including several synthetic slots (`BACK`, `HAIR`, `HAT`, `MASK`, `CAPE`, `ROBELEGS`, `BOOT`, `WEAPON2`, `SHIELD`).
- This Java model is more detailed for some equipment, but it is not a one-to-one mapping of the native array. The weapon/shield, sleeves/gloves, helmet/hair and robe/legs replacement rules can therefore produce a different appearance even when the logical item is identical.
- Java applies concealment rules and preserves palette suffixes on an override. The native client resolves appearances via its item/palette groups and its `Apparence.h` tables; equivalence for each group and each sex combination is not demonstrated by presence tests alone.
- The native code also handles appearance, instance ID and base reference separately in equipment objects. The Java code mainly stores an item key and then derives the sprite; two visually identical instances with different data cannot be distinguished by local rendering.

## 33. Map objects, animations and depth

- The native client maintains a list of visual objects with ID, type/appearance, position, direction, brightness, HP, attached item and name/guild text. It has separate paths for normal objects, shadows, animation, animated overlay and 3D objects (`VisualObjectList.h`).
- The Java code does have `ObjectRenderer`, frames, open/close sounds, `behind` flags and depth sorting. This coverage is therefore partial but real, unlike the network domains.
- The remaining verified gap is dynamic lighting: the native code combines several lightmaps (`lmPlayerLight`, `lmOtherPlayerLight`, torches/lamps) and updates object lighting; no equivalent Java system for merging a per-player/per-torch lightmap was found.
- The native code has animated water tiles and smoothing of water seams (`AnimWater01`, `WaterSmooth`, `DrawWaterLevel`). The Java code has terrain and tile rendering, but no explicit animated-water cycle or Java setting corresponding to `bAnimatedWater`.
- Java objects use logical positions/mappings and a local animation state. They do not receive object changes, movements, removals or overlays from a server object list, so multi-client interactive states remain different.

## 34. Camera, zoom and screen-to-world conversion

The base dimensions are aligned: the original client initializes 32×16-pixel tiles, a 256×256 virtual size, 3072×3072 worlds and four worlds. This is not enough to establish camera equivalence.

The original client has an explicit zoom state. `Global::SetZoomStatus` caps it between 0 and 14, and the `bEnableZoom` option allows disabling it. Each level changes the effective screen dimensions by 5%. Mouse-to-world conversion compensates for this zoom, and dialogue names/text also receive zoom-related offsets.

The Java client has neither a corresponding zoom state, nor a zoom preference, nor an equivalent of `SetZoomStatus`. It uses a libGDX orthographic camera, a `ScreenViewport`, an `unproject` conversion and pixel alignment. Behaviors therefore diverge whenever the 1.68 client is zoomed or the resolution changes:

- number of visible tiles and the culling limit;
- the world cell selected by a click;
- targeting near screen edges;
- position of names, labels and dialogues;
- the relationship between HUD coordinates and world coordinates.

The native `ScreenPosToWL` function also uses asymmetric offsets around the screen center, with base values of 32 pixels horizontally and 16 vertically. Java delegates to `camera.unproject`, which follows a different projection algorithm. Even at zoom 0, equivalence must be verified with a pixel-by-pixel test for each supported resolution; identical tile dimensions are not enough.

## 35. Mouse/keyboard input model and UI states

The native client does not just handle instantaneous key presses. `MouseAction.cpp` explicitly turns events into `DRAG`, `DROP`, `CLICK`, `DOUBLE_CLICK` and `DOWN`, then dispatches them to controls via distinct messages. This distinction is used by the inventory, chest, trade, macro and selection UIs.

The Java code has GUI screens and libGDX controls, but `GameInputHandler` only directly handles keyboard movement, a few global shortcuts and debug/map toggles. No central native equivalent of the `DOWN → DRAG → DROP` sequence, of double-activation, or of mouse capture during a drag is present in this handler. Java screens can therefore respond to a local click while diverging on the following cases: moving an item without releasing it over a valid area, double-click use, contextual right-click, cancelling a drag outside the window, and priority between the GUI and the world.

The native keyboard also maintains a 256-key DirectInput state and produces release events for letters, digits, Enter, Escape, Backspace, space, plus and minus. The Java code mostly polls `isKeyPressed`/`isKeyJustPressed` and reserves text entry for widgets. Repetition, the timing of a trigger and a key being consumed by a screen are therefore not guaranteed to be identical, in particular for macros and held actions.

Finally, the native client restores zoom after certain mouse operations (`GetlastScrollStatus` then `SetZoomStatus`). The Java code has no such state restoration, which widens the gap between navigation, UI interaction and world coordinates.

## 36. Movement, pathing and collision authority

The native client represents movement with eight discrete requests (`RQ_MoveNorth` through `RQ_MoveNorthWest`). After a click, `MovePl` calls `Player::ScreenPosToWL`, builds a sequence of moves with `pfSetPosition`/`pfGetNextMovement`, then sends the next move. These requests have their ACK configured at zero delay: the client therefore does not simulate a server validation equivalent to a continuous local animation; it advances according to the responses/states it receives.

The Java code follows a different architecture: `PlayerMovement.move` consumes a per-frame pixel budget (`PLAYER_SPEED × delta`), locally reserves a tile, interpolates position and applies collision directly. When blocked, it automatically tries adjacent directions to slide around the obstacle.

The verified functional differences are therefore:

- the Java code can move the character to a position between two tiles, whereas the native protocol reasons in transmitted directional steps;
- the Java code decides locally whether a tile and the player's footprint are passable, whereas the native client transmits intent and receives the authoritative position;
- the Java code can pick a sideways fallback direction not requested by the player because of `tryReserveAdjacentDirection`; this automatic sliding is not demonstrated in the native `MovePl`;
- effective Java speed depends on the frame delta and local multipliers, whereas the historical pace depends on request cadence, responses and server rules;
- a collision disagreement, a forbidden move or a position correction has no network equivalent in local Java.

Even though both clients use a 32×16 grid and eight directions, their movement decisions are therefore not interchangeable. A full comparison must test at least: diagonal vs. blocked corner, map edge, an obstacle on the character's upper footprint, a teleportable destination, holding a key, holding a click, and position correction.

## 37. Targeting, range and line of sight

The native client keeps one target per unit ID (`TargetID`, `FollowID`, `FreezeID`) drawn from the `GridID` object grid. An attack click can select, follow or lock a unit; double-click enables locking if the `bLockTarget` option is on. The UIs then send the target's ID to the server, after checking that the object exists in the received unit list.

The Java code mainly selects local instances of `BaseMonster`/`BaseNPC` and can pick the nearest monster. It has no comparable network ID table for players and peripheral units. Java locking is therefore a local reference state, not the same contract as a synchronized `TargetID`/`FreezeID`.

Validation is also placed at a different point:

- the native client displays the cursor and transmits the target/position; the actual validation of range, line of sight, alive state, right to attack and position correction belongs to the 1.68 server;
- the Java code locally refuses certain actions via `hasLineOfSight`, distance and collisions before applying the result; no server response can confirm or contradict this choice.

For positional spells, Java computes distance with `distance / max(GRID_W, GRID_H)` and samples the line in collision steps. This calculation does not automatically reproduce any native range expressed in tiles, nor the server rules for a target occupying several tiles. A difference is especially likely on diagonals, wall corners, targets near the range limit and area spells: Java can exclude a target before impact where the native client would have sent the request, or the reverse.

The native code also distinguishes normal attack, ranged attack, magic attack and follow via separate cursors/states. The Java code shares more of the same selection and local-application pipeline; the attack → spell → follow transitions, right-click cancellation and target locking therefore do not have the same state machine.

## 38. Temporary states, buffs and dispelling

The native protocol has two dedicated events: `RQ_CreateEffectStatus` (83) and `RQ_DispellEffectStatus` (84). Creation carries a numeric effect ID, remaining time, total time, an icon ID and a description. Dispelling removes the effect by its numeric ID, independent of the spell's name.

The Java code applies buffs directly after a spell is cast locally. `Player.ActiveBuff` indexes them mainly by spell name and keeps a description, icon, expiration and a list of Java effects. There is no native effect ID received from the server, nor a separate creation/update/dispel packet. The consequences are:

- two effects coming from the same name, or two instances of the same spell, cannot be distinguished the way the native protocol distinguishes them;
- a Java renewal replaces/recomputes the local buff, whereas the native client can receive a new duration and a new effect ID;
- the displayed duration and the effective Java duration start at the moment of the local cast, not at the reception of the server event;
- dispelling by name in Java does not guarantee removing the right effect when several effects are active;
- persistent effects on remote units, their icon and their expiration are not synchronized.

The Java code adds local HP/mana regeneration ticks and local rules for invisibility, detection, stun and periodic effects. The 1.68 client, for its part, displays the transmitted state and receives effect changes from the server; it cannot locally infer that a tick was actually accepted. A disagreement in duration, renewal, dispelling or tick therefore produces a different Java state even if the spell's animation is identical.

## 39. Music, ambience and interaction sounds

The native client does not pick music solely from an external zone file. `GameMusic::LoadNewSound` starts from the current world, level and outdoor state, then applies a long series of geometric regions (rectangles and diagonal zones) to select boss, outdoor, forest, dungeon, cavern, sadness, silence or noise music. Priorities are determined by the order of the native tests, and some changes are triggered on a world change.

The Java code picks the last `MusicZoneEntry` containing the player's tile, reloads the JSON/binary zones, then calls `SoundManager.playAmbient`. It has not demonstrated reproducing every region coded in `GameMusic.cpp`, nor the level-dependent `OutSide` variable, nor the exact priorities between overlapping regions. A map with no Java zone file can therefore play no music, or different music than the native client.

The two systems also differ in playback handling:

- the native code keeps a current track, replaces it via a manager protected by a critical section, and can use streaming or CD music (`bUseCD`); it stops/releases the track during a transition;
- Java directly stops and destroys `ambientMusic`, recreates a libGDX `Music` and loops it; it does not handle CD mode, transition fade, a playback queue or native priority;
- the native code has sounds tied to sprites/objects and to the UI (`GameSounds`, `ItemDragSounds`) with resource IDs; Java mostly resolves a file name and then plays a `Sound` or a `Music` depending on what is available;
- the native code separates music, sound and volume controls across several managers/threads, whereas Java uses local resolution and silently ignores loading errors.

These gaps change the audible result: seamless or non-seamless transitions, which track plays in a boss/town zone, a UI sound repeating, volume after changing options, and behavior when a file is missing.

## 40. Localization and text catalogue

The native client has three distinct catalogues (`LocalString`, `GUILocalString` and `GUIDELocalString`) loaded from the selected language. The language is an explicit choice among English, French, Italian, Portuguese, Spanish, German and Korean; it is stored in `Player.szLanguage` and can be hot-reloaded when the launcher signals a change. Texts are addressed by a stable numeric index (`g_LocalString[xxx]`), and native formatting uses `sprintf`/`FORMAT` with the message's parameters.

The Java code uses a single JSON catalogue, `assets/i18n/lang.json`, text keys and a fallback to the key or the supplied value. `I18n.reload` fails if the file is missing or empty, and no Java preference corresponding to the seven native languages, nor any reload triggered by a server-provided language, was found. (This is an intentional simplification in this project — see `AGENT.md`'s "Language: English only" section — not a gap to close: the game only needs to ship one language, English.)

This difference affects visible behavior:

- the native client can reload game, UI and help texts separately after receiving the language; Java only reloads a single shared JSON map;
- imported scripts and messages that reference a native index (`INTL(id, text)`) do not automatically correspond to a Java key;
- parameter formatting and length/encoding rules are not the same;
- the Java fallback sometimes displays a technical key (`ui.xxx`, `message.xxx`) where the native client has indexed text;
- server-sent texts remain already-formed strings in Java, whereas the native client can display them in the matching catalogue and build some messages locally.

The Java code therefore has a real translation infrastructure, but it guarantees neither coverage of the 1.68 catalogues, nor identity of the available languages (nor should it, per this project's English-only direction), nor compatibility with NPC scripts' `INTL` references.

## 41. Character creation, reroll and selection

The native client treats creation, deletion and reroll as server operations: `RQ_CreatePlayer` (25), `RQ_DeletePlayer` (15) and `RQ_Reroll` (31) each have an ACK queue and a maximum retry count. The allowed number of characters is received via `RQ_MaxCharacters` (103), so it is not a purely client-side constant.

The Java code performs these operations in `CharacterSelectionScreen` and `LocalCharacterStore`, with a hardcoded maximum of `MAX_CHARACTERS = 3`, local JSON files and a local UUID per character. Creation writes the state and roster immediately; deletion erases the local file; reroll modifies the local session's values. There is no server refusal, account lock, concurrent conflict, ACK, received quota or remote rollback.

The Java code additionally adds affinity-questionnaire-based creation, a sex, and a table of starting values (starting gold, items, potions, torches and skills). The native client displays the values received from the server after `RQ_Reroll`/creation; these Java starting rules are not proven identical to the 1.68 rules and can create a character with different inventory, gold or stats.

Name and roster constraints also diverge: Java checks uniqueness locally in its JSON file and normalizes the name before writing, whereas the native client sends the name to the server and displays the returned error. A second Java instance, an account change or an external deletion cannot be arbitrated by the local roster.

## 42. World map, minimap and displayed zone change

The native client loads `Zone_Map.dat` for the current world, keeps a window around the player's position and computes the current zone from that 3072×3072 map's cell value. `ValidMapZonePosition` triggers a displayed zone change when the cell changes; `GetDisplayZoneName` then retrieves the name from the `m_ZoneInfo[world][zone]` table. A world change can also replace the map bitmap and reset the zone.

The Java code has an actual world map (`GuiWorldMap`/`OriginalRtMap`) and a player marker, but the displayed zone update is not tied to an equivalent native `Zone_Map.dat` table. `MainGameScreen` notably initializes the display with `zone.lighthaven`; no Java routine comparable to `ValidMapZonePosition`/`ForceDisplayZone` was found to automatically recompute native zone names for each cell.

The functional consequences are:

- the map background can be correct while the zone name, its temporary appearance and its triggering differ;
- the native map distinguishes world map, dungeon/cavern map and a view window loaded around the player; Java rebuilds the view via `OriginalRtMap` and keeps a marker positioned in a fixed frame;
- zones not declared in the Java table cannot produce the same name or the same zone-entry event;
- the native `ForceDisplayZone` can force a zone after a teleport or transition, with no confirmed equivalent in the Java cycle.

## 43. Session liveness, keep-alive and AFK

The native client maintains a network-layer liveness state (`PacketCenter::KeepAlive`, `isAlive`, `isHalf`, `SetAlive`, `LongLive`). It distinguishes a normal network state from a degraded state after several seconds without activity, and considers the client lost after a 120-second delay. `CommCenter` in parallel has a backlog timeout, pending packets and ACK retransmissions.

The native configuration also keeps an AFK status and an AFK message (`dwAfkStatus`, `strAfkMessage`) alongside the account address and session parameters. The client can therefore display or transmit an away state distinct from simply not moving.

The Java code has no network transport or session layer exposing a keep-alive, a half-alive state, retransmission, or a timeout-based disconnect. There is also no persistent AFK status with an associated message in `GamePreferences`, `PlayerStateDto` or `GameChat`. An open Java window, a rendering pause or a motionless player therefore remain local states with no session consequence comparable to the 1.68 client.

This gap shows up on a network loss, a silent server, resuming after latency, closing the window, or prolonged inactivity: the native client can display a degraded state and then close the session, whereas Java keeps the world and local mutations going.

## 44. Animation cadence and sprite timing

The native client centralizes animation in its V2 sprites and its display rhythm. It has a `b32FPS` option in `SaveGame`, global water-animation counters (`GetAnimWaterFrame`/`StepAnimWaterFrame`) and VSB/VSF resources describing frames and their metadata. Cadence changes and animation states are therefore tied to the client's timer and to the loaded resources.

The Java code hardcodes durations: `PlayerAnimations.FRAME_DURATION = 0.05f` and `NPCAnimations.FRAME_DURATION = 0.10f`, then advances frames with libGDX's `delta`. It has no corresponding 32 FPS setting, nor a cadence extracted from native animation metadata. Player and NPC animations can therefore be twice as fast/slow depending on the resource's historical cadence, and their speed varies with the frame delta.

States also differ: Java resets the walk animation to frame 0 once there is no more movement, keeps a final attack pose, and enables static NPC animations via `standingIdle`. The native code separates idle, walk, attack, ranged and effect sprites via its V2 objects/animations. Without a frame-by-frame correspondence table, the attack's appearance, the final pose's duration, the flip, and the moment the sound triggers are not guaranteed to be identical.

## 45. Resource formats, palettes and missing-resource behavior

The native client loads resources from indexed, packed databases (`T4CGameFile.vsb`, `VSBDataBase`, `CV2Sprite`/VSF), with references by ID, palette and chunk. `GameIcons` keeps item and sound maps and always returns a fallback sprite/sound (`???` or an error resource) when an ID is not found. Rendering therefore generally stays in the game loop even if a resource is missing.

The Java code mainly loads PNG textures and metadata via `SpriteLoader`, with `TextureRegion` caches, texture generations and composed chunk/tile caches. Palettes and variants are resolved through Java mapping files, and missing resources go through missing-tile renderers, null regions or exceptions depending on the screen.

The functional differences are:

- the same native ID does not imply the same result if the Java palette mapping is absent or incomplete;
- the native code can supply a non-null error sprite, whereas a Java screen can hide the element, draw a placeholder tile or interrupt loading;
- the native code frees/reloads its packed resources by index and reference; Java invalidates its textures/caches and rebuilds PNG files;
- resource errors do not have the same effect on the object list, animation frames, offsets and associated sounds;
- a palette or offset change is therefore not necessarily visible at the same moment and does not cause the same cache invalidation.

Both clients have a caching mechanism, but their resolution and fallback contracts are different: the presence of a Java PNG file does not prove that the ID, palette, frame and fallback match the 1.68 V2 resource.

## 46. Focus, pause, resizing and graphics restoration

The native client explicitly handles window transitions (`WM_ACTIVATE`, `WM_KILLFOCUS`, `WM_SETFOCUS`). On losing focus, it releases DirectInput keyboard and mouse; on returning, it reacquires the devices, restores logical focus and restores DirectDraw surfaces before resuming display. It also has the `bLockResize` option, which blocks window resizing during play.

The Java code does implement `resize()` and `resume()` via libGDX: viewports/cameras are recomputed and the HUD calls `recoverAfterDisplayChange()`. However, `pause()` is empty in `MainGameScreen`, and no game logic equivalent to acquiring/releasing devices, resize locking, or explicit restoration of native surfaces/palettes was found.

The consequences diverge on an Alt-Tab, a minimize, a lost graphics context or a resize during an action: the Java backend can restore the OpenGL context, but the game does not reproduce the 1.68 client's focus/pause/resume states or device transitions. Movement, held inputs, audio and timers can therefore continue or resume at a different point.

## 47. Screenshot capture built into macros

The native client exposes a screenshot action (`MacroHandler::TakeScreenShot`) and calls `VideoCapture::TakeDesktopSnapshot`, including from the network/game loop. Capture is therefore a user feature built into the client and its macros, with a request state consumed by the render loop.

No equivalent screenshot capability or corresponding macro action was found in the Java code. The available keys and actions therefore cannot reproduce this 1.68 behavior; an external system capture is not the same built-in feature nor the same moment of image capture.

## 48. Social panels and player-to-player operations

The native UI contains dedicated, separate panels for `GroupPlayUI`, `GuildUI`, `TradeUI` (with `BuyUI`/`SellUI`), `RobUI`, `ChatterUI`, `MacroUI`, `EffectStatusUI` and `ChestUI`. These panels are fed by network responses and maintain waiting, selection and confirmation states specific to each operation.

The Java code has inventory, shop, chest and spell screens, but no equivalent screen or service for group, guild, player-to-player trade, robbery, user macros or full network-effect management. The remaining occurrences of `guild`, `group`, `trade` or `rob` mainly correspond to NPC data, skills or icons; they do not constitute these interactive workflows.

Even where a skill or an appearance datum exists on the Java side, the full functional cycle is therefore missing: invitation/request, remote response, locking selections, validation from both parties, cancellation, participant updates and synchronized closing. The Java UI cannot reproduce the 1.68 client's intermediate states.

## 49. Version check and update before connecting

The native client has a distinct launch flow that reads the version, sends `RQ_AuthenticateServerVersion` (packet 99), handles the authentication responses and calls `WebPatchUpdate` with the IP, account, password and client version. Startup can therefore be interrupted or redirected to an update before world access.

The Java code has no comparable launcher/patcher or network version negotiation. `CharacterSelectionScreen` and `LocalCharacterStore` work on local characters and do not check a server version before entering the game. An old, modified or incompatible Java client can therefore reach the game screens without the preliminary check the native 1.68 flow imposes.

## 50. Persisted client profile data

The native `CSaveGame` does not just save options: per account and character, it keeps the UI inventory, three macro families (items, spells, skills), chat channels with password/color/activation, the ignore list, remembered chests and a 10-layer RTMap of 192×192 tiles. This data is reloaded with the profile before play.

The Java `GamePreferences` only persists a few volumes, graphics options and logs. `LocalCharacterStore`/`PlayerStateStore` persist the character's local state, but no full equivalent model for per-type macros, private channels, ignore list, remembered chests or RTMap tiles was found. After a character change, a restart or an account change, shortcuts, channels, social filters and map reveals therefore do not follow the same rules as in the 1.68 client.

## 51. Global shortcuts and keyboard macros

The native client explicitly installs configurable Ctrl shortcuts for inventory (`I`), stats/character (`S`), attack mode (`C`), chat (`L`), group (`G`), spells (`P`), macros (`M`), options (`O`), map (`W`), trade (`T`), screenshot (`H`), chat size (`A`) and item identification (`V`). Keys can be reassigned in the profile and are executed by `Custom.gMacro`/`MacroUI`.

The Java code hardcodes a different subset: Ctrl+T opens stats, Ctrl+P spells, Ctrl+I inventory, Ctrl+Q quests and Ctrl+W the map; F1/F2/F3/R additionally drive local tools. There is no generic resolution of native macros nor a mapping for several actions (`C`, `L`, `G`, `M`, `O`, `H`, `A`, `V`). The same key combinations therefore trigger different functions, and the 1.68 client's custom shortcuts are not portable to Java.

## 52. Hotkeys extracted from translations

The native `GUILocalString` and `GUIDELocalString` catalogues parse UI strings, extract a letter preceded by an underscore as a hotkey, and expose it via `GetHotKey`. The key can therefore be localized along with the text and change per language without modifying the window's code.

The Java `I18n` catalogue resolves texts and their parameters, but no hotkey/mnemonic extraction or use was found. Java commands remain tied to the keys hardcoded in the handlers; a translation therefore cannot automatically move the shortcut associated with a button, and language variants do not reproduce the native menus' interactive behavior.

## 55. XP/hour stats and PvP stats displayed in chat

The native client contains two dedicated observation tools: `XpStat` records the start of a measurement and computes XP gained, elapsed time and average speed in XP/hour; `PvpRanking` displays PvP points, current and total kills/deaths, and kill streaks. The results are injected into `ChatterUI`'s backscroll and remain viewable as system messages.

No Java equivalent of `XpStat` or `PvpRanking` was found. The Java code keeps or displays some progression and combat values, but does not offer these start/stop measurements, their XP/hour time calculation, or the full PvP report in the chat history. Progress tracking and stat review therefore do not work the way they do in the 1.68 client.

## 56. Duration and handling of overhead character text

The native `TFCObject` keeps speech text and the name separately: the text is centered, capped by `MaxOverheadLines`, and can receive a line offset. Timed purging is differentiated: `ChkText` expires the main player's text after 10 seconds, but leaves remote units' text up to 25 seconds; an explicit `StopTalkText` can remove it earlier. The name has its own `DisplayName`/`StopNameDisplay` state.

The Java code imposes a 10-second expiration for the player's speech text (`talkTextExpiresAt`) and a configured duration for names (`showNameFor`), with no Java-side equivalent 25-second distinction for remote units. The number of lines, wrapping, offsets and the moment of disappearance therefore do not follow the native cycle; a message from another unit can disappear too early or overlap differently.

## 57. Built-in help, letters and graphic pages

The native `RTHelp` is a real paginated window: 12 help pages, 2 letter pages, 4 world-map pages, a maze page and a special image. The pages are graphic resources localized according to `Player.szLanguage`, with previous/next buttons, closing, and distinct display modes (help, letters, maps). Ctrl+W also opens this system in the native client.

The Java code has a world map and occasional help texts in chat, but no paginated screen equivalent to `RTHelp`, no set of localized graphic pages and no help/letters/maze navigation. The Java user therefore does not have the same assistance content or the same built-in learning path.

## 58. In-game video recording

In addition to one-off screenshot capture, the native client has `NMVideoCapture` with `StartCapture`, `CaptureFrame` and `StopCapture`. The Ctrl+B/Ctrl+N callbacks are set up to start and stop recording, while the render loop captures frames and writes a video stream; start and end messages are added to the system chat.

The Java code has no video-recording component nor macro-drivable frame capture. Profiling/JFR tools and any possible external capture do not produce the game's framebuffer video nor the same messages and state transitions as the native client.

## 59. Text formatting and line wrapping

The native `FormatText` engine does more than display a string: it measures each word with the active font, wraps according to a pixel width, adds a configurable indent to subsequent lines, and interprets the system marker `<>` as a forced formatting break. The resulting line count and offsets are then used by widgets and dialogue bubbles.

The Java code uses several libGDX paths (`BitmapFont`, `GlyphLayout`, `NameRenderer`) with wrapping and limits specific to each screen. No global handling of the native `<>` marker, its indentation, or its per-font wrapping rules was found. The same long sentence, a formatted dialogue, or a bubble above a character can therefore break on different words and occupy a different height.

## 53. Visual and UI options with no Java equivalent

The native options drive specific behaviors: `bShowItemSpec` shows stats on hover, `bLockTarget` changes double-click targeting, `bHighFont` selects a larger font, `bOldStatBar` picks the old or new stat-bar format, `bShowNewLife` enables modern life elements, `bShowNewOmbrage` changes shading, `bShowAnimDecorsLight` enables animated-decor lighting, `bShowWeatherEffects` filters rain/snow/fog, `bDisplayMacroFullScreen` enlarges the macro UI, and `bEnableDisplayGold` controls gold display.

The Java `GamePreferences` does not contain these separate switches. It notably offers `showHudValues`, `transparentGui`, `seraphAnimation` and `xpBarText`, but these preferences do not correspond to the same native branches, and no Java setting allows selecting bar variants, font, decor shading, decor lighting, weather, item hover, or fullscreen macros. Rendering and the visible information therefore remain dictated by the Java code, even where the 1.68 client let the user turn them off.

## 54. Mouse event routing and double-click

The native code explicitly distinguishes `DM_DOWN`, `DM_CLICK`, `DM_DOUBLE_CLICK`, `DM_DRAG`, `DM_DROP` and right-button clicks. `MouseAction.cpp` routes these states to the UI, dialogue, item use, pickup, combat or movement; as long as a UI blocks movement, movement actions are suspended. Double-click can additionally lock the target when `bLockTarget` is on.

The Java code has `touchDown`/`touchUp` handling and some drag-and-drop for the inventory and the quick bar, but the world handlers do not expose a general state machine equivalent to the native double-click. Targeting, pickup and movement are dispatched by separate handlers and do not share the same optional lock. A double-click or a drag onto a target can therefore produce a different move, attack or interaction depending on the order of the libGDX processors, whereas the 1.68 client centralizes this decision in `MouseAction`.

## 60. Spell selection and next-target capture

In the native `SpellUI`, selecting a spell does not just trigger an immediate action: for a spell that needs a target, the client installs an explicit capture of the next mouse event (`LockNextEvent(DM_CLICK, ...)`). The next click is then consumed by the target handler (position or unit), instead of being interpreted as movement or a generic interaction. Double-clicking a spell in the list directly calls `CastSpell`, and the page simultaneously shows the icon, the mana cost sent with the spell, and the selection state, with a selection sound.

The Java `SpellBook` mainly implements visual selection, page switching and drag-and-drop to the quick-slots; its `onTouchDown` remembers a spell to drag and its `onTouchUp` assigns it to a slot. No common state equivalent to "next click captured to target the spell", nor a list double-click that directly casts the spell, was found. Java spells are triggered by `MainGameScreen`'s shortcuts/handlers, which already pick the target (self, hostile unit, or position) before calling `SpellCastingService`. This changes the user flow and can let a targeting click act as movement or another interaction in cases where the 1.68 client necessarily waited for the spell's target.

Native spell macros also persist a dedicated structure (icon, ID, key, quick-slot position and name), loaded by `ClientInitialize`. The Java quick-slots persist a spell-name association, but not this full macro model nor its associated cast/capture behavior.

## 61. Multi-selection in the training and trading windows

The native `BuyUI`, `SellUI`, `SkillTeachUI` and `SkillTrainUI` windows have a per-row basket state: clicking the controls immediately recomputes the total, remaining gold and quantity, and the action button then serializes the full list into a request (`RQ_SendBuyItemList`, `RQ_SendSellItemList`, `RQ_SendTeachSkillList` or `RQ_SendTrainSkillList`). Double-clicking a row is a functional shortcut: it reuses the quantity click and can directly send the selection depending on context. Capacity, price and gold checks are therefore visible before sending, but final validation belongs to the server.

The Java `LearnScreen` does have a basket, but its interaction goes through generic +/- buttons added by `GuiListScreen`; no row double-click equivalent to the native handlers was found. Above all, `learnBasket()` directly deducts gold and points, modifies levels/flags and marks spells as learned locally, instead of serializing a native list request and waiting for the corresponding server updates. Even where the nominal result is identical, a multi-selection, a double-click, a desync or a server refusal therefore does not follow the same behavior as the 1.68 client.

## 62. Interacting with active effects

The native `EffectStatusUI` does more than display icons: it sorts effects, caps the view at seven slots, adds scrolling, removes expired effects on every recompute, and shows the remaining time in hours/minutes/seconds format in its tooltip. Double-clicking an effect tries to recast the corresponding spell; if that spell cannot be recast, the client also looks for a backpack item whose name is extracted from the effect's description and sends `RQ_UseObject`. On failure, it writes a system message to chat.

The Java code displays every `ActiveBuff` stacked one below another in `PlayerHUD`, with no native seven-item cap or scroll buttons. Hovering provides a tooltip, but no effect double-click reproduces the native "recast, then search the backpack for an item" sequence. Dispelling exists in the game services, but the client flow and the item fallback are different; a large number of effects can also overflow the HUD area instead of being paginated.

## 63. Chat channels, ignore list and destination switching

The native `ChatterUI` maintains a list of named channels, with color, password, individual activation and selection of the current channel. Keyboard input has several distinct destinations: game, private page, selected channel and GM command. The user can join a channel, leave it/hide its messages, view its users, send a private page, and manage an ignore list persisted in `SaveGame`. System messages are separate from channel messages, and the system queue is capped at five items before the oldest are dropped.

The Java code has a `GameChat` oriented around a text area and an optional log, but no equivalent structure of persisted channels with color/password/activation, no native ignore list, and no unified game/page/channel routing comparable to it was found. Java shortcuts and commands are dispatched by separate handlers; switching destination, ignoring a player, or restoring channels after a restart therefore does not reproduce the 1.68 client's state. Retention of system messages and their visual separation are also different.

## 64. Real-time map and map memory

The native `RTMap` rebuilds the view around the player's position from the current world, applies the visibility mask, and uses `CSaveGame::GetRTMapVal` to look up coordinate conversion. The save file contains a separate map memory per world (up to 10 worlds, with a 192×192-value grid in `SaveGame.h`); this memory keeps already-revealed areas across sessions. The window also has its own mask, its position marker, and a different conversion depending on old maps or high-resolution maps.

The Java `GuiWorldMap` correctly recreates a local view from `OriginalRtMap` and applies a static graphic mask, but no reading/writing of a per-player, per-world discovery memory was found in `PlayerStateStore` or `PlayerStateDto`. The Java view is therefore recomputed from the full map every time it opens, with a reveal state that does not follow native persistence. Both clients can display the same position and the same palette while revealing different areas after movement or a restart.

## 65. Foreground window and global drag-and-drop

The native `GameUI` maintains a foreground control shared by the entire UI. A modal window can request this control, prevent other children from receiving clicks, and explicitly hand it back on closing. Drag-and-drop is also global: the source, the event visitor, the parent and the initial position are kept in `GameUI`, which lets a drop zone different from the source window finalize or cancel the operation. The minimum movement before showing help is also tested against a precise distance, so a brief click does not start an unwanted drag or help popup.

The Java code has local drag states in `Inventory`, `SpellBook` and `PlayerHUD`, and `GuiManager` opens/closes screens, but no single global capture of the source and parent equivalent to `GameUI::SetDragItem` was found. Drop zones are therefore coupled to each screen (inventory to quick-slot, spell to quick-slot, etc.), and a window opened on top does not necessarily share the same input lock with the world. A drag started in one UI and then moved to another, or a close during that drag, can therefore be cancelled or interpreted differently than in the native client.

## 140. Global window manager: native stacking vs. single Java screen

The native `RootBoxUI` keeps a collection of simultaneously visible windows, a `foregroundChild`, a list of minimized windows, and global routing of left/right clicks, drag, wheel, text and keys. Opening a fullscreen panel automatically minimizes chat and macros; the map, help and the item detail sheet still have specific priority rules. Rendering draws the minimized windows first and then the foreground window, while `IsMouseOwned` prevents the world from receiving an event already consumed by the UI.

The Java `GuiManager` only keeps a single `current` reference. Opening a new screen immediately disposes of the old one, and events are only forwarded to that screen; HUD elements are managed separately, with no global stack of minimized windows or a shared `foregroundChild`. The Java code therefore cannot reproduce native combinations like "chat + macros + minimized panel", cross-window priorities, or routing the same event between stacked windows.

## 66. Local minimap and item filtering

The native `SideMenu` generates a local TMI around the player by reading the current world, then filters items by their group (`m_bShowThisObjType`). The panel can therefore show or hide different categories of nearby items separately, change world and rebuild the zone without reloading the whole map. This filtering is independent of the `RTMap` world map and the simple position marker.

The Java code provides `GuiWorldMap`/`MapScreen` for the world map and object managers for interactions, but no local minimap component equivalent to `SideMenu`'s TMI, nor a persistent per-object-group filter table, was found in the HUD. Nearby items are therefore rendered by the world or handled by the managers, without the same ability to selectively hide them in a minimap. Navigation and the legibility of nearby elements therefore differ even when the world map is correct.

## 67. Options apply-and-save cycle

The native `OptionsUI` loads `OptionParam`'s binary values on open, uses discrete ranges (volume 0–10, brightness 2–10), applies several settings to the render/audio branches during the session, and calls `g_SaveGame.bSave()` when the window closes. The log file name can be changed via a dedicated popup, then fed back into the chatter. The values are therefore not simple display preferences: they change current behavior and are saved with the client profile.

The Java code stores normalized float values (volume 0–1, brightness clamped 0.5–1.25) in a separate JSON file and does not have the same single "window load → apply all effects → save on close" cycle. Several native settings are absent or only approximated (`highQualityFont`, logging, transparency), and the format as well as the bounds differ. A value imported or set identically therefore does not necessarily have the same immediate effect nor the same persistence compatibility.

## 68. Item double-click, equipping and inventory macros

In the native `InventoryUI`, double-clicking an inventory row is a dedicated action: it tries to use the item, or equip it depending on its type, and then sends `RQ_UseObject`/the equip requests. Right-click separately requests the name and then the item's detailed info. A drag to a chest or a trade goes through a quantity popup and specific packets; a failed drag is restored to its original position. The Macro button also turns the item into a persistent macro identified by its `baseId`, and the macro then looks up the matching instance in the backpack before using it.

The Java `Inventory` handles drag and auto-equip in touch handlers, but no general event machine reproduces the native double-click, the "name then info" right-click, the cross-container quantity popup, or the global return to the source position. Java quick-slots associate a name with a slot; they do not reproduce native item macros based on the base ID and the lookup of an available instance. The same gesture on a stack, an equippable item, or an item from a chest can therefore trigger a different action or not offer the same quantity choice.

## 69. Character sheet: full skill list and usage

The native `CharacterUI` requests the skill list from the server (`RQ_GetSkillList`), builds a scrollable list of every skill received, and displays for each one the icon, the current value, the true/unmodified value, and a detailed description. Double-clicking a usable skill calls `UseSkill`; depending on the type, the action is immediate or it captures the next position/unit click via `LockNextEvent(DM_CLICK, ...)`. Skill macros then reuse the skill's numeric ID and look it up in the received list before executing.

The Java `Statistics` displays a fixed selection of combat skills (`attack`, `dodge`, `archery`) and their base/effective values, but does not reproduce the full server list, its scrolling, double-click-activatable skills, or the target capture specific to skills. Java shortcuts also do not use the native per-skill-ID macro model. A skill learned or dynamically granted by the world can therefore be missing from the Java sheet or have no equivalent use flow.

## 70. Missing or unpopulated sheet information

The native sheet actually displays luck (`Lck`), current kills and deaths, current/max weight and remaining weight, the six elemental resistances and six elemental powers, and karma turned into nine localized labels according to precise thresholds. It also distinguishes true HP from max HP as modified by bonuses, and displays the XP remaining to the next level. These fields are refreshed from the state received via `RQ_GetStatus`.

The Java `Statistics` reserves some labels (karma, remaining XP) but does not systematically populate them with an equivalent native value; luck, kill/death counters and the remaining-weight detail are not presented the way `CharacterUI` presents them. The Java state sometimes keeps karma in the DTO, without reproducing the localized threshold-based conversion. The sheet can therefore look visually complete while giving less actual information and reacting differently to status updates.

## 71. Guild UI and action permissions

The native `GuildUI` explicitly fetches the member list with `RQ_GuildGetMembers`, keeps the guild's ID, name and a rights string, and only shows the invite and kick buttons if the corresponding permission bits are set. Selecting a member can open a page, inviting captures the next click on a world unit, and accepting an invitation goes through a popup with the guild name, the sender's name and a yes/no choice. Nearby and distant members, as well as the leader, have different colors in the list, and the leave/kick controls send distinct requests.

In the Java code, no equivalent interactive guild window, nor a rights/members/invitations manager tied to the native packets, was found. The existing `guild`/`clan` elements are mostly about monster clans or NPC behaviors, not the player's guild. The Java user therefore does not have the same invitation-acceptance flow, target-a-player-to-invite, permission filtering, proximity coloring, or server-driven member-list update.

## 72. Group: automatic sharing, member states and targeting

The native `GroupPlayUI` keeps, for each member, an ID, a name, a leader status and a separately updated HP percentage. The window distinguishes the leader and nearby/distant members by color, shows reduced life bars in the HUD, and enables/disables the invite, leave, kick and auto-share buttons depending on the current role. Inviting targets the next click on a unit, while accepting a request goes through a popup; toggling auto-share sends a dedicated action to the server.

The Java code contains no equivalent player-group UI with remote members, a leader, percentage HP, auto-sharing and role-conditioned buttons. Searches for `group`/`party` mostly return effects, monster behaviors or game data, with no comparable network group workflow. Java combat can therefore work on local targets without providing the 1.68 client's group information, HUD tracking and invitation transitions.

## 73. Player-to-player trade: bilateral state and validation

In the 1.68 client, trading between players is not a simple inventory window: it has a server-driven session state. The window separately keeps the items and gold offered by the local player and by the other player, as well as both validation states (`myStatus` and `otherStatus`). Native events explicitly cover starting, cancelling, ending the trade, and each participant's status change.

Item movements go through dedicated requests in both directions (backpack to trade, trade to backpack) and take an explicit quantity. The client therefore shows a quantity selector for stacks, with a computed limit, instead of always moving the whole stack. Closing the UI while a trade is active sends a cancellation, and the backpack's contents are refreshed from the server state.

In the current Java code, `ShopScreen`, the inventory and chests exist, but no implementation equivalent to `TradeUI` was found: no bilateral player/player offer, no local/remote confirmation status, no start/cancel/finish events, and no quantified transfer in both directions. The full transactional behavior is therefore missing: validation on both sides, consistent cancellation, offer locking, and atomic resolution/rollback on the server side. The offer-related mechanisms found in the Java code concern shops or internal screens and do not constitute this player-to-player trade.

## 74. Network session: connection loss, ACK and state recovery

The 1.68 client uses a persistent communication layer with individually tracked connections, fragmented/reassembled packets, received-packet IDs, packets awaiting ACK, retransmission/expiry, and a list of lost connections. `PacketCenter::KeepAlive()` is called on every loop iteration to detect a stall or an infinite wait. Maintenance checks packet, fragment and connection timeouts, then removes the expired connection. Final closing also sends an explicit disconnect packet.

The current Java code has no equivalent game network layer in `src/main/java`: occurrences of connection/session/keepalive/reconnection are absent, while the words `teleport` and `zone` correspond to local content and rendering functions. There is therefore no comparable behavior for loss detection, ACKs, deduplication, fragmentation, retransmission, recovery, or cleaning up UI/game state after a disconnect. Java movement and map changes remain local; they cannot be compared to a server-confirmed transition with state restoration.

## 75. Pickpocketing (`RobUI`): selection and execution

The 1.68 client has a dedicated pickpocketing UI. It receives the permission (`canRob`), the target's name and a list of exposed items, displays the items with their appearance, base ID, instance ID and quantity, lets the player select one, then execute the action via a separate button. The UI can also refuse the action and close explicitly.

The Java code does have a content/skill reference named `rob` and a spell ID, but no window or logic equivalent to `RobUI`, no selectable target inventory, and no rob execution/validation command were found. The name's presence in the definitions therefore does not provide the native functionality: item selection, server permission, result, refusal and closing are all missing.

## 76. Detailed item sheet and server request

The 1.68 client has an item sheet (`RTItemI`) fed by `RQ_QueryItemInfo`. A right-click on a ground item or an inventory item can ask the server for its full description: direct stats, resistances/powers, skills, bonuses and min/max values. The sheet also colors the item according to its bonus count and distinguishes inventory-item info from ground-item info. It is shown in a dedicated window, then invalidated when the response disappears or a new request is issued.

The Java code displays tooltips built locally from `ItemDefinition`/known values, but no equivalent of an item-info request, an async response, an `RTItemI` sheet, or a min/max bonus display calculation was found. A ground item therefore does not get the same server inspection cycle, and the visible data can be limited to the local static definitions.

## 77. Selling to merchants: item price and identity

The native sale (`SellUI`) receives a sell list from the server containing, for each entry, the instance ID (`dwID`), appearance, price, available quantity and `maxQty`. Selecting an item updates the requested total and the remaining amount, then `RQ_SendSellItemList` sends the server pairs of instance ID / sold quantity. The server therefore keeps control of price, availability and the exact item consumed.

In `ShopScreen.forSelling`, the Java code rebuilds entries from local item keys, hardcodes the price as `def.getPrice() / 2`, caps the quantity by a simple item count, and on confirmation destroys the items locally and immediately credits the gold. There is no instance ID, no price transmitted by the merchant, no server `maxQty`, and no equivalent sell request. Two identical items with different durability/bonuses therefore cannot follow the same behavior as in the 1.68 client, and a local sale has no native server validation/rollback.

## 78. Buying from a merchant: local basket vs. server validation

`BuyUI` builds a basket of several references, shows the current price and locally checks available gold, but confirmation sends the server the list of IDs and quantities (`RQ_SendBuyItemList`). The server remains the authority for stock, the final price, inventory weight/volume and actually creating the items.

The Java code immediately deducts the gold and adds the keys straight into `Player`'s inventory list. `ShopScreen` has no server-submitted basket, no purchase response, no remote stock check, and no compensation on failure. Visible behavior can therefore diverge as soon as a price, an available quantity, or an inventory constraint differs from the local definition.

## 79. XP/hour stats and PvP stats

The native client contains two distinct services that turn received data into chat-history messages. `XpStat` records the starting XP and the timestamp, computes XP gained, elapsed time and average hourly speed, then displays the formatted results. `PvpRanking` separately displays PvP points, current and total kills/deaths, best streak and current streak.

The Java code computes and displays some XP or PvP values in the stats screens, but does not reproduce this native start/stop measurement cycle, the hourly calculation, or the injection into chat history. The native stats are therefore a persistent interactive client feature, not just character-sheet fields; this flow is missing or different in the Java code.

## 80. Client protocol inventory: 131 native operations with no Java equivalent

Combing through `PacketTypes.h` finds 131 distinct `RQ_` symbols. They cover, among other things, the eight movement directions, ACKs, character creation/deletion, version authentication, world changes, unit updates, peripheral items, HP/mana/weight/XP, equipment, item use, chests, buying/selling, trades, groups, guilds, chat channels, effects, spells, skills, robbery, weather, death, seraph and PvP stats.

The Java code has no `PacketTypes`, no T4C client serializer, no response dispatcher and no game socket layer. Classes with names like `Request`, `PhysicalAttackRequest` or the Content Studio's HTTP endpoints are not implementations of these packets: they serve the editor or local calls. As a result, every `RQ_` operation remains absent as a Java execution contract, even where a screen or a local mutation carries a similarly-sounding functional name. The preceding sections detail the consequences per domain; this count provides the overall verification of protocol coverage.

## 81. Game file integrity before connecting

The native startup checks external files before proceeding: the `gamefiles\MD.MD` index, the presence of `T4CGameFile.VSB` and `VSBInfo.Txt`, then compares the resource file's size against the declared value. On absence or a size mismatch, it shows an error box and interrupts/replays the loading path. The client can also launch WebPatch before authentication and keep the server/login parameters coming from the launcher.

The Java code loads its resources on demand via `SpriteLoader`, with exceptions often swallowed or graphics fallbacks. It has no bootstrap phase that validates a global index and a resource container's size before entering the game, nor an equivalent built-in WebPatch/launcher. A partially or incorrectly patched install can therefore reach the game screen with missing resources, whereas the 1.68 client blocks or re-triggers the update earlier.

## 82. Global shortcuts and macro disabling in modal windows

The native client installs global shortcuts in `MacroHandler`: Ctrl+I (inventory), Ctrl+S (character), Ctrl+C (attack mode), Ctrl+L (chat), Ctrl+G (group), Ctrl+P (spells), Ctrl+M (macros), Ctrl+O (options), Ctrl+W (map), Ctrl+T (trade), Ctrl+H (capture), Ctrl+A (chat size) and Ctrl+V (identify items). Shortcuts can be reassigned by the profile, are invoked before other keyboard handling, and can be temporarily disabled (`DisableMacroCall`) while editing a macro or during certain popups.

The Java `GameInputHandler` only implements a subset of these combinations and redirects them to local screens/states; several native openings (group, trade, macros, options, global identify, capture) have no equivalent functional target. Java screens also do not use a central callback registry with a global macro lock: depending on the active window, a key can be ignored, handled by chat, or act on the game. The same shortcut and the same opening sequence are therefore not guaranteed.

## 83. Server system events: pages, messages, time and URL

The 1.68 client handles several responses that are not simple chat lines: `RQ_MessageOfDay` displays the message of the day, `RQ_ServerMessage` and `RQ_InfoMessage` feed distinct system queues/presentations, `RQ_Page` and `RQ_TogglePage` manage private pages with enable/disable, and `RQ_OpenURL` passes a URL to the client's system action. `RQ_GetTime` also synchronizes a piece of server time information. `RQ_PlayerFastMode` changes an execution mode received/confirmed by the server, and `RQ_SafePlug` takes part in the native anti-disconnect/anti-plug control.

The Java code has a `GameChat` and `SystemMessage`s, but no dispatcher for these responses and no protocol-level distinction between message of the day, server message, info, private page enabled/disabled, and chat channel. No server-time cycle, no received-URL opening, no confirmed fast mode, and no equivalent `SafePlug` were found. Java text that looks like system messages is therefore produced locally and does not reproduce the 1.68 client's transitions, queues, permissions or external actions.

## 84. Residual native operations: refresh, existence and cleanup

Cross-checking `PacketTypes.h`'s symbols leaves several functional operations that are not just duplicates of plain screens:

- `RQ_ViewBackpack` and `RQ_ViewEquiped` ask the server for a fresh state of the backpack and equipment;
- `RQ_QueryItemName` resolves an instance's name, notably in chests, inventory and trade, before display;
- `RQ_JunkItems` sends the server the list of discarded item IDs in one action;
- `RQ_UseItemByAppearance` and `RQ_CannotFindItemByAppearance` handle use-by-appearance and the failure feedback when the instance no longer exists;
- `RQ_MissingUnit`, `RQ_QueryUnitExistence` and `RQ_SendPeriphericObjects` maintain the consistency of units around the player;
- `RQ_QueryNameExistence`, `RQ_QueryPatchServerInfo` and `RQ_QueryServerVersion` respectively arbitrate name, patch and server compatibility;
- `RQ_NotifyGroupDisband` and `RQ_ToggleChatterListening` propagate remote changes that close/refresh the relevant UIs.

The Java code directly modifies the local inventory/equipment lists, knows names via `ItemRegistry`, has no server-submitted bulk-discard action, and has no peripheral-unit refresh cycle. The searches also show no Java equivalent responses for name/unit existence, patch info, group dissolution, or channel listening. These operations round out the general protocol gaps: they explain precisely why a local Java list can remain visually valid while already being stale or invalid on the 1.68 side.

## 85. Refreshing points, spells, remort and PvP ranking

The 1.68 client has distinct requests to re-read skill points (`RQ_GetSkillStatPoints`), send a stat-training action (`RQ_SendStatTrain`), send a spell-learning list (`RQ_SendSpellList`), request remort (`RQ_Remort`), fetch the personal character list (`RQ_GetPersonnalPClist`), and request PvP ranking/status (`RQ_GetPvpRanking`). Each operation has its own ACK, and the responses then feed the relevant screens.

The Java code modifies stats, skills, learned spells, remort and PvP data directly in `Player`/the screens, with local saving. There is no distinction between request, acceptance, refusal and server refresh for these operations. Double validation, a different cost, a point cap, or a change made by some other server state therefore cannot produce the same transition as in the 1.68 client.

## 86. Clock and day/night cycle: server time vs. local time

The native `RQ_GetTime` response carries second, minute, hour, day, week, month and year separately in `g_TimeStructure`. The displayed time and the effects that depend on it therefore start from a received server reference, even though the client then keeps running its own local counters.

The Java code instantiates `DayNightCycle` with a default time or a time reloaded from `PlayerStateStore`, then increments it with `delta`. This clock is never resynced by a server response. Two clients started at different times can therefore have different day/night phases, whereas the 1.68 client converges on the server's date/time.

## 87. Arrows: server result and projectile collision

The 1.68 client receives `RQ_ArrowHit` with the shooter's ID, the target's ID and the new HP percentage, then triggers `ShootArrow` and `PlAttack` with this already-resolved state. `RQ_ArrowMiss`, conversely, carries the shooter, the final position and a collision flag; the client then plays the trajectory and the miss outcome without recomputing the accuracy roll.

In `MainGameScreen`, the Java code fires the projectile and then runs `applyBowImpact` in the visual callback. `CombatResolver` decides hit/miss and damage locally at the moment of impact, before updating the target. Visual collision, the combat result and HP reduction therefore do not follow the 1.68 contract: a Java projectile can land on screen where the native client would have received a miss, or the reverse, and the result is not corrected by a server packet.

## 88. Spell effects: result packet vs. local resolution

The native `RQ_SpellEffect` response does not just carry the spell's ID: it carries the caster, the target, the target's and caster's positions, plus `spellEffectId` and `spellChildId`. The client then picks the effect's movement and follow behavior (`Follow`), its final position and its offensive-or-beneficial presentation from this response. The animation is therefore triggered by a network result that can differ from the initial attempt.

The Java code calls `SpellEffectManager.resolve(...)` in `MainGameScreen`'s impact callbacks, then locally modifies HP, mana, buffs, drains, persistent effects and summons. No equivalent result packet, no server validation and no rollback occur between the visual cast and this mutation. The Java code therefore makes the end of the animation the effect's authority; the 1.68 client makes the `RQ_SpellEffect` response the authority, which changes the outcome when the target is dead, moved, out of range, refused, or already modified by another player.

## 89. Casting a spell: full local validation with no `RQ_CastSpell` request

In the 1.68 client protocol, `RQ_CastSpell` is a separate operation from `RQ_SpellEffect`: the cast and its impact result are not a single local mutation. The Java code has no transport for this request. `SpellCastingService.begin(...)` locally validates the known spell, the target, PvP, line of sight, cooldown, exhaustion, mana cost and success rate, then immediately deducts mana and activates cooldowns/exhaustion.

This reproduces a plausible gameplay rule, but not 1.68's network behavior: the Java client can consume resources and start the animation before any remote acceptance, with no acknowledgment, server refusal, resync or cost correction. Local validation also becomes dependent on Java's own stats and formula, whereas the native client delegates the final decision to the `RQ_CastSpell` exchange and then waits for server responses.

## 90. Persistent statuses: network creation and local expiration

The native client handles `RQ_CreateEffectStatus` (packet 83) with an effect ID, remaining time, total duration, an icon ID and a description, then calls `EffectStatusUI::AddEffect`. The persistent-effects UI is therefore fed by a dedicated server notification, independent of the visual-impact packet.

The Java code creates `Player.ActiveBuff`s directly from `SpellEffectManager`, `ItemUseService`, `SeraphAuraService` or local restoration, then decays the durations in `Player`. There is no separate server message that can create, replace, extend or remove a status. The icon, description, remaining time and stat contributions can therefore diverge whenever the 1.68 server applies a status with no animation, replaces it, dispels it, or refuses the local application.

## 91. Status dispelling: `RQ_DispellEffectStatus` absent

The native protocol also has `RQ_DispellEffectStatus` (packet 84), which calls `EffectStatusUI::DispellEffect` with the effect's ID. Removing a status is therefore not just a client counter expiring: the server can explicitly remove an effect at any time.

The Java code shows no network dispatcher and no equivalent method receiving an effect ID; `Player.dispelBuff(...)` is called by local services and by Java game logic. A remote dispel, a replacement by ID, or a simultaneous removal therefore cannot be reproduced faithfully, and the buff's stat bonuses can remain active until local expiration/cleanup.

## 92. Character resources: separate server updates

The native client treats `RQ_GetStatus` as a full initial/refreshed state (HP and max, mana and max, XP, gold, weight and other fields), then separately handles `RQ_HPchanged` with current HP, `RQ_ManaChanged` with current mana, and `RQ_UpdateWeight` with weight and max weight. These packets update the character sheet independently of combat or item-use animations.

The Java code has no such synchronization channels: `Player` is modified directly by `SpellCastingService`, `SpellEffectManager`, the shop/training screens and local services, then saved in `PlayerStateStore`. There is no periodic server snapshot, no distinction between an accepted resource and a predicted one, and no correction when HP, mana, gold or weight changed outside the local action. Screens can therefore display a value consistent with the Java action but different from the expected 1.68 state.

## 94. Trade cycle: start, content, statuses and completion

The native client receives separate events for the invitation, the start, each side's content (`RQ_TradeContents`), the validation statuses (`RQ_TradeSetStatus`), cancellation and completion (`RQ_TradeCancel`, `RQ_TradeFinish`). The window is therefore driven by a bilateral server state: a content or validation change can invalidate the prior agreement before closing.

The Java code has a trade UI, but the search shows no transport for these events nor a matching network state machine. The local state therefore cannot reproduce a refused invitation, a remote cancellation, a simultaneous change to the other party's inventory, or validations resetting after a content change.

## 95. Group: invitation and remote states not separated

The native client distinguishes the invitation (`RQ_GroupInvite`), the invitation list to refresh, the member list, leaving, kicking, and auto-sharing (`RQ_GroupToggleAutoSplit`). Members' HP is also refreshed by a dedicated packet (`RQ_UpdateGroupMemberHp`), and the window is cleared when a departure or disbandment is received.

The Java code has no dispatcher for these protocol events: its group information is produced by local state and the screens. It is therefore missing the pending-invitation, accept/refuse, remote-kick, confirmed-auto-share, and a member's HP updated-without-a-full-reload transitions.

## 96. Ground items: server consistency vs. local pickup

The native client receives nearby items and their updates via the protocol (`RQ_GetNearItems`, `RQ_GetObject`, `RQ_ViewGroundItemIndentContent`). When an item is no longer present or cannot be obtained, the client removes it from its list and shows the appropriate failure state; depositing into a container (`RQ_DepositObject`) is also a network transition.

The Java code has `GroundItemManager`, but it creates drops from local loot, displays them, and removes them immediately on pickup after locally validating distance, weight and inventory. There is no server item ID, no pickup response, no remote removal, and no appearance-detail request. Two clients can therefore see the same Java drop as available where the 1.68 client has already consumed, moved, or declared it not found.

## 97. Chat: channels and server lists absent

The native client distinguishes indirect and directed conversations, shouts, channel messages and received messages, then separately receives the channel list and the user list with their title and listening state. Joining, leaving, adding/removing and listening to a channel are therefore server state changes, not just a text prefix.

The Java `GameChat` has input, history, autocompletion and a local display, but no protocol-fed user/channel list and no confirmed routing of private, direct, shout and channel messages. Java text commands therefore do not reproduce the permissions, subscriptions, filters and error feedback of 1.68's chat.

## 93. Effect update: duration and icon received separately from the Java buff

The native client receives an effect's creation with an ID, a current duration, a total duration and an icon (`RQ_CreateEffectStatus`), and can then remove it by its ID (`RQ_DispellEffectStatus`). The UI can therefore refresh the duration or replace the icon of an already-present effect without recomputing the effect from the local spell.

The Java code builds its `ActiveBuff`s from the local use of a spell or item, with a duration and icon coming from its own definitions. It has no independent remote refresh by ID in the game path; a duration correction, icon change, or replacement sent by the server therefore cannot follow the same transition as in the 1.68 client.

## 98. XP and leveling up: server notification vs. local loop

The native level-up packet receives the new level, the remaining/needed XP, HP and max, mana and max, then triggers the level-up visual effect. `RQ_XPchanged` separately receives a 64-bit XP value and updates the XP stats without asking the client to recompute the threshold itself.

The Java code does the opposite in `PlayerProgression.addXp(...)`: it adds a local amount, loops over `xpToNextLevel`, raises the level, adds points and applies HP/mana gains. This loop can produce a different level or different gains if the curve, the buff multiplier, the XP already spent, or the server response do not match; the Java code receives neither an authoritative level nor a corrective 64-bit XP value.

## 99. Server-forced shutdown

The native client handles `RQ_ExitGame` as a remote close: it shows the shutdown message, closes the application, and resets the connection state. The Java code only shows a voluntary-exit confirmation screen and has no equivalent server event for shutdown, maintenance, or forced disconnect.

## 100. GM actions and puppet information: different scope

The native client reserves special operations to server state: `RQ_PuppetInformation` concerns a unit/puppet's information, `RQ_GodCreateObject` creates a server-controlled object, and `RQ_BroadcastTextChange` moves the text attached to a unit. `RQ_BreakConversation` also forces the current dialogue to close, while `RQ_Attack` can be received as an animation/attack order for a remote unit.

The Java code exposes local GM commands and NPC scripts, but they modify `Player`, monsters or items directly, with no permission protocol, server-side creation, broadcast, or remote dialogue interruption. A Java GM can therefore get a local effect without reproducing the 1.68 client's authority, visibility to other clients, or unit updates.

## 101. Per-operation ACK, delay and connection monitoring

The native client associates each packet type with a different ACK/delay profile in `Comm.cpp`: movement and instant updates do not wait the way a purchase, a spell, a trade or a session transition does. `RQ_Ack` is handled separately, while `PacketCenter::isAlive()` also watches for about 120 seconds of no traffic.

The Java code has no game packet layer, no ACK numbering/waiting, no retransmission, and no per-operation delay. Its content HTTP calls and its local state therefore cannot reproduce the 1.68 client's stalls, timeouts, connection indicators, or controlled repeats.

## 102. Death packet: native branch with no visible client handling

In this 1.68 source, the `RQ_YouDied` branch is indeed declared but its handling in `Packet.cpp` is empty; the client therefore does not, in that branch, apply the penalty and resurrection. The Java code, by contrast, runs the whole penalty in `configurePlayerDeathCallback`: XP/gold loss, drops, moving to the respawn point, state reset and saving.

This is not a missing equivalence to silently add: it is a difference in responsibility demonstrated by the code. The Java code moved into the client a transition that this native branch leaves to the server flow/another handler, which can change the timing, the source of the values, and the possibility of a remote correction.

## 103. Entering the world: native synchronization vs. local loading

After character preparation, the native client requests nearby items (`RQ_GetNearItems`), sets the `EnterGame` state, then sends `RQ_FromPreInGameToInGame`. This transition coordinates loading units, clearing chat, the screen fade, and actually enabling controls; it is subject to the server and its responses.

The Java code loads `LocalCharacterStore`, directly builds `MainGameScreen`, and restores the local JSON state in `CharacterLoadingScreen`. There is no `PutPlayerInGame`, no remote character list, and no entry synchronization before the world is activated. A character deleted or modified by another session therefore remains locally selectable, and the Java code can enter a map with a state the 1.68 client would have refused or reloaded.

## 104. Seraph arrival and remort: network trigger vs. local detection

The native client handles `RQ_SeraphArrival` with the transmitted coordinates, unit identity, light, type and status, then handles `RQ_Remort`, which notably resets macros. The animation and the displayed state therefore depend on a network notification and the remort's result.

The Java code infers the seraph arrival from the player's local state in `startSeraphArrivalIfNeeded()` and triggers its animation/movement lock with no `RQ_SeraphArrival` packet. Teleports are also triggered by Java definitions when a tile is reached. Missing are the remote confirmation, server-imposed coordinates, updating other units, and the native macro reset on remort.

## 105. Server script-authorization flag

`RQ_GodFlagUpdate` carries a flag ID and its state; for the relevant flag, the native client enables or disables `Player.CanRunScripts`. The server can therefore revoke, on the fly, the permission to run certain scripted or GM actions client-side.

The Java code exposes `GmCommandProcessor` and NPC script execution with no handling of an equivalent network flag. Its permissions are determined by the presence of the component/command and local state, not by a received server toggle. A remote revocation or an admin-context change therefore does not have the same scope.

## 106. Updating and checking the existence of visible units

The native client receives `RQ_UnitUpdate`/`SetUnitStat` to change the state of an already-known unit, and uses `RQ_MissingUnit` or `RQ_QueryUnitExistence` to flag a unit as absent, remove it, or check that it still exists. `RQ_SendPeriphericObjects` also feeds the area around the player; remote items are not kept indefinitely in the client's list.

The Java code updates monsters and NPCs through their own local loops (`MonsterManager`, `NPCManager`) and removes entities according to their own distance, death, or companion rules. There is no network ID, no existence response, and no server peripheral refresh. A unit created, moved, killed or removed in another game state therefore cannot be reconciled with the Java client.

## 107. Item instance identity and name

The native client requests an instance's name by ID (`RQ_QueryItemName`) for the backpack, a chest, and both sides of a trade; it then receives that name and attaches it to the matching instance. `RQ_QueryItemInfo` is also provided for detailed item information, since the visual definition and the instance are two distinct notions.

The Java code resolves items by key/number in `ItemRegistry` and displays `ItemDefinition`'s static stats. Its inventory, chest, drop and trade lists do not carry the native instance ID nor a deferred name response. Two items with the same definition but different state (charges, durability, flags or quest ownership) are therefore treated as the same local type.

## 108. Remaining native operations: no Java per-unit actions

The protocol keeps distinct per-unit operations that the Java code does not transport: the eight directions (`RQ_MoveEast`, `RQ_MoveSouth`, etc.), conversations (`RQ_IndirectTalk`, `RQ_DirectedTalk`, `RQ_Shout`), joining/leaving and adding/removing a channel, as well as user/channel messages. Likewise, the group operation separates invitation, joining, kicking, leaving, member updates and invitation-list updates; trading separates adding from the backpack, removal, and cleanup.

Java classes can reproduce part of the display or call `Player.move`, `GameChat`, the group screens and trading directly, but none of these actions are serialized with the native type, order, ACK and failure. Partial visual coverage is therefore not functional equivalence: the validations and inter-client transitions of each of these operations remain absent.

## 109. Active skill: server feedback vs. local cooldown

The native client requests the skill list (`RQ_GetSkillList`) from the server and then receives the usable states. The use-response carries a skill ID and a result code (`SkillID`, `Return`); activation and failure are therefore not inferred solely from the button pressed.

The Java code uses `SkillService.use(...)`, locally checks level, attributes, cooldown and conditions, then triggers the cooldown/mutation with no server response. A skill refused, modified, or consumed by another state can therefore produce a different Java effect, with no return code and no skill resync.

## 110. Normal attack: authoritative animation packet and HP

The native client receives validated attacks with the attacker, the defender, their positions, and, for a hit, the resulting HP (`pHp`); it then triggers `PlAttack`/`SetAttack`. A miss produces a separate branch that animates the attack without applying damage. Accuracy and the final amount are therefore not recomputed by the client on impact.

The Java code calls `CombatResolver.resolve(...)` in local callbacks in `MainGameScreen`, `MonsterManager` and `BaseMonster`, then applies HP and deaths directly. The same code decides the roll, the result and the rendering all at once. As with arrows, this allows divergence in hit, miss, damage and timing whenever a remote state should take priority.

## 111. Equipping and using items: native requests with no immediate local mutation

The native protocol distinguishes `RQ_EquipObject`, `RQ_UnequipObject` and `RQ_UseObject`, each subject to its own ACK profile. The client sends the action on the instance, then waits for the state responses to update equipment, appearance, charges and stats; the displayed equipment is therefore not proof of acceptance.

The Java code runs `InventoryService.equip/unequip` and `ItemUseService.useOnSelf` directly from `Inventory` or `MainGameScreen`, updates appearances, charges and effects, then saves locally. A server constraint, an instance conflict, an already-used item, or a failure response cannot undo this mutation.

## 112. SafePlug: server authorization to close

The native client handles `RQ_SafePlug` with a binary status: one status forbids logoff (`boInterruptLogoff`), while the other authorizes a forced logoff (`boForceLogoff`). This decision is received from the server and applies at disconnect time, independent of the confirmation window.

The Java code has no SafePlug status and no server disconnect guard. `ExitGameConfirmScreen` triggers a local exit that cannot be blocked or forced by a session response. The timing and safety of closing therefore differ from the 1.68 client.

## 134. Skills: true value, effective value and usability lost

The native `RQ_GetSkillList` response transmits, for each skill, its ID, name, description, current value (`dwStrength`), true/unmodified value (`dwTrueStrength`) and a usability flag (`bUse`). The client keeps these fields separately in `USER_SKILL`, displays them in `CharacterUI`, and only allows double-click or macro use if the received skill is usable. The execution response then adds `SkillID` and `Return`, so the actual result is provided by the server.

The Java code loads `SkillDefinition`/`SkillRegistry` and essentially stores one local level per ID. `SkillService.use` infers permission from the level and its cooldown, then immediately applies the mutations (`meditate`, `sneak`); there is no true/effective value pair, no received `bUse` flag, and no per-skill server `Return`. Bonuses that only change the effective value, server refusals, and the usable state can therefore be displayed or executed differently.

## 113. NPC dialogue: server conversation and local display decoupled

In the 1.68 client, clicking/talking to an NPC builds `RQ_DirectedTalk` with the NPC's coordinates, its ID, the character's computed direction, the color and the text. General speech goes through `RQ_IndirectTalk`. The server can then send back `RQ_GetUnitName` (ID, name, color and guild name) and `RQ_BreakConversation`, the latter explicitly resetting the target and conversation state; overhead text is therefore tied to IDs and a dialogue session, not just a local window.

The Java code handles the click and the Enter key in `NPCInputHandler`, then `NPCManager`/the `NpcSpec`s directly advance a local dialogue (`welcomeText`, `DialogueTopic`, responses and actions). There is no transport for `RQ_DirectedTalk`/`RQ_IndirectTalk`, no `RQ_GetUnitName` response with color/guild, and no interruption packet that invalidates the conversation. A Java dialogue can therefore continue or trigger an action where the 1.68 server would have refused the distance, the target, the text, or interrupted the conversation.

## 114. Character cycle: local roster vs. server account

The native client exposes distinct session and account requests: `RQ_RegisterAccount`, `RQ_CreatePlayer`, `RQ_DeletePlayer`, `RQ_GetPersonnalPClist`, `RQ_PutPlayerInGame`, `RQ_ReturnToMenu`, `RQ_Reroll`, plus an `RQ_MaxCharactersPerAccountInfo` response. The server therefore provides the list, the limits, the identity and the operation results; selection and returning to the game are network transitions, not just screen changes.

The Java code uses `LocalCharacterStore` and `characters.json` to create, delete, list and load characters, locally applies `MAX_CHARACTERS`, then enters `MainGameScreen`. There is no server account/session and no acceptance response for these operations. Two clients, a remote deletion, an account limit, or a concurrent creation therefore cannot produce the same errors and states as the 1.68 client.

## 115. Conversation scripts: persistent conditions and server effects absent

The NPC scripts shipped with the 1.68 client/source describe conversations that read and modify persistent flags, check level, gold, items, delays, offer Yes/No decisions, remove several items, give items, or trigger a teleport. Dialogue is therefore a front end for a transactional server script: the displayed text depends on the character's actual state, and the effect must only be applied once that transaction is validated.

The Java code mainly represents an NPC via `NpcSpec.DialogueTopic` (keywords, response, actions) and advances the conversation client-side. Even where `ActionType`s exist, they do not replace the atomic execution of the 1.68 server script's conditions and consumptions. A response can be shown without the preconditions matching the server's, or a reward/consumption can be applied locally with no confirmation or rollback.

## 116. Chat channels and group: remote states not represented

The native client receives structured lists of channels and users (`RQ_GetChatterChannelList`, `RQ_GetChatterUserList`) with listening state, title and guild. It also receives group updates with ID, name, level, HP percentage, leader and auto-share; `RQ_GroupInvite` opens a remote invitation, and `RQ_GroupLeave`/disbandment explicitly clears the list. Joining, leaving and channel messages are operations separate from general chat.

The Java code has `GameChat` as a display/input area, but no equivalent representation of channels, subscribed users, guilds, invitations, members or group HP was found. Messages can be displayed locally, without synchronizing subscription, the list of interlocutors, XP sharing, or leaving the group. Social behavior and cross-client updates therefore diverge even where text entry works.

## 135. Channel directory: periodic refresh and individual listening absent

In `ChatterUI`, opening the window requests the public channel list (`RQ_GetChatterChannelList`). Selecting a channel then requests its user list (`RQ_GetChatterUserList`) with the channel's name; this list is automatically refreshed every ten seconds. Each received user has at least a name, title, guild and listening state. The listen button also sends the channel and the new state (`RQ_ToggleChatterListening`).

The Java `GameChat` has no channel directory, no selecting a remote channel, no user list, no periodic refresh, and no per-channel listening subscription. Java chat is therefore a local/general conversation and does not reproduce the visibility, filtering or refreshing of the 1.68 channel system.

## 136. Built-in help: paginated manual and contextual help absent

The 1.68 client instantiates `RTHelp` as a dedicated native window. It contains twelve help pages (`Help0` through `Help11`), two letter pages, four map pages and a maze page, with previous/next navigation and conditional display. It is opened automatically on first entering the game (`Show(true, 0, 0)`) and can also be shown for special help, for example after using a particular letter. The overall UI treats this window as a modal screen that hides/minimizes other panels.

The Java code has no `RTHelp`, no paginated manual, no map/maze pages, and no contextual trigger tied to item use. `F1` is assigned to the debug overlay in `GameInputHandler`, not to player help. Even where help texts exist in scripts or GM commands, they do not reproduce the 1.68 manual's flow and modal lock.

## 137. AFK and automatic page replies: persistent state absent

The native client saves `dwAfkStatus` and a 2048-character AFK message in its configuration. The `!AFK ON`, `!AFK OFF`, `!AFK VIEW` and `!AFK MESSAGE ...` commands change this state. When a private page arrives, the client automatically replies via `RQ_Page` with the configured message, while avoiding loops and flooding on certain texts; ignored pages are filtered before this handling.

The Java code has no persistent AFK state, no equivalent command, and no automatic reply to private messages. `GameChat` displays/accepts text but does not reproduce the 1.68 client's distinction between a received page, an auto-reply, loop prevention, and the ignore list.

## 138. Screenshot capture: native macro and dedicated directory absent

The 1.68 client ties screenshot capture to a configurable macro, `Ctrl+H` by default, captures the desktop or the window depending on the execution path, creates the `ScreenShot` folder in the save directory, and then writes the image. The `TakeScreenShot` flag is consumed in the render loop, which guarantees a capture after a frame is rendered.

The Java code has no capture macro and no `TakeScreenShot` handling in `GameInputHandler`/the preferences. Its local shortcuts affect debug, coordinates, teleportation, or reloading resources; capture and its automatic storage therefore do not correspond to native behavior.

## 139. Graphics and UI options: the Java model covers only a subset

The native `OptionParam` persists, in addition to sound and brightness, toggles that immediately change the client's behavior: high graphics lighting, high graphics effects, animated water, dithering, GUI transparency, seraph animation, status display, XP-bar text, gold display, 32 FPS cadence, fullscreen macro, target lock, resize lock, high-res font, zoom, hyperchat, item spec sheet, new life bar, old stat bar, decor shading and animated-decor lighting, and weather effects. `OptionsUI` applies several changes directly to the engine (`ResetAnimWater`, enabling/disabling status effects, zoom and resizing).

The Java `GamePreferences` only contains volumes, brightness, fullscreen/VSync, a few HUD values, seraph animation, XP text, font and logging. It has none of the native switches for animated water, lighting/effects, dithering, gold, 32 FPS, zoom, locked target, hyperchat, item sheet, old/new bars, decor shading/lighting, or weather. These Java behaviors are therefore imposed by the engine or absent, with no way to reproduce 1.68's option profiles.

## 117. Trade: content operations and server cleanup missing

In the 1.68 protocol, trading is not just a window: `RQ_TradeInvite` creates the invitation, `RQ_TradeStarted` opens the session, `RQ_TradeAddItemFromBackpack` and `RQ_TradeRemoveItemToBackpack` move instances between the backpack and the offer, `RQ_TradeClear` clears the content, and then `RQ_TradeSetStatus` and `RQ_TradeFinish` validate or end the transaction. The client receives the offer list via `RQ_TradeContents` and must reflect the other player's changes.

The Java code has no trade transport nor a matching authoritative bilateral state; its inventory services and local UIs can only modify the current player. Missing, therefore, are the remote identity of items, cleanup imposed by the other party, locking before validation, and the distinction between cancelling, refusing and successfully completing the transaction.

## 118. Individual social commands: join requests and management absent

The 1.68 game distinguishes channel join/leave requests (`RQ_EnterChatterChannel`, `RQ_AddRemoveChatterChannel`, `RQ_RemoveFromChatterChannel`), private and channel sending (`RQ_SendChatterMessage`, `RQ_SendChatterChannelMessage`), and group responses (`RQ_GroupJoin`, `RQ_GroupKick`, `RQ_UpdateGroupInviteList`, `RQ_UpdateGroupMembers`). Each action can be accepted, refused, or change the remote list without the client being able to infer it from input alone.

The Java code has none of these commands nor a social-result layer. `GameChat` accepts and displays text, but cannot represent a pending invitation, a confirmed join, a kick by the leader, an invitation list, or a private message addressed to a server ID. The same names visible in the UI therefore do not guarantee the same routing or the same state transitions.

## 119. Visual unit state: light, status and server appearance

When adding or updating a unit, the native client separately reads the type, coordinates, light, status and HP. Responses can then change a character's composed equipment (`SetPuppet`: body, feet, gloves, helmet, legs, weapons, cape), name/color/guild, or create/remove a status effect. The received light value can even be applied to the player and feed the rendering of the map and torches.

`BaseNPC`, `Monster` and the Java entities build their appearance from local definitions and keep their own local HP/behaviors, with no network-equivalent state for `LIGHT`, the `STATUS` byte, server effects, guild identity, or a received composition packet. Java rendering can therefore show a plausible sprite, but cannot reproduce an equipment transformation, an invisibility/special state, an aura, a character light, or a remote update arriving after spawn.

## 120. Persistent preferences: macros, channels and local lists not equivalent

The 1.68 client's `CSaveGame` saves, per account/character, item, spell and skill macros, channels with password/color/listening, the ignore list, remembered chest content, the local inventory, and many graphics/UI settings: zoom, locked target, gold display, 32 FPS, water effects, weather, decor lighting, status bars and fullscreen macros.

The Java `GamePreferences` only keeps a subset (volumes, brightness, fullscreen, VSync, a few HUD texts and logs). Macros, channels, the ignore list, the chest cache, target lock, zoom and several 1.68 rendering options have no identified persistent equivalent. The same install therefore does not recover the same user state after a restart, and some native options that directly change interaction or rendering are absent rather than merely presented differently.

## 121. Combat control: double-click lock and forced attack

In `MouseAction::Combat`, the native client turns a double-click into a target lock when `bLockTarget` is on (`FreezeID`), distinguishes click, double-click and drag, and reserves Shift for a periodically rate-limited special-attack request. Right-click identifies the unit under the cursor instead of triggering the same path as left-click.

The Java code selects a target directly on click in `MonsterInputHandler`, while `ClickToMoveHandler` ignores Shift for movement. No double-click reproduces the native lock, and no Shift attack request with its time limits is generated. Persistent targeting, forced attack and left/right-click behavior are therefore not equivalent, even though Java auto-combat keeps a target in memory.

## 122. Music and sounds: Java engine present, native selection not equivalent

The native client's `GameMusic` picks music based on world, zone, dungeon, cavern, boss, sadness, or ambient noise. It stops/releases the old track, loads the new one via streaming, avoids restarting an identical track, and applies the saved music volume. The sound manager separately handles animation, UI and page effects, with a distinct effects volume.

The Java code does have `SoundManager`, `MusicZoneBinaryIO` and rectangular music zones loaded by `MainGameScreen`; it plays a looping ambience and applies the preference volume. The gap is therefore not a total absence of audio. However, this selection depends on Java zone files and the last entry containing the tile, whereas `GameMusic.cpp` also encodes the priority order of regions, worlds, dungeons, caverns, bosses, sadness states and ambient noise. The Java code also does not demonstrate the equivalent of all native page, UI and transition sounds. An overlapping zone, a boss, or a world change can therefore pick a different track or no track at all despite the audio engine being present.

## 123. Private pages: notification, reply and network toggle absent

The native client receives a page (`RQ_Page`), distinguishes at least a received page, a reply, and "user not found", optionally plays a sound (`bPageSound`), adds the text to the backscroll, and can then reply by sending `RQ_Page` back with the recipient. The page button toggles `bPageEnable` and sends `RQ_TogglePage` to the server; this is not just a local filter.

The Java code has no page mode, no current recipient, no reply-to-a-page, and no equivalent network toggle in `GameChat`. A private notification therefore cannot trigger the sound, the message type, the return routing, or the "user not found" refusal of the 1.68 client; at best it is treated as general text.

## 124. Spellbook: server list state vs. static catalogue

`RQ_SendSpellList` transmits an update marker, current and max mana, and then for each spell its ID, target type, mana cost, duration, level, element, mental/physical type, icon, description and name. The client then rebuilds the spellbook from this response rather than from a full list assumed to be known locally.

The Java code loads `SpellRegistry` and `SpellData` from its definitions, then uses `SpellBook` and `SpellCastingService` with the player's learned spells. There is no network replacement of the list, mana, or per-character metadata. A spell learned, removed, modified, renamed, or temporarily unavailable server-side can therefore remain visible and castable according to the local Java state.

## 125. Training offers: prerequisites and server rights absent

The native `RQ_SendTrainSkillList` and `RQ_SendTeachSkillList` responses include available points, the right to learn (`canHave`), the ID, the current or max level, the price, the name, and, for teaching, the prerequisites, required points and icon. The displayed offer is therefore recomputed by the server for the character and the NPC at request time.

The Java code builds `TrainingCatalog` and `LearnScreen.TrainingOffer`s locally, then `SkillService` directly modifies points, gold and skills. The Java offer model does not carry all of the native fields (notably textual prerequisites, icon and a per-entry server refusal) and does not receive a recomputed list. A skill temporarily forbidden or a cost changed server-side can therefore be offered or validated differently.

## 126. Character sheet: native fields not modeled identically

`RQ_GetStatus` separately updates HP/max, mana/max, 64-bit experience, base and effective AC, eight base/effective stats (strength, endurance, agility, willpower, wisdom, intelligence, luck), stat points, level, skill points, weight/max weight, karma, true max life, and then six elemental powers and six elemental resistances. The client then refreshes the character sheet and equipment simultaneously.

The Java `Stats` model mainly contains the effective values of strength, dexterity, endurance, intelligence, wisdom, HP, mana, XP, points and karma; it does not expose the native base/effective pair, AC, willpower, luck, weight, or true max life as equivalent fields. Resistances and powers are rebuilt via buff/flag maps rather than received in a server snapshot. The Java sheet can therefore display a plausible value without reproducing 1.68's status distinctions and recalculations.

## 127. PvP ranking: server counters and streaks absent

The 1.68 client has a dedicated `RQ_GetPvpRanking` packet, acknowledged separately, that transmits seven values: total deaths, total kills, current deaths/kills, best streak, current streak, and PvP points. `PvpRanking` displays them in a dedicated UI; these values are not inferred from combat's visual events.

The Java code has a PvP-death flag used by `DeathPenaltyService`, but no equivalent ranking structure nor the seven counters received by the native client. Kills, streaks and PvP points are therefore neither synchronized nor displayable from the same persistent state; a local death or kill does not automatically produce the 1.68 ranking result.

## 128. Ground items: initial synchronization and instance identity

On entering the game, the native client explicitly requests `RQ_GetNearItems`, then receives nearby items with coordinates, type, ID, light, status and associated HP/state. It resends this request after certain transitions to restore the real neighborhood; the player and items are treated differently depending on the received ID. Pickup and disappearance therefore rely on a server instance, not just an item's name at a tile.

The Java code spawns ground items via `GroundItemManager.dropItem`, `spawnFromLoot` and `spawnCorpse`, and then `GroundItemClickHandler` calls `pickUpAt` directly and adds the item to the inventory. There is no `GetNearItems` request, no existence response, no instance light/status, and no server resync after entering or changing worlds. An item already picked up, contested, or changed by the server can therefore remain visible or be added locally.

## 129. Chest: content and instance movement authorized by the server

The native protocol separates `RQ_ShowChest`, `RQ_HideChest` and `RQ_ChestContents`. The received content includes, for each item, appearance, instance ID, base ID, quantity and charges. Deposits/withdrawals then trigger distinct requests, and the response can change an item's or the player's appearance via `RQ_DepositObject`; the UI is only visible and valid according to the received state.

The Java code opens `ChestService` from a local chest definition, rolls the loot, adds the gold/items directly, and applies a local cooldown. It does not carry the chest's structured content, instance IDs, received charges, or deposit/withdraw requests with ACKs. Two players or a server-side close therefore cannot converge on the same Java chest.

## 130. Temporary effects: similar rendering, different authority

The native client does not infer a buff's activation from the spell's animation: `RQ_CreateEffectStatus` dictates the ID, the time already elapsed, the total duration, the icon and the description; `RQ_DispellEffectStatus` then removes the effect by ID. The visual counter thus stays aligned with server time and can be replaced or dispelled without recasting the spell locally.

The Java code does have `Player.ActiveBuff` and displays icon, description and duration in `PlayerHUD`, but creates and expires these buffs directly from `SpellEffectManager`/`Player`. It is missing the creation, replacement and dispel-by-server-ID flow; a Java buff can therefore remain active after a remote dispel, or expire at a different moment despite visually comparable rendering.

## 131. Fast mode: native operation distinct from the Java multiplier

The 1.68 protocol reserves `RQ_PlayerFastMode` for a dedicated operation with its own ACK profile. The native client thus distinguishes a protocol-controlled fast-movement mode from ordinary movement and sped-up animations; it is not just a local graphics value.

The Java code has no handler for `RQ_PlayerFastMode`. Its effective speed is the product of buff multipliers and `gmSpeedMultiplier`, the latter changeable by a local `.speed` command. The native fast mode, its activation/refusal, and its server synchronization are therefore not represented by the same state.

## 132. Client/server version: authentication and conditional patch absent

The native client embeds a numeric version (`Version::GetVersion`), sends it during registration/entry, requests or receives the server version, and then sends `RQ_AuthenticateServerVersion`. An incompatible version takes a dedicated error branch; if the server reports a higher version, the client can run `WebPatchUpdate` before continuing. Entering the game is then sequenced with `RQ_PutPlayerInGame`, `RQ_GetNearItems` and `RQ_FromPreInGameToInGame`.

The Java code has no such server-version negotiation, protocol authentication, or conditional patch before building the game session. It loads its local catalogues, maps and characters directly. A data or version mismatch therefore does not produce the 1.68 client's controlled refusal and resync sequence.

## 133. Map exploration: Java shows a static map, with no RTMap memory

The native client has a per-world exploration memory in `CSaveGame`: `m_uchRTMap[10][192][192]`. It is initialized, loaded and saved in the account/character file. `SetRTMapVal` marks a cell visited, and `TFCSocket.cpp` calls it when the player's position updates. Exploration is therefore persistent state, distinct from the plain graphic map.

The Java code does load the original image via `OriginalRtMap` and only applies the static `GUI_RTMapMask` graphic mask in `GuiWorldMap`. `MapScreen` rebuilds the view around the current position; no per-world array of visited cells, nor any read/write of this state in `PlayerStateStore`, was found. Consequence: the Java map does not reproduce the 1.68 client's persistent fog/exploration progress, and its state does not survive a session change.

## 142. 3D models and sounds attached to units: 2D Java rendering

The native `VisualObjectList` reserves a `VObject3D` array and instantiates `Sprite3D`s for many units (for example Beholder, Wizard, Goblin, Mummy, Demon, Minotaur, Rat, Bat, Spider and Skeleton). Each model has its own dimensions, directions, frames, and sometimes several attack, pain or death sounds in `Object3DSound`. The received object type therefore determines not just an appearance, but also a rendering path and a dedicated sound table.

The Java code renders monsters, NPCs and objects with `EntityAnimationsBase`, `PlayerAnimations`, `ObjectRenderer` and 2D textures; no equivalent `Sprite3D`/`Type3D` class or branch was found in the game path. Java sounds are tied to actions or spell/monster definitions, not to a native per-3D-model variant table. Native units using this path can therefore have a different silhouette, orientation, frames and sounds in the Java code.

## 143. Scale of the 3D registry: 153 native loads, zero Java references

Scanning `VisualObjectList.cpp` finds 153 `LoadSprite3D` calls, in addition to object registrations and `Object3DSound` entries. This is therefore not an isolated case limited to a particular monster or decoration: the 1.68 client has a substantial registry of 3D models with their own release cycle and sounds.

Scanning `src/main/java` finds no `Sprite3D`, `LoadSprite3D`, `Type3D` or `Object3DSound` reference. The gap identified in the previous section is therefore structural and measurable, not just a naming difference in one definition.

## 144. Native video capture: optional pipeline absent from Java

The native client embeds `NMVideoCapture` with `StartCapture`, `StopCapture` and `CaptureFrame`, as well as the `StartCapture`/`EndCapture` callbacks that display the capture state in chat. The pipeline is sometimes disabled, or its macros are commented out, in this revision, but the per-frame capture code and its integration into the render loop do exist in the 1.68 client.

The Java code contains no video-capture class or branch, no video-session state, and no equivalent command. Even accounting for the native path being optional, there is therefore no corresponding Java capability to record the game's frames or to show the start/end of such a recording.

## 145. Chat editor: history, destinations and local commands different

Both clients have a history and a clipboard: Java caps its history at 50 entries and its text at 256 characters, while the native `ChatterUI` keeps up to 128 sent texts. The native code walks this history with its `rollbackTyped` iterator and resets the input past the ends; Java uses a bounded index into its `List<String>`. Both offer copy/paste, but the native code injects characters into the active input, whereas Java normalizes line breaks into spaces and truncates the paste to the max length.

The native code has three explicit destination states: `SendToGame`, `SendToPage` and `SendToChannel`. The Enter key then builds different packets and prefixes depending on the mode; the page and channel buttons directly change `textInputState`. The Java `GameChat` only has a `submitHandler` and a general input area: no equivalent editor maintains a page/channel destination or the corresponding input routing in the widget.

Finally, `ChatterUI` locally intercepts test/diagnostic commands (`.fog`, `.rain`, `.snow`, `.star`, `.dagger`, `.spell`) and turns them into visual actions or spell effects without sending them to the game. Searching the Java code finds no such local command parser in `GameChat`; its command handling is separate from the widget and does not reproduce this 1.68 client command table. The same input can therefore only change native rendering, while being sent or handled differently by the Java code.

## 211. Java autocompletion of a GM command with no native equivalent

`GameChat.autocomplete()` specifically recognizes entries matching `\.summon\s+(npc|monster)\s+(.+)`. When the cursor is at the end of the line, the Tab key asks the Java provider for names matching the prefix, replaces the text, and cycles through candidates on subsequent presses; the operation is recorded for undo.

No autocompletion logic, candidate provider, or Tab-replacement is present in `ChatterUI.cpp` or in the 1.68 client's input controls. The native code has local commands and macros, but the user has to type their text manually. The Java code therefore adds GM-specific command assistance, with Tab and candidate-cycling behavior absent from the original client.

## 146. Global keyboard macros: configurable registry absent from Java

The 1.68 client installs a registry of global macros (`Custom.gMacro`) and lets the launcher reassign the keys for several actions. The default shortcuts notably cover inventory (`Ctrl+I`), character (`Ctrl+S`), attack mode (`Ctrl+C`), chat (`Ctrl+L`), group (`Ctrl+G`), spells (`Ctrl+P`), macros (`Ctrl+M`), options (`Ctrl+O`), map (`Ctrl+W`), trade (`Ctrl+T`), screenshot (`Ctrl+H`), chat resizing (`Ctrl+A`) and item identification (`Ctrl+V`). The launcher's configuration can supply the effective keys and enable/disable several of these entries.

The Java code has no `gMacro`, `VKey`, `AddNewMacro` registry or equivalent in the game path. Its keys are handled locally by the LibGDX screens and handlers; they do not form a centralized, reconfigurable shortcut table. There is therefore no functional equivalent for globally remapping an action, for keeping this launcher configuration, or for guaranteeing that the same combination opens the same window regardless of the current screen.

## 213. Native `!` diagnostic commands absent from Java chat

In addition to the dotted visual test commands, `main2.cpp` locally handles several `!`-prefixed commands: `!AFK` manages the away state and message, `!POS` enables position display on the map, `!Clear` empties the backscroll, `!FPS` toggles the frame-counter display, and `!Pvp stat` shows the PvP ranking. These commands are consumed by the client and do not become ordinary game messages.

The Java code has no equivalent `!` parser in `GameChat` or `GmCommandProcessor`: the latter only intercepts lines starting with `.`. The Java code therefore cannot locally toggle the native AFK, position/FPS display, or clear the backscroll with the same commands; its coordinate/debug overlays and its stats are triggered by other controls and do not follow the same syntax or the same persistent state.

## 212. Different local GM command set

The Java `GmCommandProcessor` intercepts any line starting with a dot and locally executes commands such as `.level`, `.xp`, `.gold`, `.hp`, `.mana`, attribute/point changes, `.teleport X,Y,Z`, `.learn`, `.repair`, `.collision`/`.noclip` and `.speed`. These commands directly modify `Player`, the inventory, skills, position or collision, then display a local message; none of them go through any server request.

The native 1.68 client has no such general GM parser. Its local commands, documented in `ChatterUI`, are limited to visual tests (`.fog`, `.rain`, `.snow`, `.star`, `.dagger`, `.spell`) and do not change the character's levels, gold, points, skills, repair, or persisted position. An identical GM string therefore does not have the same scope: the Java code exposes a local gameplay administration console that the original client does not provide.

## 147. Actually wired shortcuts: several combinations do not trigger the same action

The native table by default binds `Ctrl+S` to the character sheet, `Ctrl+T` to trade, `Ctrl+G` to the group, `Ctrl+P` to spells, `Ctrl+O` to options, and `Ctrl+W` to the map. The Java code, by contrast, wires `Ctrl+T` to `Statistics`, `Ctrl+P` to `SpellBook`, `Ctrl+I` to `Inventory`, `Ctrl+Q` to `QuestScreen` and `Ctrl+W` to the map. It does not expose the native global openings for the character sheet, group, trade or macros under these same shortcuts; `Ctrl+T` in particular is a direct, observable divergence (native trade vs. Java statistics).

The native Win32 loop separately handles `WM_KEYDOWN`, `WM_CHAR`, `WM_UNICHAR`, the Ctrl/Shift state, and system messages; `F10` is explicitly neutralized, and keys pass through the macro registry before actions are dispatched. The Java code dispatches LibGDX events to `GuiManager`, `GameInputHandler` and the screen handlers, with local debug/reload functions (`F1`, `F2`, `F3`, `F8`, `F9`, `F11`) that are not part of the 1.68 client's player-facing behavior. The same key can therefore open a UI, trigger a dev tool, or do nothing, depending on the client.

Finally, the Java code uses `S`/`W`/`A`/`D` and the arrow keys for movement in `GameInputHandler`, while the native code reserves these Ctrl combinations for macros and keeps a distinct keyboard state. With no equivalent macro-priority layer, a remapped combination can simultaneously remain visible as a movement key and not follow the native client's event locking/consumption.

## 148. Losing focus and Alt-Tab: device reacquisition absent from Java

On leaving and returning from Alt-Tab, the native client handles `WM_ACTIVATE`: it releases and then reacquires DirectInput keyboard and mouse, restores DirectDraw surfaces in fullscreen, resets the Ctrl state, and updates the application's focus state. This sequence prevents a key from staying stuck as "pressed" and allows rendering to resume after a surface loss.

The Java code has no such sequence in `MainGameScreen`: `pause()` is empty, `resume()` reacquires no device, and no surface restoration or global modifier reset is performed. LibGDX manages the window, but this does not reproduce the 1.68 client's state transitions. After a focus loss, movement, the Ctrl/Shift keys, or rendering can therefore resume in a different state.

## 149. Mouse-to-grid conversion: native VirtualGrid validation absent

`DirectXInput::SetVirtualGrid` configures a virtual grid derived from the window's size. `GetStatus` converts mouse movement into tile coordinates using the client's historical offsets (`+48`, `-8`) and only returns the tile if `VirtualGrid` allows it; a tile outside the grid or invalid becomes `(0,0)`. A native click is therefore filtered and quantized before reaching UI or world actions.

The Java code mainly converts screen coordinates via `camera.unproject`, then divides by `GRID_W`/`GRID_H` in the handlers. `ObjectClickHandler` then looks for an object position and applies its interaction distance, but does not have the same `VirtualGrid` mask, the same offsets, or the same early rejection of an invalid tile. At the window's edge, over non-walkable ground, or during a zoom/resize, a click can therefore be attributed to a different tile or interaction than in the 1.68 client.

## 150. Mouse event queue: deferred, synchronized handling absent

The native client has `UIMouseEvent`, a global lock-protected queue that separately records `LeftMouseDown`, `LeftMouseUp`, `RightMouseDown`, `RightMouseUp`, wheel and `Drag`, along with each event's position. The mouse thread adds events, and `RootBoxUI` then resolves them in the UI loop; the UI's state can therefore consume, order, or defer an event without directly running the device's code.

The Java code passes LibGDX events straight to the `InputMultiplexer`, screens and handlers (`touchDown`, `touchUp`, `scrolled`) with no central lock-protected `MousePos` queue and no separate resolution phase. A Java click is therefore handled in the immediate context of the frame and the current processor order; there is no native-equivalent contract for queuing a drag/wheel event, replaying it after a window locks, or guaranteeing the same order between the input thread and rendering.

## 152. List scrolling: common native controller absent

`ScrollUI` is a reusable controller in the 1.68 client for skill, chest, channel, user, options and chat-history lists. It has a position bounded by the list size, distinct top/bottom/free-bar regions, a thumb button, thumb dragging, held-button repetition (`pressUp`/`pressDown`/`nextPress`), and a scroll-sound setting. Native lists therefore share the same movement and refresh rules via `ScrollChanged`.

The Java code splits scrolling across `GameChat`, `GuiInventory`, `GuiListScreen`, `ShopScreen`, `OptionsScreen` and other screens, with steps and bounds specific to each class. Some widgets accept the wheel, others use page buttons; the shop's graphical thumb in particular is a button with no drag action. No common game controller simultaneously reproduces up/down clicks, held-repeat, bar dragging, the native clamp, and the optional sound. The same wheel, hold, or drag interaction therefore does not produce uniform 1.68 behavior across the different Java windows.

## 151. Critical errors: native modal box and flow interruption absent

The native client centralizes critical errors in `AppManagement::SetError` and `WarningBox`. It restores the DirectX view before calling a foreground Windows `MessageBox`; some loading or device errors then trigger an exception/close, instead of letting the world continue with a partially initialized state. Resource errors therefore have a determined exit point and user presentation.

The Java code mixes `GameException`s, logging, on-screen messages, and silent `catch` blocks. Several components (`GameChat`, `GuiWorldMap`, `PlayerStateStore`, `GuiPlayerPart`, resource managers) swallow the error or use a local fallback, while other screens interrupt their loading. There is no global critical modal box that restores the view, blocks input, and enforces closing/reconnecting under the same policy; a faulty resource can therefore produce an incomplete screen or a still-active session where the 1.68 client would have stopped the flow.

## 153. Font profiles and fallback: native selection not reproduced

The native client creates separate profiles for `Tahoma` (system), `Verdana` (information), `T4C BeaulieuxV2` (main text and buttons) and `Arial` (new UI), then applies distinct sizes to the menu, descriptions, skills, system messages and buttons. The `bHighFont` option changes these profiles' valid sizes; the metrics are then used by `FormatText` to compute line wrapping and UI heights.

The Java code mainly uses `T4CBeaulieu`, `JetBrains Mono`, `Verdana`, `Tahoma`, `HATTEN` and `Chewy`, with different fallbacks depending on the environment. `FontManager` generates FreeType glyphs with supersampling and a texture filter, but does not keep the same native profiles (notably `T4C BeaulieuxV2`/Arial), nor the `bHighFont` selection per family and size. Text measurements, line wrapping and button/dialogue height can therefore change even when the displayed string and logical size look identical.

## 154. Sprite palette resolution: generic V2 algorithm absent

`CV2PalManager::GetPal` loads the compressed `V2ColorI.dpd` database, looks up the best entry by sprite ID and palette number, distinguishes one- and two-digit numbers, and keeps a reference-palette fallback. `CV2Sprite` then passes this palette to the sprite's decompression, applies the transparent color, and converts each pixel index to a surface color. The same V2 resource can therefore change appearance from a simple palette number with no separate sprite file.

The Java code mainly resolves PNG names/mappings and has no generic manager equivalent to `GetPal(spriteId, paletteNumber)`. `SpriteLoader` only regenerates a few specialized variants (for example energy-ball palettes) or masked regions; the rest depends on already-colorized files, suffixes and overrides. Native palette changes, their fallback, and their cache invalidation are therefore not reproduced for the full set of 1.68 sprites.

## 155. V2 effect flags: only partial Java equivalents

Native rendering carries effects in `V2SPRITEFX.dwFX` on every blit: horizontal mirror, no correction, clipping, outline, dithering, fullscreen adjustment, and `FX_NODRAW`. `DrawSpriteNSemiTrans` additionally receives an explicit alpha level; the `TransAlphaImproved` and `TransAlphaGlow` paths implement semi-transparency and glows with neighboring-pixel detection. These flags are used by windows (`FX_NOCORRECTION`) just as much as by shadows, life bars, stacked items and selected effects.

The Java code has targeted equivalents for flipping, hover outlines, occlusion alpha and some spell masks, but not a generic V2 effects object propagated by every sprite. No common path reproduces the `FX_DITHER`, `FX_NOCORRECTION`, `FX_FIT2SCREEN` and `FX_NODRAW` flags, nor the native neighbor-based glow/semi-transparency algorithms; LibGDX alpha remains a general RGBA blend. Shadows, corrected UIs, partially masked sprites and light effects can therefore look different, be cropped differently, or remain visible where a native draw would have been suppressed.

## 156. Global fade transition: local Java overlay instead of native palette state

The native client has `PalManagement`, which protects the fade state with a critical section, exposes `FadeToBlack`, `FadeOut` and `inFadeOut`, and modifies the visible palette down to black. World transitions use this shared state via `World.SetFading`/`World.RealFading`: the fade is therefore not just a widget — it can lock the visual flow during a map change and be queried by the main loop.

The Java code has a time-limited fade in `GuiMapZoneDisplay` and a few local overlays, but no global palette/transition manager with an `inFadeOut` state, locking, and coordination with the world change. A map change, a reconnect, or a forced transition can therefore show the new state directly or with a screen-specific delay, with no guarantee of the same moment of masking, control unlocking, or render resumption as the 1.68 client.

## 157. Buff expiration: native under-15-second blinking absent

The native `EffectStatusUI` keeps, for each effect, an ID, an initial duration, a deadline, an icon and a description. When at most 15 seconds remain, the icon is hidden during the first 300 milliseconds of every second; the gauge keeps decreasing in parallel, and expired effects are removed by `CalcEffectInfo`. Infinite effects follow a separate path with no deadline.

The Java code does keep `ActiveBuff`s, shows an icon and a duration bar in `PlayerHUD`, and handles infinite buffs. However, `renderActiveBuffs` draws the icon every frame with no native under-15-second blink phase. Readability and the urgency signal before expiration therefore differ, even when the buff's duration and icon are correctly available.

## 158. Widget animation: native draw-driven advance vs. Java duration

The native `AnimUI` stores a list of `GraphUI`, draws `frames[currentFrame]`, then increments the index on every `Draw` call; at the end of the list it wraps back to zero. `Stop` explicitly resets the index to 0, so the animation depends directly on the 1.68 render loop's effective cadence (with the frame's requested alpha, if any). This mechanism is used by UI sequences made of successive sprites.

The Java `GuiAnimatedSprite`, by contrast, advances its frames according to a frame time (`frameTime`) accumulated in `update`, independent of how many draws actually happened. A pause, a slowdown, or an FPS change therefore does not have the same impact: the native code can slow down or speed up the animation through its draw calls, whereas Java tries to preserve a wall-clock duration. UI sequences cannot be considered identical without a shared cadence table and clock mode.

## 159. Adjustment sliders: native step and arrows absent from `GuiSlider`

The native `SliderUI` works over an integer range (`minRange`/`maxRange`) with a `step`. A click in the track rounds the position to the step, dragging preserves this quantization, and the left/right arrows move by one step and clamp the value; every change notifies an `EventVisitor` and can play a press/release sound. The slider can therefore be used both by dragging and by discrete increment.

The Java `GuiSlider` converts the position directly into a continuous, normalized `[0,1]` value. It has no integer range, no step, no arrow buttons, no change-conditioned notification, and no control sounds. Java settings can therefore take intermediate values or fail to offer the 1.68 client's keyboard/click increments, even when the width and thumb look identical.

## 160. Repeated interaction with the same NPC: native 5-second delay absent

In `MouseAction::Talking`, the native client remembers `TalkToID` and `TalkTime`. Clicking the same NPC does not immediately resend a new request: the player must switch target or wait more than 5 seconds; identification and distance lookup are protected by the object list's lock before sending. This delay limits dialogue repeats caused by clicks/double-clicks.

The Java code routes the click directly to `NPCManager.onClick`/`handleDialogClick` and has no global `TalkToID` + 5-second-delay guard in `NPCInputHandler`. Per-quest controls can have their own timers, but they do not replace this shared protection: a repeated click on the same NPC can therefore reopen or advance a dialogue more often than in the 1.68 client.

## 161. Sending movement: native 500 ms guard before a new direction

In `TFCSocket`, after sending a direction, the native client remembers `Try` and only allows a new movement request after more than 500 ms, plus the `!Move` and `!NeedRedraw` conditions. Each direction (the eight `RQ_Move...`) resets this clock. This guard separates the network send cadence from local animation and avoids stacking requests while a move is still being processed.

The Java code advances `PlayerMovement` with LibGDX's `delta` and step reservations; `GameInputHandler`/`ClickToMoveHandler` have no 500 ms `Try` network guard, since there is no T4C movement request to wait for. A held key or a rapid direction change can therefore produce a continuous local cadence, with step and blocking transitions different from the 1.68 client's order sequence.

## 162. Global name availability: native server request absent

In the `TFC_CHOOSE_NAME` state, native client validation sends packet 90 (`RQ_QueryNameExistence`, commented `RQ_ChooseName` in `TFCSocket.cpp`) with the entered name. This request has its own wait profile (1000 ms, three attempts) in `Comm.cpp`: the name being absent from the local roster is not enough, because the shared state of other accounts is decided by the server.

The Java code in `LocalCharacterStore`/`CharacterCreationRules` normalizes the name, checks its syntax, and compares it only against the characters in the local JSON file. There is no global existence request and no cross-install conflict response: two accounts can therefore both locally accept the same name, where the 1.68 client would wait for the server response before continuing creation.

## 163. Startup chain: logo, intro, title and credits states absent

`TFCFlag.h` and the `main2.cpp` loop define a distinct state chain: connection, intro line, logo, splash, introduction, title screen, menu, character selection, warnings, and credits. `TFCSocket.cpp` advances these states according to timers, keys, and connection responses; the menu can go back to character selection, the intro, or the credits before entering `TFC_PLAY`.

The Java code has no equivalent screens for the logo/splash, intro, title or credits. Startup goes directly to local selection (`CharacterSelectionScreen`), then `CharacterLoadingScreen`/`MainGameScreen`. Missing, therefore, are the timed transitions, keyboard interruptions, connection warnings, and returns to the menu that are part of the 1.68 client's functional flow, even where some selection images or buttons were reused.

## 164. Early-game contextual hints: `EventHelp` flags absent

In addition to the paginated manual, the native client keeps a lock-protected global `g_EventHelp` state. It separately enables or disables the "buy a potion", "no money", "HP", "buy a torch", "stats" and "skills" hints; the `OutSide` state also depends on the world, level, and entering an outdoor or underground zone. These flags are changed after status updates and certain music/world transitions, then consumed by the help display. They therefore let a hint be shown only at the right moment and turned off once the beginner has progressed.

The Java code has no equivalent global registry, no access lock, and no hint consumer conditioned on level, gold, HP, torches and zone. NPC text and Java messages can inform the player, but they do not reproduce the "hint pending → hint shown → flag consumed" transitions nor the coordination with the 1.68 client's outdoor state.

## 165. Visual composition refresh: `RQ_QueryPuppetInfo` absent

The native client sends packet 68 (`RQ_QueryPuppetInfo`) when it needs to get or refresh a unit's composed appearance. The `SetPuppet` response fills in the character's equipment fields (body, feet, gloves, helmet, legs, weapons and cape), and `Puppet::SetPuppet` then recomputes the layers, masked parts, special wings/capes and the matching sprites. This request lets the appearance be corrected after a remote update, even if the unit already existed in `VisualObjectList`.

The Java code applies its appearances from the inventory and local definitions (`PlayerAppearanceDefaults`, `PlayerAnimations`, `BodyPart`) and has neither packet 68, nor a received `PuppetInfo` buffer, nor a server re-query for a unit. An equipment, cape, wings or composition change made elsewhere therefore cannot force the same refresh; the local appearance can stay stale or be recomposed under different Java rules.

## 166. Incomplete player state: faith, resistances, weight and native permissions absent

`TFCPlayer` does not just store the displayed attributes: it also has `Faith`/`MaxFaith`, `AC`, `Weight`/`MaxWeight`, the eight attributes including agility and luck, death/kill/PvP counters, `Power` and `Resist` per element, and `CanRunScripts` and `CanSlayUsers`. These fields are updated by status messages and serve as context for the UI, restrictions and allowed actions; they are distinct from simple temporary bonuses.

The Java `Stats`/`PlayerStateDto` model contains no faith, weight, agility, luck, AC, elemental powers/resistances, or PvP counters. The resistances and attributes found in `Player` are only buff contributions, and Java GM commands do not constitute per-character received permissions. A 1.68 status response carrying these values therefore cannot be represented faithfully: HUD, prerequisites, overload, elemental damage, and administratively forbidden actions can diverge.

## 167. Double-clicking narrative items: openable letters and maps absent

`InventoryUI::InventoryGridEvent::LeftDblClicked` has a special path before generic use. Owain's and Crimsonscale's letters are intercepted, as is the maze map; the client restores the drag and opens `RTHelp` in the appropriate page mode. It also walks `m_vImageDisplay`: any item whose name matches a known narrative image opens `RTHelp::ShowSpecial` with that image. The item can therefore be viewed without being consumed like a potion or sent through ordinary use.

The Java code has definitions and translations for these items, but `Inventory` delegates double-click to `ItemUseService`, which handles consumables/spells and charges; no equivalent of `ShowSpecial`, a narrative-image table, or a modal reading window was found. A letter, a map, or another illustrated item therefore does not trigger the same special display and can be treated as a generic item, have no action at all, or follow a different consumption rule.

## 168. Local profile version validation: native header absent from Java JSON

`CSaveGame::bLoad` expects the exact binary header `BL_V2SAVEGAME_V004` before reading, in a defined order, the UI inventory, the three macro families, channels, the ignore list, the chest, options, and the ten layers of revealed map. If the header does not match, the client flags `InvalideSaveGame` and frees the collections instead of interpreting the content as a valid profile.

`PlayerStateStore.load` reads the JSON directly and deserializes it with Gson, with no schema number, signature, or equivalent structure validation. Missing fields are accepted with their default values, and extra fields are ignored; an old, partially written, or other-version file can therefore be loaded as a usable state instead of being rejected and reset like the 1.68 profile.

## 169. Narrative item `__OBJ_DARK_STONE`: native ID with no Java definition

The 1.68 server's item list declares `__OBJ_DARK_STONE` with ID 41839. The client can therefore receive this instance in the inventory and treat it as a server item, notably in the skeleton/Gluriurl narrative sequence; the original scripts distinguish it from the Heartstone and check for its presence by item ID.

The Java code contains the translations and several quest texts mentioning the dark stone, but no `ItemDefinition`/`ItemRegistry` entry for `dark_stone` or ID 41839 was found. A reward, drop, or quest check that actually supplies this item therefore cannot be resolved by the Java inventory as a normal item: it risks being ignored, shown with no definition, or failing to be consumed/removed correctly.

## 170. Unit attack and parry sounds: native per-family table not reproduced

The native client associates attack, hit, death and parry sounds (`onAttack`, `onAttacked`, `onKilled`, `onParry`) with each `Object3D` family, then copies these waves into the object on initialization. The Beholder, Goblin, Mummy, Demon, Minotaur, Rat, Spider and Skeleton families, among others, have specific tables; the hit sound can therefore differ from the attack or death sound, and a parry has its own separate audio event.

The Java code has `soundAttack`, `soundDeath` and `soundHit` in `MonsterDef`/`BaseMonster`, and `BaseMonster.takeDamage` does play `soundHit` for non-lethal damage. However, this value is a single sound configured per definition, with no per-family `onAttack/onAttacked/onKilled/onParry` table; no equivalent call to `onParry` was found in the Java combat path. Hits can therefore produce a sound, but the native variants and the dedicated parry sound are not reproduced.

## 171. Player hit and death sounds absent from the Java path

In the native client, the player model loaded by `VisualObjectList` (case 71, `LoadBodyPart`) explicitly ties attack, hit and death sounds to the unit's states: `Male Hit 1/2`, `Male Dying 1/2`, as well as the `Female Hit 1/2` and `Female Dying 1/2` variants. These sounds are registered in `Object3DSound[71]` along with the attack sounds.

In Java, `Player.attack()` does play `Whooshh 1/2/3` or `Bow Attack.wav`, but `Player.takeDamage()` triggers no hit sound. `handleDeath()` only triggers the death callback or the local respawn; no playback of the male/female hit or death variants is present in the `Player` path.

Impact: the Java player stays silent when taking damage and when dying, whereas the 1.68 client picks these sounds based on the visual model/sex.

## 172. Native animated action cursors reduced to a single Java icon

On startup, the native client loads dedicated sequences for `AttackCursor00..08`, `64kCursorBow-a..k`, `TakeCursor00..10` and `TalkCursor00/01`. `CombatCursor` selects these animations according to the action (melee attack, bow, pickup or dialogue) and the target context; the cursor is therefore a visual state of the current action, not just a still image.

The Java code exposes a static attack cursor and a `GameCursorManager` that partially animates the bow and dialogue cursors. However, no equivalent `TakeCursor` sequence was found, and melee attack has no native `AttackCursor00..08` sequence. The Java manager also does not tie these cursors to the native grid of categories and hostile state; pointer feedback therefore remains incomplete and can pick a different action in `GET`, attack, and unit-turned-hostile contexts.

## 173. Corpse handling: native sound and removal delay absent

When a unit dies, `VisualObjectList` keeps the `Killed` state, records `KillTimer`, and blocks its directions. After at least 500 ms, and only once its movement queue is empty, the client plays the death sound and then converts the unit to its corpse type. For the relevant monster types, `CurrentCorpseFrame` is then advanced through the corpse frames; the object is only marked removable after about 5 seconds (`KillType`/`DeleteMe`).

The Java code plays the monster's death animation, with a hardcoded one-second duration, and `shouldRemoveAfterDeath()` allows removal as soon as this animation ends for a monster with no respawn. It keeps no native `Killed` state with a wait on the movement queue, deferred conversion to a corpse type, corpse-frame progression, or a uniform five-second delay. The corpse's visible duration and the timing of the death sound can therefore differ noticeably.

## 174. Logical invisibility not applied to Java player rendering

The native client transmits the `bInvisible` state in `TFCObject`, and the `Icon3D` render paths then add `FX_NODRAW`: the unit is effectively not drawn, while remaining present in the object list. This is not just a combat or detection rule.

The Java code does have `Player.hidden` and `isHidden()` for combat profiles and dispelling on movement, but `Player.render`, the shader `render`, and `renderOcclusionReveal` call `PlayerAnimations` directly with no test of this state. An active invisibility can therefore keep showing the player (and its occlusion rendering), whereas the 1.68 client hides it graphically.

## 175. Dynamic invisibility of remote NPCs/monsters not represented

In the native client, `bInvisible` is a `TFCObject` field, so it applies to any received unit: player, NPC, or monster. Unit packets can change this state after creation, and the engine passes it to the renderer with `FX_NODRAW`.

The Java code has no dynamic invisibility field in `BaseNPC`, `ScriptedNpc` or `BaseMonster`, nor a matching check in their render methods. The many Java definitions using `@invisible` describe a sprite absent from spawn onward; they do not replace a normally visible unit that becomes invisible and then visible again on a server notification. This remote state change therefore cannot be reproduced for NPCs/monsters.

## 176. Local stealth reveal on movement not present in the native client

Java's `Player.tickSneakUpkeep()` checks on every update whether the hidden player is moving, counts witnesses, and locally calls `StealthRules.staysHidden(...)`. A move can therefore immediately reveal the player based on a local roll, with no network response.

In the 1.68 client, `bInvisible` is a state received/updated on the unit; rendering applies or removes `FX_NODRAW`, but the client does not itself decide stealth success from movement and a witness count. The Java code can therefore reveal a player the native server would have kept hidden, or keep the state despite a missing server update, on top of the rendering issue described above.

## 177. Spell child-effect identity and lifecycle not reproduced

For `RQ_SpellEffect`, the native client reads `spellEffectId` and `spellChildId`, creates the visual object with these IDs, then uses `Follow` and `SummonID` to link the projectile, the impact, and the child effect. `Packet.cpp` contains an explicit table of variants (colored orbs, meteors, lightning, heals, curses, etc.) mapped to their child/base animation. These IDs let simultaneous effects be told apart and be moved or replaced according to the server response.

The Java code creates `SpellProjectile`s and `SpellImpact`s with the visual name and an `onImpact` callback. `SummonRequest`s are used to create game entities, but no effect/child-effect ID received from the server is kept in `SpellRenderer`, and there is no `spellEffectId`/`spellChildId` linking table to update or remove an already-displayed effect. Two visually identical effects can therefore be merged, ended at the wrong time, or remain independent where a native response would treat them as parent/child.

## 178. Model-dependent vertical correction for names/bubbles absent

The native client initializes `TFCObject::TextCorrection` with different values depending on the visual type (`-20`, `-40`, `-80`, `-90`, `-120`, etc.). `VisualObjectList` then adds this correction to the position of the name and speech text; it therefore compensates for the sprite's real height, including for 3D models and monster families.

The Java code uses generic offsets in `NameRenderer`, `NPCAnimations` and the entity renderers, with no equivalent table indexed by visual type and no `TextCorrection` field received/initialized per unit. The same name or bubble therefore sits too high or too low depending on the sprite's size, whereas the 1.68 client adjusts this position per model.

## 179. Guild name and dynamic unit colors absent from Java rendering

The native client receives a unit's visual identity with several separate attributes: `SetName(..., color)` updates the name and its color, while `SetGuildName(..., color)` updates the guild name and its color. `TFCObject` keeps `NameColor`, `GuildName` and `GuildColor`, and `DrawName` then draws the guild name with a separate text object above or below the unit's name.

In the current Java code, `NameRenderer` uses a fixed name color, and entities only expose a localized/static display name. No `GuildName`/`GuildColor` state per unit, nor an equivalent update, exists in the player, NPC and monster render path. The Java client therefore cannot reproduce a remote name change, the guild label above a unit, or per-unit identity colors.

## 180. Double-click target lock absent

In `MouseAction.cpp`, when the native `bLockTarget` option is on, a double-click in combat calls `Objects.Lock(42)` and remembers the cell in `FreezeID`. Subsequent clicks and moves then reuse this target instead of freely recomputing the target under the cursor; the lock is also cancelled by the combat transitions the client provides for.

The Java code has temporary targets for spells and UI selections, but no equivalent option, no `FreezeID`/locked-target state, and no double-click branch that fixes the combat target. Moving the cursor or a new interaction can therefore change the Java target where the 1.68 client keeps the locked one.

## 181. Native selection by occupied cell and item stacking differs from the Java hit-test

The native client rebuilds an ID grid on every display (`VisualObjectList::IdentifyAll`). Every cell covered by an object receives its `ID` according to stacking order; `GridID(x2,y2)` is then the sole source of truth for talking, seeing, using, picking up, or attacking. `Objects.Identify` and `Objects.NoIdentify` modify this grid state, so the same cell can select the visually front-most object rather than an arbitrary rectangle.

The Java code mainly tests `isMouseOver` against entity rectangles/bounds, then walks the manager lists (`NameableEntityHandler`, `NPCManager`, `MonsterInputHandler`, `GroundItemManager`). There is no shared visual ID grid arbitrating every category and layer before dispatching an action. A click on an area where player, NPC, monster, item and decor overlap can therefore select a different entity, or trigger a different action, than the 1.68 client.

## 182. Extended position of multi-cell objects not kept by Java

The native `See()` mode computes the offset between the cursor and the world origin, tries several neighboring cells with `Objects::RealPos`, and then records the result via `Objects::SetExtended`. Identifying an object therefore does not just provide its ID: it also keeps the part of the visual footprint actually pointed at, information reused by the `USE`, `GET` and `USE_ONSITE` actions.

The Java code has local bounds (`isMouseOver`) for NPCs, monsters, drops and grass, but no per-object extended position and no equivalent of `RealPos`/`SetExtended`. An interaction on a decoration or an object spanning several tiles therefore cannot carry the same sub-position: the Java code treats the whole rectangle as a uniform area or just picks the entity from its list.

## 183. Arrow server result and miss trajectory not reproduced

The native client does not infer a shot's result from the attack animation. `RQ_ArrowHit` supplies the shooter, the target, and the new HP percentage; `ShootArrow` then updates the target's HP, computes the shooter's direction, and moves the projectile to the target. `RQ_ArrowMiss`, conversely, supplies a final position and a collision flag; `ShootArrow` extrapolates the arrow off-screen when it misses with no collision, or has it stop in place when it hits an obstacle.

The Java code has a bow-attack animation and local formulas (`CombatResult`, `CombatMath`), but no distinct `ArrowHit`/`ArrowMiss` cycle with a final position, collision, received HP percentage, and extrapolated trajectory. Rendering can therefore play a bow animation without reproducing the native shot's impact point, off-screen miss, or server update.

## 184. Dynamic transition of a unit to hostile state and cursor change absent

In `VisualObjectList::SetEvil`, when an offensive spell hits the player, the native client looks up the attacking unit, dynamically converts it to the monster category (`Friendly = VOL_MONSTER`) if needed, assigns it the hostile cursor, and rebuilds its identification grid. `SetAttack` applies a similar transition when the unit attacks the player. The visual state and interaction type can therefore change without recreating the unit.

The Java code has local `isHostile` flags for some NPCs and a static monster classification, but no dynamic hostility state shared across all units, no category conversion, and no cursor change triggered by a received attack. A unit initially neutral or player-aligned therefore does not automatically change identification behavior the way it does in the 1.68 client.

## 185. World-object contextual cursors not mapped in Java

The native client has a `DefaultMouseCursor` table per object group: a door or a chest gets `USE`, an NPC gets `TALK`, a pickupable item gets `GET`, a non-interactive chair or crate gets `NONE`, and these values are recomputed when the object changes type or state. The displayed cursor therefore depends directly on the object under the cell and announces the action the click will dispatch.

The Java `GameCursorManager` only offers the global default, attack, spell, bow and dialogue modes. `ObjectClickHandler` handles objects on click, but no object → `GET`/`USE`/`NONE` mapping is applied to the pointer. A door, a chest, a drop, or a non-interactive decoration can therefore show the same general cursor before the click, unlike the 1.68 client's contextual feedback.

## 186. Cursor drawn in the native scene vs. Java system cursor

`CMouseCursor::DrawCursor` renders the cursor sprite directly into the DirectX surface, with a clip region sized to the window, zoom-related offset corrections, and a direction calculation for forced cursors. The native cursor therefore follows the game's own render coordinates and is subject to the same transforms as UI sprites.

The Java `GameCursorManager`, by contrast, builds a LibGDX `Cursor` object (`Gdx.graphics.newCursor`) and hands it to the system via `Gdx.graphics.setCursor`. There is no cursor drawing in the `SpriteBatch`, nor an equivalent scene clip/offset. Hotspot, DPI scaling, moving outside the window, and positioning relative to a zoom level can therefore differ from the 1.68 client even with the same source image.

## 187. Native movement/attack event queue vs. Java animation with no queuing

The native `TFCObject` has a `MovingQueue<Deplacement>`. `SetAttack` and movement updates add received events to it; `VisualObjectList`'s loop removes them in order, checks the number of pending items, and only removes the unit once the queue is empty. Several attacks or position corrections arriving between two frames are therefore kept and played sequentially.

In the Java code, `PlayerAnimations` keeps a single `attacking`/`attackFrame` state, and `BaseMonster` only starts an attack if `!animations.isAttacking()`. A new attack or animation received while the previous one is playing is not placed in a per-unit queue; it is delayed by the cooldown or ignored by the animation guard. Under a burst of events, the order and number of visible poses therefore diverge from the 1.68 client.

## 188. Server-forced attack direction absent

`VisualObjectList::SetAttack` receives a `forcedDirection` parameter. When it is non-zero, it replaces the direction computed between the attacker and the defender before the attack is added to `MovingQueue`. The native client can therefore display an attack in a server-imposed direction, even if the target's local position would give a different orientation.

The Java code calls `PlayerAnimations.startAttack` with the movement's current angle, and `BaseMonster`/`MonsterManager` orient the unit via `faceToward` from local positions. No forced-direction field or event is passed to the animation path. A server correction, an attack with no target, or an imposed direction therefore produces a different pose in the Java code.

## 189. Native immediate orientation and a different direction convention

The native `SetDirection` works on grid offsets `OX/OY`, produces numeric directions 1 through 9, and accepts `bSetNow`: the call can either just compute the direction intended for a queued attack, or immediately change `Object->Direction`. When both offsets coincide, the native code explicitly falls back to direction 1.

The Java code keeps textual angles (`000`, `045`, `090`, `135`, etc.) with a mirror boolean, and `faceToward` does nothing when both positions are identical. It has no equivalent distinction between a direction computed for a future action and one applied immediately. Cases of a target on the same cell, an offset correction, or a targetless attack event therefore do not end up with the same orientation.

## 190. Attachment references and deferred removal of visual objects absent

The native client keeps `TFCObject::Count` on the parent object whenever an effect, a summon, or another visual object is added with `AttachID`. Removing the parent is then deferred (`DeleteMe`) as long as this counter has not returned to zero; on each child's destruction, the counter is decremented. This rule also applies to off-screen and moving objects, so as to avoid removing the support before its attached visuals.

The Java code splits `SpellImpact`, `SpellProjectile` and `ChannelEffect` into render lists that expire independently by their frames, duration, or handle. `GroundItem` has no `AttachID` and no visual reference counter. There is therefore no generic guard preventing a parent unit or object from disappearing while a native visual child is still attached to it; depending on the timing of expiration, an effect can be left orphaned or disappear with its support, instead of following the 1.68 client's coordinated lifecycle.

## 191. Server weather and environment particles absent from the Java runtime

The native client handles `RQ_WeatherMsg` with a separate effect and `ON/OFF` state: rain, snow and fog are remembered distinctly. `Tileset` initializes or stops the transition states, and `CWeather::DrawRain` and `DrawSnow` then draw drop/flake positions, reuse them between frames, and sometimes add lightning depending on intensity. Weather received from the server is therefore global state rendered on top of the scene, not just a local graphics option.

In the Java code, no class, state or render loop equivalent to `CWeather`, `RQ_WeatherMsg`, `DrawRain` or `DrawSnow` was found in the game runtime; the `rain`/`snow` occurrences are object or spell names. `GamePreferences` can store settings, but no rain, snow, fog, lightning, or weather transition is generated in `MainGameScreen`/the renderers. Weather activated by the 1.68 server is therefore visually ignored by the Java code.

## 192. Animated terrain water and the native global cadence not reproduced

The native client has six `AnimWater01` frames. `TileSet::DrawWaterLevel` walks the compiled cells, recognizes animated terrain and water-edge families, then draws each tile with the current global frame. This frame advances every three `DrawWaterLevel` runs and is reset when the `bAnimatedWater` option changes. The animation therefore concerns the ground texture and its seams, with a cadence shared across the whole scene.

The Java code can recognize that a terrain contains the word `water` and can generate terrain seams, but `GroundRenderer` has no water-frame counter and no periodic selection of the six native variants. The only generic object animation observed in `ObjectRenderer` advances at 120 ms, and `isAmbientAnimation` only recognizes `shop sign-`-type sprites. Java water surfaces therefore remain static or follow a different mapping, and toggling the native option resets no equivalent terrain animation.

## 193. Animated fountain and mill lighting absent from Java rendering

For certain decorations, the native client does not always draw the static sprite. With `bShowAnimDecorsLight`, `BIG_FONTAINE_1`, `BIG_MOULIND` and `BIG_MOULING` add an overlay animation (`AddOverlapAnim`) identified by the object and then rendered by `DrawObjectAnimOverLapID`. The fountain and both mill orientations can therefore produce an animated light layer independent of the base decoration; turning the option off explicitly reverts to the fixed sprite.

The Java code has a `DecorRenderer` and an `ObjectRenderer`, but their decoration path selects a static region, and their only identified generic ambient animation targets `shop sign-` signs. `DayNightCycle` applies a global ambience, with no per-fountain/mill overlay state and no `ShowAnimDecorsLight` option. These Java decorations therefore do not reproduce the native animated lighting and do not switch between the two paths according to the 1.68 setting.

## 194. Native keyboard macro priority and consumption absent

In `TFC_PLAY`, the native client builds a composite key from the key, Ctrl and Shift, then tries system macros (`Custom.gMacro`) and user macros (`MacroUI`) in turn. If either succeeds, the event is consumed and `RootBoxUI::VKeyInput` never receives the key; only unrecognized keys then reach normal handling. `MacroHandler::CallMacro` can also be globally disabled and refuses the call while `DoNotMove` is active. A macro can therefore simultaneously block a UI or movement action.

In the Java code, searches in the game path (`MainGameScreen`, `GameInputHandler`, `GuiManager`) show no registry of macros by Ctrl/Shift combination, no consumption step before normal dispatch, and no global `DoNotMove` flag applied to a macro call. Shortcuts are handled by the relevant screens or handlers, and some Shift combinations go straight to movement/selection. A key configured as a native macro can therefore open a different action, reach the UI, or do nothing in the Java code instead of blocking exactly the same event.

## 195. Native PvP stats and ranking screen absent

The 1.68 client handles `RQ_GetPvpRanking` and separately receives total deaths, total kills, current-period deaths and kills, current streak, best streak, and PvP points. `PvpRanking::myPvpStat` then composes several dedicated info messages (`PVP Points`, `Current kills`, `Current serial killing`, `Current deaths`, `Best serial killing`, `Total kills`, `Total deaths`). These values are not inferred from quests or level: they come from the server's PvP ranking/state.

The Java code has no `PvpRanking`, no `TotalKillNumber`/`CurrentPvpPoint` fields, and no equivalent ranking request or screen. Its kill counters found in `QuestService` serve quest progression, and `Stats` keeps skills/points rather than PvP streaks. A native PvP update therefore cannot feed the HUD, messages, or a Java ranking, and a Java death or victory does not produce the same persistent stats.

## 196. Native second cape layer not represented in the Java model

The native `Puppet` distinguishes `PUP_CAPE` and `PUP_CAPE_2` in its `BodyOrder`, `BodyOrderA`, `BodyOrderAR` and `BodyOrderR` tables. The second layer is positioned differently depending on direction, attack and flipping; it notably keeps correct overlap with the body, arms, weapons and shield. It is part of the composition sent/rebuilt by `SetPuppet`, just like the other equipment parts.

The Java code has `BodyPart.CAPE`, but no `CAPE_2` value. In `PuppetBodyOrder`, the index corresponding to `PUP_CAPE_2` is explicitly `null` in `INDEX_TO_BODY_PART`, so the order table cannot render a second cape resource. Java characters using a multi-layer cape, or a variant requiring this part, will therefore have incomplete overlap, silhouette, or cape compared to the 1.68 client.

## 197. Character-part visibility mask not reproduced

Native rendering does not blindly walk every loaded part: each `BodyOrder`, `BodyOrderA`, `BodyOrderAR` or `BodyOrderR` entry is first filtered by `Object->VisiblePart & Pow2(part)`. `Puppet` can therefore separately hide a hand, an arm, a helmet, hair, a robe, a cape, a shield, or a weapon depending on equipment, sex, attack pose, and visual variant, while still keeping the part loaded for other states.

The Java code collects the present parts in `partMap` and draws them according to `PuppetBodyOrder`, but no `VisiblePart` property or per-entity/per-pose bitmask was found in `PlayerAnimations`, `Player`, or `BodyPart`. A configured part therefore stays a rendering candidate even where the native client would disable it for a given piece of equipment or pose; the result can show stacked layers (for example hair/helmet, limbs under a robe, or a secondary weapon) that the 1.68 client hides.

## 198. Native semi-transparent, optional unit shadow not equivalent

The native client has a separate `DrawObjectShadow` pass, enabled only with `bShowNewOmbrage`. It renders `PlayerShadow` and object/unit shadows with `DrawSpriteNSemiTrans`, an explicit transparency level (`dwNiveauTrans = 160`), interpolated `OX/OY + MovX/MovY` coordinates, separate depth-ordering passes, and exceptions for doors, the dead player, and seraph effects.

The Java code loads regions suffixed `Shd` in `EntityAnimationsBase` and draws them directly in the same animation path, with no global `DrawObjectShadow` pass, no `bShowNewOmbrage` setting, no fixed native alpha of 160, and no equivalent type/depth filtering. Java shadows can therefore be absent when the `Shd` resource is missing, stay opaque, or follow a different interpolation, and cannot reproduce the 1.68 client's global old/new toggle.

## 199. Native local lightmap absent from Java rendering

The native engine computes `CurrentLight` and `CurrentLow` from the player's light and every visual object's, with attenuation depending on grid distance. It then merges several maps (`lmPlayerLight`, `lmOtherPlayerLight`, torches, lanterns, beams and candles) via `LightMap::MergeLightMap`, and applies `MakeLightingFX` to the rendered surface. A torch, a lit player, or a light-emitting decoration therefore locally changes neighboring pixels, independent of overall ambient brightness.

The Java code applies a global `DayNightCycle` and a uniform brightness overlay in `MainGameScreen`; no per-zone `LightMap`, no merging of point sources, and no per-object local light property were found in `GroundRenderer`, `ObjectRenderer`, or the entities. Underground maps and scenes with torches, lanterns, lit players, or lighting effects therefore do not produce the same halos or local illumination as the 1.68 client.

## 201. Recovering from focus loss: native input reacquisition absent from Java

On `WM_ACTIVATE`, the 1.68 client explicitly handles leaving and returning from Alt-Tab: it releases the DirectInput mouse and keyboard when the window becomes inactive, then reacquires both devices on return. In fullscreen mode, it also restores the DirectDraw surfaces (`DXDRestoreSurfaceF`/`DXDRestoreSurface`) before resuming display. `WM_SETFOCUS` additionally resets `CTRL_State` to zero so a Ctrl key held before the window change is not left considered pressed.

In `MainGameScreen`, `pause()` is empty and `resume()` only recomputes the HUD camera and calls `hud.recoverAfterDisplayChange()`. No equivalent Java path suspends/resets input state, reacquires a device, restores render surfaces, or resets keyboard modifiers on a focus loss. After Alt-Tab, minimizing, or a graphics context change, the Java code can therefore keep an action/modifier in a different state or resume with invalidated display resources, whereas the native client has a dedicated exit-and-resume sequence.

## 202. Different audio volume curve between DirectSound and LibGDX

The native client stores effects and music volume on a discrete 0-to-10 scale. For a sound loaded in memory, `T3VSBSound::SetVolume` passes DirectSound `-166 * (10 - v)` (hundredths of a decibel), and level zero stops the buffer. In-game playback is also gated by `dwSoundVol`, while streams (`TS_STREAMING`) follow a separate path. Native volume is therefore not a simple linear multiplication of PCM amplitude.

The Java `GamePreferences` keeps `musicVolume` and `effectsVolume` as `[0,1]` floats, then passes these values directly to `Music.setVolume` or `Sound.play`. No remapping of the 0–10 scale to DirectSound attenuation exists, and the native memory/streaming paths are not differentiated by the same state. At a comparable user setting, intermediate levels, the silence threshold, and how a sound loaded in a different buffer type reacts therefore do not produce the same audible result.

## 204. Different default display height (native 1280×800 vs. Java 1280×768)

The 1.68 client's `Global` constructor explicitly calls `SetDisplaySize(1280,800)`. This height then serves as the reference for DirectDraw surfaces, clipping limits, positioning bottom-of-screen elements, loading `..._<ScreenH>` resources, and virtual-grid conversions.

The Java `GameConstants` sets `WINDOW_WIDTH = 1280` but `WINDOW_HEIGHT = 768`, and `MyGame` uses this value in `setWindowedMode`. Even at identical width, the missing 32 pixels change vertical framing, the relative position of the HUD/chat, clickable areas, and any calculation that uses the current height as a base. The Java code therefore does not reproduce the 1.68 client's default display geometry.

## 205. Pixel format and transparency: native 16-bit/RGB565 surfaces vs. Java RGBA8888

`DXDCreate` initializes the 1.68 client's surfaces with `ScreenBPP = 16`. Sprites and effect routines therefore work with 16-bit words, convert colors to RGB565 (5 bits red, 6 bits green, 5 bits blue), and define a precise DirectDraw color key for transparency. Mask, glow, semi-transparency and seam tests compare these quantized values, sometimes against the video surface's `wRMask`, `wGMask` and `wBMask` masks.

The Java code, by contrast, creates the Pixmaps and textures for these paths in `RGBA8888` and relies on OpenGL alpha/blending. The 16-bit color key is not quantized the same way, and a pixel close to the native key can stay visible or become transparent differently; channel blending and semi-transparent gradients also produce different values. Even with the same source images and the same coordinates, pixel-by-pixel rendering therefore cannot be identical in outlines, glows, and object masks.

## 206. Window resizing always forbidden in Java

The 1.68 client has a `bLockResize` setting: border messages (`WM_NCHITTEST`) only block resize regions when this option is on. When it is off, the window can be resized, and the client recomputes its display from the current width/height, with its viewport corrections and resolution-dependent resources.

`MyGame` always calls `config.setResizable(false)`, with no corresponding Java preference or state to allow resizing. The Java code therefore also forbids the native profiles where `bLockResize` is off, and cannot reproduce interactive resizing followed by updating coordinates, the HUD, and click areas.

## 203. Native tile-seam algorithms not equivalent to Java's Tmpl compositing

`Tileset.cpp` contains several dedicated seam-smoothing passes (`Smootage`, `Smootage2`, `Smootage3` and `WaterSmooth`). For each directional variant, they read the surfaces of two terrains and pick the source pixel according to a third surface's mask: some variants test the DirectDraw color key, others test the mask value `0`, `0x7FFF`, or the video card's RGB mask. The native tables cover normal and diagonal directions and distances `X2` through `X5`, with separate tables for water.

The Java code does not go through these tables or these DirectDraw sentinels. `GroundRenderer.renderTmplTile` reads a Pixmap mask, uses its alpha/color byte as an exact key into a terrain, takes the matching pixel or a fallback transparent terrain, and then caches a composed texture. There is no native-equivalent selection by `Smoothing`/`Smoothing2` variant, RGB color-key test, or `WaterSmooth` pass. Terrain seams and water/land transitions can therefore pick a different source at edges, diagonals, and partially transparent masks, even when tile names and dimensions stay identical.

## 200. Native RTMap cache not invalidated on a world change at identical coordinates

In `RTMap::CreateRTMap`, the 1.68 client considers the view already loaded when `m_dwLoadX == xPos*2` and `m_dwLoadY == yPos`; the `m_dwLoadW == World` check is explicitly commented out. If the character switches to a different world while keeping the same coordinates, the function can therefore reuse the previous world's image and mask instead of reloading the map and its exploration memory. The world is only reset when the window is destroyed/cached, or on a move that invalidates this cache.

The Java `GuiWorldMap`, by contrast, includes `world` in its key (`renderedTileX`, `renderedTileY`, `renderedWorld`) and rebuilds the view as soon as the world changes. The result diverges in this specific case: the native code can show a stale view, whereas Java switches immediately to the requested world.

Handling of out-of-range worlds also differs: the native code resets `iWorld` to 0 in `LoadRTWorld`, but `CreateRTMap` returns after clearing the output if `World > 7`; the Java code directly clamps the world to `[0,7]` before loading. An invalid world value therefore does not produce the same fallback map.

## 207. Different first-load synchronization

The native client creates a `FirstInitObject` thread and keeps `g_bFirstLoadComplete` false until this initialization finishes. The loading loop keeps drawing `LOAD<ScreenW>.PCX`, the progress text, and the tip of the day, then explicitly waits on the flag before continuing. This thread initializes the object list (`Objects.Create`) and notably creates the in-memory sounds `Open Box`, `Equip` and `Vampire Dying` before releasing startup.

The Java `LoadingScreen` queues every WAV/MP3/OGG found under the sounds folder and, in parallel, launches `startMapPreloadAsync()`. Moving on to `CharacterSelectionScreen` depends on the `AssetManager` finishing, not on `mapPreloadExecutor` finishing; maps can therefore still be loading after the selection screen and get opened on demand via `getOrLoadMapReader`. The native code blocks its startup step on its own dedicated initialization, whereas Java allows UI progress while a map preload is still incomplete.

## 208. Native text editor much more limited than the Java chat editor

The 1.68 client's `NewInterface/EditUI.cpp` only handles insertion at the cursor, Backspace, DeleteChar, left/right movement, and CTRL word-jumping. `LeftClick` is still a TODO and does not place the cursor in the text. There is no selection anchor, no SHIFT selection, no copy/cut/paste, and no undo; the default limit is 256 characters, and an optional filter only decides whether an incoming character is accepted.

`GameChat.java`, by contrast, implements selection (SHIFT and CTRL+A), clipboard (CTRL+C/X/V), undo (CTRL+Z), HOME/END movement, history, and timed repeat for left/right/Backspace. Pasting replaces line breaks with spaces and respects the limit after removing the selection. Java behavior is therefore functionally richer than the native 1.68 editor; an ergonomics or keyboard-test comparison cannot be considered equivalent even though the 256-character maximum coincides.

## 209. Durability and repair added by Java with no native client equivalent

In `Packet.cpp`, the 1.68 client does not keep a local per-item durability gauge. When an `RQ_GetObject` response flags an item as unusable/broken, it shows the localized message "The object is broken and cannot be used" and then removes the identified object from `VisualObjectList`. Other item errors mark the unit as missing or remove it; no durability value, per-hit wear, or repair screen is computed by this client.

The Java code, by contrast, introduces `ItemDurabilityService` with per-inventory-entry and per-equipped-slot percentages, wear on attack and on death, blocking bonuses/attacks when equipment is broken, a gauge display, and a `RepairScreen` that charges for individual or bulk repair. These rules are therefore an additional Java mechanic: they can break, implicitly unequip, or locally repair an item in situations where the 1.68 client would only have received a server error and removed the item in question, with neither the same economy nor the same instance state.

## 210. Herb gathering added to Java

The 1.68 client's tree contains no herb manager, no harvestable node, no gathering channel, and no gathering request. The client's world interactions concern items, units, chests, spells, and movement; no native class spawns random plants, marks them as harvested, or hands out an item after a channeling time.

The Java code, by contrast, has `HerbManager`, `HerbNode` and `HarvestChannel`. It rolls a definition among several weighted herbs, creates nodes on the map, shows their name on hover/right-click, enforces a channeling duration and a movement tolerance, and remembers harvested tiles for the session. This mechanic and its availability rules are therefore entirely additional to the native 1.68 client; they can display an interaction, consume time, and produce an item where the original client had no harvestable element at all.

## 214. Native chat routing syntaxes absent from the Java path

In `main2.cpp`, the 1.68 client interprets the text's first character before sending the packet: `:message` becomes an `RQ_Shout`, while the short strings `:)` and `:p` remain ordinary messages (with the historical exception of `:k`/`:K`). `/name message` becomes a private page `RQ_Page` if the page option is on; a quoted recipient allows multi-word names, for example `/"First Last" message`. The client also blocks sending a page while in AFK mode and shows local feedback if the recipient is ignored. Finally, `;message` is sent to the currently selected channel via `SendMessageToCurrentChannel`.

The Java `GameChat` hands the full text to a single `submitHandler`. `MainGameScreen` then only routes commands starting with `.` to `GmCommandProcessor`; otherwise it shows the text above the player and possibly forwards it to the active NPC conversation. No equivalent Java parser for `:`, `/`, `;`, quoted recipients, the AFK state, or the ignore list is present in this path. A Java player can therefore produce local/NPC text where the 1.68 client would have changed packet type, channel, and send control.

## 215. Fatal exception handling and crash reporting not equivalent

The native client installs `CExpFltr::Filter` via `SetUnhandledExceptionFilter`. For an access violation, division by zero, stack overflow, illegal instruction, or other Win32 exception, it builds a diagnostic, calls `LogException` (date, address, code/description, and register context), runs the shutdown procedure, and then terminates the process with code 1. The filter also caps the number of faults and has an auto-restart configuration.

The Java runtime has no equivalent installation of `Thread.setDefaultUncaughtExceptionHandler`, no crash report with machine context, no global fault counter, and no configured restart in the game path. Local errors are often caught and ignored, while an uncaught exception depends on the JVM's generic behavior. Diagnostics, the produced file, the moment of closing, and the possibility of resuming after a crash therefore differ from the 1.68 client.

## 216. Encrypted VSB audio bank and chunked loading absent from Java

The native client bundles effects and music in `gamefiles\T4CGameFile.vsb`. `VSBDataBase::LoadIndex` reads an index of IDs, offsets, sizes, sample rate, and bit depth; `MemMapFile::CpyMemory` then decrypts the bytes with a position-dependent XOR table (4096-byte blocks). `LoadChunck` supplies segments to `T3VSBFilter`, which keeps a reference count and only loads the portions the decoder needs. Startup also checks integrity and can rebuild/decompress the VSB bank.

The Java `SoundManager`, by contrast, looks up an individual audio file in `Paths.SOUNDS_DIR`, loads it as a `Sound` or `Music` via LibGDX, and then ignores loading errors. No VSB index reader, positional XOR decryption, chunk cache, audio reference counter, or `T4CGameFile.vsb` check is used by the Java runtime. An install containing only the native bank, a sound ID with no separate file, or a partially corrupted bank therefore does not produce the same loading or fallback behavior.

## 217. Total resolution of unknown icons and sounds vs. Java `null` returns

`GameIcons::operator()` and `GameSounds::operator()` are designed as total functions: an unregistered ID returns a `???` sprite or the generic `Generic Drop Item` sound, respectively. UI and drag-and-drop calls therefore always keep a valid visual/audio object even when a content binding is missing.

The Java `SpriteLoader`, by contrast, returns `null` for a missing name, an out-of-range ID, an empty image, or an unavailable palette; `SoundManager` silently gives up playback if no file can be loaded. Java calls therefore have to test for absence and can drop the drawing, the sound feedback, or the UI element, whereas the 1.68 client always displayed/played its identified fallback. The result of incomplete content data is therefore not equivalent.

## 218. Encrypted language catalogues and native fallback different from Java JSON

`LocalString::LoadAllStrings` opens `English.elng` or `French.elng` in binary, then decrypts each byte with a deterministic pseudo-random table of 7,823 positions. The decoded file is then parsed into `[id]` entries, and a requested language that is missing falls back to English; an index beyond the entry count falls back to the last loaded string. The GUI and help catalogues follow the same indexed-binary-resource principle.

The Java `I18n` reads `assets/i18n/lang.json` directly in UTF-8 with text keys. A missing/empty file throws an exception, a missing key returns the key or the supplied fallback, and there is no decryption, entry-size check, or fallback to the last string. The Java code therefore cannot directly consume the 1.68 client's `.elng` catalogues and does not react the same way to a missing/corrupted language or translation entry. (As with section 40, this is an intentional simplification for this project's English-only scope — see `AGENT.md` — not a gap that needs closing.)

## 219. Different collision policy for icon and sound bindings

In `GameIcons::BindSprite` and `GameSounds::BindSound`, bindings are inserted into a `std::map` with `insert`. If the same numeric ID is bound more than once, the first entry remains the one returned; the sprite/sound object newly created for the colliding binding is destroyed. The native client therefore explicitly uses a "first binding wins" policy.

The Java definitions (`ItemIconDefinitions` and the content registries) rely on static maps/entries and do not reproduce this runtime insertion contract: a key collision during an immutable build can fail at load time, while a mutable map using `put` would replace the previous binding. Depending on the generation/import path, the same duplicated content can therefore be rejected, replace the existing icon, or prevent startup, instead of silently keeping the first binding the way the 1.68 client does.

## 220. Native system timer resolution absent from Java startup

At startup, `main2.cpp` calls `timeGetDevCaps`, then `timeBeginPeriod(caps.wPeriodMin)` to increase the resolution of the Windows timer used by the render loop, maintenance, and cadence waits. The client later ends this period with `timeEndPeriod`. This configuration is layered on top of the internal 17/34 FPS timing and reduces rounding in `Sleep`/`timeGetTime` measurements.

The Java code relies on the LibGDX cadence, `delta`, `System.nanoTime`, and the JVM's own timing services, with no equivalent request to raise the system timer's resolution. Even when the same nominal durations are configured, wake-up granularity, animation jitter, input repetition, and short delays therefore do not necessarily follow the 1.68 client's timing profile.

## 221. Click-to-move algorithm: native greedy pursuit vs. Java A*

`Pf.cpp` does not build a node list or a cost map. `pfSetPosition` only remembers the target tile and the last position; `pfGetNextMovement` compares the signs of `xDif`/`yDif` and directly returns one of the eight directions. If the player has not made the expected step, the function returns 0; a `Force` mode can additionally replace this direction with the cursor's angle, and `pfStopMovement` cancels the pursuit. The 1.68 client therefore follows a target by a greedy choice at every step, with no search for an optimal detour around an obstacle.

The Java code has `Pathfinding.findPath`, a priority queue, `g/f` scores, an octile heuristic, a diagonal cost of `1.4142135`, and neighbor examination until it finds a route. It can therefore compute a detour, compare several paths, and keep heading to the target despite an obstacle, whereas the native client stops or only picks the next axis toward the target. On a map with a wall, a corridor, or a diagonal obstacle, the two clients therefore do not pick the same sequence of tiles, even before considering the network authority and interpolation already described.

## 222. Different diagonal validation and "corner cutting" prevention

In `Pathfinding.java`, any diagonal neighbor is rejected if either of the two adjacent orthogonal tiles is blocked (`sideX/sideY` or `sideX2/sideY2`). The Java path therefore cannot cut diagonally through the corner formed by two obstacles, even if the diagonal tile itself is free; it looks for another route or declares the destination unreachable.

The native `pfGetNextMovement` does not consult the collision map to pick the next step: after comparing the deltas, it directly returns `2`, `4`, `6`, or `8` for a diagonal (and `Force` mode can still impose the cursor's angle). Collision and step acceptance are decided by the server flow/player state, not by this local both-sides test. At a blocked corner, the Java code therefore refuses the intent before sending it, whereas the native code can send the diagonal and then wait for a correction, a refusal, or an unchanged state.

## 223. Native target easing after correction absent from Java movement

The native client has `pfNearPosition`, called by `MouseAction`, `TFCSocket`, and several response-handling branches. This function progressively moves the remembered target (`pfSaveXPosition`/`pfSaveYPosition`) one tile toward the player's actual position before recomputing the next move. The automatic path is thus eased back toward the authoritative position after a delay, a correction, or a movement response, instead of blindly keeping the old destination.

The Java `PlayerMovement` keeps its destination/step reservation and, on a mismatch, clears the active step and recomputes with the current input; it has no equivalent that progressively eases a persistent target toward the corrected position before resuming the path. After a position correction, a held click, or a simulated delayed response, the native and Java clients can therefore resume toward different tiles.

## 224. Non-uniform double-click time windows

The native `GameUI` sets `ClickTime` to 250 ms, and `RootBoxUI` directly compares two `timeGetTime` timestamps to turn the second click into `GWIN_MSG_DBLCLICK`. This same convention feeds the lists, panels, and world actions that receive `DM_DOUBLE_CLICK`; it is independent of the delay the user configured in Windows.

The Java code has no equivalent global constant: `Inventory` recognizes a double-click under 300 ms, while `ClickToMoveHandler` uses 350 ms for the quick bar. Other screens go through LibGDX events or their own handlers. A second click between 250 and 350 ms can therefore be a native double-click but a single click in a Java screen, or trigger a Java use action where the 1.68 client would not have classified it as a double-click.

## 225. Native continuous scroll controls and hold-to-repeat absent from Java lists

The native `ScrollUI` is a controller shared by lists, grids, text, inventory, chest, trade, skills, guild, options, and chat history. Clicking an arrow first moves by one line, then holding repeats the action every 100 ms. Clicking in the track jumps by 5 lines depending on which half was targeted; the thumb can be dragged and converts its position into `linePos`. The bounds are centralized over `0..listSize-1`, and every change calls `ScrollChanged`.

The Java code has no such shared contract: `GuiChat` scrolls the wheel by 3 lines, `GuiInventory` by pixels (`amountY * 24`), `GuiOptionList` by one line, and `GuiListScreen` by pages of six entries. Java trade lists do not expose the same track thumb/drag or the native 5-line jump, and holding an arrow is not handled by a shared 100 ms repeat. At the same cursor position or after a hold, the number of visible lines, the moment of the update, the sound, and the active selection therefore diverge from the 1.68 client.

## 226. Different movement-key matrix and numpad dependency

In `TFCSocket.cpp`, native manual movement reads the raw DirectInput state. The eight directions accept the arrow keys, and also `NumPad 2/4/6/8` when the local `NumLock` computation makes them active; `NumPad 1/3/7/9` and `End/Home/PgDn/PgUp` additionally provide the four diagonals. Before sending, the client refuses movement if Ctrl or Shift is held, if `ChestUI` or `TradeUI` blocks the world, or if `DoNotMove`/`boKeyProcess` forbids it. Each key is tested separately in the loop, after the diagonal combination, with a `GridBlocking` check on the matching offset.

The Java `GameInputHandler` only reads WASD (with Q/Z variants) and the four arrow keys: no numpad key, no Home/End/PgUp/PgDn substitution, and no equivalent dependency on the NumLock state are present. Java filtering mainly relies on `textInputActive`, and does not reproduce the native "Ctrl/Shift blocks movement" combination nor the same `ChestUI`/`TradeUI` and `DoNotMove` states. Pressing a numpad key, Shift, or Ctrl can therefore move the character in a case where the 1.68 client would not have, while several native diagonals produce no Java input at all.

## 227. Right-click grid contextual help not reproduced globally

The native `GridUI::RightMouseUp` handles every brief right-click in the grid: on an empty cell, it calls the grid's help; on an occupied cell, it shows the item's help if `allowHelp` is on and also forwards `RightMouseUp` to the item; outside the bounds, it still shows the grid's help. This resolution is shared by the inventory, chest, trade, and other panel grids, and happens after selection is captured on `RightMouseDown`.

The Java code has no such generic help routing. `GroundItemClickHandler` only shows a drop's name under the cursor, and `ObjectClickHandler` only a mapped object's name; a right-click on an empty cell, outside an object, or on a control with no dedicated handler is consumed with no grid help. The Java code therefore does not systematically forward the right-click to the selected cell nor distinguish grid help from item help the way the 1.68 client does.

## 228. Incompatible user configuration format and location

The native `Global::ReadClientConfig` automatically builds the path `CSIDL_PERSONAL\Rebirth\T4CV2`, creates the directories, and then reads `T4C.dat` in binary. The file contains, in a fixed order and with fixed sizes, the account name, IP address, AFK status, a 2048-byte AFK message, debug/FPS/position flags, `FirstTimeAddon`, and `WebpatchEnable`; `WriteClientConfig` rewrites these same blocks. The client reloads this configuration before initializing language, capture, logs, and the launcher.

The Java code loads `game_preferences.json`, `characters.json`, and `player_state.json` from the working directory, using Gson and independent JSON fields. It neither reads nor writes `T4C.dat`, does not create the native user path, and does not keep the same account/IP, AFK, addon, webpatch, and diagnostic-flag information in that binary file. Copying a 1.68 profile, or changing the launch directory, therefore does not produce the same persistent state, and an existing native configuration is invisible to Java.

## 229. Taming and companion system added in Java

The 1.68 client uses the term `Puppet` for a character's visual composition: `RQ_PuppetInformation`/`SetPuppet` transmit equipment parts and rebuild the sprite's layers. In the client's native sources, no taming sequence, channeling bar, companion registry, AI mode, companion loss, or tamed-animal save was found; a monster remains a visual/combat unit received from the server.

The Java code adds a full mechanic: `TameValidator` checks the target and level, `TameChannel` imposes channeling and a movement tolerance, `TamedCompanionFactory` converts the monster, `CompanionManager` manages the companion, its modes, its XP and its attacks, and `PlayerStateStore` restores it across sessions. A taming spell can therefore create and keep an allied unit in Java where no equivalent path exists in the 1.68 client; the resulting combat, damage, effects, and disappearance rules are all additional behavior.

## 230. Different time source and day/night thresholds

The native client receives the full time via `RQ_GetTime` (second, minute, hour, day, week, month and year), copies it into `g_TimeStructure`, and then advances this structure in the network loop with `AddSeconde`. `NTime::SetLight` applies precise thresholds: night 00:00–04:00 and 21:00–24:00, dawn 04:00–08:00, transition to day 08:00–10:00, day 10:00–19:00, and evening transition 19:00–21:00. Colors are computed separately in integer 5-bit channels, and worlds 1/2 or certain world-3 zones additionally force dungeon/cavern tints.

The Java `DayNightCycle` starts at 07:00 by default, advances with LibGDX's local `delta`, and saves a floating-point hour in `PlayerStateStore`; no Java packet equivalent to `RQ_GetTime` resyncs it against a server clock. Its thresholds are night 18:00–06:00, with transitions around 05:00–07:00 and 17:00–19:00, using a global black overlay. At the same nominal hour, the darkness ranges, the color, the time precision, and the reaction to underground worlds are therefore not those of the 1.68 client.

## 231. Different mouse-coordinate quantization and rejection

The native `DirectXInput::SetVirtualGrid` defines a window-dependent virtual grid. `GetStatus` then converts the mouse into a tile by applying the client's historical offsets (`+48` on X and `-8` on Y), then checks that the tile belongs to `VirtualGrid`. A position outside the grid or invalid is normalized to `(0,0)` before being passed to actions; a click is therefore never interpreted as an arbitrary off-world coordinate.

The Java code unprojects the pixel directly with the LibGDX camera, and then several handlers separately convert it with `(int)(world / GRID_W)` and `(int)(world / GRID_H)`. The camera, viewport, and origin point replace the native offsets/limits, with no shared virtual grid and no `(0,0)` sentinel value for an invalid click. A click at the edges, in a negative area, or during a camera change can therefore target a different tile, reach an object handler, or trigger movement where the 1.68 client would have rejected/normalized the position.

## 232. Cleaning up actions and the UI during a world change

- The native client treats a world change as an exclusive transition: it sets `DoNotMove`, calls `CloseAllUI()`, stops pathfinding, resets the music, and only re-enables play after the new zone's load and fade. Windows and interactions from the old zone therefore cannot stay active during the transfer.
- In `MainGameScreen.switchMapForZ`, the Java code rebuilds the renderer and the NPC/monster managers, but does not call `GuiManager.close()` and has no equivalent global lock. It explicitly cancels harvesting (`cancelHarvest()`), but not the taming channel or other in-progress spell progressions/effects. An open window or a transient action can therefore survive the map change depending on its own state, instead of being atomically invalidated the way the 1.68 client does.

## 233. Resetting and repopulating units around the player

- After a world change, the native client runs `Objects.DeleteAll()` under lock, rebuilds static/animated objects, moves the player, and then explicitly sends the server packet `GetNearUnits` (opcode 60) before resuming game state. Visible units are therefore a new proximity window supplied by the server, not a leftover from the previous zone.
- The Java `switchMapForZ` recreates `NPCManager` and `MonsterManager` from the map's local spawns and keeps the companion separately. There is no `GetNearUnits` request, no remote-unit registry, and no equivalent server phase; visible content therefore depends on the map file and the local managers, with different semantics for players/monsters that appeared or disappeared during the transition.

## 234. Quantity selection and clamping for item transfers

- The native client offers, in `ChestUI` and `TradeUI`, a shared quantity popup with a slider clamped by the available quantity, a filtered numeric field, a 9-character max length, and validation that clamps a value above the real stock. For a maximum quantity of 1, the popup automatically fills in `1` and validates directly; the confirmed quantity is then sent in a dedicated transfer request.
- The Java code has no equivalent popup and no transfer of stacks to a chest or a player-to-player trade. `ChestService` opens loot and drops it on the ground, while the inventory and shop locally handle keys/repetitions and unit increments. A "move N items" operation and its bounds/confirmations therefore do not follow the 1.68 client's UI and protocol behavior.

## 235. Coordinated shutdown of internal threads

- The native client has separate shutdown states for sound, sound control, drawing, maintenance, mouse, and CD. `AppManagement::AsyncClose` first notifies the UIs, sets `g_boQuitApp`, wakes blocking events, waits for or suspends still-active threads, and then releases handles/resources. Networking, rendering, input, and sound are therefore not simply destroyed in the caller's order.
- The Java code is mainly a single LibGDX loop: `MainGameScreen.dispose()` frees its resources and `MyGame.dispose()` stops the map preloader, but there is no shared shutdown state between input, network logic, rendering, and audio, and no equivalent queue/thread coordination. Exiting during a load, an action, or audio playback therefore does not follow the same completion and cleanup guarantees as the 1.68 client.

## 236. Different visual-object sort key

- The native `VisualObjectList::Sort` sorts units by a key computed from `OY + OC`, then breaks ties with `OC`, `OX`, and the attached object's ID. Sorting happens after filtering out-of-window objects and checking movement/child queues; attached objects can therefore stay grouped with their parent, and their draw order depends on the interpolated position, not just the tile.
- The Java code splits ground, decor, entity, and effect passes. `DecorRenderer` uses a key based on the decoration's name, its Y coordinate, and `getZOrderFast`, while entities are rendered in their own pass; there is no shared key equivalent to `OY + OC → OC → OX → AttachID` arbitrating every category. Overlaps between decor, unit, shadow, and effect can therefore be drawn in a different order than the 1.68 client, even when the grid positions are identical.

## 237. Different culling window and off-screen object retention

- The native client keeps a fixed window around the player via `VisualObjectList::RangeWidth` and `RangeHeight`, independent of the graphics viewport. During an update, an object whose `abs(OX) > RangeWidth` or `abs(OY) > RangeHeight` is removed if its type is below `30000`, it lacks `allowOutOfBound`, and it has no child (`Count == 0`). Attached objects then decrement their parent's counter; certain system objects and objects flagged out-of-bound are exempt from this removal. Static and animated objects are also reinjected at the edges with exact comparisons against `RangeWidth - 1`/`RangeHeight - 1`.
- The Java code computes `renderStartX/Y` and `renderEndX/Y` from the camera, adds a three-tile buffer plus a separate decor overrun, and then `NPCManager` and `MonsterManager` ignore entities outside this zone during their update. This logic is a camera-tied visibility optimization: it does not remove things by a shared lifetime rule, has no equivalent to `allowOutOfBound`, `Count`, `Type >= 30000`, or `AttachID`, and decor/entities have distinct margins. A native unit or object kept or destroyed at the edge of the window can therefore be updated, reinjected, or disappear at a different moment in the Java code.

## 238. Conditional NPC script preprocessing not equivalent

- The native NPC sources are C/C++ files built into the client/server: they contain preprocessor directives (`#ifdef`, `#ifndef`, `#define`, `#endif`) and commented-out blocks that select or remove dialogues, quests, and handlers depending on the compiled variant. The behavior actually available in a 1.68 build therefore depends on the compile configuration, even before the dialogue runs.
- The Java code passes `originalScript.sourceScript()`'s text directly to `NpcScriptEngine`. `statements(...)` only skips blank lines, `//`, `/*`, or `*`; no preprocessor pass resolves symbols or strips `#ifdef/#else/#endif` branches. Directives present in imported scripts therefore become unknown lines or non-executable text, and the native build variants cannot be selected the same way. Two installs using a different native `#define` can therefore share the same Java behavior, where the 1.68 client/server would have run only one compiled branch.

## 239. Different generic list selection and double-click

- The native `ListUI` is a control shared by several windows: it maintains an item, column, and row selection. A left click selects the row, a double-click is detected as early as `LeftMouseDown` and triggers the item's action, while a right-click also selects the item and shows its help (or the list's default help). Selection stays available to the event visitor, including after scrolling, and the columns of the same row are manipulated together.
- The Java code has no central `ListUI` with this contract. `GuiListScreen` composes buttons/rows specific to each screen; selections and callbacks are implemented separately in `ShopScreen`, `RepairScreen`, `QuestScreen`, etc. No generic routing simultaneously reproduces the native double-click, column/row selection, right-click help, and passing the selection to a visitor. A right-click or a double-click on a Java list can therefore be ignored or follow the screen's local handler, instead of selecting and opening the row's help/action as in 1.68.

## 240. Different paragraph pagination and line wrapping

- The native `TextPageUI` does not just store an already-rendered string: it keeps a list of paragraphs with text, color, font size, a computed line count, and an `allowNewLine` flag. `AddText` recomputes the total line count based on the area's width, `UpdateViewSize` adjusts the visible capacity, and `ScrollChanged` rebuilds the displayed `TextObject`s from the current offset. The same control can therefore mix colored paragraphs, forced line breaks, and text automatically reflowed in a scrolling area.
- The Java code splits this need across `GuiBoxedText`/`GlyphLayout`, `SystemMessage`, and `GameChat`. Wrapping, color, height, and scrolling are recomputed per screen or per component; no shared paragraph model with `allowNewLine`, a stored font size, and a native line offset is exposed. Text injected into a help window, a page, or a log can therefore produce different line breaks, height, and scroll position, even when the content and the visual width look identical.

## 241. Different Escape-key cycle and menu state

- In `RootBoxUI::VKeyInput`, Escape does not simply close the active window. If a fullscreen window is open, the native code hides chat and the TMI and then restores the macros; otherwise it advances the menu's persistent state (`MENU_BOTH` → chat only/TMI → none, depending on the current combination), shows or hides `SideMenu` and `ChatterUI`, and updates `dwMenuState`. A foreground window, however, receives the key with priority, which makes the cycle depend on global focus.
- The Java code gives each screen its own Escape rule: screens generally call `GuiManager.close()`, and `MainGameScreen` then opens `OptionsScreen` when no UI is active. There is no persistent chat/TMI/macros cycle, and no coordinated restore/minimize of panels. Repeatedly pressing Escape therefore does not produce the same intermediate states and can open the Java options screen where the 1.68 client would only have toggled the proximity UI.

## 242. Elemental spellbook filtering absent from Java

- The native `SpellUI` exposes seven element buttons (fire, water, air, earth, light, darkness, and normal). Clicking one temporarily rebuilds the page index, keeping only spells of the chosen element, moves the view to the first page of the result, and restores the previous page if no spell matches. The page's four slots are therefore a filtered view of the received list, not always four consecutive spells from the full book.
- The Java `SpellBook` always rebuilds pages with `currentPage * 4` over the full `spells` list. It has no elemental-filter buttons/state and no per-element index recomputation; a player therefore cannot get the same targeted view, the same page fallback, or the same "no results" behavior as in the 1.68 client, even though each `SpellData` carries element information the spell engine could use.

## 243. Recasting the last spell absent from Java

- The native `SpellUI` keeps `lastSpell` updated on every successful/engaged cast. `CastLastSpell` then looks up this ID in the spellbook and recasts the spell with `autoTargetSelf == false` and the `noCallback` path meant for repeated actions; `MouseAction` calls this function from several click/attack paths. The recast therefore depends on the spell's ID still being present in the list, not on the last macro slot used.
- The Java code has no equivalent `lastSpell` field or service. `MainGameScreen` keeps transient states (`selectedTargetedSpell`, `currentAttackSpell`, `lastClickedBuffSpellName`), and the quick-slots remember a name per slot, but no general gesture looks up and recasts the last spell by its ID after closing the spellbook or changing target. A native recast can therefore relaunch the spell while the Java code does nothing, only keeps a pending target, or reuses a different slot.

## 244. Different target-type priority for spells

- Before capturing the targeting click, `SpellUI::CastSpell` sets the object grid's mode: `monsterPriority` for spells aimed at monsters, `playerPriority` for players, and `equalPriority` for spells accepting any unit type. The native handler can therefore resolve an overlap according to the allowed category; for a unit spell, `CastSpellUnit` sends nothing if no valid unit ID is found, while a positional spell can send a position with no unit.
- The Java code mainly classifies the spell with `isHostileUnitSpell`, `isTameSpell`, `isPositionTargetSpell`, and separate handlers. It picks a local monster/NPC or a position depending on the chosen path, with no shared grid mode arbitrating a player-monster overlap and no native distinction between "no unit found" (nothing sent) and a fallback position. On a tile containing several categories, or a target that became invalid between the click and the cast, the chosen candidate and the resulting action can therefore differ.

## 245. Different spell order in the spellbook

- `SpellUI::UpdateSpells` always sorts the received list with `Spell::operator<`: first by numeric element, then by ascending level. The four slots of a page, and the result of elemental filters, therefore follow this order regardless of the order the server packet arrived in.
- The Java `SpellBook` walks `player.getSpells()` in its current order and adds each spell with no sorting. Java's global definitions are sorted by ID when discovered, but this sort is not applied to the personal list in `loadSpells()`. With an identical received list, spells can therefore appear on different pages and in different positions than in the 1.68 client, which also changes which spell a click or a drag-and-drop selects.

## 246. A spellbook entry's description not shown the same way

- During `SpellPageUI::FillSpellPage`, the native code places `spell.desc` into the control's help (`GetHelpText()->SetText`). The description is therefore available on hover/help-click for each slot, in addition to the name, level, duration, type, cost, and icon shown on the page.
- Java's `SpellBook.addSpellEntry` only displays the name, type, duration, mana, level, and icon. The method creates no help or tooltip from `SpellData` (and its `onTouchDown` starts a drag on the icon instead). The spell's received/defined description is therefore not viewable from the spellbook slot the way it is in the 1.68 client; the gesture can start a drag where the native code provides contextual information.

## 247. Different display conversion for spell durations

- The native client receives a numeric duration in milliseconds and formats it in `FillSpellPage` through several branches: a duration over a minute is shown in minutes with leftover seconds, a duration over a second in seconds, a zero duration as "instant", and a duration of one second or less with a dedicated presentation. The labels and the minutes/seconds combination come from the localized catalogue, not from a string supplied by the server.
- Java's `SpellData` stores `duration` as a `String`, and `SpellBook` displays it directly; an empty value only becomes the hardcoded text `instant`. This path has no milliseconds → minutes/seconds conversion and no branch for sub-second durations. A numeric definition, a fractional duration, or a differently worded translation can therefore appear literally or in a format different from the native sheet.

## 248. Different mana-cost type and interpretation

- In the native `SpellUI::Spell` structure, `manaCost` is a `WORD` received with the spell list; `FillSpellPage` converts it with `itoa` and therefore displays a fixed integer value. The visible cost is the field transmitted for that spell, with no random expression to evaluate in the UI.
- Java's `SpellData`, by contrast, stores `manaCost` as a `String`, and the spellbook displays it with no conversion. The Java engine can then interpret this string as a dice formula (`DiceFormula`) in `SpellCastingService`. A definition such as `1d6`, an empty string, or a non-numeric value can therefore produce a displayed text and an effective cost different from the native "received integer / displayed integer" pair.

## 249. Different skill colors and help

- `CharacterUI::UpdateSkills` picks the value's color along two native axes: whether the skill is usable (`bUse`), and whether the effective value is higher, equal to, or lower than the true value. It thus produces six visual states (gray/dark green/red if unusable, white/pale green/pale red if usable) and builds the help text from the description plus the `dwStrength` / `dwTrueStrength` pair.
- Java's `Statistics` only renders three fixed skills, and `buffedStatLabel` shows the effective value in green only when it is higher, otherwise the base value in white. It lacks `bUse`'s red/gray matrix, does not distinguish a lower effective value in its display, and does not build the native description + `effective/true` help text. A negative bonus, a disabled skill, or a temporarily unusable skill therefore does not get the same visual feedback.

## 250. Different button activation modes

- The native `ButtonUI` distinguishes a single click from a double-click in `LeftMouseUp`, plays press/release sounds separately, and has an `enableDragCycle` mode: during `DragCycle`, the event can periodically call `LeftClicked` back as long as the button is held. The control can therefore serve repeat buttons or actions with special double-click handling.
- Java's `GuiButton` only carries a single `Runnable` called on a left release that stayed within the area; it has no double-click callback, no drag-hold repeat mode, and no configurable separation of press/release sounds. A hold, a double-click, or a release after moving therefore does not trigger the same callbacks as a native `ButtonUI` configured for these modes.

## 251. Different input-cursor visibility and blink cadence

- The native `EditUI` only shows its cursor when `EnableCursor` is on and the parent considers the control the last-clicked element. Its state alternates with two configurable 300 ms delays (`cursorBlinkOnTime` and `cursorBlinkOffTime`); the text is drawn with `textOffset` and stays clipped by the editor's rectangle. Losing the parent's focus therefore hides the cursor even though the field still exists.
- Java's `GameChat` shows the cursor as long as chat is `active`, alternating on a fixed 350 ms period, with no notion of a `parentUI`/last-clicked control and no per-field `EnableCursor` setting. Its `inputScroll` follows the cursor, but visibility depends on chat's global activation. A field left open, or an overlapping input, can therefore show/hide the cursor and blink it at a different moment than the 1.68 client.

## 252. Different window capture and generic resizing

- The native `BoxUI` is a dispatch container: it looks for the first visible child hit, but lets a foreground child window intercept the event if it is not part of that hierarchy. Left/right clicks, the wheel, the keyboard, and `DragCycle` are then forwarded to the targeted control; a right-click with no child opens the box's help. `DragCycle` is globally rate-limited to about 100 ms, and the shown window is moved to the front of the root list by `Show(true)`.
- The Java code adds, in `GuiScreenBase`, a cross-cutting gesture that is not a native `BoxUI` operation: Ctrl-drag moves any candidate `GuiResizable`/element, while Shift-drag resizes its nearest edge down to a minimum size of 8 pixels. This capture happens before the screen's own handlers and logs the result on release; it reproduces neither the native `foregroundChild` priority, nor native `DragCycle` forwarding, nor right-click contextual help. Depending on the modifier key and the click point, the Java code can therefore move/resize a window the 1.68 client would only have used to dispatch to a control, or block its child event.

## 253. Different question count and selection in the character-creation questionnaire

- The 1.68 client separately shuffles the eight questions and the five answers of each question (`Shuffle`). It then shows only the first four questions of the run: on each answer, it increments one of five affinities, then immediately sends `RQ_CreatePlayer` with the five counters, the sex, and the name once `QuestionNumber == 4`. The four questions shown are therefore a draw without replacement from eight, with an answer order specific to each question.
- The Java `CharacterSelectionScreen` does shuffle eight questions and five answers, but `acceptQuestionAnswer()` keeps going until all eight entries are exhausted (`questionnaire.size() == 8`) before generating stats and continuing. The Java code therefore asks the player twice as many choices and accumulates different affinities for the same seed/draw; the moment of creation and the starting stats cannot match the 1.68 flow, even though the texts and the order of the five answers are otherwise reused.
