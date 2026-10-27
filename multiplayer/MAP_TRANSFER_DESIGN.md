# Authoritative map transfer: implementation handoff

Status: design for the next executable slice, October 27, 2026. Portal data and collision validation are implemented; hosted multi-region gameplay, transfer transactions and native map switching are not implemented. This document does not claim map transfer or Beta completion.

## Verified starting point

The server currently owns one `PreviewMap`, one `PreviewWorld`, one `PersistentSessions`, and one set of creature/combat services. `PreviewMap.load` selects an 80 by 60 crop of world 0 with origin (2904,1029). `PersistentSessions` rejects other saved worlds and checkpoints world 0. `PreviewWorld` selects world-0 authored spawns. `/map` is a global startup document. `OnlineConnection` fetches it once, before join. `OnlineClient.create` opens `Paths.MAP` and constructs `MapRenderer` with world 0. These are actual integration boundaries, not merely configuration changes.

The new foundations are:

- `PortalContent`: versioned ordered authored definitions with unique portal IDs.
- `OnlinePortalContentExporter`: exports the existing `TeleportRegistry` into `content/portals-v1.json`.
- `PortalCatalog`: indexed source lookup, preserves the offline first-source-match winner, resolves only authored destinations, and exposes diagnostics.
- `PortalDestinations`: reads explicit world-to-collision-file mappings, validates bounds and the existing player footprint, and reports raw collision codes.
- Three focused portal tests pass. The existing content freshness test now includes portals; the isolated exporter has compiled and run.

There are 1,118 authored definitions across worlds 0, 1 and 2, with four shadowed duplicate source definitions. Actual collision audit: 1,103 destinations pass strict ordinary footprint validation and 15 are blocked. Detailed local evidence is `target/portal-destination-audit.json` (regenerable, not production state). Portal 179 points at world 0 (0,0), collision code 3; the other blocked destinations report code 1. Do not silently choose nearby coordinates or label codes as water without authoritative collision metadata.

## Smallest actual game slice

Host two regions in the same JVM and on the same HTTP endpoint:

1. Existing starting crop, world 0, origin (2904,1029), size 80 by 60.
2. Dungeon region, world 1, proposed origin (264,353), size 80 by 60, containing the authored destination and return door below.

Use portal **16**: world 0 (2931,1056) to world 1 (304,383).
Use return portal **17**: world 1 (306,381) to world 0 (2932,1059).

Those are existing authored coordinates, not QA teleport commands. The dungeon rectangle contains 11 resolved authored monster spawns. Full behavior eligibility still needs startup validation; resolution alone does not prove every NPC interaction or attack is implemented.

A consequential movement detail: portal 16's source has collision code 16 at the foot and code 1 above. The current preview footprint blocks it. Offline `CollisionManager.hasPlayerCollision` explicitly allows an authored portal source before checking static footprint collision, while retaining dynamic obstruction checks. The online slice must implement the same narrowly scoped source exception for enabled, validated hosted portals; do not globally remove above-foot clearance or permit attacks through the wall. A four-neighbor traversal of the current crop finds a 32-tile route from (2944,1059) to portal 16 when that source exception is applied. It is blocked without it.

Portal 947 is also inside the crop, but it is not reachable in the current connected walkable area under ordinary footprint rules. It is not the initial acceptance route. All inactive portals remain blocked or explicitly unavailable; entering one must not send a player to an unhosted destination.

Acceptance for this slice: player A walks through portal 16 in the native client, loads dungeon art, moves there, logs out and reconnects there, then returns through portal 17. Player B remains in the starting area and continues moving/fighting. Neither can see, target, damage or recover corpses across regions/worlds. Then both enter the dungeon and can see one another. Restart preserves the destination. Single-player launch and its save files remain unchanged.

## Runtime ownership and boundaries

Keep one simulation thread, one bounded DB executor and one global 20-player limit. No distributed zone services, message broker or second login endpoint are needed for this scale.

Add a simulation-owned `HostedWorlds` coordinator with an explicit validated region table. A region owns `PreviewMap`, `PreviewWorld`, `PersistentCreatures`, `OnlineMelee` and `OnlineRetaliation`. A global `PersistentSessions` owns session tokens, character leases, command serialization, player IDs and the current region binding. Do not instantiate a separate session/lease manager per region.

Suggested records and methods (proposal, not existing API):

```java
record RegionId(int world, String key) {}
record RegionDescriptor(RegionId id, int originX, int originY,
                        int width, int height, String contentRevision) {}
RegionContext HostedWorlds.requireRegionFor(int world, double x, double y);
RegionContext HostedWorlds.regionForToken(String token);
CompletionStage<RewardResult> PersistentSessions.transfer(String token, TransferDraft draft);
```

