# Native tamper battery (T4C-0251, M5 cheating resistance)

`NativeTamperProbe.java` attacks one real `ServerMain --online` child with concrete tampering and records,
for every case, the exact response, whether anyone's committed state changed, and whether the bystander
keeps playing. It is local HTTP and native-adapter evidence only: it is not a rendered-client run, not an
internet-exposed or TLS run and not the integrated release build.

It never changes server code or weakens a guard. A FAIL row means the server accepted tampering, a
committed state changed, the bystander was affected, or the refusal was not the definite one the code
documents. A FAIL is reported as a finding, not repaired here.

## Cast and method

- **B (victim and bystander)** is a warrior at Kilhiam's tile in the hosted temple ground. It joins through the
  real `OnlineConnection` adapter and stays connected for the whole battery. After the last tamper case it
  talks to Kilhiam and moves through the adapter, and its committed record is compared with provisioning.
- **A (attacker)** is a paladin at Warden Aelric's tile, on a different account key, joined through raw
  `java.net.http` so every header, body and byte is under the probe's control. It starts at 1 hp so a
  healing potion has something to heal. (A raw join is the only way to hold a session token; the adapter does
  not expose it.)
- **C** is a third account whose character is provisioned at an unhosted tile, only to prove the join refusal.
- All three come from the normal provisioning path (`OnlineStore.provisionAccount` / `create`), as in the other probes.
- **Seeded fixtures** (the one place the probe writes rows itself, before the server starts): one corpse for A,
  one corpse for B and one owner-window ground-loot row for B, the last two on A's tile. There is no other
  route to a foreign corpse or foreign owner-window loot without lethal combat (T4C-0248). The server's own
  cache load, owner window, reservation and range rules then run unchanged. This depends on schema-13 table
  columns; a later migration that adds a required column to those tables needs this seeding updated.
- Before and after **every** case the probe reads A's and B's committed record from the database
  (gold, experience, attributes, advancement, inventory, equipment, skills, spells, quest flags, storage, world).
  Vitals, cooldowns, position and revision are excluded because the server persists a checkpoint (and so
  bumps the revision, and settles regeneration) every ~5 s per session; valid probe commands therefore re-read
  the revision and retry once on a stale-revision answer that a checkpoint overtook.
- `B-ALIVE` rows check, at each group boundary, that the adapter is connected, still sees its own identity and
  that snapshot ticks advance.

## Groups and what each proves

| Group | Cases | Attack |
|---|---|---|
| `JOIN`, `ROSTER`, `FOREIGN-ACTIVE` | A's key with B's character UUID (B offline and connected), B's key with A's UUID, own key twice, random key, unknown UUID, missing/old protocol header, forged/missing Origin, wrong content type, 9 malformed join bodies, unhosted join | foreign ownership |
| `LEAK` | A's `/state`, `/character`, `/corpses`, `/map` scanned for B's UUID, any account key, A's session token, B's corpse id; B's owner-window drop shown without an owner | information disclosure |
| `AUTH`, `ADMIN` | missing Authorization on all 11 session routes, 9 garbled forms, account keys and random keys used as session tokens, GET routes without a token, A's token/account key on `/admin/shutdown` and `/admin/metrics` | broken authentication, privilege escalation |
| `MAL` | 90+ malformed numbers/UUIDs/slots/spells/NPC ids on every route, wrong method/route/query/content type/Origin/protocol | malformed input |
| `STALE`, `EPOCH` | wrong revision on `/attack /pickup /cast /use /equip /unequip /recover /interact`; wrong location epoch on all 10 epoch-carrying routes | stale commands |
| `BIG` | one byte over the cap on each capped route, 128 KiB body, chunked body without length, raw lying/duplicate/negative/non-numeric `Content-Length`, foreign Host | oversized requests |
| `FORGE` | `/attack` on ids 0, 1, A's own actor, B's actor, 4e9, Long.MAX and a real out-of-range creature; `/pickup` on B's owner-window drop and random ids; `/recover` on B's corpse and with B's item id; `/use` and `/equip` with B's item instances; casting unlearned/item-only spells; `/interact` on an NPC out of range | forged targets, foreign property |
| `IDEM` | unequip/equip replay; two identical `/use` released together on one potion; `/cast`, `/recover`, quest-accept replay; reordered/replayed/zero input sequence; character-creation `requestId` replay and conflicting reuse | duplicates and replays |
| `EDIT` | extra tokens, JSON bodies, quantity/gold/flag/damage/speed/position fields on every route; 10 extra fields and a duplicate on character creation; unsupported class; final stats compared with provisioning | stat, item, gold and quest-flag edits |
| `SPEED`, `SEQ` | 6 absurd direction vectors, a spammed input run measured against `PLAYER_SPEED / GRID_W / TICK_RATE`, 24 s of walking into walls and the region edge sampled for collision/hosting/per-tick step, a Long.MAX sequence jump | speed, teleport, collision |
| `B-PLAY`, `DB` | after everything: B's native dialogue (one letter), native movement committed, B's stats unchanged; B's loot and corpse rows untouched, A's corpse recovered exactly once | the victim is unharmed |

The protocol has no client-supplied request ids, positions, stats, gold, experience, flags or item
definitions on any gameplay route: bodies are fixed-grammar text of revision, epoch and one id or enum.
Replays are fenced by the revision compare-and-set (and the input sequence); the only client request id
is character creation's `requestId`, which is covered. The probe therefore checks that nothing outside
the grammar is accepted and that each valid command applies once.

## Isolation and run

Run only on a disposable cluster and dedicated ports; the probe refuses any database that is not
`t4c_acc_<12 hex>` or `t4c_tamper_<12 hex>`, any non-loopback or TLS setup, and a classpath outside the
copied runtime. Never use a shared database, `t4c_online`, `t4c_test`, QA ports or processes.

