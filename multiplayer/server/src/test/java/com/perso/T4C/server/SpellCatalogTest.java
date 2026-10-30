package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.perso.T4C.online.model.SpellContent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SpellCatalogTest {
  @Test void bundledCatalogCastsOnlyTheSelfHealAndNeverGrantOnlySpells() throws Exception {
    var catalog = SpellCatalog.loadBundled();
    assertEquals(SpellContent.Use.CAST, catalog.castable("spell.heal_light").use());
    assertEquals(SpellContent.Use.GRANT_ONLY, catalog.require("spell.renew_armor").use());
    assertThrows(IllegalArgumentException.class, () -> catalog.castable("spell.renew_armor"));
    assertThrows(IllegalArgumentException.class, () -> catalog.castable("spell.not_authored"));
  }

  @Test void consumableHealingSpellsAreUsableOnlyThroughItemUse() throws Exception {
    var catalog = SpellCatalog.loadBundled();
    assertEquals(SpellContent.Use.ITEM, catalog.require("spell.item_potion_of_light_healing").use());
    assertThrows(IllegalArgumentException.class,
        () -> catalog.castable("spell.item_potion_of_light_healing"));
  }

  @Test void unknownVersionAndNonHealEffectOnACastableSpellFailBeforeStartup() throws Exception {
    var json = new Gson().fromJson(bundledJson(), JsonObject.class);
    json.addProperty("version", 999);
    assertThrows(IOException.class, () -> SpellCatalog.read(bytes(json.toString())));
    json.addProperty("version", 1);
    // A teleport or buff effect (any type other than heal or mana) must not become castable.
    var effect = json.getAsJsonObject("spells").getAsJsonObject("spell.heal_light")
        .getAsJsonArray("effects").get(0).getAsJsonObject();
    effect.addProperty("type", 7);
    assertThrows(IOException.class, () -> SpellCatalog.read(bytes(json.toString())));
  }

  @Test void manaPotionIsAConsumableButOnlyAsAManaRestore() throws Exception {
    var catalog = SpellCatalog.loadBundled();
    assertEquals(SpellContent.Use.ITEM, catalog.require("spell.item_potion_of_mana").use());
    assertThrows(IllegalArgumentException.class, () -> catalog.castable("spell.item_potion_of_mana"));
    // The same effect type aimed at any other attribute is a buff the server cannot run.
    var json = new Gson().fromJson(bundledJson(), JsonObject.class);
    var params = json.getAsJsonObject("spells").getAsJsonObject("spell.item_potion_of_mana")
        .getAsJsonArray("effects").get(0).getAsJsonObject().getAsJsonArray("params");
    params.get(1).getAsJsonObject().addProperty("expression", "Strength");
    assertThrows(IOException.class, () -> SpellCatalog.read(bytes(json.toString())));
  }

  @Test void modeledBuffPotionsAreItemOnlyAndUnknownBuffsRemainRejected() throws Exception {
    var catalog = SpellCatalog.loadBundled();
    assertEquals(SpellContent.Use.ITEM, catalog.require("spell.item_potion_of_fury").use());
    assertThrows(IllegalArgumentException.class, () -> catalog.castable("spell.item_potion_of_fury"));
    assertEquals(SpellContent.Use.ITEM, catalog.require("spell.item_potion_of_nimbleness").use());
    assertThrows(IllegalArgumentException.class,
        () -> catalog.castable("spell.item_potion_of_nimbleness"));

    var invalidAttributeJson = new Gson().fromJson(bundledJson(), JsonObject.class);
    var params = invalidAttributeJson.getAsJsonObject("spells").getAsJsonObject("spell.item_potion_of_nimbleness")
        .getAsJsonArray("effects").get(0).getAsJsonObject().getAsJsonArray("params");
    params.get(1).getAsJsonObject().addProperty("expression", "unmodelled attribute");
    assertThrows(IOException.class, () -> SpellCatalog.read(bytes(invalidAttributeJson.toString())));

    var unknownBuffJson = new Gson().fromJson(bundledJson(), JsonObject.class);
    var spells = unknownBuffJson.getAsJsonObject("spells");
    var unknownBuff = new Gson().fromJson(
        new Gson().toJsonTree(spells.getAsJsonObject("spell.item_potion_of_nimbleness")), JsonObject.class);
    unknownBuff.addProperty("key", "spell.item_unapproved_agility_buff");
    spells.add("spell.item_unapproved_agility_buff", unknownBuff);
    assertThrows(IOException.class, () -> SpellCatalog.read(bytes(unknownBuffJson.toString())));
  }

  private static String bundledJson() throws IOException {
    try (var input = SpellCatalog.class.getResourceAsStream("/content/spells-v1.json")) {
      assertNotNull(input, "Missing bundled spell content");
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static ByteArrayInputStream bytes(String value) {
    return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
  }
}
