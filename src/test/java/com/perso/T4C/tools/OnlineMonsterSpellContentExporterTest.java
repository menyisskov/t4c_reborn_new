package com.perso.T4C.tools;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.divine.DivineAfflictions;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.online.model.MonsterSpellContent.Behavior;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class OnlineMonsterSpellContentExporterTest {
  @Test
  void exportsExactlyTheAuthoredMonsterSpellIdsWithOfflineBehaviorClassification() {
    var content = OnlineMonsterSpellContentExporter.snapshot();
    var ids = new TreeSet<Integer>();
    for (var definition : MonsterRegistry.load()) {
      if (definition.getAttacks() == null) continue;
      definition.getAttacks().stream().filter(java.util.Objects::nonNull)
          .filter(com.perso.T4C.monster.core.MonsterDef.Attack::isSpell)
          .map(com.perso.T4C.monster.core.MonsterDef.Attack::getSpellId).forEach(ids::add);
    }
    assertEquals(ids.size(), content.spells().size());
    for (int id : ids) {
      var exported = content.spells().get(Integer.toString(id));
      assertNotNull(exported, "Missing authored monster spell " + id);
      if (id == 10616) assertEquals(Behavior.SELF_HEAL, exported.behavior());
      else if (id == 10596) assertEquals(Behavior.TELEPORT, exported.behavior());
      else if (DivineAfflictions.isAffliction(id)) assertEquals(Behavior.AFFLICTION, exported.behavior());
      else if (id >= 18100 && id < 18200) assertEquals(Behavior.UNSUPPORTED, exported.behavior());
      else if (com.perso.T4C.monster.core.MonsterSpellRules.usesArmor(id))
        assertEquals(Behavior.ARMOR_DAMAGE, exported.behavior());
      else if (com.perso.T4C.monster.core.MonsterSpellRules.isElementalDamageSpell(id))
        assertEquals(Behavior.ELEMENTAL_DAMAGE, exported.behavior());
      else assertEquals(Behavior.UNSUPPORTED, exported.behavior());
    }
  }

  @Test
  void generatedResourceMatchesThePackagedMonsterSpellInventory() throws Exception {
    String expected = OnlineMonsterSpellContentExporter.render(OnlineMonsterSpellContentExporter.snapshot());
    assertEquals(
        expected,
        Files.readString(Path.of("src/main/resources/online/monster-spells-v1.json"), StandardCharsets.UTF_8)
            .replace("\r\n", "\n"));
    assertEquals(
        expected,
        Files.readString(
                Path.of("multiplayer/server/src/main/resources/content/monster-spells-v1.json"),
                StandardCharsets.UTF_8)
            .replace("\r\n", "\n"));
  }

  @Test
  void monsterOnlyFallbackDamageSpellsRetainTheirOfflineElements() {
    var spells = OnlineMonsterSpellContentExporter.snapshot().spells();

    assertEquals(Behavior.ELEMENTAL_DAMAGE, spells.get("10095").behavior());
    assertEquals("fire", spells.get("10095").element());
    assertEquals(Behavior.ELEMENTAL_DAMAGE, spells.get("10120").behavior());
    assertEquals("air", spells.get("10120").element());
    assertEquals(Behavior.ELEMENTAL_DAMAGE, spells.get("10086").behavior());
    assertEquals("water", spells.get("10086").element());
  }
}
