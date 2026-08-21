package com.perso.T4C.monster.core;

import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.player.Player;

public interface MonsterLifecycle {

  default NpcScriptRuntime.Effects onSpawn(Player player) {

    return NpcScriptRuntime.Effects.empty();
  }

  default NpcScriptRuntime.Effects onAttack(Player player) {

    return NpcScriptRuntime.Effects.empty();
  }

  default NpcScriptRuntime.Effects onAttacked(Player player) {

    return NpcScriptRuntime.Effects.empty();
  }

  default NpcScriptRuntime.Effects onHit(Player player) {

    return NpcScriptRuntime.Effects.empty();
  }

  default NpcScriptRuntime.Effects onAttackHit(Player player) {

    return NpcScriptRuntime.Effects.empty();
  }

  default NpcScriptRuntime.Effects onDeath(Player player) {

    return NpcScriptRuntime.Effects.empty();
  }
}
