package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DesperatePrisoner extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public static final String CANONICAL_NAME = "Desperate Prisoner";

  public DesperatePrisoner(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Desperate Prisoner",
        "${monster.desperate_prisoner}",
        440,
        0,
        3,
        765,
        21,
        48,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Rat Attack.wav",
        "Rat Dying.wav",
        "Rat Hit.wav",
        43,
        132,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Rusted short sword", 0.05f)),
        false,
        0.0f,
        39,
        36,
        36,
        43,
        0,
        36,
        0,
        new int[] {82, 82, 82, 82, 55, 5000, 100, 100, 100, 100, 100, 100},
        24,
        106,
        0,
        1076363264,
        20006,
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
        java.util.List.of(new MonsterDef.Attack("1d28+20", 298, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
