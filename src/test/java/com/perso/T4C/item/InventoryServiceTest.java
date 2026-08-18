package com.perso.T4C.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    assertEquals(250L, InventoryService.maximumWeight(player));
  }
}
