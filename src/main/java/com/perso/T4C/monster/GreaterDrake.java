package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Greater Drake", x = 2000, y = 2300, z = 0, stationary = false, aggressive = true)
public final class GreaterDrake extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Greater Drake";

  public GreaterDrake(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Greater Drake",
        "${monster.greater_drake}",
        54943,
        0,
        0,
        40000000,
        676,
        1533,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        899,
        2750,
        java.util.List.of(
            new MonsterDef.LootDrop("heartfire_of_the_greater_drake", 0.01f),
            // T4C-0021: physical/warrior-flavor source for the Ancient Celestial/Empyrean armor
            // sets (ArmorSetGenerator) - previously generated with zero acquisition path.
            new MonsterDef.LootDrop("ancient_celestial_warrior_armor", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_boots", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_gauntlets", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_helmet", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_leggings", 0.025f),
            new MonsterDef.LootDrop("ancient_celestial_warrior_protector", 0.025f),
            new MonsterDef.LootDrop("empyrean_warrior_armor", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_boots", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_gauntlets", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_helmet", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_leggings", 0.012f),
            new MonsterDef.LootDrop("empyrean_warrior_protector", 0.012f)),
        false,
        0.0f,
        515,
        464,
        464,
        615,
        0,
        464,
        0,
        new int[] {63, 5000, 63, -63, 63, 5000, 100, 100, 100, 100, 100, 100},
        500,
        2010,
        0,
        1081032704,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        1,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d858+675", 6010, 75, 0, 0, 1),
            new MonsterDef.Attack("", 0, 75, 10095, 2, 20),
            new MonsterDef.Attack("", 0, 25, 10616, 0, 20)),
        false,
        0,
        java.util.List.of("GreaterDrake"),
        java.util.Map.of());
  }
}
