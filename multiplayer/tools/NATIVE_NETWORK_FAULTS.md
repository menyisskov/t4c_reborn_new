# Native network fault probe

Delivery status (2026-10-27): this document describes pending implementation in the
working tree based on `397d9ace`. That runtime/tooling is not yet merged. Recorded
checks apply only to the stated build; this documentation does not mark a milestone
complete or make the corresponding code available in an earlier release.

`NativeNetworkFaultProbe.java` exercises two real `OnlineConnection` instances from
the native game client without opening rendering windows. It creates two disposable
online accounts/characters in the configured PostgreSQL realm. It never reads or
writes offline saves/preferences or exports account keys.

The owner connects through a bounded loopback HTTP shim. The observer connects
directly to the same server. The shim delays GET responses by 150 ms, then forwards
one successful Kilhiam `/interact` request to completion and withholds its reply for
three seconds, beyond the native adapter's two-second deadline. The probe checks:

- Both adapters continue receiving snapshots with the delayed reads.
- The server commits one letter/quest flag, while the owner sees an ambiguous failure.
- The owner disconnects and sends no automatic replay of the failed mutation.
- Observer snapshots continue advancing and its persistent progress remains unchanged.
- Reconnecting after normal logout, or actual lease expiry if necessary, loads the
  same single letter UUID and the same committed owner progress/balances.

The reconnect budget is 40 seconds, with bounded connection attempts. No lease is
edited or stolen to accelerate recovery. The successful reconnect does not issue
another interaction; this distinguishes recovery reads from mutation replay. A normal
409 before the injected fault may be explicitly retried at most five times.

## Run locally

First coordinate server/client builds and start the persistent local QA server with
the current code. Use the same `T4C_DB_URL`, `T4C_DB_USER` and `T4C_DB_PASSWORD` as that
server, supplied privately in the environment. A test database different from the
running server's database will not work. Do not put passwords in commands or logs.
From the repository root, after the desktop and multiplayer artifacts are built:

```powershell
$probeClasspath = 'target/classes;multiplayer/server/target/classes;multiplayer/server/target/lib/*;' + (Get-Content target/classpath.txt -Raw).Trim()
New-Item -ItemType Directory -Force target/network-fault-probe | Out-Null
javac -cp $probeClasspath -d target/network-fault-probe multiplayer/tools/NativeNetworkFaultProbe.java
java -cp ('target/network-fault-probe;' + $probeClasspath) com.perso.T4C.server.NativeNetworkFaultProbe 18765
```

Only a port between 1024 and 65535 is accepted; the target is always
`http://127.0.0.1:PORT`. The shim binds another loopback-only ephemeral port and accepts
only the fixed native paths/methods this scenario uses. It rewrites Origin to the fixed
target, forwards only authentication/protocol headers, rejects query strings, caps
requests at 128 bytes and backend replies at 2 MiB, and uses four workers with a
16-request queue. Header/body/response deadlines and connection limits bound malformed
local requests. Headers, credentials and request/response bodies are never logged.

An expected native disconnect diagnostic may appear during the withheld reply. A
passing run ends with the tool's `PASS two real OnlineConnection adapters` message.
Failed assertions exit unsuccessfully. Disposable online records remain in PostgreSQL
for inspection; the tool never deletes account or character data and does not stop
the target server.

## Evidence boundary

This is deterministic HTTP application-level delay and lost-reply evidence on
loopback. It is not TCP/UDP packet loss, congestion, out-of-order packets, TLS/WAN
validation, a server/database crash, rendered two-window gameplay or a sustained
20-player soak. It does not prove arbitrary command recovery. The consequential
mutation tested is the existing authored first-letter reward only. Record the run
date, outcome and current code revision when using this as Beta evidence; an unrun
tool is not acceptance evidence.

## Recorded local result

2026-10-27, approximately 10:05 Asia/Jerusalem: the probe passed against the local
persistent QA server on port 18765 using the real `t4c_online` PostgreSQL realm.
The tested working tree was based on `397d9ace` (PR #119 merged), plus pending
account-roster, database-pool and simulation-metrics changes. The full PostgreSQL
reactor verification/package passed immediately beforehand: 18 domain and 81 server
tests, with zero failures, errors or skips. These results describe that working tree,
not a subsequently merged release.

The expected owner request-timeout diagnostic occurred, followed by the probe's PASS
message. The observer remained connected, the withheld committed interaction was not
automatically replayed, and recovery retained the exact single letter UUID and owner
progress. The separate `NativeRosterProbe` also passed owner-only roster discovery,
active-lease reporting and two selected-character joins without progress changes.
The QA server remained healthy afterward, with an empty error log. No GUI was opened;
the rendering and actual packet-loss limitations above still apply.
