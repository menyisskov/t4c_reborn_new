package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import org.junit.jupiter.api.Test;

class OnlineEquipmentBonusesTest {
  @Test void shieldDoesNotSupplyOffHandDamageEvenWithDualWeaponTraining() throws Exception {
    var catalog = ItemCatalog.loadBundled();
    var initial = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new java.util.Random(7)).progress();
    var gson = new Gson();
    var saved = gson.toJsonTree(initial).getAsJsonObject();
    var shield = java.util.UUID.randomUUID();
    saved.getAsJsonArray("inventory").add(gson.toJsonTree(new CharacterProgress.Item(shield, "item.newbi_set_shield", 1, 0)));
    saved.getAsJsonObject("equipment").remove("WEAPON2");
    saved.getAsJsonObject("equipment").addProperty("SHIELD", shield.toString());
    saved.getAsJsonObject("skills").addProperty("two_weapons", 100);
    var progress = gson.fromJson(saved, CharacterProgress.class);
    var withShield = OnlineCombatLoadout.resolve(progress, catalog, 0, false);
    assertEquals(100, withShield.profile().skill("two_weapons"));
    assertNull(withShield.secondary());
    assertEquals(0, com.perso.T4C.combat.WeaponDamageRules.offHand(withShield.stats(),
        withShield.primary(), withShield.secondary(), withShield.damageBonus()));
    var withoutShield = OnlineCombatLoadout.resolve(OnlineEquipmentCommands.unequip(progress,
        com.perso.T4C.player.BodyPart.SHIELD), catalog, 0, false);
    assertTrue(withShield.profile().armorClass() > withoutShield.profile().armorClass(),
        "A shield must still contribute its defensive stats");
  }

  @Test void combatLoadoutCombinesOwnedGearAndSkillsWithoutCountingSecondaryLayers() throws Exception {
    var gson = new Gson();
    var content = gson.toJsonTree(ItemCatalog.loadBundled()).getAsJsonObject();
    var sword = content.getAsJsonObject("items").getAsJsonObject("item.recruits_greatsword");
    sword.addProperty("secondaryBodyPart", "WEAPON2");
    sword.addProperty("armorClass", 9);
    sword.addProperty("dodgeLost", 6);
    sword.add("boosts", gson.toJsonTree(java.util.List.of(
        new ItemContent.Boost(1, 3, "self.str/2", 0, 0),
        new ItemContent.Boost(2, 8, "3", 0, 0), new ItemContent.Boost(3, 10012, "4", 0, 0),
        new ItemContent.Boost(4, 10011, "4", 0, 0), new ItemContent.Boost(5, 20, "3", 0, 0),
        new ItemContent.Boost(6, 10, "5", 0, 0))));
    var catalog = gson.fromJson(content, ItemContent.class);
    var initial = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new java.util.Random(7)).progress();
    var saved = gson.toJsonTree(initial).getAsJsonObject();
    saved.add("equipment", gson.toJsonTree(java.util.Map.of("WEAPON", initial.equipment().get("WEAPON"))));
    saved.add("skills", gson.toJsonTree(java.util.Map.of("attack", 40, "dodge", 20, "archery", 15)));
    saved.addProperty("hiddenUntil", 200);
    var progress = gson.fromJson(saved, CharacterProgress.class);
    var loadout = OnlineCombatLoadout.resolve(progress, catalog, 100, true);
    assertEquals(progress.attributes().strength() + progress.attributes().strength()/2, loadout.stats().strength());
    assertEquals(47, loadout.profile().attack());
    assertEquals(18, loadout.profile().dodge());
    assertEquals(15, loadout.profile().archery());
    assertEquals(12, loadout.profile().armorClass());
    assertEquals(5, loadout.damageBonus());
    assertTrue(loadout.profile().stunned());
    assertTrue(loadout.profile().hidden());
    assertEquals(progress.skills(), loadout.profile().skills(), "Proc skills retain the offline trained-skill contract");
    assertNotNull(loadout.primary());
    assertNull(loadout.secondary(), "A mirrored two-handed layer cannot become an extra attack");

    saved.add("buffs", gson.toJsonTree(java.util.List.of(new CharacterProgress.Buff("spell.test", 150, 10, 10))));
    var buffed = gson.fromJson(saved, CharacterProgress.class);
    assertThrows(IllegalArgumentException.class, () -> OnlineCombatLoadout.resolve(buffed, catalog, 100, false));
    assertDoesNotThrow(() -> OnlineCombatLoadout.resolve(buffed, catalog, 150, false));
    saved.getAsJsonObject("questFlags").addProperty("worldevent.test.stage", 1);
    var ward = gson.fromJson(saved, CharacterProgress.class);
    assertThrows(IllegalArgumentException.class, () -> OnlineCombatLoadout.resolve(ward, catalog, 150, false),
        "Unresolved world wards cannot silently lose their armor contribution");
  }

  @Test void resolvesOwnedEquipmentAgainstCatalogAndRejectsUnknownSavedReferences() throws Exception {
    var gson = new Gson();
    var content = gson.toJsonTree(ItemCatalog.loadBundled()).getAsJsonObject();
    var sword = content.getAsJsonObject("items").getAsJsonObject("item.recruits_greatsword");
    sword.add("boosts", gson.toJsonTree(java.util.List.of(new ItemContent.Boost(1, 3, "self.str/2", 0, 0))));
    var catalog = gson.fromJson(content, ItemContent.class);
    var progress = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new java.util.Random(7)).progress();
    assertEquals(progress.attributes().strength()/2, OnlineEquipmentBonuses.resolve(progress, catalog).get(3));
    var saved = gson.toJsonTree(progress).getAsJsonObject();
    saved.getAsJsonObject("equipment").add("UNKNOWN_SLOT", saved.getAsJsonObject("equipment").remove("WEAPON"));
    var invalidSlot = gson.fromJson(saved, CharacterProgress.class);
    assertThrows(IllegalArgumentException.class, () -> OnlineEquipmentBonuses.resolve(invalidSlot, catalog));
    var missing = new java.util.HashMap<>(catalog.items());
    missing.remove("item.recruits_greatsword");
    assertThrows(IllegalArgumentException.class, () -> OnlineEquipmentBonuses.resolve(progress, new ItemContent(ItemContent.VERSION, missing)));
  }
}
