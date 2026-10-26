package com.perso.T4C.wiki;

import com.perso.T4C.config.GameConstants;
import com.perso.T4C.divine.DivinePantheon;
import com.perso.T4C.gui.widget.ItemTooltipText;
import com.perso.T4C.gui.widget.SpellTooltipText;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.*;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.npc.core.*;
import com.perso.T4C.quest.*;
import com.perso.T4C.spawn.*;
import com.perso.T4C.spell.*;
import com.perso.T4C.teleport.*;
import java.util.*;
import java.util.function.Supplier;

/** Read-only snapshot of the loaded runtime registries. Pages are built only when opened. */
public final class WikiCatalog {
  public enum Category {
    GUIDES,
    ITEMS,
    SPELLS,
    MONSTERS,
    NPCS,
    QUESTS,
    AREAS
  }

  public record Link(String target, String label) {}

  public record Page(String text, List<Link> links) {
    public Page {
      links = List.copyOf(links);
    }
  }

  public record Entry(
      String id, Category category, String title, String searchText, Supplier<Page> page) {}

  private final Map<String, Entry> entries = new LinkedHashMap<>();
  private final Map<String, List<MonsterDef>> drops = new HashMap<>();
  private final Map<String, List<SpawnDefinition>> monsterSpawns = new HashMap<>();
  private final Map<String, List<SpawnDefinition>> npcSpawns = new HashMap<>();
  private final Map<String, List<String>> npcItemSources = new HashMap<>();
  private final Map<String, List<String>> teachers = new HashMap<>();
  private final List<Entry> sorted;

  public WikiCatalog() {
    for (var m :
        MonsterRegistry.load().stream()
            .map(d -> MonsterRegistry.findByName(d.getName()))
            .distinct()
            .toList()) {
      add(Category.MONSTERS, m.getName(), name(m), () -> monster(m));
      for (var drop : m.getLoot())
        drops
            .computeIfAbsent(ItemDefinition.normalizeKey(drop.getItem()), k -> new ArrayList<>())
            .add(m);
    }
    for (var s : SpawnRegistry.monsters()) {
      var m = MonsterRegistry.findByName(s.type());
      if (m != null) monsterSpawns.computeIfAbsent(m.getName(), k -> new ArrayList<>()).add(s);
    }
    for (var s : SpawnRegistry.npcs()) {
      var n = NpcFactoryRegistry.find(s.type());
      if (n != null) npcSpawns.computeIfAbsent(n.id(), k -> new ArrayList<>()).add(s);
    }
    for (var i :
        ItemRegistry.load().stream()
            .map(d -> ItemRegistry.findByKey(d.getKey()))
            .distinct()
            .toList())
      add(Category.ITEMS, i.getKey(), title(i.getName(), i.getKey()), () -> item(i));
    for (var s :
        SpellRegistry.load().stream()
            .map(d -> SpellRegistry.findByName(d.getKey()))
            .distinct()
            .toList())
      add(Category.SPELLS, s.getKey(), title(s.getName(), s.getKey()), () -> spell(s));
    for (var n : NpcFactoryRegistry.registrations()) {
      add(Category.NPCS, n.id(), title(n.displayName(), n.id()), () -> npc(n));
      var spec = NpcFactoryRegistry.specification(n.id());
      if (spec != null)
        for (var topic : spec.topics())
          for (var action : topic.actions())
            for (String target : action.targets()) {
              if (action.type() == com.perso.T4C.npc.ActionType.OPEN_SHOP
                  || action.type() == com.perso.T4C.npc.ActionType.GIVE_ITEM) {
                var item = ItemRegistry.findByKey(target);
                if (item != null)
                  npcItemSources.computeIfAbsent(item.getKey(), k -> new ArrayList<>()).add(n.id());
              }
              if (action.type() == com.perso.T4C.npc.ActionType.OPEN_SPELL_LEARNING) {
                var spell = SpellRegistry.findByName(target);
                if (spell != null)
                  teachers.computeIfAbsent(spell.getKey(), k -> new ArrayList<>()).add(n.id());
              }
            }
    }
    for (var q : QuestRegistry.load())
      add(Category.QUESTS, q.getId(), title(q.getTitle(), q.getId()), () -> quest(q));
    for (var g : HuntingGrounds.all())
      add(
          Category.AREAS,
          g.locationKey(),
          I18n.message("wiki.field_name", g.monster()),
          () -> area(g));
    for (var l : NamedLocations.all()) {
      if (HuntingGrounds.all().stream()
          .anyMatch(g -> l.worldZ() == 0 && g.x() == l.tileX() && g.y() == l.tileY())) continue;
      add(Category.AREAS, locationId(l), l.displayName(), () -> location(l));
    }
    for (var guide : LevelingGuide.entries(this)) entries.putIfAbsent(guide.id(), guide);
    sorted =
        entries.values().stream()
            .sorted(
                Comparator.comparingInt((Entry e) -> LevelingGuide.order(e.id()))
                    .thenComparing(Entry::title, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Entry::id))
            .toList();
  }

