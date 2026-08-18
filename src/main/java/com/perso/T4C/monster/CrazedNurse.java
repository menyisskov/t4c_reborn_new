package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBCRAZEDNURSE", x = 2707, y = 2307, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2714, y = 2492, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2720, y = 2472, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2726, y = 2576, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2727, y = 2568, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2729, y = 2555, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2732, y = 2296, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2736, y = 2590, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2743, y = 2569, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2744, y = 2483, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2746, y = 2407, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2746, y = 2494, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2749, y = 2581, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2808, y = 2220, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2811, y = 2206, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2811, y = 2240, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2815, y = 2251, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2824, y = 2222, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2827, y = 2309, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2827, y = 2391, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2830, y = 2408, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2831, y = 2234, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCRAZEDNURSE", x = 2832, y = 2319, z = 0, stationary = false, aggressive = true)
public final class CrazedNurse extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public CrazedNurse(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onHit(Player p) {

    return Math.random() < .02
        ? message("npc.crazednurse.shout." + (int) (Math.random() * 4))
        : MonsterScriptBridge.Effects.empty();
  }

  @Override
  public MonsterScriptBridge.Effects onAttackHit(Player p) {

    return onHit(p);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Crazed Nurse",
        "${monster.crazed_nurse}",
        313,
        0,
        0,
        0,
        0,
        0,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(
            new MonsterDef.LootDrop("J1 Permit", 0.05f),
            new MonsterDef.LootDrop("B1 Permit", 0.2f)),
        false,
        0.0f,
        33,
        31,
        0,
        0,
        0,
        0,
        0,
        new int[] {0, 0, 0, 0, 0, 0, 100, 100, 100, 100, 100, 100},
        18,
        82,
        0,
        1075970048,
        10008,
        41139,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        47,
        0,
        true,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
