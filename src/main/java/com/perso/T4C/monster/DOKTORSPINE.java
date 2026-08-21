package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "DOKTORSPINE", x = 2831, y = 2331, z = 0, stationary = false, aggressive = true)
public final class DOKTORSPINE extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public DOKTORSPINE(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "DOKTORSPINE",
        "${monster.doktorspine}",
        581,
        0,
        19,
        5771,
        28,
        63,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        53,
        165,
        java.util.List.of(),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        41,
        41,
        21,
        new int[] {79, 79, 79, 79, 53, 5000, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1097859072,
        10011,
        278,
        288,
        0,
        276,
        261,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d36+27", 370, 1, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
