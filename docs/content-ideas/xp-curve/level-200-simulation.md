# Level 200 → 201: Drake’s Lair combat simulation

Offline model only; no game changes. Uses the proposed 19,710,821,719-XP level cost at 1× XP, with 1,000 seeded trials per scenario.

Highest spawned XP targets are Arch Drake (2.1B XP, 206,623 HP, level 1000) and Kraanian Dragonguard (1.19B XP, 130,000 HP, level 750). The dense Dragonguard field is at 2844,2714; the Arch Drake anchor is at 2850,2780, world 0.

The currently installed Ultimate bow resolves to `1d401+399+3*arrow_dmg` (400–800 base). A second model substitutes the earlier requested `1d154+396+3*arrow_dmg` (397–550). Both retain the installed Archery bonus and 750 ms interval.

- Level 200, 50 rebirths, STR 270 / AGI 1000 / END 550 / INT 270 / WIS 270
- Current installed bow, Worldbreaker Quiver and mixed offensive armor/accessories; 100 Armor Penetration, Powerful Blow and Stun Blow
- CombatResolver handles accuracy, armor, penetration, powerful hits and stuns; quiver splash uses the installed percentage
- 80% attack uptime plus 8 seconds per pull; solo player receives full kill XP; no XP buffs, party sharing or quest rewards
- Successful continuous ranged kiting assumed: no deaths, incoming-hit delays, regeneration, Seraph aura damage/healing, or defensive spells
- Arch Drake has two nominal spawn copies with 30s respawn; kill time exceeds respawn so alternating copies causes no idle time
- Dragonguard field has 40 dense anchors and 8–12s refill; 17 kills fit initial population; 4/8-target packs assume targets stay inside splash radius and LOS
- Monte Carlo variation covers combat rolls only; it does not measure navigation, pulling feasibility, survivability or player mistakes

| Weapon damage | Target | Base Archery | Pulled targets | Mean minutes | 10th–90th percentile | Hit rate |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| 400–800 installed | Arch Drake | 1,000 | 1 | 89.5 | 87.5–91.5 min | 35.9% |
| 400–800 installed | Kraanian Dragonguard | 1,000 | 1 | 94.7 | 92.7–96.5 min | 48.6% |
| 400–800 installed | Kraanian Dragonguard | 1,000 | 4 | 50.1 | 48.6–51.5 min | 48.6% |
| 400–800 installed | Kraanian Dragonguard | 1,000 | 8 | 29.8 | 28.7–30.9 min | 48.6% |
| 400–800 installed | Arch Drake | 2,000 | 1 | 59.5 | 58.3–60.7 min | 54.4% |
| 400–800 installed | Kraanian Dragonguard | 2,000 | 1 | 69.3 | 68.2–70.5 min | 66.9% |
| 400–800 installed | Kraanian Dragonguard | 2,000 | 4 | 36.5 | 35.7–37.4 min | 66.9% |
| 400–800 installed | Kraanian Dragonguard | 2,000 | 8 | 21.8 | 21.1–22.4 min | 66.9% |
| 397–550 earlier request | Arch Drake | 1,000 | 1 | 104.2 | 102.0–106.5 min | 35.9% |
| 397–550 earlier request | Kraanian Dragonguard | 1,000 | 1 | 116.0 | 113.8–118.1 min | 48.6% |
| 397–550 earlier request | Kraanian Dragonguard | 1,000 | 4 | 61.5 | 59.9–63.1 min | 48.6% |
| 397–550 earlier request | Kraanian Dragonguard | 1,000 | 8 | 36.6 | 35.4–37.9 min | 48.6% |
| 397–550 earlier request | Arch Drake | 2,000 | 1 | 69.3 | 68.1–70.5 min | 54.3% |
| 397–550 earlier request | Kraanian Dragonguard | 2,000 | 1 | 84.8 | 83.5–86.2 min | 66.9% |
| 397–550 earlier request | Kraanian Dragonguard | 2,000 | 4 | 44.8 | 43.8–45.8 min | 66.9% |
| 397–550 earlier request | Kraanian Dragonguard | 2,000 | 8 | 26.7 | 25.9–27.5 min | 66.8% |

Combat randomness accounts for only a small part of real uncertainty. Maintaining a tight pack and surviving level-750/1000 monsters are assumed, not verified. The eight-target case is an optimistic farming benchmark, and these results do not describe a typical freshly leveled character.

Reproduce by compiling ArcherLevelingSimulation.java against target/classes and target/classpath.txt, then running it from the repository root. It reads live definitions and rewrites the JSON result alongside this report.
