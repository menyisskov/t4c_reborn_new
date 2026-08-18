package com.perso.T4C.npc.registry;

import com.perso.T4C.monster.core.*;

import com.perso.T4C.npc.core.*;

@FunctionalInterface
public interface NpcDamageCallback {

  void applyDamage(BaseNPC attacker, int damage);
}
