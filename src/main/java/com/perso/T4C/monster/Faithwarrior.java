package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Faithwarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 3.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Faith warrior";

  public Faithwarrior(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Faith warrior",
        "${monster.faith_warrior}",
        244,
        0,
        2,
        362,
        14,
        36,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        26,
        82,
        java.util.List.of(
            new MonsterDef.LootDrop("Sword of Light", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Studded leather armor", 0.01f),
            new MonsterDef.LootDrop("Studded leather pants", 0.01f),
            new MonsterDef.LootDrop("Potion of fury", 0.02f)),
        false,
        0.0f,
        30,
        28,
        28,
        33,
        0,
        28,
        0,
        new int[] {86, 86, 86, 86, 115, 5000, 100, 100, 100, 100, 100, 100},
        15,
        50,
        0,
        1075052544,
        10010,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        46,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d23+13", 190, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
