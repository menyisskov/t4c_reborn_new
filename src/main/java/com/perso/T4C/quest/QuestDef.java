package com.perso.T4C.quest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class QuestDef {
  private final String id;
  private final String title;
  private final String giverNpc;
  private final String targetMonster;
  private final int requiredKills;
  private final int targetWorldZ;
  private final int areaCenterX;
  private final int areaCenterY;
  private final int areaRadiusTiles;
  private final int rewardGold;
  private final int rewardXp;
  private final String offerText;
  private final String completionText;
  private final String completedText;
  private final String activationFlag;
  // T4C-0019: an optional second objective (collect N of an item, consumed on turn-in) and an
  // optional durable "zone unlock" flag set on completion - see QuestService.hasRequiredItem()/
  // complete() and NamedLocations' unlock-flag gating. null/0 preserve the original
  // kill-count-only quest shape used by every quest predating this pass.
  private final String requiredItemKey;
  private final int requiredItemQty;
  private final String unlockZoneId;
  // T4C-0033: an optional item granted on completion, in addition to rewardGold/rewardXp - see
  // QuestService.complete(). Used by the Godsforged crafting chain (quest/definition/Godsforged*)
  // so a crafting NPC's turn-in quest can hand back the crafted item itself, not just currency.
  // null preserves every quest shape predating this pass (gold/XP only).
  private final String rewardItemKey;
  // T4C-0035: an optional character-level floor a player must meet before this quest can be
  // TURNED IN (not merely accepted) - see QuestService.turnInReadyQuests()/completeCraftingQuest().
  // 0 preserves every quest shape predating this pass (no level gate at all). Kills/items can
  // still be gathered below the floor; only the final completion is blocked.
  private final int minLevel;
  // T4C-0052: an optional longer-form prose walkthrough for the compendium website only (see
  // CompendiumExporter.exportQuests(), compendium/app.js's quests/:id route) - the in-game
  // offer/completion/completed text stays short by design (see quest-creator skill), so this is
  // a separate field rather than lengthening those. null means the compendium falls back to its
  // existing short-text-only rendering, which every quest predating this pass keeps doing.
  private final String walkthroughText;
  // T4C-0068: an optional spell granted on completion, in addition to rewardGold/rewardXp - see
  // QuestService.complete(). Resolved via SpellRegistry.findByName() and added to the player's
  // known spells (player.getSpells()), same mechanical shape as rewardItemKey above but for a
  // spell instead of an item. null preserves every quest shape predating this pass.
  private final String rewardSpellKey;

  /** Original 14-arg shape (no activationFlag, no item/unlock objective, no item reward). */
  public QuestDef(
      String id,
      String title,
      String giverNpc,
      String targetMonster,
      int requiredKills,
      int targetWorldZ,
      int areaCenterX,
      int areaCenterY,
      int areaRadiusTiles,
      int rewardGold,
      int rewardXp,
      String offerText,
      String completionText,
      String completedText) {
    this(
        id,
        title,
        giverNpc,
        targetMonster,
        requiredKills,
        targetWorldZ,
        areaCenterX,
        areaCenterY,
        areaRadiusTiles,
        rewardGold,
        rewardXp,
        offerText,
        completionText,
        completedText,
        null,
        null,
        0,
        null,
        null,
        0);
  }

  /** Pre-T4C-0019 15-arg shape (activationFlag, no item/unlock objective, no item reward) - every
   * quest added before T4C-0019 uses this constructor unchanged. */
  public QuestDef(
      String id,
      String title,
      String giverNpc,
      String targetMonster,
      int requiredKills,
      int targetWorldZ,
      int areaCenterX,
      int areaCenterY,
      int areaRadiusTiles,
      int rewardGold,
      int rewardXp,
      String offerText,
      String completionText,
      String completedText,
      String activationFlag) {
    this(
        id,
        title,
        giverNpc,
        targetMonster,
        requiredKills,
        targetWorldZ,
        areaCenterX,
        areaCenterY,
        areaRadiusTiles,
        rewardGold,
        rewardXp,
        offerText,
        completionText,
        completedText,
        activationFlag,
        null,
        0,
        null,
        null,
        0);
  }

  /** Pre-T4C-0033 18-arg shape (item/unlock objective, no item reward) - every quest added by
   * T4C-0019 through T4C-0032 uses this constructor unchanged. */
  public QuestDef(
      String id,
      String title,
      String giverNpc,
      String targetMonster,
      int requiredKills,
      int targetWorldZ,
      int areaCenterX,
      int areaCenterY,
      int areaRadiusTiles,
      int rewardGold,
      int rewardXp,
      String offerText,
      String completionText,
      String completedText,
      String activationFlag,
      String requiredItemKey,
      int requiredItemQty,
      String unlockZoneId) {
    this(
        id,
        title,
        giverNpc,
        targetMonster,
        requiredKills,
        targetWorldZ,
        areaCenterX,
        areaCenterY,
        areaRadiusTiles,
        rewardGold,
        rewardXp,
        offerText,
        completionText,
        completedText,
        activationFlag,
        requiredItemKey,
        requiredItemQty,
        unlockZoneId,
        null,
        0);
  }

  /** Pre-T4C-0035 19-arg shape (item reward, no level gate) - every quest added by T4C-0033
   * through T4C-0034 uses this constructor unchanged. */
  public QuestDef(
      String id,
      String title,
      String giverNpc,
      String targetMonster,
      int requiredKills,
      int targetWorldZ,
      int areaCenterX,
      int areaCenterY,
      int areaRadiusTiles,
      int rewardGold,
      int rewardXp,
      String offerText,
      String completionText,
      String completedText,
      String activationFlag,
      String requiredItemKey,
      int requiredItemQty,
      String unlockZoneId,
      String rewardItemKey) {
    this(
        id,
        title,
        giverNpc,
        targetMonster,
        requiredKills,
        targetWorldZ,
        areaCenterX,
        areaCenterY,
        areaRadiusTiles,
        rewardGold,
        rewardXp,
        offerText,
        completionText,
        completedText,
        activationFlag,
        requiredItemKey,
        requiredItemQty,
        unlockZoneId,
        rewardItemKey,
        0);
  }

  /** Pre-T4C-0052 20-arg shape (no compendium walkthrough text) - every quest added by
   * T4C-0035 through T4C-0051 uses this constructor unchanged. */
  public QuestDef(
      String id,
      String title,
      String giverNpc,
      String targetMonster,
      int requiredKills,
      int targetWorldZ,
      int areaCenterX,
      int areaCenterY,
      int areaRadiusTiles,
      int rewardGold,
      int rewardXp,
      String offerText,
      String completionText,
      String completedText,
      String activationFlag,
      String requiredItemKey,
      int requiredItemQty,
      String unlockZoneId,
      String rewardItemKey,
      int minLevel) {
    this(
        id,
        title,
        giverNpc,
        targetMonster,
        requiredKills,
        targetWorldZ,
        areaCenterX,
        areaCenterY,
        areaRadiusTiles,
        rewardGold,
        rewardXp,
        offerText,
        completionText,
        completedText,
        activationFlag,
        requiredItemKey,
        requiredItemQty,
        unlockZoneId,
        rewardItemKey,
        minLevel,
        null,
        null);
  }

  /** Pre-T4C-0068 21-arg shape (adds the compendium walkthrough text, no reward spell) - every
   * quest predating T4C-0068's rewardSpellKey addition uses this constructor unchanged. Restored
   * exactly as it was: T4C-0068 originally replaced this overload's last parameter with
   * rewardSpellKey, which silently discarded every existing quest's walkthroughText (it was still
   * being passed positionally as the 21st argument) and misread it as a reward-spell key instead -
   * caught by regenerating the compendium data and seeing every quest's walkthrough vanish. */
  public QuestDef(
      String id,
      String title,
      String giverNpc,
      String targetMonster,
      int requiredKills,
      int targetWorldZ,
      int areaCenterX,
      int areaCenterY,
      int areaRadiusTiles,
      int rewardGold,
      int rewardXp,
      String offerText,
      String completionText,
      String completedText,
      String activationFlag,
      String requiredItemKey,
      int requiredItemQty,
      String unlockZoneId,
      String rewardItemKey,
      int minLevel,
      String walkthroughText) {
    this(
        id,
        title,
        giverNpc,
        targetMonster,
        requiredKills,
        targetWorldZ,
        areaCenterX,
        areaCenterY,
        areaRadiusTiles,
        rewardGold,
        rewardXp,
        offerText,
        completionText,
        completedText,
        activationFlag,
        requiredItemKey,
        requiredItemQty,
        unlockZoneId,
        rewardItemKey,
        minLevel,
        walkthroughText,
        null);
  }

  // Current 22-arg shape (adds an optional reward spell, T4C-0068 - see rewardSpellKey above) is
  // Lombok's @AllArgsConstructor, generated automatically for all 22 fields in declaration order -
  // don't redeclare it manually here, that's a duplicate-constructor compile error.
}
