package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class JadeKingsnake extends DataMonster {
  public static final String SOUND_ATTACK = "Snake Attack.wav";
  public static final String SOUND_DEATH = "Snake Dying.wav";
  public static final String SOUND_HIT = "Snake Hit.wav";

  public static final String CANONICAL_NAME = "Jade Kingsnake";

  public JadeKingsnake(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Jade Kingsnake",
        "${monster.jade_kingsnake}",
        581,
        0,
        3,
        1158,
        41,
        50,
        30000L,
        "Snake#h",
        "SnakeA#g",
        "SnakeC#m",
        "Snake Attack.wav",
        "Snake Dying.wav",
        "Snake Hit.wav",
        53,
        165,
        java.util.List.of(
            new MonsterDef.LootDrop("Drachenhelm", 0.005f),
            new MonsterDef.LootDrop("Grim sword of war", 0.02f),
            new MonsterDef.LootDrop("Scroll of orientation center", 0.01f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {79, 79, 53, 106, 79, 5000, 100, 100, 100, 100, 100, 100},
        30,
        100,
        0,
        1075052544,
        20019,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d10+40", 450, 50, 0, 0, 1),
            new MonsterDef.Attack("", 0, 50, 10649, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
