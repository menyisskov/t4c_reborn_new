# Task Ledger

A lightweight, in-repo task tracker. This project doesn't use an external
issue tracker, so every unit of content/feature work gets a sequential ID
here (`T4C-XXXX`) for visibility and cross-referencing — from a
commit message, a `CHANGELOG.md` entry, or a PR description back to "what
was this work and why."

The multiplayer roadmap and milestone acceptance criteria are in
[MILESTONES.md](multiplayer/MILESTONES.md). This ledger tracks individual work items,
not milestone completion; release evidence is in [ACCEPTANCE.md](multiplayer/ACCEPTANCE.md).

**Next free ID: `T4C-0221`.** When starting new work, claim the next ID,

add a row below with status `Planned`/`In Progress`, and flip it to `Done`
(with a commit range and a `CHANGELOG.md` entry) when it ships. See
`CLAUDE.md` for the full policy this file is part of.

| ID | Title | Type | Status | Commits | Changelog |
|---|---|---|---|---|---|
| T4C-0220 | Record owner-approved two-rendered-client Beta/GA concurrency target | Process / Multiplayer milestones | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--two-player-beta-acceptance-target-t4c-0220) |
| T4C-0219 | Verify live account-key rotation through two native sessions | Multiplayer / Beta security | Done | PR156/main`b475b3f4` | [2026-10-27](CHANGELOG.md#2026-10-27--live-account-key-rotation-check-t4c-0219) |
| T4C-0218 | Native online client: talk to nearby NPCs with topic choices and an inventory panel to equip and unequip | Multiplayer / Beta client | Done | PR155/main`0b350b42` | [2026-10-27](CHANGELOG.md#2026-10-27--talk-to-nearby-people-and-manage-equipment-in-the-online-client-t4c-0218) |
| T4C-0217 | Owner-approved starter kits: Paladin mace, Arcane Spark for mages, Archer quiver; checkpoint scope approval recorded | Content / Multiplayer | Done | PR152/main`91296e12` | [2026-10-27](CHANGELOG.md#2026-10-27--new-starting-gear-and-first-spell-t4c-0217) |
| T4C-0216 | Verify two native adapters over trusted TLS | Multiplayer / Beta | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--local-native-tls-acceptance-t4c-0216) |
| T4C-0215 | Package a clean local QA server runtime and safe lifecycle tools | Multiplayer / Beta operations | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--local-online-server-qa-package-t4c-0215) |
| T4C-0214 | Design authored starter-route hosting without fabricated portals | Multiplayer / Beta architecture only | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--authored-starter-route-hosting-design-t4c-0214) |
| T4C-0213 | Verify restored schema checks and read committed state through two native adapters | Multiplayer / Beta reliability | Done | PR148; adapter-read evidence in [ACCEPTANCE](multiplayer/ACCEPTANCE.md) | [2026-10-27](CHANGELOG.md#2026-10-27--backup-check-verification-on-schema-10-t4c-0213) |
| T4C-0212 | Activate guarded server monster patrol and pursuit in supported hosted regions | Multiplayer / Beta implementation and native-adapter verification | Done | this PR; 182 isolated tests, two-adapter pursuit/restart PASS | [2026-10-27](CHANGELOG.md#2026-10-27--monsters-patrol-and-pursue-in-online-regions-t4c-0212) |
| T4C-0211 | Render authoritative native creature movement and facing | Multiplayer / Beta groundwork | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--native-creature-motion-from-server-snapshots-t4c-0211) |
| T4C-0210 | Share authored monster acquisition, leash and reach decisions | Multiplayer / Beta groundwork | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--shared-monster-pursuit-decisions-t4c-0210) |
| T4C-0209 | Authoritative monster pursuit architecture and peer handoff | Multiplayer / Beta design only | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--monster-pursuit-architecture-t4c-0209) |
| T4C-0208 | Isolated PostgreSQL crash/recovery through two native clients | Multiplayer / Beta | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--isolated-postgresql-recovery-probe-t4c-0208) |
| T4C-0207 | Two-native-adapter graceful and forced server restart recovery probe | Multiplayer / Beta | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--two-adapter-server-restart-probe-t4c-0207) |
| T4C-0206 | M3/M4 shared quest, spell-cast and heal rules plus quest/ability design (Claude lane) | Multiplayer / Beta | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--shared-quest-and-spell-rules-t4c-0206) |
| T4C-0205 | Package the online client without developer checkout dependencies | Multiplayer / Beta | Done | `cd9e2a44` | [2026-10-27](CHANGELOG.md#2026-10-27--online-client-test-package-t4c-0205) |
| T4C-0204 | M2 server loot wiring L6-L12, with owner-approved L11 individual corpse-item recovery (Claude lane) | Multiplayer / Beta | In Progress | PR128, PR130, PR142, PR144, PR145 partial | pending |
| T4C-0203 | Coordinate Codex and Claude task/file/resource ownership | Process/Tooling | Done | `0e258e40` | [2026-10-27](CHANGELOG.md#2026-10-27--peer-agent-coordination-t4c-0203) |
| T4C-0202 | M2 general monster loot and claim rules: domain rules first (Claude lane) | Multiplayer / Beta | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--online-loot-rules-t4c-0201-t4c-0202) |
| T4C-0201 | Two-player scope proposal and M2 loot design docs (Claude lane) | Multiplayer / Beta | Done | this PR | [2026-10-27](CHANGELOG.md#2026-10-27--online-loot-rules-t4c-0201-t4c-0202) |
| T4C-0200 | Centralize owner model routing and instruction references | Process/Tooling | Done | `b88ac040` | [2026-10-27](CHANGELOG.md#2026-10-27--agent-model-routing-t4c-0200) |
| T4C-0199 | Define exact two-player milestone completion criteria | Process/Tooling | Done | `b88ac040` | [2026-10-27](CHANGELOG.md#2026-10-27--multiplayer-milestone-checklist-t4c-0199) |
| T4C-0198 | Measure server scheduler debt and runtime resources | Multiplayer / Beta | Done | `6fb69e2b`; final baseline evidence PR151 | [2026-10-27](CHANGELOG.md#2026-10-27--private-server-runtime-observations-t4c-0198) |
| T4C-0197 | Bound and reuse online database connections | Multiplayer / Beta | Done | `22cf1771` | [2026-10-27](CHANGELOG.md#2026-10-27--online-database-reliability-t4c-0197) |
| T4C-0196 | Authenticated online character roster | Multiplayer / Alpha | Done | `22cf1771` | [2026-10-27](CHANGELOG.md#2026-10-27--online-character-discovery-t4c-0196) |
| T4C-0195 | Authoritative Kilhiam opening story interaction | Multiplayer / Alpha | Done | `2eb37078` | [2026-10-27](CHANGELOG.md#2026-10-27--online-opening-story-service-t4c-0195) |
| T4C-0194 | Transfer native players between hosted worlds safely | Multiplayer / Alpha | Done | `62d80d48` | [2026-10-27](CHANGELOG.md#2026-10-27--hosted-world-travel-t4c-0194) |
| T4C-0193 | Export and validate authored portal destinations | Multiplayer / Alpha | Done | `b7551ab8` | [2026-10-27](CHANGELOG.md#2026-10-27--online-portal-validation-t4c-0193) |
| T4C-0192 | Configure verified encrypted remote transport | Multiplayer / Alpha | Done | `9a2f70d4` | [2026-10-27](CHANGELOG.md#2026-10-27--encrypted-online-transport-t4c-0192) |
| T4C-0191 | Verify consistent PostgreSQL backup and fresh restore | Multiplayer / Beta | Done | `d9b44536` | [2026-10-27](CHANGELOG.md#2026-10-27--realm-backup-verification-t4c-0191) |
| T4C-0190 | Persist monster retaliation, death and owned corpse recovery | Multiplayer / Alpha | Done | `bb002ff2` | [2026-10-27](CHANGELOG.md#2026-10-27--online-retaliation-and-recovery-t4c-0190) |
| T4C-0189 | Isolate online Windows focus crash correction | Multiplayer / Alpha | Done | `93800f69` | [2026-10-27](CHANGELOG.md#2026-10-27--online-window-stability-t4c-0189) |
| T4C-0188 | Preserve authored protection for online targets | Multiplayer / Alpha | Done | `9848eaf1` | [2026-10-27](CHANGELOG.md#2026-10-27--protected-online-targets-t4c-0188) |
| T4C-0187 | Connect native melee attacks to persistent damage and rewards | Multiplayer / Alpha | Done | `b565ad8a` | [2026-10-27](CHANGELOG.md#2026-10-27--native-online-melee-t4c-0187) |
| T4C-0186 | Use shared progression for durable online level gains | Multiplayer / Alpha | Done | `0c96e55a` | [2026-10-27](CHANGELOG.md#2026-10-27--online-progression-rules-t4c-0186) |
| T4C-0185 | Keep creature damage and its rewards together across recovery | Multiplayer / Alpha | Done | `5dd94255` | [2026-10-27](CHANGELOG.md#2026-10-27--persistent-creature-damage-t4c-0185) |
| T4C-0184 | Restore and respawn persistent creatures in the running world | Multiplayer / Alpha | Done | `f6ca92d0` | [2026-10-27](CHANGELOG.md#2026-10-27--live-creature-recovery-t4c-0184) |
| T4C-0183 | Persist creature spawn generations across recovery | Multiplayer / Alpha | Done | 116a1ea5 | [2026-10-27](CHANGELOG.md#2026-10-27--persistent-creature-life-tracking-t4c-0183) |
| T4C-0182 | Commit one recipient for shared-world rewards | Multiplayer / Alpha | Done | fef7b707 | [2026-10-27](CHANGELOG.md#2026-10-27--shared-world-reward-protection-t4c-0182) |
| T4C-0181 | Bound authenticated client command processing per session | Multiplayer / Alpha | Done | 4f9ca8d9 | [2026-10-27](CHANGELOG.md#2026-10-27--online-command-flood-limits-t4c-0181) |
| T4C-0180 | Share authoritative weapon timing with offline combat | Multiplayer / Alpha | Done | 1264f841 | [2026-10-27](CHANGELOG.md#2026-10-27--shared-online-weapon-timing-t4c-0180) |
| T4C-0179 | Keep shields out of online off-hand damage | Multiplayer / Alpha | Done | 24062be6 | [2026-10-27](CHANGELOG.md#2026-10-27--shield-combat-correction-t4c-0179) |
| T4C-0178 | Derive combat loadouts from authoritative equipment | Multiplayer / Alpha | Done | a5b5d457 | [2026-10-27](CHANGELOG.md#2026-10-27--online-combat-equipment-stats-t4c-0178) |
| T4C-0177 | Gate online login on explicit wire compatibility | Multiplayer / Alpha | Done | cb758124 | [2026-10-27](CHANGELOG.md#2026-10-27--online-compatibility-checks-t4c-0177) |
| T4C-0176 | Send native equipment commands and refresh committed state | Multiplayer / Alpha | Done | 3a5e86ef | [2026-10-27](CHANGELOG.md#2026-10-27--native-equipment-commands-t4c-0176) |
| T4C-0175 | Commit authenticated equipment changes without item duplication | Multiplayer / Alpha | Done | 265804dd | [2026-10-27](CHANGELOG.md#2026-10-27--authoritative-equipment-changes-t4c-0175) |
| T4C-0174 | Share equipment boost evaluation with headless gameplay | Multiplayer / Alpha | Done | `0bacdab7` | [2026-10-27](CHANGELOG.md#2026-10-27--shared-equipment-bonuses-t4c-0174) |
| T4C-0173 | Package authoritative equipment definitions for online combat | Multiplayer / Alpha | Done | `f4b95edc` | [2026-10-27](CHANGELOG.md#2026-10-27--online-equipment-content-t4c-0173) |
| T4C-0172 | Share weapon damage and dice formulas with the headless server | Multiplayer / Alpha | Done | `9fa59ff2` | [2026-10-27](CHANGELOG.md#2026-10-27--shared-online-weapon-rules-t4c-0172) |
| T4C-0171 | Replicate starting-area creatures into the native online client | Multiplayer / Alpha | Done | `f365c53b` | [2026-10-27](CHANGELOG.md#2026-10-27--shared-online-creatures-t4c-0171) |
| T4C-0170 | Package authoritative monster definitions and authored spawns for the headless server | Multiplayer / Alpha | Done | `70c92d20` | [2026-10-27](CHANGELOG.md#2026-10-27--online-monster-content-t4c-0170) |
| T4C-0169 | Deliver private committed character state to the native network adapter | Multiplayer / Alpha | Done | `8196b924` | [2026-10-26](CHANGELOG.md#2026-10-26--private-online-character-state-t4c-0169) |
| T4C-0168 | Serialize live gameplay results with movement checkpoints and logout | Multiplayer / Alpha | Done | `b887009b` | [2026-10-26](CHANGELOG.md#2026-10-26--online-reward-save-ordering-t4c-0168) |
| T4C-0167 | Preserve authoritative terrain for combat sight and sanctuary checks | Multiplayer / Alpha | Done | `bc5fc898` | [2026-10-26](CHANGELOG.md#2026-10-26--online-combat-terrain-t4c-0167) |
| T4C-0166 | Commit gameplay rewards and character changes atomically with retry protection | Multiplayer / Alpha | Done | `e7450202` | [2026-10-26](CHANGELOG.md#2026-10-26--atomic-gameplay-rewards-t4c-0166) |
| T4C-0165 | Create online characters from shared class rules and starter kits | Multiplayer / Alpha | Done | `96abcfdf` | [2026-10-26](CHANGELOG.md#2026-10-26--online-character-creation-t4c-0165) |
| T4C-0164 | Preserve online gameplay state and validate crash and backup recovery | Multiplayer / Alpha | Done | `5d45f5bb` | [2026-10-26](CHANGELOG.md#2026-10-26--online-gameplay-state-t4c-0164) |
| T4C-0163 | Persist native online movement across reconnects and server restarts | Multiplayer / Alpha | Done | `b494516a` | [2026-10-26](CHANGELOG.md#2026-10-26--persistent-native-sessions-t4c-0163) |
| T4C-0162 | Bind online characters to authenticated accounts and expiring writer sessions | Multiplayer / Alpha | Done | `860bc046` | [2026-10-26](CHANGELOG.md#2026-10-26--online-session-ownership-t4c-0162) |
| T4C-0161 | Establish transactional PostgreSQL online character storage | Multiplayer / Alpha | Done | `99dd47ca` | [2026-10-26](CHANGELOG.md#2026-10-26--online-persistence-foundation-t4c-0161) |
| T4C-0160 | Connect the native game renderer and characters to the Alpha server | Multiplayer / Alpha | Done | `865a2bbc` | [2026-10-26](CHANGELOG.md#2026-10-26--native-online-client-t4c-0160) |
| T4C-0159 | Deliver a local two-client movement preview with server-owned positions | Multiplayer / Alpha | Done | `1e9178b5` | [2026-10-26](CHANGELOG.md#2026-10-26--local-multiplayer-movement-preview-t4c-0159) |
| T4C-0158 | Share world-scoped static collision rules without changing offline movement | Architecture / Alpha | Done | `6bbb7f93` | [2026-10-26](CHANGELOG.md#2026-10-26--shared-world-collision-t4c-0158) |
| T4C-0157 | Establish an isolated headless multiplayer build and server lifecycle | Architecture / Alpha | Done | `5b08a7f4` | [2026-10-26](CHANGELOG.md#2026-10-26--multiplayer-alpha-foundation-t4c-0157) |
| T4C-0156 | Consolidate pending single-player work before multiplayer development | Process / Integration | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--single-player-integration-t4c-0156) |
| T4C-0155 | Move mythic bosses to a quest-gated island | World / Quest | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--shattered-isle-passage-t4c-0155) |
| T4C-0154 | Design dragon, griffin and hydra sprites and add their mobs after visual review | Art / Content | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--approved-dragon-griffin-and-hydra-bosses-t4c-0154) |
| T4C-0153 | Raise Arch Drake's spell threat and add level-1000+ monsters using available art | Content / Balance | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--mythic-monster-encounters-t4c-0153) |
| T4C-0152 | Publish the current compendium and show when reference data was refreshed | Website / Delivery | Done | `1e40c6ac`, PR #79 | [2026-10-26](CHANGELOG.md#2026-10-26--current-compendium-publication-t4c-0152) |
| T4C-0151 | Add senior divine encounters and modestly strengthen existing gods | Content / Balance | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--senior-gods-and-stronger-divine-trials-t4c-0151) |
| T4C-0150 | Load exactly one god per shrine instead of applying the ordinary spawn multiplier | Fix / Gods | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--single-god-shrine-spawns-t4c-0150) |
| T4C-0149 | Give divine judgments a moderate boss damage budget without restoring dense-pack spell bursts | Balance / Gods | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--divine-judgment-balance-t4c-0149) |
| T4C-0148 | Order hunting and training travel destinations by monster level | UI / Travel | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--hunting-and-training-teleport-order-t4c-0148) |
| T4C-0147 | Restore drake spell visuals, casting poses and elemental on-hit aura reactions | Fix / Combat | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--drake-spell-visuals-and-aura-reactions-t4c-0147) |
| T4C-0146 | Remove test and numeric placeholder items from the playable catalog | Cleanup / Items | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--playable-item-catalog-cleanup-t4c-0146) |
| T4C-0145 | Reduce elemental spell bursts for every monster caster and verify drake packs at 652 Fire resistance | Balance | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--monster-elemental-spell-damage-t4c-0145) |
| T4C-0144 | Explain item activation failures and expose the protection gem mana cost | Fix / Items | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--item-activation-failure-messages-t4c-0144) |
| T4C-0143 | Extend the damage budget to all new regular monsters and every outgoing attack path | Balance | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--damage-budget-for-all-new-regular-monsters-t4c-0143) |
| T4C-0142 | Lower dense hunting-field melee bursts without relying on a fifty-rebirth healing aura | Balance | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--dense-pack-hunting-damage-t4c-0142) |
| T4C-0141 | Strengthen high-level protection rolls, fix item macros with the journal open, and prevent accidental graphics reload/layout editing | Balance / Controls | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--protection-macros-and-interface-input-t4c-0141) |
| T4C-0140 | Add item macros and extend unstable protection to thirty minutes | Items / Controls | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--item-macros-and-longer-protection-t4c-0140) |
| T4C-0139 | Calibrate hunting XP against the observed 234-274 leveling pace | Balance | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--live-hunting-xp-calibration-t4c-0139) |
| T4C-0138 | Correct drake healing targets and distinguish above-cap challenges from leveling farms | Fix / Guides | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--drake-spells-and-hunting-guidance-t4c-0138) |
| T4C-0137 | Refresh website maps and cover the Witness and Threnody campaign | Wiki / Maps | Done | `1e40c6ac`, PR #79 | [2026-10-26](CHANGELOG.md#2026-10-26--website-map-atlas-refresh-t4c-0137) |
| T4C-0136 | Warrior armor, shields and weapons through 2000 strength with Attack and Dodge bonuses | Content / Balance | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--warrior-equipment-progression-t4c-0136) |
| T4C-0135 | Searchable leveling guide in the in-game Wiki | Wiki / Content | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--in-game-leveling-guide-t4c-0135) |
| T4C-0134 | Scale hunting experience from level 150 through the level-400 endgame | Balance | Done | `f25803b0` | [2026-10-26](CHANGELOG.md#2026-10-26--hunting-experience-through-level-400-t4c-0134) |
| T4C-0133 | Align early and mid-tier quiver damage with the full archer progression | Balance | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--quiver-damage-progression-t4c-0133) |
| T4C-0132 | Double monster experience and expand new-island hunting populations | Balance / Spawns | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--island-hunting-expansion-t4c-0132) |
| T4C-0131 | Stronger Seraph trial HP and elder god combat debuffs | Balance / Combat | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--divine-trial-debuffs-t4c-0131) |
| T4C-0130 | Archer bow and quiver progression through 2000 agility, bow rebalance and tiered Archery bonuses | Balance | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--bow-damage-and-quiver-scaling-t4c-0130) |
| T4C-0129 | Unified journal style for every player menu | UI | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--unified-game-menus-t4c-0129) |
| T4C-0128 | Flat seraph attribute training cost | Balance | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--flat-seraph-attribute-cost-t4c-0128) |
| T4C-0127 | Rebirth-scaled starting Attack and Archery | Progression | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--rebirth-weapon-skills-t4c-0127) |
| T4C-0126 | Raise seraph attribute training limit to +100 | Balance | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--seraph-attribute-training-t4c-0126) |
| T4C-0125 | Equip armor by dropping it onto the inventory character preview | Inventory | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--character-preview-equipping-t4c-0125) |
| T4C-0124 | Fully restore health and mana on level-up | Progression | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--level-up-resource-restoration-t4c-0124) |
| T4C-0123 | Ctrl+C continuous warrior and archer auto-attack | Combat/Input | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--continuous-weapon-auto-attack-t4c-0123) |
| T4C-0122 | Storage journal design and quantity selection for dragged stacks | UI/Storage | Done | `f25803b0` | [2026-10-25](CHANGELOG.md#2026-10-25--storage-stack-transfers-t4c-0122) |
| T4C-0121 | Live quest journal with story categories, stages and next steps | UI/Quests | Done | `f25803b0` | [2026-10-24](CHANGELOG.md#2026-10-24--live-quest-journal-t4c-0121) |
| T4C-0120 | Approved horseshoe mana and health orb | UI | In Progress | `f25803b0` | [2026-10-24](CHANGELOG.md#2026-10-24--horseshoe-mana-and-health-orb-t4c-0120) |
| T4C-0119 | Unified vitals and HUD layout | UI | Done | `f25803b0` | [2026-10-24](CHANGELOG.md#2026-10-24--unified-vitals-and-hud-layout-t4c-0119) |
| T4C-0118 | Active spell hover details and journal macros page | UI/Fix | Done | `f25803b0` | [2026-10-24](CHANGELOG.md#2026-10-24--spell-hover-details-and-macros-journal-t4c-0118) |
| T4C-0117 | Character journal and combat HUD | UI | Done | `f25803b0` | [2026-10-24](CHANGELOG.md#2026-10-24--character-journal-and-combat-hud-t4c-0117) |
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
| T4C-0057 | Shift+quickbar arms a friendly-unit target cursor for Barrier/Protection/Mana Shield/Mana Surge/Bless/Healing instead of instant self-cast; click-resolution groundwork for a future other-player entity, which doesn't exist in this single-player build yet | Systems | Done | _this PR_ | [2026-09-26](CHANGELOG.md#2026-09-26--friendly-target-casting-groundwork-t4c-0057) |
| T4C-0058 | Gloomblade/Demonblade crafting ingredients (Ring of Darkness, Demon Skull, Necklace of the Black Heart, Nightsword, Chaos Sword) raised to a completable drop rate; the necklace and nightsword didn't drop from anything before | Fix | Done | `85e1c671` | [2026-09-26](CHANGELOG.md#2026-09-26--gloomblade-and-demonblade-recipes-actually-finishable-t4c-0058) |
| T4C-0059 | Class-based character creation: the eight-question quiz is replaced by a seven-archetype picker (Warrior, Archer, Paladin, Cleric, Healer, Mage, Battle Mage) with a live dressed preview; every attribute now starts at 20 plus a fixed 30-point class spread, rerolling touches only health and mana, and each class starts wearing its own kit (including six new starter items for the casting and melee paths) and knowing its own spell | Systems/Content/Fix | Done | `fd6c6b77` | [2026-09-26](CHANGELOG.md#2026-09-26--pick-a-class-instead-of-answering-riddles-t4c-0059) |
| T4C-0060 | Fix `self.true_*` formula variables (own resistance, dodge, and the rest of the family) so Resist Fire, Resist Ice, Nimbleness, Tranquility, Clear Thought and several potions/item procs stop silently rolling 0 | Fix | Done | `da00fa7f` | [2026-09-26](CHANGELOG.md#2026-09-26--fixed-self-buff-spells-that-did-nothing-t4c-0060) |
| T4C-0061 | Temple blessing chests outside all six towns (nine wards at once, an ordinary town blessing and a stronger one at Avalon), spells now leave the caster's hands instantly with the between-cast pacing untouched, unlearnable no-requirement spells dropped from the spellbook and site, and buffs cast by someone other than the player keep their strength across a save | Content/Systems/Fix | Done | `fbdedc53`, `619a8bf4` | [2026-09-26](CHANGELOG.md#2026-09-26--offering-chests-at-the-temples-and-spells-that-go-off-when-you-cast-them-t4c-0061) |
| T4C-0062 | Quest-giver and NPC locations on the reference site: fast-travel landmark, distance and bearing, and a map link, all derived from the game's own spawn and landmark data | Content/Fix | Done | `207c6485` | [2026-09-26](CHANGELOG.md#2026-09-26--the-site-now-tells-you-where-to-find-people-t4c-0062) |
| T4C-0063 | 33 creatures and characters this fork placed were standing in water or inside scenery, including every Sunken Chancel creature and the Avalon crossing; all moved onto reachable ground, with a guard test and a "too far away" message when a click can't reach someone | Fix | Done | `7bda17f7` | [2026-09-26](CHANGELOG.md#2026-09-26--people-and-creatures-stranded-in-the-sea-t4c-0063) |
| T4C-0064 | Lighthaven rat quest can be turned in, and the game saves your character on a timer instead of only on shutdown | Fix | Done | `998e3218` | [2026-09-26](CHANGELOG.md#2026-09-26--lighthaven-errand-turn-in-and-autosave-t4c-0064) |
| T4C-0065 | Quest Journal split into In Progress / Completed tabs with chain-stage progress shown for the Passage to Avalon and Godsforged lines, plus a new Monster Kill Log window tallying every kill by creature type | Systems/UI | Done | `5834d597` | [2026-09-26](CHANGELOG.md#2026-09-26--quest-journal-tabs-chain-progress-and-a-monster-kill-log-t4c-0065) |
| T4C-0066 | Original-game quest audit (t4cbible.com) + first fixes: placed Gabriel Archonis/Gaenen Elthorn in the Oracle's final chamber and Dwarthon Stoneface at Bane Blackblood's castle, and wired the "Audience to Bane Blackblood" quest gate (kill Delwobble) that none of that content could previously complete | Content/Fix | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--three-original-game-npcs-that-were-never-actually-placed-t4c-0066) |
| T4C-0067 | Fixed the "too exhausted" double-click on the five protection spells (0ms exhaustion), added their level-150+ Ultra tier (double effect, stacks with the original, 1,000,000 gold to learn), and a new Renew Armor spell that re-lays whichever of the ten (base + Ultra) the caster knows in one cast | Content/Fix | Done | `397dff3a` | [2026-09-27](CHANGELOG.md#2026-09-27--protection-spells-that-keep-up-with-you-and-their-ultra-versions-t4c-0067) |
| T4C-0068 | Renewed Wards: a new Lighthaven-area quest (Warden Aelric, clear the skeletons rising at the old grave markers) that teaches the Renew Armor spell for free | Content | Done | `397dff3a` | [2026-09-27](CHANGELOG.md#2026-09-27--renewed-wards-t4c-0068) |
| T4C-0069 | Area attack spells at level 150+ now land as a scattered, staggered multi-burst impact instead of one flash, escalating with the spell's own tier | Content/Fix | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--bigger-impacts-for-the-strongest-area-spells-t4c-0069) |
| T4C-0070 | Two Tideworn Reaver spawns near Harbormaster Rangor's dock kept aggroing and interrupting the Passage to Avalon conversation; moved them further out | Fix | In Progress | | |
| T4C-0071 | Temple blessing chests now also restore full health and mana, and play Bless's visual effect on the player | Content | In Progress | | |
| T4C-0072 | Removed item durability entirely: gear no longer degrades, breaks or needs repair | Content/Systems | In Progress | | |
| T4C-0073 | Balork was spawned at (0,0), an uninitialized placeholder; moved into the Lighthaven temple's basement | Fix | In Progress | | |
| T4C-0074 | Combat mode (Ctrl+C) was never bound to a key, so peaceful NPCs like Balork could never be attacked; wired it up | Fix | In Progress | | |
| T4C-0075 | Bound Ctrl+S to open the character sheet (attributes/stat points), alongside its existing Ctrl+T binding | Content | In Progress | | |
| T4C-0076 | Removed plain Healing Potion from monster loot tables (higher tiers untouched); brought 19 monsters whose gold drop had drifted well under the roster's established level curve back in line, with a `MonsterGoldCurve` helper and an invariant test guarding it | Content/Fix | In Progress | | |
| T4C-0077 | Level 250+ elemental attack spells (`HighTierSpellCurve`) now render a palette-shifted "Ascended" impact instead of reusing the plain lower-tier burst; recolors generated from the legacy animation frames via a new `AscendedVfxGenerator` tool | Content/Fix | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--new-colors-for-the-strongest-elemental-spells-t4c-0077) |
| T4C-0078 | GM `.summon monster` now takes an optional count (e.g. `.summon monster wolf 20`) and the summoned monsters never respawn on their own | Process/Tooling | Done | | [2026-09-27](CHANGELOG.md#2026-09-27--gm-summon-monster-count-t4c-0078-processtooling) |
| T4C-0079 | Renew Armor's mana cost is now the sum of all ten spells it can recast, and it costs 20,000,000 gold to learn | Content/Balance | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--renew-armors-real-price-t4c-0079) |
| T4C-0080 | A dead monster's body now fades from view 3-5 seconds after death, instead of lying there for the whole (often much longer) respawn wait | Content/Fix | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--corpses-dont-linger-t4c-0080) |
| T4C-0081 | Every monster level 300+ now has a 5% chance each to also drop a mana prism and a critical healing potion | Content/Fix | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--endgame-monsters-drop-more-useful-potions-t4c-0081) |
| T4C-0082 | Level 350+ elemental attack spells render brand-new SpriteCook-drawn impact animations (one per element) via a new `MythicVfxPacker` tool, on top of the 150+ shower and 250+ Ascended recolor | Content | Done | `6d9d458`, `af7e6c8` | [2026-09-27](CHANGELOG.md#2026-09-27--all-new-spell-effects-for-the-level-350-and-400-nukes-t4c-0082) |
| T4C-0083 | GM ranks (player/GM/Super GM, saved per character, owner seed file) gating the GM commands, plus new GM commands: named/NPC teleport, position, quest/world flags, unlearn, stats, respawn point, save, god/peace modes, time of day, item summon counts | Systems/Tooling | Done | `190efbd` | [2026-09-27](CHANGELOG.md#2026-09-27--gm-ranks-and-new-gm-commands-t4c-0083) |
| T4C-0084 | Removed the level-400 rung of the attack ladder (Ashfall, Tectonic Ruin, Heavenfall, Cataclysm's Herald, Solar Apotheosis, Eclipse of Ruin) and the level-400 ward Sanctum Ward; old saves drop them on load with no refund | Content/Balance | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--the-level-400-spells-are-gone-t4c-0084) |
| T4C-0085 | Every high-tier ladder spell (150-350) gets its own impact animation: new SpriteCook strikes at 150, the pixel-art eruptions at 200, the recolors at 250, the original game's top-tier animations at 300 (made halo-free), SpriteCook upgrades of those originals at 350; `GrandVfxGenerator`, `MythicVfxPacker` painted-art options | Content | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--every-high-level-spell-looks-different-t4c-0085) |
| T4C-0086 | Pressing Alt now reveals the names of every ground item and NPC nearby for a few seconds | Systems | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--reveal-nearby-item-and-npc-names-with-alt-t4c-0086) |
| T4C-0087 | Inventory: click an item to select it, then Drop or Junk it without dragging | Systems | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--drop-and-junk-buttons-in-the-inventory-t4c-0087) |
| T4C-0088 | Fix: a monster killed by an instant-kill ("vaporize") spell effect dropped no loot and gave no quest credit | Fix | Done | _this PR_ | [2026-09-27](CHANGELOG.md#2026-09-27--instant-kill-spells-now-drop-loot-and-count-for-quests-t4c-0088) |
| T4C-0089 | Compendium: every monster (including original-game ones) gets a page with drops/gold, portrait sprites, and monster/item/spell/NPC tables expand details inline instead of navigating away | Content/Systems | Done | _this PR_ | [2026-09-28](CHANGELOG.md#2026-09-28--every-monster-documented-with-portraits-inline-details-t4c-0089) |
| T4C-0090 | Fix: 37 of 39 quest/trade items (incl. the Red Spellbook) had no source anywhere in the game - restored via monster drops, new NPC trades, and a fixed quest-flag typo that had silently disconnected part of the Will of Artherk questline | Fix | Done | _this PR_ | [2026-09-28](CHANGELOG.md#2026-09-28--missing-quest-item-drops-restored-t4c-0090) |
| T4C-0091 | Locations panel: gate original-island landmarks by real progression instead of showing everything unconditionally; Key of Ogrimar crafting wired up; Oracle entries gated on holding the Key of Artherk/Ogrimar per canon walkthrough | Content/Fix | Done | _this PR_ | [2026-09-28](CHANGELOG.md#2026-09-28--locations-panel-now-respects-island-progression-t4c-0091) |
| T4C-0092 | Inventory: Drop/Junk now offer a quantity picker for stacked items instead of one at a time; general vendors' "sell" list was silently empty for everyone and now offers the player's actual sellable inventory | Fix | Done | _this PR_ | [2026-09-28](CHANGELOG.md#2026-09-28--inventory-quantity-picker-and-vendor-selling-fix-t4c-0092) |
| T4C-0093 | Fix two quest-reward necklaces (Tideworn Avalon Chart, Godsforged Torc of the First Pact) rendering as a blank inventory icon due to a nonexistent sprite variant | Fix | Done | _this PR_ | [2026-09-28](CHANGELOG.md#2026-09-28--fixed-blank-quest-item-icons-t4c-0093) |
| T4C-0094 | Corrected the Windhowl fast-travel location's coordinates | Fix | Done | _this PR_ | [2026-09-28](CHANGELOG.md#2026-09-28--windhowl-teleport-coordinate-fix-t4c-0094) |
| T4C-0095 | Fix: 11 item-turn-in quests (Passage to Avalon and 10 others) never registered progress on the required item no matter how many the player carried, because each was missing the "item." prefix inventory entries actually use | Fix | Done | _this PR_ | [2026-09-28](CHANGELOG.md#2026-09-28--fixed-11-quests-stuck-at-0n-on-their-item-turn-in-t4c-0095) |
| T4C-0096 | Fix: Avalon island's terrain was mostly an unpainted, collision-blocked "Black Tile" void (the reported solid-black patches); repainted a cohesive, much larger island with distinct grove/blight/coastal biomes, a grown Avalon Sanctuary town, and roads out to each area | Content/Fix | Done | _this PR_ | [2026-09-28](CHANGELOG.md#2026-09-28--avalon-island-terrain-rebuilt-t4c-0096) |
| T4C-0097 | Highlight recognized keywords in NPC dialogue | Content/Systems | Done | `93b0c42c` | [2026-09-28](CHANGELOG.md#2026-09-28--npc-dialogue-highlights-what-you-can-ask-about-next-t4c-0097) |
| T4C-0098 | Avalon NPC/quest audit: confirmed every quest-giving keyword actually works; independently found and fixed the same missing-prefix bug on 7 more quests outside Avalon (already covered by T4C-0095's fix/test); flagged two NPCs whose forge/mantle keywords are never mentioned anywhere in their own dialogue | Fix | Done | _this PR_ | [2026-09-28](CHANGELOG.md#2026-09-28--avalon-npcquest-audit-t4c-0098) |


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
| T4C-0099 | Avalon sanctuary, Wilds hunting grounds and staged quest dialogue | Content/Systems | Done | `d18b1772` | [2026-09-29](CHANGELOG.md#2026-09-29--avalon-sanctuary-and-wilds-t4c-0099) |
| T4C-0100 | Expand world and separate Avalon from legacy interiors | Content/Systems | Done | `c39fd433` | [2026-09-29](CHANGELOG.md#2026-09-29--avalon-separated-from-the-great-library-t4c-0100) |

| T4C-0101 | Rarity-based resale values for new items | Economy | Done | `fb6944e4` | [2026-09-29](CHANGELOG.md#2026-09-29--rarity-based-item-sell-values-t4c-0101) |

| T4C-0102 | Map editor build workspace and reusable templates | Editor | Done | `7bc7e82a` | [2026-09-29](CHANGELOG.md#2026-09-29--map-editor-build-workspace-t4c-0102) |

| T4C-0103 | Repeat sprite placement while dragging | Editor | Done | `a83e0b91` | [2026-09-29](CHANGELOG.md#2026-09-29--repeat-sprites-while-dragging-t4c-0103) |

| T4C-0104 | Map editor box selection and group dragging | Editor | Done | `9c0b0a7f` | [2026-09-29](CHANGELOG.md#2026-09-29--map-editor-group-selection-t4c-0104) |

| T4C-0105 | Avalon islands and mainland landmarks | Content/Map | Done | `acfc4a87` | [2026-09-29](CHANGELOG.md#2026-09-29--avalon-islands-and-landmarks-t4c-0105) |

| T4C-0106 | Hollow Dawn Avalon campaign and Threnody Reach (levels 200-400) | Content/Map | Done | `cfc7bda7` | [2026-09-29](CHANGELOG.md#2026-09-29--the-hollow-dawn-campaign-t4c-0106) |
| T4C-0107 | Witness Isles story routes, hunting grounds and equipment | Content/Systems | Done | `ef2db40a` | [2026-09-29](CHANGELOG.md#2026-09-29--witness-isles-story-and-hunting-t4c-0107) |
| T4C-0108 | Correct Witness Isles compendium names and hunting respawn details | Fix | Done | `fb0ef732` | [2026-09-30](CHANGELOG.md#2026-09-30--witness-isles-compendium-corrections-t4c-0108) |
| T4C-0109 | Connect the mainland chapters to the Witness Isles and Hollow Dawn questline with persistent NPC testimony, clearer handoffs, and a resolved epilogue | Content/Systems | Done | `2291b22e` | [2026-09-30](CHANGELOG.md#2026-09-30--the-names-that-remain-t4c-0109) |
| T4C-0110 | Resolve quest delivery names in the compendium, protect new-character saves, and trim redundant tests | Fix/Process | Done | `8509ba30` | [2026-09-30](CHANGELOG.md#2026-09-30--character-saves-and-quest-item-names-t4c-0110) |
| T4C-0111 | Raise Ancient Celestial armor and all Empyrean item resale to endgame values | Balance | Done | `f1834530` | [2026-09-30](CHANGELOG.md#2026-09-30--rare-endgame-resale-t4c-0111) |
| T4C-0112 | Let new characters choose Good or Evil starting karma | Systems/UI | Done | `e5abc9a3` | [2026-09-30](CHANGELOG.md#2026-09-30--choose-your-path-t4c-0112) |
| T4C-0113 | Cast self-centered area spells without target selection | Spells | Done | `20ac2b53` | [2026-09-30](CHANGELOG.md#2026-09-30--self-centered-area-spells-t4c-0113) |
| T4C-0114 | Restrict Lighthaven sanctuary to temple PvP protection | Map/Combat | Done | `85165631` | [2026-09-30](CHANGELOG.md#2026-09-30--lighthaven-temple-sanctuary-t4c-0114) |
| T4C-0115 | Add a Stonecrest shortcut to the Oracle's final trial | Content/Systems | Done | `9db4863a` | [2026-09-30](CHANGELOG.md#2026-09-30--stonecrest-ritekeeper-t4c-0115) |
| T4C-0116 | Allow CI-green PRs to merge without waiting for Codex review | Process/Tooling | Done | `4c257e95` | [2026-09-30](CHANGELOG.md#2026-09-30--merge-review-timing-t4c-0116) |
