package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterLifecycle;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import java.util.concurrent.ThreadLocalRandom;

public final class Carman extends DataMonster implements MonsterLifecycle {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public Carman(MonsterDef definition, float worldX, float worldY) throws GameException {
    super(definition, worldX, worldY);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player player) {
    if (ThreadLocalRandom.current().nextInt(11) != 0) return MonsterScriptBridge.Effects.empty();
    return new MonsterScriptBridge.Effects(
        java.util.List.of(), java.util.List.of("spell.mob_fast_regen"), java.util.List.of());
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player player) {
    if (player != null) {
      InventoryService.add(player, "hel_soulstone");
      com.perso.T4C.npc.script.NpcScriptEngine.setGlobalFlag("GLOBAL_FLAG_ADDON_CARMAN_PRESENT", 0);
    }
    return new MonsterScriptBridge.Effects(
        java.util.List.of("${npc.carman.soulstone}"),
        java.util.List.of(),
        java.util.List.of("spell.carman_flag_spell"));
  }

  public static MonsterDef definition() {
    return new MonsterDef(
         "CARMAN",
        "${monster.carman}",
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
