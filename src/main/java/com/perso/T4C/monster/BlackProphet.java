package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;

public final class BlackProphet extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public BlackProphet(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onAttack(Player p) {

    return Math.random() < 1.0 / 40
        ? message("npc.blackprophet.other." + (int) (Math.random() * 2))
        : NpcScriptRuntime.Effects.empty();
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player p) {

    return Math.random() < .1
        ? message("npc.blackprophet.death")
        : NpcScriptRuntime.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBBLACKPROPHET",
        "${monster.mobblackprophet}",
        3259,
        0,
        1,
        100,
        39,
        275,
        30000L,
        "",
        "",
        "",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(
            new MonsterDef.LootDrop("finely_cut_emerald", 0.005f),
            new MonsterDef.LootDrop("finely_cut_limestone", 0.005f),
            new MonsterDef.LootDrop("finely_cut_malachite", 0.005f),
            new MonsterDef.LootDrop("rough_emerald", 0.01f),
            new MonsterDef.LootDrop("rough_limestone", 0.01f),
            new MonsterDef.LootDrop("rough_malachite", 0.01f),
            new MonsterDef.LootDrop("screaming_helm_of_ogrimar", 0.01f),
            new MonsterDef.LootDrop("scroll_of_recall", 0.02f)),
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
        100,
        0,
        0,
        0,
        10003,
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
