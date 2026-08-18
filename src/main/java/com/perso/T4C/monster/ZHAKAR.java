package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "ZHAKAR", x = 55, y = 1769, z = 0, stationary = false, aggressive = false)
public final class ZHAKAR extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public ZHAKAR(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ZHAKAR",
        "${monster.zhakar}",
        1000000,
        0,
        0,
        0,
        128,
        291,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        340,
        1044,
        java.util.List.of(),
        false,
        0.0f,
        110,
        100,
        100,
        129,
        100,
        100,
        34,
        new int[] {49, 49, 49, 49, 65, 5000, 150, 150, 150, 150, 150, 150},
        95,
        65535,
        0,
        1232348160,
        10011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d164+127", 1150, 33, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
