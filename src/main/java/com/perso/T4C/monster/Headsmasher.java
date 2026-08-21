package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Headsmasher", x = 338, y = 623, z = 1, stationary = false, aggressive = true)
public final class Headsmasher extends DataMonster {
  public static final String SOUND_ATTACK = "Minotaur Attack.wav";
  public static final String SOUND_DEATH = "Minotaur Dying.wav";
  public static final String SOUND_HIT = "Minotaur Hit.wav";

  public static final String CANONICAL_NAME = "Headsmasher";

  public Headsmasher(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Headsmasher",
        "${monster.headsmasher}",
        1694,
        0,
        4,
        4181,
        42,
        94,
        30000L,
        "Minotaur#f",
        "MinotaurA#i",
        "MinotaurC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        142,
        442,
        java.util.List.of(
            new MonsterDef.LootDrop("Mana elixir", 0.05f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Potion of fury", 0.01f),
            new MonsterDef.LootDrop("Fine steel short sword", 0.001f),
            new MonsterDef.LootDrop("Sapphire bracelet", 0.01f)),
        false,
        0.0f,
        55,
        50,
        50,
        63,
        0,
        50,
        0,
        new int[] {100, 50, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        40,
        170,
        0,
        1077149696,
        20014,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d53+41", 490, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
