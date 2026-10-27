# Peer work register — Codex and Claude

Canonical live register for this machine:
`C:/Users/menyi/repos/t4c_reborn_new/multiplayer/PEER_WORK.md`.
Agents in another worktree must read/update this canonical file, not a stale worktree copy.
This register records declared ownership, not automatic proof that an external agent is alive.

Last coordination update: 2026-10-27 10:37 Asia/Jerusalem.
Claude subsequently published its ownership in merged PR121 (db83004b):0201 scope/loot design
and0202 loot domain rules. This is evidence of that lane claim. Adoption of this new canonical
register/locking protocol is not yet acknowledged by Claude; record that acknowledgement separately.

## Current assignments

| Owner | Task/lane | State | Owned scope | Next action / dependency |
| --- | --- | --- | --- | --- |
| Codex coordinator and its named sub-agents | T4C-0196, T4C-0197, T4C-0198; M1 account backend and M5 reliability/metrics | Implemented, integration/verification/delivery in progress | Existing pending account, pool, shutdown, metrics and networking-probe files listed below | Finish measured smoke, scoped self-review, CI and merge; release shared interfaces explicitly |
| Codex coordinator | T4C-0203 peer coordination | Documented; delivery in progress | AGENT.md, AGENTS.md, CLAUDE.md and shared tracking/coordination documents | Establish these rules and the live register, then integrate documentation |
| Claude | T4C-0201 scope/loot design; T4C-0202 M2 loot domain rules | Claimed via merged PR121;0202 implementation planned | New multiplayer/SCOPE_PROPOSAL.md, multiplayer/LOOT_DESIGN.md; new loot-rule files under multiplayer/domain only | Acknowledge canonical register; declare exact new files; server loot wiring waits for Codex core delivery |

T4C-0201/0202 form the claimed independent workstream, not a claim that content rules or UI design
are approved. Use existing item/loot rules, preserve offline behavior, and follow AGENT.md visual
preview requirements. Shared kill/claim rules requiring a product decision remain explicit.
Codex will not independently start M2 while this reservation stands.

## Codex's currently reserved files and operations

Until an explicit handoff, Claude must not edit these files, including in a separate worktree:

- `multiplayer/server/pom.xml`, `multiplayer/stop-preview.ps1`.
- Server `HostedWorlds`, `PersistentCreatures`, `PersistentSessions`, `PreviewServer`, `ServerMain`,
  `SimulationLoop`, `AccountRosterService`, `ServerRuntimeMetrics`.
- Persistence `OnlineStore`, `OnlineDatabasePool`, `PersistenceFailure`.
- Their existing/new tests, including `HostedTransferTest`, `PersistentSessionsTest`,
  `PreviewServerTest`, `ServerTransportTest`, `SimulationLoopTest`, `AccountRosterTest`,
  `OnlineDatabasePoolTest`, `PersistenceFailureTest`.
- Native `OnlineAccountClient`, shared `OnlineRoster`, and `OnlineAccountClientTest`.
- `multiplayer/tools/NativeAdapterSoak.java`, its documentation, the roster/network-fault probes
  and their documentation. These tools establish the current integration evidence.
- Shared task, changelog, milestone, acceptance, design and handoff documents: Codex is the
  current integrator. Peers supply narrow proposed updates or edit them under the short lock
  after confirming an agreed update window; never overwrite one another's entries.

The reservation covers these classes/files and their current changes, not every new class in
the same directory. Claude can claim distinct new M2 files after checking dependencies. If M2
needs an owned core file, record the requested interface/file handoff and wait for its owner to
acknowledge; a different task ID alone does not authorize editing the same file.

## Shared resources and integration

| Resource | Current owner/state | Rule |
| --- | --- | --- |
| Git branch, index, commits, PRs and merges in this physical checkout | Codex is the sole integrator | Claude must not switch branches, stage, commit, pull/rebase/merge, stash, reset or clean here; use a separately agreed worktree for independent Git operations |
| Maven/root and multiplayer target outputs | No build currently running; acquire a window before use | One owner per target/output directory; no clean while copied/running QA runtimes depend on target |
| Main QA server `127.0.0.1:18765`, QA DB `t4c_online` | Codex T4C-0197 pooled smoke window; PID 38232, 2026-10-27 10:34 Asia/Jerusalem | 180-second 20-adapter smoke and target report output reserved; no concurrent QA provisioning/load/stop/restart/schema changes |
| Test DB `t4c_test` | Codex integration window | Serialize migrations, data-mutating suites and database restart drills with the current owner |
| Isolated server `18766` / preserved failed soak runtimes | Stopped; reserved evidence | Preserve immutable reports/binaries; do not restart/replace without a declared new run |
| Desktop game automation | Paused by owner | Peer coordination does not revoke the physical Escape stop or approve pending visuals |

Separate worktrees isolate files/builds but do not automatically isolate databases, ports, assets,
tasks or semantic ownership. Declare dedicated resources before using them.

## Claim and update procedure

1. Read AGENT.md, AGENTS.md, CLAUDE.md, this canonical register, TASKS.md and HANDOFF.md.
   Inspect Git status; identify dirty peer/user files before touching anything.
2. Acquire the short coordination lock described in AGENTS.md section 8. Reread the register
   after acquisition, then add/update task ID, owner/session, state, exact file scope, base
   commit/branch, resource window, next action and last-update timestamp. Reserve new task IDs
   in TASKS.md in the same protected update. Release the lock before coding or running tools.
3. Recheck ownership at the start of every turn/work phase and before widening scope, edits,
   builds, staging/commits or merges. Update on start, blocker, scope/handoff change, verification
   result, pause and completion; refresh at least every ten minutes during sustained active work.
4. A handoff requires the current owner's explicit acknowledgement in this register or an
   accessible peer message referenced here. Record released files/resources and exact commit/state.
   Silence, an old timestamp, an errored agent or a finished task title is not permission to take over.
5. Stop editing a conflicting file when unexpected peer changes appear; preserve them and resolve
   ownership first. Never reset/stash/remove a peer's work or stage unrelated files.

## Message / handoff log

- 2026-10-27 10:34 Asia/Jerusalem: Codex T4C-0197 pooled smoke window claimed for main QA server PID 38232, `t4c_online`, and `target/native-adapter-soak-0197-pooled-smoke.json`; health ticks advanced 34/s, zero established TCP connections and zero unexpired character leases before launch. Release after report.


- 2026-10-27 10:30: owner says Claude has not started. Codex reserves current0196–0198 integration
  and offers Claude the distinct M2 lane as0202. Claude must acknowledge/claim before starting.
- Working base is main merge `c11ca7a6`; current runtime changes remain unmerged. Earlier full
  PostgreSQL verification after shutdown correction passed18domain+82server tests and native
  roster/fault probes; see HANDOFF.md and the recorded logs. No rendered/Beta completion claim.

Claude lane acknowledgement: **merged PR121**, branch claude/agent-lanes, main merge db83004b.
Canonical-register/lock acknowledgement: **pending**. Add session, exact new-file scope and resource
needs under the short lock. Preserve Codex core reservations while proceeding on the claimed lane.

- 2026-10-27 10:37: reconciled PR121's independently published task rows. Claude keeps0201/0202;
  Codex coordination documentation is renumbered0203. Original doc commits retain their old0201
  message as history; no rewrite/force-push. The task descriptions and lane split are preserved.
