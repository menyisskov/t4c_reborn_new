package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterLifecycle;
import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Carman extends DataMonster implements MonsterLifecycle {
  public Carman(MonsterDef definition, float worldX, float worldY) throws GameException {
    super(definition, worldX, worldY);
  }

  @Override
  public MonsterScriptBridge.Effects onAttack(Player player) {
    if (ThreadLocalRandom.current().nextInt(11) != 0) return MonsterScriptBridge.Effects.empty();
    return new MonsterScriptBridge.Effects(List.of(), List.of("spell.mob_fast_regen"), List.of());
  }

  @Override
  public MonsterScriptBridge.Effects onDeath(Player player) {
    if (player != null) {
      InventoryService.add(player, "hel_soulstone");
      com.perso.T4C.npc.script.NpcScriptEngine.setGlobalFlag(
          "GLOBAL_FLAG_ADDON_CARMAN_PRESENT", 0);
    }
    return new MonsterScriptBridge.Effects(
        List.of("${npc.carman.soulstone}"), List.of(), List.of("spell.carman_flag_spell"));
  }
}
