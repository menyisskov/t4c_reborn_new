# The Names We Choose

An eight-chapter playable campaign across the detailed Witness Isles and Threnody Reach. Start with **Archivist Aveline at (4044, 1480)** in the Witness sanctuary. Ask **"story"**, then **"accept"**. The Celestial Chronicler nearby points players to her.

Rhunor feeds on erased identities. The Concord's winged gods can witness an oath, but cannot decide whose life deserves to be remembered. The castles hold evidence that the erasure began with mortal rulers, before the supernatural invasion. Recovering that history gradually changes the player's task from investigating a disappearance to confronting the order that caused it.

This route does not require the legacy Hollow Dawn monster quotas. Existing quests, drops, god encounters and saved progress remain available. The new route starts after reaching the Witness Isles; its checkpoint levels progress from 200 to 395. Players can gather evidence below a checkpoint's level, but must meet its level before claiming the reward.

| Chapter | Play and story | Guaranteed equipment | Gold / base XP | Level |
|---|---|---|---|---|
| 1. The Empty Charter | Explore Oathwood Castle. Compare a damaged ledger and an oath's echo to recover erased households. | Ring of Remembered Names | 150,000 / 30m | 200 |
| 2. The Bell That Saved Them | At Moonwake Castle, distinguish an evacuation warning from a falsified summons to war. | Bracelet of the Sheltering Tide | 300,000 / 50m | 230 |
| 3. A Charter for the Living | Confront Emberglass's contradictory account and obtain a signed confession. Discover the Regent ordered the original erasure. | Choose a tier I Witness weapon | 450,000 / 75m | 260 |
| 4. No Names for the Regent | Hear Neris's refugees in Threnody camp. Choose a sealed protective archive or public testimony from consenting survivors. Nobody is handed over. | Necklace of the Free Witness | 600,000 / 100m | 300 |
| 5. The Unfinished Chord | Reconstruct and sound a three-note oath at Bellward Castle. Read both clues, then speak the words in order; a wrong note restarts the sequence. | Mantle of the Unfinished Chord | 750,000 / 150m | 330 |
| 6. The Mortal Behind the Crown | Defeat the Dusk Regent once during the accepted chapter. His broken oath releases the bells; Rhunor begins to wake. | Choose a tier II Oathbreaker weapon | 900,000 / 200m | 375 |
| 7. Three Voices at Dawn | Revisit Moonwake, Emberglass and Bellward in any order. Each has new evidence and a different free oath. All three must answer before the seal forms. | Signet of Three Free Voices | 1,050,000 / 175m | 390 |
| 8. The Names We Choose | Enter the opened Hollow Dawn and defeat Rhunor once. Choose to restore the dead names to the public record or release the imprisoned echoes. | Choose a tier III Dawnkeeper weapon | 1,200,000 / 250m | 395 |

Chapter 3 unlocks **Threnody Reach**, including Aveline's **"passage"** shortcut. Chapter 7 unlocks the physically isolated **Hollow Dawn** arena through named travel. Neris offers **"return"** to the sanctuary. Other sites are reached by exploration and existing travel landmarks.

The full campaign awards **5.4 million gold, 1.03 billion base XP and eight equipment items**. XP follows the server rate (currently 1x, making the full payout 1.03 billion before the cap), and stops accumulating at level 400. The final turn-in requires level 395 so its XP can contribute before the cap. Every checkpoint lets the player choose warrior, archer, fire, water, earth, air, light or dark. The chosen style can change between checkpoints. Accessories and mantles come in eight class-specific variants, following the existing attribute, endurance, armor and bonus budgets. Weapons use progressively stronger attributes, school power or attack/archery, requirements and damage; they reuse shipped native equipment art. There are 64 equipment definitions, from which each player earns eight items. Rewards never include recovery potions or mana elixirs.

## Ending and decisions

The refugee decision is recorded when its evidence is first resolved. It determines whether the epilogue describes a protected archive or a public council founded on voluntary testimony. Reclaiming lost evidence cannot rewrite that decision.

After Rhunor falls, **"restore"** gives the dead a public memorial, with the grief and accountability that brings. **"release"** lets the trapped dead rest while living witnesses choose how to remember them. Both endings have equal rewards. The choice can be considered again until the final report; after completion it is permanent.

The campaign is a climax followed by an epilogue, not an endless bounty. Its only mandatory combat objectives are one Regent victory and one Rhunor victory while the corresponding chapter is active. Normal enemies along castle approaches remain environmental threats.

## Implementation and integrity

- `ConcordCampaign` defines independent stable `concord_*` quest IDs and journal chain metadata. Legacy `HollowDawnCampaign` IDs and statuses are preserved.
- `ConcordStoryNpc` handles acceptance, reports, personal evidence, ordered notes, decisions, equipment selection and story travel. Six registered stationary NPC classes place these interactions on walkable, connected castle gates and protected hubs.
- Evidence requires both the local record and echo, an accepted current chapter, the correct response and physical proximity in world 0. State is stored in player quest flags and saved immediately, including partial chords and individual ritual bells.
- `ConcordEvidence` registers six native non-equippable story materials. Evidence is bound and can be replaced at its original witness if lost. Replacing it never pays a completed quest again. NPC instances do not store player-specific puzzle state.
- Every report checks native quest level/objective readiness. Item capacity is checked before evidence consumption, XP, gold, unlocks or completion. Weapon choices replace the normal item reward while preserving the chapter's gold/XP payout.
- The journal displays evidence objectives rather than a misleading `0/0` monster counter, shows eight-stage progression, and exposes equipment rewards through its item tooltip.
- `tools/generate_concord_assets.py` authors the English catalogue, NPC declarations and JSON equipment. Run targeted Java formatting after regenerating. The tool reserves item IDs 59000 onward and boost IDs 61000 onward and checks the JSON equipment registry for collisions. All equipment follows `ItemBalance`; evidence stays outside the equipment catalogue.

`ConcordCampaignTest` covers the complete route, reward replay protection, legacy progress preservation, local clue requirements, player/NPC reload state, evidence recovery, blocked rewards and level gates, the ordered chord, gated travel, and NPC-to-map/quest/item joins. Existing quest-service and Hollow Dawn tests continue to cover the underlying quest contracts.

Validation on October 24, 2026: the full Maven suite passed **616 tests, zero failures or errors**. Validation used a separate build directory to avoid concurrent UI builds clearing shared class files; the source, dependencies and test configuration were unchanged. The log is `target/concord-validation-tests.log`.

After increasing XP rewards and lowering the finale's turn-in floor, all **nine campaign and Hollow Dawn integration tests passed**. The complete-route test now checks XP totals beyond the 32-bit range and earning the final reward below the cap, reaching level 400. See `target/concord-xp-tests.log`.
