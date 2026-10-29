package com.perso.T4C.online.model;

import java.util.List;

/**
 * Owner-only shop listing for one shopkeeper. Every price is the server's: {@code buy} lines are the
 * vendor's stock with its buy price, {@code sell} lines are what the character can sell (unequipped
 * units) with the per-unit payout and the carried quantity. The client never computes or sends a price.
 */
public record ShopOffersReply(long revision, long gold, String npc, List<Line> buy, List<Line> sell) {
  public static final int MAX_LINES = 512;

  public record Line(String key, long price, int quantity) {
    public Line {
      if (key == null || !key.matches("item\\.[a-z0-9_]{1,120}") || price < 0 || quantity < 0)
        throw new IllegalArgumentException("Invalid shop line");
    }
  }

  public ShopOffersReply {
    if (revision < 0 || gold < 0 || npc == null || !npc.matches("[A-Za-z][A-Za-z0-9_]{0,63}"))
      throw new IllegalArgumentException("Invalid shop reply");
    buy = buy == null ? List.of() : List.copyOf(buy);
    sell = sell == null ? List.of() : List.copyOf(sell);
    if (buy.size() > MAX_LINES || sell.size() > MAX_LINES) throw new IllegalArgumentException("Too many shop lines");
  }
}
