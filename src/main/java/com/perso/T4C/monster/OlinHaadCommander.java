package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "OLINHAADCOMMANDER", x = 2973, y = 246, z = 0, stationary = false, aggressive = true)
public final class OlinHaadCommander extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public OlinHaadCommander(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "OLINHAADCOMMANDER",
        "${monster.olinhaadcommander}",
        254,
        0,
        0,
        0,
        13,
        29,
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
        30,
        28,
        28,
        33,
        28,
        28,
        17,
        new int[] {86, 86, 86, 86, 57, 5000, 100, 100, 100, 100, 100, 100},
        15,
        70,
        0,
        1088421888,
        10011,
        269,
        265,
        259,
        270,
        268,
        277,
        273,
        287,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d17+12", 190, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
