package com.perso.T4C.monster;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.json.MonsterJsonDef;
import java.util.ArrayList;
import java.util.List;

/** Shared combat and loot contract for the three high-level beasts in Drake's Lair. */
final class MythicBeasts {
  record Beast(
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
      String armorFlavor,
      List<String> signatureLoot) {}

  static final Beast CENTAUR =
      new Beast(
          "ironhorn_centaur",
          "Ironhorn Centaur",
          "Centaur",
          "Beast",
          1050,
          300000,
          2500,
          4000,
          4400,
          450,
          600,
          "warrior",
          List.of("moonsteel_halberd"));
  static final Beast PEGASUS =
      new Beast(
          "stormbound_pegasus",
          "Stormbound Pegasus",
          "Pegasus",
          "Pegase",
          1125,
          375000,
          3000,
          4700,
          5100,
          520,
          680,
          "air",
          List.of("skybreaker_longbow", "skybreaker_quiver"));
  static final Beast UNICORN =
      new Beast(
          "galehorn_unicorn",
          "Galehorn Unicorn",
          "Unicorn",
          "Pegase",
          1200,
          450000,
          3500,
          5500,
          5800,
          600,
          780,
          "light",
          List.of("windpiercer_longbow", "windpiercer_quiver"));

  private static final List<String> ARMOR_PIECES =
      List.of("armor", "boots", "gauntlets", "helmet", "leggings", "protector");

  private MythicBeasts() {}

  static MonsterDef definition(Beast beast) {
    MonsterJsonDef d = new MonsterJsonDef();
    d.name = beast.name();
    d.displayName = "${monster." + beast.id() + "}";
    d.level = beast.level();
    d.health = beast.health();
    d.xpPerHit = 12;
    d.xpOnDeath = 2100000000;
    d.hitDamageMin = beast.damageMin();
    d.hitDamageMax = beast.damageMax();
    d.respawnTime = 600000L;
    d.walkPattern = beast.sprite() + "#h";
    d.attackPattern = beast.sprite() + "A#h";
    d.deathPattern = beast.sprite() + "C!" + (beast == CENTAUR ? "o" : "m");
    d.soundAttack = beast.sound() + " Attack.wav";
    d.soundDeath = beast.sound() + " Dying.wav";
    d.soundHit = beast.sound() + " Hit.wav";
    d.gold.min = 3000;
    d.gold.max = 9000;
    d.stats.str = beast.level() * 2;
    d.stats.end = beast.level();
    d.stats.agi = beast.level() * 2;
    d.stats.intel = beast.level();
    d.stats.wis = beast.level();
    d.dodge = beast.dodge();
    d.armorClass.min = beast.armorMin();
    d.armorClass.max = beast.armorMax();
    d.resists = new int[] {110, 110, 110, 110, 110, 110, 100, 100, 100, 100, 100, 100};
    d.aggro = 1;
    d.speed = 125;
    MonsterJsonDef.AttackJson strike = new MonsterJsonDef.AttackJson();
    strike.formula =
        "1d" + (beast.damageMax() - beast.damageMin() + 1) + "+" + (beast.damageMin() - 1);
    strike.combatAttack = beast.level() * 10;
    d.attacks = List.of(strike);
    List<MonsterJsonDef.LootJson> loot = new ArrayList<>();
    for (String key : beast.signatureLoot()) loot.add(drop(key, .015f));
    for (String piece : ARMOR_PIECES) {
      loot.add(drop("ancient_celestial_" + beast.armorFlavor() + "_" + piece, .025f));
      loot.add(drop("empyrean_" + beast.armorFlavor() + "_" + piece, .012f));
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