`requireRegionFor` must select exactly one configured region. Reject missing or ambiguous ownership at startup or join. Never fall back to world 0. Initial regions have explicit IDs and fixed non-overlapping rectangles. Regions of the same world cannot overlap actor ownership; adjacent-region seam walking is a later extension, not implicitly implemented by this portal slice.

Refactor `PreviewMap.load` into an explicit bounded region loader receiving origin, dimensions and world identity. Keep the existing start-area wrapper for compatibility. Separate player portal-source passability from ordinary creature collision and line of sight. `PortalDestinations.canStand` proves physical placement only; it does not prove a region is hosted or game content is supported.

Refactor `PreviewWorld` admission so the global coordinator supplies stable token/player identity. Moving an actor between regions must preserve player ID, appearance, input sequence, session expiry and request-rate budget. It must clear directional motion, selected combat target and previous-region prediction state. Local creature IDs may remain region-local only if every target request is fenced by the region epoch; durable spawn keys already include world and coordinates.

Change `PersistentSessions.Binding` to carry `RegionId`, last absolute position, durable location epoch, and pending transition state. Remove immutable singleton map/world dependencies. All checkpoint, health publication, leave, corpse distance and retirement paths resolve through the binding. A region context is captured with a queued DB operation, never read later from a binding that may have moved.

Update combat services to use explicit region world identity. Creature selection uses `content.region(region.world(),...)`; victim lookup requires the same owning region. Global character-token lookup alone cannot authorize a monster attack. Persisted aggro from a departed character is cleared atomically on transfer, then all corresponding creature caches are refreshed, including uncertain-commit outcomes.

Global capacity counts acquired/admitted bindings, including closing and transferring players, until lease release completes. A transfer moves one reservation; it neither consumes a second slot nor temporarily releases its slot. Pending joins reserve capacity before asynchronous destination preparation to avoid admitting 21 simultaneous players.

## Transfer state machine and persistence

Detect portal occupation after authoritative movement on the simulation thread. The client supplies movement directions only. Lookup uses absolute world/tile coordinates and the first authored match. Check the destination region is hosted and ready, source is enabled, and target placement is valid. Preserve the existing 350 ms portal cooldown and suppression of repeated activation while occupying the same source; represent the authoritative deadline durably so reconnect cannot bypass it.

Initial slice preloads both regions and their creature persistence before accepting connections. No file I/O or synchronous database work may be introduced into a simulation tick.

When a transfer is accepted:

1. Require no other gameplay event pending for that character. Stop direction input and freeze that actor. Capture an immutable source sample, portal identity, authored destination, destination region and event UUID. Never recompute or reroll a pending draft.
2. Finish any already queued checkpoint, retaining the freeze. Submit the transfer using the resulting committed character revision.
3. In one PostgreSQL transaction, lock the active character lease, look up the transfer receipt, validate revision/world/epoch, write destination and increment location epoch/revision, record cooldown and receipt, and clear monster aggro for this character. Gameplay inventory/XP/HP are unchanged for a portal transition.
4. On confirmed commit, detach the source actor and attach the destination actor in one simulation-thread operation. Keep its token, player ID and global capacity reservation. Destination construction must already be validated and allocated before committing; a post-commit attach failure disconnects and recovers from saved destination, never writes back the source.
5. Publish the new region descriptor and epoch. Keep the actor frozen for map-load acknowledgement; other actors and lease maintenance continue.
6. An authenticated `region-ready` acknowledgement names only the epoch. After matching it to the binding, unfreeze. Timeout/disconnect uses normal durable logout handling.

Proposed store entry point:

```java
RewardResult commitTransfer(Session session, long expectedRevision,
                            UUID eventId, TransferDraft draft);
```

The draft is server-only. Bind its receipt hash to character ID, portal ID, exact source sample, source world/epoch, authored destination, target region/content revision and cooldown. Use a dedicated `online_transfer_events` table and a migration adding `location_epoch` and portal cooldown state to characters. Receipt lookup precedes stale-revision rejection, but follows active lease validation. Matching retries return current committed state without replaying movement; mismatched payloads fail. Target changes in a later content export do not alter an already accepted draft.

Do not compare source XY against an old movement checkpoint: the authoritative source sample may legitimately be newer. The session-owned queued command and source world/epoch/revision fence authorize the sampled location. Verify source occupation in the simulation before drafting. Never accept draft coordinates from an HTTP request.

