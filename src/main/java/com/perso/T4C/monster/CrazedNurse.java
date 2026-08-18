package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class CrazedNurse extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

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
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
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
