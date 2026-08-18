package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "GoblinBoss", x = 2739, y = 966, z = 0, stationary = false, aggressive = true)
public final class GoblinChieftain extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Goblin Chieftain";

  public GoblinChieftain(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Goblin Chieftain",
        "${monster.goblin_chieftain}",
        313,
        0,
        2,
        453,
        15,
        35,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC#o",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        32,
        99,
        java.util.List.of(
            new MonsterDef.LootDrop("Feather", 0.01f),
            new MonsterDef.LootDrop("Flask of Goblin Blood", 0.005f),
            new MonsterDef.LootDrop("Iron key", 0.009f),
            new MonsterDef.LootDrop("Ringmail armor", 5.0E-4f),
            new MonsterDef.LootDrop("Ringmail leggings", 5.0E-4f),
            new MonsterDef.LootDrop("Ringmail helmet", 5.0E-4f)),
        false,
        0.0f,
        33,
        31,
        31,
        36,
        0,
        31,
        0,
        new int[] {113, 56, 85, 85, 85, 5025, 100, 100, 100, 100, 100, 100},
        18,
        82,
        0,
        1075970048,
        20001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        70,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d21+14", 226, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of("GoblinBoss"),
        java.util.Map.of());
  }
}
