# First authoritative story interaction (T4C-0195)

This slice implements Kilhiam's existing first-conversation letter reward. It starts
the authored Crimsonscale story; it is not a completed quest chain or the Beta quest
acceptance gate. Kilhiam is authored at world 0, tile 2961,1058, within the temple
region. The exporter reads her actual `@Spawn` and `NpcSpec` appearance. No new NPC,
sprite, quest objective, experience or gold reward is introduced.

## Existing behavior preserved

When `ADDON_CRIMSONSCALE_LETTER` is exactly zero, the conversation grants one
`item.letter_from_crimsonscale`, sets the flag to 1, and replies with
`npc.kilhiam.letter`. Item charges come from the packaged item registry; unique,
weight and drop behavior remain registry properties. The current letter is
weightless, non-unique and droppable. Losing it does not reset the flag or authorize
another reward. Nonzero legacy flag values retain the existing no-grant behavior.

Repeat greetings inspect `ADDON_STORYLINE_PROGRESS`: 19 uses
`npc.kilhiam.highpriest`, 20 uses `npc.kilhiam.brotherkiran`, less than 42 uses
`npc.kilhiam.temple`, and 42 or greater uses `npc.kilhiam.welcome`. These decisions
are shared with the offline NPC through `CrimsonscaleLetterRules`; the offline
conversation still invokes its existing inventory service and writes its same flag.

## Runtime integration contract

`NpcInteractionCatalog.loadBundled()` provides immutable authored NPC locations and
appearance parts. Only Kilhiam is supported. `OnlineNpcInteractions.prepare` takes
the committed owner progress, authoritative world/absolute tile position, NPC ID,
server-computed line of sight and a server UUID supplier. It rejects unknown NPCs,
wrong worlds, dead actors, non-finite positions, blocked sight and distances beyond
the existing `NPC_INTERACTION_RANGE_TILES_SQUARED`. The caller must also verify that
the NPC belongs to the player's currently loaded region.

The return value is `Result(progress, messageKey, changed)`. A changed result adds
the immutable item instance and flag together. It contains no balances or client
supplied reward data. Prepare once; reuse that exact draft and operation UUID during
an uncertain database retry. Commit through the existing session worker's
`commitGameplayEvent(token, expectedRevision, operationId, 0, 0, result.progress())`.
Do not publish the reply or item before commit. A repeated conversation with the
committed flag returns unchanged progress without allocating an item UUID.

The runtime contract implemented in the server is:

1. `/state` publishes `npcs` containing public NPC ID, authored display-name key,
   region-local x/y and appearance parts for the active region. Quest flags and
   inventory remain owner-only. `RegionNpcs` filters catalog entries by hosted region.
2. `POST /interact` accepts `REVISION EPOCH NPC_ID` only (128-byte body cap,
   19-digit nonnegative numbers and a 64-character alphanumeric/underscore NPC ID). Authenticate, rate-limit and validate all state
   on the simulation owner before preparing the draft. Transfer races must reject
   stale location epochs. Check dead/frozen/pending mutation admission consistently
   with other gameplay commands; do not stack two uncommitted letter drafts.
3. `PersistentSessions.interact` executes the draft through the existing transactional
   session worker. `NpcInteractionReply` returns `{revision,messageKey,changed}` only
   after commit. A first letter requires `changed=true` and an increased revision.
   A repeat greeting returns `changed=false` and may retain the request revision; it
   still validates ready state, revision, region, range and raw line of sight.
4. Native single-flight interaction intent and committed private refresh are implemented. Next
   and show the reply using the existing i18n catalog. Root will preview input/HUD
   changes before installing visuals. Network mutations must not auto-replay.
5. OnlineMelee uses
   `CrimsonscaleLetterRules.hasUnsupportedCombatFlags`. This permits only the proven
   `ADDON_CRIMSONSCALE_LETTER=1` flag; all unknown quest scripts remain unsupported.
   Otherwise the first conversation would unintentionally disable melee.

The store-level PostgreSQL test proves flag/item/balance persistence and receipt
deduplication. `OnlineNpcHttpTest` covers authenticated authored-region interaction,
ready/epoch/revision/range/unknown-NPC checks, private rewards, no-op replies and
reconnect without duplicate item grants. `RegionNpcsTest` checks that sanctuary
terrain permits talking while walls and foreign regions do not. Rendered NPCs and
two-client dialogue still require native evidence; service tests do not close that gate.

Verified locally on 2026-10-27 against the isolated `t4c_test` PostgreSQL database:
`mvn -q -f multiplayer/pom.xml verify` passed 18 domain tests and 72 server tests,
with no failures, errors or skips. This includes the authored two-region HTTP
interaction, the sanctuary/LOS checks, atomic reward receipt replay and melee after
receiving the story flag. The focused desktop run also passed the existing five
witness/legacy NPC tests and four isolated generated-content freshness checks.
No rendered-client evidence is claimed by these results.

## Subsequent full quest

Warden Aelric (2948,1044) offers existing `renewed_wards`: kill twelve Skeletons near
2785,1095 within radius 50, then receive 500 gold, 400 XP and `renew_armor`.
The kill area is outside the current temple crop, and spell rewards are not yet
implemented. Do not shorten or relocate this quest to make a test pass. Supporting
that complete authored flow requires its region/creatures, authoritative kill-credit
flags and transactional turn-in with spell grants. Kilhiam's remaining classic
storyline and teaching/sanctuary/witness dialogue are also outside this opening slice.

## Recorded validation (2026-10-27)

Full PostgreSQL reactor verification and packaging passed: 18 domain tests and 72 server tests,
zero failures/errors/skips. Seventeen focused desktop tests passed, including the existing NPC
conversation/offline behavior test, native adapter integration, exporter parity and native checksum.
The adapter test covers public NPC data, increased-revision letter acknowledgement and same-revision
repeat greeting. A separate two-actual-native-adapter QA probe passed replication, first letter/flag,
repeat greeting, observer isolation, unchanged unrelated inventory/stats/gold and reconnect preserving
the identical letter UUID. Reproduce with tools/NATIVE_NPC_PROBE.md; the local log is
`$env:TEMP/t4c-0195-native-npc-probe.log`. No rendered NPC/dialogue test is claimed.

CI now runs the complete PostgreSQL reactor rather than the older four-class allowlist, so hosted
transfer, retaliation and NPC transaction tests execute with a real database on this delivery.
