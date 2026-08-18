package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBMADPATIENT", x = 2711, y = 2217, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2712, y = 2401, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2713, y = 2315, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2721, y = 2469, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2723, y = 2378, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2723, y = 2392, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2724, y = 2206, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2725, y = 2290, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2725, y = 2415, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2726, y = 2223, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2726, y = 2308, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2728, y = 2330, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2728, y = 2504, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2728, y = 2590, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2731, y = 2507, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2734, y = 2339, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2735, y = 2248, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2735, y = 2596, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2744, y = 2310, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2748, y = 2322, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBMADPATIENT", x = 2751, y = 2231, z = 0, stationary = false, aggressive = true)
public final class MadPatient extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public MadPatient(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return Math.random() < .02
        ? message("npc.madpatient.shout." + (int) (Math.random() * 3))
        : MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Mad Patient",
        "${monster.mad_patient}",
        254,
        0,
        0,
        0,
        13,
        29,
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
            new MonsterDef.LootDrop("A3 Permit", 0.1f), new MonsterDef.LootDrop("E5 Permit", 0.2f)),
        false,
        0.0f,
        30,
        28,
        28,
        33,
        0,
        28,
        0,
        new int[] {86, 86, 86, 86, 57, 5000, 100, 100, 100, 100, 100, 100},
        15,
        70,
        0,
        1075576832,
        10011,
        40220,
        0,
        0,
        0,
        40213,
        0,
        0,
        0,
        100,
        47,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d17+12", 190, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
