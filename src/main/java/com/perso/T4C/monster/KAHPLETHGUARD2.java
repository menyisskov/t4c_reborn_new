package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "KAHPLETHGUARD2", x = 2771, y = 557, z = 2, stationary = false, aggressive = false)
@Spawn(type = "KAHPLETHGUARD2", x = 2789, y = 539, z = 2, stationary = false, aggressive = false)
@Spawn(type = "KAHPLETHGUARD2", x = 2790, y = 567, z = 2, stationary = false, aggressive = false)
@Spawn(type = "KAHPLETHGUARD2", x = 2799, y = 558, z = 2, stationary = false, aggressive = false)
@Spawn(type = "KAHPLETHGUARD2", x = 2801, y = 587, z = 2, stationary = false, aggressive = false)
@Spawn(type = "KAHPLETHGUARD2", x = 2819, y = 569, z = 2, stationary = false, aggressive = false)
public final class KAHPLETHGUARD2 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public KAHPLETHGUARD2(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "KAHPLETHGUARD2",
        "${monster.kahplethguard2}",
        32590,
        0,
        4,
        74158,
        135,
        306,
        30000L,
        "64kSkavenWarrior#i",
        "64kSkavenWarriorA#k",
        "64kSkavenWarriorC#s",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        179,
        550,
        java.util.List.of(),
        false,
        0.0f,
        115,
        104,
        104,
        135,
        104,
        104,
        35,
        new int[] {47, 47, 47, 47, 47, 5000, 100, 100, 100, 100, 100, 100},
        100,
        410,
        0,
        1112014848,
        20050,
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
        java.util.List.of(new MonsterDef.Attack("1d172+134", 1210, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
