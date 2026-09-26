# Changelog

All notable game-content and systems changes are documented here, newest first.
Format is loosely based on [Keep a Changelog](https://keepachangelog.com/),
grouped by content pass rather than by individual commit. Every entry
references a task ID from [`TASKS.md`](TASKS.md) — see that file for status,
commit links, and finer-grained notes.

This file was backfilled on 2026-09-19 by walking the project's git history;
dates are the commit dates of the work, not retroactively invented. From now
on, every content/feature pass adds its own entry here as part of the work
(see `CLAUDE.md`), not after the fact.

## [Unreleased]

## 2026-09-26 — Gloomblade and Demonblade recipes actually finishable (T4C-0058)

### Changed
- **Gloomblade and Demonblade crafting.** Lance Silversmith's recipes for these two blades ask
  for a Ring of Darkness, a Demon Skull, a Necklace of the Black Heart and a Chaos Sword. One of
  those didn't drop from anything at all, and the rest were rare enough that the recipes were
  practically unreachable. All four now drop far more often from the enemies that carried them
  (Griroesh and Dune Raiders), and the Necklace and a matching Nightsword now drop too, so both
  blades can actually be forged.

## 2026-09-26 — Spell hierarchy, spellbook info, and a few quality-of-life fixes (T4C-0054)

### Added
- Hovering a spell in your spellbook, or in a shop or trainer list, now shows what it actually
  does: its description, element, whether it's a physical or mental attack, and its level,
  Intelligence, Wisdom and mana requirements. Hovering a shop item shows the same kind of detail.
- Holding down a "+1" button (spending stat points, skill points, or adding to a shop/spell
  basket) now keeps adding for as long as you hold it, instead of one click per point.

### Changed
- Every element's offensive spells now form a proper progression at the Lighthaven spell
  seller — you need to already know the previous spell in the line (for example Stone Shard →
  Shatter → Earthquake → Boulders) before the next one becomes available.
- Learning a spell now costs skill points based on how powerful it is, instead of a flat 5
  points for everything. Low-level spells stay cheap; the strongest spells cost up to 100.
  A few spells that used to teach you nothing when bought from certain trainers (their gold and
  points were spent, but the spell never actually appeared in your spellbook) now work correctly.
- Three internal effects that were never meant to be player spells (a boss aura, the rebirth
  aura, and the automatic on-level-up stat boost) no longer show up in the spellbook or at the
  spell seller. If your character already had one of these in their spell list from before this
  fix, it's simply hidden from view now — nothing else changes.
- Spell cast times now speed up the more your level outgrows a spell's own requirement, the same
  way older spells already worked. The very strongest spells (right at the level cap) are already
  as fast as they'll ever get the moment you learn them.
- Strength keeps meaningfully increasing how much you can carry well past 1000 strength, instead
  of the old formula flattening out almost entirely by that point.

## 2026-09-26 — The reference website, rewritten for players (T4C-0055)

### Changed
- **The world now reads as a journey, not a list.** Zones, maps and quests are grouped into
  five named stretches of the world — the coast road, the crossing to Kraanhold, the road to
  Avalon, the world coming apart, and the last peak — each introduced in a short paragraph and
  ordered by the level you would reach them at. You can read the Zones page from top to bottom
  and come away knowing the whole route.
- **Every zone says where you go next.** A zone page now names the place you came from and the
  place that follows it, and describes who holds the ground, why they turned hostile and what
  to bring, instead of listing when it was built.
- **Plain language everywhere.** Page headings, captions, table columns and empty-state
  messages across the site were rewritten for someone who plays the game and has never read a
  line of its code. Panels now say "What lives here", "How it fights" and "Where to get it"
  rather than naming internal systems.
- **The harder numbers are explained or tucked away.** Damage rolls, spell effect data and
  other raw figures now sit behind a "For the curious" link, with the plain version — a damage
  range, a mana cost, a chance to land — shown by default.
- **Patch notes on the site are written for players**, and no longer mention internal tooling.

### Fixed
- **Quest pages described required items as rewards.** Ten quests told you that you would
  *receive* an item that you actually have to *bring* and hand over — and several said the boss
  carrying it "isn't required" when that boss is the only place it drops. Every quest page now
  separates what you hand over from what you are given, and the walkthroughs match.
- **Quest item rewards were invisible.** The item you get for finishing a quest was never shown
  at all, so the whole Godsforged crafting chain looked as though it paid only gold. Those
  seven quests now show the weapon, core or sigil they hand you, and each of those items now
  lists the quest as a way to get it.
- **Spell pages showed gibberish where the mana cost should be.** Fourteen gateway and portal
  spells displayed a fragment of the game's own scripting instead of a cost; they now read
  "All your mana".
- **Deep Ones Cave listed its level range as a place name**, and never said what level it is
  for. It now reads 32–42, like every other zone.
- **The site contradicted itself about the level cap**, quoting an old, higher number in one
  place and the real one in another.

## 2026-09-26 — Quest locations always shown (T4C-0053)

### Changed
- Every quest page on the reference website now shows where to go, even a crafting turn-in
  quest that has no kill objective — previously only kill quests listed a location. A quest
  page now also always shows a map when one exists for that area, including a couple of quests
  that used to show coordinates but no map.

## 2026-09-26 — Full quest walkthroughs (T4C-0052)

### Added
- **Full quest walkthroughs on the reference website.** Every quest page now opens with a
  plain-language walkthrough explaining where to find the quest-giver, where to fight, any item
  you need to bring, and what you get for finishing — on top of the existing step-by-step
  breakdown and exact offer/completion dialogue. In-game dialogue is unchanged.

## 2026-09-25 — Safer changes and fewer review round-trips (T4C-0051, Process/Tooling)

### Changed
- The project's working rules now include a self-check before any change is proposed. It
  covers existing characters' saves when a limit changes, numbers on the reference website
  matching the game exactly, and notes that could go out of date. It also adds step-by-step
  guides for balance changes, for shipping a change, and for confirming the website is live.
  No in-game effect.

## 2026-09-25 — The Bloodline Vault (T4C-0044)

### Added
- **Bloodline Vault.** Your storage chest now has a second stash: click the title of the Storage
  window to switch between **Personal Storage** (this character only) and the new **Bloodline
  Vault**, which is shared by every character on your account. Move a rare drop or a pile of
  gold into the vault on one character, then pull it back out on another — no more mule runs.
  The vault has its own banked gold, tracked separately from any one character's stash.

## 2026-09-25 — The Windhowl War-Party, a warband camp (T4C-0045)

### Added
- **The Windhowl War-Party.** A Centaur warband now camps in its own clearing near the
  Windhowl Marches: five **Warband Raiders** around a **Warband Banner-Bearer**. Kill the
  banner-bearer first — it visibly rallies the rest — and every raider you fell while its
  banner is down chips away at the war-party. Clear the whole camp that way and the
  **Warband Warlord** rides in on the spot where the last raider fell, a tougher fight with
  its own loot table headlined by the new **Chieftain's Warhorn** neck piece.

## 2026-09-25 — Two Masters: a choice at the Kraanhold dock (T4C-0046)

### Added
- **Old Corrin** now sits near Dockmaster Thessaly at the Kraanhold crossing. Once you've
  cleared enough Toll Trolls for passage, you get a choice: report to Thessaly as before for
  gold and experience on the spot, or talk to Corrin instead and trade that payout for her
  standing invitation to the Kraanian trading houses — a small experience bonus on every
  Kraanian kill, for as long as this life lasts. Whichever one you pick first is final; the
  other closes the offer.

## 2026-09-25 — The Wyrm Scales and the Convergent Wyrm (T4C-0047)

### Added
- Each of the five Elder Wyrms can now drop its own named **scale**. Bring one of each -
  Rootcrown, Pyreclaw, Mistwing, Duskmaw, Galecrest - to the new **Keeper of the Sixth Seal** in
  the Colosseum, and she'll open the seal behind her. What answers isn't a sixth wyrm like the
  others: it's **The Convergent Wyrm**, a boss above all five that draws on every element at
  once, with its own legendary staff, neck piece and crown, plus a piece from every elemental
  armor set. You can do this again on a later life - the seal only asks for the scales, not a
  one-time favor.

## 2026-09-25 — The Hourglass Trials (T4C-0048)

### Added
- **Trial Warden Osric** now stands near the Mirror of Echoes in the Colosseum. Where the mirror
  asks who you were, Osric only asks how fast you are: say "trial" and he'll summon a Sandglass
  Sentinel — a fixed opponent, the same strength every time, so a fast clear actually means
  something. Beat your own record and you're paid for it; clear it again without beating your
  own time and it's practice, not profit. The tier you're offered rises the more times you've
  been reborn. Ask "times" to hear every tier you've ever cleared.

## 2026-09-25 — Lost Keys of Kraanhold (T4C-0049)

### Added
- **Twelve named keys** are now rare drops across Kraanhold's own creatures — Kraanian Flyers,
  Workers, Milipedes, Plagues, Reapers, Stompers, Wyrmlings and Dragonguards, the Toll Trolls on
  the coast road, and the Windhowl War-Party's own raiders — each a hint at a specific lock, with
  nothing pointing you there but the key's own story. Bring one to the matching chest — the
  Sunken Ledger Coffer in the caverns, the Plague Warden's Strongbox in the depths, the
  Wyrmling's Hoard Casket near Drake's Lair, or the Warband's Buried Chest outside the war-party's
  camp — and it opens on gold, potions, and for two of the rarest keys, a piece of gear worth
  keeping.

## 2026-09-25 — The Unsigned Letter (T4C-0050)

### Added
- The first time you're reborn, a letter turns up in your pack that wasn't there before — no
  seal, no name, just three words: "I remember you." Someone already knows. Bring it to
  Mirrorwarden Ysmera in the Colosseum and ask her about it — she's been watching every echo a
  rebirth leaves in her mirror, and yours was no exception.

_Nothing pending._

## 2026-09-25 — Login, character selection and radar (T4C-0043)

### Added
- **Radar.** A round radar now sits in the top-right corner of the game view, under your gold.
  You are the white dot in the middle; everything around you shows in the direction it is on
  screen: **other players blue, NPCs green, monsters yellow, bosses red**. Bosses are drawn a
  little bigger and on top, so a crowd never hides them. An NPC you have turned hostile shows
  as a monster. The inner ring is roughly the edge of your screen; the outer ring reaches about
  twice as far. You can turn it off with the new **Radar** box in Options (Video).
- The startup screen now has a **loading bar** with a percentage, so you can see the game is
  making progress.
- The character selection screen shows a **details line** for the chosen character: gender,
  rebirths and gold.
- **Key hints** along the bottom of the character selection and creation screens list every
  shortcut for the current step.

### Changed
- The game **remembers the last character you played** and selects it when you start the game
  or switch characters.
- **Double-click** a character to enter the world. Rows light up when you point at them, and
  the mouse wheel moves through the list.
- The loading screen after you pick a character now says **who you are entering the world as**,
  over the dimmed selection artwork instead of a black screen.
- The character selection **Back** button is now labeled **Quit**, because that's what it does.
- The Options button for switching characters now reads **Switch Character** instead of just
  "Character".
- The name you type for a new character now has a blinking cursor. When you choose a gender with
  the arrow keys or M/F, the chosen button now lights up.

### Fixed
- Characters saved above the level cap were listed with their old level (for example 638). The
  list now shows the level you'll actually have in game (400 at most).
- If a character failed to load, or deleting one failed, the error was never shown on the
  selection screen. It now appears under the character list.

## 2026-09-25 — The Mirror of Echoes (T4C-0042)

### Added
- **Ysmera the Mirrorwarden** now stands in the Colosseum beside a very old mirror. Every life
  you shed when you're reborn leaves an echo in its glass, and she will let you face yours.
- **Your Echo** steps out of the mirror looking exactly like you: your face, your armor, your
  weapon, your wings, your name. It fights at your level with your own combat skill and dodge.
  Warriors face their own blade, archers their own bow, and mages watch their own strongest
  attack spell come flying back at them.
- The Echo talks to you while you fight, and it knows you: your level, how many times you've
  been reborn, how much gold you're carrying, and the kind of fighter you are.
- **Ten trials.** Each Echo is stronger than the last (up to twice as dangerous by the tenth).
  The first win of each trial pays experience (a share of your current level) and gold, growing
  with every trial. Say "trials" to Ysmera to see how far you've come.
- The fight is fair for everyone: the Echo's blows are measured against your own health, and it
  sizes its own health to how hard you hit, so a level-20 archer and a level-400 archmage face
  the same kind of battle.
- **Falling to your Echo costs nothing.** You wake on the spot with half your health, no lost
  experience, no dropped items. The Echo fades. Walking away or stalling for too long also makes
  it fade.
- **Win all ten trials and your Echo is bound to you.** Say "call" to Ysmera and it joins you as
  a companion, dressed in whatever you're wearing at the time, healing you and casting your own
  signature spell.
- The first time you log in after this update, listen carefully.

## 2026-09-25 — F2 is free for your macros (T4C-0041)

### Changed
- The on-screen coordinates readout moved from **F2** to **F12**. F2 no longer does anything by
  default, so you can bind it to a spell in the Macros window (Ctrl+M) without it also toggling
  the coordinates.

## 2026-09-25 — Fast travel, spell book and macros windows redesigned (T4C-0040)

### Changed
- **Fast travel (Ctrl+L)** has been redesigned. Destinations now sit neatly in the list's slots
  instead of floating between them. Clicking one shows what it is, and unlocked zones also show
  a suggested level range and a short description. A Travel button, a double-click or Enter
  takes you there, so a stray click no longer teleports you by accident. The scroll bar shows
  where you are in the list, and its arrows and the mouse wheel scroll it.
- **The spell book** shows which page you're on and how many pages there are. The mouse wheel
  and Page Up / Page Down now turn pages as well as the book's corners. Spell durations read as
  real times ("45 s", "5 min") worked out for your character, instead of raw numbers.
  Hovering the small + / - button next to a spell explains that it adds or removes the spell
  from your macros.
- **The macros window (Ctrl+M)** has been rebuilt on the original game's macro window art. Each
  macro shows its spell icon, name and key in tidy rows. Select one, then click Bind (or its key
  box) and press the key you want. Clear unbinds it, Remove deletes it, and the green and red
  arrows move it down or up the list. The book icon opens your spell book to add more macros,
  and the scroll icon opens the Controls list.
- The Controls list now calls Ctrl+L "Fast travel" and mentions the new spell book page keys.

### Fixed
- The spells "Gel" and "poison fleche" now have their English names: **Freeze** and **Poison
  Arrow**.
- Pressing the key you're binding to a macro no longer also walks your character or triggers
  that key's normal shortcut.
- The spell book no longer plays its page-turn sound when you're already on the first or last
  page.
- Pressing Enter in the storage chest's search box now finishes the search instead of opening
  chat.

## 2026-09-25 — A new storage chest, and better control over your character (T4C-0039)

### Changed
- **The storage chest has been completely redesigned.** Your stored items and your backpack now
  sit side by side as two item grids, in the same style as the game's other windows, instead of
  a long text list that spilled out of the window. Identical items stack with a count, damaged
  gear shows its durability, and hovering over anything shows its full details (stats,
  requirements, durability).
- The chest now has category tabs (Weapons, Armor, Legs, Boots, Gloves, Rings, Misc) and a
  search box, so you can find one item among hundreds.
- Moving items is faster: drag and drop between the two grids, right-click or double-click to
  move one item, Shift+click to move a whole stack, or Ctrl+click to type exactly how many.
  **Take All** empties whatever the current tab and search show into your bag. **Stash All Gear**
  puts every weapon, armor piece and ring from your bag into the chest and leaves your potions
  and scrolls with you.
- Depositing or withdrawing gold opens a small window where you can type the amount or press
  Max, and your banked and carried gold are shown with thousands separators.
- A new **Controls** window lists every keyboard shortcut and mouse action in the game, grouped
  by topic. Open it with **Ctrl+H** or from the new Controls button in the Options window.
- Reloading the map graphics (a testing tool) moved from the R key to **Ctrl+Shift+R**, so you
  can no longer trigger the stall by accident.

### Fixed
- **Items and gold in your storage chest are now actually saved.** They used to disappear
  whenever you restarted the game.
- Putting a damaged item into storage and taking it back out no longer repairs it for free, and
  charged items (such as books) keep their remaining charges instead of refilling.
- If you can't take something out of storage because it's too heavy, or because it's a unique
  item you already carry, the game now tells you why instead of silently doing nothing.
- Pressing Ctrl+W (world map) or Ctrl+Q (quest journal) no longer also walks your character.
- Typing in the storage search box no longer walks your character or triggers other shortcuts.
- The "32 FPS" video option in the Options window now has a proper name ("Smooth Animation")
  instead of showing its internal label.

## 2026-09-25 — The Elder Wyrms are complete, and three new armor sets (T4C-0038)

### Added
- **Four new Elder Wyrms** join the Rootcrown Wyrm in Drake's Lair, so every class now has its
  own level-700 wyrm to hunt:
  - **The Pyreclaw Wyrm** (warriors, fire) drops the Searing Greatsword, Molten Warhelm and
    Forgeplate Gauntlets.
  - **The Mistwing Wyrm** (archers, water) drops the Farsight Longbow, Rainveil Mantle and
    Fogstride Boots.
  - **The Duskmaw Wyrm** (intelligence mages, dark) drops the Umbral Rod, Nightshroud Mantle and
    Eclipsed Crown.
  - **The Galecrest Wyrm** (hybrid mages, air) drops the Tempest Wand, Windswept Mantle and
    Thunderhead Circlet.

  Each is a unique, legendary piece built for its class. Every wyrm can also drop pieces of its
  class's Ancient Celestial and Empyrean armor, plus healing and mana potions, so no kill is
  wasted.
- **Centaur Slaying set:** a seven-piece archer set (armor, helmet, gauntlets, boots, leggings,
  belt and a quiver) to go with the Bow of Centaur Slaying. It drops from the Centaur King and,
  more rarely, from Centaur Warriors.
- **Drowned Inquisition set:** a six-piece water set for intelligence mages, with extra
  resistance to dark magic. It drops from Mordrenn the Drowned Inquisitor in the Sunken Chancel
  and, more rarely, from Drowned Acolytes.
- **Cinderforged set:** a six-piece fire set for intelligence mages, with extra fire resistance.
  It drops from Ignarok the Emberfang in Cinderreach Hills and, more rarely, from Cinder Whelps.

## 2026-09-24 — Mordrenn and the Centaur King pay out more gold (T4C-0037)

### Changed
- **Mordrenn the Drowned Inquisitor** and the **Centaur King** now drop noticeably more gold on
  death - their old payouts were low enough that regular monsters near their level paid out
  better gold for less effort. Mordrenn now drops 200-600 gold (up from 90-275), and the Centaur
  King now drops 1,100-2,900 gold (up from 350-900). Nothing else about either fight changes.

## 2026-09-24 — A gold economy pass (T4C-0036)

### Changed
- The biggest quest payouts in the game have come back down to earth. The five quests that forge
  a Godsforged relic, the two quests that craft its components, and the rewards for clearing
  Drake's Lair and reckoning with the Fading Veil all paid out gold far beyond anything else in
  the game - enough that finishing even one of them made every other gold reward, and everything
  there was to spend gold on, feel pointless. Their gold payouts are now a fraction of what they
  were; the items, XP, and everything else about these quests are unchanged.
- Every town's general goods vendor now also carries two consumables that were previously
  impossible to buy anywhere: **Mana Prisms** (10,000 gold) and **Critical Healing Potions**
  (25,000 gold). Look for them alongside the regular potions and scrolls in Lighthaven, Silversky,
  Windhowl, Stonecrest, and Avalon Sanctuary - a real place to put gold to work at any stage of the
  game, not just at the very top of it.

### Fixed
- Critical Healing Potions actually heal you now. They used to do nothing at all when drunk.

## 2026-09-24 — A shortcut for repeat rebirths (T4C-0035)

### Added
- A new NPC, **Anchorite Rowan**, can be found within the Avalon Wilds. Complete a short quest for
  them once - **The Waking Rite** - and Rowan can perform the same rebirth rite the Oracle does,
  right there in Avalon, every time from then on. No need to make the long trip back to the
  Oracle's dungeon for a second, third, or later rebirth.
- The Waking Rite requires character level 125 or higher to complete (you can still work on it
  below that level, you just can't turn it in yet), and once finished, the shortcut is unlocked
  permanently for that character.

## 2026-09-24 — Every boss drops something (T4C-0034)

### Changed
- Every boss now has a real chance of dropping something useful even without landing its rarest
  item. Bastion Warden and the Deep Ones Cave boss used to drop only a single item each - they
  now also drop potions (and Bastion Warden a full matching gear set, in line with other bosses
  its size). Several other bosses (Ignarok the Emberfang, Arch Drake, Greater Drake, Mordrenn the
  Drowned Inquisitor, the Centaur King, the Hollow King, Coastwarden Ithrak, and Makrsh P'Tangh)
  now also drop healing and mana potions alongside their existing rare loot, so a kill that misses
  every rare roll still isn't a total loss.

## 2026-09-24 — Godsforged: a new tier of crafted relics (T4C-0033)

### Added
- A new tier of gear above Legendary: **Godsforged** relics, one for each class - the
  **Godsforged Warblade** (warrior), **Godsforged Stormbow** (archer), **Godsforged Voidglass
  Rod** (intelligence mage), **Godsforged Zephyr Wand** (hybrid mage), and the **Godsforged Torc
  of the First Pact** (wisdom mage).
- These aren't dropped by any monster - they're crafted. Three survivors of an ancient order, the
  **Forgewrights of the First Pact**, can now be found within the Avalon Wilds: **Ember-Smith
  Corvain** tempers a core from Wyrmforged Embers (a rare drop from Makrsh P'Tangh and Ignarok the
  Emberfang), **Warden Seressa** binds a sigil from Veiled Aether Shards (a rare drop from The
  Rootcrown Wyrm and Ysolde the Veiled Matriarch), and **Grandmaster Tholvenn** combines both into
  whichever Godsforged item you name, once you bring him one of each.
- The reference website's item list now shows a distinct "Godsforged" rarity above Legendary.

## 2026-09-24 — Passage to Avalon becomes a two-stage quest (T4C-0032)

### Changed
- Reaching Avalon is no longer a single fight. Harbormaster Rangor now sends you after Coastwarden
  Ithrak's scouting party first, to prove you can actually handle yourself out on the tideline.
  Only once they're cleared out will he trust you with the real job: breaking Ithrak's whole
  warband and taking the chart that opens the crossing to Avalon.
- Both stages have their own reward, and Rangor's dialogue changes to match wherever you are in
  the chain. Anyone who already fought their way to Avalon before this change won't be sent back
  to face the scouts — that fight already happened for them.

## 2026-09-24 — Passage to Avalon gets a personal story (T4C-0031)

### Changed
- Harbormaster Rangor, who sends you on the quest to reach Avalon, now has more to say if you ask
  him about it. He'll tell you why he really cares about getting you across safely, and share a
  dockside rumor about the old pact that's supposed to protect Avalon — and whether it's still
  holding. His send-off once you complete the quest reflects that too.
- Nothing about the quest itself changed — the same fight, the same reward, the same unlock.
  This is just Rangor having more of a voice.

## 2026-09-24 — Broader elemental resistance, and no more light resistance (T4C-0030)

### Changed
- Rebalanced how armor and jewelry resist elemental damage. Gear used to give a large amount of
  resistance to just one element (usually whichever one it was themed around) and nothing at
  all against the other five. It now gives a smaller amount of resistance against several
  different elements at once, so you're not left completely exposed to damage types your gear
  doesn't happen to match.
- No item anywhere in the game grants resistance to light damage anymore, including gear themed
  around light magic. Everything else about light magic (dealing light damage, being taught
  light spells, and so on) is unaffected — only resisting it is now off the table.
- One item, the Wight-Bound Amulet, used to make you slightly more vulnerable to light damage as
  a drawback for its otherwise strong bonuses. That drawback has been removed rather than moved
  to a different element.

## 2026-09-24 — The Rootcrown Wyrm, first of the Elder Wyrms (T4C-0029)

### Added
- A new boss, **The Rootcrown Wyrm**, now lairs alongside Arch Drake in Drake's Lair. It's the
  first of the **Elder Wyrms** — ancient dragons said to predate Ignarok, Mordrenn, and the rest
  of the known Drake line, each one tied to a different calling instead of a different element.
  The Rootcrown Wyrm is the wisdom-mage of the line, wreathed in root and stone.
- Defeating it can drop three new legendary items for wisdom-focused spellcasters: the
  **Verdant Sceptre**, an **Ageless Mantle**, and a **Timeless Circlet** — all themed around
  earth magic and unlike anything else currently in the game.

## 2026-09-24 — Endgame legendary weapons, richer boss loot, and a flatter items page (T4C-0028)

### Added
- Two new legendary weapons for endgame players: **P'Tangh's Boneshard Longbow**, a legendary
  bow, and **P'Tangh's Lichbone Staff**, a legendary staff for spellcasters — both dropped by
  Makrsh P'Tangh. Every earlier legendary weapon was a melee weapon for warriors, so archers and
  casters now have their own signature endgame option too.

### Changed
- Several bosses that used to drop only their signature item now also drop a full matching
  armor set, the same way Makrsh P'Tangh already does: Sir Caradoc, the Sundered Knight; The
  Hollow King; Coastwarden Ithrak; and the Lesser Drake.
- The Items page on the reference website now shows every item's requirements, armor class,
  damage, bonuses and other stats directly in the table, so you no longer need to open an item
  to see what it does.
- The Items page now names the exact monster or boss that drops an item, instead of just saying
  "monster drop".
- The Items page now has Weapons/Armor/Accessories tabs above the table, so you can narrow the
  list to one category instead of scrolling through everything at once.

## 2026-09-23 — Item rebalance: every item matches its class (T4C-0027)

### Changed
- Every piece of new gear has been rebalanced around one rule: what an item asks for decides
  what it gives.
  - Warrior gear (strength): armor class, resistance to every element, strength and a lot of
    attack.
  - Archer gear (agility): armor class, resistance to every element, agility and archery.
  - Intelligence gear (fire, water, dark): that school's power and resistance, plus extra
    intelligence, but a little less armor class.
  - Wisdom gear (earth, light): that school's power and resistance, plus wisdom and more armor
    class.
  - Gear that asks for intelligence and wisdom equally: air power and resistance, plus both
    stats.
- Armor class now follows an item's endurance requirement: the more endurance it asks for, the
  more armor it gives. This includes rings and amulets.
- No item asks for more than 600 endurance anymore. Several drake, boss and Bastion items used to
  ask for 900–2600 endurance and stat totals no character could reach. They are now wearable
  at the level cap and hit hard in their class instead.
- Aerie's Drakeheart Signet is now a true warrior ring: it asks for 600 strength and 600
  endurance, and gives +50 strength, +120 attack, 33 armor class, resistance to every element
  (doubled for fire) and +40 damage.
- The Ancient Celestial and Empyrean armor sets now ask for each element's own casting stat:
  intelligence for fire, water and dark; wisdom for earth and light; both for air. Each set
  gives that stat back. Warrior sets carry the most armor class of any set.
- Some boss items changed stat to match their theme. The Crown of the Hollow King, Ysolde's
  Veiled Circlet, the Wight-Bound Amulet and the water-themed cowl, talisman, chart and vestment
  now ask for intelligence. Their dark or water power stays.
- Ring of the Archer's very large bonuses (+150 agility, +250 archery) have been brought down
  to match its requirement (+62 agility, +150 archery). It now also gives armor class and
  resistances.

### Added
- The project's design rules are now written down in one place, covering levels, rebirths,
  spells, items, colors and the reference website. The file is kept up to date as new decisions
  are made. No in-game effect.

## 2026-09-23 — Rebirths page on the reference website (T4C-0026)

### Added
- The reference website has a new "Rebirths" page listing all 50 rebirths. For each one it
  shows the level you need, the attributes you start with, the energy points you get to spend,
  and how strong your Seraph aura becomes. It also explains how to be reborn and what each of
  Alphan's associates sells for energy points.

## 2026-09-23 — Level cap 400, rebirth limit, and an even spell ladder (T4C-0025)

### Changed
- The maximum character level is now 400. Characters above it are brought down to 400, and
  experience stops counting once you reach it.
- A character can now be reborn at most 50 times. The Oracle will tell you when your soul can
  be purified no further. At 50 rebirths you start each life with 270 in every attribute.
  Characters already past 50 rebirths keep their stats but count as 50 for the Seraph aura.
- The high-level spells have been rebuilt into one even ladder. Every school (fire, water,
  earth, air, dark and light) now has exactly one attack spell at levels 150, 200, 250, 300,
  350 and 400. Spells at the same level ask for the same stats and hit equally hard.
- Spell stat requirements now fit the stats you actually have at that level. A level-L spell
  asks for 2.5×L of its school's main stat (Intelligence for fire, water and dark; Wisdom for
  earth and light) plus a little of the other. Air asks for Intelligence and Wisdom equally.
  The level-400 spells need 1000 in the main stat. Spells no longer ask for levels (650, 900)
  or stats you could never reach.
- Nine existing high-level spells moved onto the new ladder. They keep their names, and anyone
  who already knows them keeps them:
  - Sunscour moved to level 150.
  - Land Slide and Leyward Bastion stay at level 200.
  - Gravebreaker moved to level 250 and now hits a single target.
  - Voidreave Lance moved to level 300.
  - Stormcaller's Judgment and Emberqueen's Wrath moved to level 350.
  - Cataclysm's Herald and Sanctum Ward moved to level 400.
- Archmage Thalindra in the Avalon Sanctuary now teaches by school. Say "fire", "water",
  "earth", "air", "dark" or "light" to see that school's six spells. "train" still shows the
  wards and healing spells.

### Added
- 29 new attack spells complete the ladder:
  - Fire: Scorchbrand, Pyreburst, Magmaheart Lance, Sunforge Brand, Ashfall.
  - Water: Rime Lance, Frostgale, Abyssal Spear, Tidebreaker, Drowning Deep.
  - Earth: Stonefang, Mountain's Fist, Worldroot Upheaval, Tectonic Ruin.
  - Air: Galespike, Thunderhead, Skysplitter, Tempest Lance, Heavenfall.
  - Dark: Nightfang, Shadowblight, Soulrend, Umbral Tide, Eclipse of Ruin.
  - Light: Dawnflare, Radiant Spear, Seraph's Verdict, Hallowed Nova, Solar Apotheosis.
- Dawnwell Renewal, a level-300 light spell that heals you and your nearby group.
- Six elemental archmage mantles, one per school, in the school's color. Say "mantle" to
  Archmage Thalindra to buy them. Each boosts its school's spell power and resistance and adds
  to your main casting stat. Each needs 600 of that school's main stat; the air mantle needs
  375 Intelligence and 375 Wisdom instead.
  - Pyromancer's Mantle (red, fire)
  - Tidecaller's Mantle (blue, water)
  - Geomancer's Mantle (green, earth)
  - Windweaver's Mantle (gold, air)
  - Shadowmancer's Mantle (black, dark)
  - Lightbringer's Mantle (white, light)

### Fixed
- The Bastion Sentinel's Mantle and the Sunken Vestment couldn't be seen or taken off once
  worn, because the character screen had no slot for them. They now go in the normal cape slot.

## 2026-09-23 — Kraanhold, a new high-level continent (T4C-0024)

### Added
- A new continent, Kraanhold, has risen across the sea from the mainland — a proper landmass
  with five connected provinces, dirt roads linking them, and its own coastline, built to hold
  the game's highest-level content in one place instead of scattered pockets.
- Windhowl Marches, The Hollow March, Lesser Drake's Aerie, Greater Drake's Bastion, and Drake's
  Lair have all relocated onto Kraanhold, now sitting together as neighboring provinces — open
  centaur marchland at the entrance, a haunted barrow-field at the center, and the mountainous
  Drake ladder (aerie, bastion, lair) rising toward the peaks in the west and east.
- A new dockmaster on the mainland coast now offers passage to Kraanhold: clear out the toll
  troll blocking the crossing and the way across opens up, giving every character a real,
  discoverable path onto the new continent instead of only reaching it by chance.

### Changed
- The Maps page's zone images for Windhowl Marches, The Hollow March, Lesser Drake's Aerie,
  Greater Drake's Bastion, and Drake's Lair now show their new Kraanhold surroundings and
  neighbors instead of their old mainland locations.

## 2026-09-23 — Zone map images on the compendium site (T4C-0023)

### Added
- The reference website now has a "Maps" page: a colored top-down map for every new zone,
  drawn straight from the game's own terrain art, with every NPC and notable monster (bosses
  shown bigger and always labeled, common spawns labeled on hover) pinned at its real
  location, plus nearby landmark names for orientation. Each zone's own page also shows a
  small preview of its map with a link to the full view.

### Fixed
- The "where to find it" listing on item pages could reorder itself on every site refresh with
  no actual data change, since one of the lists it's built from wasn't sorted. Now stable.

## 2026-09-23 — Town-side access quests for Sunken Chancel and Cinderreach Hills (T4C-0022)

### Added
- Two new NPCs, one stationed in Silversky and one in Windhowl, each offering a short new quest
  (clearing troublesome wildlife or bandits on the town's own outskirts) before pointing you
  toward the Sunken Chancel or Cinderreach Hills. Completing either quest unlocks that zone's
  fast-travel entry, the same way the existing in-zone quests already did — so there's now a
  deliberate, town-side way to be sent toward these zones instead of only ever finding them by
  wandering the coast or the hills yourself.

## 2026-09-23 — Spell balance, lost armor recovered, and honest site data (T4C-0021)

### Changed
- The two highest-tier Wisdom-fueled nukes (favored by priest/druid-style builds) were doing far
  less damage for their mana than an Intelligence-fueled mage nuke of a similar level — the
  weaker of the two barely out-hit a nuke over three tiers below it. Both now scale up smoothly
  alongside the mage nukes, with mage spells keeping a slight edge as intended, not a lopsided
  one.

### Fixed
- Ninety-six pieces of high-tier armor (two full sets, across every elemental and warrior/archer
  flavor) had no way to be obtained at all — nothing sold them, nothing dropped them. They now
  drop from eight fitting monsters and bosses across the world.
- The reference website's spell pages always showed "0–0" for damage. They now show a real
  damage figure and the underlying formula, so a spell's power is visible before you spend the
  gold to learn it.
- The reference website's "where to find it" listings for items were missing several real
  sources (some enchanted weapons, a couple of pieces of jewelry) that were sold in-game all
  along — the site just wasn't checking that seller. Fixed, and made more resilient to the same
  kind of miss for future items.

## 2026-09-23 — Changelog writing policy: player-facing, not technical (T4C-0020)

### Changed
- The rules for writing these patch notes now call for plain language throughout: what was
  added, changed, fixed, or removed, described the way a player would talk about it — never the
  technical details of how it was built. No player-visible effect.

## 2026-09-22 — Zone access quests, Avalon dead-end fix, dynamic fast travel (T4C-0019)

_T4C-0018 is reserved by a separate, still-open pass (spell renames/gear); this one branched
before it and claims 0019 to avoid an ID collision on merge._

### Added
- `QuestDef`/`QuestService` gained a real, reusable second objective: a quest can now optionally
  require turning in `requiredItemQty` copies of `requiredItemKey` (consumed on completion) on
  top of its existing kill count, and can set a durable `unlockZoneId` flag
  (`QuestService.zoneUnlockFlag`/`hasUnlockedZone`) when completed. Both are `null`/`0` by
  default so every quest predating this pass is unaffected; backward-compatible 14- and 15-arg
  `QuestDef` constructors are unchanged.
- All 10 of the fork's existing zone quests (Sunken Chancel, Cinderreach Hills, Windhowl
  Marches, Hollow March, Lesser Drake's Aerie, Greater Drake's Bastion, Drake's Lair, Deep Ones
  Cave, Avalon Wilds, Fading Veil) now also require turning in that zone's signature boss-drop
  item, and unlock that zone's fast-travel entry on completion — combining "kill N creatures"
  with "collect a rare item" as requested, without inventing new items where a real drop already
  existed for every one of them.
- **Fixed a real soft-lock, not just new content**: every NPC and shop that could grant access to
  Avalon (the `scroll_of_avalon` consumable, the `AvalonGateway` spell) was itself stationed
  inside Avalon, so a fresh character could never reach it at all. Added a coastal gate zone (The
  Avalon Crossing, mainland shore facing Avalon) with two new monsters (Tideworn Reaver trash,
  Coastwarden Ithrak boss), one new item (`tideworn_avalon_chart`), a new NPC
  (`HarbormasterRangor`), and a new access quest (`passage_to_avalon`) that unlocks fast travel
  into Avalon Sanctuary on completion — the same role the Oracle plays for rebirth access.
- The Locations (fast-travel) panel is now player-aware: `NamedLocation` gained an optional
  `unlockZoneId`, and `NamedLocations.forPlayer(Player)` filters to the original always-available
  landmarks plus any zone whose unlock quest the player has completed. `LocationsScreen` now uses
  `forPlayer` instead of the previous unconditional `all()`. Zone access (and therefore its fast
  travel entry) is quest-flag-based, and `RebirthBehavior.perform()` never clears arbitrary quest
  flags — only specific named ones (level/stats/equipment) — so access is never lost on
  rebirth/remort.

### Fixed (Codex review)
- Reaching a quest's kill count while its item objective was still unmet announced the quest as
  "ready"/reported "0 remaining" with no indication an item was needed at all, so the new
  zone-unlock progression looked silently stuck. `recordKill`'s notification and
  `giveOrReport`'s progress dialog now name the required item and how many of it are held
  whenever the kill count alone isn't enough to turn the quest in.
- A character whose zone quest was already `STATUS_COMPLETED` on an older save (i.e. from before
  this pass added `unlockZoneId`) would never receive that zone's `unlock.zone.*` flag or fast
  travel entry, since only an active quest's turn-in path sets it. `QuestService.hasUnlockedZone`
  now also derives access directly from any matching quest already at `STATUS_COMPLETED`, so a
  prior completion is never permanently missed.

### Tests
- `QuestServiceItemObjectiveTest`: item-gated turn-in, exact-quantity consumption, zone-unlock
  flag setting, kill-only quests unaffected, kills-complete-but-item-missing messaging (both the
  kill notification and the NPC dialog name the still-needed item), and a pre-existing
  `STATUS_COMPLETED` quest still unlocking its zone.
- `NamedLocationsTest`: unconditional locations always visible, zone-gated locations appear only
  after their unlock quest completes, every `NamedLocation.unlockZoneId` matches a real quest and
  vice versa (catches a typo silently hiding a location), and a zone-unlock flag (and its fast
  travel entry) survives `RebirthBehavior.perform()`.

## 2026-09-22 — Spell renames, Apex-tier spells, enchanted gear, and warrior/archer armor fix (T4C-0018)

### Fixed
- Five spells added by earlier passes (T4C-0004/T4C-0009) were literally named after real
  t4cfantasy.com/Addon "Ancient tier" spells (Sentinel, Divine Veil, Clemancy, Undead
  Annihilation, Omega Planetoids) — a fork adding new content shouldn't reuse the real game's own
  spell names. Renamed to invented names with identical mechanics/`spellId`s: Sentinel → Leyward
  Bastion, Divine Veil → Veilstone Aegis, Clemancy → Wellspring Mercy, Undead Annihilation →
  Sunscour, Omega Planetoids → Gravebreaker. Archmage Thalindra and Skywatch Ilvara's dialogue/
  training wiring, and the compendium exporter's allow-list, updated to match.
- `tools/ArmorSetGenerator.java`'s warrior/archer armor flavors (Ancient Celestial and Empyrean
  tiers) were mechanically identical to their mage/elemental siblings — same Armor Class, and the
  same intelligence/wisdom requirement stacked *on top of* the strength/agility gate, so a
  "warrior" set demanded mage stats too. Now genuinely physical-class gear: ~65% of the mage
  tier's AC, no intelligence/wisdom requirement at all, and a flat endurance boost the mage
  flavors don't get, on top of the existing strength/agility + attack/archery skill split. All 24
  warrior/archer armor pieces regenerated; the 72 mage/elemental pieces are unaffected (Empyrean's
  36 elemental files show only cosmetic `boostId` renumbering from the shared counter).
