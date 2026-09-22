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

  /** Original 14-arg shape (no activationFlag, no item/unlock objective). */
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
        null);
  }

  /** Pre-T4C-0019 15-arg shape (activationFlag, no item/unlock objective) - every quest added
   * before this pass uses this constructor unchanged. */
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
        null);
  }
}
