package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.perso.T4C.combat.SeraphAuraService;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import com.perso.T4C.npc.ActionType;
import com.perso.T4C.npc.behavior.RebirthBehavior;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.definition.QuestDefinitions;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
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
import java.util.LinkedHashSet;
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
          "The Verdant Warden",
          "Tideworn Reaver",
          "Coastwarden Ithrak",
          "The Rootcrown Wyrm",
          "The Pyreclaw Wyrm",
          "The Mistwing Wyrm",
          "The Duskmaw Wyrm",
          "The Galecrest Wyrm");

  /** Pre-existing legacy monsters that a content pass placed/activated rather than authored. */
  private static final Set<String> ACTIVATED_MONSTER_NAMES = Set.of("Arch Drake");

  private static final Set<String> NEW_SPELL_CLASSES =
      Set.of(
          "RiptideSurge",
          "DrownedWard",
          "Cinderburst",
          "EmberheartResolve",
          "WellspringMercy",
          "VeilstoneAegis",
          "Sunscour",
          "Gravebreaker",
          "LeywardBastion",
          "VoidreaveLance",
          "StormcallersJudgment",
          "SanctumWard",
          "EmberqueensWrath",
          "CataclysmsHerald",
          "AvalonGateway",
          "Scorchbrand",
          "Pyreburst",
          "MagmaheartLance",
          "SunforgeBrand",
          "Ashfall",
          "RimeLance",
          "Frostgale",
          "AbyssalSpear",
          "Tidebreaker",
          "DrowningDeep",
          "Stonefang",
          "MountainsFist",
          "WorldrootUpheaval",
          "TectonicRuin",
          "Galespike",
          "Thunderhead",
          "Skysplitter",
          "TempestLance",
          "Heavenfall",
          "Nightfang",
          "Shadowblight",
          "Soulrend",
          "UmbralTide",
          "EclipseOfRuin",
          "Dawnflare",
          "RadiantSpear",
          "SeraphsVerdict",
          "HallowedNova",
          "SolarApotheosis",
          "DawnwellRenewal");

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
          "GrandmasterVoss",
          "HarbormasterRangor",
          "SentinelCorwin",
          "OutriderHalvard",
          "DockmasterThessaly",
          "EmberSmithCorvain",
          "WardenSeressa",
          "GrandmasterTholvenn",
          "AnchoriteRowan");

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
          "fading_veil_reckoning",
          "passage_to_avalon",
          "silversky_borderwatch",
          "windhowl_borderwatch",
          "passage_to_kraanhold",
          "tideworn_shore_scouts",
          "forge_the_godcore",
          "bind_the_godsigil",
          "forge_godsforged_warblade",
          "forge_godsforged_stormbow",
          "forge_godsforged_voidglass_rod",
          "forge_godsforged_zephyr_wand",
          "forge_godsforged_torc",
          "the_waking_rite");

  // T4C-0033: the Godsforged crafting chain's raw materials/components are legacy Java items (not
  // assets/items/*.json), specifically so they're exempt from ItemBalanceGuidelinesTest's gear
  // rules - see item/definition/WyrmforgedEmber.java. exportItems() only scans assets/items/, so
  // without this they'd resolve to nothing on the reference site (itemLink() would fall back to
  // showing the raw key). Listed explicitly and merged in from ItemRegistry.load() instead.
  // A List, not a Set: iterated directly into the exported file, so its order must be stable
  // across regenerations (a Set.of() here reorders randomly run to run, producing a pure-noise
  // diff on every regenerate-compendium CI run).
  private static final List<String> NEW_UTILITY_ITEM_KEYS =
      List.of(
          "item.wyrmforged_ember",
          "item.veiled_aether_shard",
          "item.tempered_godcore",
          "item.bound_godsigil",
          // T4C-0036: both pre-existing but previously unsold at any vendor (price 0/333,
          // absent from every ShopCatalog list) - now stocked by every town's general-goods
          // vendor as a gold sink, so they need to show up here with their real price.
          "item.mana_prism",
          "item.critical_healing_potion");

  // T4C-0033: the five final Godsforged forge quests each also require a Tempered Godcore, which
  // GrandmasterTholvenn.java's custom javaBehavior() checks/consumes itself since a single
  // QuestDef can only natively track one required item (requiredItemKey below tracks the Bound
  // Godsigil). Documented here purely so the reference site's structured walkthrough shows both
  // required items instead of just the one QuestDef itself knows about.
  private static final Map<String, String> EXTRA_REQUIRED_ITEM_KEYS =
      Map.of(
          "forge_godsforged_warblade", "item.tempered_godcore",
          "forge_godsforged_stormbow", "item.tempered_godcore",
          "forge_godsforged_voidglass_rod", "item.tempered_godcore",
          "forge_godsforged_zephyr_wand", "item.tempered_godcore",
          "forge_godsforged_torc", "item.tempered_godcore");

  private static final Set<String> SHOP_EXCLUDED_NPC_IDS =
      Set.of("Boreas", "Yolak", "TtayhMark", "Kiadus", "RhodarHeatforge", "GulfridSteelhammer");

  // T4C-0036: Boreas.java and Yolak.java build their BUY item list inline in a custom
  // javaBehavior() rather than through ShopCatalog or a declarative OPEN_SHOP action, so
  // neither loop in exportShops() can see it - the reflective ShopCatalog.get() call above
  // returns null for both (see SHOP_EXCLUDED_NPC_IDS's comment: their M entry is stale/unused).
  // Kept in sync by hand whenever those two files' base item lists change; omits the
  // __QUEST_ISLAND_ACCESS-gated scroll additions since those are conditional, not a fixed
  // shop listing.
  private static final Map<String, List<String>> HAND_MAINTAINED_SHOP_ITEMS =
      Map.of(
          "Boreas",
              List.of(
                  "torch",
                  "light_healing_potion",
                  "potion_of_mana",
                  "healing_potion",
                  "mana_elixir",
                  "item.mana_prism",
                  "item.critical_healing_potion",
                  "scroll_of_lighthaven",
                  "scroll_of_windhowl"),
          "Yolak",
              List.of(
                  "torch",
                  "light_healing_potion",
                  "potion_of_mana",
                  "healing_potion",
                  "item.mana_prism",
                  "item.critical_healing_potion",
                  "scroll_of_lighthaven",
                  "scroll_of_windhowl"));

  public static void main(String[] args) throws Exception {
    Path outDir = Path.of(args.length > 0 ? args[0] : "compendium/data");
    Files.createDirectories(outDir);

    MonsterJsonLoader.loadAndRegister();

    Map<String, Object> shops = exportShops();
    List<Map<String, Object>> items = exportItems();
    Set<String> itemKeys = new java.util.HashSet<>();
    for (Map<String, Object> item : items) itemKeys.add((String) item.get("key"));
    List<Map<String, Object>> lootSources = exportLootSources(itemKeys);

    writeJson(outDir.resolve("monsters.json"), exportMonsters());
    writeJson(outDir.resolve("spells.json"), exportSpells());
    writeJson(outDir.resolve("quests.json"), exportQuests());
    writeJson(outDir.resolve("npcs.json"), exportNpcs());
    writeJson(outDir.resolve("items.json"), items);
    writeJson(outDir.resolve("shops.json"), shops);
    writeJson(outDir.resolve("lootSources.json"), lootSources);
    writeJson(outDir.resolve("xpcurve.json"), exportXpCurve());
    writeJson(outDir.resolve("rebirths.json"), exportRebirths());

    // Bundle everything (generated + the hand-authored zones/statids/meta files, if present)
    // into a single data.js so the site works by opening index.html directly, no local server
    // or fetch()-over-file:// CORS workaround required.
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    Map<String, Object> bundle = new LinkedHashMap<>();
    bundle.put("monsters", exportMonsters());
    bundle.put("spells", exportSpells());
    bundle.put("quests", exportQuests());
    bundle.put("npcs", exportNpcs());
    bundle.put("items", items);
    bundle.put("shops", shops);
    bundle.put("lootSources", lootSources);
    bundle.put("xpCurve", exportXpCurve());
    bundle.put("rebirths", exportRebirths());
    bundle.put("zones", readHandAuthored(outDir.resolve("zones.json")));
    bundle.put("statIds", readHandAuthored(outDir.resolve("statids.json")));
    bundle.put("meta", readHandAuthored(outDir.resolve("meta.json")));
    // T4C-0023: not hand-authored, but generated ahead of this exporter by MinimapExporter (run
    // it first) - reusing readHandAuthored is fine, it's just "read whatever JSON is already
    // sitting in outDir back in for bundling", regardless of which tool produced it.
    bundle.put("maps", readHandAuthored(outDir.resolve("maps.json")));

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
      m.put("displayName", resolveOrFallback(def.getDisplayName(), def.getName()));
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
          // BaseMonster#rollDamage: value1 is the combat-attack score fed into hit-chance
          // resolution (the real "hit power" stat); value2 is only a selection weight among
          // multiple eligible attacks at this range, never a hit percentage - do not relabel it
          // as one (see BaseMonster#pickAttack).
          a.put("combatAttack", atk.getValue1());
          a.put("selectionWeight", atk.getValue2());
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

  /**
   * {@link I18n#resolve} returns its input unchanged when the key isn't in lang.json - some
   * JSON-authored monsters (the level 525-750 Colosseum ladder) reference i18n keys that were
   * never added to the catalogue, so resolving them leaves a literal {@code ${monster.xxx}}
   * placeholder. Fall back to the raw spawn name rather than publish that placeholder verbatim.
   */
  private static String resolveOrFallback(String i18nValue, String fallback) {
    String resolved = I18n.resolve(i18nValue);
    if (resolved != null && resolved.startsWith("${") && resolved.endsWith("}")) {
      return fallback;
    }
    return resolved;
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

  // Matches BaseMonster#getElementResistance's element-code -> combatResists-index mapping
  // exactly (air=0, earth=1, water=2, fire=3, dark=4, light=5) - do not reorder without
  // re-checking that switch, the array itself carries no labels.
  private static Map<String, Integer> resistMap(int[] resists) {
    Map<String, Integer> out = new LinkedHashMap<>();
    if (resists == null) return out;
    String[] labels = {"air", "earth", "water", "fire", "dark", "light"};
    for (int i = 0; i < labels.length && i < resists.length; i++) {
      out.put(labels[i], resists[i]);
    }
    return out;
  }

  // ------------------------------------------------------------------ spells

  private static List<Map<String, Object>> exportSpells() {
    List<Map<String, Object>> out = new ArrayList<>();
    Set<String> seenClasses = new java.util.HashSet<>();
    // SpellRegistry.playerCastableSpells() is the game's own authoritative filter - it already
    // excludes mob-only/item-triggered/test/npc-only spells and dedupes aliases, so trust it
    // rather than re-deriving the same rule from key prefixes.
    for (SpellData spell : SpellRegistry.playerCastableSpells()) {
      String simpleClass = classNameGuess(spell);
      seenClasses.add(simpleClass);
      out.add(spellToMap(spell, NEW_SPELL_CLASSES.contains(simpleClass)));
    }
    // A couple of curated new spells (e.g. AvalonGateway, a travel spell with no icon) are
    // real new content but fail playerCastableSpells()'s "has an icon" check, which exists for
    // the in-game training UI, not for "is this new content worth documenting" - add them back
    // explicitly rather than silently dropping them.
    for (SpellData spell : SpellDefinitions.all()) {
      String simpleClass = classNameGuess(spell);
      if (NEW_SPELL_CLASSES.contains(simpleClass) && seenClasses.add(simpleClass)) {
        out.add(spellToMap(spell, true));
      }
    }
    return out;
  }

  private static Map<String, Object> spellToMap(SpellData spell, boolean isNew) {
    Map<String, Object> s = new LinkedHashMap<>();
    s.put("key", spell.getKey());
    s.put("isNew", isNew);
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
    s.put("damageAtReference", damageAtReference(spell));
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
    return s;
  }

  /** A concrete, reproducible damage figure for an attack spell's primary effect (effectType 1
   * or 10 - both are treated as the "health delta" formula by SpellEffectManager#
   * resolveHealthDelta, effectType 10 being the drain-life variant), since the raw min/maxDamage
   * fields on SpellData are always 0 for every effect-driven spell (the real damage lives in the
   * T4cEffect formula string, not those fields). Evaluated at a fixed, labeled reference: the
   * caster at exactly this spell's own minInt/minWis/minLevel requirements, an untrained (100)
   * elemental skill, and a neutral (100) target resistance - the same "self.element" default the
   * engine itself falls back to. Real in-game damage scales up with the caster's trained
   * elemental skill and down/up with the target's real resistance (see SpellEffectManager -
   * self.fire/self.water/etc. are trained skills, not attributes), so this is a reference point
   * for comparison, not a promise of what any given cast will deal. Returns null for non-attack
   * spells or spells with no damage-effect formula (pure buffs/wards/heals). */
  private static Map<String, Object> damageAtReference(SpellData spell) {
    if (!spell.isAttack() || spell.getT4cEffects() == null) return null;
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect == null || effect.getParameters() == null) continue;
      int type = effect.getEffectType();
      if (type != 1 && type != 10) continue;
      String formula = effect.getParameters().isEmpty() ? null : effect.getParameters().get(0).getExpression();
      if (formula == null || formula.isBlank()) continue;
      // The formula is a "health delta" (negative = damage), sometimes wrapped in a leading
      // "-(...)" and sometimes with the sign buried inside a conditional (e.g. Mana Burst's
      // "if(cond?-(...):0)"). Rather than assume the sign sits at the start of the string,
      // negate the whole expression - the parser supports a leading unary minus over any
      // sub-expression, including if(...), so this reliably turns "damage" into a positive
      // magnitude (and a 0-delta miss/resist branch stays 0) regardless of where the "-" is.
      String magnitude = "-(" + formula + ")";
      DiceFormula.Context ctx =
          new DiceFormula.Context(
              0, 0, 0, spell.getMinInt(), 0, spell.getMinWis(), 0, spell.getMinLevel(),
              0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100);
      int a = DiceFormula.of(magnitude).min(ctx);
      int b = DiceFormula.of(magnitude).max(ctx);
      Map<String, Object> out = new LinkedHashMap<>();
      out.put("formula", formula);
      out.put("min", Math.min(a, b));
      out.put("max", Math.max(a, b));
      return out;
    }
    return null;
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
      m.put("requiredItemKey", q.getRequiredItemKey());
      m.put("requiredItemQty", q.getRequiredItemQty());
      m.put("alsoRequiresItemKey", EXTRA_REQUIRED_ITEM_KEYS.get(q.getId()));
      m.put("minLevel", q.getMinLevel());
      m.put("unlockZoneId", q.getUnlockZoneId());
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
    // NpcFactoryRegistry.registrations() order follows classpath-scan/directory-listing order,
    // which is not guaranteed stable across machines/filesystems - sort by id so re-running this
    // exporter on a different machine (e.g. CI) doesn't produce a pure-reorder diff.
    List<NpcFactoryRegistry.Registration> sortedRegs =
        new ArrayList<>(NpcFactoryRegistry.registrations());
    sortedRegs.sort(Comparator.comparing(NpcFactoryRegistry.Registration::id));
    for (NpcFactoryRegistry.Registration reg : sortedRegs) {
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
      // T4C-0021: previously restricted to NEW_NPC_IDS/ACTIVATED_NPC_IDS, which hid a real
      // acquisition path for any item sold by a pre-existing/legacy NPC (e.g. the +4/+5
      // weapons sold by LordoftheShops) - exportItems() itself is unfiltered (every JSON item,
      // new or legacy), so an item's sources shouldn't be filtered by the seller's newness
      // either. SHOP_EXCLUDED_NPC_IDS still applies (entries that aren't real shop listings).
      //
      // Reading the private M map's own list is only correct for sellers with no runtime
      // override: ShopCatalog#get() special-cases several ids (e.g. ChryseidaYolangda,
      // WitchDoctorKwarlgloth, Fali) to a *different* inventory than their own M entry, and
      // returns null (not a real shop) for others (Boreas, Yolak, ...) despite them having an
      // M entry. get() is the single source of truth the game itself uses, so call it per id
      // instead of trusting M's own value directly.
      java.lang.reflect.Method getMethod = shopCatalog.getDeclaredMethod("get", String.class);
      for (String npcId : new TreeMap<>(m).keySet()) {
        if (SHOP_EXCLUDED_NPC_IDS.contains(npcId)) continue;
        Object behavior = getMethod.invoke(null, npcId);
        if (!(behavior instanceof com.perso.T4C.npc.behavior.ShopBehavior shop)) continue;
        out.put(npcId, shop.items());
      }
    } catch (ReflectiveOperationException ex) {
      System.err.println("Could not read ShopCatalog: " + ex);
    }
    // NPCs can also sell through a dialogue topic's OPEN_SHOP action (e.g. ArchmageThalindra's
    // mantles) rather than a ShopCatalog entry - include those so the items show a real source.
    List<NpcFactoryRegistry.Registration> regs =
        new ArrayList<>(NpcFactoryRegistry.registrations());
    regs.sort(Comparator.comparing(NpcFactoryRegistry.Registration::id));
    for (NpcFactoryRegistry.Registration reg : regs) {
      if (SHOP_EXCLUDED_NPC_IDS.contains(reg.id()) || reg.specification() == null) continue;
      NpcSpec spec = safeSpec(reg);
      if (spec == null) continue;
      Set<String> sold = new LinkedHashSet<>();
      for (NpcSpec.DialogueTopic topic : spec.topics())
        for (NpcSpec.Action action : topic.actions())
          if (action.type() == ActionType.OPEN_SHOP) sold.addAll(action.targets());
      if (sold.isEmpty()) continue;
      List<String> merged = new ArrayList<>();
      Object existing = out.get(reg.id());
      if (existing instanceof List<?> list) for (Object o : list) merged.add(String.valueOf(o));
      for (String item : sold) if (!merged.contains(item)) merged.add(item);
      out.put(reg.id(), merged);
    }
    out.putAll(HAND_MAINTAINED_SHOP_ITEMS);
    return out;
  }

  /** Every real monster-loot source for a tracked item, scanning the FULL monster registry
   * (not just the new/activated ones exportMonsters() documents as their own pages) - the same
   * blind spot exportShops() had: an item's drop source can be a pre-existing/legacy monster
   * (e.g. Deep One / Deep One Boss) that doesn't get its own compendium page. monsterLink() in
   * the site already falls back to plain text for a name with no page, so this is safe to
   * surface even for monsters this compendium doesn't otherwise document. */
  private static List<Map<String, Object>> exportLootSources(Set<String> trackedItemKeys) {
    List<Map<String, Object>> out = new ArrayList<>();
    for (MonsterDef def : MonsterRegistry.load()) {
      if (def.getLoot() == null) continue;
      for (MonsterDef.LootDrop drop : def.getLoot()) {
        if (drop == null || !trackedItemKeys.contains(drop.getItem())) continue;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("item", drop.getItem());
        m.put("monster", def.getName());
        m.put("monsterDisplayName", resolveOrFallback(def.getDisplayName(), def.getName()));
        m.put("chance", drop.getChance());
        out.add(m);
      }
    }
    // MonsterRegistry.load()'s own order follows classpath-scan order, which (like
    // NpcFactoryRegistry.registrations() before it, see T4C-0014) is not guaranteed stable
    // across machines/filesystems - sort so re-running this exporter doesn't produce a
    // pure-reorder diff.
    out.sort(
        Comparator.<Map<String, Object>, String>comparing(m -> (String) m.get("item"))
            .thenComparing(m -> (String) m.get("monster")));
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
    for (String key : NEW_UTILITY_ITEM_KEYS) {
      ItemDefinition d = ItemRegistry.findByKey(key);
      if (d == null) continue;
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("key", d.getKey());
      item.put("name", I18n.resolve(d.getName()));
      item.put("bodyPart", d.getBodyPart());
      item.put("price", d.getPrice());
      item.put("weight", d.getWeight());
      item.put("armorClass", d.getArmorClass());
      Map<String, Object> requirements = new LinkedHashMap<>();
      requirements.put("endurance", d.getMinEnd());
      requirements.put("strength", d.getReqStr());
      requirements.put("agility", d.getReqAgi());
      requirements.put("intelligence", d.getMinInt());
      requirements.put("wisdom", d.getMinWis());
      requirements.put("attack", d.getReqAttack());
      item.put("requirements", requirements);
      item.put("unique", d.isUnique());
      item.put("isBow", d.isBow());
      item.put("dmgFormula", d.getDmgFormula());
      item.put("boosts", List.of());
      // T4C-0036: this loop previously left every utility item looking effectless (no
      // unlimitedUse, no use-effect text) - harmless for the four crafting materials this list
      // originally covered (no ItemSpell entries to show), but item.mana_prism and
      // item.critical_healing_potion are real consumables whose whole point is the spell they
      // trigger on use, so the reference site needs to say so.
      item.put("unlimitedUse", d.isUnlimitedUse());
      List<String> useEffects = new ArrayList<>();
      for (ItemDefinition.ItemSpell spell : d.getSpells()) {
        SpellData effect = SpellRegistry.findById(spell.getSpellId());
        if (effect != null) useEffects.add(I18n.resolve(effect.getDescription()));
      }
      item.put("useEffects", useEffects);
      out.add(item);
    }
    return out;
  }

  // -------------------------------------------------------------- xp curve

  /**
   * The live leveling curve ({@link XpCurve#loadDefault()}, levels 1 to
   * {@link GameConstants#MAX_PLAYER_LEVEL}), for the Systems page's
   * XP-per-level chart. {@code serverXpRate} is {@link GameConstants#SERVER_XP_RATE}, the flat
   * multiplier applied to every monster's granted XP ({@code PlayerProgression#addXp}) - it does
   * not change the curve itself, only how many real kills a given {@code xpToNextLevel} costs.
   */
  private static Map<String, Object> exportXpCurve() {
    List<Map<String, Object>> entries = new ArrayList<>();
    for (XpCurve.Entry e : XpCurve.loadDefault().entries()) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("level", e.getLevel());
      row.put("xpToNextLevel", e.getXpToNextLevel());
      row.put("totalXp", e.getTotalXp());
      entries.add(row);
    }
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("serverXpRate", GameConstants.SERVER_XP_RATE);
    out.put("entries", entries);
    return out;
  }

  // -------------------------------------------------------------- rebirths

  /**
   * One row per rebirth (1 to {@link GameConstants#REBIRTH_MAX_REMORTS}) for the Rebirths page,
   * computed from the same helpers the Oracle, RebirthBehavior and SeraphAuraService use (aura
   * chances are the real per-roll probability, see SeraphAuraService#effectivePercent), plus a
   * plain-language summary of what the rebirth energy points buy. The energy shop costs mirror
   * RemortNPC2-5 (Betran, Caplan, Del Aan, Epilan); keep them in sync if those NPCs change.
   */
  private static Map<String, Object> exportRebirths() {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (int n = 1; n <= GameConstants.REBIRTH_MAX_REMORTS; n++) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("rebirth", n);
      row.put("requiredLevel", RebirthBehavior.requiredLevelFor(n));
      row.put("startingAttributes", RebirthBehavior.startingAttributeFor(n));
      row.put("energyPoints", RebirthBehavior.energyPointsFor(n));
      row.put("auraHealChance", SeraphAuraService.effectivePercent(SeraphAuraService.healingChance(n)));
      row.put("auraRetaliationChance", SeraphAuraService.effectivePercent(SeraphAuraService.retaliationChance(n)));
      row.put("auraBurstChance", SeraphAuraService.effectivePercent(SeraphAuraService.areaBurstChance(n)));
      rows.add(row);
    }
    List<Map<String, Object>> energyShop = new ArrayList<>();
    energyShop.add(energyVendor("Betran", "Attributes",
        "Raises strength, endurance, agility, intelligence or wisdom by 1, up to 10 above your"
            + " new starting value. Each attribute costs more the higher you push it: the 1st"
            + " point costs 1, the 2nd-4th cost 2, the 5th-7th cost 3, the 8th costs 4 and the"
            + " 9th-10th cost 5 (30 points to max one attribute)."));
    energyShop.add(energyVendor("Caplan", "Elemental power",
        "Raises your power in fire, water, air, earth, light or dark by 5. Costs 1 point at"
            + " first, rising as that element's power grows."));
    energyShop.add(energyVendor("Del Aan", "Elemental resistance",
        "Raises your resistance to fire, water, air, earth or dark by 10 for 2 points."));
    energyShop.add(energyVendor("Epilan", "Health and mana",
        "Adds 10 maximum health or 5 maximum mana for 1 point each, with no limit - use it to"
            + " spend whatever is left over."));
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("maxRebirths", GameConstants.REBIRTH_MAX_REMORTS);
    out.put("maxLevel", GameConstants.MAX_PLAYER_LEVEL);
    out.put("firstRebirthLevel", RebirthBehavior.FIRST_REBIRTH_LEVEL);
    out.put("levelPerPreviousRebirth", RebirthBehavior.LEVEL_PER_PREVIOUS_REBIRTH);
    out.put("energyShop", energyShop);
    out.put("rows", rows);
    return out;
  }

  private static Map<String, Object> energyVendor(String npc, String buys, String details) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("npc", npc);
    m.put("buys", buys);
    m.put("details", details);
    return m;
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
