package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.item.CarryCapacityRules;
import com.perso.T4C.online.model.CharacterProgress;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OnlineOverweightFixtureTest {
  @Test void everyStarterClassKeepsItsKitAndIsPushedOverTheLimit() throws Exception {
    var content = ItemCatalog.loadBundled();
    for (CharacterClass type : CharacterClass.values()) {
      var kit = OnlineCharacterCreator.prepare(type, "female", false, new Random(214)).progress();
      var fixture = OnlineOverweightFixture.size(kit, content);
      var heavy = OnlineOverweightFixture.apply(kit, fixture);

      assertEquals(kit.equipment(), heavy.equipment(), type + ": equipment slots are unchanged");
      assertTrue(heavy.inventory().containsAll(kit.inventory()), type + ": the whole kit stays in the inventory");
      for (UUID worn : kit.equipment().values()) {
        assertTrue(heavy.inventory().stream().anyMatch(i -> i.id().equals(worn)), type + ": every worn item still exists");
      }
      assertEquals(kit.inventory().size() + 1, heavy.inventory().size());
      if (type == CharacterClass.WARRIOR) {
        assertTrue(fixture.carriedBefore() <= fixture.maximum(), "the probe's warrior kit alone fits before the fixture");
      }

      long maximum = CarryCapacityRules.maximumWeight(OnlinePickupRules.effectiveStrength(heavy, content, 0L));
      assertEquals(maximum, fixture.maximum(), "equipment bonuses are part of the capacity");
      long carried = OnlinePickupRules.carriedWeight(heavy, content);
      assertEquals(fixture.carriedAfter(), carried);
      assertTrue(carried > maximum, type + ": strictly over the limit, so even a weightless drop is refused");
      long weight = content.items().get(fixture.itemKey()).weight();
      if (fixture.carriedBefore() <= maximum) {
        assertTrue(carried - weight <= maximum, type + ": one stack unit fewer would not be over (smallest stack)");
      } else {
        assertEquals(1, fixture.quantity(), type + ": a kit already over the limit still gets a single unit");
      }
    }
  }

  @Test void fixtureIsARealDroppableAuthoredItemAndPickupIsRefusedTooHeavy() throws Exception {
    var content = ItemCatalog.loadBundled();
    var item = OnlineOverweightFixture.fixtureItem(content);
    assertTrue(content.items().containsKey(item.key()));
    assertTrue(item.weight() > 0);
    assertFalse(item.unique());
    assertFalse(item.undroppable());

    var kit = OnlineCharacterCreator.prepare(CharacterClass.WARRIOR, "female", false, new Random(214)).progress();
    var heavy = OnlineOverweightFixture.apply(kit, OnlineOverweightFixture.size(kit, content));
    var owner = UUID.randomUUID();
    var drop = new OnlinePickupRules.Loot(UUID.randomUUID(), item.key(), 0, 0, 0, owner, 0, 10_000);
    var pickup = new OnlinePickupRules.Pickup(owner, 0, 0, 0, 5);
    assertEquals(Optional.of(OnlinePickupRules.Refusal.TOO_HEAVY),
        OnlinePickupRules.refusal(drop, pickup, heavy, content));
  }
}
