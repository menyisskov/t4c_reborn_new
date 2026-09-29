package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.spawn.Spawn;

// Raiders on Stoneheim's eastern shore road near (420,730), blocking the Witness Isles passage.
// Reuses the
// legacy "Thief" human-raider animation/sound family (same one r197Raider uses) at fork-tier
// stats, since no dedicated smuggler sprite exists yet.
@Spawn(type = "Tideworn Reaver", x = 390, y = 680, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 410, y = 715, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 435, y = 750, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 450, y = 690, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 405, y = 765, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 445, y = 770, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 480, y = 780, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 365, y = 760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 425, y = 735, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Tideworn Reaver", x = 400, y = 720, z = 0, stationary = false, aggressive = true)
public final class TidewornReaver extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Tideworn Reaver";

  public TidewornReaver(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Tideworn Reaver",
        "${monster.tideworn_reaver}",
        29000,
        0,
        5,
        240000,
        150,
        330,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        MonsterGoldCurve.goldMin(280),
        MonsterGoldCurve.goldMax(280),
        java.util.List.of(new MonsterDef.LootDrop("mana_potion", 0.06f)),
        false,
        0.0f,
        250,
        230,
        230,
        190,
        0,
        250,
        0,
        new int[] {110, 120, 95, 80, 70, 130, 100, 100, 100, 100, 100, 100},
        280,
        1080,
        0,
        165,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d410+355", 3300, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("TidewornReaver"),
        java.util.Map.of());
  }
}
