package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;

public final class Loon extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public Loon(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Loon",
        "${monster.loon}",
        293,
        0,
        0,
        0,
        14,
        33,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(
            new MonsterDef.LootDrop("D4 Permit", 0.15f),
            new MonsterDef.LootDrop("H2 Permit", 0.2f)),
        false,
        0.0f,
        32,
        30,
        30,
        35,
        0,
        30,
        0,
        new int[] {86, 86, 86, 86, 57, 5000, 100, 100, 100, 100, 100, 100},
        17,
        78,
        0,
        1075838976,
        10011,
        8,
        288,
        0,
        0,
        0,
        5,
        0,
        0,
        50,
        47,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d20+13", 214, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
