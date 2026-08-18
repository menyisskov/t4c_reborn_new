package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class NpcOriginalAppearanceTest {
  @Test
  void keepsSimpleAndMultipartAppearancesDistinct() {
    assertEquals("Demon", NpcOriginalAppearance.forName("Balork").spriteBase());
    assertEquals("PaysanModel1", NpcOriginalAppearance.forName("LighthavenSamaritan").spriteBase());
    assertEquals(List.of(), NpcOriginalAppearance.forName("LighthavenSamaritan").parts());
    var moonrock = NpcOriginalAppearance.forName("Moonrock");
    assertNull(moonrock.spriteBase());
    assertEquals(
        List.of("BODY:WoWhiteRobe", "FEET:WoLeatherBoots", "WEAPON:PupWoodenStaff"),
        parts(moonrock));
  }

  @Test
  void restoresExactNpcSpecificEquipmentInsteadOfGenericDefaults() {
    assertEquals(
        List.of(
            "BODY:PupChainMailBody",
            "FEET:PupPlateFoot",
            "LEFT_HAND:PupLeatherGloveL",
            "RIGHT_HAND:PupLeatherGloveR",
            "LEGS:PupLeatherPants"),
        parts(NpcOriginalAppearance.forName("Markam")));
    assertEquals(
        List.of(
            "BODY:PupLeatherBody",
            "FEET:PupLeatherBoots",
            "LEFT_HAND:PupLeatherGloveL",
            "RIGHT_HAND:PupLeatherGloveR",
            "LEGS:PupStuddedLegs",
            "WEAPON:PupBattleSword",
            "SHIELD:PupRomanShield"),
        parts(NpcOriginalAppearance.forName("Ortanalas")));
  }

  private static List<String> parts(NpcOriginalAppearance.Appearance appearance) {
    return appearance.parts().stream()
        .map(part -> part.getBodyPart() + ":" + part.getSpriteBase())
        .toList();
  }
}
