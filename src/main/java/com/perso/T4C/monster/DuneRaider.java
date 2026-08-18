package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DuneRaider extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Dune Raider";

  public DuneRaider(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dune Raider",
        "${monster.dune_raider}",
        440,
        0,
        3,
        754,
        21,
        48,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        43,
        132,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.01f),
            new MonsterDef.LootDrop("Ring of the bear", 0.01f),
            new MonsterDef.LootDrop("Chaos Sword", 0.01f)),
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
