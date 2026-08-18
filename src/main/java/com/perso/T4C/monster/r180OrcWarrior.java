package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Orc Warrior", x = 2466, y = 517, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2481, y = 483, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2493, y = 501, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2494, y = 533, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2511, y = 475, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2550, y = 467, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2562, y = 546, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2565, y = 532, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2567, y = 456, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2575, y = 444, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2581, y = 496, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2586, y = 450, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2592, y = 555, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2594, y = 580, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2602, y = 534, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2617, y = 460, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2620, y = 562, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2620, y = 570, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2622, y = 585, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2629, y = 522, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2630, y = 483, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2630, y = 551, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2636, y = 578, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2636, y = 584, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2637, y = 519, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2637, y = 540, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2639, y = 512, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2643, y = 559, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2649, y = 567, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2651, y = 541, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2651, y = 583, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2653, y = 551, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2654, y = 563, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2656, y = 581, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2657, y = 602, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2659, y = 536, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2665, y = 580, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2669, y = 592, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2672, y = 551, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2674, y = 558, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2678, y = 587, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2691, y = 581, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 2722, y = 581, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 793, y = 995, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 811, y = 1010, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 847, y = 966, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 851, y = 990, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 857, y = 966, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Warrior", x = 911, y = 1023, z = 2, stationary = false, aggressive = true)
public final class r180OrcWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r180OrcWarrior(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Orc Warrior",
        "${monster.orc_warrior}",
        199,
        0,
        2,
        235,
        10,
        23,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        21,
        66,
        java.util.List.of(
            new MonsterDef.LootDrop("Feather", 0.01f),
            new MonsterDef.LootDrop("Leather armor", 0.003f),
            new MonsterDef.LootDrop("Studded leather helmet", 0.009f),
            new MonsterDef.LootDrop("Dagger of Piercing", 8.0E-4f),
            new MonsterDef.LootDrop("Iron key", 0.01f),
            new MonsterDef.LootDrop("Healing potion", 0.01f)),
        false,
        0.0f,
        27,
        23,
        26,
        29,
        0,
        25,
        0,
        new int[] {117, 58, 88, 88, 88, 5000, 100, 100, 100, 100, 100, 100},
        12,
        58,
        0,
        1075314688,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        15,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d14+9", 154, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
