package com.perso.T4C.online;

/**
 * Maps a server refusal code to an English message key for the native client. Unknown codes fall
 * back to one generic refusal per command so a new server reason never leaves the player silent.
 */
final class OnlineCommandRefusals {
  private OnlineCommandRefusals() {}

  /** Returns null for commands whose refusals are not shown to the player. */
  static String messageKey(String path, String reason) {
    if (path == null) return null;
    String code = reason == null ? "" : reason;
    return switch (path) {
      case "/pickup" -> pickup(code);
      case "/equip" -> switch (code) {
        case "requirements" -> "online.equip.requirements";
        case "equipment_conflict" -> "online.equip.conflict";
        case "wrong_slot" -> "online.equip.wrong_slot";
        default -> "online.equip.refused";
      };
      case "/unequip" -> "item_not_equipped".equals(code) ? "online.unequip.not_equipped" : "online.equip.refused";
      case "/interact" -> "npc_unavailable".equals(code) ? "online.npc.unavailable" : "online.npc.refused";
      default -> null;
    };
  }

  private static String pickup(String code) {
    return switch (code) {
      case "loot_unavailable" -> "online.loot.unavailable";
      case "loot_reserved" -> "online.loot.reserved";
      case "out_of_range" -> "online.loot.out_of_range";
      case "too_heavy" -> "online.loot.too_heavy";
      case "unsupported_item" -> "online.loot.unsupported_item";
      default -> "online.loot.refused";
    };
  }
}
