package com.perso.T4C.monster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterPuppetDress;
import com.perso.T4C.player.BodyPart;
import org.junit.jupiter.api.Test;

class RunawayPatientAppearanceTest {

  @Test
  void originalPuppetDressUsesClothBootsAndStaffAppearanceGroups() {
    MonsterDef def = RunawayPatient.definition();
    assertEquals(10011, def.getAppearance());
    assertEquals(285, def.getItemBody());
    assertEquals(260, def.getItemFeet());
    assertEquals(284, def.getItemLegs());
    assertEquals(118, def.getItemWeapon());
    assertEquals(BodyPart.BODY, MonsterPuppetDress.find(285).getBodyPart());
    assertEquals(BodyPart.FEET, MonsterPuppetDress.find(260).getBodyPart());
    assertEquals(BodyPart.LEGS, MonsterPuppetDress.find(284).getBodyPart());
    ItemDefinition staff = MonsterPuppetDress.find(118);
    assertNotNull(staff);
    assertEquals(BodyPart.WEAPON, staff.getBodyPart());
    assertEquals("PupWoodenStaff", staff.getAppearanceEquippedPrimary());
  }
}
