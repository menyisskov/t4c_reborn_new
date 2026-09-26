package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.player.Player;
import com.perso.T4C.spell.SpellData;
import java.util.List;
import org.junit.jupiter.api.Test;

class InventoryServiceTest {
  @Test
  void maximumWeightUsesEffectiveStrength() {
    Player player = new Player();
    player.setStrength(50);
    assertEquals(166L, InventoryService.maximumWeight(player));
    player.applyBuff(
        "Strength buff",
        "",
        "",
        null,
        true,
        List.of(new SpellData.SpellEffect("ATTRIBUTE", "strength", "50", "")));
    assertEquals(100, player.getEffectiveStrength());
    // T4C-0054: curve recalibrated (higher asymptote) so strength keeps mattering well past 1000;
    // this value moved from 250 to 307.
    assertEquals(307L, InventoryService.maximumWeight(player));
  }

  @Test
  void maximumWeightKeepsScalingAtHighStrength() {
    // T4C-0054: gear alone can hand out +600 strength per item, so a heavily-equipped or
    // rebirthed character routinely exceeds 1000 strength - the old 500/100 curve had already
    // saturated to ~91% of its ceiling by then, making further strength invisible. Assert the
    // recalibrated curve still grows meaningfully between 1000 and 3000.
    Player player = new Player();
    player.setStrength(1000);
    long atThousand = InventoryService.maximumWeight(player);
    player.setStrength(3000);
    long atThreeThousand = InventoryService.maximumWeight(player);
    assertEquals(1290L, atThousand);
    assertEquals(1690L, atThreeThousand);
    assertTrue(atThreeThousand - atThousand > 300L);
  }
}
