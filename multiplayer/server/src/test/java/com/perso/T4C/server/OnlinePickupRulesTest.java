package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.item.CarryCapacityRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlinePickupRulesTest {
  private static final UUID OWNER = UUID.randomUUID();
  private static final UUID OTHER = UUID.randomUUID();
  private static final String GLOVES = "item.leather_gloves";
  private static final String RING = "item.ring_of_confidence";

  @Test void rangeOwnerWindowAndExpiryFollowTheCommittedRules() throws Exception {
    var content = ItemCatalog.loadBundled();
    var progress = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(7)).progress();
    var loot = new OnlinePickupRules.Loot(UUID.randomUUID(), GLOVES, 1, 10, 10, OWNER, 1_000, 5_000);
    assertEquals(Optional.empty(), OnlinePickupRules.refusal(loot,
        new OnlinePickupRules.Pickup(OWNER, 1, 18, 10, 500), progress, content), "distance 8 is in range");
    assertEquals(Optional.of(OnlinePickupRules.Refusal.OUT_OF_RANGE), OnlinePickupRules.refusal(loot,
        new OnlinePickupRules.Pickup(OWNER, 1, 19, 10, 500), progress, content), "distance 9 is refused");
    assertEquals(Optional.of(OnlinePickupRules.Refusal.LOOT_RESERVED), OnlinePickupRules.refusal(loot,
        new OnlinePickupRules.Pickup(OTHER, 1, 10, 10, 999), progress, content), "other players wait for the window");
    assertEquals(Optional.empty(), OnlinePickupRules.refusal(loot,
        new OnlinePickupRules.Pickup(OTHER, 1, 10, 10, 1_000), progress, content), "the window has closed");
    assertEquals(Optional.of(OnlinePickupRules.Refusal.LOOT_UNAVAILABLE), OnlinePickupRules.refusal(loot,
        new OnlinePickupRules.Pickup(OWNER, 1, 10, 10, 5_000), progress, content), "expired loot is never claimable");
  }

  @Test void carryLimitCountsEachEquippedInstanceOnce() throws Exception {
    var gson = new Gson();
    var base = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(7)).progress();
    var saved = gson.toJsonTree(base).getAsJsonObject();
    var worn = UUID.randomUUID();
    saved.getAsJsonArray("inventory").add(gson.toJsonTree(new CharacterProgress.Item(worn, GLOVES, 1, 0)));
    saved.getAsJsonObject("equipment").addProperty("SHIELD", worn.toString());
    var progress = gson.fromJson(saved, CharacterProgress.class);

    var json = gson.toJsonTree(ItemCatalog.loadBundled()).getAsJsonObject();
    json.getAsJsonObject("items").getAsJsonObject(GLOVES).addProperty("weight", 10);
    var content = gson.fromJson(json, ItemContent.class);
    var maximum = CarryCapacityRules.maximumWeight(OnlinePickupRules.effectiveStrength(progress, content, 0));
    long current = OnlinePickupRules.carriedWeight(progress, content);
    assertEquals(OnlinePickupRules.carriedWeight(base, content) + 10, current,
        "the worn gloves are one instance, counted once");
    assertTrue(current <= maximum, "the starting kit alone must fit");
    var pickup = new OnlinePickupRules.Pickup(OWNER, 1, 0, 0, 0);
    var loot = new OnlinePickupRules.Loot(UUID.randomUUID(), RING, 1, 0, 0, OWNER, 1_000, 5_000);

    json.getAsJsonObject("items").getAsJsonObject(RING).addProperty("weight", maximum - current);
    assertEquals(Optional.empty(), OnlinePickupRules.refusal(loot, pickup,
        progress, gson.fromJson(json, ItemContent.class)), "the total can reach the limit exactly");
    json.getAsJsonObject("items").getAsJsonObject(RING).addProperty("weight", maximum - current + 1);
    assertEquals(Optional.of(OnlinePickupRules.Refusal.TOO_HEAVY), OnlinePickupRules.refusal(loot, pickup,
        progress, gson.fromJson(json, ItemContent.class)), "one unit over the limit is refused");
  }

  @Test void uniqueFlaggedGroundItemsMayBePickedUpAsAdditionalCopies() throws Exception {
    var gson = new Gson();
    var progress = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "male", false, new Random(7)).progress();
    var json = gson.toJsonTree(ItemCatalog.loadBundled()).getAsJsonObject();
    json.getAsJsonObject("items").getAsJsonObject(RING).addProperty("unique", true);
    var loot = new OnlinePickupRules.Loot(UUID.randomUUID(), RING, 1, 0, 0, OWNER, 1_000, 5_000);
    var pickup = new OnlinePickupRules.Pickup(OWNER, 1, 0, 0, 500);

    assertEquals(Optional.empty(), OnlinePickupRules.refusal(loot, pickup,
        progress, gson.fromJson(json, ItemContent.class)));
  }
}
