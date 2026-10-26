package com.perso.T4C.divine;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.monster.json.MonsterJsonDef;
import com.perso.T4C.monster.loot.LootTable;
import java.util.ArrayList;
import java.util.List;

/** The Concord's shrines share a combat and loot contract, while retaining distinct identities. */
public final class DivinePantheon {
  public record God(
      String id,
      String name,
      String school,
      int tier,
      int level,
      int x,
      int y,
      boolean female,
      int itemBase) {
    public String scrollKey() {
      return "divine_" + id + "_return_scroll";
    }

    public int returnSpellId() {
      return 18000 + (itemBase - 55000) / 100;
    }

    public int judgmentSpellId() {
      return returnSpellId() + 100;
    }

    public long respawnMillis() {
      return tier >= 3 ? 600000L : tier == 2 ? 450000L : 300000L;
    }

    public String equipmentKey(String piece) {
      return "divine_" + id + "_" + piece;
    }

    public int health() {
      return switch (tier) {
        case 0 -> 66000;
        case 1 -> 135000;
        case 2 -> 460000;
        default -> 1200000 + Math.max(0, level - 380) * 20000;
      };
    }

    public int damage() {
      return switch (tier) {
        case 0 -> id.equals("artherk") ? 110 : 132;
        case 1 -> 880;
        case 2 -> 1650;
        default -> 3000 + Math.max(0, level - 380) * 30;
      };
    }

    public int judgmentDamageMin() {
      return damage() / 2 * judgmentMultiplier();
    }

    public int judgmentDamageMax() {
      return damage() * judgmentMultiplier();
    }

    private int judgmentMultiplier() {
      if (tier == 0 || school.equals("warrior") || school.equals("archer")) return 1;
      return tier >= 2 ? 3 : 2;
    }
  }

  private static final List<God> GODS =
      List.of(
          new God("demeter", "Demeter", "earth", 1, 240, 3835, 1440, true, 55000),
          new God("njord", "Njord", "water", 1, 250, 3738, 780, false, 55100),
          new God("ra", "Ra", "fire", 1, 260, 4080, 2530, false, 55200),
          new God("artemis", "Artemis", "archer", 1, 270, 4050, 675, true, 55300),
          new God("athena", "Athena", "warrior", 2, 340, 4140, 1432, true, 55400),
          new God("apollo", "Apollo", "light", 2, 350, 3888, 720, false, 55500),
          new God("anubis", "Anubis", "dark", 2, 360, 4465, 2060, false, 55600),
          new God("thor", "Thor", "air", 2, 370, 5540, 1320, false, 55700),
          new God("zeus", "Zeus", "air", 3, 380, 5715, 1130, false, 56000),
          new God("odin", "Odin", "warrior", 3, 390, 5410, 1830, false, 56100),
          new God("osiris", "Osiris", "light", 3, 400, 5800, 2270, false, 56200),
          new God("artherk", "Artherk", "light", 0, 100, 5370, 1660, false, 55800),
          new God("ogrimar", "Ogrimar", "dark", 0, 110, 5880, 2000, false, 55900));

  private DivinePantheon() {}

  public static List<God> all() {
    return GODS;
  }

