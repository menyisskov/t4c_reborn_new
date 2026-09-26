# PR Steward Policy — t4c_reborn_new

Repo-specific instructions for any Claude Code session opening or driving a
pull request in this repository. Per the harness's own steward/babysit
precedence rules, this takes precedence over default PR-babysitting posture
on conventions and how proactive to be — it does not grant new access, and
it does not override anything the harness states as a hard "never" (no
skipping/disabling tests, no rewriting someone else's history, etc.).

## Review engine

**Automated PR review is currently disabled in this repo** (owner's call, 2026-09-26):
there is no reviewer to wait for, and green CI on the current head is the merge bar.
Everything below describes the protocol for when it is switched back on.

This repo's automated PR reviewer is **Codex** (`chatgpt-codex-connector[bot]`),
triggered automatically on PR open/sync/reopen. There is no Claude-based
review workflow here — don't add one back unless the user asks for it again.
`AGENTS.md` at the repo root is the scope/severity contract any reviewer
(Codex included) is expected to follow; use it to sanity-check whether a
Codex finding is legitimately in scope, not just to judge your own review
comments if you're ever the one leaving them.

## After opening a PR

1. Subscribe to the PR and wait for Codex's review to complete — its status
   comment moves from "🔄 Running" to "✅ Completed".
2. Read every finding Codex posted. For each one, use your own judgment —
   there is no rule that decides this for you:
   - **Fix it** if it's a real correctness, security, or data/behavior
     integrity issue (the same bar `AGENTS.md` sets for what a reviewer
     should even raise). Push the fix as a normal commit on the PR branch.
   - **Skip it** if it's a nitpick, out of scope, or you disagree with it.
     You do not need the user's sign-off to skip a finding, and you do not
     need to justify every skip in a comment — reserve a reply for a
     finding substantial enough that a reasonable author would want to
     explain the call.
   - There is no round limit and no obligation to reach "zero open
     comments." The bar is "nothing important left unaddressed," not
     "every comment resolved."
3. If you pushed fixes, Codex will typically re-review the new commit. Per
   `AGENTS.md` Section 3, that pass is fix-verification only against the
   findings from step 2 above — not a license to raise anything new, even
   something the fix commit itself introduced. If Codex raises a new
   finding on a later pass anyway, that's out of protocol on Codex's part;
   use your own judgment on whether it's worth a follow-up (it never blocks
   *this* merge under Section 4, since it wasn't part of the original
   review), and consider flagging the out-of-protocol behavior to the user
   if it keeps happening.

## Merging

Once all of the following hold, merge the PR yourself — don't wait for a
human to click merge:
- CI (`Build and test`) is green on the current head.
- There is no merge conflict.
- No automated review is "🔄 Running" (with Codex disabled, there never is one).
- You've made a judgment call on every Codex finding: fixed what mattered,
  consciously skipped the rest.

Use `merge_pull_request` directly. This repo does not have GitHub's native
auto-merge toggle enabled, so don't rely on `enable_pr_auto_merge` — it will
fail; merge directly instead.

Never merge while an enabled reviewer is still running, while CI is red, or while an
important finding you judged worth fixing hasn't actually been pushed yet.
