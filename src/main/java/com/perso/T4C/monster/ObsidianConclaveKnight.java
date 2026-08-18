package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "MOBOBSIDIANCONCLAVEKNIGHT",
    x = 567,
    y = 764,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "MOBOBSIDIANCONCLAVEKNIGHT",
    x = 568,
    y = 771,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "MOBOBSIDIANCONCLAVEKNIGHT",
    x = 573,
    y = 771,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "MOBOBSIDIANCONCLAVEKNIGHT",
    x = 579,
    y = 768,
    z = 0,
    stationary = false,
    aggressive = true)
public final class ObsidianConclaveKnight extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public ObsidianConclaveKnight(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBOBSIDIANCONCLAVEKNIGHT",
        "${monster.mobobsidianconclaveknight}",
        1801,
        0,
        35,
        31845,
        82,
        187,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        122,
        374,
        java.util.List.of(),
        false,
        0.0f,
        83,
        76,
        76,
        96,
        76,
        76,
        28,
        new int[] {75, 75, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        68,
        282,
        0,
        1107820544,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d106+81", 826, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
