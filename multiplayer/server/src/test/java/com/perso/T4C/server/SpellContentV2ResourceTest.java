package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.online.model.SpellContentV2;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** spells-v2 ships beside the live v1 allowlist and agrees with it; nothing serves it yet. */
class SpellContentV2ResourceTest {
  private static SpellContentV2 v2() throws Exception {
    try (var input = SpellCatalog.class.getResourceAsStream("/content/spells-v2.json")) {
      assertNotNull(input, "Missing bundled spells-v2 content");
      return new Gson().fromJson(new String(input.readAllBytes(), StandardCharsets.UTF_8), SpellContentV2.class);
    }
  }

  @Test void bundledV2CoversAllPlayerSpellsWithTheFortyEightStageOneSpells() throws Exception {
    var content = v2();
    assertEquals(2, content.version());
    // 132 player spells plus item spells (spell.item_*, one per online-usable potion effect), which grow with item parity work.
    long itemSpells = content.spells().keySet().stream().filter(key -> key.startsWith("spell.item_")).count();
    assertEquals(132 + itemSpells, content.spells().size());
    assertEquals(48, content.spells().values().stream().filter(spell -> spell.stage() == 1).count());
  }

  @Test void everyV1SpellThatIsAlsoAPlayerSpellKeepsItsFormulasInV2() throws Exception {
    var v2 = v2();
    var catalog = SpellCatalog.loadBundled();
    int compared = 0;
    for (var old : catalog.spells().values()) {
      var spell = v2.spells().get(old.key());
      if (spell == null) continue; // potions and other item spells are not player spells
      assertEquals(old.effects(), spell.effects(), old.key());
      assertEquals(old.manaCost(), spell.manaCost(), old.key());
      assertEquals(old.targetType(), spell.targetType(), old.key());
      compared++;
    }
    assertTrue(compared >= 2, "heal_light and renew_armor must be in both: " + compared);
  }

  @Test void liveAllowlistBehaviourIsUnchangedByThePresenceOfV2() throws Exception {
    var catalog = SpellCatalog.loadBundled();
    assertEquals(SpellContent.Use.CAST, catalog.castable("spell.heal_light").use());
    assertThrows(IllegalArgumentException.class, () -> catalog.castable("spell.fire_dart"));
  }
}
