package com.perso.T4C.combat;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.Locale;

public final class SeraphArrivalAnimation {
  public enum Kind {
    ARTHERK(
        "64kSpellSeraphArrival-",
        "Seraph.wav",
        25,
        89),
    NEPHILIM(
        "64kSeraphArivalBlack-",
        "Evil Seraph.wav",
        78,
        89);

    private final String spritePrefix;
    private final String sound;
    private final int revealFrame;
    private final int moveFrame;

    Kind(String spritePrefix, String sound, int revealFrame, int moveFrame) {
      this.spritePrefix = spritePrefix;
      this.sound = sound;
      this.revealFrame = revealFrame;
      this.moveFrame = moveFrame;
    }

    public String spritePrefix() {
      return spritePrefix;
    }

    public String sound() {
      return sound;
    }

    public int revealFrame() {
      return revealFrame;
    }

    public int moveFrame() {
      return moveFrame;
    }
  }

  private SeraphArrivalAnimation() {}

  public static Kind kindOf(Player player) {
    return kindOf(equippedBackAppearance(player));
  }

  public static Kind kindOf(String appearance) {
    if (appearance == null || appearance.isBlank()) {
      return null;
    }
    String value = appearance.toLowerCase(Locale.ROOT);
    if (startsWithAny(value, "pupseraphwhitewings", "archwings", "nms_x6white")) {
      return Kind.ARTHERK;
    }
    if (startsWithAny(
        value, "pupseraphblackwings", "darkwings", "pupseraphdarkwings", "nms_x6black")) {
      return Kind.NEPHILIM;
    }
    return null;
  }

  public static boolean playerVisible(Kind kind, int frameIndex, boolean animationActive) {
    if (kind == null || !animationActive) {
      return true;
    }
    return frameIndex >= kind.revealFrame();
  }

  public static boolean movementAllowed(Kind kind, int frameIndex, boolean animationActive) {
    if (kind == null || !animationActive) {
      return true;
    }
    return frameIndex >= kind.moveFrame();
  }

  private static String equippedBackAppearance(Player player) {
    if (player == null || player.getEquippedItems() == null) {
      return null;
    }
    String itemKey = player.getEquippedItems().get(BodyPart.BACK);
    if (itemKey == null || itemKey.isBlank()) {
      return null;
    }
    if (kindOf(itemKey) != null) {
      return itemKey;
    }
    ItemDefinition definition = ItemRegistry.findByKey(itemKey);
    return definition == null ? itemKey : definition.getAppearanceEquippedFor(BodyPart.BACK);
  }

  private static boolean startsWithAny(String value, String... prefixes) {
    for (String prefix : prefixes) {
      if (value.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
