package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Dune Raider", x = 146, y = 2532, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 165, y = 2570, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 177, y = 2448, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 199, y = 2410, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 199, y = 2411, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 199, y = 2428, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 233, y = 2406, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 249, y = 2605, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 274, y = 2516, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 275, y = 2462, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 282, y = 2517, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 354, y = 2667, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 363, y = 2537, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 402, y = 2542, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 434, y = 2461, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 480, y = 2427, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 511, y = 2413, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dune Raider", x = 529, y = 2426, z = 0, stationary = false, aggressive = true)
public final class DuneRaider extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Dune Raider";

  public DuneRaider(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dune Raider",
        "${monster.dune_raider}",
        440,
        0,
        3,
        754,
        21,
        48,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        43,
        132,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Flask of crystal water", 0.01f),
            new MonsterDef.LootDrop("Ring of the bear", 0.01f),
            new MonsterDef.LootDrop("Chaos Sword", 0.01f)),
        false,
        0.0f,
        39,
        36,
        36,
        43,
        0,
        36,
        0,
        new int[] {82, 82, 82, 82, 55, 5000, 100, 100, 100, 100, 100, 100},
        24,
        106,
        0,
        1076363264,
        20006,
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
        java.util.List.of(new MonsterDef.Attack("1d28+20", 298, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
