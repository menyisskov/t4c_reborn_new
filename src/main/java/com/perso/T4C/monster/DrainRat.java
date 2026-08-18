package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DrainRat extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public static final String CANONICAL_NAME = "Drain Rat";

  public DrainRat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Drain Rat",
        "${monster.drain_rat}",
        631,
        0,
        3,
        1052,
        28,
        43,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC#j",
        "Rat Attack.wav",
        "Rat Dying.wav",
        "Rat Hit.wav",
        53,
        165,
        java.util.List.of(
            new MonsterDef.LootDrop("Ring of the forester", 0.004f),
            new MonsterDef.LootDrop("Drachenstaff", 0.005f),
            new MonsterDef.LootDrop("Raindrop", 0.02f),
            new MonsterDef.LootDrop("Scroll of minor combat sense", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {106, 53, 79, 79, 79, 5000, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1077477376,
        20003,
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
            new MonsterDef.Attack("1d16+27", 370, 50, 0, 0, 1),
            new MonsterDef.Attack("", 0, 50, 10648, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
