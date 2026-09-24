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
        null);
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
        null);
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
        null);
  }
}
