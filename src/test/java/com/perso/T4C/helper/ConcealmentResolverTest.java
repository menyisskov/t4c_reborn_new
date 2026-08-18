package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.perso.T4C.config.Paths;
import com.perso.T4C.player.BodyPart;
import java.io.File;
import java.util.EnumMap;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConcealmentResolverTest {
  @BeforeEach
  void reloadCatalog() {
    assumeTrue(new File(Paths.APPEARANCE_DEFAULTS_BIN).exists());
    AppearanceDefaultsCatalog.invalidate();
  }

  @Test
  void robeHidesLegsFeetAndExplicitBoot() {
    EnumMap<BodyPart, String> parts = standardParts("PupWhiteRobe");
    parts.put(BodyPart.BOOT, "PupLeatherBoots");
    ConcealmentResolver.applyConcealment(parts, EnumSet.of(BodyPart.BODY, BodyPart.BOOT));
    assertFalse(parts.containsKey(BodyPart.LEGS));
    assertFalse(parts.containsKey(BodyPart.FEET));
    assertFalse(parts.containsKey(BodyPart.BOOT));
  }

  @Test
  void clothSetKeepsArms() {
    EnumMap<BodyPart, String> parts = standardParts("PupBodyClothSet1");
    ConcealmentResolver.applyConcealment(parts);
    assertTrue(parts.containsKey(BodyPart.LEFT_ARM));
    assertTrue(parts.containsKey(BodyPart.RIGHT_ARM));
  }

  @Test
  void paletteVariantUsesBaseRuleAndTriggerSurvives() {
    EnumMap<BodyPart, String> parts = standardParts("PupWhiteRobe__pal7");
    ConcealmentResolver.applyConcealment(parts);
    assertTrue(parts.containsKey(BodyPart.BODY));
    assertFalse(parts.containsKey(BodyPart.LEGS));
    assertFalse(parts.containsKey(BodyPart.FEET));
  }

  private static EnumMap<BodyPart, String> standardParts(String body) {
    EnumMap<BodyPart, String> parts = new EnumMap<>(BodyPart.class);
    parts.put(BodyPart.BODY, body);
    parts.put(BodyPart.LEGS, "PupNakedLegs");
    parts.put(BodyPart.FEET, "PupNakedFoot");
    parts.put(BodyPart.LEFT_ARM, "PupNakedArmL");
    parts.put(BodyPart.RIGHT_ARM, "PupNakedArmR");
    return parts;
  }
}
