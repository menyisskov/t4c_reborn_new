# The Hollow Dawn campaign

## Story

Avalon's fey pact was one part of an older prison. Rhunor, an ancient god of unremembering,
breaks a people by erasing their names and the bonds between them before conquering their land.
The original warders bound him with three bells and a mortal oath: living witnesses had to keep
one another's names alive while the bells held the gate. Ysolde's Veil, Caradoc's binding, and
the disturbed fey are early symptoms of the prison failing.

Chronicler Maelin preserves the names returned by Avalon’s first two quests. Her accounts send
players to Moonwake Shoals and Emberglass Crown, where Rhunor's Pale Cantor and Cinder Marshal
have broken two bells. The Marshal's defeat reveals Threnody Reach, the third ward island. Keeper
Vael waits there in a protected camp, guides the player past a second cantor and the Dusk Regent,
then reveals the isolated inner court. Rhunor is fought there at the end of the chain. The story
draws only on broad epic fantasy themes of companions, ancient war, and a long quest; its lore,
names, geography, and events are original to this game.

## World layout and progression

The world is **6144×3072** tiles. Existing terrain, caves, stairs, spawns, and coordinates in
the original 5120 columns remain in place. Threnody Reach occupies the newly added eastern
columns, roughly X 5160–6100 and Y 850–2550. It is a separate land beyond Avalon, with a
protected northern arrival camp at **(5505,1150)**, a northern bell court near **(5700,1440)**,
a middle watch near **(5480,1700)**, a southern regent court near **(5800,1970)**, and an outer
rift anchor near **(5640,2120)**. Water isolates the final arena at **(5650,2290)**. The campaign
unlocks the first fast travel destination after the Cinder Marshal and the inner court after the
rift wraith objective. Both are saved as durable zone flags.

| Levels | Area | Story steps | Enemies |
| --- | --- | --- | --- |
| 200–240 | Avalon Wilds, Veil, Moonwake | Ophira's two opening quests, missing names, first bell | Fey Wardens, Stalkers, Wraiths, Moonwake Revenants, Pale Cantor |
| 240–300 | Emberglass Crown | Ashguard oath, Cinder Marshal, passage to the Reach | Emberglass Ashguards, Cinder Marshal |
| 300–340 | Threnody north | Exiles' march, silenced warning | Ashbound Exiles, Hush Cantor |
| 340–375 | Threnody middle and south | Nullguard watch, last witness | Nullguard, Dusk Regent |
| 375–400 | Outer anchor and inner court | Rift unbinding, Rhunor | Rift Wraiths, Rhunor |

Quest stages have separate accepted, kill-progress, and completed flags. The Pale Cantor and
Dusk Regent each drop a quest relic reliably; their chapters require both the kill and the
relic, which is consumed at report. Maelin is gated on
Ophira's completed Veil quest, and Vael is gated on Maelin's Cinder Marshal quest. Each witness
offers the first incomplete stage. `story` explains it, `accept` starts it, `report` checks both
the objective and minimum level, and `route` gives coordinates. Old completed Avalon quest flags
remain valid. The old 2% blade and circlet drops remain optional loot rather than mandatory quest
proof. Players who already completed those quests keep their progress and unlocks.

## XP budget

The current XP curve needs about **1.10 billion** XP from level 200 to 300. The Avalon opening
and Maelin's four new turn-ins grant **218 million** XP total. Their mandatory kill objectives,
boss kills, and ordinary travel fights cover part of the remaining budget; roughly several
hundred additional ordinary kills remain, depending on level and chosen hunting ground. At an
illustrative sustained rate of about half to one kill per minute, plus dialogue, travel, and
boss attempts, the intended route falls near the requested **10–30 hours**. This is a design
model, not a measured playtest. Existing repeatable content, XP bonuses, group play, and
high-value side quests can change the elapsed time. The 300–400 curve needs substantially more
XP and has no time target from the owner yet.

## Content follow-up

The ten-stage main chain and tiered encounters are implemented. Later passes can add side
characters, deliveries, branching decisions, dungeon interiors, unique lieutenant drops,
party mechanics, cinematic effects, and a playtested tuning pass. No copied Dragonlance names,
characters, or plot are used.
