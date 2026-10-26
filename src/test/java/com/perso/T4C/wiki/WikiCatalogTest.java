package com.perso.T4C.wiki;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.item.*;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.spawn.HuntingGrounds;
import com.perso.T4C.spell.SpellRegistry;
import org.junit.jupiter.api.*;

class WikiCatalogTest {
  private static WikiCatalog catalog;

  @BeforeAll
  static void loadRuntimeContent() {
    ItemJsonLoader.loadAndRegister("assets/items");
    com.perso.T4C.monster.json.MonsterJsonLoader.loadAndRegister();
    catalog = new WikiCatalog();
  }

  @AfterAll
  static void restoreRegistries() {
    ItemRegistry.resetAdditionalDefinitions();
    MonsterRegistry.resetToGeneratedDefinitions();
  }

  @Test
  void everyRuntimeRegistryEntryHasAnArticleAndEveryLinkResolves() {
    for (var i : ItemRegistry.load())
      assertNotNull(catalog.find(WikiCatalog.id(WikiCatalog.Category.ITEMS, i.getKey())));
    for (var s : SpellRegistry.load())
      assertNotNull(catalog.find(WikiCatalog.id(WikiCatalog.Category.SPELLS, s.getKey())));
    for (var m : MonsterRegistry.load())
      assertNotNull(catalog.find(WikiCatalog.id(WikiCatalog.Category.MONSTERS, m.getName())));
    for (var n : NpcFactoryRegistry.registrations())
      assertNotNull(catalog.find(WikiCatalog.id(WikiCatalog.Category.NPCS, n.id())));
    for (var q : QuestRegistry.load())
      assertNotNull(catalog.find(WikiCatalog.id(WikiCatalog.Category.QUESTS, q.getId())));
    for (var entry : catalog.search(null, "")) {
      var page = assertDoesNotThrow(entry.page()::get, entry.id());
      assertNotNull(page.text());
      for (var link : page.links())
        assertNotNull(catalog.find(link.target()), entry.id() + " links to " + link.target());
    }
  }

  @Test
  void jsonOverridesReplaceTheWikiDefinitionAsTheyDoInGameplay() {
    var original = ItemRegistry.load().get(0);
    var json = new com.perso.T4C.item.json.ItemJsonDef();
    json.key = original.getKey();
    json.name = "Wiki override fixture";
    json.bodyPart = "RING1";
    json.armorClass = 123;
    try {
      ItemRegistry.registerAdditionalDefinitions(java.util.List.of(json.toItemDefinition()));
      var current =
          new WikiCatalog().find(WikiCatalog.id(WikiCatalog.Category.ITEMS, original.getKey()));
      assertEquals(json.name, current.title());
      assertTrue(current.page().get().text().contains("123"));
    } finally {
      ItemRegistry.resetAdditionalDefinitions();
      ItemJsonLoader.loadAndRegister("assets/items");
    }
  }

  @Test
  void trainingBossAndReturnScrollHaveReciprocalSourceAndFieldLinks() {
    var ground =
        HuntingGrounds.all().stream()
            .filter(g -> g.trainingTier() == 1 && "Skeleton".equals(g.family()))
            .findFirst()
            .orElseThrow();
    String boss = WikiCatalog.id(WikiCatalog.Category.MONSTERS, ground.bossType());
    String scroll =
        WikiCatalog.id(
            WikiCatalog.Category.ITEMS, ItemRegistry.findByKey(ground.scrollKey()).getKey());
    assertTrue(
        catalog.find(boss).page().get().links().stream()
            .anyMatch(l -> l.target().equals(scroll) && l.label().contains("100%")));
    assertTrue(
        catalog.find(scroll).page().get().links().stream().anyMatch(l -> l.target().equals(boss)));
    assertTrue(
        catalog.find(boss).page().get().links().stream()
            .anyMatch(
                l ->
                    l.target()
                        .equals(WikiCatalog.id(WikiCatalog.Category.AREAS, ground.locationKey()))));
  }

  @Test
  void searchIsCaseInsensitiveAndSpellPagesShowTheirRealEffectFormula() {
    var found = catalog.search(WikiCatalog.Category.SPELLS, "MOUNTAIN FIST");
    var spell = SpellRegistry.findByName("spell.mountains_fist");
    var entry = catalog.find(WikiCatalog.id(WikiCatalog.Category.SPELLS, spell.getKey()));
    assertTrue(found.contains(entry));
    assertFalse(catalog.search(WikiCatalog.Category.ITEMS, "MOUNTAIN FIST").contains(entry));
    var formula = spell.getT4cEffects().get(0).getParameters().get(0).getExpression();
    assertTrue(entry.page().get().text().contains(formula));
  }

