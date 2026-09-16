package com.perso.T4C.monster.json;

import com.perso.T4C.monster.core.MonsterDef;
import java.util.List;
import java.util.Map;

/**
 * Friendlier, Gson-deserializable shape for authoring monsters in JSON. Fields default to
 * sensible values so an author only needs to specify what makes the monster distinct, then {@link
 * #toMonsterDef()} expands it into the full {@link MonsterDef} the game engine expects.
 */
public class MonsterJsonDef {
  public String name;
  public String displayName;
  public int health = 10;
  public int mana = 0;
  public int xpPerHit = 1;
  public int xpOnDeath = 1;
  public int hitDamageMin = 1;
  public int hitDamageMax = 2;
  public long respawnTime = 30000L;
  public String walkPattern = "";
  public String attackPattern = "";
  public String deathPattern = "";
  public String soundAttack = "";
  public String soundDeath = "";
  public String soundHit = "";
  public GoldRange gold = new GoldRange();
  public List<LootJson> loot = List.of();
  public boolean animateWhileStationary = false;
  public float stationaryAnimationPauseSeconds = 0.0f;
  public Stats stats = new Stats();
  public int[] resists = new int[12];
  public int level = 1;
  public int dodge = 0;
  public ArmorClass armorClass = new ArmorClass();
  public int appearance = 0;
  public Equipment equipment = new Equipment();
  public int aggro = 0;
  public int clan = 0;
  public int speed = 0;
  public boolean canAttack = true;
  public List<AttackJson> attacks = List.of();
  public boolean tameable = false;
  public int tameMaxLevel = 0;
  public List<String> spawnAliases = List.of();
  public Map<String, String> sourceEvents = Map.of();

  public MonsterDef toMonsterDef() {
    if (name == null || name.isBlank()) {
      throw new IllegalStateException("Monster JSON definition is missing required field 'name'");
    }
    List<MonsterDef.LootDrop> lootDrops =
        loot.stream().map(l -> new MonsterDef.LootDrop(l.item, l.chance)).toList();
    List<MonsterDef.Attack> attackDefs =
        attacks.stream()
            .map(
                a ->
                    new MonsterDef.Attack(
                        a.formula == null ? "" : a.formula,
                        a.combatAttack,
                        a.percentage,
                        a.spellId,
                        a.minRange,
                        a.maxRange))
            .toList();
    int[] resistValues = resists != null && resists.length == 12 ? resists : new int[12];
    return new MonsterDef(
        name,
        displayName == null ? name : displayName,
        health,
        mana,
        xpPerHit,
        xpOnDeath,
        hitDamageMin,
        hitDamageMax,
        respawnTime,
        walkPattern,
        attackPattern,
        deathPattern,
        soundAttack,
        soundDeath,
        soundHit,
        gold.min,
        gold.max,
        lootDrops,
        animateWhileStationary,
        stationaryAnimationPauseSeconds,
        stats.str,
        stats.end,
        stats.agi,
        stats.intel,
        stats.will,
        stats.wis,
        stats.luck,
        resistValues,
        level,
        dodge,
        armorClass.min,
        armorClass.max,
        appearance,
        equipment.body,
        equipment.feet,
        equipment.hands,
        equipment.head,
        equipment.legs,
        equipment.weapon,
        equipment.shield,
        equipment.back,
        aggro,
        clan,
        speed,
        canAttack,
        attackDefs,
        tameable,
        tameMaxLevel,
        spawnAliases,
        sourceEvents);
  }

  public static final class GoldRange {
    public int min = 0;
    public int max = 0;
  }

  public static final class Stats {
    public int str = 10;
    public int end = 10;
    public int agi = 10;
    public int intel = 10;
    public int will = 0;
    public int wis = 10;
    public int luck = 0;
  }

  public static final class ArmorClass {
    public int min = 0;
    public int max = 0;
  }

  public static final class Equipment {
    public int body = 0;
    public int feet = 0;
    public int hands = 0;
    public int head = 0;
    public int legs = 0;
    public int weapon = 0;
    public int shield = 0;
    public int back = 0;
  }

  public static final class LootJson {
    public String item;
    public float chance;
  }

  public static final class AttackJson {
    public String formula = "";
    public int combatAttack = 1;
    public int percentage = 100;
    public int spellId = 0;
    public int minRange = 0;
    public int maxRange = 0;
  }
}