1. Claim a window in [PEER_WORK.md](../PEER_WORK.md) (cluster port, server port, TEMP directory).
2. `mvn -q -DskipTests package` at the repository root and in `multiplayer/`.
3. Build the TEMP runtime exactly as in [NATIVE_LOOT_PROBE.md](NATIVE_LOOT_PROBE.md): `client/T4C.jar`,
   `t4c-server.jar`, `lib/*`, both `.colbin` maps; compile the probe into `runtime/probe` with `javac -proc:none`.
4. `./multiplayer/tools/acceptance-cluster.ps1 -Action Create -Port <free>` then
   `-Action RunEnv -RunId t4c_acc_<id> -Command java -CommandArgs @('-cp',$probeClasspath,'com.perso.T4C.server.NativeTamperProbe',$runtime,$javaExe,'<server-port>',$output)`.
   The output directory must not exist. It receives `report.txt`, `cases.md`, `control.token` and child logs.
5. `-Action Destroy`, delete the TEMP runtime, confirm both ports are free and the protected ports unchanged.

Runtime is a few minutes. Exit status 1 means at least one FAIL.

## Recorded run

**Tamper battery, local PG16 WSL, 322 cases, not the integrated release build** (T4C-0251, 2026-10-28).
Source `origin/main` 4bcb72b4 (protocol 5, schema 13), disposable PostgreSQL 16 cluster `t4c_acc_727269d019ac` on
loopback 56901 (destroyed afterwards, `DESTROY_OK`), one `ServerMain --online` child on 18795 per run, TEMP runtime
snapshot (removed afterwards). Result of the final run: **321 PASS, 1 FAIL**. The server exited 0 after the private
admin stop and no probe step weakened a guard.

