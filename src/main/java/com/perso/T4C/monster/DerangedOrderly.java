package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;

public final class DerangedOrderly extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public DerangedOrderly(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onAttack(Player p) {

    return Math.random() < .02
        ? message("npc.derangedorderly.shout." + (int) (Math.random() * 2))
        : NpcScriptRuntime.Effects.empty();
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
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
        10011,
        285,
        260,
        0,
        0,
        284,
        118,
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
