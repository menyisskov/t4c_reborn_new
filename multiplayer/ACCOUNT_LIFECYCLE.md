# Online account roster

Delivery status (2026-10-27): this document describes pending implementation in the
working tree based on `397d9ace`. That runtime/tooling is not yet merged. Recorded
checks apply only to the stated build; this documentation does not mark a milestone
complete or make the corresponding code available in an earlier release.

T4C-0196 adds authenticated account discovery without changing the existing operator-issued key model. This is a roster service and network adapter; a native selection menu, public registration, character creation/deletion and credential recovery UI are not implemented.

## Existing credentials and authority

Operators issue a cryptographically random 32-byte account access key through the existing provisioning command. PostgreSQL stores its SHA-256 verifier, not the key. These credentials are random keys, not user passwords. Existing private profile files contain the account key and a preferred character UUID. Do not print, commit or put keys in command-line arguments.

The account endpoint derives ownership from the verified key. An account UUID, character UUID or gameplay session token cannot substitute for the account key. Reading the roster neither acquires a character write lease nor consumes a realm player slot. Existing `/join` still authenticates the selected character and checks its current lease and hosted destination. A roster availability flag is advisory, not authorization to enter.

## Wire contract

`GET /account/characters` returns the first page. The only accepted query is `?after=<canonical-lowercase-UUID>` for continuation. Unknown/duplicate parameters, encoded alternatives, empty cursors and malformed UUIDs are rejected. Other routes continue rejecting query strings.

Send exactly one `X-T4C-Protocol` header equal to `OnlineProtocol.VERSION`, and exactly one `Authorization: Bearer <64-lowercase-hex-account-key>` header. The server verifies protocol before inspecting credentials. Native callers first discover `/map` anonymously and require a persistent server with the exact protocol version before sending an account key. Existing HTTPS hostname/trust validation applies; non-loopback plaintext is refused. Credentials are never placed in URLs.

The shared immutable reply is:

```java
OnlineRoster(List<CharacterSummary> characters, UUID nextAfter)
CharacterSummary(UUID id, String name, String characterClass,
                 String gender, Integer level, int world, String availability)
```

Pages contain at most `OnlineRoster.PAGE_SIZE` (20) entries. Ordering is PostgreSQL UUID byte order, equivalently lexicographic canonical UUID strings; do not use Java UUID's signed `compareTo` for pagination checks. The server reads one extra row to determine continuation. A non-null nextAfter equals the last returned UUID. A null/omitted nextAfter ends the roster.

All existing accounts remain readable, including accounts with more than the offline three-slot creation limit. This endpoint does not delete, initialize, reroll or hide legacy movement-only characters. Their class/gender/level are null (possibly omitted in JSON). Replies contain no account ID, access verifier, session/lease identifier, inventory, quests or other owner's characters.

Availability values:

| Value | Meaning |
| --- | --- |
| `ready` | Initialized character has a supported location/respawn and no active database lease at read time. |
| `in_use` | Same checks pass, but an active character lease currently exists. |
| `uninitialized` | Legacy character has no gameplay payload; it is listed without silently granting a kit. |
| `unhosted_region` | The saved location is outside the hosted/standable region set. |
| `unavailable_respawn` | The saved respawn is absent or unavailable. |

Multiple pages are separate reads, not a long-lived database snapshot. If operators change a roster during pagination, refresh from the first page to discover new rows. Joining always revalidates the selected character.

Errors are bounded JSON error codes: 400 `input` for malformed query, 401 `account` for invalid credentials, 426 `protocol` for incompatible/missing version, 429 `account_rate` for peer admission throttling, and 503 `account_busy`/`account_unavailable` for bounded-worker or database unavailability. Temporary preview servers return 404 because they have no account service. Invalid keys and gameplay session tokens receive the same authentication error.

## Resource and lifecycle boundaries

Account work uses its own two-worker executor and a 20-job queue. It never runs on the simulation thread or occupies gameplay persistence workers. Queued requests cancelled after an HTTP timeout are skipped before database access. Queries have a two-second query timeout, and the HTTP handler waits at most three seconds for its result. Existing connection/socket/server request limits remain in force. Closing the service rejects new requests, completes queued/running response futures exceptionally, interrupts workers, and waits up to 12 seconds for their exit before reporting a termination failure. The database pool must remain open until server shutdown has fenced these workers; cancelling a response future alone does not prove a JDBC operation has ended.

The peer limiter keys actual socket peer addresses, never a client-supplied forwarding header. It permits a burst of 40 attempts and refills one credit every two seconds, stores at most 1,024 peers, and expires idle entries after five minutes. A full limiter refuses new peers rather than evicting active entries to bypass their limits. These are bounded-service controls, not a complete Internet denial-of-service guarantee or an account-lockout policy.

The store holds an account share lock while authenticating/reading so key rotation cannot interleave an already authenticated roster read. Rotation already revokes account character leases transactionally; a subsequent roster request with the old key fails. Summaries use database time for active-lease status. Reads do not renew leases or change character revisions.

## Native adapter and next UI work

`OnlineAccountClient(endpoint, accountKey).page(UUID after)` synchronously returns one `OnlineRoster`; call it from a bounded UI/network worker, never the render thread. It is AutoCloseable, serializes its own requests, validates `OnlineEndpoint`, uses standard TLS trust and refuses redirects. Each page performs anonymous protocol/persistent discovery before transmitting credentials. The adapter bounds map replies to 1 MiB and roster replies to 64 KiB, applies a two-second whole-response deadline per HTTP request (up to roughly four seconds for discovery plus roster), and rejects repeated/nonadvancing cursor data. `OnlineAccountClient.Rejected.statusCode()` exposes an expected HTTP refusal without logging the credential.

The adapter does not read/write profile files, acquire game leases, create characters, render UI or automatically iterate every page. A later online-only lobby should use the existing private key/profile, display owned summaries, select a character and hand its key/UUID to `OnlineConnection`. Returning to selection must finish or explicitly report the bounded leave/lease-expiry path. Keep offline `CharacterSelectionScreen`, `LocalCharacterStore`, local saves and normal launch untouched.

## Focused verification and remaining work

With the private T4C_DB_* test environment configured, run from the repository root:

```powershell
mvn -B -f multiplayer/pom.xml '-Dtest=AccountRosterTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
mvn -B '-Dtest=OnlineAccountClientTest' test
```

Tests cover bounded peer memory/refill, saturated independent worker queues, 21-row pagination including legacy entries, account isolation, active-lease summaries, protocol-before-credential rejection, rotation invalidation and absence of lease acquisition/private payloads. Adapter tests cover its discovery, authentication, pagination and bounded-response contract. Record actual outcomes in the release ledger; merely adding these tests is not evidence they passed.

Next slice: native online-only selection UI, followed by authenticated idempotent creation using `CharacterCreationRules`, shared class kits and server-generated stats/items/gold. Creation needs a receipt, account-level serialization and the existing roster/name rules; it must not trust client progress or silently modify older operator-created rows. Public registration/key delivery/recovery and operator rotation UX remain explicit later account-lifecycle work. No OAuth, password scheme or external account service is introduced here.