- (Codex review, same pass) `SpellRegistry.findByName` had no alias for the five just-renamed
  spell keys, so a character who'd already learned one under its old name (a real, checked-in
  save has `${spell.divine_veil}` etc. in its spell list) would silently lose it — spellbook
  omits it, casting rejects it as unlearned. Added the five old→new key mappings to the registry's
  existing `canonicalAlias` table (the same mechanism already used for a couple of legacy
  spell-key renames), with a new `SpellRenameBackwardCompatTest` locking in all five.
- (Codex review, same pass) The nine new weapons/shields had no in-game acquisition path at all —
  `price: 0` with no shop listing and no loot entry. Added all nine to `LordoftheShops`'
  `ShopCatalog` list, right after their own line's existing +1/+2/+3 tiers (which are sold the
  same way, also at `price: 0` — an existing, consistent convention for these exact lines, not
  something this pass introduced).

### Added
- Five new "Apex tier" player spells (levels 320-900) filling the gap between the existing
  "Elder tier" (40-260) and the level-1000 curve cap / Avalon boss band (550-650): Voidreave
  Lance (320, dark bolt), Stormcaller's Judgment (480, air AOE), Sanctum Ward (550, defensive
  group ward), Emberqueen's Wrath (650, fire AOE), Cataclysm's Herald (900, water AOE capstone).
  Also taught by Archmage Thalindra at the Avalon Sanctuary Spell Trainer's Tower.
