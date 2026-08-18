package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class AnimatedCorpse extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String CANONICAL_NAME = "Animated Corpse";

  public AnimatedCorpse(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Animated Corpse",
        "${monster.animated_corpse}",
        235,
        0,
        2,
        301,
        12,
        27,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        25,
        77,
        java.util.List.of(
            new MonsterDef.LootDrop("Ring of confidence", 0.01f),
            new MonsterDef.LootDrop("Skeleton bone", 0.003f)),
        false,
        0.0f,
        29,
        27,
        27,
        31,
        0,
        27,
        0,
        new int[] {87, 87, 116, 58, 5025, 58, 100, 100, 100, 100, 100, 100},
        14,
        66,
        0,
        1075576832,
        20012,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d16+11", 178, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
