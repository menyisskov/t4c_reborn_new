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
      case "/cast" -> ability(code, true);
      case "/use" -> ability(code, false);
      default -> null;
    };
  }

  /** Spell and potion refusals share the mana/cooldown/exhaustion reasons the server reuses for both. */
  private static String ability(String code, boolean cast) {
    return switch (code) {
      case "not_enough_mana" -> "online.ability.not_enough_mana";
      case "cooldown" -> "online.ability.cooldown";
      case "exhausted" -> "online.ability.exhausted";
      case "spell_failed" -> "online.ability.failed";
      case "dead" -> "online.ability.dead";
      case "busy", "revision" -> "online.ability.busy";
      case "not_learned" -> "online.cast.not_learned";
      case "item_not_owned" -> "online.use.not_owned";
      case "item_equipped" -> "online.use.equipped";
      case "no_healing_needed" -> "online.use.no_healing_needed";
      case "no_mana_needed" -> "online.use.no_mana_needed";
      case "chance_failed" -> "online.use.chance_failed";
      case "unsupported_spell", "unsupported_item", "unsupported_ability", "unsupported_effects", "no_effect" ->
          cast ? "online.cast.unsupported" : "online.use.unsupported";
      default -> cast ? "online.cast.refused" : "online.use.refused";
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