  public List<Entry> search(Category category, String query) {
    var words = (query == null ? "" : query.trim().toLowerCase(Locale.ROOT)).split("\\s+");
    return sorted.stream()
        .filter(e -> category == null || e.category() == category)
        .filter(e -> Arrays.stream(words).allMatch(e.searchText()::contains))
        .toList();
  }

  public Entry find(String id) {
    return entries.get(id);
  }

  public static String id(Category category, String key) {
    return category.name() + ":" + key;
  }

  private void add(Category c, String key, String name, Supplier<Page> page) {
    String id = id(c, key);
    entries.putIfAbsent(
        id,
        new Entry(
            id,
            c,
            name,
            (name
                    + " "
                    + key
                    + " "
                    + I18n.key("wiki.category." + c.name().toLowerCase(Locale.ROOT)))
                .toLowerCase(Locale.ROOT),
            page));
  }

  private static String title(String value, String fallback) {
    String resolved = I18n.resolve(value);
    return resolved == null || resolved.isBlank() || resolved.startsWith("${")
        ? fallback
        : resolved;
  }

  private static String name(MonsterDef m) {
    return title(m.getDisplayName(), m.getName());
  }

  private static String locationId(NamedLocation l) {
    return l.worldZ() + ":" + l.tileX() + ":" + l.tileY() + ":" + l.displayName();
  }

  private static String coordinates(int x, int y, int z) {
    return I18n.message("wiki.coordinates", x, y, z);
  }

  private static String percent(float chance) {
    return String.format(Locale.ROOT, "%.3f%%", chance * 100)
        .replaceAll("0+%$", "%")
        .replace(".%", "%");
  }

  private final class Article {
    final StringBuilder text = new StringBuilder();
    final Map<String, Link> links = new LinkedHashMap<>();

    void paragraph(String s) {
      if (s == null || s.isBlank()) return;
      String resolved = I18n.resolve(s);
      if (!resolved.startsWith("${")) text.append(resolved).append("\n\n");
    }

    void line(String key, Object... args) {
      text.append(I18n.message("wiki." + key, args)).append('\n');
    }

    void link(Category c, String key, String suffix) {
      Entry target = entries.get(id(c, key));
      if (target != null)
        links.putIfAbsent(
            target.id(),
            new Link(target.id(), target.title() + (suffix == null ? "" : " - " + suffix)));
    }

    void item(String key, String suffix) {
      var def = ItemRegistry.findByKey(key);
      if (def != null) link(Category.ITEMS, def.getKey(), suffix);
    }

    void spell(String key, String suffix) {
      var def = SpellRegistry.findByName(key);
      if (def != null) link(Category.SPELLS, def.getKey(), suffix);
    }

    void npc(String key) {
      var def = NpcFactoryRegistry.find(key);
      if (def != null) link(Category.NPCS, def.id(), null);
    }

    Page finish() {
      return new Page(text.toString().strip(), List.copyOf(links.values()));
    }
  }

