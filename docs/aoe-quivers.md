# AoE quivers

Ten reusable quivers occupy the off-hand slot. They require strength and agility,
with no minimum character level. Fali sells the first three tiers, Rawlin the
first six, and Quartermaster Elenna at Avalon Sanctuary (4068, 1510) sells all ten.

Every successful damaging bow hit creates one small impact flash and six sparks.
Nearby eligible monsters receive a percentage of the primary hit's damage.
The total target limit includes the primary monster and never exceeds eight.
Misses and zero damage do not trigger a burst. Secondary targets must be within
the quiver's radius, have line of sight from the impact and be attackable outside
sanctuaries. Invulnerable monsters are excluded. Bursts do not chain or repeat
on-hit effects. Normal death, XP, loot and kill tracking apply to secondary kills.

The bow's existing `arrow_dmg` formula now receives damage rolled from the
equipped quiver. The quiver and arrow damage are captured when the shot launches.

The [local preview](http://127.0.0.1:8767/quivers.html) lists each tier's attributes,
arrow damage, radius, splash percentage and total target limit. For example,
`.summon item item.flintspark_quiver` summons the first tier; use
`item.worldbreaker_quiver` for the final tier.

Strength requirements follow ordinary bow progression at comparable agility. The
650-agility tier interpolates between Black Locust Composite (590 AGI / 138 STR)
and Tension Primordiale (690 AGI / 165 STR). The 800- and 1,000-agility tiers
extend that curve, rounded to the nearest five strength points. Specialty bows
with no strength requirement do not define this progression.

| Quiver | Agility | Strength |
| --- | ---: | ---: |
| Flintspark Quiver | 15 | 13 |
| Cinderburst Quiver | 35 | 17 |
| Splinterstorm Quiver | 70 | 18 |
| Thunderburst Quiver | 140 | 27 |
| Frostshatter Quiver | 240 | 42 |
| Faultline Quiver | 360 | 80 |
| Stormfront Quiver | 500 | 90 |
| Starfall Quiver | 650 | 155 |
| Dawnstorm Quiver | 800 | 195 |
| Worldbreaker Quiver | 1000 | 250 |
