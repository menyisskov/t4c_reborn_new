package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Blackguard extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Blackguard";

  public Blackguard(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Blackguard",
        "${monster.blackguard}",
        418,
        0,
        3,
        699,
        20,
        46,
        30000L,
        "BlackWarrior#m",
        "BlackWarriorA#l",
        "BlackWarriorC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        41,
        126,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.02f),
            new MonsterDef.LootDrop("Ring of the bear", 0.01f),
            new MonsterDef.LootDrop("Yellow gem", 0.005f),
            new MonsterDef.LootDrop("Golden chalice", 0.003f),
            new MonsterDef.LootDrop("Fine steel short sword", 0.005f)),
        false,
        0.0f,
        38,
        35,
        35,
        42,
        0,
        35,
        0,
        new int[] {83, 83, 83, 83, 55, 5000, 100, 100, 100, 100, 100, 100},
        23,
        117,
        0,
        1075838976,
        20043,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        23,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d27+19", 286, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
