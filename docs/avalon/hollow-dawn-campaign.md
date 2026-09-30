# The Hollow Dawn campaign

## Premise

The Witness Isles (the region formerly called Avalon) were part of a prison for Rhunor, an
ancient god who conquers by erasing names and memories. Three bells and a mortal oath held him.
The failing pact, Caradoc's binding, Ysolde's Veil, and the unsettled fey are warnings that the
prison is opening. The Dusk Regent, a demigod made from a mortal warder and a shard of Rhunor,
commands the Pale Cantor, Cinder Marshal, and Hush Cantor. Their revenants, Ashguards, exiles,
Nullguards, and rift wraiths prepare the way for him.

These characters, names, locations, and events are original to this game. The story uses broad
mythic themes of memory, loyalty, and an old evil returning.

## Player route and cast

The optional mainland investigation in [The Names That Remain](the-names-that-remain.md)
connects Kilhiam and Lantalir on Arakas, the two Great Library historians on Raven's Dust,
and Rangor on Stoneheim. Its evidence flags provide context and a repeatable `story` route;
they do not replace classic quests or revoke existing passage. The island route begins with
Harbormaster Rangor in **Stonecrest, Stoneheim (180,740)**. His `isles`,
`scouts`, `report`, and `chart` topics lead through Tideworn scouts and the chart from
Coastwarden Ithrak. The passage quest unlocks travel to the Witness Isles. That unlock and the
later Threnody destinations survive rebirth. Existing quest IDs and completed saves remain
valid despite the player-facing region rename.

| Character | Role and conversation path |
| --- | --- |
| Elder Ophira | Sanctuary guide. `wilds`/`accept`/`report`, then `veil`/`accept`/`report` introduce the failing pact. `route` points to the current objective. |
| Chronicler Maelin | Records the returned names. `witnesses` and `warders` explain the two accounts; `moonwake` or `emberglass` commits the route. `story`, `route`, `accept`, and `report` advance its chapters. `hierarchy` explains Rhunor's command. |
| Ilyra, Moonwake Witness | After Maelin's revenant deed, `testimony` then `clue` reveals the Pale Cantor's Bell Shard. The clue is saved before Maelin offers the lieutenant. |
| Soren, Emberglass Warder | After Maelin's Ashguard deed, `testimony` then `clue` reveals the Cinder Marshal's betrayal and chart. The clue is saved before Maelin offers the lieutenant. |
| Keeper Vael | Guides the shared Threnody chapters. `story`, `accept`, `route`, and `report` connect every objective and next destination. `hierarchy` recalls the enemy structure. |

Players choose **one of two outer accounts**:

- **Moonwake witnesses:** defeat revenants, speak with Ilyra, defeat the Pale Cantor, and bring
  her guaranteed Bell Shard to Maelin.
- **Emberglass warders:** defeat Ashguards, speak with Soren, defeat the Cinder Marshal, and
  report to Maelin. The Marshal's chart exposes the same route to Threnody.

Either lieutenant opens Threnody Reach. Vael then guides six shared stages: the Ashbound Exiles,
Hush Cantor, Nullguard, Dusk Regent, rift wraiths, and Rhunor. The Regent's guaranteed Last
Witness Seal is required for his turn-in. Each active stage waits for its kill objective,
required item where applicable, and stated minimum level before `report` advances the story.
NPC dialogue names the next speaker, keyword, and destination; players need no external guide.
Old active or completed branch quests remain playable, even without the newly added witness
clue flags.

## Lands, levels, and hunting

The world is **6144 by 3072** tiles. Original terrain, cave and stair coordinates stay in place.
The Witness Isles occupy the separate eastern region; Threnody Reach lies in the added columns,
roughly X 5160-6100 and Y 850-2550. Its protected arrival camp is at **(5505,1150)**. The
Hollow Dawn inner court is unlocked separately after the rift objective.

| Levels | Hunting areas | Ordinary enemies and story encounter |
| --- | --- | --- |
| 200-240 | Wilds, Veil, Moonwake Shoals | Fey Wardens, Stalkers, wraiths, Moonwake Revenants; Pale Cantor |
| 240-300 | Emberglass Crown and outer Isles | Emberglass Ashguards; Cinder Marshal |
| 300-340 | Threnody northern court | Ashbound Exiles; Hush Cantor |
| 340-375 | Threnody middle and southern courts | Nullguard; Dusk Regent |
| 375-400 | Rift anchors and inner court | Rift Wraiths; Rhunor |

Ordinary campaign monsters have multiple spread spawn points. Local hunting packs permit up to
eight live monsters of the same type within twelve tiles, with staggered short respawns. Leaving
and re-entering a visible hunting area can refill dead spawns sooner. Story bosses keep their
separate, slower behavior. These are initial settings for one or two players; XP per hour and
the requested **10-30 hours from level 200 to 300** still need an in-game playtest.

## Equipment hunt

Fifty-one new droppable items cover six elemental colors: fire (red), water (blue), air,
earth, light, and dark. Each family includes a robe, plate set, wings, ring, bracelet, amulet,
tiara, and weapon; selected families add a mace, staff, or dagger. Their attribute requirements
and bonuses offer different physical and caster build choices across the two lands. Ordinary
monsters supply accessories; bosses supply armor and weapons. All have resale values above one
gold, with rarity used by the game's pricing system. Robes and wings use the shipped palette
variants for visible colors. The shipped plate sprite pack has no matching colored variants,
so plate families currently share the existing plate artwork; distinct plate art remains an
asset task.

## Follow-up

The quest and hunting route is implemented. A live multiplayer playtest should tune XP per hour,
drop frequency, monster density and path safety. Additional side characters, deliveries,
dungeon interiors, and unique boss mechanics can deepen the two accounts without breaking the
main route or saved unlocks.
