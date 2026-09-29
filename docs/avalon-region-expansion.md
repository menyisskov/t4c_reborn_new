# Avalon region expansion

This document records the earlier two-island terrain pass on the 5120×3072 world. The later
[Hollow Dawn campaign](avalon/hollow-dawn-campaign.md) expands the world to 6144×3072 and adds
Threnody Reach with encounters and story quests.

## Reference and direction

The [Realmud map index](https://www.t4c.com/cms/rmmaps.html) shows how a large outdoor region gains depth through distinct caves, temples, towns, and smaller destinations. The [T4C Fantasy map collection](https://www.t4cfantasy.com/Maps) uses a mainland with several routes on its [Avalon map](https://www.t4cfantasy.com/images/maps/i4Avalon/Avalon.png), connected water precincts on its [Atlantis map](https://www.t4cfantasy.com/images/maps/i5-7Atlantis/Atlantis.png), and a long route through several hubs on its [Oblivion map](https://www.t4cfantasy.com/images/maps/i8Oblivion/Oblivion.png). Those are spatial references only. This region uses original place names and does not import their characters, quests, or location names.

The expansion uses the existing 5120×3072 world map. Two new islands extend Avalon north and south. Both have a narrow walkable causeway from the existing eastern land, so their terrain can be explored before quests and travel services are added. Three mainland landmarks give the previously open grass and wetland distinct destinations. Their open centers leave room for future encounter and quest design.

| Area | Approximate footprint (tiles) | Center (X, Y) | Terrain and landmarks |
| --- | --- | --- | --- |
| **Moonwake Shoals** | 710×455 | 3930, 715 | Grass and stone shore, lagoon, temple court, harbor cottage, northern watch house; causeway from 3800, 1130. |
| **Emberglass Crown** | 820×560 | 4320, 2600 | Dark rock, crater pool, southern green pocket, stone ring and two small buildings; causeway from 4300, 2220. |
| **Crescent Pools** | 110×110 | 3610, 1320 | Western Avalon clearing, two shallow-looking water pools, a cottage and an approach path. The water tiles are impassable. |
| **Oathstone Grove** | 110×110 | 3820, 1430 | Forest clearing with an open standing-stone circle and a route toward the Wilds. |
| **Sable Fen** | 116×116 | 4450, 2050 | Southern wetland clearing, pools, storehouse and an approach path. |

Other prepared clearings on the islands are at **(4050, 675)**, **(4080, 2530)**, and **(4460, 2670)**. These can support future quests or encounters without covering the travel routes with scenery.

## Scope and map safety

- The world dimensions, origin, layer IDs, existing travel endpoints, cave entrances, monster and NPC spawns remain at their coordinates. No new boss, spawn, quest, functional chest, teleport, or stair is registered in this terrain pass.
- New islands are painted only over untouched ocean; existing Avalon decor and special collision tiles are left in place. The current handmade scenery at roughly (3790–3818, 1228–1265) was preserved in the shipped map.
- Buildings reuse complete scenery templates with scale, offsets and draw order. Template stair artwork is omitted; blocking walls keep their collision, and no safe haven is added unintentionally.
- Walkability is checked from the sanctuary arrival point to every new site and both causeways. The actual world overview and rendered ground/decor scenes are also inspected.
- The terrain can be edited normally in the map editor. `AvalonRegionBuilder` is a one-time, dry-run-first authoring tool for this exact pre-expansion map state; reapplying it to an already expanded or manually edited island is deliberately rejected by its untouched-ocean checks.
