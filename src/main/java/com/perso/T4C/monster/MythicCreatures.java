package com.perso.T4C.monster;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.json.MonsterJsonDef;
import java.util.ArrayList;
import java.util.List;

/** Combat and loot for the approved dragon, griffin and hydra creature art. */
final class MythicCreatures {
  record Creature(
      String id,
      String name,
      String sprite,
      String sound,
      int level,
      int health,
      int damageMin,
      int damageMax,
      int dodge,
      int armorMin,
      int armorMax,
      int speed,
      int favoredResist,
      String armorFlavor,
      List<String> signatureLoot) {}

  static final Creature DRAGON =
      new Creature(
          "obsidian_dragon",
          "Obsidian Dragon",
          "ObsidianDragon",
          "Kraanian",
          1250,
          600000,
          4200,
          6800,
          6200,
          700,
          900,
          125,
          5,
          "dark",
          List.of("duskmaw_wyrms_eclipsed_crown"));
  static final Creature GRIFFIN =
      new Creature(
          "bronze_griffin",
          "Bronze Griffin",
          "BronzeGriffin",
          "Pegase",
          1325,
          550000,
          4800,
          7200,
          7100,
          650,
          850,
          150,
          0,
          "archer",
          List.of("skybreaker_longbow", "skybreaker_quiver"));
  static final Creature HYDRA =
      new Creature(
          "swamp_hydra",
          "Swamp Hydra",
          "SwampHydra",
          "Beast",
          1400,
          850000,
          5200,
          8200,
          6300,
          800,
          1050,
          110,
          3,
          "earth",
          List.of("rootcrown_wyrms_ageless_mantle"));

  private static final List<String> ARMOR_PIECES =
      List.of("armor", "boots", "gauntlets", "helmet", "leggings", "protector");

  private MythicCreatures() {}

  static MonsterDef definition(Creature creature) {
    MonsterJsonDef d = new MonsterJsonDef();
    d.name = creature.name();
    d.displayName = "${monster." + creature.id() + "}";
    d.level = creature.level();
    d.health = creature.health();
    d.xpPerHit = 14;
    d.xpOnDeath = 2100000000;
    d.hitDamageMin = creature.damageMin();
    d.hitDamageMax = creature.damageMax();
    d.respawnTime = 900000L;
    d.walkPattern = creature.sprite() + "#h";
    d.attackPattern = creature.sprite() + "A#h";
    d.deathPattern = creature.sprite() + "C!h";
    d.soundAttack = creature.sound() + " Attack.wav";
    d.soundDeath = creature.sound() + " Dying.wav";
    d.soundHit = creature.sound() + " Hit.wav";
    d.gold.min = 5000;
    d.gold.max = 12000;
    d.stats.str = creature.level() * 2;
    d.stats.end = creature.level() * 2;
    d.stats.agi = creature.level() * 2;
    d.stats.intel = creature.level();
    d.stats.wis = creature.level();
    d.dodge = creature.dodge();
    d.armorClass.min = creature.armorMin();
    d.armorClass.max = creature.armorMax();
    d.resists = new int[] {120, 120, 120, 120, 120, 120, 100, 100, 100, 100, 100, 100};
    d.resists[creature.favoredResist()] = 220;
    d.aggro = 1;
    d.speed = creature.speed();
    MonsterJsonDef.AttackJson strike = new MonsterJsonDef.AttackJson();
    strike.formula =
        "1d" + (creature.damageMax() - creature.damageMin() + 1) + "+" + (creature.damageMin() - 1);
    strike.combatAttack = creature.level() * 10;
    d.attacks = List.of(strike);
    List<MonsterJsonDef.LootJson> loot = new ArrayList<>();
    for (String key : creature.signatureLoot()) loot.add(drop(key, .015f));
    for (String piece : ARMOR_PIECES) {
      loot.add(drop("ancient_celestial_" + creature.armorFlavor() + "_" + piece, .025f));
      loot.add(drop("empyrean_" + creature.armorFlavor() + "_" + piece, .012f));
    }
    loot.add(drop("serious_healing_potion", .25f));
    loot.add(drop("mana_elixir", .25f));
    d.loot = loot;
    return d.toMonsterDef();
  }

  private static MonsterJsonDef.LootJson drop(String key, float chance) {
    MonsterJsonDef.LootJson drop = new MonsterJsonDef.LootJson();
    drop.item = key;
    drop.chance = chance;
    return drop;
  }
}
