package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonDef;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

class CombatMathTest {
  @Test
  void equippedQuiverFeedsArrowDamageIntoBowFormulaOnly() throws Exception {
    var bow = new ItemJsonDef();
    bow.key = "test_arrow_bow";
    bow.bodyPart = "WEAPON";
    bow.isBow = true;
    bow.dmgFormula = "10+3*arrow_dmg";
    var quiver = new ItemJsonDef();
    quiver.key = "test_arrow_quiver";
    quiver.bodyPart = "WEAPON2";
    quiver.structure = 8;
    quiver.dmgFormula = "7";
    ItemRegistry.registerAdditionalDefinitions(
        List.of(bow.toItemDefinition(), quiver.toItemDefinition()));
    try {
      Player player = new Player();
      player.setStrength(20);
      player.setDexterity(20);
      player.getEquippedItems().put(BodyPart.WEAPON, "item.test_arrow_bow");
      assertEquals(10, CombatMath.computeBowDamage(player));
      player.getEquippedItems().put(BodyPart.WEAPON2, "item.test_arrow_quiver");
      assertEquals(31, CombatMath.computeBowDamage(player));
      quiver.structure = 2;
      ItemRegistry.registerAdditionalDefinitions(List.of(quiver.toItemDefinition()));
      assertEquals(10, CombatMath.computeBowDamage(player));
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
    }
  }
}
