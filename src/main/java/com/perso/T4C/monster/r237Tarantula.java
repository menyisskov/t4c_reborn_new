package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r237Tarantula extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public r237Tarantula(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Tarantula",
        "${monster.tarantula}",
        606,
        0,
        4,
        1235,
        29,
        66,
        30000L,
        "Tarantula#m",
        "Tarantula#m",
        "TarantulaC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        55,
        170,
        java.util.List.of(new MonsterDef.LootDrop("Tarantula eyes", 0.03f)),
        false,
        0.0f,
        46,
        42,
        42,
        52,
        0,
        42,
        0,
        new int[] {105, 52, 79, 79, 79, 5000, 100, 100, 100, 100, 100, 100},
        31,
        134,
        0,
        1075052544,
        20033,
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
        java.util.List.of(new MonsterDef.Attack("1d38+28", 432, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("Dark Tarantula"),
        java.util.Map.of());
  }
}
