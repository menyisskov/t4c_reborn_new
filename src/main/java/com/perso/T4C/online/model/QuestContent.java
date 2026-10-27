package com.perso.T4C.online.model;

import java.util.Map;

/**
 * Authored quest objectives and rewards for the authoritative server. Display text stays as i18n
 * keys for the client to resolve; unsupported quest fields are absent, so they fail closed.
 */
public record QuestContent(int version, Map<String, Quest> quests) {
  public static final int VERSION = 1;

  public QuestContent {
    if (version != VERSION) throw new IllegalArgumentException("Unsupported quest content version");
    quests = Map.copyOf(quests);
    if (quests.isEmpty()) throw new IllegalArgumentException("Empty quest content");
    quests.forEach((key, quest) -> {
      if (!key.equals(quest.id())) throw new IllegalArgumentException("Quest identity mismatch");
    });
  }

  public record Quest(String id, String title, String giverNpc, String targetMonster,
      int requiredKills, int targetWorldZ, int areaCenterX, int areaCenterY, int areaRadiusTiles,
      int rewardGold, int rewardXp, String offerText, String completionText, String completedText,
      String activationFlag, String requiredItemKey, int requiredItemQty, String rewardItemKey,
      int minLevel, String rewardSpellKey) {
    public Quest {
      if (id == null || id.isBlank() || id.length() > 128 || title == null || giverNpc == null
          || targetMonster == null || targetMonster.isBlank() || requiredKills <= 0
          || targetWorldZ < 0 || areaRadiusTiles < 0 || rewardGold < 0 || rewardXp < 0
          || minLevel < 0 || offerText == null || completionText == null || completedText == null
          || (requiredItemKey == null ? requiredItemQty != 0 : requiredItemQty < 1))
        throw new IllegalArgumentException("Invalid quest definition");
    }
  }
}