Unknown transaction outcome disconnects the actor and reloads all affected creature caches. Rejoin reads the database: if committed, join the destination and new epoch; otherwise join the prior persisted state. Do not automatically restore the source with a compensating write. Logout accepted during a pending transfer must drain it, then checkpoint the committed destination, mirroring the death/logout rule already tested.

Death reuses destination validation and region preparation. The fatal transaction must still atomically persist HP, XP penalty, inventory/corpse and destination. Its location epoch increments on relocation, including same-world respawn. Corpse location stays in the world where death occurred. Remove the current outside-crop/world-zero fallback only after saved respawn resolution is implemented. Invalid or unhosted saved respawns require an explicit surfaced recovery policy; silently spawning in another world is not acceptable.

## Protocol and native rendering

Increment `OnlineProtocol.VERSION` for this incompatible contract. Include `RegionDescriptor` and durable `locationEpoch` with join and every snapshot. `/map` must be authenticated and return the caller's current region, or descriptor/blocked cells may be embedded in a bounded initial-region response. A global pre-join map response cannot describe where a returning character is located.

Carry `locationEpoch` on movement, melee, equipment and corpse-recovery requests. Reject stale-epoch requests before resolving region-local actor IDs. Preserve a monotonically increasing movement sequence through transfer; reset accepted direction to zero. A delayed attack from the starting region must not hit a dungeon monster with the same local numeric ID.

`OnlineConnection` stages snapshots until descriptor and snapshot epochs match. Publish one immutable view containing map metadata, character state, corpses and actor lists for that epoch. Never combine the old origin with new local positions. Clear interpolation/targeting and drop previous-epoch snapshots. Keep transport polling/heartbeat active while native assets load, and send `region-ready(epoch)` only after the render thread successfully installs that region's map. A failed load reports a specific error and disconnects safely; it does not draw the previous map under new actors.

Use the existing explicit native asset mapping:

| World | MapReader asset |
| --- | --- |
| 0 | `Paths.WORLDMAP_MAPBIN` / `assets/maps/worldmap/worldmap.mapbin` |
| 1 | `Paths.DUNGEON_MAPBIN` / `assets/maps/dungeon/DungeonMap.mapbin` |
| 2 | `Paths.CAVERN_MAPBIN` / `assets/maps/cavern/CavernMap.mapbin` |
| 3 | `Paths.UNDERWORLD_MAPBIN` / `assets/maps/underworld/underworld.mapbin` |

`MapDefinition.fromZ` currently returns worldmap for unknown IDs. Do not use that fallback for network data. Select an exact enum entry by `getZ()` and reject absent IDs. A server-advertised world does not authorize an arbitrary local file path; paths come only from this client allowlist.

`OnlineClient` currently stores sprites only in `create`; retain the shared sprite loader for subsequent region installs. Create the new `MapReader` and `MapRenderer(..., worldId)` before replacing the old pair, then dispose the old renderer and close the old reader. Open/close and renderer lifecycle must respect the LibGDX render thread; do not let the network thread mutate native assets. Clear avatars, creature animation instances, selected target, corpse target and `lastTick`; reposition the camera from the first matching-epoch authoritative actor. Region change within the same world may reuse the map reader, but still resets origin-dependent view state. The first implementation may reopen it for clarity if measured loading remains acceptable.

## Implementation order and verification

1. Implement explicit region loading and enabled portal-source movement exception; validate both slice regions at startup. Add one movement regression proving the authored door is enterable while neighboring wall tiles and combat LOS stay blocked.
2. Introduce coordinator/global bindings and stable actor migration without exposing a teleport command. Keep existing single-region tests running through the compatibility constructor.
3. Implement transfer transaction and epoch persistence. Test rollback, identical replay, altered replay rejection, lease/revision fencing, commit-response loss/rejoin, and logout while transfer is pending. Extend existing persistence test setup.
4. Add protocol epoch/descriptor handling and native asset switch, then wire automatic source-tile activation. Test stale attack isolation and global capacity across both regions.
5. Run two real clients through portal 16, reconnect in dungeon, return via 17, then both in one dungeon. Capture client/server logs and screenshots without credentials or personal offline saves. Verify ordinary offline launch separately.
6. Integrate cross-map death/respawn with the same transition publication and recovery machinery. Then expand hosted coverage and supported portal graph deliberately; blocked/unhosted diagnostics remain visible in operator startup output.

Full Beta still requires broader hosted-map coverage, supported content/NPC/quest behavior, progression and economy joins, secure remote transport/accounts, restart/restore evidence and sustained mixed 20-client operation. Passing this two-region slice proves a transfer vertical slice, not those remaining acceptance criteria.
