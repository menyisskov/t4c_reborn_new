package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Griroesh", x = 1096, y = 157, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Griroesh", x = 1131, y = 159, z = 1, stationary = false, aggressive = true)
public final class Griroesh extends DataMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public static final String CANONICAL_NAME = "Griroesh";

  public Griroesh(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Griroesh",
        "${monster.griroesh}",
        710,
        0,
        4,
        1575,
        34,
        78,
        30000L,
        "Demon#i",
        "DemonA#i",
        "DemonC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        62,
        192,
        java.util.List.of(
            new MonsterDef.LootDrop("Ring of confidence", 0.01f),
            new MonsterDef.LootDrop("Ring of darkness", 0.01f),
            new MonsterDef.LootDrop("Iron ring", 0.01f),
            new MonsterDef.LootDrop("Demon skull", 0.01f),
            new MonsterDef.LootDrop("Bracelet of power", 0.001f),
            new MonsterDef.LootDrop("Dark key", 0.02f),
            new MonsterDef.LootDrop("Healing potion", 0.05f)),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        0,
        46,
        0,
        new int[] {103, 103, 103, 103, 103, 5000, 100, 100, 100, 100, 100, 100},
        35,
        150,
        0,
        1076953088,
        20013,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d45+33", 430, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10095, 3, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
