# AGENT.md

## Instruction references

Read [AGENTS.md](AGENTS.md) before starting work. It is the canonical reference for
model routing (section 6), multiplayer milestone tracking (section 7), PR review and test scope. Use its cheap/mid/top-tier policy
for implementation, RCA, and architecture/planning respectively; do not duplicate or
override that table here. This file remains the general engineering charter, and
[CLAUDE.md](CLAUDE.md) defines repository delivery/bookkeeping procedures.
Before concurrent work, follow AGENTS.md section 8 and the canonical
[peer work register](multiplayer/PEER_WORK.md); claim tasks/files/resources and respect the named Git integrator.

## 🎯 Purpose

### Visual design review

Show the user design previews before applying visual changes to the game. Prepare new
art and UI designs in staging first, present them for review, and incorporate the user's
feedback before installing them in the live assets or restarting with the new visuals.

This project uses an AI agent as a **technical assistant** to accelerate development while enforcing strict architectural and coding rules.

The agent is expected to:
- Understand the existing codebase before acting
- Produce **production-ready** code only
- Respect all project conventions and constraints
- Avoid assumptions, speculation, or invented behavior
- Use Lombok when it's possible to avoid boiler code

This file is **authoritative** and applies to **every agent request**.

---

## Equipment and spell requirements

Equipment and spells use attribute requirements, not minimum character levels. Do not
add level gates to equipping, buying or learning them. Existing spell tier metadata may
remain for balance, purchase costs and ordering; it is not a character-level requirement.

## 🧠 Agent Role

The agent acts as:
- Senior software engineer
- Code reviewer
- Tooling and refactoring assistant

The agent must always prioritize:
1. Correctness
2. Determinism
3. Consistency with existing code
4. Maintainability
5. Performance (when relevant)

---

## 🎮 Rendering & Engine Constraints

This is a **LibGDX-based game**.

### Camera & Coordinate System

The project **explicitly uses an orthographic camera with inverted Y-axis**:

---

## 🗣️ Language: English only

This is an **English-only game**. The original T4C client/server this project recreates was
French, and legacy imported content occasionally carries that over — a hardcoded French string
in a screen class, an untranslated value in `assets/i18n/lang.json`, a French word used as an
NPC dialogue keyword. None of that is intentional bilingual support; it is leftover import noise
and should be fixed, not preserved or extended.

- **Never add a second language, a locale switcher, or any French/multi-lingual text.**
  `I18n` (`src/main/java/com/perso/T4C/i18n/I18n.java`) is intentionally single-catalogue,
  single-locale — keep it that way. Don't add a `lang_fr.json` or any per-locale variant.
- **All player-facing strings are English**, routed through `assets/i18n/lang.json` via
  `I18n.key(...)`/`I18n.resolve(...)`, not hardcoded inline in a screen/NPC class. If you add a
  new hardcoded string, make it a new i18n key with English text — see the existing
  `message.*`/`npc.*`/`item.*` conventions.
- **If you find French (or other non-English) text while working on something** — a hardcoded
  string, an i18n value, a dialogue keyword, an item/monster display name — translate it to
  English as part of your change, even if it's outside your task's direct scope; this is a
  standing exception to the "stay inside the PR's own diff" rule in `AGENTS.md` (which governs
  *review* comments, not authoring). Don't invent new flavor text while doing this — translate
  what's there, preserving the original meaning and tone.
- **Exception, not a violation**: code that generically handles Unicode/multi-script input for
  robustness (e.g. `BaseNPC`'s dialogue-quote stripping supporting curly quotes and guillemets,
  or `SpriteBinIO` round-tripping accented characters in a sprite name) is fine to keep — that's
  defensive text handling, not bilingual game content. The line is: does a player ever see French
  text in this game? If yes, fix it. If it's just infrastructure that happens to tolerate
  non-ASCII input, leave it.
- **Asset/sprite identifiers are not "text"** — internal sprite names baked into the compiled
  `assets/sprites/*.bin` packs (e.g. `SPRITE_BASE = "PaysanModel1"`) are opaque asset IDs, not
  player-facing dialogue, and renaming the Java-side string without also repacking the binary
  sprite data would break rendering. Don't "fix" these; they're not what this rule is about.

