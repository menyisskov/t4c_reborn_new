package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Barrow Wight", x = 2470, y = 2670, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Barrow Wight", x = 2530, y = 2730, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Barrow Wight", x = 2460, y = 2740, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Barrow Wight", x = 2540, y = 2660, z = 0, stationary = false, aggressive = true)
public final class BarrowWight extends DataMonster {
  // Reuses the Mummy animation/sound family — a dry, land-bound undead, distinct from the
  // Zombie-family Drowned Acolytes of the Sunken Chancel.
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Mummy Dying.wav";
  public static final String SOUND_HIT = "Mummy Hit.wav";

  public static final String CANONICAL_NAME = "Barrow Wight";

  public BarrowWight(MonsterDef definition, float x, float y) throws GameException {
    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {
    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Barrow Wight",
        "${monster.barrow_wight}",
        12050,
        0,
        9,
        45000,
        220,
        380,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC!a",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        260,
        800,
        java.util.List.of(new MonsterDef.LootDrop("wight_bound_amulet", 0.03f)),
        false,
        0.0f,
        160,
        150,
        140,
        130,
        0,
        130,
        0,
        new int[] {100, 100, 100, 90, 170, 30, 100, 100, 100, 100, 100, 100},
        165,
        500,
        0,
        110,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        75,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d260+210", 1700, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("BarrowWight"),
        java.util.Map.of());
  }
}