  private Page item(ItemDefinition i) {
    var a = new Article();
    String tooltip = ItemTooltipText.build(i.getKey());
    int newline = tooltip.indexOf('\n');
    a.paragraph(title(i.getName(), i.getKey()) + (newline < 0 ? "" : tooltip.substring(newline)));
    if (i.isUndroppable()) a.line("bound");
    for (var b : i.getBoosts()) {
      a.line(
          "bonus",
          I18n.key("wiki.stat." + b.getStatId(), I18n.message("wiki.stat_other", b.getStatId())),
          b.getExpression());
      if (b.getMinInt() > 0 || b.getMinWis() > 0)
        a.line("bonus_requirements", b.getMinInt(), b.getMinWis());
    }
    for (var s : i.getSpells()) {
      var def = SpellRegistry.findById(s.getSpellId());
      if (def != null) {
        a.line("item_spell", title(def.getName(), def.getKey()), s.getChance(), s.getLevel());
        a.link(Category.SPELLS, def.getKey(), null);
      }
    }
    a.line("drop_sources");
    var sources = drops.getOrDefault(ItemDefinition.normalizeKey(i.getKey()), List.of());
    if (sources.isEmpty()) a.line("no_drop_source");
    for (var m : sources) {
      float chance = 0;
      for (var drop : m.getLoot())
        if (Objects.equals(
            ItemDefinition.normalizeKey(drop.getItem()), ItemDefinition.normalizeKey(i.getKey())))
          chance += drop.getChance();
      a.link(Category.MONSTERS, m.getName(), percent(Math.min(1, chance)));
    }
    for (var q : QuestRegistry.load()) {
      if (Objects.equals(
          ItemDefinition.normalizeKey(q.getRewardItemKey()),
          ItemDefinition.normalizeKey(i.getKey())))
        a.link(Category.QUESTS, q.getId(), I18n.key("wiki.reward"));
      if (Objects.equals(
          ItemDefinition.normalizeKey(q.getRequiredItemKey()),
          ItemDefinition.normalizeKey(i.getKey())))
        a.link(Category.QUESTS, q.getId(), I18n.key("wiki.required"));
    }
    for (var group : i.getContainerLootGroups())
      for (String key : group.getItemKeys()) a.item(key, I18n.key("wiki.container"));
    for (var event : com.perso.T4C.adventure.WorldEvents.definitions())
      if (com.perso.T4C.quest.definition.ConcordCampaign.STYLES.stream()
          .anyMatch(
              style ->
                  Objects.equals(
                      ItemDefinition.normalizeKey(
                          com.perso.T4C.adventure.WorldEvents.rewardKey(event, style)),
                      ItemDefinition.normalizeKey(i.getKey())))) {
        a.paragraph(I18n.message("event.wiki_reward", event.name()));
        a.link(Category.AREAS, event.ground().locationKey(), I18n.key("event.wiki_source"));
      }
    for (String npc : npcItemSources.getOrDefault(i.getKey(), List.of()))
      a.link(Category.NPCS, npc, I18n.key("wiki.offers"));
    return a.finish();
  }

