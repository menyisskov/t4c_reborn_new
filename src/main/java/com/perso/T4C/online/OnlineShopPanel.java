package com.perso.T4C.online;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.online.model.ShopOffersReply;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Key-state and text for the native shop panel, drawn as plain HUD text like the talk, inventory and
 * ability panels. It holds the server's latest listing and turns a digit into a buy or sell request;
 * it never talks to the network and never prices anything: every price shown and every request sent
 * comes from the server's {@link ShopOffersReply}. Quantity is a fixed small set cycled with one key.
 */
final class OnlineShopPanel {
  enum Mode { BUY, SELL }

  /** One request for the connection to send: identity and a small count only. */
  record Action(Mode mode, String npc, String itemKey, int quantity) {}

  /** Number keys 1-9 are the only row choices the panel offers. */
  static final int MAX_CHOICES = 9;
  /** Units per press, cycled with one key. Always at or below the server's per-request limit. */
  static final int[] QUANTITIES = {1, 5, 10};

  private boolean open;
  private String npc;
  private String name = "";
  private Mode mode = Mode.BUY;
  private int page;
  private int quantityIndex;
  private ShopOffersReply reply;

  boolean isOpen() {
    return open && reply != null;
  }

  String npc() {
    return npc;
  }

  Mode mode() {
    return mode;
  }

  int quantity() {
    return QUANTITIES[quantityIndex];
  }

  int page() {
    return page;
  }

  long gold() {
    return reply == null ? 0 : reply.gold();
  }

  /** Opens on the buy list with the server's listing. */
  void open(String npcId, String displayName, ShopOffersReply listing) {
    npc = npcId;
    name = displayName == null ? "" : displayName;
    reply = java.util.Objects.requireNonNull(listing);
    mode = Mode.BUY;
    page = 0;
    open = true;
  }

  /** A fresh listing after a trade or a refusal: same list, same page where it still exists. */
  void refresh(ShopOffersReply listing) {
    if (!open) return;
    reply = java.util.Objects.requireNonNull(listing);
    page = clampPage(page, rows().size());
  }

  void close() {
    open = false;
    reply = null;
    page = 0;
  }

  void toggleMode() {
    if (!isOpen()) return;
    mode = mode == Mode.BUY ? Mode.SELL : Mode.BUY;
    page = 0;
  }

  void cycleQuantity() {
    quantityIndex = (quantityIndex + 1) % QUANTITIES.length;
  }

  void turnPage(int delta) {
    if (isOpen()) page = clampPage(page + delta, rows().size());
  }

  List<ShopOffersReply.Line> rows() {
    if (reply == null) return List.of();
    return mode == Mode.BUY ? reply.buy() : reply.sell();
  }

  static int clampPage(int page, int size) {
    return Math.max(0, Math.min(page, Math.max(0, (size - 1) / MAX_CHOICES)));
  }

  List<ShopOffersReply.Line> visible() {
    var rows = rows();
    int start = clampPage(page, rows.size()) * MAX_CHOICES;
    return rows.subList(start, Math.min(start + MAX_CHOICES, rows.size()));
  }

  /**
   * Digit 1 picks the first visible row. Selling never asks for more than the carried count, so a
   * request the character cannot cover is not even sent; buying is left to the server (gold, weight).
   */
  Optional<Action> choose(int digit) {
    if (!isOpen()) return Optional.empty();
    var rows = visible();
    if (digit < 1 || digit > rows.size() || digit > MAX_CHOICES) return Optional.empty();
    var row = rows.get(digit - 1);
    int units = mode == Mode.SELL ? Math.min(quantity(), row.quantity()) : quantity();
    if (units < 1) return Optional.empty();
    return Optional.of(new Action(mode, npc, row.key(), units));
  }

  /** Title with gold, the active list, one line per visible row, paging note and key hints. */
  List<String> lines(Function<String, String> itemName) {
    var lines = new ArrayList<String>();
    if (!isOpen()) return lines;
    lines.add(I18n.message(mode == Mode.BUY ? "online.shop.title_buy" : "online.shop.title_sell", name));
    lines.add(I18n.message("online.shop.gold", reply.gold()));
    var rows = visible();
    if (rows.isEmpty()) lines.add(I18n.key(mode == Mode.BUY ? "online.shop.empty_buy" : "online.shop.empty_sell"));
    for (int i = 0; i < rows.size(); i++) {
      var row = rows.get(i);
      lines.add(mode == Mode.BUY
          ? I18n.message("online.shop.buy_row", i + 1, itemName.apply(row.key()), row.price())
          : I18n.message("online.shop.sell_row", i + 1, itemName.apply(row.key()), row.price(), row.quantity()));
    }
    int all = rows().size();
    if (all > MAX_CHOICES)
      lines.add(I18n.message("online.shop.page", clampPage(page, all) + 1, (all - 1) / MAX_CHOICES + 1));
    lines.add(I18n.message("online.shop.quantity", quantity()));
    lines.add(I18n.key(mode == Mode.BUY ? "online.shop.hint_buy" : "online.shop.hint_sell"));
    return lines;
  }
}
