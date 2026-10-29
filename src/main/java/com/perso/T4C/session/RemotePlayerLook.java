package com.perso.T4C.session;

import com.perso.T4C.helper.AppearanceDefaultsCatalog;
import com.perso.T4C.helper.PlayerAppearanceDefaults;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds the look of another online player from the public data of a snapshot actor, through the
 * same appearance pipeline single-player uses for the own character: naked defaults for the
 * gender, then {@link PlayerAppearanceDefaults#applyDefaults} over the worn items.
 */
final class RemotePlayerLook {
  private RemotePlayerLook() {}

  /** A stable text that changes exactly when the visible look changes. */
  static String signature(OnlineConnection.Actor actor) {
    return gender(actor)
        + "|"
        + new TreeMap<>(actor.equipment() == null ? Map.of() : actor.equipment())
        + "|"
        + (actor.name() == null ? "" : actor.name());
  }

  /** The single-player gender constant for an actor; actors without a published look keep the old id parity. */
  static String gender(OnlineConnection.Actor actor) {
    if (actor.gender() != null) {
      return "female".equalsIgnoreCase(actor.gender())
          ? AppearanceDefaultsCatalog.FEMALE
          : AppearanceDefaultsCatalog.MALE;
    }
    return actor.id() % 2 == 0 ? AppearanceDefaultsCatalog.FEMALE : AppearanceDefaultsCatalog.MALE;
  }

  /** Worn items as the player model holds them, including the second slot of two-slot items. */
  static Map<BodyPart, String> equipped(Map<String, String> equipment) {
    Map<BodyPart, String> worn = new EnumMap<>(BodyPart.class);
    if (equipment == null) return worn;
    for (Map.Entry<String, String> entry : equipment.entrySet()) {
      BodyPart part;
      try {
        part = BodyPart.valueOf(entry.getKey());
      } catch (IllegalArgumentException unknown) {
        continue;
      }
      String key = entry.getValue();
      if (key == null || key.isEmpty()) continue;
      worn.put(part, key);
      ItemDefinition definition = ItemRegistry.findByKey(key);
      BodyPart secondary = definition == null ? null : definition.getSecondaryBodyPart();
      if (secondary != null) worn.put(secondary, key);
    }
    return worn;
  }

  static Player build(OnlineConnection.Actor actor) {
    String gender = gender(actor);
    var parts = new ArrayList<Object>();
    AppearanceDefaultsCatalog.nakedParts(gender)
        .forEach(
            (part, sprite) -> {
              parts.add(part);
              parts.add(sprite);
            });
    Player player = new Player(parts.toArray());
    player.setGender(gender);
    if (actor.name() != null) player.setName(actor.name());
    Map<BodyPart, String> worn = equipped(actor.equipment());
    if (!worn.isEmpty()) {
      player.setEquippedItems(worn);
      PlayerAppearanceDefaults.applyDefaults(player);
    }
    player.setRemote(true);
    return player;
  }
}
