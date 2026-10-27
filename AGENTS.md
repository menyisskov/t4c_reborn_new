# AGENTS.md — Agent Policy and PR Review Protocol

Sections 1–4 govern automated agents that review or babysit a pull request.
Section 5 governs agents that author or modify tests. Section 6 is the canonical
model-routing policy for every agent working in this repository. Section 7 governs
milestone, task and evidence tracking for all multiplayer work. Section 8 governs peer ownership and handoffs. This file does **not**
relax or replace `AGENT.md` (the general engineering charter).

The two failure modes this file exists to prevent: a reviewer that wanders
outside the PR's actual purpose and buries real feedback under nitpicks and
pre-existing-code complaints, and a reviewer that re-litigates the same PR
from scratch on every push, re-raising things it already let go or piling on
new findings after the author has started responding to the first round.

## 1. Stay inside the PR's own diff and purpose

- Only comment on lines the PR actually changes, or on code whose behavior
  the PR's changes demonstrably affect (e.g. a caller of a method the PR
  changed). Do not review, critique, or suggest improvements to code the PR
  does not touch, however tempting — file a separate issue instead of a PR
  comment, or say nothing.
- Infer the PR's purpose from its title, description, and diff as a whole
  before reviewing line by line. A finding that's technically true but
  orthogonal to that purpose (a style preference, a "while you're here"
  refactor, a pre-existing bug the PR didn't introduce or touch) does not
  belong in the review.
- If the PR touches generated files, vendored code, or `.claude/skills/*`
  reference material, hold those to the standard the PR itself sets (does
  the generated output match its generator, is the reference content
  internally consistent) rather than general code-quality nitpicking.

## 2. Severity bar — real issues only

Post a comment only if it is one of:
- **Correctness**: the change will produce a wrong result, crash, or violate
  an invariant the surrounding code relies on.
- **Security**: the change introduces or worsens a real vulnerability.
- **Data/behavior integrity specific to this codebase**: a dangling
  reference (an item/spell/monster/quest key that doesn't resolve), a
  boostId/spellId collision, a broken registry contract — the kind of thing
  `game-director`'s quality bar already calls out for content PRs.

Do not post:
- Style/formatting nits the build's own tooling (spotless, compiler
  warnings) would catch or doesn't care about.
- Naming preferences, "consider extracting a method," or other taste calls
  with no functional consequence.
- Praise-only comments, or comments restating what the diff already makes
  obvious.
- Speculative "what if" scenarios with no plausible path to actually
  occurring given how this code is called.

When genuinely unsure whether something clears the bar, leave it out. A
missed nit costs nothing; a wrong or noisy blocking comment costs the
author's trust in every future review this agent posts.

## 3. One review round, then fix-verification only

This is the hard rule and it does not bend:

- The **first** automated review comment (or review-comment thread) an
  agent posts on a given pull request is the **original review**. Whatever
  issues it raises, in whatever number, is the complete set this PR will
  ever be reviewed against by an automated agent.
- On every subsequent automated pass on the same PR (triggered by a new
  commit, a re-run, or a check-in), the agent's only job is to check each
  previously-raised comment against the PR's current head: **fixed**,
  **not fixed**, or **fixed differently than suggested but the underlying
  issue is resolved**. Post a short status update (e.g. resolve the thread,
  or reply "fixed in <sha>" / "still present: <why>") — nothing else.
- The agent must not raise a new issue on a later pass that was not part of
  the original review, even if it is real, even if new commits introduced
  it, even if the agent only now notices it. If a later commit introduces a
  genuine new problem, that is a gap in the original review's coverage, not
  license to reopen the review — note it at most once, clearly labeled as
  new-and-out-of-protocol (e.g. "not part of the original review, flagging
  separately for a human"), and do not block merge on it.
- Before posting anything, an agent must check whether it (or a prior
  automated review run) has already commented on this PR. If yes, it is in
  fix-verification mode per this section, full stop, regardless of how much
  the diff has grown since.

This rule exists so a PR author can fix what was flagged and get to green
without an automated reviewer moving the goalposts every push.

## 4. What "ready to merge" means

There is no separate automated approval check in this repo. The PR's
driving agent (a Claude Code session; see `.claude/skills/steward/SKILL.md`
for its exact mechanics) merges directly, once:
- CI (`Build and test`) is green on the current head, with no merge
  conflict, and
- the steward has self-reviewed the change and handled any automated review
  findings already posted at merge time.

Do not wait for Codex review to start or finish. Its "🔄 Running" status does
not block a merge after the conditions above are met. A review posted after
merge can be handled as follow-up work if it identifies a real issue.

A steward's own considered skip is a legitimate way to clear a finding —
it does not require a human to sign off on each one. It is only legitimate
for a finding that doesn't clear Section 2's severity bar (or is otherwise
wrong/out of scope); a finding that does clear that bar must actually be
fixed and pushed, not skipped, before the PR is ready.

"Ready to merge" is not the same as "zero open comments." A PR with
consciously-skipped nitpicks is ready; a PR with an unaddressed Section-2
finding is not, and nothing outside Sections 1–2 should hold a PR up in
the first place.

## 5. Test scope when authoring changes

- Add a test for a real behavior, regression, data contract, or save
  compatibility risk that existing tests do not cover. Prefer one focused
  representative case per behavior. Add a second case only when it exercises
  a different failure mode or an important boundary.
- Before adding a test, search for existing coverage. Extend an existing
  focused test when that makes the behavior clearer; do not repeat its setup
  and assertions in a new method or class.
- Do not enumerate every input, direction, item, map, or NPC variation when
  they follow the same code path. Do not pin incidental implementation details,
  exact design numbers, or content counts unless the number itself is a game
  rule or integrity contract.
- Keep integration tests for consequential joins: save/load, quest and item
  references, registry uniqueness, map entrances, and other failures that
  could silently strand a player or corrupt progress.
- When removing tests, identify the surviving coverage for each removed
  behavior. Fewer tests is useful only if the suite still catches meaningful
  regressions.

## 6. Model routing for all agent work

Owner policy, added 2026-10-27. Choose the model by the work being performed,
including delegated work. These tier labels express the owner's routing preference;
they are not claims about current provider pricing or model availability.

| Work | Tier | Owner's preferred model names | Exact IDs where exposed by the current Codex agent tools |
|---|---|---|---|
| Coding and implementation, including implementing agreed fixes and tests | Cheap | `gpt-6-sol`, `haiku-5.5` | `gpt-6-sol`; resolve Haiku against the actual installed provider catalog |
| Bug investigation and root-cause analysis (RCA) | Mid-tier | `gpt-terra-6.1`, `gpt-sol-6`, `sonnet 5.5` | `gpt-6.1-terra`, `gpt-6-sol`; resolve Sonnet against the actual installed provider catalog |
| Deep architecture investigation, milestone planning and design | Top tier | `gpt-sol-6.1`, `opus 5-5` | `gpt-6.1-sol`; resolve Opus against the actual installed provider catalog |

Application rules:

- Read this section before selecting a model or delegating work. `AGENT.md` and
  `CLAUDE.md` refer here; do not maintain conflicting copies of the routing table.
- Split mixed requests into phases: a top-tier model may establish architecture or
  milestones, a cheap model implements the agreed plan, and a mid-tier model investigates
  a failing behavior. An architecture phase does not justify keeping every coding task
  on the top-tier model.
- The owner deliberately lists the Sol 6 model in both cheap coding and mid-tier RCA.
  Preserve that overlap rather than silently assigning a different preference.
- Use an available listed model from the requested tier. Preferred names are not always
  executable IDs: inspect the host's advertised model catalog, use its exact ID, and
  never invent a Claude/provider identifier or assume that a named model is installed.
- If one preferred model is unavailable, use another available model listed for that
  tier. If none is available, report that limitation rather than silently upgrading to
  a more expensive tier. Continue independent work that has an available authorized model.
- Where the current primary session cannot switch its own model, delegate the relevant
  phase to the correctly routed model through available agent tools. Do not claim that
  changing this document changed the running session or installed provider configuration.
- In Codex collaboration, an explicit model override requires `fork_turns="none"`.
  Give the agent a concise brief with repository path, required instruction files,
  scope/file ownership, constraints and validation expectations. Do not inherit an
  entire top-tier conversation just to perform a bounded coding task.
- Apply routing when starting or handing off a phase. Do not interrupt an already running
  test/build merely to change models; route the next implementation/investigation phase
  correctly. All tiers still follow the same engineering, security, review and test rules.

## 7. Multiplayer milestone checklists and task tracking

Read these documents before choosing the next multiplayer task or reporting completion:

| Document | Purpose |
|---|---|
| [multiplayer/MILESTONES.md](multiplayer/MILESTONES.md) | Canonical roadmap: Alpha, the intermediate two-player checkpoint, full 20-player Beta, essential GA and Post-GA; exact requirements, completion evidence and final two-player QA script. |
| [TASKS.md](TASKS.md) | Individual implementation task IDs, status, commits and changelog links. A completed task does not mean its parent milestone is complete. |
| [multiplayer/ACCEPTANCE.md](multiplayer/ACCEPTANCE.md) | Verified release evidence and unchecked Alpha/Beta/GA gates. |
| [multiplayer/HANDOFF.md](multiplayer/HANDOFF.md) | Model/session continuation: architecture, current working state, commands, ownership, failures, limitations and next work. |

The six two-player milestones are:

1. **M1 — Enter and interact:** online accounts, native character creation/selection and authored NPC interaction.
2. **M2 — Inventory and loot:** usable authoritative inventory/equipment, drops, pickup and ownership/quantity integrity.
3. **M3 — Complete authored adventure:** a full quest/reward loop and all required hosted routes, with durable progress.
4. **M4 — Supported combat and abilities:** required monster behavior and the declared starter roles' usable abilities.
5. **M5 — Reliability and anti-cheat:** safe shutdown/recovery, abuse/duplication protection, restored gameplay and measured stability.
6. **M6 — Package and native acceptance:** reproducible distribution/operations and the complete two-rendered-client playthrough with offline regression checks.

Use MILESTONES.md for the detailed criteria; do not create a competing checklist here.
Every relevant delivery must update its TASKS row, milestone status, ACCEPTANCE evidence
and HANDOFF continuation notes together. Record exact build/content/schema versions,
reproduction commands, observed results and limitations; keep credentials/personal state out.

Never mark a milestone complete from merged PR counts, unit/service tests or native network
adapters alone. Distinguish implemented, verified, merged, rendered-client and remote evidence.
Preserve failed runs and unchecked gates. The two-player checkpoint does not replace the
agreed 20-player Beta, and its exact content/class scope must be explicit before completion.
Single-player launch, gameplay, existing saves and preferences remain protected throughout.

## 8. Working with peer agents (Codex, Claude and sub-agents)

[multiplayer/PEER_WORK.md](multiplayer/PEER_WORK.md) is the shared ownership register.
Read it, TASKS.md and HANDOFF.md before starting or resuming work. Use the canonical
absolute register path recorded there when working from another worktree. Do not
assume external sessions share this chat's context, agent list or automatic messages.

- Claim a unique task and exact file/interface scope before editing. Record owner/session,
  status, branch/base, next action, resource needs and timestamp; reserve a new TASKS ID
  in the same update. A task reservation without a peer acknowledgement is not an active claim.
- Check both task and file ownership. Different tasks can still conflict in OnlineStore,
  PreviewServer, shared models, launchers, assets or global documentation. Request and record
  an explicit handoff before changing a peer-owned file; do not rely on disjoint line numbers.
- Recheck the register before each turn/work phase and before changing scope, editing,
  building, staging or merging. Update it on start, blocker, scope change, handoff,
  verification, pause and completion, and at least every ten minutes during active long work.
- Sub-agents inherit the parent owner's reservation only for their expressly assigned files.
  The parent records their assignments and remains responsible for preventing overlap.
  Follow section 6's model routing for every new delegated phase.
- Use one Git integrator per physical checkout. Other peers must not switch branches,
  stage/commit, pull/rebase/merge, stash/reset or clean that shared checkout. Agree a separate
  worktree/branch for independent Git work; keep semantic/file ownership coordinated there too.
- Reserve build targets, server ports, database/migration/load windows and desktop control
  separately. Separate worktrees do not isolate a shared database or QA server. Never stop,
  restart, migrate or clean a resource while a peer owns its window.
- Treat pre-existing dirty files as owned until identified. Stage only the integrator's
  reviewed delivery; never sweep peer work or personal saves into a commit or rollback.
- If an unexpected overlapping change appears, stop editing that file and resolve ownership.
  Do not overwrite, revert or reimplement it independently. Preserve the exact existing state.
- Peer messages are useful but do not replace the register. Record acknowledgements and
  handoffs there with commit/state, tests/evidence, remaining issues and the next owner.
  Do not claim a peer agreed or started merely because a request was sent.
- A stale timestamp, crashed agent or silence does not release ownership. Confirm the peer
  stopped and preserve its changes before the coordinator records a takeover. Do not use
  automatic expiry to steal a task from a slow or waiting agent.

### Short coordination lock

Serialize register/TASKS edits so two peers cannot claim the same work simultaneously.
For local Git worktrees, derive the common Git directory with
`git rev-parse --path-format=absolute --git-common-dir`, and use its
`peer-coordination.lock` directory as the lock. All peers must use that same directory.
Acquire with `New-Item -ItemType Directory -ErrorAction Stop` **without `-Force`**;
failure because it exists means wait and reread, not proceed. After acquiring, reread
the canonical register and TASKS before writing the narrow update. Release the empty
lock directory in `finally` with `Remove-Item -LiteralPath` (nonrecursive).

Hold this lock only for the short claim/register update, never during coding, tests,
CI waits or gameplay. A leftover lock requires explicit verified recovery by the
coordinator; age alone is not evidence it is abandoned. Peers in independent clones
or on different machines must agree a genuinely shared register/locking mechanism
before concurrent work; independent copies of this Markdown file provide no exclusion.
