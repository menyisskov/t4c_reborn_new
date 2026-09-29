package com.perso.T4C.monster;

import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Builds distinct campaign enemies using animation families already shipped with the game. */
final class EndgameMonsterFactory {
  private static final float WITNESS_HUNTING_DROP_CHANCE = .006f;
  private static final float WITNESS_BOSS_DROP_CHANCE = .005f;

  private EndgameMonsterFactory() {}

  static MonsterDef withWitnessLoot(MonsterDef base, boolean boss, String... itemKeys) {
    List<MonsterDef.LootDrop> loot = new ArrayList<>(base.getLoot());
    float chance = boss ? WITNESS_BOSS_DROP_CHANCE : WITNESS_HUNTING_DROP_CHANCE;
    for (String key : itemKeys) {
      loot.add(new MonsterDef.LootDrop(key, chance));
    }
    return base.withLoot(loot);
  }

  static MonsterDef create(
      String name,
      String key,
      MonsterDef art,
      int level,
      int health,
      int xp,
      int damage,
      boolean boss) {
    int min = Math.max(1, damage * 2 / 3);
    return new MonsterDef(
        name,
        "${monster." + key + "}",
        health,
        art.getMana(),
        boss ? 8 : 5,
        xp,
        min,
        damage,
        boss ? 120000L : 30000L,
        art.getWalkPattern(),
        art.getAttackPattern(),
        art.getDeathPattern(),
        art.getSoundAttack(),
        art.getSoundDeath(),
        art.getSoundHit(),
        MonsterGoldCurve.goldMin(level),
        MonsterGoldCurve.goldMax(level),
        boss
            ? List.of(
                new MonsterDef.LootDrop("serious_healing_potion", .5f),
                new MonsterDef.LootDrop("mana_elixir", .4f))
            : List.of(
                new MonsterDef.LootDrop("serious_healing_potion", .08f),
                new MonsterDef.LootDrop("potion_of_mana", .08f)),
        false,
        0f,
        level,
        level,
        level,
        level / 2,
        0,
        level / 2,
        0,
        art.getResists(),
        level,
        level * 4,
        0,
        level / 2,
        art.getAppearance(),
        art.getItemBody(),
        art.getItemFeet(),
        art.getItemHands(),
        art.getItemHead(),
        art.getItemLegs(),
        art.getItemWeapon(),
        art.getItemShield(),
        art.getItemBack(),
        100,
        art.getClan(),
        art.getSpeed(),
        true,
        List.of(
            new MonsterDef.Attack(
                "1d" + Math.max(1, damage - min) + "+" + min, 1000 + level * 8, 100, 0, 0, 0)),
        false,
        0,
        List.of(name),
        Map.of());
  }
}
