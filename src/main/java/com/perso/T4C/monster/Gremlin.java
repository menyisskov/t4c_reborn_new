package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class Gremlin extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Gremlin";

  public Gremlin(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Gremlin",
        "${monster.gremlin}",
        1391,
        0,
        6,
        4385,
        66,
        150,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC!o",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        102,
        313,
        java.util.List.of(
            new MonsterDef.LootDrop("Mana elixir", 0.01f),
            new MonsterDef.LootDrop("Rough carnelian", 0.02f),
            new MonsterDef.LootDrop("Rough ruby", 0.004f),
            new MonsterDef.LootDrop("Rough garnet", 0.001f),
            new MonsterDef.LootDrop("Hickory compound bow", 0.005f)),
        false,
        0.0f,
        72,
        66,
        66,
        83,
        0,
        66,
        0,
        new int[] {0, 53, 80, 80, 80, 5000, 100, 100, 100, 100, 100, 100},
        57,
        238,
        0,
        1077673984,
        20001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        41,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d85+65", 694, 70, 0, 0, 0),
            new MonsterDef.Attack("", 0, 5, 10347, 1, 12),
            new MonsterDef.Attack("", 0, 5, 10365, 1, 12),
            new MonsterDef.Attack("", 0, 30, 10119, 0, 12),
            new MonsterDef.Attack("", 0, 55, 10364, 1, 12),
            new MonsterDef.Attack("", 0, 5, 10357, 1, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
