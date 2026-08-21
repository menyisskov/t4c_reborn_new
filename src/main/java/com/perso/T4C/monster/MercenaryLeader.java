package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;

public final class MercenaryLeader extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public MercenaryLeader(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onAttacked(Player p) {

    return Math.random() < 1.0 / 30
        ? message(Math.random() < .5 ? "npc.mercenary.shout.match" : "npc.mercenary.shout.blood")
        : NpcScriptRuntime.Effects.empty();
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player p) {

    return messageAndSpell(
        "npc.mercenary.leader.death", "spell.mercenary_leader_defeat_flag_spell");
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBMERCENARYLEADER",
        "${monster.mobmercenaryleader}",
        100,
        0,
        1,
        100,
        1,
        2,
        30000L,
        "",
        "",
        "",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        10,
        10,
        10,
        10,
        10,
        10,
        10,
        new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        1,
        0,
        0,
        0,
        10011,
        269,
        265,
        259,
        469,
        268,
        468,
        470,
        287,
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
