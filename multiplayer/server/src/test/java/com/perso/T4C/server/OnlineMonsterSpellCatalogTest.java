package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.MonsterSpellContent;
import com.perso.T4C.online.model.MonsterSpellContent.Behavior;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OnlineMonsterSpellCatalogTest {
  @Test
  void onlyExplicitDamageBehaviorsAreExecutable() {
    var content = new MonsterSpellContent(1, Map.of(
        "10091", new MonsterSpellContent.Spell(10091, "spell.mob_poison_arrow_spell", "water", Behavior.ELEMENTAL_DAMAGE),
        "10092", new MonsterSpellContent.Spell(10092, "spell.mob_physical", null, Behavior.ARMOR_DAMAGE),
        "10596", new MonsterSpellContent.Spell(10596, "spell.mob_teleport", null, Behavior.TELEPORT),
        "10616", new MonsterSpellContent.Spell(10616, "spell.mob_heal", null, Behavior.SELF_HEAL),
        "18200", new MonsterSpellContent.Spell(18200, "spell.divine_status", null, Behavior.AFFLICTION),
        "19999", new MonsterSpellContent.Spell(19999, null, null, Behavior.UNSUPPORTED)));
    var catalog = new OnlineMonsterSpellCatalog(content);

    assertEquals(Behavior.ELEMENTAL_DAMAGE, catalog.damageSpell(10091).behavior());
    assertEquals(Behavior.ARMOR_DAMAGE, catalog.damageSpell(10092).behavior());
    assertNull(catalog.damageSpell(10596));
    assertNull(catalog.damageSpell(10616));
    assertNull(catalog.damageSpell(18200));
    assertNull(catalog.damageSpell(19999));
    assertNull(catalog.damageSpell(99999));
  }

  @Test
  void bundledInventoryParsesAndKeepsUnsafeSpecialsFailClosed() throws Exception {
    var catalog = OnlineMonsterSpellCatalog.loadBundled();
    assertNotNull(catalog.damageSpell(10091));
    assertNotNull(catalog.damageSpell(10086), "Authored spell without a registry row uses its offline fallback element");
    assertNull(catalog.damageSpell(10596));
    assertNull(catalog.damageSpell(10616));
    assertNull(catalog.damageSpell(18100));
    assertNull(catalog.damageSpell(18200));
  }
}
