package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public final class DerangedOrderly extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public DerangedOrderly(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player p) {

    return Math.random() < .02
        ? message("npc.derangedorderly.shout." + (int) (Math.random() * 2))
        : MonsterScriptBridge.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Deranged Orderly",
        "${monster.deranged_orderly}",
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
            new MonsterDef.LootDrop("C5 Permit", 0.05f),
            new MonsterDef.LootDrop("J3 Permit", 0.5f)),
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
        10012,
        41139,
        0,
        0,
        0,
        0,
        40655,
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
