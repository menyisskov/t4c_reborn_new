package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterLifecycle;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;
import java.util.concurrent.ThreadLocalRandom;

public final class Carman extends DataMonster implements MonsterLifecycle {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Female Dying 1.wav";
  public static final String SOUND_HIT = "Female Hit 1.wav";

  public Carman(MonsterDef definition, float worldX, float worldY) throws GameException {
    super(definition, worldX, worldY);
  }

  @Override
  public NpcScriptRuntime.Effects onAttack(Player player) {
    if (ThreadLocalRandom.current().nextInt(11) != 0) return NpcScriptRuntime.Effects.empty();
    return new NpcScriptRuntime.Effects(
        java.util.List.of(), java.util.List.of("spell.mob_fast_regen"), java.util.List.of());
  }

  @Override
  public NpcScriptRuntime.Effects onDeath(Player player) {
    if (player != null) {
      InventoryService.add(player, "hel_soulstone");
      player.setQuestFlag("ADDON_USER_HAS_KILLED_CARMAN", 1);
      com.perso.T4C.npc.core.NpcWorldFlags.set("ADDON_CARMAN_PRESENT", 0);
      com.perso.T4C.npc.core.NpcWorldFlags.set("GLOBAL_FLAG_ADDON_CARMAN_PRESENT", 0);
    }
    return new NpcScriptRuntime.Effects(
        java.util.List.of("${npc.carman.soulstone}"),
        java.util.List.of(),
        java.util.List.of("spell.carman_flag_spell"));
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "CARMAN",
        "${monster.carman}",
        6500,
        0,
        0,
        369905,
        136,
        298,
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
        115,
        104,
        104,
        135,
        104,
        104,
        35,
        new int[] {100, 100, 100, 100, 100, 5000, 100, 100, 100, 100, 100, 100},
        100,
        410,
        0,
        1112014848,
        10012,
        278,
        288,
        0,
        0,
        0,
        277,
        273,
        472,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d163+135", 1210, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
