package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Centaur Warrior", x = 2350, y = 2400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Centaur Warrior", x = 2410, y = 2460, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Centaur Warrior", x = 2340, y = 2470, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Centaur Warrior", x = 2420, y = 2390, z = 0, stationary = false, aggressive = true)
public final class CentaurWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Taunting Hit.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public static final String CANONICAL_NAME = "Centaur Warrior";

  public CentaurWarrior(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Centaur Warrior",
        "${monster.centaur_warrior}",
        3800,
        0,
        8,
        14000,
        140,
        250,
        30000L,
        "64kCentaurWarrior#i",
        "64kCentaurWarriorA#i",
        "64kCentaurWarriorC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        190,
        600,
        java.util.List.of(
            new MonsterDef.LootDrop("centaur_warband_ring", 0.03f),
            new MonsterDef.LootDrop("bow_of_centaur_slaying", 0.008f)),
        false,
        0.0f,
        130,
        120,
        120,
        145,
        0,
        120,
        0,
        new int[] {100, 130, 100, 90, 100, 100, 100, 100, 100, 100, 100, 100},
        110,
        450,
        0,
        90,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        70,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d190+147", 1334, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("CentaurWarrior"),
        java.util.Map.of());
  }
}
