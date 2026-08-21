package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "GORLOKBLOODAXE", x = 2605, y = 1200, z = 0, stationary = false, aggressive = true)
public final class GorlokBloodaxe extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public GorlokBloodaxe(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "GORLOKBLOODAXE",
        "${monster.gorlokbloodaxe}",
        463,
        0,
        17,
        4050,
        22,
        50,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        40,
        37,
        37,
        45,
        37,
        37,
        20,
        new int[] {82, 82, 82, 82, 54, 5000, 100, 100, 100, 100, 100, 100},
        25,
        110,
        0,
        1094713344,
        10011,
        269,
        265,
        259,
        270,
        268,
        7,
        272,
        287,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d29+21", 310, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
