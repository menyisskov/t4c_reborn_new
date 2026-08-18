package com.perso.T4C.monster.core;

import com.perso.T4C.npc.script.MonsterScriptBridge;
import com.perso.T4C.player.Player;

public interface MonsterLifecycle {

  default MonsterScriptBridge.Effects onSpawn(Player player) {

    return MonsterScriptBridge.Effects.empty();
  }

  default MonsterScriptBridge.Effects onAttack(Player player) {

    return MonsterScriptBridge.Effects.empty();
  }

  default MonsterScriptBridge.Effects onAttacked(Player player) {

    return MonsterScriptBridge.Effects.empty();
  }

  default MonsterScriptBridge.Effects onHit(Player player) {

    return MonsterScriptBridge.Effects.empty();
  }

  default MonsterScriptBridge.Effects onAttackHit(Player player) {

    return MonsterScriptBridge.Effects.empty();
  }

  default MonsterScriptBridge.Effects onDeath(Player player) {

    return MonsterScriptBridge.Effects.empty();
  }
}
