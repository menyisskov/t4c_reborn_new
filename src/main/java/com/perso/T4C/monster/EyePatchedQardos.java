package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Eye-Patched Qardos", x = 879, y = 2441, z = 0, stationary = false, aggressive = true)
public final class EyePatchedQardos extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Eye-Patched Qardos";

  public EyePatchedQardos(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Eye-Patched Qardos",
        "${monster.eye_patched_qardos}",
        1162,
        0,
        3,
        2314,
        28,
        63,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        106,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Rhodar hammer", 0.06f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.008f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {79, 79, 79, 79, 53, 5000, 100, 100, 100, 100, 100, 100},
        30,
        165,
        0,
        1075838976,
        20042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        23,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 20, 10122, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
