package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.online.model.QuestContent;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QuestCatalogTest {
  @Test void bundledRenewedWardsResolvesItsMonsterAndRewardSpell() throws Exception {
    var catalog = QuestCatalog.loadBundled();
    var definition = catalog.definition("renewed_wards");
    assertNotNull(definition);
    assertEquals("Skeleton", definition.getTargetMonster());
    assertEquals(12, definition.getRequiredKills());
    assertEquals("spell.renew_armor", definition.getRewardSpellKey());
    assertNull(definition.getRequiredItemKey());
  }

  @Test void bareRequiredItemKeyIsRefusedBecauseInventoryEntriesAreNormalized() throws Exception {
    var items = ItemCatalog.loadBundled();
    var monsters = MonsterCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    assertThrows(IllegalArgumentException.class,
        () -> new QuestCatalog(content(quest("Skeleton", "tideworn_avalon_chart", "spell.renew_armor")),
            monsters, items, spells));
  }

  @Test void unknownMonsterAndUnknownRewardSpellAreRefused() throws Exception {
    var items = ItemCatalog.loadBundled();
    var monsters = MonsterCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    assertThrows(IllegalArgumentException.class,
        () -> new QuestCatalog(content(quest("NoSuchMonster", null, "spell.renew_armor")), monsters, items, spells));
    assertThrows(IllegalArgumentException.class,
        () -> new QuestCatalog(content(quest("Skeleton", null, "spell.not_authored")), monsters, items, spells));
  }

  private static QuestContent content(QuestContent.Quest quest) {
    return new QuestContent(QuestContent.VERSION, Map.of(quest.id(), quest));
  }

  private static QuestContent.Quest quest(String monster, String requiredItemKey, String rewardSpellKey) {
    return new QuestContent.Quest("renewed_wards", "${quest.renewed_wards.title}", "WardenAelric",
        monster, 12, 0, 2785, 1095, 50, 500, 400, "${quest.renewed_wards.offer}",
        "${quest.renewed_wards.completion}", "${quest.renewed_wards.completed}", null,
        requiredItemKey, requiredItemKey == null ? 0 : 1, null, 0, rewardSpellKey);
  }
}