  public static God find(String id) {
    return GODS.stream()
        .filter(g -> g.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown god: " + id));
  }

  public static God findJudgment(int spellId) {
    return GODS.stream().filter(god -> god.judgmentSpellId() == spellId).findFirst().orElse(null);
  }

  public static boolean isEmbodiment(String canonicalName) {
    return GODS.stream().anyMatch(god -> ("Divine " + god.name()).equals(canonicalName));
  }

  public static List<String> equipmentKeys(God god) {
    boolean physical = god.school().equals("warrior") || god.school().equals("archer");
    List<String> pieces =
        new ArrayList<>(
            List.of(
                "weapon",
                physical ? "armor" : "robe",
                "boots",
                "gauntlets",
                "helm",
                "leggings",
                "wings",
                "necklace",
                "ring",
                "bracelet"));
    if (!seraphQuestKeys(god).isEmpty())
      pieces.addAll(
          List.of(
              "plate",
              "warblade",
              "plate_boots",
              "plate_gauntlets",
              "plate_helm",
              "plate_leggings"));
    return pieces.stream().map(god::equipmentKey).toList();
  }

  public static MonsterDef definition(String id) {
    God god = find(id);
    MonsterJsonDef d = new MonsterJsonDef();
    d.name = "Divine " + god.name();
    d.displayName = "${monster.divine_" + id + "}";
    d.level = god.level();
    d.health = god.health();
    d.mana = god.level() * 30;
    d.xpPerHit = 5;
    d.xpOnDeath = god.tier() == 0 ? god.level() * 1500 : god.tier() * 12000000;
    d.hitDamageMin = god.damage() / 2;
    d.hitDamageMax = god.damage();
    d.respawnTime = god.respawnMillis();
    d.appearance = god.female() ? 10012 : 10011;
    d.soundAttack = "Whooshh 1.wav";
    d.soundDeath = "Male Dying 1.wav";
    d.soundHit = "Male Hit 1.wav";
    d.gold.min = MonsterGoldCurve.goldMin(god.level()) * 5;
    d.gold.max = MonsterGoldCurve.goldMax(god.level()) * 5;
    d.stats.str = god.level() * 2;
    d.stats.end = god.level();
    d.stats.agi = god.level();
    d.stats.intel = god.level() * 2;
    d.stats.wis = god.level() * 2;
    d.dodge = god.level() * (god.tier() >= 3 ? 5 : 4);
    double armorMultiplier = god.tier() >= 3 ? 4.8 : 1.15;
    d.armorClass.min = (int) Math.round(god.level() / 4d * armorMultiplier);
    d.armorClass.max = (int) Math.round(god.level() / 3d * armorMultiplier);
    d.aggro = 0;
    // The embodiments have no hostile clan affiliation.
    d.clan = 0;
    int baseResistance = god.tier() >= 3 ? 140 : 80;
    d.resists =
        new int[] {
          baseResistance,
          baseResistance,
          baseResistance,
          baseResistance,
          baseResistance,
          baseResistance,
          100,
          100,
          100,
          100,
          100,
          100
        };
    int schoolIndex =
        switch (god.school()) {
          case "air" -> 0;
          case "fire" -> 1;
          case "water" -> 2;
          case "earth" -> 3;
          case "light" -> 4;
          case "dark" -> 5;
          default -> -1;
        };
    if (schoolIndex >= 0) d.resists[schoolIndex] = god.tier() >= 3 ? 220 : 120 + god.tier() * 20;
    d.equipment.body = god.itemBase() + (!seraphQuestKeys(god).isEmpty() ? 10 : 1);
    d.equipment.feet = god.itemBase() + 2;
    d.equipment.hands = god.itemBase() + 3;
    // Athena wears her war helm; the other embodiments keep their human faces visible.
    d.equipment.head = god.school().equals("warrior") ? god.itemBase() + 4 : 0;
    d.equipment.legs = god.itemBase() + 5;
    d.equipment.back = god.itemBase() + 6;
    d.equipment.weapon = god.itemBase();
    MonsterJsonDef.AttackJson melee = new MonsterJsonDef.AttackJson();
    melee.formula = "1d" + (god.damage() / 2) + "+" + (god.damage() / 2);
    melee.combatAttack = god.level() * 10;
    melee.percentage = 35;
    MonsterJsonDef.AttackJson judgment = new MonsterJsonDef.AttackJson();
    judgment.spellId = god.judgmentSpellId();
    judgment.combatAttack = god.level() * 10;
    judgment.percentage = 65;
    judgment.maxRange = god.school().equals("warrior") ? 2 : 12;
    List<MonsterJsonDef.AttackJson> attacks = new ArrayList<>();
    attacks.add(melee);
    if (!god.school().equals("warrior")) attacks.add(judgment);
    for (int spellId : DivineAfflictions.spellIds(god)) {
      MonsterJsonDef.AttackJson debuff = new MonsterJsonDef.AttackJson();
      debuff.spellId = spellId;
      debuff.combatAttack = god.level() * 10;
      debuff.percentage = 12;
      debuff.maxRange = god.school().equals("warrior") ? 1 : 12;
      attacks.add(debuff);
    }
    d.attacks = attacks;
    List<MonsterJsonDef.LootJson> loot = new ArrayList<>();
    List<String> equipment = equipmentKeys(god);
    boolean seraphTrial = !seraphQuestKeys(god).isEmpty();
    for (String key : equipment) addLoot(loot, key, seraphTrial ? .5f / equipment.size() : .02f);
    addLoot(loot, god.scrollKey(), .01f);
    if (seraphTrial) {
      for (String key : seraphQuestKeys(god)) addLoot(loot, key, 1f / seraphQuestKeys(god).size());
    }
    d.loot = loot;
    return d.toMonsterDef();
  }

  public static List<String> seraphQuestKeys(God god) {
    return switch (god.id()) {
      case "artherk" -> List.of("heart_of_artherk", "soul_of_artherk", "will_of_artherk");
      case "ogrimar" -> List.of("blood_of_ogrimar", "bones_of_ogrimar", "skull_of_ogrimar");
      default -> List.of();
    };
  }

  /** Definition metadata lists marginal odds; these groups enforce the per-kill contract. */
  static LootTable seraphLoot(God god, MonsterDef definition) {
    return LootTable.builder()
        .gold(definition.getGoldMin(), definition.getGoldMax())
        .item(god.scrollKey(), .01f)
        .oneOf(1f, seraphQuestKeys(god))
        .oneOf(.5f, equipmentKeys(god))
        .build();
  }

  private static void addLoot(List<MonsterJsonDef.LootJson> loot, String key, float chance) {
    MonsterJsonDef.LootJson drop = new MonsterJsonDef.LootJson();
    drop.item = key;
    drop.chance = chance;
    loot.add(drop);
  }
}
