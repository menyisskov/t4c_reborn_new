package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Bulldaoza", x = 375, y = 987, z = 1, stationary = false, aggressive = true)
public final class Bulldaoza extends DataMonster {
  public static final String SOUND_ATTACK = "Minotaur Attack.wav";
  public static final String SOUND_DEATH = "Minotaur Dying.wav";
  public static final String SOUND_HIT = "Minotaur Hit.wav";

  public static final String CANONICAL_NAME = "Bulldaoza";

  public Bulldaoza(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bulldaoza",
        "${monster.bulldaoza}",
        1812,
        0,
        5,
        4661,
        45,
        102,
        30000L,
        "Minotaur#f",
        "MinotaurA#i",
        "MinotaurC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        150,
        461,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of fury", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Skeleton bone", 0.01f)),
        false,
        0.0f,
        57,
        52,
        52,
        65,
        0,
        52,
        0,
        new int[] {98, 49, 74, 74, 74, 5000, 100, 100, 100, 100, 100, 100},
        42,
        178,
        0,
        1077215232,
        20014,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d58+44", 514, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
