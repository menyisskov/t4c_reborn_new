package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ItemCatalogTest {
  @Test void allProvisionedItemsResolveToImmutableRuntimeEquipmentDefinitions() throws Exception {
    var content = ItemCatalog.loadBundled();
    for (var type : CharacterClass.values()) {
      var progress = OnlineCharacterCreator.prepare(type, "female", false, new java.util.Random(7)).progress();
      for (var item : progress.inventory()) assertNotNull(content.items().get(item.key()), item.key());
    }
    var sword = content.items().get("item.recruits_greatsword");
    assertNotNull(sword.weapon().formula());
    assertEquals(sword.key(), sword.weapon().key());
    assertThrows(UnsupportedOperationException.class, () -> content.items().clear());
    assertThrows(UnsupportedOperationException.class, () -> sword.boosts().clear());
  }

  @Test void unsupportedVersionAndMismatchedRegistryKeyFailClosed() throws Exception {
    var json = new Gson().toJsonTree(ItemCatalog.loadBundled()).getAsJsonObject();
    json.addProperty("version", 999);
    assertThrows(IOException.class, () -> ItemCatalog.read(bytes(json.toString())));
    json.addProperty("version", 1);
    json.getAsJsonObject("items").getAsJsonObject("item.recruits_greatsword").addProperty("key", "item.other");
    assertThrows(IOException.class, () -> ItemCatalog.read(bytes(json.toString())));
  }

  private static ByteArrayInputStream bytes(String value) {
    return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
  }
}