- Six new enchanted weapons extending two existing top-tier legacy lines one/two ranks past their
  previous +3 ceiling: Adamantite Two-Handed Sword +4/+5, Mithril Two-Handed Sword +4/+5, Black
  Locust Composite Bow +4/+5 — each continuing that line's own established damage-dice/flat-bonus/
  skill-boost growth curve, sold by LordoftheShops alongside their line's existing +1/+2/+3 tiers.
- Three new Adamantite Shield tiers (+3/+4/+5) — this codebase had no shield enchant-tier line at
  all before now; adds one on top of the existing unenchanted Adamantite Shield (also sold by
  LordoftheShops), with AC, parry skill, and endurance scaling per tier.

### Notes
- `item/json/ItemJsonDef.java` already supports `dmgFormula`/`atkDelay` pass-through (four
  existing JSON items — `goblin_slayer`, `bow_of_centaur_slaying`, `caradocs_sundered_blade`,
  `ignaroks_emberfang_claw` — already use it) — the `item-creator` skill's documented "JSON
  weapons can't have real damage" limitation is stale and no longer applies; no engine change was
  needed for the new weapons' real, scaled damage.

## 2026-09-22 — XP-per-level chart on the compendium Systems page (T4C-0017)

### Added
- `tools/CompendiumExporter.java` now exports `xpcurve.json` (and bundles it into `data.js` as
  `xpCurve`): every `XpCurveDefinitions` entry (levels 1-1000, `xpToNextLevel`/`totalXp`) plus
  `GameConstants.SERVER_XP_RATE`, the flat multiplier the server applies to monster XP grants
  before it's checked against this curve.
