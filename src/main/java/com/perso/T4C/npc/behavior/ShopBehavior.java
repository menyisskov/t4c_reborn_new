package com.perso.T4C.npc.behavior;

import java.util.List;
import java.util.Locale;

public final class ShopBehavior implements NpcBehavior {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private final List<String> items;

  public ShopBehavior(List<String> items) {

    this.items = List.copyOf(items);
  }

  public List<String> items() {
    return items;
  }

  @Override
  public boolean onKeyword(NpcBehaviorContext c, String keyword) {

    String k = keyword == null ? "" : keyword.toUpperCase(Locale.ROOT);

    if (k.contains("SELL")) {

      c.openSellShop();

      return true;
    }

    if (k.contains("BUY") || k.contains("SHOP") || k.contains("TRADE")) {

      if (!items.isEmpty()
          && (items.get(0).startsWith("rusted_short_sword_1")
              || items.get(0).startsWith("rusted_short_sword_2"))) c.askYesNo("shop_browse");
      else c.openShop(items);

      return true;
    }

    return false;
  }

  @Override
  public boolean onYesNo(NpcBehaviorContext c, String state, boolean yes) {

    if (!"shop_browse".equals(state)) return false;

    if (yes) c.openShop(items);

    return true;
  }
}
