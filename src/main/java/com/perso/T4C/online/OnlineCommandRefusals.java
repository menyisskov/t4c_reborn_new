package com.perso.T4C.online;

/**
 * Maps a server refusal code to an English message key for the native client. Unknown codes fall
 * back to one generic refusal per command so a new server reason never leaves the player silent.
 */
final class OnlineCommandRefusals {
  /** Routine refusal inside the swing cooldown; the client reads it but does not show it. */
  static final String ATTACK_COOLDOWN = "online.attack.cooldown";

  private OnlineCommandRefusals() {}

  /** Returns null for commands whose refusals are not shown to the player. */
  static String messageKey(String path, String reason) {
    if (path == null) return null;
    String code = reason == null ? "" : reason;
    return switch (path) {
      case "/pickup" -> pickup(code);
      case "/recover" -> switch (code) {
        case "corpse_unavailable" -> "online.recover.unavailable";
        case "item_unavailable" -> "online.recover.item_unavailable";
        case "out_of_range" -> "online.recover.out_of_range";
        case "too_heavy" -> "online.recover.too_heavy";
        case "dead" -> "online.recover.dead";
        case "busy", "revision" -> "online.recover.busy";
        default -> "online.recover.refused";
      };
      case "/equip" -> switch (code) {
        case "requirements" -> "online.equip.requirements";
        case "equipment_conflict" -> "online.equip.conflict";
        case "wrong_slot" -> "online.equip.wrong_slot";
        case "busy", "revision" -> "online.equip.busy";
        default -> "online.equip.refused";
      };
      case "/unequip" -> switch (code) {
        case "item_not_equipped" -> "online.unequip.not_equipped";
        case "busy", "revision" -> "online.equip.busy";
        default -> "online.equip.refused";
      };
      case "/interact" -> switch (code) {
        case "npc_unavailable" -> "online.npc.unavailable";
        case "busy", "revision" -> "online.npc.busy";
        default -> "online.npc.refused";
      };
      case "/shop-offers", "/shop-buy", "/shop-sell" -> shop(code);
      case "/cast" -> ability(code, true);
      case "/use" -> ability(code, false);
      case "/attack" -> attack(code);
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
      case "wrong_target", "pvp_forbidden" -> cast ? "online.cast.wrong_target" : "online.use.refused";
      case "out_of_range" -> cast ? "online.cast.out_of_range" : "online.use.refused";
      case "no_line_of_sight" -> cast ? "online.cast.no_line_of_sight" : "online.use.refused";
      case "safe_haven" -> cast ? "online.cast.safe_haven" : "online.use.refused";
      case "creature_unavailable" -> cast ? "online.cast.no_target" : "online.use.refused";
      case "friend_unavailable" -> cast ? "online.cast.no_friend" : "online.use.refused";
      case "protected_target" -> cast ? "online.cast.protected" : "online.use.refused";
      case "unsupported_combat" -> cast ? "online.cast.unsupported_combat" : "online.use.refused";
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

  /**
   * Melee refusals as the server's OnlineMelee and session guards report them. The routine
   * {@link #ATTACK_COOLDOWN} refusal is mapped so callers can read it, but the HUD stays silent for it.
   */
  private static String attack(String code) {
    return switch (code) {
      case "cooldown" -> ATTACK_COOLDOWN;
      case "dead" -> "online.attack.dead";
      case "out_of_range_or_blocked" -> "online.attack.out_of_reach";
      case "creature_unavailable" -> "online.attack.no_target";
      case "creature_busy" -> "online.attack.target_busy";
      case "protected_target" -> "online.attack.protected";
      case "unsupported_combat" -> "online.attack.unsupported";
      case "busy", "revision", "region", "uninitialized_character" -> "online.attack.busy";
      default -> "online.attack.refused";
    };
  }

  /** Vendor refusals as the server's shop routes and its trade transaction report them. */
  private static String shop(String code) {
    return switch (code) {
      case "npc_unavailable" -> "online.shop.unavailable";
      case "not_a_shop" -> "online.shop.not_a_shop";
      case "not_for_sale" -> "online.shop.not_for_sale";
      case "insufficient_gold" -> "online.shop.not_enough_gold";
      case "too_heavy" -> "online.shop.too_heavy";
      case "item_unavailable" -> "online.shop.not_enough_items";
      case "not_sellable" -> "online.shop.not_sellable";
      case "unsupported_item" -> "online.shop.unsupported_item";
      case "dead" -> "online.shop.dead";
      case "busy", "revision", "region_epoch" -> "online.shop.busy";
      default -> "online.shop.refused";
    };
  }

  private static String pickup(String code) {
    return switch (code) {
      case "loot_unavailable" -> "online.loot.unavailable";
      case "loot_reserved" -> "online.loot.reserved";
      case "loot_taken" -> "online.loot.taken";
      case "out_of_range" -> "online.loot.out_of_range";
      case "too_heavy" -> "online.loot.too_heavy";
      case "unsupported_item" -> "online.loot.unsupported_item";
      default -> "online.loot.refused";
    };
  }
}
