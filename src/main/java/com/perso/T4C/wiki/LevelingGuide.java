package com.perso.T4C.wiki;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.MonsterExperience;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.spawn.HuntingGrounds;
import com.perso.T4C.spawn.SpawnRegistry;
import java.util.*;

/**
 * Authored advice with live rewards, locations and progression estimates. Never changes a player.
 */
public final class LevelingGuide {
  private static final List<String> KEYS =
      List.of(
          "leveling", "leveling_1_150", "leveling_150_250", "leveling_250_350", "leveling_350_400");
  private static final int[][] LEVEL_RANGES = {
    {1, 400}, {1, 150}, {150, 250}, {250, 350}, {350, 400}
  };
  private static final Map<String, Integer> ORDER = buildOrder();
  public static final String OVERVIEW_ID =
      WikiCatalog.id(WikiCatalog.Category.GUIDES, KEYS.getFirst());

  private LevelingGuide() {}

  static List<WikiCatalog.Entry> entries(WikiCatalog catalog) {
    List<WikiCatalog.Entry> result = new ArrayList<>();
    for (int i = 0; i < KEYS.size(); i++) {
      int chapter = i;
      String key = KEYS.get(i);
      String title = I18n.key("wiki.guide." + key + ".title");
      String keywords = title + " " + key + " " + I18n.key("wiki.guide.search");
      StringBuilder search = new StringBuilder(keywords);
      for (int level = LEVEL_RANGES[i][0]; level <= LEVEL_RANGES[i][1]; level++)
        search.append(' ').append(level);
      result.add(
          new WikiCatalog.Entry(
              WikiCatalog.id(WikiCatalog.Category.GUIDES, key),
              WikiCatalog.Category.GUIDES,
              title,
              search.toString().toLowerCase(Locale.ROOT),
              () -> page(catalog, chapter)));
    }
    return List.copyOf(result);
  }

  static int order(String id) {
    return ORDER.getOrDefault(id, Integer.MAX_VALUE);
  }

  private static Map<String, Integer> buildOrder() {
    Map<String, Integer> order = new HashMap<>();
    for (int i = 0; i < KEYS.size(); i++)
      order.put(WikiCatalog.id(WikiCatalog.Category.GUIDES, KEYS.get(i)), i);
    return Map.copyOf(order);
  }

  private static WikiCatalog.Page page(WikiCatalog catalog, int chapter) {
    var a = new Article(catalog);
    a.text("wiki.guide." + KEYS.get(chapter) + ".title");
    if (chapter == 0) {
      a.text("wiki.guide.overview.route");
      a.text("wiki.guide.overview.readiness");
      a.text("wiki.guide.overview.classes");
      a.text("wiki.guide.overview.rebirth");
      a.text("wiki.guide.overview.benchmark");
      a.text("wiki.guide.live_explanation");
      for (int i = 1; i < KEYS.size(); i++) a.link(WikiCatalog.Category.GUIDES, KEYS.get(i));
      a.unlock("passage_to_kraanhold");
      a.unlock("passage_to_avalon");
      a.unlock("concord_embers");
      a.text("wiki.guide.overview.checkpoints");
      a.farm("Fey Warden", 200);
      a.farm("Moonlit Stalker", 225);
      a.farm("Veilbound Wraith", 250);
      a.farm("Nullguard", 350);
      a.farm("Rift Wraith", 399);
    } else if (chapter == 1) {
      a.text("wiki.guide.early.start");
      a.text("wiki.guide.early.middle");
      a.text("wiki.guide.early.finish");
      a.text("wiki.guide.early.training");
      a.farm("Brown Rat", 0);
      a.farm("Tarantula", 0);
      a.farm("Drowned Acolyte", 0);
      a.farm("Tideclaw Crab", 0);
      a.farm("Cinder Whelp", 0);
      a.farm("Ashfang Stalker", 0);
      a.farm("Barrow Wight", 150);
      a.quest("lighthaven_samaritan_rats");
      a.quest("ortanalas_bridge_goblins");
      a.unlock("passage_to_kraanhold");
      a.npc("ColosseumClerk");
    } else if (chapter == 2) {
      a.text("wiki.guide.middle.route");
      a.text("wiki.guide.middle.drakes");
      a.text("wiki.guide.middle.witness");
      a.text("wiki.guide.middle.concord");
      a.text("wiki.guide.middle.training");
      a.farm("Barrow Wight", 150);
      a.farm("Kraanian Wyrmling", 175);
      a.farm("Lesser Drake", 200);
      a.link(WikiCatalog.Category.MONSTERS, "Greater Drake");
      a.farm("Fey Warden", 200);
      a.farm("Moonlit Stalker", 225);
      a.farm("Moonwake Revenant", 225);
      a.farm("Training 1 Tarantula", 200);
      a.unlock("passage_to_kraanhold");
      a.unlock("passage_to_avalon");
      a.quest("tideworn_shore_scouts");
      a.quest("concord_charter");
      a.quest("concord_drowned");
      a.npc("ArchivistAveline");
    } else if (chapter == 3) {
      a.text("wiki.guide.late.route");
      a.text("wiki.guide.late.unlock");
      a.text("wiki.guide.late.training");
      a.text("wiki.guide.late.builds");
      a.farm("Veilbound Wraith", 250);
      a.farm("Emberglass Ashguard", 275);
      a.farm("Sundered Sentinel", 300);
      a.farm("Ashbound Exile", 315);
      a.farm("Nullguard", 350);
      for (String family : List.of("Tarantula", "Skraug", "Skeleton"))
        a.farm("Training 2 " + family, 300);
      a.unlock("concord_embers");
      a.unlock("pale_cantor");
      a.unlock("cinder_marshal");
      a.quest("concord_refuge");
      a.quest("concord_resonance");
      a.npc("MirrorwardenYsmera");
    } else {
      a.text("wiki.guide.endgame.route");
      a.text("wiki.guide.endgame.elites");
      a.text("wiki.guide.endgame.story");
      a.text("wiki.guide.endgame.check");
      a.farm("Nullguard", 350);
      a.farm("Rift Wraith", 375);
      a.farm("Kraanian Dragonguard", 399);
      a.quest("concord_regent");
      a.quest("concord_three_bells");
      a.quest("concord_dawn");
      a.quest("dusk_regent");
      a.quest("rift_unbinding");
    }
    if (chapter > 0) {
      a.text("wiki.guide.live_explanation");
      a.link(WikiCatalog.Category.GUIDES, KEYS.getFirst());
      if (chapter > 1) a.link(WikiCatalog.Category.GUIDES, KEYS.get(chapter - 1));
      if (chapter < 4) a.link(WikiCatalog.Category.GUIDES, KEYS.get(chapter + 1));
    }
    return new WikiCatalog.Page(a.text.toString().strip(), List.copyOf(a.links.values()));
  }

