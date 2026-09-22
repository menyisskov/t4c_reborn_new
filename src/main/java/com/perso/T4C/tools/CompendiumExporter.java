package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.definition.QuestDefinitions;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.definition.SpellDefinitions;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Dumps every new-since-fork monster/spell/npc/quest/item into flat JSON files under
 * {@code compendium/data/} for the standalone local compendium website (T4C-0012). Reuses the
 * game's own registries (MonsterRegistry, SpellDefinitions, QuestDefinitions,
 * NpcFactoryRegistry) instead of re-parsing source, and resolves every i18n key through
 * {@link I18n} so the site never has to embed lang.json itself.
 *
 * <p>The "is this new content" allow-lists below were compiled by cross-referencing
 * CHANGELOG.md and docs/content-ideas/*.md against the actual class names in the repo (see
 * T4C-0012's changelog entry for the pass this was built in).
 */
public final class CompendiumExporter {
  private CompendiumExporter() {}

  private static final Set<String> NEW_MONSTER_NAMES =
      Set.of(
          "Drowned Acolyte",
          "Tideclaw Crab",
          "Mordrenn the Drowned Inquisitor",
          "Cinder Whelp",
          "Ashfang Stalker",
          "Ignarok the Emberfang",
          "Centaur Warrior",
          "Centaur King",
          "Barrow Wight",
          "The Hollow King",
          "Kraanian Wyrmling",
          "Lesser Drake",
          "Bastion Warden",
          "Greater Drake",
          "Kraanian Dragonguard",
          "Fey Warden",
          "Moonlit Stalker",
          "Veilbound Wraith",
          "Sundered Sentinel",
          "Sir Caradoc, the Sundered Knight",
          "Ysolde, the Veiled Matriarch",
          "The Verdant Warden");

  /** Pre-existing legacy monsters that a content pass placed/activated rather than authored. */
  private static final Set<String> ACTIVATED_MONSTER_NAMES = Set.of("Arch Drake");

  private static final Set<String> NEW_SPELL_CLASSES =
      Set.of(
          "RiptideSurge",
          "DrownedWard",
          "Cinderburst",
          "EmberheartResolve",
          "Clemancy",
          "DivineVeil",
          "UndeadAnnihilation",
          "OmegaPlanetoids",
          "Sentinel",
          "AvalonGateway");

  private static final Set<String> NEW_NPC_IDS =
      Set.of(
          "TideWardenBryn",
          "RurikCinderwatch",
          "SpellMerchant",
          "StorageChest",
          "ElderOphira",
          "QuartermasterElenna",
          "WayfarerBryndis",
          "ArchmageThalindra",
          "SisterIlyndra",
          "OutriderKaelis",
          "KeeperTamsin",
          "MarshalTorrhen",
          "WardenCael",
          "GrandmasterVoss");

  private static final Set<String> ACTIVATED_NPC_IDS = Set.of("RhodarHeatforge", "SkywatchIlvara");

  private static final Set<String> NEW_QUEST_IDS =
      Set.of(
          "silversky_tide_warden",
          "emberfang_hills_bounty",
          "windhowl_marches_centaurs",
          "hollow_march_wights",
          "aerie_wyrmling_cull",
          "bastion_warden_siege",
          "deep_ones_cave_purge",
          "drakes_lair_vigil",
          "avalon_wilds_vigil",
          "fading_veil_reckoning");

  private static final Set<String> SHOP_EXCLUDED_NPC_IDS =
      Set.of("Boreas", "Yolak", "TtayhMark", "Kiadus", "RhodarHeatforge", "GulfridSteelhammer");

  public static void main(String[] args) throws Exception {
    Path outDir = Path.of(args.length > 0 ? args[0] : "compendium/data");
    Files.createDirectories(outDir);

    MonsterJsonLoader.loadAndRegister();

    Map<String, Object> shops = exportShops();

    writeJson(outDir.resolve("monsters.json"), exportMonsters());
    writeJson(outDir.resolve("spells.json"), exportSpells());
    writeJson(outDir.resolve("quests.json"), exportQuests());
    writeJson(outDir.resolve("npcs.json"), exportNpcs());
    writeJson(outDir.resolve("items.json"), exportItems());
    writeJson(outDir.resolve("shops.json"), shops);

    // Bundle everything (generated + the hand-authored zones/statids/meta files, if present)
    // into a single data.js so the site works by opening index.html directly, no local server
    // or fetch()-over-file:// CORS workaround required.
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    Map<String, Object> bundle = new LinkedHashMap<>();
    bundle.put("monsters", exportMonsters());
    bundle.put("spells", exportSpells());
    bundle.put("quests", exportQuests());
    bundle.put("npcs", exportNpcs());
    bundle.put("items", exportItems());
    bundle.put("shops", shops);
    bundle.put("zones", readHandAuthored(outDir.resolve("zones.json")));
    bundle.put("statIds", readHandAuthored(outDir.resolve("statids.json")));
    bundle.put("meta", readHandAuthored(outDir.resolve("meta.json")));

    StringBuilder js = new StringBuilder();
    js.append("// Auto-generated by CompendiumExporter. Do not edit by hand.\n");
    js.append("window.T4C_DATA = ").append(gson.toJson(bundle)).append(";\n");
    Files.writeString(outDir.getParent().resolve("data.js"), js, StandardCharsets.UTF_8);
    System.out.println("Wrote " + outDir.getParent().resolve("data.js"));

    System.out.println("Compendium data written to " + outDir.toAbsolutePath());
  }

