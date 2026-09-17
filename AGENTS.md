# AGENTS.md — PR Review Protocol

This file governs any automated agent (the `claude-review` GitHub Actions
workflow, or a Claude Code session asked to review or babysit a pull request
in this repo) that posts review comments on a pull request here. It does
**not** relax or replace `AGENT.md` (the general engineering charter) — it
adds review-specific scope rules that take precedence over a reviewer's own
instincts about what's "worth mentioning."

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

## 4. What "approved" means for auto-merge

This repo merges a PR automatically once its required checks pass,
including the automated review check. That check should only report
approval when:
- No comment from the original review is still outstanding (all are fixed,
  or a human has explicitly overridden one), and
- CI (`Build and test`) is green on the current head.

Do not approve a PR with any unresolved original-review comment still
open, and do not hold up a PR over anything that falls outside Sections 1–2
of this file.