  private static final class Article {
    final WikiCatalog catalog;
    final XpCurve curve = XpCurve.loadDefault();
    final StringBuilder text = new StringBuilder();
    final Map<String, WikiCatalog.Link> links = new LinkedHashMap<>();

    Article(WikiCatalog catalog) {
      this.catalog = catalog;
    }

    void text(String key, Object... args) {
      text.append(I18n.message(key, args)).append("\n\n");
    }

    void line(String key, Object... args) {
      text.append(I18n.message(key, args)).append('\n');
    }

    void link(WikiCatalog.Category category, String key) {
      var entry = catalog.find(WikiCatalog.id(category, key));
      if (entry != null)
        links.putIfAbsent(entry.id(), new WikiCatalog.Link(entry.id(), entry.title()));
    }

    void npc(String key) {
      link(WikiCatalog.Category.NPCS, key);
    }

    void quest(String key) {
      link(WikiCatalog.Category.QUESTS, key);
    }

    void unlock(String key) {
      var q = QuestRegistry.findById(key);
      if (q == null) return;
      var n = NpcFactoryRegistry.find(q.getGiverNpc());
      String giver = n == null ? q.getGiverNpc() : I18n.resolve(n.displayName());
      String gate =
          q.getMinLevel() <= 0 ? "" : I18n.message("wiki.guide.level_gate", q.getMinLevel());
      text("wiki.guide.unlock", I18n.resolve(q.getTitle()), giver, gate);
      quest(key);
      npc(q.getGiverNpc());
    }

    void farm(String name, int referenceLevel) {
      var m = MonsterRegistry.findByName(name);
      if (m == null) return;
      String title = I18n.resolve(m.getDisplayName());
      long xp =
          Math.round(
              (MonsterExperience.reward(m.getXpOnDeath())
                      + MonsterExperience.damageReward(m.getXpPerHit(), m.getHealth()))
                  * (double) GameConstants.SERVER_XP_RATE);
      line("wiki.guide.farm", title, m.getHealth(), xp);
      link(WikiCatalog.Category.MONSTERS, m.getName());
      var field =
          HuntingGrounds.all().stream()
              .filter(g -> g.monster().equals(name) || name.equals(g.bossType()))
              .findFirst();
      if (field.isPresent()) {
        var g = field.get();
        line("wiki.guide.field", g.x(), g.y(), g.points().size());
        link(WikiCatalog.Category.AREAS, g.locationKey());
      } else {
        SpawnRegistry.monsters().stream()
            .filter(s -> s.type().equals(name))
            .findFirst()
            .ifPresent(s -> line("wiki.coordinates", s.x(), s.y(), s.z()));
      }
      if (m.getLevel() > GameConstants.MAX_PLAYER_LEVEL) {
        line("wiki.guide.above_cap", m.getLevel(), GameConstants.MAX_PLAYER_LEVEL);
      } else {
        long needed = curve.getXpToNextLevel(referenceLevel);
        if (referenceLevel > 0 && needed > 0 && xp > 0)
          line("wiki.guide.kills", referenceLevel, Math.ceilDiv(needed, xp));
      }
      text.append('\n');
    }
  }
}
