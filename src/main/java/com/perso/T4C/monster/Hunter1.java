package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBHUNTER1", x = 437, y = 755, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBHUNTER1", x = 495, y = 553, z = 0, stationary = false, aggressive = false)
@Spawn(type = "MOBHUNTER1", x = 607, y = 925, z = 0, stationary = false, aggressive = false)
public final class Hunter1 extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public Hunter1(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBHUNTER1",
        "${monster.mobhunter1}",
        2086,
        0,
        37,
        39279,
        93,
        211,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        20,
        22,
        24,
        20,
        25,
        21,
        22,
        new int[] {58, 58, 58, 58, 39, 5000, 100, 100, 100, 100, 100, 100},
        75,
        310,
        0,
        1108606976,
        0,
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
        java.util.List.of(new MonsterDef.Attack("1d119+92", 910, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