- Compendium Systems page: a log-scale line chart of XP required per level (the curve spans
  100 XP at level 1 to ~4.9T at level 999, so linear would flatten the entire early game to a
  single pixel), with a hover/keyboard crosshair + tooltip (level, XP to next level, total XP
  so far) and a collapsible milestone table for the same data without hovering.

## 2026-09-22 — Fix mislabeled attack "hit chance" on compendium monster pages (T4C-0016)

### Fixed
- The compendium's monster detail pages labeled a monster attack's `value2` field as "hit chance
  %", and never showed `value1` at all. Per `BaseMonster#rollDamage`/`pickAttack`, `value2` is
  only a selection weight used to pick among several eligible attacks at the same range — never a
  to-hit percentage — while `value1` is the real combat-attack/hit-power stat fed into hit
  resolution. Same class of bug Codex flagged in `MonsterBalanceReportGenerator.java` on PR #12
  (T4C-0015); found here independently while cross-checking the live site against that fix.
  `tools/CompendiumExporter.java` now exports `combatAttack` (value1) and `selectionWeight`
  (value2) instead of the old bare `value2`, and `compendium/app.js` displays "attack N" (plus
  "· weight N" only when a monster has more than one attack to choose among).

## 2026-09-22 — Monster balance report generator (T4C-0015)

