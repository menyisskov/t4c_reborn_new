# Original-game T4C Bible content audit (in progress)

Reference scope: the original-game [Items](https://www.t4cbible.com/Items),
[Spells](https://www.t4cbible.com/Spells),
[Item Effects](https://www.t4cbible.com/itemeffect),
[Spell Descriptions](https://www.t4cbible.com/spelldesciprt),
[Monster Chart](https://www.t4cbible.com/monster),
[Monster Drops](https://www.t4cbible.com/monster1),
[Monster Graphics](https://www.t4cbible.com/monsterg),
[Arakas Quests](https://www.t4cbible.com/ArakasQuest),
[Raven's Dust Quests](https://www.t4cbible.com/RavensDustQ),
[Stoneheim Quests](https://www.t4cbible.com/StoneheimIslandQuest),
[Add-on Quests](https://www.t4cbible.com/Addon), and
[Oracle Walkthrough](https://www.t4cbible.com/oraclewlk).
RealMud and Saga server sections are excluded. Quest dialogue is checked for whether
the described keywords and steps work; walkthrough narration is not copied into NPC speech.
English is the target language; the remaining player-facing text still needs a
whole-catalog language check.

The site's other original-game sections also need comparison: Weapon, Ranged,
Armor, Jewelry, Potions, Scrolls, Quivers, Keys, Armor Gems; island and dungeon
maps; Karma, NPC Chart, Chest, Traders, Skills, Roll your Character, and
Character Classes. Their names, stats, effects, acquisition, placement, or rules
have not yet been reconciled here. Registry-name matching below is only the
first part of the requested audit.

## Registry comparison, 2026-10-23

The comparison reads the running Java and JSON registries, not filenames. Names are
case/punctuation normalized. A name mismatch is an investigation lead, not proof that
content is absent: the Bible uses several aliases and its monster table includes NPC
bosses outside `MonsterRegistry`.

The item name leads include several confirmed aliases: "Stack of Demon Tree Wood"
is the existing Demon Tree Wood Stack, "Crimsonscale's Letter" is Letter from
Crimsonscale, and "Pink Tree's Leaf" is Leaf of a Pink Tree. The Bible explicitly
says Bloodied Rat Pelt is mentioned in dialogue but never enters the backpack,
and lists Gloves of Thieving as available "Nowhere". The Tightly Wrapped Scroll
entry points to the Death of Shadeen quest and still needs an acquisition audit.

| Reference | Rows | Exact normalized display-name matches | Further work |
| --- | ---: | ---: | --- |
| Items | 1,005 | 955 | 50 names need alias or source checks; same-key display mismatches resolved |
| Spells | 75 | 73 | Gateway and Portal are categories with named destination spells |
| Monster chart | 306 | 288 | 18 include NPC bosses, plural forms, and aliases |
| Monster drop chart | 292 | 268 | 24 include colored Skraug qualifiers and NPC bosses |

For drop pairs with a resolvable monster and item, 851 simple pairs were checked.
3 remain unmatched by loot tables after item aliases and current repairs. All three
are soulstones awarded directly by their monsters' death handlers, so the simple
comparison reports them even though they have a source. This count excludes
grouped entries (for example "Platemail all"), alternative entries written with a
slash, and 14 monster names not resolved by the current registry matcher. It is **not**
a completion count. The 14 unresolved monster names, grouped and alternative entries,
and quest or shop acquisition still need manual validation.
The latest stat comparison found 46 chart monsters without an exact
level/health/minimum-damage/maximum-damage match in the registry. Some are
minor source/version differences; some are spawned placeholder definitions.
Separately, 55 dialogue NPC classes have an origin `(0,0,0)` spawn annotation.
Each needs a placement or event-summon audit before it can be counted as
playable world content.

## Changes made in this pass

- King Theodore's audience now follows the documented Thomar keywords, ten Rogue
  Mage kills, thirty-minute deadline, and return to Thomar before the King speaks.
- Lord Stonecrest and Shadeen's Masked Assassin now resolve from their quest summon
  aliases; Lord Stonecrest has the items listed in the boss drop chart.
- Nightcreeper, Pilferer, and Skeletal Centaur deaths now advance the counters read
  by Stoneheim quest NPCs.
- The Lost Helm key chain was already present in chest loot definitions. Chest opening
  now enforces each chest's configured key.
- Delnar Steelblade accepts Villain's Skulls with the documented cumulative XP
  milestones. Dark Fang gives one Ruby Dragon Fang at a time after the Tomb Raider
  task. Sir Beltigan handles the Good-path Ring of the Lion exchange.
- Crazed Nurse's documented item sources were added. French item, spell, monster,
  and NPC display values encountered in the comparison were translated to English.
- Meltar Winterstorm now trades all 24 documented rough and finely cut gems for
  their listed base XP and a possible resistance potion. The Bible says potions
  are optional but gives no odds; this implementation uses a 50% chance.
- The spawned Giant Black Widow, Purifier, and Neoflare had placeholder combat
  values. Their health, level, and damage now follow the monster chart; the
  Giant Black Widow uses the existing level-75 variant's remaining combat data.
  These monsters now have their listed drops at rates inferred from similar loot.
  Purifier and Neoflare deaths now update the counters used by Glaenshenmilandira
  to withhold the Lute quest until the player has atoned.
- The spawned Quentin Tarantula and Shrieker Rodent now use the existing fully
  specified variants' combat data with Bible chart health, level, and damage.
  Shrieker Rodent and Illusion Weaver received their listed drops. The spawned
  Illusion Weaver and Bloodlust no longer have level-1, 100-HP placeholder
  combat values; their health, level, and damage follow the chart, with XP
  inferred from existing monsters of comparable level.
- An integrity check found a dangling mana-potion drop on Tideworn Reaver; its
  item reference was corrected. A registry-wide test now checks monster loot
  against the item catalog.
- The Orcish Arch Mage quest boss was spawned at `(0,0,0)` and set the wrong
  completion flag. It now spawns on a walkable tile near Grimish's existing
  Orc Camp position; its death sets the flag read by Aloysius Starbolt. His
  warning about danger to the north now triggers randomly as described, with
  an inferred one-in-five conversation chance because the Bible gives no rate.
- The Alliance quest now counts ten pig kills and follows Baldric, Doremas,
  and Grimish's documented keyword sequence. Baldric requires level 28 and
  grants 70,000 base XP once. The dialogue-capable Grimish now occupies the
  Orc Camp position formerly used by a non-dialogue duplicate, whose spawn
  group was removed.
- The Oracle's killable Gatekeeper now drops the Gleaming Orichalcum Key. All
  six Timeless Guardian room variants now resolve and yield the stone shard
  required by the Chamber of Perseverance chest; three variants were absent.
- A spawn-group registry check found four stale creature aliases in the
  asylum and Nether Isles groups; they now resolve to the existing monster
  definitions. An obsolete Balork group was removed because `_Balork` already
  has a confirmed accessible NPC spawn. Deranged Orderly, Mad Man, both Raving
  Lunatic definitions, and Crazed Nurse now have the monster chart's health,
  level, and damage where they were placeholders. Their charted asylum drops
  were added at comparable inferred rates. Celestial Cobra and Gauze Corpse
  also gained their charted drops, including the Key of Ascension.
- The Harvester of Life drop-chart entries for Hel, Fenrir, and Jormungand
  were false positives from checking loot tables alone: their death handlers
  already grant the soulstones. The Centaur Village portal now consumes all
  three stones only after its boss summon succeeds, sends the player to the
  Harvester, and that boss yields the Crystal of Providence. Filandrius now
  handles the documented "Burden" exchange for the Bracer and Gem of the
  Immortals once. Hel's death also sets the kill flag read by the Windhowl
  Sentry for the documented gold and XP reward. The Hel form had level-1,
  100-HP placeholder combat data; its level, health, and melee damage now
  match the monster chart, with remaining combat data adapted from Fenrir.
- The remaining simple loot-chart pairs now have item-resolving drops. This includes
  Skraug variants, centaurs, Blackguard, asylum enemies, Red-Eyed Centaur, Black
  Prophet, Dark Cleric, Dark Noble, Doktor Spine, Dr. Von Pyre, Edgar, Flesh Golem,
  Ghundarg Rumblefoot, Mad Berserker Demon, Mad Madrigan, Commander Owain,
  Commander Mordred, and the four Vicars. The Bible generally names a source but
  omits probabilities; rates were inferred from comparable in-game drops. Quest
  keys on Edgar, Mad Madrigan, and Runaway Patient were set to guaranteed drops.
- The 14 drop-chart names absent from the monster registry include dialogue NPCs,
  spelling aliases, and three entries with no listed loot. Existing death handlers
  already award A Sweeper's Arcane Scroll, Greyleaf's Belladonna Berries, and Haden's
  Blade of Ruin. Delwobble now drops Ring of Light. Balork, Bane Blackblood,
  Dorken Rotsmell, Jarko, Mhorgwloth, and the Renegade Orc Leader now grant their
  listed items when killed. Jarko's Spellbook is guaranteed because Lord Sunrock's
  quest consumes it; other new NPC loot probabilities are inferred.
- Seventeen monster definitions with level, health, or melee damage placeholders
  now use the chart values. This includes Black Prophet, Dark Cleric, Dark Noble,
  Mad Berserker Demon, Commander Mordred, Centaur Manaweaver, and several Skraugs
  whose level was incorrectly set to 150.
- Vincent Swiftblade's Add-on camp assault now requires level 8, counts fifteen
  mercenaries at each of the three existing camps, summons each camp's lieutenant,
  then summons the leader at a walkable northern-camp location. Killing the leader
  unlocks Vincent's repeatable 750 XP per level and 500 gold reward. The lieutenants'
  combat stats now match the monster chart, and Vincent's directions include the
  required sequence. The northern-camp summon point is inferred from the existing
  camp positions because the Bible provides directions rather than tile coordinates.
- Tristan's Obsidian Conclave quest now counts ten Knight kills and summons Commander
  Mordred beside the existing camp chest. Mordred already had a guaranteed key; his
  death now advances the quest, and using that key on the chest completes the stage.
  The summon point is walkable and adjacent to the existing Knight and chest spawns.
- The Add-on Tome of Arcane Knowledge handoff now advances when Anthor the Mad dies.
  His Tome drop is guaranteed, allowing Filandrius to take it and award the Belt
  and Gem of Unstable Protection as the walkthrough describes.
- All 51 exact-name spell gold-cost mismatches in the Bible's spell chart were
  aligned with the listed values. Light now has the chart's level, Intelligence,
  and Wisdom requirements; Detect Hidden and Resist Ice now have the listed
  Wisdom requirement. The registry comparison now reports no numeric mismatch
  for level, Intelligence, Wisdom, mana, or gold cost among the 73 exact-name spells.
- The Raven's Dust Kraanian Egg chain now moves from Zhakar's egg handoff through
  two separate level-up gates, the Deep One Boss's Note, Aloysius's 32,000-gold
  Mana Rod sale, and the Zzartgax summon. Zzartgax awards the Ethereal Amulet
  and 75,000 experience. Zhakar's scripted NPC uses the Tower of Sorcery spawn,
  replacing the mute duplicate there; a focused end-to-end test covers the path.

## Remaining audit work

1. Reconcile grouped and alternative drop-chart entries and the Purple
   Bigbruddalbashah variant, whose owner NPC does not expose a distinct color loot
   source. Check whether each added loot source is obtainable in-game; several
   event monster definitions currently have no direct world spawn and need their
   summon paths audited. Document inferred rates as implementation choices because
   the Bible does not give canonical probabilities.
2. Check item acquisition through shops, chests, and quest rewards. Registry presence
   alone does not mean a player can obtain an item.
3. Trace every Arakas, Raven's Dust, Stoneheim, Add-on, and Oracle walkthrough step
   from keyword through reward. Previous research in
   `docs/content-ideas/2026-09-original-quest-audit.md` identifies likely gaps, but
   its chest-key and Ruby Dragon Fang notes are superseded by this pass.
   The Raven's Dust Alliance quest was repaired in this pass, but the other
   walkthrough chains still need the same end-to-end check.
4. Compare spell prerequisites, teachers, skill-point costs, and effects and check
   the separate item-effects and spell-description pages. Gold cost and numeric
   prerequisites now match the 73 exact-name spells, but teachability and spell
   behavior still need end-to-end checks.
5. Verify map entrances and spawn placement for the named NPCs and bosses in the
   walkthroughs, then run the full game suite and an in-game smoke test. The
   Oracle Guardian gap shows spawn group creature references need a registry
   check in addition to direct `@Spawn` annotations.

The original-game audit is not complete until these checks and repairs are done.

## Angelic gods follow-on

The user subsequently directed implementation of the gods feature. Artherk and
Ogrimar now have white and black angelic regalia, quest-item drops and five-minute
respawns. Eight mythological gods cover every caster school, warriors and archers,
with their own equipment, three tiers, and five- or seven-and-a-half-minute respawns.
Their shrines occupy the new maps and have stories explained by the Celestial
Chronicler. Every god has a 1% chance to drop a reusable return scroll bound to
the backpack, preserved through death and rebirth. This follow-on is now
implemented as the [Divine Concord](../divine-concord.md), with ten gods and
112 equipment items. The original-game audit's remaining checks above are still open.

Latest full verification: `mvn -B test` passed all 605 tests on 2026-10-24.
`assets/i18n/lang.json` parses as JSON, and `git diff --check` passed.
