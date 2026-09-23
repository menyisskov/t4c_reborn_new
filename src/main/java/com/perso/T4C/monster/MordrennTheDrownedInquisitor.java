package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Mordrenn", x = 1750, y = 2300, z = 0, stationary = false, aggressive = true)
public final class MordrennTheDrownedInquisitor extends DataMonster {
  // Reuses the "Skeleton King" boss puppet/sound family (see VICARRAMIEL for the same
  // precedent) — the tallest, most ornate undead humanoid rig already in the sprite set.
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public static final String CANONICAL_NAME = "Mordrenn the Drowned Inquisitor";

  public MordrennTheDrownedInquisitor(MonsterDef definition, float x, float y)
      throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Mordrenn the Drowned Inquisitor",
        "${monster.mordrenn}",
        1450,
        0,
        0,
        55000,
        70,
        120,
        30000L,
        "64kSkeletonKing#m",
        "64kSkeletonKingA#k",
        "64kSkeletonKingC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        90,
        275,
        java.util.List.of(
            new MonsterDef.LootDrop("mordrenns_drowned_cowl", 0.01f),
            // T4C-0021: water-flavor source for the Ancient Celestial/Empyrean armor sets
            // (ArmorSetGenerator) - previously generated with zero acquisition path.
            new MonsterDef.LootDrop("ancient_celestial_water_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_water_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_water_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_water_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_water_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_water_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_water_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_water_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_water_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_water_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_water_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_water_protector", 0.012f)),
        false,
        0.0f,
        60,
        58,
        58,
        75,
        80,
        60,
        0,
        new int[] {90, 100, 180, 70, 220, 25, 100, 100, 100, 100, 100, 100},
        50,
        210,
        0,
        55,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d95+72", 950, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Mordrenn", "DrownedInquisitor"),
        java.util.Map.of());
  }
}
