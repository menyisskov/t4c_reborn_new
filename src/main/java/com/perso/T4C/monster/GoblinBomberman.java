package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class GoblinBomberman extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Goblin Bomberman";

  public GoblinBomberman(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Goblin Bomberman",
        "${monster.goblin_bomberman}",
        254,
        0,
        2,
        337,
        13,
        29,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC!o",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        26,
        82,
        java.util.List.of(),
        false,
        0.0f,
        30,
        28,
        28,
        33,
        0,
        28,
        0,
        new int[] {115, 57, 86, 86, 86, 5025, 100, 100, 100, 100, 100, 100},
        15,
        70,
        0,
        1075576832,
        20001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        20,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d17+12", 190, 95, 0, 0, 0),
            new MonsterDef.Attack("", 0, 5, 10219, 0, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
