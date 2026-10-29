package com.perso.T4C.online.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Only worn items that draw something are public; nothing else of the character is. */
class PublicLookTest {
  private static final Gson GSON = new Gson();

  private static ItemContent.Definition definition(String key, String body, String appearance) {
    return GSON.fromJson(
        "{\"key\":\"" + key + "\",\"name\":\"" + key + "\",\"bodyPart\":\"" + body + "\","
            + (appearance == null ? "" : "\"appearanceEquippedPrimary\":\"" + appearance + "\",")
            + "\"boosts\":[],\"spells\":[]}",
        ItemContent.Definition.class);
  }

  private static CharacterProgress progress(List<CharacterProgress.Item> items, Map<String, UUID> worn) {
    return new CharacterProgress(
        1, "warrior", "male", new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(10, 20, 5, 10), new CharacterProgress.Advancement(1, 0, 0, 0, 0),
        items, worn, Map.of("secret_skill", 3), java.util.Set.of(), Map.of("secret_flag", 1), List.of(), 0,
        new CharacterProgress.Respawn(0, 1, 1), List.of(), Map.of(), 0, null);
  }

  @Test
  void onlyVisibleWornItemsAreListedAndCarriedGearIsNot() {
    var robe = new CharacterProgress.Item(UUID.randomUUID(), "item.robe", 1, 0);
    var ring = new CharacterProgress.Item(UUID.randomUUID(), "item.ring", 1, 0);
    var spare = new CharacterProgress.Item(UUID.randomUUID(), "item.spare_helm", 1, 0);
    var unknown = new CharacterProgress.Item(UUID.randomUUID(), "item.gone", 1, 0);
    var items =
        new ItemContent(
            ItemContent.VERSION,
            Map.of(
                "item.robe", definition("item.robe", "BODY", "PupWhiteRobe"),
                "item.ring", definition("item.ring", "RING1", null),
                "item.spare_helm", definition("item.spare_helm", "HEAD", "PupHelm")));
    var worn = Map.of("BODY", robe.id(), "RING1", ring.id(), "LEGS", unknown.id());
    var look = PublicLook.visibleEquipment(progress(List.of(robe, ring, spare, unknown), worn), items);
    assertEquals(Map.of("BODY", "item.robe"), look);
  }

  @Test
  void noProgressOrNoCatalogueMeansNothingIsPublished() {
    assertTrue(PublicLook.visibleEquipment(null, null).isEmpty());
    assertTrue(PublicLook.visibleEquipment(progress(List.of(), Map.of()), null).isEmpty());
  }
}