### Added
- `tools/MonsterBalanceReportGenerator.java`: dumps every registered monster (Java + JSON, 427
  total) with full combat/resist stats to JSON, for auditing new-content bosses against the
  existing monster/level curve. Used to cross-check every new/activated monster's HP, damage,
  and `xpOnDeath` against real legacy monsters at comparable levels and against the live XP
  curve's documented pacing target (~9 kills/level for trash, 3-4 for a boss, accounting for the
  5x server XP rate). Findings (Mordrenn/Ignarok badly XP-overtuned from before the curve
  replacement in T4C-0005; four Avalon monsters badly XP-undertuned) reported separately, not
  yet corrected in this pass.

## 2026-09-22 — Deterministic NPC ordering in compendium exporter (T4C-0014)

### Fixed
- `tools/CompendiumExporter.java`'s NPC export followed `NpcFactoryRegistry.registrations()`'s
  classpath-scan order, which is not guaranteed stable across machines/filesystems — running the
  exporter locally and via the `compendium.yml` CI workflow (T4C-0013) on GitHub's runners
  produced the same NPC records in different orders, showing up as pure-reorder noise in
  `compendium/data/npcs.json`/`data.js` diffs with no actual content change. Now sorted by NPC id
  before export.

## 2026-09-22 — CI auto-regenerates compendium data on push to main (T4C-0013)