  // ---------------------------------------------------------------- monsters

  private static List<Map<String, Object>> exportMonsters() {
    List<MonsterDef> defs =
        MonsterRegistry.load().stream()
            .sorted(Comparator.comparing(MonsterDef::getLevel).thenComparing(MonsterDef::getName))
            .toList();
    List<Map<String, Object>> out = new ArrayList<>();
    for (MonsterDef def : defs) {
      String origin =
          NEW_MONSTER_NAMES.contains(def.getName())
              ? "new"
              : ACTIVATED_MONSTER_NAMES.contains(def.getName()) ? "activated" : null;
      boolean jsonAuthored = JSON_MONSTER_NAMES.contains(def.getName());
      if (origin == null && !jsonAuthored) continue;
      if (origin == null) origin = "new";

      Map<String, Object> m = new LinkedHashMap<>();
      m.put("name", def.getName());
      m.put("displayName", I18n.resolve(def.getDisplayName()));
      m.put("origin", origin);
      m.put("level", def.getLevel());
      m.put("health", def.getHealth());
      m.put("mana", def.getMana());
      m.put("xpPerHit", def.getXpPerHit());
      m.put("xpOnDeath", def.getXpOnDeath());
      m.put("hitDamageMin", def.getHitDamageMin());
      m.put("hitDamageMax", def.getHitDamageMax());
      m.put("respawnTimeMs", def.getRespawnTime());
      m.put("goldMin", def.getGoldMin());
      m.put("goldMax", def.getGoldMax());
      m.put("dodge", def.getDodge());
      m.put("acMin", def.getAcMin());
      m.put("acMax", def.getAcMax());
      m.put("aggro", def.getAggro());
      m.put("speed", def.getSpeed());
      m.put("canAttack", def.isCanAttack());
      m.put("tameable", def.isTameable());
      m.put("tameMaxLevel", def.getTameMaxLevel());
      Map<String, Integer> stats = new LinkedHashMap<>();
      stats.put("str", def.getStr());
      stats.put("end", def.getEnd());
      stats.put("agi", def.getAgi());
      stats.put("intel", def.getIntel());
      stats.put("will", def.getWill());
      stats.put("wis", def.getWis());
      stats.put("luck", def.getLuck());
      m.put("stats", stats);
      m.put("resists", resistMap(def.getResists()));
      List<Map<String, Object>> loot = new ArrayList<>();
      if (def.getLoot() != null) {
        for (MonsterDef.LootDrop drop : def.getLoot()) {
          Map<String, Object> l = new LinkedHashMap<>();
          l.put("item", drop.getItem());
          l.put("chance", drop.getChance());
          loot.add(l);
        }
      }
      m.put("loot", loot);
      List<Map<String, Object>> attacks = new ArrayList<>();
      if (def.getAttacks() != null) {
        for (MonsterDef.Attack atk : def.getAttacks()) {
          Map<String, Object> a = new LinkedHashMap<>();
          a.put("formula", atk.getName());
          a.put("value2", atk.getValue2());
          a.put("isSpell", atk.isSpell());
          a.put("spellId", atk.getValue3());
          a.put("rangeMinTiles", atk.getValue4());
          a.put("rangeMaxTiles", atk.getValue5());
          attacks.add(a);
        }
      }
      m.put("attacks", attacks);
      out.add(m);
    }
    return out;
  }

  private static final Set<String> JSON_MONSTER_NAMES = loadJsonMonsterNames();

  private static Set<String> loadJsonMonsterNames() {
    Set<String> names = new java.util.HashSet<>();
    File dir = new File(MonsterJsonLoader.DEFAULT_DIRECTORY);
    File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
    if (files == null) return names;
    Gson gson = new Gson();
    for (File f : files) {
      try (FileReader reader = new FileReader(f, StandardCharsets.UTF_8)) {
        JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
        if (obj.has("name")) names.add(obj.get("name").getAsString());
      } catch (IOException | RuntimeException ignored) {
      }
    }
    return names;
  }

