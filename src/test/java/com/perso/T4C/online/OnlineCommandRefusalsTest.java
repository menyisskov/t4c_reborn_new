package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.i18n.I18n;
import java.util.List;
import org.junit.jupiter.api.Test;

class OnlineCommandRefusalsTest {
  @Test
  void mapsEquipAndUnequipReasonsToReadableKeysAndFallsBackToOneGenericRefusal() {
    assertEquals("online.equip.requirements", OnlineCommandRefusals.messageKey("/equip", "requirements"));
    assertEquals("online.equip.conflict", OnlineCommandRefusals.messageKey("/equip", "equipment_conflict"));
    assertEquals("online.unequip.not_equipped", OnlineCommandRefusals.messageKey("/unequip", "item_not_equipped"));
    assertEquals("online.equip.refused", OnlineCommandRefusals.messageKey("/equip", "revision"));
    assertEquals("online.equip.refused", OnlineCommandRefusals.messageKey("/unequip", null));
  }

  @Test
  void keepsPickupRefusalsAndMapsInteractionRefusals() {
    assertEquals("online.loot.out_of_range", OnlineCommandRefusals.messageKey("/pickup", "out_of_range"));
    assertEquals("online.loot.refused", OnlineCommandRefusals.messageKey("/pickup", null));
    assertEquals("online.npc.unavailable", OnlineCommandRefusals.messageKey("/interact", "npc_unavailable"));
    assertEquals("online.npc.busy", OnlineCommandRefusals.messageKey("/interact", "busy"));
    assertEquals("online.npc.busy", OnlineCommandRefusals.messageKey("/interact", "revision"));
    assertEquals("online.npc.refused", OnlineCommandRefusals.messageKey("/interact", "anything-unknown"));
    assertEquals("online.loot.taken", OnlineCommandRefusals.messageKey("/pickup", "loot_taken"));
  }

  @Test
  void mapsSpellAndPotionRefusalsAndFallsBackToOneGenericRefusalEach() {
    assertEquals("online.ability.not_enough_mana", OnlineCommandRefusals.messageKey("/cast", "not_enough_mana"));
    assertEquals("online.ability.not_enough_mana", OnlineCommandRefusals.messageKey("/use", "not_enough_mana"));
    assertEquals("online.cast.not_learned", OnlineCommandRefusals.messageKey("/cast", "not_learned"));
    assertEquals("online.use.no_healing_needed", OnlineCommandRefusals.messageKey("/use", "no_healing_needed"));
    assertEquals("online.ability.busy", OnlineCommandRefusals.messageKey("/cast", "revision"));
    assertEquals("online.cast.refused", OnlineCommandRefusals.messageKey("/cast", "something_new"));
    assertEquals("online.use.refused", OnlineCommandRefusals.messageKey("/use", null));
  }

  @Test
  void everyAbilityRefusalKeyExistsInTheEnglishCatalogue() {
    for (var path : List.of("/cast", "/use")) {
      for (var reason : List.of("not_enough_mana", "cooldown", "exhausted", "spell_failed", "dead", "busy",
          "revision", "not_learned", "item_not_owned", "item_equipped", "no_healing_needed", "no_mana_needed",
          "chance_failed", "unsupported_spell", "unsupported_item", "unsupported_ability", "unsupported_effects",
          "no_effect", "friend_unavailable", "anything-unknown")) {
        var key = OnlineCommandRefusals.messageKey(path, reason);
        assertTrue(I18n.has(key), key);
      }
    }
  }

  @Test
  void mapsCorpseRecoveryRefusalsToReadableKeysAndEveryKeyExists() {
    assertEquals("online.recover.item_unavailable", OnlineCommandRefusals.messageKey("/recover", "item_unavailable"));
    assertEquals("online.recover.too_heavy", OnlineCommandRefusals.messageKey("/recover", "too_heavy"));
    assertEquals("online.recover.unavailable", OnlineCommandRefusals.messageKey("/recover", "corpse_unavailable"));
    assertEquals("online.recover.refused", OnlineCommandRefusals.messageKey("/recover", "something_new"));
    for (var reason : List.of("corpse_unavailable", "item_unavailable", "out_of_range", "too_heavy", "dead", "busy",
        "revision", "anything-unknown")) {
      var key = OnlineCommandRefusals.messageKey("/recover", reason);
      assertTrue(I18n.has(key), key);
    }
  }

  @Test
  void mapsAttackRefusalsAndFallsBackToOneGenericRefusalWithoutLeakingServerText() {
    assertEquals("online.attack.cooldown", OnlineCommandRefusals.messageKey("/attack", "cooldown"));
    assertEquals("online.attack.dead", OnlineCommandRefusals.messageKey("/attack", "dead"));
    assertEquals("online.attack.out_of_reach", OnlineCommandRefusals.messageKey("/attack", "out_of_range_or_blocked"));
    assertEquals("online.attack.no_target", OnlineCommandRefusals.messageKey("/attack", "creature_unavailable"));
    assertEquals("online.attack.busy", OnlineCommandRefusals.messageKey("/attack", "revision"));
    assertEquals("online.attack.refused", OnlineCommandRefusals.messageKey("/attack", "something_new"));
    assertEquals("online.attack.refused", OnlineCommandRefusals.messageKey("/attack", null));
    assertNull(OnlineCommandRefusals.messageKey("/input", "busy"));
  }

  @Test
  void everyAttackRefusalKeyExistsInTheEnglishCatalogue() {
    for (var reason : List.of("cooldown", "dead", "out_of_range_or_blocked", "creature_unavailable",
        "creature_busy", "protected_target", "unsupported_combat", "busy", "revision", "region",
        "uninitialized_character", "anything-unknown")) {
      var key = OnlineCommandRefusals.messageKey("/attack", reason);
      assertTrue(I18n.has(key), key);
    }
  }

  @Test
  void everyMappedKeyExistsInTheEnglishCatalogue() {
    for (var path : List.of("/equip", "/unequip", "/interact", "/pickup")) {
      for (var reason : List.of("requirements", "equipment_conflict", "wrong_slot", "item_not_equipped",
          "npc_unavailable", "out_of_range", "loot_unavailable", "anything-unknown")) {
        var key = OnlineCommandRefusals.messageKey(path, reason);
        assertTrue(I18n.has(key), key);
      }
    }
  }

  @Test
  void mapsShopRefusalsAndEveryShopKeyExistsInTheEnglishCatalogue() {
    assertEquals("online.shop.not_enough_gold", OnlineCommandRefusals.messageKey("/shop-buy", "insufficient_gold"));
    assertEquals("online.shop.too_heavy", OnlineCommandRefusals.messageKey("/shop-buy", "too_heavy"));
    assertEquals("online.shop.not_enough_items", OnlineCommandRefusals.messageKey("/shop-sell", "item_unavailable"));
    assertEquals("online.shop.not_a_shop", OnlineCommandRefusals.messageKey("/shop-offers", "not_a_shop"));
    assertEquals("online.shop.refused", OnlineCommandRefusals.messageKey("/shop-sell", "something_new"));
    for (var path : List.of("/shop-offers", "/shop-buy", "/shop-sell")) {
      for (var reason : List.of("npc_unavailable", "not_a_shop", "not_for_sale", "insufficient_gold", "too_heavy",
          "item_unavailable", "not_sellable", "unsupported_item", "dead", "busy", "revision", "region_epoch",
          "anything-unknown")) {
        var key = OnlineCommandRefusals.messageKey(path, reason);
        assertTrue(I18n.has(key), key);
      }
    }
  }
}
