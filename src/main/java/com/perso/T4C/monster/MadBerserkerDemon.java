package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;

public final class MadBerserkerDemon extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Demon Attack.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public MadBerserkerDemon(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  @Override
  public NpcScriptRuntime.Effects onSpawn(Player p) {

    return selfSpell("spell.npc_cantrip_pentacle");
  }

  @Override
  public NpcScriptRuntime.Effects onAttack(Player p) {

    int r = (int) (Math.random() * 21);

    return r < 2 ? message("npc.madberserker.shout." + r) : NpcScriptRuntime.Effects.empty();
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBMADBERSERKERDEMON",
        "${monster.mobmadberserkerdemon}",
        3797,
        0,
        1,
        100,
        100,
        335,
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
            new MonsterDef.LootDrop("manastone", 0.05f),
            new MonsterDef.LootDrop("mithril_battle_axe", 0.01f),
            new MonsterDef.LootDrop("mithril_shield", 0.01f),
            new MonsterDef.LootDrop("potion_of_fury", 0.05f),
            new MonsterDef.LootDrop("rough_carnelian", 0.01f),
            new MonsterDef.LootDrop("rough_garnet", 0.01f),
            new MonsterDef.LootDrop("rough_ruby", 0.01f)),
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
        110,
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
