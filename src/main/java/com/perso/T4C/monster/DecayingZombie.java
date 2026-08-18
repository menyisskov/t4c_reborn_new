package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class DecayingZombie extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String CANONICAL_NAME = "Decaying Zombie";

  public DecayingZombie(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Decaying Zombie",
        "${monster.decaying_zombie}",
        55,
        0,
        1,
        46,
        4,
        8,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        5,
        16,
        java.util.List.of(
            new MonsterDef.LootDrop("Torch", 0.03f),
            new MonsterDef.LootDrop("Light healing potion", 0.05f)),
        false,
        0.0f,
        18,
        17,
        17,
        18,
        0,
        17,
        0,
        new int[] {92, 92, 123, 61, 5025, 61, 100, 100, 100, 100, 100, 100},
        3,
        22,
        0,
        1072693248,
        20009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        14,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d5+3", 46, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