### Added
- `.github/workflows/compendium.yml`: on every push to `main`, recompiles, re-runs
  `tools/CompendiumExporter.java`, and pushes a `chore: regenerate compendium data` commit
  straight to `main` if `compendium/data.js`/`compendium/data/*.json` drifted (stat tweaks,
  reworded quest/dialogue text, new loot entries, etc. on already-tracked content). Guards
  against re-triggering itself on its own push via a `[skip compendium]` tag in its commit
  message.

### Notes
- This does not make brand-new content (a new zone/monster/spell/NPC/quest) appear on the
  site automatically — the exporter's "is this new" allow-lists are hand-curated (the game
  data has no `isNew`/rarity field to key off), so adding a genuinely new class still needs a
  source change to `CompendiumExporter.java` (and, for a new zone, a `compendium/data/zones.json`
  row) before this workflow's regeneration has anything new to pick up. See
  `compendium/README.md`'s "Regenerating the data" section for exactly when this workflow
  covers you and when it doesn't.
- Pushes straight to `main` with no review step, per explicit request — if `main` ever gets
  branch protection requiring PRs, this workflow's push will start failing and will need to
  switch to opening a PR instead.

## 2026-09-22 — Local compendium website (T4C-0012)

A static, searchable stat-sheet site (`compendium/`) documenting everything this fork added
on top of the original game — zones, monsters, items, spells, NPCs, quests — styled after
the classic `t4cfantasy.com` stat sheet but modern, cross-linked, and colored by item
rarity/spell element. Opens directly from the filesystem (`compendium/index.html`), no
server or build step required.

