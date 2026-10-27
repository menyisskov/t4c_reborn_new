package com.perso.T4C.server;

import com.google.gson.Gson;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.MonsterContent;
import com.perso.T4C.online.model.QuestContent;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.spell.SpellKeys;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Online quest definitions, each resolved against the monster, item and spell catalogs at load.
 * A reference that is bare, unnormalized or unknown refuses the whole catalog, so nothing dangles.
 */
public final class QuestCatalog {
  private final Map<String, QuestDef> definitions = new LinkedHashMap<>();

  public QuestCatalog(QuestContent content, MonsterContent monsters, ItemContent items, SpellCatalog spells) {
    Objects.requireNonNull(content);
    for (var quest : content.quests().values()) {
      if (!monsters.monsters().containsKey(quest.targetMonster()))
        throw new IllegalArgumentException("Unresolved quest monster");
      requireItem(quest.requiredItemKey(), items);
      requireItem(quest.rewardItemKey(), items);
      if (quest.rewardSpellKey() != null) {
        requireNormalizedSpell(quest.rewardSpellKey());
        spells.require(quest.rewardSpellKey());
      }
      definitions.put(quest.id(), toDefinition(quest));
    }
  }

  /** Inventory entries use the normalized item.<key> form; a bare key would silently never match. */
  private static void requireItem(String key, ItemContent items) {
    if (key == null) return;
    if (!key.startsWith("item.") || !items.items().containsKey(key))
      throw new IllegalArgumentException("Unresolved quest item");
  }

  private static void requireNormalizedSpell(String key) {
    if (!key.startsWith("spell.") || !key.equals(SpellKeys.normalize(key)))
      throw new IllegalArgumentException("Unnormalized quest spell");
  }

  private static QuestDef toDefinition(QuestContent.Quest q) {
    return new QuestDef(q.id(), q.title(), q.giverNpc(), q.targetMonster(), q.requiredKills(),
        q.targetWorldZ(), q.areaCenterX(), q.areaCenterY(), q.areaRadiusTiles(), q.rewardGold(),
        q.rewardXp(), q.offerText(), q.completionText(), q.completedText(), q.activationFlag(),
        q.requiredItemKey(), q.requiredItemQty(), null, q.rewardItemKey(), q.minLevel(), null,
        q.rewardSpellKey());
  }

  /** Null when the quest is not implemented online. */
  public QuestDef definition(String id) {
    return definitions.get(id);
  }

  public Collection<QuestDef> definitions() {
    return java.util.Collections.unmodifiableCollection(definitions.values());
  }

  /** Shared immutable bundled catalog, loaded once; the packaged content is validated on first use. */
  public static QuestCatalog bundled() {
    return Bundled.INSTANCE;
  }

  private static final class Bundled {
    static final QuestCatalog INSTANCE = load();

    private static QuestCatalog load() {
      try {
        return loadBundled();
      } catch (IOException unavailable) {
        throw new IllegalStateException("Bundled quest content is unavailable", unavailable);
      }
    }
  }

  public static QuestCatalog loadBundled() throws IOException {
    try (var input = QuestCatalog.class.getResourceAsStream("/content/quests-v1.json")) {
      if (input == null) throw new IOException("Missing bundled quest content");
      var content = read(input);
      return new QuestCatalog(content, MonsterCatalog.loadBundled(), ItemCatalog.loadBundled(),
          SpellCatalog.loadBundled());
    }
  }

  static QuestContent read(InputStream input) throws IOException {
    int limit = 1024 * 1024;
    byte[] bytes = input.readNBytes(limit + 1);
    if (bytes.length > limit) throw new IOException("Quest content exceeds size limit");
    try {
      var content = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), QuestContent.class);
      if (content == null) throw new IllegalArgumentException("Empty content");
      return content;
    } catch (RuntimeException error) {
      throw new IOException("Invalid or unsupported quest content", error);
    }
  }
}
