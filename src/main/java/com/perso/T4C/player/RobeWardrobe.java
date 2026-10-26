package com.perso.T4C.player;

import com.perso.T4C.helper.AppearanceDefaultsCatalog;
import java.util.List;
import java.util.Map;

/** Appearance-only robes; equipment, requirements and combat bonuses are never changed. */
public final class RobeWardrobe {
  public static final String SAVE_FLAG = "cosmetic.robe";
  public static final List<String> STYLES =
      List.of("embercourt", "rootwarden", "stormsilk", "tidewoven", "dawnchoir", "nightveil");

  private RobeWardrobe() {}

  public static int selection(Player player) {
    int selected = player == null ? 0 : player.getQuestFlag(SAVE_FLAG);
    return selected >= 0 && selected <= STYLES.size() ? selected : 0;
  }

  public static void select(Player player, int selected) {
    if (player == null || selected < 0 || selected > STYLES.size()) return;
    player.setQuestFlag(SAVE_FLAG, selected);
  }

  public static String sprite(String gender, int selected) {
    if (selected <= 0 || selected > STYLES.size()) return null;
    String base =
        AppearanceDefaultsCatalog.FEMALE.equalsIgnoreCase(gender) ? "WoWhiteRobe" : "PupWhiteRobe";
    return base + "__robe_" + STYLES.get(selected - 1);
  }

  public static void apply(Player player, Map<BodyPart, String> parts) {
    String sprite = sprite(player.getGender(), selection(player));
    if (sprite != null) parts.put(BodyPart.BODY, sprite);
  }
}