  private Page monster(MonsterDef m) {
    var a = new Article();
    a.paragraph(name(m));
    a.line("monster_stats", m.getLevel(), m.getHealth(), m.getMana());
    a.line("damage", m.getHitDamageMin(), m.getHitDamageMax());
    a.line("defence", m.getAcMin(), m.getAcMax(), m.getDodge());
    a.line("attributes", m.getStr(), m.getEnd(), m.getAgi(), m.getIntel(), m.getWis());
    a.line(
        "xp",
        m.getXpOnDeath(),
        GameConstants.SERVER_XP_RATE * com.perso.T4C.monster.core.MonsterExperience.MULTIPLIER,
        Math.round(
            com.perso.T4C.monster.core.MonsterExperience.reward(m.getXpOnDeath())
                * (double) GameConstants.SERVER_XP_RATE));
    a.line("gold", m.getGoldMin(), m.getGoldMax());
    a.line(m.isDefaultAggressive() ? "aggressive" : "passive");
    if (MonsterManager.hasHuntingRespawn(m.getName()))
      a.line(
          "hunting_respawn",
          MonsterManager.huntingRespawnMinMillis() / 1000,
          MonsterManager.huntingRespawnMaxMillis() / 1000);
    else a.line("respawn", m.getRespawnTime() / 1000.0);
    String[] elements = {"air", "earth", "water", "fire", "dark", "light"};
    for (int index = 0; index < Math.min(elements.length, m.getResists().length); index++)
      a.line("resistance", I18n.key("element." + elements[index]), m.getResists()[index]);
    for (var attack : m.getAttacks()) {
      a.line(
          "attack",
          attack.getName(),
          attack.getValue1(),
          attack.getMinRangeTiles(),
          attack.getMaxRangeTiles());
      if (attack.isSpell()) {
        var s = SpellRegistry.findById(attack.getValue3());
        if (s != null) a.link(Category.SPELLS, s.getKey(), null);
      }
    }
    a.paragraph(I18n.key("wiki.spawn_locations"));
    var spawns = monsterSpawns.getOrDefault(m.getName(), List.of());
    if (spawns.isEmpty()) a.line("no_spawn");
    for (var s : spawns) a.paragraph(coordinates(s.x(), s.y(), s.z()));
    for (var g : HuntingGrounds.all())
      if (g.monster().equals(m.getName()) || Objects.equals(g.bossType(), m.getName()))
        a.link(Category.AREAS, g.locationKey(), null);
    DivinePantheon.all().stream()
        .filter(
            g ->
                m.getName().equals("Divine " + g.name())
                    && !DivinePantheon.seraphQuestKeys(g).isEmpty())
        .findFirst()
        .ifPresent(g -> a.paragraph(I18n.key("wiki.seraph_drops")));
    a.line("drop_rates");
    for (var d : m.getLoot()) a.item(d.getItem(), percent(d.getChance()));
    for (var q : QuestRegistry.load())
      if (Objects.equals(q.getTargetMonster(), m.getName()))
        a.link(Category.QUESTS, q.getId(), null);
    return a.finish();
  }

  private Page spell(SpellData s) {
    var a = new Article();
    String tooltip = SpellTooltipText.build(s);
    int newline = tooltip.indexOf('\n');
    a.paragraph(title(s.getName(), s.getKey()) + (newline < 0 ? "" : tooltip.substring(newline)));
    String description = I18n.resolve(s.getDescription());
    if (description != null && !tooltip.contains(description)) a.paragraph(description);
    a.line("spell_stats", s.getRadius(), s.getCooldownSeconds());
    if (s.getMinDamage() != 0 || s.getMaxDamage() != 0)
      a.line("base_damage", s.getMinDamage(), s.getMaxDamage());
    a.line(
        "duration",
        s.getDuration() == null ? "0" : s.getDuration(),
        s.getFrequency() == null ? "0" : s.getFrequency());
    a.line("price", s.getPrice());
    a.line("success_rate", s.getSuccessRate());
    a.line(
        "exhaustion", s.getMentalExhaustion(), s.getPhysicalExhaustion(), s.getAttackExhaustion());
    a.line(s.isLineOfSight() ? "line_of_sight" : "no_line_of_sight");
    if (s.getBuff() != null && s.getBuff().getEffects() != null)
      for (var e : s.getBuff().getEffects())
        a.line("effect", title(e.getDescription(), e.getAttribute()), e.getAmount());
    for (var effect : s.getT4cEffects()) {
      String type =
          I18n.key(
              "wiki.effect_type." + effect.getEffectType(),
              I18n.message("wiki.effect_other", effect.getEffectType()));
      for (var parameter : effect.getParameters())
        a.line("effect_parameter", type, parameter.getParamId(), parameter.getExpression());
    }
    for (String teacher : teachers.getOrDefault(s.getKey(), List.of()))
      a.link(Category.NPCS, teacher, I18n.key("wiki.teaches"));
    for (var i : ItemRegistry.load())
      for (var effect : i.getSpells())
        if (effect.getSpellId() == s.getSpellId())
          a.link(Category.ITEMS, i.getKey(), I18n.key("wiki.grants_spell"));
    for (var q : QuestRegistry.load())
      if (SpellRegistry.findByName(q.getRewardSpellKey()) == s)
        a.link(Category.QUESTS, q.getId(), I18n.key("wiki.reward"));
    return a.finish();
  }

