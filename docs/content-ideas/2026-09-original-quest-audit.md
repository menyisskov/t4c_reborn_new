# Content pass: original-game quest audit (T4C-0066)

The owner asked to audit all of the original game's quests against the real reference data
(t4cbible.com - mirrors t4cfantasy.com/Bible content, both reachable this pass) and build out
whatever's genuinely missing. This doc is the research record: what was found, what shipped in
this first pass, and what's left for the next one. See DESIGN_GUIDELINES.md section 6 for the
standing rule this established ("read this before assuming any original-game quest is missing").

## Headline finding

Going in, the working assumption was that most of the original game's quests were unbuilt. That
assumption was wrong. A four-agent research/audit pass (canon data scrape + codebase
cross-reference) found that the overwhelming majority of Arakas, Raven's Dust, Stoneheim, the
Crimsonscale Letter chain, the Good/Evil Seraph endgame, and the Oracle dungeon puzzle are already
real, spawned, and working - built on an older per-NPC flag system (`__QUEST_*` flags,
`ADDON_STORYLINE_PROGRESS`, `npcFlag`/`globalFlag`) that predates `quest/QuestDef`/`QuestService`
and therefore doesn't show up in the Quest Journal UI (T4C-0065). The real gap list, once
cross-referenced, is narrow and specific - see below.

## What shipped this pass (T4C-0066)

- **Dwarthon Stoneface** (`npc/DwarthonStoneface.java`) was fully written (21 dialogue topics, a
  real `__FLAG_BLACKBLOOD_WANTS_YOU` reminder mechanic) but spawned at `(0,0,0)` - never actually
  placed. Placed a few tiles from Bane Blackblood's own real spot (342,1679,0), matching canon's
  "west of Bane's Throne." (Left the pre-existing `monster/DWARTHONSTONEFACE.java` - a mute
  duplicate stat-monster at (303,1678,0), wired through the separate `SpawnGroup0168`
  pooled-spawn system - alone; touching it means also touching that system, out of scope for a
  single careful pass. Worth a follow-up.)
- **The "Audience to Bane Blackblood" quest gate** didn't exist even though every piece of it was
  present: `BaneBlackblood.java`'s own `onConversationStart` already checked
  `"__QUEST_DWARTHON_STONEFACE" != 5` to block conversation, but *nothing* ever set that flag.
  Added: asking Dwarthon Stoneface about "Bane" sets it to 3 (quest accepted); killing Delwobble
  the Mad Summoner (`monster/Delwobble.java`, already spawned and stat-authored, just needed an
  `onDeath` hook) sets it to 5 if-and-only-if the player had the quest active. This is a separate
  flag from Bane's own pre-existing `"__QUEST_ROYAL_KEY4"` negotiation counter - confirmed by test,
  see `DwarthonStonefaceBaneQuestTest.java`.
- **Gabriel Archonis / Gaenen Elthorn** (`npc/GabrielArchonis.java`, `npc/GaenenElthorn.java`), the
  Oracle's two alignment-specific final-test bosses - both had a complete, real fight mechanic
  (miss-until-near-death, `__FLAG_USER_HAS_DEFEATED_ASSISTANT`) but were spawned at `(0,0,0)`. The
  entire upstream Oracle dungeon (chests, doors, portals, guardians, all ~2700-2810,2180-2340,z=2)
  is spawned and reachable; only this final room wasn't. Placed both a few tiles past the Oracle
  Invulnerable Guardian cluster's far edge (2822-2828, 2172-2178, z=2).

## Confirmed already working (no action needed) - the long list

See the full research trail for detail; headline confirmations: Dragon.java/"Dark Fang"'s Tomb
Raider quest, Mirak Nira's Trust Quest (100 goblins, `__GOBLINS_KILLED_BY_HERO`), Rhodar
Heatforge's Goblin Slayer (500 goblins, same counter) and Rhodar's Hammer (already dropping from
`EyePatchedQardos` at 6% - a prior pass's note that it wasn't dropping is stale), Yrian's Stone of
Life, Lantalir's Book of Feylor, the Gypsy alignment quiz, Ttayh Mark's Black Market, Chamberlain
Thomar's Royal Key #2 negotiation, the entire Crimsonscale Letter chain (~40 NPCs across Arakas +
Raven's Dust, ending in Olin Haad -> Gluriurl), the full Good/Evil Seraph questline and the Oracle
dungeon's puzzle chain up to (not including) the final room, and most of Stoneheim's Collector's
Quest / Ring of Pure Faith / Staff of Hope / Lute of Peace / Ethereal Key quests.

## Confirmed genuinely broken/incomplete - next pass's candidates, roughly in order of size

