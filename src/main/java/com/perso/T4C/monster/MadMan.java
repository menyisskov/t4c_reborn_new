package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBMADMAN", x = 2705, y = 2567, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2792, y = 2222, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2792, y = 2483, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2794, y = 2573, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2796, y = 2227, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2796, y = 2402, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2797, y = 2315, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2798, y = 2490, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2809, y = 2292, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2809, y = 2552, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2812, y = 2506, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2813, y = 2485, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2815, y = 2418, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2816, y = 2337, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2816, y = 2385, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2816, y = 2472, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2817, y = 2315, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2819, y = 2593, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2826, y = 2480, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2828, y = 2568, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2831, y = 2492, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2832, y = 2582, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADMAN", x = 2844, y = 2580, z = 0, stationary = false, aggressive = true)
public final class MadMan extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public MadMan(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return Math.random() < .02
        ? message("npc.madman.shout." + (int) (Math.random() * 3))
        : MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Madman",
        "${monster.madman}",
        0,
        0,
        0,
        0,
        0,
        0,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(
            new MonsterDef.LootDrop("C2 Permit", 0.15f),
            new MonsterDef.LootDrop("F1 Permit", 0.3f)),
        false,
        0.0f,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        true,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