  private Page npc(NpcFactoryRegistry.Registration n) {
    var a = new Article();
    a.paragraph(title(n.displayName(), n.id()));
    for (var s : npcSpawns.getOrDefault(n.id(), List.of()))
      a.paragraph(coordinates(s.x(), s.y(), s.z()));
    NpcSpec spec = NpcFactoryRegistry.specification(n.id());
    if (spec != null) {
      a.paragraph(spec.welcomeText());
      for (var topic : spec.topics()) {
        a.line(
            "keywords", String.join(", ", topic.keywords().stream().map(I18n::resolve).toList()));
        a.paragraph(topic.response());
        for (var action : topic.actions())
          for (String target : action.targets())
            switch (action.type()) {
              case OPEN_SHOP, GIVE_ITEM -> a.item(target, I18n.key("wiki.offers"));
              case OPEN_SPELL_LEARNING -> a.spell(target, I18n.key("wiki.teaches"));
              case GIVE_QUEST -> a.link(Category.QUESTS, target, null);
              default -> {}
            }
      }
    }
    for (var q : QuestRegistry.load()) {
      var giver = NpcFactoryRegistry.find(q.getGiverNpc());
      if (giver != null && giver.id().equals(n.id())) a.link(Category.QUESTS, q.getId(), null);
    }
    return a.finish();
  }

  private Page quest(QuestDef q) {
    var a = new Article();
    a.paragraph(title(q.getTitle(), q.getId()));
    a.line("quest_giver", title(q.getGiverNpc(), ""), q.getMinLevel());
    a.npc(q.getGiverNpc());
    a.paragraph(q.getOfferText());
    a.paragraph(q.getWalkthroughText());
    if (q.getRequiredKills() > 0) {
      a.line("kills", q.getRequiredKills(), q.getTargetMonster());
      a.link(Category.MONSTERS, q.getTargetMonster(), null);
    }
    if (q.getRequiredItemQty() > 0) {
      a.line("collect", q.getRequiredItemQty());
      a.item(q.getRequiredItemKey(), I18n.key("wiki.required"));
    }
    a.line("quest_rewards", q.getRewardGold(), q.getRewardXp());
    a.item(q.getRewardItemKey(), I18n.key("wiki.reward"));
    a.spell(q.getRewardSpellKey(), I18n.key("wiki.reward"));
    if (q.getUnlockZoneId() != null)
      for (var l : NamedLocations.all())
        if (q.getUnlockZoneId().equals(l.unlockZoneId())) {
          a.line("unlocks", l.displayName());
          var g =
              HuntingGrounds.all().stream()
                  .filter(f -> f.x() == l.tileX() && f.y() == l.tileY())
                  .findFirst();
          a.link(Category.AREAS, g.isPresent() ? g.get().locationKey() : locationId(l), null);
        }
    a.paragraph(q.getCompletionText());
    return a.finish();
  }

  private Page area(HuntingGrounds.Ground g) {
    var a = new Article();
    a.paragraph(I18n.message("wiki.field_name", g.monster()));
    a.paragraph(coordinates(g.x(), g.y(), 0));
    a.line("population", g.points().size());
    a.paragraph(I18n.key("location." + g.locationKey() + ".desc", ""));
    a.line(
        "travel",
        g.unlock() == null
            ? I18n.key("wiki.available")
            : I18n.key("location." + g.unlock(), g.unlock().replace('_', ' ')));
    a.link(Category.MONSTERS, g.monster(), null);
    if (g.trainingTier() > 0) {
      a.link(Category.MONSTERS, g.bossType(), I18n.key("wiki.boss"));
      a.item(g.scrollKey(), I18n.key("wiki.return_scroll"));
    }
    return a.finish();
  }

  private Page location(NamedLocation l) {
    var a = new Article();
    a.paragraph(l.displayName());
    a.paragraph(coordinates(l.tileX(), l.tileY(), l.worldZ()));
    a.paragraph(I18n.key("location." + NamedLocations.catalogueKey(l) + ".desc", ""));
    a.line("island_access", l.minIslandAccess());
    for (var q : QuestRegistry.load())
      if (l.unlockZoneId() != null && l.unlockZoneId().equals(q.getUnlockZoneId()))
        a.link(Category.QUESTS, q.getId(), I18n.key("wiki.required"));
    return a.finish();
  }
}
