# Task Ledger

A lightweight, in-repo task tracker. This project doesn't use an external
issue tracker, so every unit of content/feature work gets a sequential ID
here (`T4C-XXXX`) purely for visibility and cross-referencing — from a
commit message, a `CHANGELOG.md` entry, or a PR description back to "what
was this work and why."

**Next free ID: `T4C-0060`.** When starting new work, claim the next ID,
add a row below with status `Planned`/`In Progress`, and flip it to `Done`
(with a commit range and a `CHANGELOG.md` entry) when it ships. See
`CLAUDE.md` for the full policy this file is part of.

| ID | Title | Type | Status | Commits | Changelog |
|---|---|---|---|---|---|
| T4C-0001 | JSON content pipeline, armor sets, level cap 500 | Content/Systems | Done | `a7e7247` | [2026-09-16](CHANGELOG.md#2026-09-16--json-content-pipeline-armor-sets-level-cap-500-t4c-0001) |
| T4C-0002 | Rebirth economy, storage, spell vendor, macros | Content/Systems | Done | `0404411` | [2026-09-17](CHANGELOG.md#2026-09-17--rebirth-economy-storage-and-quality-of-life-pass-t4c-0002) |
| T4C-0003 | Content-authoring Claude skills | Process/Tooling | Done | `0f2cba7`, `2bfcd38`, `99e6f39`, `1758ed7` | [2026-09-17](CHANGELOG.md#2026-09-17--content-authoring-skills-t4c-0003-processtooling) |
| T4C-0004 | Sunken Chancel & Cinderreach Hills zones | Content | Done | `8f67c16` | [2026-09-17](CHANGELOG.md#2026-09-17--sunken-chancel--cinderreach-hills-zones-t4c-0004) |
| T4C-0005 | Leveling overhaul (XP overflow fix) + 4 endgame zones 100–500 | Content/Fix | Done | `a8c3f55` | [2026-09-17](CHANGELOG.md#2026-09-17--leveling-overhaul--4-endgame-zones-levels-100500-t4c-0005) |
| T4C-0006 | Fix new spells unlearnable via TrainingCatalog | Fix | Done | `52db68e` | [2026-09-17](CHANGELOG.md#2026-09-17--spell-learnability-fix-t4c-0006) |
| T4C-0007 | English-only localization pass | Fix/Process | Done | `a8d76b1` | [2026-09-18](CHANGELOG.md#2026-09-18--english-only-localization-pass-t4c-0007) |
| T4C-0008 | Canon-verified content pass (Goblin Slayer, Arch Drake, Deep Ones Cave, Sentinel) | Content | Done | `67d940e` | [2026-09-18](CHANGELOG.md#2026-09-18--canon-verified-content-pass-t4c-0008) |
| T4C-0009 | Avalon island expansion | Content | Done | `09ad82d`, `40e0f19`, `2127376`, `a3c968a`, `cd7bdf3`, `d607f66` | [2026-09-18–19](CHANGELOG.md#2026-09-1819--avalon-island-expansion-t4c-0009) |
| T4C-0010 | CI: skip on docs-only changes, scoped test selection | Process/Tooling | Done | `d29b421` | [2026-09-19](CHANGELOG.md#2026-09-19--smarter-ci-skip-docs-only-changes-scope-test-runs-t4c-0010) |
| T4C-0011 | MMO server groundwork: Java 21, libGDX purity guard | Process/Tooling | Done | `8aef3a8`, `148752b` | [2026-09-21](CHANGELOG.md#2026-09-21--mmo-server-groundwork-java-21-libgdx-purity-guard-t4c-0011) |
| T4C-0012 | Local compendium website (items/spells/monsters/NPCs/quests/zones) | Content/Tooling | Done | `e0170f1` | [2026-09-22](CHANGELOG.md#2026-09-22--local-compendium-website-t4c-0012) |
| T4C-0013 | CI auto-regenerates compendium data on push to main | Process/Tooling | Done | `c0c15e7` | [2026-09-22](CHANGELOG.md#2026-09-22--ci-auto-regenerates-compendium-data-on-push-to-main-t4c-0013) |
| T4C-0014 | Deterministic NPC ordering in compendium exporter | Process/Tooling | Done | `0282e7c` | [2026-09-22](CHANGELOG.md#2026-09-22--deterministic-npc-ordering-in-compendium-exporter-t4c-0014) |
| T4C-0015 | Monster balance report generator (audit new bosses vs. legacy curve) | Process/Tooling | Done | `6757374` | [2026-09-22](CHANGELOG.md#2026-09-22--monster-balance-report-generator-t4c-0015) |
| T4C-0016 | Fix mislabeled attack "hit chance" on compendium monster pages | Fix | Done | `3f7fd65` | [2026-09-22](CHANGELOG.md#2026-09-22--fix-mislabeled-attack-hit-chance-on-compendium-monster-pages-t4c-0016) |
| T4C-0017 | XP-per-level chart on the compendium Systems page | Content/Tooling | Done | `dbc3add` | [2026-09-22](CHANGELOG.md#2026-09-22--xp-per-level-chart-on-the-compendium-systems-page-t4c-0017) |
| T4C-0018 | Rename t4cfantasy-colliding spells, add Apex-tier spells, +4/+5 weapons/shields, fix warrior/archer armor AC | Content/Fix | Done | `c982e26` | [2026-09-22](CHANGELOG.md#2026-09-22--spell-renames-apex-tier-spells-enchanted-gear-and-warriorarcher-armor-fix-t4c-0018) |
| T4C-0019 | Zone access quests, Avalon dead-end fix, dynamic fast travel | Content/Systems | Done | `43e9890` | [2026-09-22](CHANGELOG.md#2026-09-22--zone-access-quests-avalon-dead-end-fix-dynamic-fast-travel-t4c-0019) |
| T4C-0020 | CLAUDE.md: CHANGELOG.md must be written for players, not engineers | Process/Tooling | Done | `ce722be` | [2026-09-23](CHANGELOG.md#2026-09-23--changelog-writing-policy-player-facing-not-technical-t4c-0020) |
| T4C-0021 | Spell class balance pass, armor set loot wiring, compendium data/display fixes | Content/Fix | Done | `148c76f`, `97151ea` | [2026-09-23](CHANGELOG.md#2026-09-23--spell-balance-lost-armor-recovered-and-honest-site-data-t4c-0021) |
| T4C-0022 | Standalone town-side access quests for Sunken Chancel and Cinderreach Hills | Content | Done | `960509f` | [2026-09-23](CHANGELOG.md#2026-09-23--town-side-access-quests-for-sunken-chancel-and-cinderreach-hills-t4c-0022) |
| T4C-0023 | Zone map images on the compendium site (real tile-art coloring, NPC/boss pins) | Content/Tooling | Done | `04fc7bc` | [2026-09-23](CHANGELOG.md#2026-09-23--zone-map-images-on-the-compendium-site-t4c-0023) |
| T4C-0024 | Kraanhold: new high-level continent (Windhowl Marches through Drake's Lair relocated there) | Content | Done | `bab83af` | [2026-09-23](CHANGELOG.md#2026-09-23--kraanhold-a-new-high-level-continent-t4c-0024) |
| T4C-0025 | Level cap 400, 50-rebirth limit, even high-tier spell ladder (150-400, every school), elemental archmage mantles | Content/Systems | Done | `557ebd2` | [2026-09-23](CHANGELOG.md#2026-09-23--level-cap-400-rebirth-limit-and-an-even-spell-ladder-t4c-0025) |
| T4C-0026 | Rebirths page on the compendium site (per-rebirth requirements and rewards) | Content/Tooling | Done | `e53a781c` | [2026-09-23](CHANGELOG.md#2026-09-23--rebirths-page-on-the-reference-website-t4c-0026) |
| T4C-0027 | Item rebalance: class-based bonuses, AC tied to endurance (max 600), design guidelines file | Content/Balance | Done | `b7f20b4d` | [2026-09-23](CHANGELOG.md#2026-09-23--item-rebalance-every-item-matches-its-class-t4c-0027) |
| T4C-0028 | Legendary bow + staff, multi-drop boss loot tables, flat items table on the compendium | Content/Tooling | Done | `11f30883` | [2026-09-24](CHANGELOG.md#2026-09-24--endgame-legendary-weapons-richer-boss-loot-and-a-flatter-items-page-t4c-0028) |
| T4C-0029 | The Rootcrown Wyrm (Elder Wyrms pilot): new boss + 3 legendary wisdom-mage items | Content | Done | `a3b0b80d` | [2026-09-24](CHANGELOG.md#2026-09-24--the-rootcrown-wyrm-first-of-the-elder-wyrms-t4c-0029) |
| T4C-0030 | Resistance rebalance: five non-light schools instead of one, light resist banned everywhere | Content/Balance | Done | `0c08e9e7` | [2026-09-24](CHANGELOG.md#2026-09-24--broader-elemental-resistance-and-no-more-light-resistance-t4c-0030) |
| T4C-0031 | Narrative depth for Passage to Avalon (Harbormaster Rangor personal stake + Avalon foreshadowing) | Content | Done | `534618ae` | [2026-09-24](CHANGELOG.md#2026-09-24--passage-to-avalon-gets-a-personal-story-t4c-0031) |
| T4C-0032 | Passage to Avalon becomes a two-stage quest chain (prove yourself against the scouts, then take on Ithrak's warband) | Content | Done | `1dd47507` | [2026-09-24](CHANGELOG.md#2026-09-24--passage-to-avalon-becomes-a-two-stage-quest-t4c-0032) |
| T4C-0033 | Godsforged: a crafting-quest tier above Legendary, one item per class archetype, via a 3-NPC crafting chain | Content/Systems | Done | `af7aa265` | [2026-09-24](CHANGELOG.md#2026-09-24--godsforged-a-new-tier-of-crafted-relics-t4c-0033) |
| T4C-0034 | Boss loot pass: no more one-item/rare-or-nothing drop tables | Content/Fix | Done | `43a77280` | [2026-09-24](CHANGELOG.md#2026-09-24--every-boss-drops-something-t4c-0034) |
| T4C-0035 | The Waking Rite: a short Avalon quest that permanently unlocks a rebirth shortcut (level 125+) | Content/Systems | Done | `82f646bb` | [2026-09-24](CHANGELOG.md#2026-09-24--a-shortcut-for-repeat-rebirths-t4c-0035) |
| T4C-0036 | Gold economy pass: trimmed the endgame quest gold spike, new town-vendor gold sinks (mana prisms, critical healing potions) | Content/Systems | Done | `90919592` | [2026-09-24](CHANGELOG.md#2026-09-24--a-gold-economy-pass-t4c-0036) |
| T4C-0037 | Boss gold rebalance: Mordrenn and the Centaur King paid noticeably less gold than nearby regular monsters for their difficulty | Content/Balance | Done | `8795f7c3` | [2026-09-24](CHANGELOG.md#2026-09-24--mordrenn-and-the-centaur-king-pay-out-more-gold-t4c-0037) |
| T4C-0038 | Finish what's started: the four remaining Elder Wyrms (Pyreclaw, Mistwing, Duskmaw, Galecrest), Centaur Slaying armor set, Drowned Inquisition and Cinderforged zone armor sets | Content | Done | `258adf1` | [2026-09-25](CHANGELOG.md#2026-09-25--the-elder-wyrms-are-complete-and-three-new-armor-sets-t4c-0038) |
| T4C-0039 | Storage chest overhaul (new two-pane UI, storage now saved, keeps durability/charges) + player-control pass (Controls window, typing-safe hotkeys, Ctrl no longer walks, debug reload moved off bare R) | Systems/Fix | Done | `146b23f`, `d95c9d0` | [2026-09-25](CHANGELOG.md#2026-09-25--a-new-storage-chest-and-better-control-over-your-character-t4c-0039) |
| T4C-0040 | Fast travel, spell book and macros windows redesign (+ Freeze / Poison Arrow spell names translated) | Systems/Fix | Done | `bc2f8f5` | [2026-09-25](CHANGELOG.md#2026-09-25--fast-travel-spell-book-and-macros-windows-redesigned-t4c-0040) |
| T4C-0041 | Coordinates readout moved from F2 to F12, leaving F2 free for macros | Systems | Done | `4530b8f` | [2026-09-25](CHANGELOG.md#2026-09-25--f2-is-free-for-your-macros-t4c-0041) |
| T4C-0042 | The Mirror of Echoes: Ysmera the Mirrorwarden (Colosseum), a boss built live from the player's own character (look, name, level, skills, signature spell), ten scaling trials, penalty-free falls, and the bound Echo companion | Content/Systems | Done | `365458f` | [2026-09-25](CHANGELOG.md#2026-09-25--the-mirror-of-echoes-t4c-0042) |
| T4C-0043 | Login & character selection polish (startup loading bar, last-played character, details line, hover/double-click, key hints, level shown at the cap), clearer Switch Character button, and an in-game radar (players blue, NPCs green, monsters yellow, bosses red) with an Options toggle | UI/Systems | Done | _this PR_ | [2026-09-25](CHANGELOG.md#2026-09-25--login-character-selection-and-radar-t4c-0043) |
| T4C-0044 | The Bloodline Vault: a second, shared storage stash (items + gold) reachable from every character on the account, toggled from the Storage window title | Systems | Done | _this PR_ | [2026-09-25](CHANGELOG.md#2026-09-25--the-bloodline-vault-t4c-0044) |
| T4C-0045 | The Windhowl War-Party: a 5-raider Centaur warband + banner-bearer mini-boss near Windhowl Marches; felling the bearer opens a window where clearing the rest summons the Warband Warlord (new item: Chieftain's Warhorn) | Content | Done | _this PR_ | [2026-09-25](CHANGELOG.md#2026-09-25--the-windhowl-war-party-a-warband-camp-t4c-0045) |
| T4C-0046 | The "Two Masters" quest-completion pattern (`QuestService.completeWithAlternateReward`, DESIGN_GUIDELINES.md), proved on `passage_to_kraanhold`: Old Corrin offers a permanent per-life XP-trickle perk as an alternative to Dockmaster Thessaly's immediate gold/XP | Systems/Content | Done | _this PR_ | [2026-09-25](CHANGELOG.md#2026-09-25--two-masters-a-choice-at-the-kraanhold-dock-t4c-0046) |
| T4C-0047 | The Wyrm Scales: each Elder Wyrm can drop its own scale; all five turned in at the Colosseum's new Keeper of the Sixth Seal summons The Convergent Wyrm (level 750, all-element, 3 new legendary items + a piece from every elemental armor set) | Content | Done | _this PR_ | [2026-09-25](CHANGELOG.md#2026-09-25--the-wyrm-scales-and-the-convergent-wyrm-t4c-0047) |
| T4C-0048 | The Hourglass Trials: Trial Warden Osric (Colosseum) summons a fixed-HP Sandglass Sentinel (5 tiers, rising with rebirths); best time per tier is a persisted quest flag, a new best pays a one-time gold bonus | Content/Systems | Done | _this PR_ | [2026-09-25](CHANGELOG.md#2026-09-25--the-hourglass-trials-t4c-0048) |
| T4C-0049 | Lost Keys of Kraanhold: 12 named keys, rare drops across Kraanhold's monsters + the Windhowl War-Party, open one of 4 chest NPCs (3 keys each) for gold/potions or, for 2 marquee keys, a new unique item | Content | Done | _this PR_ | [2026-09-25](CHANGELOG.md#2026-09-25--lost-keys-of-kraanhold-t4c-0049) |
| T4C-0050 | The Unsigned Letter: a first rebirth grants a mysterious letter (RebirthBehavior hook), resolved by asking MirrorwardenYsmera about it - she's been watching every echo a rebirth leaves | Content | Done | _this PR_ | [2026-09-25](CHANGELOG.md#2026-09-25--the-unsigned-letter-t4c-0050) |
| T4C-0051 | Workflow skills (ship-pr, balance-change, verify-website) and pre-PR self-review rules from the usage-insights review | Process/Tooling | Done | `f48dfaa8` | [2026-09-25](CHANGELOG.md#2026-09-25--safer-changes-and-fewer-review-round-trips-t4c-0051-processtooling) |
| T4C-0052 | Full-length compendium quest walkthroughs (23 quests): a new prose field alongside the existing offer/completion text, separate from in-game dialogue | Content | Done | `cccfeb56` | [2026-09-26](CHANGELOG.md#2026-09-26--full-quest-walkthroughs-t4c-0052) |
| T4C-0053 | Quest pages always show a location (giver/target/turn-in spot) and a map, including turn-in-only quests and quests not previously linked to a zone | Content | Done | `cccfeb56` | [2026-09-26](CHANGELOG.md#2026-09-26--quest-locations-always-shown-t4c-0053) |
| T4C-0054 | Spellbook/tooltip info, spell prerequisite chains + skill-point cost rebalance, hiding system-only "spells", level-scaled cast speed for high-tier spells, universal hover tooltips, hold-to-repeat skill point spending | Systems/Content/Fix | Done | _this PR_ | [2026-09-26](CHANGELOG.md#2026-09-26--spell-hierarchy-spellbook-info-and-a-few-quality-of-life-fixes-t4c-0054) |
| T4C-0055 | Reference-site editorial pass: the world reorganised into an ordered journey, plain-language rewrite throughout, corrected quest reward/requirement wording, plus a technical-writer review skill | Content/Fix/Process/Tooling | Done | `3af64a1b` | [2026-09-26](CHANGELOG.md#2026-09-26--the-reference-website-rewritten-for-players-t4c-0055) |
| T4C-0056 | Locations panel fast travel to 9 NPCs revisited across many quests or within one long questline (Lord Sunrock, Asarr, Araknor, Lance Silversmith, Zhakar, Elysana Blackrose, Dionysus Silverstream, Grant Hornkeep, Filandrius) | Content | Done | `ebb6dcf8` | [2026-09-26](CHANGELOG.md#2026-09-26--fast-travel-to-well-worn-npcs-t4c-0056) |
| T4C-0058 | Gloomblade/Demonblade crafting ingredients (Ring of Darkness, Demon Skull, Necklace of the Black Heart, Nightsword, Chaos Sword) raised to a completable drop rate; the necklace and nightsword didn't drop from anything before | Fix | Done | _this PR_ | [2026-09-26](CHANGELOG.md#2026-09-26--gloomblade-and-demonblade-recipes-actually-finishable-t4c-0058) |
| T4C-0059 | Class-based character creation: the eight-question quiz is replaced by a seven-archetype picker (Warrior, Archer, Paladin, Cleric, Healer, Mage, Battle Mage) with a live dressed preview; every attribute now starts at 20 plus a fixed 30-point class spread, rerolling touches only health and mana, and each class starts wearing its own kit (including six new starter items for the casting and melee paths) and knowing its own spell | Systems/Content/Fix | Done | `fd6c6b77` | [2026-09-26](CHANGELOG.md#2026-09-26--pick-a-class-instead-of-answering-riddles-t4c-0059) |

## Type legend

- **Content** — new/changed in-world content (zones, monsters, items,
  spells, quests, NPCs, maps).
- **Systems** — new or changed game mechanics/economy (rebirth, storage,
  progression curves, UI infra).
- **Fix** — bug fix with no new content.
- **Process/Tooling** — repo/dev workflow (skills, CI, review policy) with
  no in-game effect.

## Backfill note

T4C-0001 through T4C-0009 were assigned retroactively on 2026-09-19 while
setting up this ledger and `CHANGELOG.md`, by walking `git log` for the
project's content/feature passes. Earlier history (companion/taming system,
HUD rework, original NPC/quest content, etc.) predates this tracking effort
and was not backfilled with IDs — only the passes covered by
`CHANGELOG.md` are. Everything from T4C-0010 onward is tracked live, as the
work happens.
