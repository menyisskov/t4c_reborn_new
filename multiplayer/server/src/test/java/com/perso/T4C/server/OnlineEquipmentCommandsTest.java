package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.player.BodyPart;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineEquipmentCommandsTest {
  @Test void replacingTwoHandedEquipmentPreservesEveryOwnedInstance() throws Exception {
    var catalog = ItemCatalog.loadBundled();
    var progress = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new java.util.Random(1)).progress();
    var gson = new Gson();
    var json = gson.toJsonTree(progress).getAsJsonObject();
    String twoHanded = catalog.items().values().stream().filter(item -> item.bodyPart() == BodyPart.WEAPON
        && item.secondaryBodyPart() == BodyPart.WEAPON2).map(item -> item.key()).sorted().findFirst().orElseThrow();
    for (var item : json.getAsJsonArray("inventory")) {
      if (item.getAsJsonObject().get("id").getAsString().equals(progress.equipment().get("WEAPON").toString()))
        item.getAsJsonObject().addProperty("key", twoHanded);
    }
    var shieldId = UUID.randomUUID();
    json.getAsJsonArray("inventory").add(gson.toJsonTree(new CharacterProgress.Item(shieldId, "item.newbi_set_shield", 1, 0)));
    progress = gson.fromJson(json, CharacterProgress.class);
    var changed = OnlineEquipmentCommands.equip(progress, catalog, shieldId, BodyPart.SHIELD);
    assertFalse(changed.equipment().containsKey("WEAPON"), "Shield displaces the two-handed weapon");
    assertEquals(shieldId, changed.equipment().get("SHIELD"));
    assertEquals(progress.inventory(), changed.inventory());
    var removed = OnlineEquipmentCommands.unequip(changed, BodyPart.SHIELD);
    assertEquals(progress.inventory(), removed.inventory());
    assertFalse(removed.equipment().containsValue(shieldId));
  }

  @Test void ownershipSlotsAndBaseAttributeRequirementsAreEnforcedBeforeChangingState() throws Exception {
    var catalog = ItemCatalog.loadBundled();
    var initial = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new java.util.Random(1)).progress();
    var weapon = initial.equipment().get("WEAPON");
    var progress = OnlineEquipmentCommands.unequip(initial, BodyPart.WEAPON);
    assertThrows(PreviewWorld.Rejected.class, () -> OnlineEquipmentCommands.equip(progress, catalog, UUID.randomUUID(), BodyPart.WEAPON));
    assertThrows(PreviewWorld.Rejected.class, () -> OnlineEquipmentCommands.equip(progress, catalog, weapon, BodyPart.HEAD));
    var gson = new Gson();
    var json = gson.toJsonTree(progress).getAsJsonObject();
    json.getAsJsonObject("attributes").addProperty("strength", 1);
    var weak = gson.fromJson(json, CharacterProgress.class);
    assertThrows(PreviewWorld.Rejected.class, () -> OnlineEquipmentCommands.equip(weak, catalog, weapon, BodyPart.WEAPON));
    assertEquals(initial.equipment(), OnlineEquipmentCommands.equip(progress, catalog, weapon, BodyPart.WEAPON).equipment());

    var withArrow = gson.toJsonTree(progress).getAsJsonObject();
    var arrowId = UUID.randomUUID();
    withArrow.getAsJsonArray("inventory").add(gson.toJsonTree(new CharacterProgress.Item(arrowId, "item.wooden_arrow", 1, 0)));
    var arrowEquipped = OnlineEquipmentCommands.equip(gson.fromJson(withArrow, CharacterProgress.class), catalog, arrowId, BodyPart.SHIELD);
    assertThrows(PreviewWorld.Rejected.class, () -> OnlineEquipmentCommands.equip(arrowEquipped, catalog, weapon, BodyPart.WEAPON),
        "The shield alias cannot bypass the quiver/main-hand incompatibility");
  }
}