- **King Theodore has no "Audience to the King" quest at all**, not just a missing hookup as first
  assumed. Canon: talk to Chamberlain Thomar, kill 10 Rogue Mages within 30 minutes, return to
  Thomar, then King Theodore will talk to you (and mentions the 12 Undead Brothers / Royal Keys
  chain, which - once past this gate - is already ~90% built, per the audit). Neither
  `KingTheodore.java` nor `ChamberlainThomar.java` currently implement the audience gate or the
  timed kill-count; this needs a real timed kill-counter design (this codebase's existing timed
  mechanics, e.g. Yrian's 7200s retry timer, are a starting reference, not a kill-counter - there's
  no existing "N kills in Y minutes" pattern in the legacy flag system to copy directly).
- **Two Stoneheim quests are unplayable end-to-end despite looking complete**: Lost Blade of the
  Dragon and Scroll of Horse Friendship both gate their turn-in on kill-count flags
  (`__FLAG_NIGHTCREEPERS_KILLED`, `__FLAG_PILFERERS_KILLED`, `__FLAG_SKELETAL_CENTAURS_KILLED`)
  that are defined (as macro IDs, in `OriginalNpcScriptMacros.java`) but never incremented anywhere
  - the monster classes they'd attach to (`r160Nightcreeper.java`, `r184Pilferer.java`,
  `r214SkeletalCentaur.java`) have no `onDeath` hook. Same shape of fix as this pass's Delwobble.
- **Lost Helm of the Dragon's 4 intermediate keys have no drop source** anywhere (Shiny Metal Key,
  Dark Iron Key, Chipped Bone Key, Steel Safe Key) - the turn-in NPC (`GrantHornkeep.java`) is
  real and correct, but the canon "chest-to-chest" chain that hands them out was never wired into
  any loot table.
- **Ruby Dragon Fang** (`item/definition/RubyDragonFang.java`) is a registered item with zero drop
  source - breaks one ingredient of the Seraph "Fangs of True Resolve" step. Canon source is Dark
  Fang himself (`npc/Dragon.java`); a rare drop entry there is the natural fix.
- **Three Stoneheim NPCs are spawned but have no turn-in logic written**: Villain's Skull Quest
  (`DelnarSteelblade.java`, `StaticDialogueBehavior`-only), the Gems Quest
  (`MeltarWinterstorm.java`, only implements an unrelated faction-renown greeting), and the
  Collector's Quest's Good-path finale (`BeltiganWhitesword.java` - the Evil-path finale,
  `Mordenthal.java`, already works and is the template to mirror).
- **Skeleton Cave** and **Deep Ones Cave's `CAVERN`-side kill quest** don't exist (reconfirmed from
  the prior canon-verified-additions.md pass, not re-investigated in depth this time).
- Smaller/cosmetic, optional: the "Crimsonscale vs. Dark Fang" and "cult of Makrsh" narrative
  connections some prior notes speculated about don't exist in code - `MakrshPtangh` is a wholly
  separate encounter. Not a bug, just an unbuilt flavor connection, lowest priority of everything
  here.

## Uncertain / needs a direct read before acting on it

- Whether `r244TimeGuardian` is canon's "Timeless Guardian" (Chamber of Perseverance, x6,
  kill-in-order + shard drop) - name is close but not exact, mechanic not verified.
- Whether `MalachaiFatebringer` (a Scroll of Horse Friendship item-choice hub) is fully wired or a
  stub - only `StaticDialogueBehavior.INSTANCE` was found on a first pass.
- Whether Griroesh's loot table actually drops "Demon Skull" (needed for the original Demonblade
  quest) - not checked.
- Full end-to-end verification of the ~25 Royal Keys-chain NPCs beyond Chamberlain Thomar (only
  spot-checked, not read end to end) and the ~30 Seraph-questline NPCs beyond NissusHaloseeker
  (which matched canon exactly on a full read).
- Whether the classic Arakas quests not touched by name this pass (Merchant Letter, Chaotic Sword,
  Sunrock Diamond, Goblin Fever, Bluish Liquid, Nightsword, Scroll of Enchantment, Blessed
  Chainmail, Jarko's Spellbook, Balork's Evil) are complete end-to-end - all their named NPC
  classes exist, but none of these specific flag chains were traced start-to-reward.

## Where the canon reference data lives

Full quest text for Arakas and Raven's Dust was captured directly into this session's
conversation; Stoneheim/Add-on/Oracle-walkthrough/NPC-chart/monster-chart/monster-drops/traders/
karma-tips were captured to scratchpad files (session-local, not committed - re-scrape
t4cbible.com if a future pass needs the raw text again; the URLs are `/ArakasQuest`,
`/RavensDustQ`, `/StoneheimIslandQuest`, `/Addon`, `/oraclewlk`, plus the sidebar's NPC/Monster/
Monster Drop/Traders/Karma Tips links).
