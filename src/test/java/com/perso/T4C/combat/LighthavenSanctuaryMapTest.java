package com.perso.T4C.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.death.DeathPenaltyService;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.CollisionType;
import java.io.File;
import org.junit.jupiter.api.Test;

class LighthavenSanctuaryMapTest {
  @Test
  void templeProtectsPvpWhileTownAndTempleAllowPve() throws Exception {
    CollisionReader map = new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
    int temple = map.getCollision(2961, 1058); // Kilhiam stands inside the temple.
    int town = map.getCollision(2961, 1093); // Mithrand stands at the bank.

    assertEquals(CollisionType.PVP_SANCTUARY.getValue(), temple);
    assertFalse(DeathPenaltyService.isSafeHaven(temple));
    assertTrue(DeathPenaltyService.isPvpSafeHaven(temple));
    assertFalse(map.hasCollision(2961, 1058));
    assertFalse(DeathPenaltyService.isPvpSafeHaven(town));
    assertFalse(map.hasCollision(2961, 1093));
  }
}
