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

  @Test void shapesTheOnlineRulesDoNotRunAreRefused() throws Exception {
    var items = ItemCatalog.loadBundled();
    var monsters = MonsterCatalog.loadBundled();
    var spells = SpellCatalog.loadBundled();
    var base = quest("Skeleton", null, "spell.renew_armor");
    assertThrows(IllegalArgumentException.class,
        () -> new QuestCatalog(content(variant(base, "story.flag", 12, base.giverNpc())), monsters, items, spells));
    assertThrows(IllegalArgumentException.class,
        () -> new QuestCatalog(content(variant(base, null, 0, base.giverNpc())), monsters, items, spells));
  }

  @Test void twoQuestsCannotShareOneGiverBecauseOnlyTheFirstWouldBeReachable() throws Exception {
    var first = quest("Skeleton", null, "spell.renew_armor");
    var second = new QuestContent.Quest("second_quest", first.title(), first.giverNpc(), first.targetMonster(),
        first.requiredKills(), first.targetWorldZ(), first.areaCenterX(), first.areaCenterY(),
        first.areaRadiusTiles(), first.rewardGold(), first.rewardXp(), first.offerText(),
        first.completionText(), first.completedText(), null, null, 0, null, 0, null);
    var both = new java.util.LinkedHashMap<String, QuestContent.Quest>();
    both.put(first.id(), first);
    both.put(second.id(), second);
    assertThrows(IllegalArgumentException.class,
        () -> new QuestCatalog(new QuestContent(QuestContent.VERSION, both),
            MonsterCatalog.loadBundled(), ItemCatalog.loadBundled(), SpellCatalog.loadBundled()));
  }

  private static QuestContent.Quest variant(QuestContent.Quest q, String activationFlag, int kills, String giver) {
    return new QuestContent.Quest(q.id(), q.title(), giver, q.targetMonster(), kills, q.targetWorldZ(),
        q.areaCenterX(), q.areaCenterY(), q.areaRadiusTiles(), q.rewardGold(), q.rewardXp(), q.offerText(),
        q.completionText(), q.completedText(), activationFlag, null, 0, null, 0, q.rewardSpellKey());
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