The first run on the same cluster reached the end (320 cases) with 5 FAIL, four of them probe mistakes that were
fixed in the probe only: the JDK client strips a trailing space from a header (the "garbled" request was a valid
token and advanced A's input sequence, which also invalidated the sequence and movement rows); `RING1` is not a
slot the `/unequip` grammar can express; character names are limited to 8 letters, so every creation row failed on the
name instead of the case; and the movement rows then ran against a poisoned sequence. The fifth FAIL is the finding
below and reproduced unchanged in the rerun.

### Finding (server, not fixed here)

**`/recover` reports a definite corpse refusal as `503 busy`.** `POST /recover <rev> <epoch> <own corpse> <item id of a
different corpse>` is refused inside the commit as `item_unavailable` (`CORPSE_REFUSALS`), which the session layer
completes as `Rejected(409, item_unavailable)`. The `/recover` route in `PreviewServer.handle` waits on that stage
with `get(2, SECONDS)` but, unlike `/pickup`, `/cast` and `/use`, does not unwrap the `ExecutionException`, so the generic
handler answers `503 {"error":"busy"}`. Evidence: case `FORGE-recover-B-item-on-A-corpse`, observed `503 busy` in both
runs, and the same pattern applies to the `too_heavy` corpse refusal. Impact is limited: nothing changed (A's and
B's records and both corpse rows were verified unchanged), but a definite refusal looks like a transient overload, so a
client may retry or show "busy". No tamper succeeded. Suggested fix, for a separate task: unwrap the cause as `/pickup`
does.

### Observations that are not failures

- `/recover` of an already emptied corpse with a fresh revision returns `200 {"revision":N}` with no change (a no-op,
  gold and items were taken exactly once: `IDEM-recover-total`).
- A Long.MAX input sequence is accepted once and then locks A's own movement until it rejoins; no other player is
  affected (`SEQ-jump`). It is an own-session denial, recorded for completeness.
- `/equip` and `/unequip` cannot name `RING1`, `RING2` or `WEAPON2` (the slot regex has no digits). Not a security issue.
- The server's HTTP layer accepts `Authorization: Bearer <token> ` with a trailing space as the same principal
  (`AUTH-GARBLED-trailing-space`, 200). No other principal was ever accepted.
- The `SPEED-walls` per-tick step reached exactly `0.2757` tiles (the configured speed) and never exceeded it; the spam run
  moved A 1.38 tiles in 77 ticks.

### NOT COVERED (stated, not faked)

- The refused portal 947 (target world 1, unhosted): its tile lies in a pocket walled off from the temple ground (nearest
  reachable tile is 4 tiles away), so A could not stand on it; the probe's BFS found no route and records that. Refused
  portals 12/13/14 are inside the lethal Skeleton ground. Unhosted-region **joins** are covered (`JOIN-11`).
- Enabled portal (routes 16/17) transfer tampering and teleport through a transfer.
- A stale revision on `/attack` **after** target validation, and damage/reward forging in combat: no combat is run
  (starter ground is lethal, T4C-0248). `/attack` checks the revision first, which is covered.
- B collecting its own owner-window drop or corpse: both fixtures lie on A's tile, out of B's reach.
- Creature or loot ids that belong to another hosted region (random ids cover the refusal path).
- Account-key rotation fencing (T4C-0219 `NativeKeyRotationProbe`), TLS/remote transport, rendered-client behavior.
- The cluster runs PostgreSQL 16; the CI PostgreSQL 18 job stays the authority for persistence contracts.

### Case table (final run)

| Case | Expected | Observed | Result |
|---|---|---|---|
| `JOIN-1` A's account key + B's character UUID on /join (B offline, protocol header present) | 409, B lease stays free, no session issued | 409 account_or_session; state unchanged | PASS |
| `JOIN-1b` B lease still free after A's foreign join | lease free | lease free | PASS |
| `JOIN-2` B's account key + A's character UUID on /join | 409, A lease stays free | 409 account_or_session; state unchanged | PASS |
| `JOIN-2b` A lease still free after B's foreign join | lease free | lease free | PASS |
| `JOIN-3` foreign /join without the protocol header | 426 | 426 protocol; state unchanged | PASS |
| `JOIN-4` foreign /join with a stale protocol version (4) | 426 | 426 protocol; state unchanged | PASS |
| `JOIN-4b` B lease still free after protocol-less/old-version foreign joins | lease free | lease free | PASS |
| `JOIN-5` random well-formed key + A's UUID | 409 | 409 account_or_session; state unchanged | PASS |
| `JOIN-6` A's key + unknown character UUID | 409 | 409 account_or_session; state unchanged | PASS |
| `JOIN-7a` malformed /join body: 63-hex key | 400 (413 when over the 101-byte cap) | 400 input; state unchanged | PASS |
| `JOIN-7b` malformed /join body: uppercase key | 400 (413 when over the 101-byte cap) | 400 input; state unchanged | PASS |
| `JOIN-7c` malformed /join body: uppercase UUID | 400 (413 when over the 101-byte cap) | 400 input; state unchanged | PASS |
| `JOIN-7d` malformed /join body: missing UUID | 400 (413 when over the 101-byte cap) | 400 input; state unchanged | PASS |
| `JOIN-7e` malformed /join body: extra token | 400 (413 when over the 101-byte cap) | 413 input; state unchanged | PASS |
| `JOIN-7f` malformed /join body: empty body | 400 (413 when over the 101-byte cap) | 400 input; state unchanged | PASS |
| `JOIN-7g` malformed /join body: newline separator | 400 (413 when over the 101-byte cap) | 400 input; state unchanged | PASS |
| `JOIN-7h` malformed /join body: key and UUID swapped | 400 (413 when over the 101-byte cap) | 400 input; state unchanged | PASS |
| `JOIN-7i` malformed /join body: B's UUID twice | 400 (413 when over the 101-byte cap) | 400 input; state unchanged | PASS |
| `JOIN-8` /join from a foreign Origin | 403 | 403 origin; state unchanged | PASS |
| `JOIN-9` /join without an Origin header | 403 | 403 origin; state unchanged | PASS |
| `JOIN-10` /join with application/json content type | 415 | 415 input; state unchanged | PASS |
| `JOIN-11` C (provisioned at an unhosted tile) /join | 409 unhosted_region, no lease kept | 409 unhosted_region; state unchanged | PASS |
| `JOIN-11b` C lease released after the unhosted refusal | lease free | lease free | PASS |
| `ROSTER-1` GET /account/characters with A's key lists only A's characters | 200 without B's UUID | 200 (body withheld); state unchanged | PASS |
| `ROSTER-2` GET /account/characters with a random key | 401 | 401 account; state unchanged | PASS |
| `ROSTER-3` GET /account/characters without the protocol header | 426 | 426 protocol; state unchanged | PASS |
| `ROSTER-4` GET /account/characters?after=<not a UUID> | 400 | 400 input; state unchanged | PASS |
| `SETUP` A (raw HTTP) and B (native adapter) both joined and see each other | both connected, mutual visibility | A session ok; B status connected; A sees B: true | PASS |
| `LEAK/state` GET /state with A's session carries no B UUID, account key, session token or B corpse id | 200 without secrets | HTTP 200 (14491 bytes scanned); nothing leaked | PASS |
| `LEAK/character` GET /character with A's session carries no B UUID, account key, session token or B corpse id | 200 without secrets | HTTP 200 (2557 bytes scanned); nothing leaked | PASS |
| `LEAK/corpses` GET /corpses with A's session carries no B UUID, account key, session token or B corpse id | 200 without secrets | HTTP 200 (104 bytes scanned); nothing leaked | PASS |
| `LEAK/map` GET /map with A's session carries no B UUID, account key, session token or B corpse id | 200 without secrets | HTTP 200 (166878 bytes scanned); nothing leaked | PASS |
| `LEAK-LOOT` B's owner-window ground loot is visible to A without naming its owner | drop visible, ownerPlayerId null | visible=true, ownerPlayerId=null | PASS |
| `AUTH-NONE/input` POST /input without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/attack` POST /attack without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/pickup` POST /pickup without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/cast` POST /cast without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/use` POST /use without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/equip` POST /equip without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/unequip` POST /unequip without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/recover` POST /recover without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/interact` POST /interact without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/region-ready` POST /region-ready without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-NONE/leave` POST /leave without Authorization | 401 session | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-bearer-lower` garbled Authorization (bearer-lower) on /input | 401 | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-basic` garbled Authorization (basic) on /input | 401 | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-short` garbled Authorization (short) on /input | 401 | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-upper-hex` garbled Authorization (upper-hex) on /input | 401 | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-padded` garbled Authorization (padded) on /input | 401 | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-empty` garbled Authorization (empty) on /input | 401 | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-raw-token` garbled Authorization (raw-token) on /input | 401 | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-token-only` garbled Authorization (token-only) on /input | 401 | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-prefix-junk` garbled Authorization (prefix-junk) on /input | 401 | 401 session; state unchanged | PASS |
| `AUTH-GARBLED-trailing-space` raw Authorization with a trailing space after the token | 401, or 200 only if the HTTP layer strips trailing whites... | 200 {"ok":true}; state unchanged | PASS |
| `AUTH-FOREIGN-B-account-key` POST /attack with B-account-key as a session token | 401 | 401 session; state unchanged | PASS |
| `AUTH-FOREIGN-GET-B-account-key` GET /character with B-account-key as a session token | 401 | 401 session; state unchanged | PASS |
| `AUTH-FOREIGN-A-account-key` POST /attack with A-account-key as a session token | 401 | 401 session; state unchanged | PASS |
| `AUTH-FOREIGN-GET-A-account-key` GET /character with A-account-key as a session token | 401 | 401 session; state unchanged | PASS |
| `AUTH-FOREIGN-random` POST /attack with random as a session token | 401 | 401 session; state unchanged | PASS |
| `AUTH-FOREIGN-GET-random` GET /character with random as a session token | 401 | 401 session; state unchanged | PASS |
| `AUTH-GET-NONE/state` GET /state without Authorization | 401 | 401 session; state unchanged | PASS |
| `AUTH-GET-NONE/character` GET /character without Authorization | 401 | 401 session; state unchanged | PASS |
| `AUTH-GET-NONE/corpses` GET /corpses without Authorization | 401 | 401 session; state unchanged | PASS |
| `ADMIN-1` POST /admin/shutdown with A's session token | 403 admin, server keeps running | 403 admin; state unchanged | PASS |
| `ADMIN-2` POST /admin/shutdown with A's account key | 403 admin | 403 admin; state unchanged | PASS |
| `ADMIN-3` GET /admin/metrics with A's session token | 403 admin | 403 admin; state unchanged | PASS |
| `ADMIN-4` GET /admin/metrics without Authorization | 401 or 403 | 401 session; state unchanged | PASS |
| `ADMIN-5` server and both sessions still alive after the admin attempts | /health 200, B connected, A /state 200 | 200 {"capacity":20,"mode":"local-persistent","tick":363}; B connected; A /state 200 | PASS |
| `FOREIGN-ACTIVE` A's key + B's UUID on /join while B is connected | 409; B stays connected | 409 account_or_session; state unchanged | PASS |
| `FOREIGN-ACTIVE-2` B's key + A's UUID on /join while A is connected | 409 | 409 account_or_session; state unchanged | PASS |
| `FOREIGN-ACTIVE-3` A's own key + A's UUID second /join while A already connected | 409 (one lease per character) | 409 account_or_session; state unchanged | PASS |
| `B-ALIVE after authorization attempts` B (native adapter) keeps playing after authorization attempts | connected, own identity, snapshot ticks advancing | status connected, ticks 379->405, gold 242 | PASS |
| `MAL-input-5fe88` /input body 'abc' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-48626` /input body '' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-6517c` /input body '1 2 3' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-77e8` /input body '1  2 1 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-984bb` /input body '-1 0 1 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-339b` /input body '0 -1 1 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-e6a08` /input body '0 0 2 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-6e9d9` /input body '0 0 1 1 1' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-f90d8` /input body '99999999999999999999 1 1 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-653ba` /input body '9223372036854775808 1 0 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-e76a7` /input body '? ? 1 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-5427e` /input body '+1 1 1 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-d4349` /input body '0 0 1.0 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-2a942` /input body '0x1 2 1 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-b82c4` /input body '0 1 1 0\0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-b089` /input body '0 1 100 100' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-input-c7e81` /input body '0 1 1 ?' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-793cc` /attack body 'abc' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-6d76d` /attack body '1 2' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-26d54` /attack body '1 2 3 4' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-1f32f` /attack body '-1 0 1' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-7f8ee` /attack body '0 0 -1' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-ffd42` /attack body '0 0 1.5' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-915b1` /attack body '0 0 0x10' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-85a8a` /attack body '0 0 99999999999999999999' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-2dbad` /attack body '0 0 9223372036854775808' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-4722e` /attack body '9223372036854775808 0 1' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-38eee` /attack body '0 9223372036854775808 1' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-attack-7f939` /attack body '0 0 1\0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-6a0e5` /pickup body '0 0 1D832403-AE04-481D-93F1-0609E28ACF9C' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-f1f76` /pickup body '0 0 d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-36889` /pickup body '0 0 {1d832403-ae04-481d-93f1-0609e28acf9c}' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-1c615` /pickup body '0 0 ' OR 1=1 --' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-edc5f` /pickup body '0 0 1d832403-ae04-481d-93f1-0609e28acf9cx' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-7a305` /pickup body '0 0 ../../1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-1a571` /pickup body '-1 0 1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-45291` /pickup body '0 -1 1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-43df6` /pickup body '0 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-4fe18` /pickup body 'abc' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-pickup-78e12` /pickup body '9223372036854775808 0 1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-5af8` /use body '0 0 1D832403-AE04-481D-93F1-0609E28ACF9C' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-8d989` /use body '0 0 d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-d229c` /use body '0 0 {1d832403-ae04-481d-93f1-0609e28acf9c}' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-b8028` /use body '0 0 ' OR 1=1 --' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-89672` /use body '0 0 1d832403-ae04-481d-93f1-0609e28acf9cx' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-15d18` /use body '0 0 ../../1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-b5f84` /use body '-1 0 1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-e0ca4` /use body '0 -1 1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-df809` /use body '0 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-eb82b` /use body 'abc' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-use-14825` /use body '9223372036854775808 0 1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-recover-1ff3b` /recover body '0 0 1D832403-AE04-481D-93F1-0609E28ACF9C' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-recover-f9c4c` /recover body '0 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-recover-5965b` /recover body '0 0 1d832403-ae04-481d-93f1-0609e28acf9c 1d832403-ae04-481d-93f1-0609e28acf9c... | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-recover-85953` /recover body '0 0 1d832403-ae04-481d-93f1-0609e28acf9c nope' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-recover-94a28` /recover body '0 0 832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-cast-cddf8` /cast body '0 0 spell.Heal_Light' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-cast-ec179` /cast body '0 0 spell.' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-cast-a006f` /cast body '0 0 item.light_healing_potion' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-cast-ed2ce` /cast body '0 0 spell.aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-cast-ca2d7` /cast body '0 0 spell.heal light' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-cast-4cb95` /cast body '0 0 spell.a;b' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-cast-38d56` /cast body '0 0 ../spell.x' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-equip-60917` /equip body '0 0 1d832403-ae04-481d-93f1-0609e28acf9c NOSUCHSLOT' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-equip-5a1ce` /equip body '0 0 1d832403-ae04-481d-93f1-0609e28acf9c WEAPONX9' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-equip-6da2d` /equip body '0 0 1d832403-ae04-481d-93f1-0609e28acf9c weapon' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-equip-aacae` /equip body '0 0 1d832403-ae04-481d-93f1-0609e28acf9c WEAPON;' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-equip-83790` /equip body '0 0 1d832403-ae04-481d-93f1-0609e28acf9c AAAAAAAAAAAAAAAAA' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-equip-6222f` /equip body '0 0 1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-equip-961f3` /equip body '0 0 WEAPON 1d832403-ae04-481d-93f1-0609e28acf9c' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-unequip-a495` /unequip body '0 0 weapon' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-unequip-41476` /unequip body '0 0 WEAPONX9' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-unequip-e25fa` /unequip body '0 0 AAAAAAAAAAAAAAAAA' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-unequip-b2d9` /unequip body '0 0' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-unequip-13ff` /unequip body '0 0 NOSUCHSLOT' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-interact-911f5` /interact body '0 0 ../../etc/passwd' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-interact-8a15d` /interact body '0 0 AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-interact-6a216` /interact body '0 0 WardenAelric 100' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-interact-98a57` /interact body '0 0 WardenAelric -1' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-interact-6a028` /interact body '0 0 WardenAelric 1 2' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-interact-77bf1` /interact body '0 0 1Aelric' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-interact-5b0bb` /interact body '0 0 Warden Aelric' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-interact-f5aa2` /interact body '0 0 WardenAelric gold=999999' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-region-ready-7240a` /region-ready body 'abc' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-region-ready-5b14c` /region-ready body '-1' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-region-ready-5aba8` /region-ready body '' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-region-ready-667ab` /region-ready body '1 2' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-region-ready-84ae8` /region-ready body '99999999999999999999' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-region-ready-587cb` /region-ready body '9223372036854775808' | 400 (definite input refusal, never 2xx/5xx) | 400 input; state unchanged | PASS |
| `MAL-nil-uuid-pickup` /pickup with the all-zero UUID | 409 loot_unavailable (or stale revision) | 409 loot_unavailable; state unchanged | PASS |
| `MAL-unknown-npc` /interact with an unknown NPC id | 409 | 409 npc_unavailable; state unchanged | PASS |
| `MAL-unknown-spell` /cast spell.nonexistent | 409 (not learned or unsupported) | 409 not_learned; state unchanged | PASS |
| `MAL-method-put` PUT /input | 405 | 405 method; state unchanged | PASS |
| `MAL-method-delete` DELETE /character | 405 | 405 method; state unchanged | PASS |
| `MAL-post-character` POST /character (GET-only route) | 404 | 404 route; state unchanged | PASS |
| `MAL-unknown-route` POST /grant-gold | 404 | 404 route; state unchanged | PASS |
| `MAL-query-post` POST /input?x=1 | 400 | 400 input; state unchanged | PASS |
| `MAL-query-get` GET /character?gold=99999 | 400 | 400 input; state unchanged | PASS |
| `MAL-ctype/input` POST /input with application/json | 415 | 415 input; state unchanged | PASS |
| `MAL-ctype/equip` POST /equip with application/json | 415 | 415 input; state unchanged | PASS |
| `MAL-ctype/interact` POST /interact with application/json | 415 | 415 input; state unchanged | PASS |
| `MAL-ctype/attack` POST /attack with application/json | 415 | 415 input; state unchanged | PASS |
| `MAL-no-ctype` POST /input without Content-Type | 415 | 415 input; state unchanged | PASS |
| `MAL-origin-evil` POST /attack with foreign Origin | 403 | 403 origin; state unchanged | PASS |
| `MAL-origin-none` POST /use without Origin | 403 | 403 origin; state unchanged | PASS |
| `MAL-proto/pickup` POST /pickup without the protocol header | 426 | 426 protocol; state unchanged | PASS |
| `MAL-proto/cast` POST /cast without the protocol header | 426 | 426 protocol; state unchanged | PASS |
| `MAL-proto/use` POST /use without the protocol header | 426 | 426 protocol; state unchanged | PASS |
| `B-ALIVE after malformed input` B (native adapter) keeps playing after malformed input | connected, own identity, snapshot ticks advancing | status connected, ticks 1103->1122, gold 242 | PASS |
| `STALE-attack` /attack with a future revision against creature 1 | 409 revision (revision is checked before the target) | 409 revision; state unchanged | PASS |
| `STALE-attack-low` /attack with a past revision (0, or +1 when the revision is still 0) | 409 revision | 409 revision; state unchanged | PASS |
| `STALE-pickup` /pickup B's owner-window loot with a wrong revision | 409 revision | 409 revision; state unchanged | PASS |
| `STALE-cast` /cast spell.heal_light with a wrong revision | 409 revision | 409 revision; state unchanged | PASS |
| `STALE-use` /use A's own potion with a wrong revision | 409 revision | 409 revision; state unchanged | PASS |
| `STALE-unequip` /unequip WEAPON (equipped) with a wrong revision | 409 revision, weapon stays equipped | 409 revision; state unchanged | PASS |
| `STALE-interact` /interact WardenAelric with a wrong revision | 409 revision | 409 revision; state unchanged | PASS |
| `EPOCH/attack` POST /attack with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `EPOCH/pickup` POST /pickup with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `EPOCH/cast` POST /cast with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `EPOCH/use` POST /use with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `EPOCH/equip` POST /equip with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `EPOCH/unequip` POST /unequip with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `EPOCH/recover` POST /recover with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `EPOCH/interact` POST /interact with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `EPOCH/input` POST /input with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `EPOCH/region-ready` POST /region-ready with a stale/future location epoch | 409 region_epoch | 409 region_epoch; state unchanged | PASS |
| `B-ALIVE after stale epoch` B (native adapter) keeps playing after stale epoch | connected, own identity, snapshot ticks advancing | status connected, ticks 1283->1301, gold 242 | PASS |
| `B-ALIVE after stale revision/epoch` B (native adapter) keeps playing after stale revision/epoch | connected, own identity, snapshot ticks advancing | status connected, ticks 1301->1331, gold 242 | PASS |
| `BIG/input` POST /input with 65 bytes (cap 64) | 413 | 413 input; state unchanged | PASS |
| `BIG/attack` POST /attack with 65 bytes (cap 64) | 413 | 413 input; state unchanged | PASS |
| `BIG/region-ready` POST /region-ready with 65 bytes (cap 64) | 413 | 413 input; state unchanged | PASS |
| `BIG/leave` POST /leave with 65 bytes (cap 64) | 413 | 413 input; state unchanged | PASS |
| `BIG/recover` POST /recover with 129 bytes (cap 128) | 413 | 413 input; state unchanged | PASS |
| `BIG/cast` POST /cast with 129 bytes (cap 128) | 413 | 413 input; state unchanged | PASS |
| `BIG/interact` POST /interact with 129 bytes (cap 128) | 413 | 413 input; state unchanged | PASS |
| `BIG/use` POST /use with 97 bytes (cap 96) | 413 | 413 input; state unchanged | PASS |
| `BIG/equip` POST /equip with 97 bytes (cap 96) | 413 | 413 input; state unchanged | PASS |
| `BIG/unequip` POST /unequip with 97 bytes (cap 96) | 413 | 413 input; state unchanged | PASS |
| `BIG/pickup` POST /pickup with 97 bytes (cap 96) | 413 | 413 input; state unchanged | PASS |
| `BIG/join` /join with 102 bytes (cap 101) | 413 | 413 input; state unchanged | PASS |
| `BIG/account` POST /account/characters with 513 bytes (cap 512) | 413 | 413 input; state unchanged | PASS |
| `BIG-huge-input` POST /input with 128 KiB | 413 (or the connection closed without applying anything) | 413 input; state unchanged | PASS |
| `BIG-chunked-input` POST /input as a chunked stream of 4096 bytes (no Content-Length) | 413 | 413 input; state unchanged | PASS |
| `BIG-lie-length` raw POST /input declaring Content-Length 1000000 with a 5-byte body | 413 | 413 input; state unchanged | PASS |
| `BIG-dup-length` raw POST /input with two Content-Length headers | 400 | 400 <h1>400 Bad Request</h1>Conflicting or malformed headers detected; state unchanged | PASS |
| `BIG-neg-length` raw POST /input with Content-Length -1 | 400 (or the connection refused) | 400 <h1>400 Bad Request</h1>Illegal Content-Length value; state unchanged | PASS |
| `BIG-nan-length` raw POST /input with a non-numeric Content-Length | 400 (or the connection refused) | 400 <h1>400 Bad Request</h1>NumberFormatException thrown; state unchanged | PASS |
| `BIG-bad-host` raw POST /input with a foreign Host header | 403 | 403 origin; state unchanged | PASS |
| `BIG-alive` A's session and the server still respond after the oversize volley | A /state 200, /health 200 | 200 (body withheld); 200 {"capacity":20,"mode":"local-persistent","tick":1617} | PASS |
| `B-ALIVE after oversize bodies` B (native adapter) keeps playing after oversize bodies | connected, own identity, snapshot ticks advancing | status connected, ticks 1619->1633, gold 242 | PASS |
| `FORGE-attack-0` /attack id 0 with the current revision | 409 (creature_unavailable/protected/out of range) | 409 creature_unavailable; state unchanged | PASS |
| `FORGE-attack-1` /attack id 1 with the current revision | 409 (creature_unavailable/protected/out of range) | 409 out_of_range_or_blocked; state unchanged | PASS |
| `FORGE-attack-2` /attack A's own actor id with the current revision | 409 (creature_unavailable/protected/out of range) | 409 out_of_range_or_blocked; state unchanged | PASS |
| `FORGE-attack-3` /attack B's player actor id with the current revision | 409 (creature_unavailable/protected/out of range) | 409 out_of_range_or_blocked; state unchanged | PASS |
| `FORGE-attack-4` /attack nonexistent id 4e9 with the current revision | 409 (creature_unavailable/protected/out of range) | 409 creature_unavailable; state unchanged | PASS |
| `FORGE-attack-5` /attack Long.MAX_VALUE with the current revision | 409 (creature_unavailable/protected/out of range) | 409 creature_unavailable; state unchanged | PASS |
| `FORGE-attack-far` /attack a real creature (id 1) that is outside melee range | 409 out_of_range_or_blocked; creature health unchanged | 409 out_of_range_or_blocked; state unchanged | PASS |
| `FORGE-attack-far-health` the out-of-range creature's health is unchanged | same health | 84 -> 84 | PASS |
| `FORGE-pickup-random` /pickup a random UUID | 409 loot_unavailable | 409 loot_unavailable; state unchanged | PASS |
| `FORGE-pickup-B` /pickup B's ground loot inside B's owner window, standing on it | 409 loot_reserved, drop stays on the ground | 409 loot_reserved; state unchanged | PASS |
| `FORGE-pickup-B-db` B's loot row after A's attempt (state/owner is B/unclaimed/receipts) | ON_GROUND/true/true/0 | ON_GROUND/true/true/0 | PASS |
| `FORGE-recover-B-corpse` /recover B's corpse (it lies on A's tile) | 409 corpse_unavailable | 409 corpse_unavailable; state unchanged | PASS |
| `FORGE-recover-random` /recover a random corpse UUID | 409 corpse_unavailable | 409 corpse_unavailable; state unchanged | PASS |
| `FORGE-recover-B-item-on-A-corpse` /recover A's own corpse naming an item of B's corpse | 409 refusal, A's corpse intact | 503 busy; state unchanged | **FAIL** |
| `FORGE-recover-db` both corpses after the attempts | B corpse unrecovered with gold 777 and 1 item; A corpse u... | false/777/1 ; false/111/1 | PASS |
| `FORGE-use-B-item` /use B's inventory item instance | 409 item_not_owned | 409 item_not_owned; state unchanged | PASS |
| `FORGE-use-B-worn` /use B's worn item instance | 409 item_not_owned | 409 item_not_owned; state unchanged | PASS |
| `FORGE-equip-B-item` /equip B's item instance into A's WEAPON slot | 409 item_not_owned | 409 item_not_owned; state unchanged | PASS |
| `FORGE-equip-random` /equip a random item UUID | 409 item_not_owned | 409 item_not_owned; state unchanged | PASS |
| `FORGE-unequip-empty-slot` /unequip a slot A does not occupy (BELT) | 409 item_not_equipped | 409 item_not_equipped; state unchanged | PASS |
| `FORGE-cast-unlearned` /cast spell.stone_shard (A is a Paladin, not learned) | 409 not_learned | 409 not_learned; state unchanged | PASS |
| `FORGE-cast-item-spell` /cast spell.heal_critical (an item-only spell, not learned) | 409 not_learned | 409 not_learned; state unchanged | PASS |
| `FORGE-use-heal-full` /use B-independent: unknown item UUID | 409 item_not_owned | 409 item_not_owned; state unchanged | PASS |
| `FORGE-interact-far-npc` /interact Kilhiam from A's tile (out of NPC range) | 409 npc_unavailable, no letter granted | 409 npc_unavailable; state unchanged | PASS |
| `FORGE-interact-topic-far-npc` /interact Kilhiam topic 0 from A's tile | 409 npc_unavailable | 409 npc_unavailable; state unchanged | PASS |
| `B-ALIVE after forged targets` B (native adapter) keeps playing after forged targets | connected, own identity, snapshot ticks advancing | status connected, ticks 1848->1874, gold 242 | PASS |
| `IDEM-unequip-1` /unequip WEAPON with the current revision (valid) | 200 | 200 {"revision":11} | PASS |
| `IDEM-unequip-replay` replay of the exact /unequip request | 409 (applied at most once), equipment stays as after the ... | 409 item_not_equipped; state unchanged | PASS |
| `STALE-equip` /equip the unequipped weapon with a wrong revision | 409 revision, weapon stays unequipped | 409 revision; state unchanged | PASS |
| `IDEM-equip-1` /equip the weapon back (valid) | 200 | 200 {"revision":12} | PASS |
| `IDEM-equip-replay` replay of the exact /equip request | 409 (applied at most once) | 409 equipment_conflict; state unchanged | PASS |
| `IDEM-use-parallel` two identical /use requests released together on one potion | exactly one 200, potion count -1 | 200 {"manaGained":0,"revision":13,"healthGained":21,"manaSpent":0} / 409 busy; potions 3->2 | PASS |
| `IDEM-use-replay` later replay of the same /use request | 409 revision or item_not_owned, potion count unchanged | 409 revision; state unchanged | PASS |
| `IDEM-cast-1` /cast spell.heal_light (valid, learned) | 200 | 200 {"manaGained":0,"revision":15,"healthGained":0,"manaSpent":2} | PASS |
| `IDEM-cast-replay` replay of the exact /cast request | 409 (mana is not spent twice) | 409 revision; state unchanged | PASS |
| `STALE-recover` /recover A's own corpse with a wrong revision | 409 revision, corpse untouched | 409 revision; state unchanged | PASS |
| `IDEM-recover-1` /recover A's own corpse (valid) | 200, gold +111, +1 item | 200 {"revision":16}; gold 202->313, stacks 15->16 | PASS |
| `IDEM-recover-replay` replay of the exact /recover request | 409 (nothing taken twice) | 409 revision; state unchanged | PASS |
| `IDEM-recover-again` /recover the same corpse again with a fresh revision | no further gold/items (409, or a 200 no-op) | 200 {"revision":16}; state unchanged | PASS |
| `IDEM-recover-total` gold and stacks after the replays | exactly +111 gold and +1 item in total | gold 202->313, stacks 15->16 | PASS |
| `IDEM-quest-accept` /interact WardenAelric topic 0 (accept the quest; valid) | 200, flags = exactly the authored accept flags | 200 {"revision":18,"messageKey":"quest.renewed_wards.offer","changed":true,"args":[]}; flags... | PASS |
| `IDEM-quest-replay` replay of the exact accept request | 409 revision, flags unchanged | 409 revision; state unchanged | PASS |
| `IDEM-quest-again` accept again with a fresh revision | 200 progress line, no flag/gold/xp change | 200 {"revision":18,"messageKey":"message.quest_progress_dialog","changed":false,"args":[0,12... | PASS |
| `IDEM-quest-greet` greeting (turn-in attempt) with the kill goal unmet | 200 welcome/progress line, no reward | 200 {"revision":18,"messageKey":"npc.welcome.wardenaelric","changed":false,"args":[]}; state... | PASS |
| `IDEM-quest-lore` topic 1 (lore) | 200, no change | 200 {"revision":18,"messageKey":"npc.topic.wardenaelric.1","changed":false,"args":[]}; state... | PASS |
| `IDEM-quest-topic2` topic 2 (does not exist) | 409 npc_unavailable, no change | 409 npc_unavailable; state unchanged | PASS |
| `IDEM-quest-topic3` topic 3 (does not exist) | 409 npc_unavailable, no change | 409 npc_unavailable; state unchanged | PASS |
| `IDEM-quest-topic9` topic 9 (does not exist) | 409 npc_unavailable, no change | 409 npc_unavailable; state unchanged | PASS |
| `IDEM-quest-topic10` topic 10 (does not exist) | 409 npc_unavailable, no change | 409 npc_unavailable; state unchanged | PASS |
| `IDEM-quest-topic50` topic 50 (does not exist) | 409 npc_unavailable, no change | 409 npc_unavailable; state unchanged | PASS |
| `IDEM-quest-topic99` topic 99 (does not exist) | 409 npc_unavailable, no change | 409 npc_unavailable; state unchanged | PASS |
| `IDEM-quest-allowlist` A's quest flags after every dialogue attempt are inside the online allowlist and equal the a... | OnlineQuestFlags.supported == true; flags == pre-accept f... | flags {quest.renewed_wards.kills=0, quest.renewed_wards.status=1}; supported=true | PASS |
| `IDEM-input-seq` input sequence: send N+2, then N+1 (reordered), N+2 again (replay), N+2 with a different dir... | 200, then 409 sequence for each | 200 {"ok":true} / 409 sequence / 409 sequence / 409 sequence / 409 sequence | PASS |
| `IDEM-create-replay` POST /account/characters twice with the same requestId, then again with a changed name | at most one character created | 200 (body withheld) / 200 replay / 409 creation_conflict; roster 1->2 | PASS |
| `B-ALIVE after replay/idempotency` B (native adapter) keeps playing after replay/idempotency | connected, own identity, snapshot ticks advancing | status connected, ticks 2277->2296, gold 242 | PASS |
| `EDIT-equip-extra` extra trailing token on /equip | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-use-qty` a quantity token on /use | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-use-neg` negative quantity | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-cast-power` a power/damage token on /cast | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-interact-gold` gold=999999 token on /interact | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-interact-flag` a quest-flag assignment on /interact | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-attack-damage` a damage token on /attack | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-recover-gold` a gold token on /recover | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-input-speed` a speed multiplier on /input | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-input-pos` a position on /input | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-json-state` a JSON progress object in the body | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-json-stats` a JSON stats object in the body | 400 (the grammar carries no stat/gold/flag/item fields) | 400 input; state unchanged | PASS |
| `EDIT-create-gold` character creation with an extra 'gold' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-level` character creation with an extra 'level' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-strength` character creation with an extra 'strength' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-hp` character creation with an extra 'hp' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-x` character creation with an extra 'x' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-world` character creation with an extra 'world' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-progress` character creation with an extra 'progress' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-inventory` character creation with an extra 'inventory' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-questFlags` character creation with an extra 'questFlags' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-experience` character creation with an extra 'experience' field | 400 creation_input | 400 creation_input; state unchanged | PASS |
| `EDIT-create-dup-field` character creation with a duplicated field | 400 | 400 creation_input; state unchanged | PASS |
| `EDIT-create-class` character creation of an unsupported class (mage) | 400 unsupported_class | 400 unsupported_class; state unchanged | PASS |
| `EDIT-create-count` characters on A's account after the creation tampering | unchanged | roster 2->2 | PASS |
| `EDIT-final-stats` A's attributes, level, skills and spells after the whole tamper battery | identical to provisioning | attributes equal=true, advancement equal=true, skills equal=true, spells equal=true | PASS |
| `B-ALIVE after stat/item/quest edit attempts` B (native adapter) keeps playing after stat/item/quest edit attempts | connected, own identity, snapshot ticks advancing | status connected, ticks 2462->2481, gold 242 | PASS |
| `SPEED-vec-100_100` /input direction vector '100 100' | 400 | 400 input | PASS |
| `SPEED-vec-2_0` /input direction vector '2 0' | 400 | 400 input | PASS |
| `SPEED-vec-0_-2` /input direction vector '0 -2' | 400 | 400 input | PASS |
| `SPEED-vec--2_-2` /input direction vector '-2 -2' | 400 | 400 input | PASS |
| `SPEED-vec-1000000_0` /input direction vector '1000000 0' | 400 | 400 input | PASS |
| `SPEED-vec-1.5_0` /input direction vector '1.5 0' | 400 | 400 input | PASS |
| `SPEED-spam` spam 19 east inputs (19 accepted, 0 rate-limited) over ~1.5 s and measure the displacement | moved at most 21.78 tiles per axis (77.0 ticks x 0.2757 + 2) | dx=1.38 dy=0.00 over 77 ticks | PASS |
| `SPEED-walls` walk east/north/west/south/diagonals into walls and the region boundary for 24 s, sampling 1... | every sample standable and inside the hosted rectangle; p... | no violations; max step 0.2757 tiles/tick | PASS |
| `SPEED-world` A's committed world and session after walking | still world 0, connected | committed world 0; /state 200 | PASS |
| `SEQ-jump` /input with sequence Long.MAX_VALUE, then a normal sequence | accepted once (own session only); later inputs 409 sequen... | 200 {"ok":true} / 409 sequence | PASS |
| `SPEED-B` B's committed record is unaffected by A's movement tampering | B fingerprint unchanged (A's own state is allowed to diff... | B unchanged | PASS |
| `B-ALIVE after movement tampering` B (native adapter) keeps playing after movement tampering | connected, own identity, snapshot ticks advancing | status connected, ticks 3614->3638, gold 242 | PASS |
| `B-PLAY-dialogue` B's native /interact Kilhiam after the whole battery | accepted, exactly one letter, story flag set once | accepted=true, message npc.kilhiam.letter, letters 1 x1 | PASS |
| `B-PLAY-move` B moves through the native adapter and the move is committed | position changes and is persisted | native 226.50,29.50->228.15,29.50; committed 2961.50,1058.50->2963.15,1058.50 | PASS |
| `B-PLAY-final` B's attributes, level, skills, spells, storage and gold versus provisioning | identical except the one authored letter and its flag | gold 242->242, attributes equal=true, advancement equal=true, skills equal=true | PASS |
| `DB-loot-B` B's seeded owner-window drop at the end of the run | ON_GROUND, owner B, never claimed, no receipt | ON_GROUND/true/0 | PASS |
| `DB-corpse-B` B's corpse at the end of the run | unrecovered, 777 gold, 1 item | false/777/1 | PASS |
| `DB-corpse-A` A's own corpse at the end of the run | recovered exactly once (gold 0, empty) | true/0/0 | PASS |

## Movement rule change (T4C-0277)

The server now moves players with single-player's grid-reservation rule (rollback:
`-Dt4c.server.movement=swept`). The recorded run above used the swept rule and was not repeated.
The speed assertion is unchanged and stays valid under the new rule: a tick still covers at most
0.2757 tiles per axis (300 px/s at 34 Hz; leftover budget after finishing a tile is spent on the
next reservation, never added), so `SPEED-spam` (displacement bound) and `SPEED-walls` (max step
0.2757, every sample standable) hold by construction and are proved headlessly by
`ServerMovementParityTest`. Two observable differences for the next live run: a released key
finishes its tile (the actor keeps gliding for up to 3.6 ticks), and blocked steps slide instead
of stopping, so the samples of `SPEED-walls` along a wall are no longer constant. The probe was
not run for this change.