### Added
- `tools/CompendiumExporter.java`: a runnable dumper that reads the live game registries
  (`MonsterRegistry`, `SpellDefinitions`, `QuestDefinitions`, `NpcFactoryRegistry`,
  `ShopCatalog`, `assets/items/*.json`) and every i18n string they reference, filters to a
  curated "new since fork" allow-list cross-referenced against `CHANGELOG.md`/
  `docs/content-ideas/`, and writes both per-category JSON files and a single bundled
  `compendium/data.js` (`window.T4C_DATA`).
- `compendium/index.html` + `app.css` + `app.js`: a vanilla-JS, dependency-free, hash-routed
  single-page site — global fuzzy search, sortable/filterable tables for monsters/items/
  spells/NPCs, full monster characteristic pages (stats, resists, attacks, loot tables with
  drop-chance bars), item pages with `boosts[]` decoded into human-readable stat names
  (`statId` reference table) and a derived rarity tier (Legendary/Set/Rare/Common) used for
  color-coding, spell pages with element-colored tags, NPC dialogue-tree pages, and full
  quest walkthrough pages (offer/completion/completed text, objective geofence shown on a
  schematic minimap, rewards).
- `compendium/data/{zones,statids,meta}.json`: hand-curated reference data the exporter
  can't derive from code — zone name/level-range/biome/world-placement/summary (sourced from
  `docs/content-ideas/*.md`'s exact coordinates), the item `statId` → label table (from
  `.claude/skills/item-creator/references/stat-ids.md`), and non-zone systems/economy passes
  plus the armor-set and Colosseum-ladder collections for the Systems page.
- `compendium/README.md`: how to open the site and how to regenerate its data after a new
  content pass.

### Notes
- Scope is new-since-fork content only (10 zones, 33 monsters, 120 items, 10 new spells + the
  full player spellbook for context, 16 NPCs, 10 quests) rather than the entire legacy game
  database — see the README's "Scope" section.
- The Ancient Celestial / Empyrean armor sets (96 of the 120 items) have no shop or monster
  drop source in the current codebase (`price: 0`, no loot-table reference anywhere) — the
  Systems page documents this rather than inventing a fake source.

## 2026-09-21 — MMO server groundwork: Java 21, libGDX purity guard (T4C-0011)

First step of the client/server split. The original T4C was an MMO and this
recreation's single-player-only shape is an artifact of how it was rebuilt,
not a design goal — so the desktop client is being moved toward rendering a
world the server owns. Nothing player-visible changes in this pass.

### Added
- `ServerPurityTest` guards the rule packages the future headless server
  will load (`combat`, `spell`, `item`, `quest`, `skill`, `death`,
  `monster/loot`, `model`) against libGDX references. All 5,794 files across
  them are gdx-free today apart from four presentation classes
  (`CombatGeometry`, `CompanionCastVfxHook`, `NpcCastVfxHook`,
  `TameChannel`), recorded as known violations so the list doubles as the
  remaining client/server split work. A companion test fails once a recorded
  violation is cleaned up, so the list can't rot. A server has no GL
  context, so this class of mistake otherwise surfaces as a crash on a
  headless box rather than a compile error on a developer's desktop.

### Changed
- Toolchain moves to Java 21 LTS (`maven.compiler` 17 → 21, CI JDK 17 → 21).
  The server wants virtual threads for thread-per-connection socket
  handling; bumping the whole project keeps one toolchain rather than
  splitting versions per module.
- `scripts/ci-select-tests.sh` always appends `ServerPurityTest` in scoped
  mode. The guard asserts properties of packages other than its own, and
  `combat`/`skill`/`death` are neither foundational nor
  content-registry-defining — so they scope, and a gdx import added there
  would previously have run only that package's own tests and never been
  checked. Appended after the existing "matched no test class" check so it
  cannot mask that full-suite fallback.

### Notes
- `mvn spotless:apply` currently reformats 526 pre-existing files: CI runs
  compile and test but never `spotless:check`, so formatting has drifted.
  Left alone deliberately rather than bundling an unrelated 526-file diff
  into this pass.

## 2026-09-19 — Smarter CI: skip docs-only changes, scope test runs (T4C-0010)

### Added
- `scripts/ci-select-tests.sh` decides, from the diff between the base and
  head commit, whether CI has anything to do at all (skip), needs to run
  every test (full), or can run a scoped subset (scoped) — runnable and
  testable locally against any two refs.

### Changed
- `.github/workflows/ci.yml`: a docs/process-only change (no `src/`,
  `assets/`, or `pom.xml` touched) now skips compile and test entirely.
  Compile always runs, unconditionally, whenever the job doesn't skip.
  Content-registry-defining packages (`item`/`monster`/`spell`/`npc`/
  `quest`/`tools`), `assets/`, `pom.xml`, foundational packages
  (`helper`/`entity`/`model`/`world`/`mapping`/`config`/`content`/
  `exception`), root-level entry classes, and >6 touched packages at once
  all fall back to the full suite — this codebase has several
  cross-cutting "registry parity" tests (e.g. `SpellRegistryParityTest`,
  `LighthavenSamaritanTest`) that live in a different package than the
  content that can break them, so naive per-package test scoping would let
  those regress silently. A narrower change (e.g. `render`/`audio`/`gui`
  only) now runs only the touched package's own tests.

