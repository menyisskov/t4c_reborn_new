package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import org.junit.jupiter.api.Test;

class OnlineEquipmentBonusesTest {
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
