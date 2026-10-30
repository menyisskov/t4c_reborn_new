package com.perso.T4C.online;

import com.perso.T4C.i18n.I18n;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/** Selection-only view of one owned remote corpse. It never changes local player or save state. */
public final class OnlineCorpsePanel {
  public static final int MAX_CHOICES = 9;

  public record Action(UUID corpse, UUID item) {}

  private UUID corpse;
  private int page;
  private List<OnlineConnection.CorpseItem> displayed = List.of();
  private List<OnlineConnection.CorpseItem> source = List.of();
  private boolean goldOnly;

  public boolean isOpen() { return corpse != null; }

  public UUID corpse() { return corpse; }

  public void open(OnlineConnection.Corpse selected) {
    close();
    if (selected == null || selected.items().isEmpty() && selected.gold() <= 0) return;
    corpse = selected.id();
    source = List.copyOf(selected.items());
    displayed = page(source, page);
    goldOnly = source.isEmpty() && selected.gold() > 0;
  }

  public void turnPage(int delta, OnlineConnection.Corpse current) {
    if (!isOpen() || current == null || !corpse.equals(current.id()) || !source.equals(current.items())) return;
    page = clampPage(page + delta, source.size());
    displayed = page(source, page);
  }

  public void close() {
    corpse = null;
    page = 0;
    displayed = List.of();
    source = List.of();
    goldOnly = false;
  }

  public Optional<Action> choose(int digit, OnlineConnection.Corpse current) {
    if (!isOpen() || current == null || !corpse.equals(current.id())
        || digit < 1 || digit > MAX_CHOICES || !source.equals(current.items())) return Optional.empty();
    if (goldOnly) return digit == 1 ? Optional.of(new Action(corpse, null)) : Optional.empty();
    return digit <= displayed.size()
        ? Optional.of(new Action(corpse, displayed.get(digit - 1).id()))
        : Optional.empty();
  }

  public List<String> lines(OnlineConnection.Corpse current, Function<String, String> itemName) {
    var lines = new ArrayList<String>();
    if (!isOpen()) return lines;
    lines.add(I18n.key("online.corpse.title"));
    if (current == null || !corpse.equals(current.id()) || !source.equals(current.items())) {
      lines.add(I18n.key("online.corpse.changed"));
      lines.add(I18n.key("online.corpse.close"));
      return lines;
    }
    if (goldOnly) {
      lines.add(I18n.message("online.corpse.gold_row", 1, current.gold()));
    } else {
      for (int i = 0; i < displayed.size(); i++) {
        var item = displayed.get(i);
        String row = I18n.message("online.corpse.item_row", i + 1, itemName.apply(item.key()), item.quantity());
        if (item.charges() > 0) row += " (" + item.charges() + ")";
        lines.add(row);
      }
      if (current.gold() > 0) lines.add(I18n.key("online.corpse.gold_on_first"));
      if (source.size() > MAX_CHOICES)
        lines.add(I18n.message("online.corpse.page", page + 1, (source.size() - 1) / MAX_CHOICES + 1));
    }
    lines.add(I18n.key("online.corpse.close"));
    return lines;
  }

  private static int clampPage(int page, int size) {
    return Math.max(0, Math.min(page, Math.max(0, (size - 1) / MAX_CHOICES)));
  }

  private static List<OnlineConnection.CorpseItem> page(List<OnlineConnection.CorpseItem> items, int page) {
    int start = clampPage(page, items.size()) * MAX_CHOICES;
    return List.copyOf(items.subList(start, Math.min(start + MAX_CHOICES, items.size())));
  }
}
