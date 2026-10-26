package com.perso.T4C.adventure;

import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.*;
import com.perso.T4C.player.*;
import com.perso.T4C.quest.definition.ConcordCampaign;
import java.util.*;

/** Discovery is character history, independent of continued ownership and surviving rebirth. */
public final class CollectionBook {
  private static final String FOUND = "collection.found.";
  private static final String TITLE = "collection.title.";

  public record SetEntry(String id, String name, List<String> items) {
    public SetEntry {
      items = List.copyOf(items);
    }

    public long discovered(Player player) {
      return items.stream().filter(k -> has(player, k)).count();
    }

    public boolean complete(Player player) {
      return !items.isEmpty() && discovered(player) == items.size();
    }
  }

  private CollectionBook() {}

  public static boolean equipment(ItemDefinition item) {
    return item != null && item.getBodyPart() != null;
  }

  public static void record(Player player, String key) {
    var item = ItemRegistry.findByKey(key);
    if (player != null && equipment(item)) player.setQuestFlag(FOUND + item.getKey(), 1);
  }

  public static boolean has(Player player, String key) {
    var item = ItemRegistry.findByKey(key);
    return player != null && item != null && player.getQuestFlag(FOUND + item.getKey()) > 0;
  }

  public static void synchronize(Player player) {
    if (player == null) return;
    for (String key : player.getInventory()) record(player, key);
    for (String key : player.getEquippedItems().values()) record(player, key);
    for (String key : player.getStorage()) record(player, key);
  }

  public static List<SetEntry> sets() {
    List<SetEntry> result = new ArrayList<>();
    Set<String> assigned = new HashSet<>();
    for (var god : DivinePantheon.all())
      add(
          result,
          assigned,
          "god_" + god.id(),
          I18n.message("collection.god_set", god.name()),
          DivinePantheon.equipmentKeys(god));
    for (String style : ConcordCampaign.STYLES) {
      List<String> concord = new ArrayList<>();
      for (int chapter = 0; chapter < 8; chapter++)
        concord.add(ConcordCampaign.reward(chapter, style));
      add(
          result,
          assigned,
          "concord_" + style,
          I18n.message("collection.concord_set", I18n.key("adventure.style." + style)),
          concord);
      List<String> events = new ArrayList<>();
      for (var e : WorldEvents.definitions()) events.add(WorldEvents.rewardKey(e, style));
      add(
          result,
          assigned,
          "event_" + style,
          I18n.message("collection.event_set", I18n.key("adventure.style." + style)),
          events);
    }
    Map<String, List<String>> others = new TreeMap<>();
    for (var d : ItemRegistry.load()) {
      var item = ItemRegistry.findByKey(d.getKey());
      if (!equipment(item) || assigned.contains(item.getKey())) continue;
      String group = item.getBodyPart().name().toLowerCase(Locale.ROOT);
      others.computeIfAbsent(group, k -> new ArrayList<>()).add(item.getKey());
    }
    others.forEach(
        (key, items) ->
            add(result, assigned, "slot_" + key, I18n.key("collection.slot." + key), items));
    return List.copyOf(result);
  }

  private static void add(
      List<SetEntry> sets, Set<String> assigned, String id, String name, List<String> candidates) {
    var keys =
        candidates.stream()
            .map(ItemRegistry::findByKey)
            .filter(CollectionBook::equipment)
            .map(ItemDefinition::getKey)
            .distinct()
            .toList();
    if (!keys.isEmpty()) {
      sets.add(new SetEntry(id, name, keys));
      assigned.addAll(keys);
    }
  }

  public static void useTitle(Player player, SetEntry set) {
    if (player == null || !set.complete(player)) return;
    player.getQuestFlags().keySet().removeIf(k -> k.startsWith(TITLE));
    player.setQuestFlag(TITLE + set.id(), 1);
  }

  public static String activeTitle(Player player) {
    if (player == null) return "";
    var active =
        player.getQuestFlags().entrySet().stream()
            .filter(e -> e.getKey().startsWith(TITLE) && e.getValue() > 0)
            .map(e -> e.getKey().substring(TITLE.length()))
            .findFirst();
    if (active.isEmpty()) return "";
    return sets().stream()
        .filter(s -> s.id().equals(active.get()) && s.complete(player))
        .findFirst()
        .map(s -> I18n.message("collection.title", s.name()))
        .orElse("");
  }
}