  @Test
  void levelingOverviewOpensTheFourSearchableRangesAndRelatedWorldArticles() {
    var guides = catalog.search(WikiCatalog.Category.GUIDES, "");
    assertEquals(LevelingGuide.OVERVIEW_ID, guides.getFirst().id());
    var overview = catalog.find(LevelingGuide.OVERVIEW_ID).page().get();
    for (String range : java.util.List.of("1_150", "150_250", "250_350", "350_400")) {
      String id = WikiCatalog.id(WikiCatalog.Category.GUIDES, "leveling_" + range);
      assertTrue(overview.links().stream().anyMatch(link -> link.target().equals(id)));
      var chapter = catalog.find(id).page().get();
      assertFalse(chapter.text().contains("wiki.guide."), "Every guide string must resolve");
      assertFalse(chapter.text().contains("${"));
      assertTrue(
          chapter.links().stream()
              .anyMatch(link -> link.target().equals(LevelingGuide.OVERVIEW_ID)));
    }
    var middle = catalog.find(WikiCatalog.id(WikiCatalog.Category.GUIDES, "leveling_150_250"));
    assertTrue(catalog.search(WikiCatalog.Category.GUIDES, "levelling 225").contains(middle));
    var links = middle.page().get().links();
    for (String expected :
        java.util.List.of(
            WikiCatalog.id(WikiCatalog.Category.MONSTERS, "Fey Warden"),
            WikiCatalog.id(WikiCatalog.Category.AREAS, "hunting_fey_warden"),
            WikiCatalog.id(WikiCatalog.Category.QUESTS, "passage_to_avalon"),
            WikiCatalog.id(WikiCatalog.Category.NPCS, "ArchivistAveline"))) {
      assertTrue(links.stream().anyMatch(link -> link.target().equals(expected)), expected);
    }
  }

  @Test
  void guideArticlesResolveCurrentMonsterRewardsAfterARegistryOverride() {
    var original = MonsterRegistry.findByName("Fey Warden");
    var guide = catalog.find(WikiCatalog.id(WikiCatalog.Category.GUIDES, "leveling_150_250"));
    String before = guide.page().get().text();
    try {
      MonsterRegistry.registerAdditionalDefinitions(
          java.util.List.of(original.withXpOnDeath(1500000000)));
      var current = MonsterRegistry.findByName(original.getName());
      long reward =
          Math.round(
              (com.perso.T4C.monster.core.MonsterExperience.reward(current.getXpOnDeath())
                      + com.perso.T4C.monster.core.MonsterExperience.damageReward(
                          current.getXpPerHit(), current.getHealth()))
                  * (double) com.perso.T4C.config.GameConstants.SERVER_XP_RATE);
      String after = guide.page().get().text();
      assertNotEquals(before, after);
      assertTrue(after.contains(String.format(java.util.Locale.ROOT, "%,d", reward)));
      long kills =
          Math.ceilDiv(com.perso.T4C.helper.XpCurve.loadDefault().getXpToNextLevel(200), reward);
      assertTrue(after.contains(com.perso.T4C.i18n.I18n.message("wiki.guide.kills", 200, kills)));
    } finally {
      MonsterRegistry.resetToGeneratedDefinitions();
      com.perso.T4C.monster.json.MonsterJsonLoader.loadAndRegister();
    }
  }

  @Test
  void aboveCapChallengesAreNotPresentedAsOrdinaryLevelingFarms() {
    var middle =
        catalog.find(WikiCatalog.id(WikiCatalog.Category.GUIDES, "leveling_150_250")).page().get();
    var greater = MonsterRegistry.findByName("Greater Drake");
    long greaterXp = com.perso.T4C.monster.core.MonsterExperience.reward(greater.getXpOnDeath());
    assertFalse(
        middle
            .text()
            .contains(
                com.perso.T4C.i18n.I18n.message(
                    "wiki.guide.farm",
                    com.perso.T4C.i18n.I18n.resolve(greater.getDisplayName()),
                    greater.getHealth(),
                    greaterXp)));
    assertTrue(
        middle.links().stream()
            .anyMatch(
                link ->
                    link.target()
                        .equals(WikiCatalog.id(WikiCatalog.Category.MONSTERS, greater.getName()))));

    var endgame =
        catalog
            .find(WikiCatalog.id(WikiCatalog.Category.GUIDES, "leveling_350_400"))
            .page()
            .get()
            .text();
    var elite = MonsterRegistry.findByName("Kraanian Dragonguard");
    String card =
        endgame.substring(
            endgame.lastIndexOf(com.perso.T4C.i18n.I18n.resolve(elite.getDisplayName())));
    assertTrue(
        card.contains(
            com.perso.T4C.i18n.I18n.message(
                "wiki.guide.above_cap",
                elite.getLevel(),
                com.perso.T4C.config.GameConstants.MAX_PLAYER_LEVEL)));
    long kills =
        Math.ceilDiv(
            com.perso.T4C.helper.XpCurve.loadDefault().getXpToNextLevel(399),
            com.perso.T4C.monster.core.MonsterExperience.reward(elite.getXpOnDeath()));
    assertFalse(card.contains(com.perso.T4C.i18n.I18n.message("wiki.guide.kills", 399, kills)));
  }
}
