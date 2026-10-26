package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;

public final class Mordred extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public Mordred(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player player) {
    if (player != null && player.getQuestFlag("ADDON_SERAPH_ARMOR_QUEST") == 1
        && player.getQuestFlag("ADDON_OBSIDIAN_MORDRED_SPAWNED") == 1)
      player.setQuestFlag("ADDON_SERAPH_ARMOR_QUEST", 2);
    return NpcScriptRuntime.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBMORDRED",
        "${monster.mobmordred}",
        2301,
        0,
        1,
        100,
        126,
        261,
        30000L,
        "",
        "",
        "",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(new MonsterDef.LootDrop("mordred_key", 1.0f),
            new MonsterDef.LootDrop("raw_crystal", 0.3f)),
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
        80,
        0,
        0,
        0,
        10011,
        278,
        288,
        0,
        0,
        261,
        275,
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