  private static final String[] RESIST_LABELS = {
    "air", "fire", "water", "earth", "?4", "?5", "?6", "?7", "?8", "?9", "?10", "light", "dark"
  };

  private static Map<String, Integer> resistMap(int[] resists) {
    Map<String, Integer> out = new LinkedHashMap<>();
    if (resists == null) return out;
    String[] labels = {"air", "fire", "water", "earth", "light", "dark"};
    int[] indices = {0, 1, 2, 3, 10, 11};
    for (int i = 0; i < labels.length && indices[i] < resists.length; i++) {
      out.put(labels[i], resists[indices[i]]);
    }
    return out;
  }

  // ------------------------------------------------------------------ spells

  private static List<Map<String, Object>> exportSpells() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (SpellData spell : SpellDefinitions.all()) {
      String key = spell.getKey();
      String simpleClass = classNameGuess(spell);
      boolean isNew = NEW_SPELL_CLASSES.contains(simpleClass);
      String category =
          key != null && key.startsWith("spell.mob_")
              ? "monster"
              : key != null && key.startsWith("spell.item_") ? "item-triggered" : "player";
      if (!isNew && !"player".equals(category)) continue;
      Map<String, Object> s = new LinkedHashMap<>();
      s.put("key", key);
      s.put("isNew", isNew);
      s.put("category", category);
      s.put("name", I18n.resolve(spell.getName()));
      s.put("description", I18n.resolve(spell.getDescription()));
      s.put("manaCost", spell.getManaCost());
      s.put("minInt", spell.getMinInt());
      s.put("minWis", spell.getMinWis());
      s.put("minLevel", spell.getMinLevel());
      s.put("isAttack", spell.isAttack());
      s.put("lineOfSight", spell.isLineOfSight());
      s.put("minDamage", spell.getMinDamage());
      s.put("maxDamage", spell.getMaxDamage());
      s.put("cooldownSeconds", spell.getCooldownSeconds());
      s.put("duration", spell.getDuration());
      s.put("price", spell.getPrice());
      s.put("spellId", spell.getSpellId());
      s.put("element", spell.getElement());
      s.put("targetType", spell.getTargetType());
      s.put("attackType", spell.getAttackType());
      s.put("successRate", spell.getSuccessRate());
      s.put("pvp", spell.isPvp());
      List<Map<String, Object>> effects = new ArrayList<>();
      if (spell.getT4cEffects() != null) {
        for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
          Map<String, Object> e = new LinkedHashMap<>();
          e.put("effectType", effect.getEffectType());
          List<Map<String, Object>> params = new ArrayList<>();
          if (effect.getParameters() != null) {
            for (SpellData.T4cEffect.EffectParam p : effect.getParameters()) {
              Map<String, Object> pm = new LinkedHashMap<>();
              pm.put("paramId", p.getParamId());
              pm.put("expression", p.getExpression());
              params.add(pm);
            }
          }
          e.put("parameters", params);
          effects.add(e);
        }
      }
      s.put("effects", effects);
      out.add(s);
    }
    return out;
  }

  private static String classNameGuess(SpellData spell) {
    // SpellDefinitions.all() doesn't carry the source class name, so recover it from the i18n
    // key (e.g. "spell.riptide_surge" -> "RiptideSurge") to match against NEW_SPELL_CLASSES.
    String key = spell.getKey();
    if (key == null) return "";
    String tail = key.contains(".") ? key.substring(key.lastIndexOf('.') + 1) : key;
    StringBuilder sb = new StringBuilder();
    for (String part : tail.split("_")) {
      if (part.isEmpty()) continue;
      sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
    }
    return sb.toString();
  }

  // ------------------------------------------------------------------ quests

  private static List<Map<String, Object>> exportQuests() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (QuestDef q : QuestDefinitions.all()) {
      if (!NEW_QUEST_IDS.contains(q.getId())) continue;
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", q.getId());
      m.put("title", I18n.resolve(q.getTitle()));
      m.put("giverNpc", q.getGiverNpc());
      m.put("targetMonster", q.getTargetMonster());
      m.put("requiredKills", q.getRequiredKills());
      m.put("targetWorldZ", q.getTargetWorldZ());
      m.put("areaCenterX", q.getAreaCenterX());
      m.put("areaCenterY", q.getAreaCenterY());
      m.put("areaRadiusTiles", q.getAreaRadiusTiles());
      m.put("rewardGold", q.getRewardGold());
      m.put("rewardXp", q.getRewardXp());
      m.put("offerText", I18n.resolve(q.getOfferText()));
      m.put("completionText", I18n.resolve(q.getCompletionText()));
      m.put("completedText", I18n.resolve(q.getCompletedText()));
      out.add(m);
    }
    return out;
  }

  // -------------------------------------------------------------------- npcs

  private static List<Map<String, Object>> exportNpcs() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (NpcFactoryRegistry.Registration reg : NpcFactoryRegistry.registrations()) {
      boolean isNew = NEW_NPC_IDS.contains(reg.id());
      boolean activated = ACTIVATED_NPC_IDS.contains(reg.id());
      if (!isNew && !activated) continue;
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", reg.id());
      m.put("origin", isNew ? "new" : "activated");
      m.put("displayName", I18n.resolve(reg.displayName()));
      m.put("spriteBase", reg.spriteBase());
      NpcSpec spec = reg.specification() == null ? null : safeSpec(reg);
      if (spec != null) {
        m.put("welcomeText", I18n.resolve(spec.welcomeText()));
        List<Map<String, Object>> topics = new ArrayList<>();
        for (NpcSpec.DialogueTopic topic : spec.topics()) {
          Map<String, Object> t = new LinkedHashMap<>();
          List<String> keywords = new ArrayList<>();
          for (String kw : topic.keywords()) keywords.add(I18n.resolve(kw));
          t.put("keywords", keywords);
          t.put("response", I18n.resolve(topic.response()));
          List<String> actions = new ArrayList<>();
          for (NpcSpec.Action action : topic.actions()) {
            actions.add(action.type() + (action.targets().isEmpty() ? "" : (":" + String.join(",", action.targets()))));
          }
          t.put("actions", actions);
          topics.add(t);
        }
        m.put("topics", topics);
        NpcSpec.CombatProfile cp = spec.combatProfile();
        if (cp != null) {
          Map<String, Object> combat = new LinkedHashMap<>();
          combat.put("level", cp.level());
          combat.put("maxHp", cp.maxHp());
          combat.put("strength", cp.strength());
          combat.put("endurance", cp.endurance());
          combat.put("dexterity", cp.dexterity());
          combat.put("armorClass", cp.armorClass());
          combat.put("attackSkill", cp.attackSkill());
          combat.put("dodge", cp.dodge());
          combat.put("damageFormula", cp.damageFormula());
          m.put("combatProfile", combat);
        }
      }
      out.add(m);
    }
    return out;
  }

  private static NpcSpec safeSpec(NpcFactoryRegistry.Registration reg) {
    try {
      return reg.specification().get();
    } catch (RuntimeException ex) {
      return null;
    }
  }

  // -------------------------------------------------------------------- shops

  @SuppressWarnings("unchecked")
  private static Map<String, Object> exportShops() {
    Map<String, Object> out = new LinkedHashMap<>();
    try {
      Class<?> shopCatalog = Class.forName("com.perso.T4C.npc.catalog.ShopCatalog");
      Field mField = shopCatalog.getDeclaredField("M");
      mField.setAccessible(true);
      Map<String, List<String>> m = (Map<String, List<String>>) mField.get(null);
      for (Map.Entry<String, List<String>> entry : new TreeMap<>(m).entrySet()) {
        if (SHOP_EXCLUDED_NPC_IDS.contains(entry.getKey())) continue;
        if (!NEW_NPC_IDS.contains(entry.getKey()) && !ACTIVATED_NPC_IDS.contains(entry.getKey()))
          continue;
        out.put(entry.getKey(), entry.getValue());
      }
    } catch (ReflectiveOperationException ex) {
      System.err.println("Could not read ShopCatalog: " + ex);
    }
    return out;
  }

  // -------------------------------------------------------------------- items

  private static List<Map<String, Object>> exportItems() {
    List<Map<String, Object>> out = new ArrayList<>();
    File dir = new File("assets/items");
    File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
    if (files == null) return out;
    List<File> sorted = new ArrayList<>(List.of(files));
    sorted.sort(Comparator.comparing(File::getName));
    Gson gson = new Gson();
    for (File f : sorted) {
      try (FileReader reader = new FileReader(f, StandardCharsets.UTF_8)) {
        JsonElement parsed = JsonParser.parseReader(reader);
        JsonObject obj = parsed.getAsJsonObject();
        Map<String, Object> item = new LinkedHashMap<>();
        for (String field : obj.keySet()) {
          item.put(field, gson.fromJson(obj.get(field), Object.class));
        }
        if (!item.containsKey("key")) item.put("key", f.getName().replace(".json", ""));
        out.add(item);
      } catch (IOException | RuntimeException ex) {
        System.err.println("Skipping item file " + f + ": " + ex);
      }
    }
    return out;
  }

  // ------------------------------------------------------------------- write

  private static Object readHandAuthored(Path path) throws IOException {
    if (!Files.exists(path)) return List.of();
    try (FileReader reader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
      return new Gson().fromJson(reader, Object.class);
    }
  }

  private static void writeJson(Path path, Object data) throws IOException {
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    Files.writeString(path, gson.toJson(data), StandardCharsets.UTF_8);
    System.out.println("Wrote " + path);
  }
}
