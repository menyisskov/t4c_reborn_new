package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterPuppetDress;
import com.perso.T4C.player.BodyPart;
import org.junit.jupiter.api.Test;

class MonsterPuppetDressTest {

  @Test
  void dressAppearanceGroupsResolveToEquippedVisualsNotStubItems() {
    assertWeapon(118, "PupWoodenStaff");
    assertWeapon(274, "PupBattleDagger");
    assertWeapon(277, "PupBattleSword");
    assertEquals(BodyPart.BODY, MonsterPuppetDress.find(285).getBodyPart());
    assertEquals("PupBodyClothSet1", MonsterPuppetDress.find(285).getAppearanceEquippedPrimary());
    assertTrue(MonsterPuppetDress.hasEquippedVisual(MonsterPuppetDress.find(118)));
  }

  @Test
  void remortPatientsKeepOriginalClothAndStaffDress() {
    assertDress(RunawayPatient.definition(), 285, 260, 284, 118);
    assertDress(DerangedOrderly.definition(), 285, 260, 284, 118);
    assertDress(MadMan.definition(), 285, 260, 284, 447);
  }

  private static void assertWeapon(int appearanceGroup, String sprite) {
    ItemDefinition item = MonsterPuppetDress.find(appearanceGroup);
    assertNotNull(item, "missing dress visual for " + appearanceGroup);
    assertEquals(BodyPart.WEAPON, item.getBodyPart());
    assertEquals(sprite, item.getAppearanceEquippedPrimary());
  }

  private static void assertDress(MonsterDef def, int body, int feet, int legs, int weapon) {
    assertEquals(body, def.getItemBody());
    assertEquals(feet, def.getItemFeet());
    assertEquals(legs, def.getItemLegs());
    assertEquals(weapon, def.getItemWeapon());
  }
}
