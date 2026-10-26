# Hunting fields and family bosses

Witness Isles and Threnody fields have 100 regular enemies each; mainland and Kraanhold fields retain 40. Training fields also have one larger boss of the same family. All fields appear in Locations; island fields require only the corresponding island unlock. Normal packs refill in 8–12 seconds. Existing wilderness encounters remain. Coordinates are world-0 tiles. Greater Drake (level 500), Kraanian Dragonguard (level 750) are above-cap challenges, not normal leveling farms.

| Field | Coordinates | Population |
|---|---|---|
| Fey Warden | (3901, 1386) | 100 |
| Moonlit Stalker | (3974, 1331) | 100 |
| Veilbound Wraith | (4194, 1523) | 100 |
| Sundered Sentinel | (4374, 1789) | 100 |
| Moonwake Revenant | (3873, 890) | 100 |
| Emberglass Ashguard | (4237, 2545) | 100 |
| Ashbound Exile | (5849, 1313) | 100 |
| Nullguard | (5450, 1674) | 100 |
| Rift Wraith | (5606, 2082) | 100 |
| Barrow Wight | (2583, 2676) | 40 |
| Lesser Drake | (2453, 2853) | 40 |
| Greater Drake | (2735, 2950) | 40 |
| Ashfang Stalker | (1918, 1544) | 40 |
| Cinder Whelp | (1836, 1582) | 40 |
| Centaur Warrior | (2338, 2388) | 40 |
| Deep One | (362, 2197) | 40 |
| Drowned Acolyte | (1703, 2239) | 40 |
| Kraanian Dragonguard | (2844, 2714) | 40 |
| Kraanian Wyrmling | (2332, 2834) | 40 |
| Tideclaw Crab | (1645, 2470) | 40 |
| Tideworn Reaver | (390, 680) | 40 |
| Witness Tarantula | (3805, 1580) | 100 + 1 boss |
| Witness Skraug | (3700, 1580) | 100 + 1 boss |
| Witness Skeleton | (3700, 1450) | 100 + 1 boss |
| Threnody Tarantula | (5385, 1220) | 100 + 1 boss |
| Threnody Skraug | (5280, 1290) | 100 + 1 boss |
| Threnody Skeleton | (5373, 1328) | 100 + 1 boss |

## Training rewards

| Island | Intended AC | Normal HP | Boss HP | Boss gold | Boss XP before personal bonuses | Boss respawn |
|---|---|---|---|---|---|---|
| Witness | 150 | 5,000 | 100,000 | 100,000–125,000 | 10,000,000 | 30 seconds |
| Threnody | 250 | 15,000 | 250,000 | 200,000–250,000 | 167,714,884 | 45 seconds |

Each family boss guarantees its own field’s reusable return scroll. Scrolls are weightless, bound to the backpack, and survive death and rebirth. Duplicate unique drops can be collected; unique marks rarity, not an ownership limit. Bosses are 1.6 times normal size and remain peaceful until challenged. Monster combat XP is multiplied by 2 before the server rate (1x) and personal bonuses. Quest XP is unchanged. Low dodge, low AC and neutral spell resistances support melee, bows and spells. Neither packs nor bosses drop healing potions or mana elixirs.

The reproducible manifest is `assets/maps/worldmap/hunting-grounds.json`. To recreate the layout, run `HuntingGroundsBuilder`, then `TrainingPackBuilder`, then `IslandHuntingExpansion --apply`, then `tools/update_hunting_guide.py`. Builders use native walkability, connected terrain and quest/NPC buffers.
