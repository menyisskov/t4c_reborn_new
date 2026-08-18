package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Bend Hayjes", x = 456, y = 1909, z = 1, stationary = false, aggressive = true)
public final class BendHayjes extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Bend Hayjes";

  public BendHayjes(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bend Hayjes",
        "${monster.bend_hayjes}",
        1620,
        0,
        4,
        3745,
        39,
        87,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        136,
        418,
        java.util.List.of(
            new MonsterDef.LootDrop("Mummy bandages", 0.05f),
            new MonsterDef.LootDrop("Mana elixir", 0.05f)),
        false,
        0.0f,
        53,
        49,
        49,
        60,
        0,
        49,
        0,
        new int[] {76, 76, 101, 50, 5025, 50, 100, 100, 100, 100, 100, 100},
        38,
        160,
        0,
        1076756480,
        20011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d49+38", 465, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10119, 7, 14),
            new MonsterDef.Attack("", 0, 50, 10090, 7, 14),
            new MonsterDef.Attack("", 0, 100, 10120, 3, 6)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
