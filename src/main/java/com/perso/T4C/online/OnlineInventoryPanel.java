package com.perso.T4C.online;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.player.BodyPart;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/** Paged view of committed server inventory; selecting a row produces an equip or unequip intent. */
public final class OnlineInventoryPanel {
  public static final int MAX_CHOICES = 9;

  public enum Kind { CARRIED, EQUIPPED }

  public record Row(Kind kind, CharacterProgress.Item item, BodyPart slot) {}

  public record Action(UUID item, BodyPart slot) {}

  private boolean open;
  private int page;
  private List<Row> displayed = List.of();
  private List<Row> displayedSource = List.of();
  private int displayedPage = -1;

  public boolean isOpen() { return open; }

  public int page() { return page; }

  public void open() {
    open = true;
    page = 0;
    displayed = List.of();
    displayedSource = List.of();
    displayedPage = -1;
  }

  public void close() {
    open = false;
    page = 0;
    displayed = List.of();
    displayedSource = List.of();
    displayedPage = -1;
  }

  public void turnPage(int delta, CharacterProgress progress) {
    if (!open) return;
    page = clampPage(page + delta, rows(progress).size());
    displayed = List.of();
    displayedSource = List.of();
    displayedPage = -1;
  }

  public Optional<Action> choose(int digit, CharacterProgress progress) {
    List<Row> rows = rows(progress);
    if (!open || digit < 1 || digit > MAX_CHOICES
        || !rows.equals(displayedSource) || page != displayedPage) return Optional.empty();
    List<Row> visible = displayed;
    if (digit > visible.size()) return Optional.empty();
    Row row = visible.get(digit - 1);
    return Optional.of(new Action(row.item().id(), row.slot()));
  }

  /** Server-authoritative contents, with worn items shown in their slots and not repeated as carried. */
  public List<Row> rows(CharacterProgress progress) {
    if (progress == null) return List.of();
    var byId = new java.util.HashMap<UUID, CharacterProgress.Item>();
    progress.inventory().forEach(item -> byId.put(item.id(), item));
    Set<UUID> worn = new HashSet<>();
    var result = new ArrayList<Row>();
    for (BodyPart slot : BodyPart.values()) {
      UUID id = progress.equipment().get(slot.name());
      CharacterProgress.Item item = id == null ? null : byId.get(id);
      if (item != null && worn.add(id)) result.add(new Row(Kind.EQUIPPED, item, slot));
    }
    for (CharacterProgress.Item item : progress.inventory())
      if (!worn.contains(item.id())) result.add(new Row(Kind.CARRIED, item, null));
    return List.copyOf(result);
  }

  public List<String> lines(CharacterProgress progress, Function<String, String> itemName) {
    var lines = new ArrayList<String>();
    if (!open) return lines;
    List<Row> rows = rows(progress);
    page = clampPage(page, rows.size());
    lines.add(I18n.key("online.inventory.title"));
    if (progress == null) {
      lines.add(I18n.key("online.inventory.not_connected"));
      lines.add(I18n.key("online.inventory.close"));
      displayed = List.of();
      displayedSource = List.of();
      displayedPage = -1;
      return lines;
    }
    if (rows.isEmpty()) lines.add(I18n.key("online.inventory.empty"));
    displayed = page(rows, page);
    displayedSource = rows;
    displayedPage = page;
    for (int i = 0; i < displayed.size(); i++) {
      Row row = displayed.get(i);
      String name = itemName.apply(row.item().key());
      String text = row.kind() == Kind.EQUIPPED
          ? I18n.message("online.inventory.unequip_row", i + 1, name, row.slot().name())
          : I18n.message("online.inventory.equip_row", i + 1, name);
      if (row.item().quantity() > 1) text += " x" + row.item().quantity();
      if (row.item().charges() > 0) text += " (" + row.item().charges() + ")";
      lines.add(text);
    }
    if (rows.size() > MAX_CHOICES)
      lines.add(I18n.message("online.inventory.page", page + 1, (rows.size() - 1) / MAX_CHOICES + 1));
    lines.add(I18n.key("online.inventory.close"));
    return lines;
  }

  public static int clampPage(int page, int size) {
    return Math.max(0, Math.min(page, Math.max(0, (size - 1) / MAX_CHOICES)));
  }

  private static List<Row> page(List<Row> rows, int page) {
    int start = clampPage(page, rows.size()) * MAX_CHOICES;
    return rows.subList(start, Math.min(start + MAX_CHOICES, rows.size()));
  }
}
