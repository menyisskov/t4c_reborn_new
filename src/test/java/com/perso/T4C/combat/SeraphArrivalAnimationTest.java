package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeraphArrivalAnimationTest {

  @Test
  void classifiesArtherkAndNephilimWingsFromOriginalClientCapeGroups() {
    assertEquals(SeraphArrivalAnimation.Kind.ARTHERK, SeraphArrivalAnimation.kindOf("PupSeraphWhiteWings"));
    assertEquals(SeraphArrivalAnimation.Kind.ARTHERK, SeraphArrivalAnimation.kindOf("ArchWings__pal2"));
    assertEquals(SeraphArrivalAnimation.Kind.NEPHILIM, SeraphArrivalAnimation.kindOf("PupSeraphBlackWings"));
    assertEquals(SeraphArrivalAnimation.Kind.NEPHILIM, SeraphArrivalAnimation.kindOf("DarkWings"));
    assertNull(SeraphArrivalAnimation.kindOf("NM_DechuWings"));
    assertNull(SeraphArrivalAnimation.kindOf((String) null));
  }

  @Test
  void hidesPlayerUntilOriginalRevealFramesThenUnlocksMovement() {
    SeraphArrivalAnimation.Kind artherk = SeraphArrivalAnimation.Kind.ARTHERK;
    assertFalse(SeraphArrivalAnimation.playerVisible(artherk, 24, true));
    assertTrue(SeraphArrivalAnimation.playerVisible(artherk, 25, true));
    assertFalse(SeraphArrivalAnimation.movementAllowed(artherk, 88, true));
    assertTrue(SeraphArrivalAnimation.movementAllowed(artherk, 89, true));

    SeraphArrivalAnimation.Kind nephilim = SeraphArrivalAnimation.Kind.NEPHILIM;
    assertFalse(SeraphArrivalAnimation.playerVisible(nephilim, 77, true));
    assertTrue(SeraphArrivalAnimation.playerVisible(nephilim, 78, true));
    assertEquals("64kSpellSeraphArrival-", artherk.spritePrefix());
    assertEquals("Seraph.wav", artherk.sound());
    assertEquals("64kSeraphArivalBlack-", nephilim.spritePrefix());
    assertEquals("Evil Seraph.wav", nephilim.sound());
  }
}
