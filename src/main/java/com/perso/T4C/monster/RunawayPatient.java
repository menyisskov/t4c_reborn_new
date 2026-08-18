package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBRUNAWAYPATIENT", x = 2445, y = 260, z = 0, stationary = false, aggressive = true)
public final class RunawayPatient extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public RunawayPatient(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return Math.random() < 1.0 / 30
        ? message("npc.runawaypatient.shout." + (int) (Math.random() * 3))
        : MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBRUNAWAYPATIENT",
        "${monster.mobrunawaypatient}",
        396,
        0,
        16,
        3207,
        19,
        43,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        39,
        121,
        java.util.List.of(),
        false,
        0.0f,
        37,
        34,
        34,
        41,
        34,
        34,
        19,
        new int[] {83, 83, 83, 83, 55, 5000, 100, 100, 100, 100, 100, 100},
        22,
        98,
        0,
        1093664768,
        10011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d25+18", 274, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
