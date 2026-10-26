# Hunting experience through level 400

Target: 5-10 hours of appropriate solo hunting per 100 levels, including 300-400. This budget uses the runtime monster registry and level requirements. It is not a measured combat simulation; build, hit chance, defenses, grouping and travel determine actual time. No quests, personal XP bonuses or bosses are counted.

The Fantasy addon table is the scale reference: [T4C Fantasy Add-Ons](https://www.t4cfantasy.com/Addon). Our curve extends that scale through our level-400 cap.

## Live calibration

The owner reported going from level 234 to 274 in about ten minutes while killing Fey Wardens and Moonlit Stalkers on 26 October 2026. The kill count and mixture were not recorded. The table below scales that observed pace by each farm's old and current full-kill rewards. It assumes the same kill rate and personal bonuses; it is a conditional projection, not a measured post-change result or a forecast for every later region.

| Farm | Previous full-kill XP | Current full-kill XP | Hours per 100 levels at the observed kill rate |
| --- | ---: | ---: | ---: |
| Fey Warden | 392,310,600 | 22,206,432 | 7.36 |
| Moonlit Stalker | 499,437,808 | 28,313,778 | 7.35 |

## Ordinary hunting rewards

XP includes the combat and server multipliers, before personal bonuses. A full kill includes damage XP for exactly one health bar, without regeneration.

| Monster | Level | HP | Death XP | Full-kill XP |
| --- | ---: | ---: | ---: | ---: |
| Barrow Wight | 165 | 12,050 | 1,675,670 | 1,892,570 |
| Kraanian Wyrmling | 220 | 20,300 | 12,841,034 | 13,247,034 |
| Fey Warden | 210 | 36,300 | 21,770,832 | 22,206,432 |
| Moonwake Revenant | 230 | 43,000 | 28,029,628 | 28,459,628 |
| Veilbound Wraith | 255 | 53,400 | 36,663,010 | 37,517,410 |
| Sundered Sentinel | 275 | 61,600 | 45,629,628 | 46,738,428 |
| Ashbound Exile | 315 | 72,000 | 59,746,834 | 60,466,834 |
| Nullguard | 355 | 94,000 | 85,363,246 | 86,303,246 |
| Rift Wraith | 390 | 130,000 | 131,313,130 | 132,613,130 |

## Dense-pack survivability

The owner reported 12-14 simultaneous attackers and survival dependent on a fifty-rebirth healing aura. A live Veilbound Wraith log on 26 October 2026 contained 14,586 post-mitigation damage in one two-second window against 10,465 HP. The earlier XP calibration did not establish survivability. Preserve dense fields while reducing ordinary melee outliers.

The runtime Wraith roll is now 296-638 raw damage. With the separately reported 9,000 HP / 432 AC baseline, fourteen Wraiths landing every attack at the unchanged 1.5-second cooldown deal about 580 average damage per second before any healing. That is 15.5 seconds of mean HP drain, excluding the initial volley's timing, misses, kills, movement and other defenses. Two complete maximum-damage volleys deal at most 5,768 damage. This is an analytical reaction-window benchmark, not a measured fight or a promise to tank indefinitely.

| Rebirths | Healing trigger chance per landed hit |
| --- | ---: |
| 10 | 14.9% |
| 15 | 19.8% |
| 50 | 54.5% |

These are trigger odds only; healing amount, stats and overhealing also differ between builds. The fifty-rebirth aura is not the ordinary-farm baseline.

## Progression budget

Each level starts at zero progress and rounds its kills up independently; real XP overflow carries into the next level, so this slightly overstates kills. The sustained DPS ranges below divide total HP by the target session time. Damage is summed across all targets and the whole session, including movement and downtime. No arbitrary pack-size assumption is used. These are planning requirements, not measured build performance; the live observation above is the calibration evidence.

| Levels | Kills | HP to defeat | Sustained DPS for 10 hours | Sustained DPS for 5 hours |
| --- | ---: | ---: | ---: | ---: |
| 150-200 | 29,708 | 493,743,400 | 27,430 | 54,860 |
| 200-300 | 56,718 | 2,394,957,000 | 66,527 | 133,053 |
| 300-400 | 11,435 | 1,004,990,000 | 27,916 | 55,833 |
