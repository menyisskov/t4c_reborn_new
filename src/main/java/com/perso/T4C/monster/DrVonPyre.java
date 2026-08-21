package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "DRVONPYRE", x = 2831, y = 2397, z = 0, stationary = false, aggressive = true)
public final class DrVonPyre extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public DrVonPyre(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onAttack(Player p) {

    return Math.random() < 1.0 / 40
        ? message("npc.drvonpyre.shout")
        : NpcScriptRuntime.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "DRVONPYRE",
        "${monster.drvonpyre}",
        581,
        0,
        19,
        5771,
        28,
        63,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        53,
        165,
        java.util.List.of(),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        41,
        41,
        21,
        new int[] {79, 79, 106, 53, 5025, 53, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1097859072,
        10011,
        278,
        288,
        0,
        0,
        261,
        274,
        0,
        0,
        75,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d36+27", 370, 98, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