### Fixed
- Codex's review of this pass caught two real issues before merge, both
  fixed in the same pass rather than as follow-ups: selector outputs
  (including test-file-derived class names, which a PR's own diff
  controls) are now passed through `env:` and expanded as `"$VAR"` instead
  of being interpolated with `${{ }}` directly into `run:` script text —
  the latter is a shell-injection vector, since a maliciously-named test
  file could execute arbitrary commands in the CI runner. A first version
  of the content-package handling above tried a narrower heuristic (only
  pull in tests that import one of the five registry classes) instead of
  a blanket full-suite fallback; that heuristic missed real cross-package
  dependencies with no such import (e.g. a `NpcScripts` change breaking
  `SelfDestructSpellRegistryTest`), so it was dropped in favor of the
  simpler, safer full-suite fallback described above.

## 2026-09-18–19 — Avalon island expansion (T4C-0009)

### Added
- New island **Avalon**, reached via the `AvalonGateway` spell / `scroll_of_avalon`
  consumable — two sub-zones, **The Avalon Wilds** (lush) and **The Fading Veil**
  (corrupted), plus the **Avalon Sanctuary** settlement (temple, inn, weapons
  merchant, scroll/travel merchant, spell trainer's tower).
- Trash monsters: Fey Warden, Moonlit Stalker (lvl 300–420), Veilbound Wraith,
  Sundered Sentinel (lvl 450–600).
- Bosses (lvl 550–650): Sir Caradoc the Sundered Knight, Ysolde the Veiled
  Matriarch, The Verdant Warden — each with guard adds and a unique drop.
- Items: Caradoc's Sundered Blade, Ysolde's Veiled Circlet, Verdant Warden's
  Bulwark.
- NPCs: Elder Ophira (quest-giver), Quartermaster Elenna (weapons shop),
  Wayfarer Bryndis (sells `scroll_of_avalon`), Archmage Thalindra (spell
  trainer), Sister Ilyndra (temple priest).
- Quests: *Avalon Wilds Vigil*, *The Fading Veil's Reckoning* (both from Elder
  Ophira, geofenced kill-counts).

### Changed
- Avalon trash monster spawn density increased ~2.5x (8→20 spawn points per
  type) after the initial pass felt sparse.

## 2026-09-18 — Canon-verified content pass (T4C-0008)

### Added
- Items: Bow of Centaur Slaying, Goblin Slayer, Archdrake's Molten Heart
  (belt), Dragonguard's Scale Bracer, Barnacled Gauntlets, Depths Warden's
  Talisman.
- Monster: Kraanian Dragonguard ("Drake's Lair" — guards Arch Drake).
- Quests: *Drake's Lair Vigil*, *Deep Ones Cave Purge*.
- Spell: Sentinel (level-200 "Ancient tier" group-support).
- Activated pre-existing, previously-unplaced legacy content found already
  authored in the codebase: **Arch Drake** (level 1000, completes the Drake
  ladder), full loot for the existing Deep Ones Cave, and a real "Goblin
  Slayer" bounty on the existing Rhodar Heatforge NPC.

### Fixed
- JSON-authored weapons can now deal real, stat-scaled damage —
  `ItemJsonDef` previously always defaulted `dmgFormula`/`atkDelay` to a flat
  1–4 roll regardless of stats.

## 2026-09-18 — English-only localization pass (T4C-0007)

### Fixed
- Documented the English-only rule in `AGENT.md` (no locale switcher, no
  second language — translate legacy French remnants on sight).
- Translated remaining French strings: `ShopScreen`'s hardcoded error text,
  several `assets/i18n/lang.json` entries, dialogue keywords on 3 NPCs, the
  1650-line `T4C_168_BEHAVIOR_AUDIT.md`, and French test fixtures.

## 2026-09-17 — Spell learnability fix (T4C-0006)

### Fixed
- Spells added in the leveling-overhaul/zone passes were unlearnable via
  `TrainingCatalog`.

## 2026-09-17 — Leveling overhaul + 4 endgame zones, levels 100–500 (T4C-0005)

### Fixed
- XP-carrying fields (`Stats`, `XpCurve.Entry`, save data, death-penalty
  math) widened `int`→`long`, fixing a silent overflow/wraparound in the old
  curve starting at level 541.
- Mirak Nira's "kill 100 goblins" quest was unwinnable — nothing incremented
  its tracking flag. Now increments on any goblin kill.

### Changed
- New `XpCurveHardener` curve (levels 100–1000) with a level-scaling exponent
  that structurally outgrows monster XP awards at every level band, replacing
  the old fixed-exponent `XpCurveExtender` curve.

### Added
- 4 new zones, levels 100→500: **Windhowl Marches** (Centaur Warrior/King),
  **The Hollow March** (Barrow Wight/The Hollow King), **Lesser Drake's
  Aerie** (Kraanian Wyrmling/Lesser Drake), **Greater Drake's Bastion**
  (Bastion Warden/Greater Drake) — two are new monsters, two activate
  fully-stat'd legacy bosses that had never been placed in the world.

## 2026-09-17 — Sunken Chancel & Cinderreach Hills zones (T4C-0004)

### Added
- Zone **The Sunken Chancel** (water/undead, lvl 38–50): Drowned Acolyte,
  Tideclaw Crab, boss Mordrenn the Drowned Inquisitor; items; spells Riptide
  Surge / Drowned Ward; quest *Tide Warden's Plea*.
- Zone **Cinderreach Hills** (fire, lvl 58–70): Cinder Whelp, Ashfang
  Stalker, boss Ignarok the Emberfang; items; spells Cinderburst / Emberheart
  Resolve; quest *The Emberfang Hunt*.

## 2026-09-17 — Content-authoring skills (T4C-0003, process/tooling)

### Added
- `game-director`, `item-creator`, `quest-creator`, `spell-creator`,
  `npc-monster-creator`, `graphic-designer` Claude Code skills for
  structured, cross-checked content authoring going forward.

## 2026-09-17 — Rebirth economy, storage, and quality-of-life pass (T4C-0002)

### Added
- `SpellMerchant` NPC (sells every player-castable spell); `StorageChest`
  NPC at Lighthaven + Windhouse with a full deposit/withdraw UI (categories,
  search, drag-and-drop, quantity picker).
- New spells: Clemancy, Divine Veil, Undead Annihilation, Omega Planetoids.
- New fast-travel destinations: Stonecrest, Tarantula Pond, Skraug Camp,
  Timeprotectors.
- New GM commands: `.rebirth`, `.setpower`.
- New "Elemental Stats" tab on the character sheet; macro keybinds now
  support Ctrl/Shift/Alt modifiers.

### Changed
- Remort points now scale per rebirth (10 + 5/prior remort) instead of a
  flat 10; monster spawn density doubled; server XP rate raised 5x;
  `REBIRTH_MAX_REMORTS` raised to 100, level cap to 700, base light
  resistance to 5000.

### Fixed
- Alphan's post-rebirth teleport (was calling a spell that didn't exist).
- Null-safe regalia check in `RebirthBehavior` that could crash on rebirth.

## 2026-09-16 — JSON content pipeline, armor sets, level cap 500 (T4C-0001)

### Added
- JSON-driven authoring pipeline for monsters/items (`MonsterJsonDef`/
  `MonsterJsonLoader`, `ItemJsonDef`/`ItemJsonLoader`) merged into the
  existing registries — content no longer requires hand-written Java classes.
- Two new 6-piece, 8-flavor armor sets: Ancient Celestial, Empyrean.
- Ring of the Archer.
- Locations fast-travel panel (`Ctrl+L`), open to all players.
- `run.bat` / `run.sh` launcher scripts.

### Changed
- Colosseum arena ladder extended from level 500 to 750.
- Player level cap extended from 200 to 500 (continuing the real XP curve).
