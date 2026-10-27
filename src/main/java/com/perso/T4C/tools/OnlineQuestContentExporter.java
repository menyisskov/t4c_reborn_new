package com.perso.T4C.tools;

import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.online.model.QuestContent;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.spell.SpellKeys;
import com.perso.T4C.spell.SpellRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

/**
 * Build-time bridge for the quests the online server implements. Quests outside the allowlist stay
 * offline-only, and any quest field the server cannot run fails the export instead of being dropped.
 */
public final class OnlineQuestContentExporter {
  static final List<String> ALLOWLIST = List.of("renewed_wards");

  private OnlineQuestContentExporter() {}

  public static QuestContent snapshot() {
    // Restore registries before reading so repeated exports cannot accumulate definitions.
    MonsterRegistry.resetToGeneratedDefinitions();
    MonsterJsonLoader.loadAndRegister();
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
    var quests = new TreeMap<String, QuestContent.Quest>();
    for (var id : ALLOWLIST) {
      var definition = QuestRegistry.findById(id);
      if (definition == null) throw new IllegalArgumentException("Missing authored quest " + id);
      quests.put(id, export(definition));
    }
    return new QuestContent(QuestContent.VERSION, quests);
  }

  static QuestContent.Quest export(QuestDef d) {
    if (d.getUnlockZoneId() != null) throw new IllegalArgumentException("Unsupported quest field unlockZoneId: " + d.getId());
    if (MonsterRegistry.findByName(d.getTargetMonster()) == null)
      throw new IllegalArgumentException("Unresolved quest monster: " + d.getId());
    requireItem(d.getRequiredItemKey(), d.getId());
    requireItem(d.getRewardItemKey(), d.getId());
    String spellKey = null;
    if (d.getRewardSpellKey() != null) {
      spellKey = SpellKeys.normalize(d.getRewardSpellKey());
      if (SpellRegistry.findByName(spellKey) == null) throw new IllegalArgumentException("Unresolved quest spell: " + d.getId());
    }
    return new QuestContent.Quest(d.getId(), d.getTitle(), d.getGiverNpc(), d.getTargetMonster(),
        d.getRequiredKills(), d.getTargetWorldZ(), d.getAreaCenterX(), d.getAreaCenterY(),
        d.getAreaRadiusTiles(), d.getRewardGold(), d.getRewardXp(), d.getOfferText(),
        d.getCompletionText(), d.getCompletedText(), d.getActivationFlag(), d.getRequiredItemKey(),
        d.getRequiredItemQty(), d.getRewardItemKey(), d.getMinLevel(), spellKey);
  }

  private static void requireItem(String key, String questId) {
    if (key == null) return;
    // Inventory entries use the normalized item.<key> form; a bare key would never match.
    if (!key.startsWith("item.") || ItemRegistry.allByKey().get(key) == null)
      throw new IllegalArgumentException("Unresolved quest item in " + questId + ": " + key);
  }

  public static String render(QuestContent content) {
    return OnlineContentJson.render(content);
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Expected output JSON path");
    var path = Path.of(args[0]);
    if (path.getParent() != null) Files.createDirectories(path.getParent());
    Files.writeString(path, render(snapshot()));
    System.out.println("Exported allowlisted online quest objectives and rewards; other quests remain offline-only.");
  }
}
