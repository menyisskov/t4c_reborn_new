package com.perso.T4C.session;

import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.npc.core.NPCManager;

/** The registries a world driver hands back to the screen. */
public record WorldEntities(MonsterManager monsters, NPCManager npcs) {}
