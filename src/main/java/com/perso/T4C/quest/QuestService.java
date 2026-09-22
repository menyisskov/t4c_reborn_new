package com.perso.T4C.quest;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class QuestService {
  public static final int STATUS_NOT_STARTED = 0;
  public static final int STATUS_ACTIVE = 1;
  public static final int STATUS_COMPLETED = 2;
  private final XpCurve xpCurve;
  private final Runnable persist;
  private final Consumer<String> systemMessage;
  private final Supplier<List<QuestDef>> definitions;

  public QuestService(XpCurve xpCurve, Runnable persist, Consumer<String> systemMessage) {
    this(xpCurve, persist, systemMessage, QuestRegistry::load);
  }

  QuestService(
      XpCurve xpCurve,
      Runnable persist,
      Consumer<String> systemMessage,
      Supplier<List<QuestDef>> definitions) {
    this.xpCurve = xpCurve;
    this.persist = persist == null ? () -> {} : persist;
    this.systemMessage = systemMessage == null ? ignored -> {} : systemMessage;
    this.definitions = definitions == null ? List::of : definitions;
  }

  public String giveOrReport(String questId, String npcName, Player player) {
    QuestDef definition = findById(questId);
    if (definition == null || player == null) {
      log.warn("Unknown quest '{}' referenced by NPC '{}'", questId, npcName);
      return null;
    }
    if (!same(definition.getGiverNpc(), npcName)) {
      log.warn(
          "NPC '{}' attempted to give quest '{}' owned by '{}'",
          npcName,
          definition.getId(),
          definition.getGiverNpc());
      return null;
    }
    int status = statusFor(player, definition);
    if (status == STATUS_NOT_STARTED) {
      player.setQuestFlag(statusFlag(definition), STATUS_ACTIVE);
      player.setQuestFlag(killsFlag(definition), 0);
      persist.run();
      systemMessage.accept(
          I18n.message("message.quest_accepted", I18n.resolve(definition.getTitle())));
      return I18n.resolve(definition.getOfferText());
    }
    if (status == STATUS_COMPLETED) {
      return I18n.resolve(definition.getCompletedText());
    }
    int kills = kills(player, definition);
    return I18n.message(
        "message.quest_progress_dialog",
        kills,
        definition.getRequiredKills(),
        definition.getRequiredKills() - kills);
  }

  public String turnInReadyQuests(String npcName, Player player) {
    if (npcName == null || player == null) {
      return null;
    }
    List<String> completionLines = new ArrayList<>();
    for (QuestDef definition : loadDefinitions()) {
      if (same(definition.getGiverNpc(), npcName)
          && statusFor(player, definition) == STATUS_ACTIVE
          && kills(player, definition) >= definition.getRequiredKills()
          && hasRequiredItem(player, definition)) {
        completionLines.add(complete(definition, player));
      }
    }
    return completionLines.isEmpty() ? null : String.join("\n", completionLines);
  }

  public boolean recordKill(Player player, String monsterName, int worldZ, int tileX, int tileY) {
    if (player == null || monsterName == null) {
      return false;
    }
    boolean changed = false;
    List<String> notifications = new ArrayList<>();
    for (QuestDef definition : loadDefinitions()) {
      if (statusFor(player, definition) != STATUS_ACTIVE
          || !matchesTargetMonster(definition, monsterName)
          || !insideObjectiveArea(definition, worldZ, tileX, tileY)) {
        continue;
      }
      int current = kills(player, definition);
      if (current >= definition.getRequiredKills()) {
        continue;
      }
      int next = Math.min(definition.getRequiredKills(), current + 1);
      player.setQuestFlag(killsFlag(definition), next);
      changed = true;
      String title = I18n.resolve(definition.getTitle());
      if (next >= definition.getRequiredKills()) {
        notifications.add(I18n.message("message.quest_ready", title, giverDisplayName(definition)));
      } else {
        notifications.add(
            I18n.message("message.quest_progress", title, next, definition.getRequiredKills()));
      }
    }
    if (changed) {
      persist.run();
      notifications.forEach(systemMessage);
    }
    return changed;
  }

  public void awardScriptXp(Player player, int amount) {
    if (player == null || amount == 0) return;
    player.addXpExact(amount, xpCurve);
    persist.run();
  }

  private String complete(QuestDef definition, Player player) {
    if (statusFor(player, definition) == STATUS_COMPLETED) {
      return I18n.resolve(definition.getCompletedText());
    }
    consumeRequiredItem(player, definition);
    player.setQuestFlag(statusFlag(definition), STATUS_COMPLETED);
    player.setQuestFlag(killsFlag(definition), definition.getRequiredKills());
    player.addGold(definition.getRewardGold());
    player.addXpExact(definition.getRewardXp(), xpCurve);
    String zoneId = definition.getUnlockZoneId();
    if (zoneId != null && !zoneId.isBlank()) {
      player.setQuestFlag(zoneUnlockFlag(zoneId), 1);
    }
    persist.run();
    systemMessage.accept(
        I18n.message("message.quest_reward", definition.getRewardGold(), definition.getRewardXp()));
    return I18n.resolve(definition.getCompletionText());
  }

  /** True unless the quest also requires turning in {@code requiredItemQty} copies of
   * {@code requiredItemKey} and the player doesn't have enough. Quests with no item objective
   * (the original shape, and most quests) always pass this check. */
  private static boolean hasRequiredItem(Player player, QuestDef definition) {
    String key = definition.getRequiredItemKey();
    if (key == null || key.isBlank() || definition.getRequiredItemQty() <= 0) return true;
    return Collections.frequency(player.getInventory(), key) >= definition.getRequiredItemQty();
  }

  private static void consumeRequiredItem(Player player, QuestDef definition) {
    String key = definition.getRequiredItemKey();
    int qty = definition.getRequiredItemQty();
    if (key == null || key.isBlank() || qty <= 0) return;
    for (int i = 0; i < qty; i++) {
      InventoryService.remove(player, -1, key);
    }
  }

  /** The durable, rebirth-proof flag a completed {@code unlockZoneId} quest sets - checked by
   * the fast-travel menu ({@code NamedLocations}) to decide whether a zone's entry shows. Public
   * so any other system that also wants to know "has this player unlocked zone X" (a gateway
   * NPC's dialogue check, a teleport gate, etc.) reads the exact same flag. */
  public static String zoneUnlockFlag(String zoneId) {
    return "unlock.zone." + zoneId;
  }

  public static boolean hasUnlockedZone(Player player, String zoneId) {
    return player != null && zoneId != null && player.getQuestFlag(zoneUnlockFlag(zoneId)) != 0;
  }

  public static int statusFor(Player player, QuestDef definition) {
    if (player == null || definition == null) return STATUS_NOT_STARTED;
    int status = player.getQuestFlag(statusFlag(definition));
    if (status != STATUS_NOT_STARTED) return status;
    String activationFlag = definition.getActivationFlag();
    if (activationFlag != null
        && !activationFlag.isBlank()
        && player.getQuestFlag(activationFlag) != 0) {
      return STATUS_ACTIVE;
    }
    return STATUS_NOT_STARTED;
  }

  private static int kills(Player player, QuestDef definition) {
    return Math.max(
        0, Math.min(definition.getRequiredKills(), player.getQuestFlag(killsFlag(definition))));
  }

  private static boolean insideObjectiveArea(
      QuestDef definition, int worldZ, int tileX, int tileY) {
    if (worldZ != definition.getTargetWorldZ()) {
      return false;
    }
    long dx = (long) tileX - definition.getAreaCenterX();
    long dy = (long) tileY - definition.getAreaCenterY();
    long radius = Math.max(0, definition.getAreaRadiusTiles());
    return dx * dx + dy * dy <= radius * radius;
  }

  private static boolean same(String left, String right) {
    return left != null && right != null && left.equalsIgnoreCase(right);
  }

  private static boolean matchesTargetMonster(QuestDef definition, String monsterName) {
    if (same(definition.getTargetMonster(), monsterName)) {
      return true;
    }
    MonsterDef canonical = MonsterRegistry.findByName(monsterName);
    return canonical != null && same(definition.getTargetMonster(), canonical.getName());
  }

  private QuestDef findById(String id) {
    if (id == null) {
      return null;
    }
    for (QuestDef definition : loadDefinitions()) {
      if (definition != null && same(definition.getId(), id)) {
        return definition;
      }
    }
    return null;
  }

  private List<QuestDef> loadDefinitions() {
    List<QuestDef> loaded = definitions.get();
    return loaded == null ? List.of() : loaded;
  }

  public static String giverDisplayName(QuestDef definition) {
    if (definition == null) return "";
    NpcFactoryRegistry.Registration giver = NpcFactoryRegistry.find(definition.getGiverNpc());
    if (giver == null) {
      return definition.getGiverNpc();
    }
    return I18n.resolve(giver.displayName());
  }

  public static String statusFlag(QuestDef definition) {
    return "quest." + definition.getId() + ".status";
  }

  public static String killsFlag(QuestDef definition) {
    String progressKey = definition.getActivationFlag();
    if (progressKey == null || progressKey.isBlank()) {
      progressKey = definition.getId();
    }
    return "quest." + progressKey + ".kills";
  }
}
