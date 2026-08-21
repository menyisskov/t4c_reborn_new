package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.NpcScriptRuntime;
import com.perso.T4C.npc.core.NpcWorldFlags;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

class NpcScriptRuntimeTest {

  @Test
  void arenaDeathAwardsTokenAndDecrementsOccupancy() throws Exception {
    var def =
        MonsterRegistry.load().stream()
            .filter(d -> d.getName().equalsIgnoreCase("ArenaMobXP500"))
            .findFirst()
            .orElseThrow();
    Player player = new Player();
    player.setLevel(1);
    NpcWorldFlags.set("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA", 1);
    DataMonster monster = new DataMonster(def, 0, 0);
    NpcScriptRuntime.Effects death = NpcScriptRuntime.death(monster, player);
    assertEquals(List.of("You receive a battle token for your efforts."), death.messages());
    assertEquals(List.of("spell.mob_arena_level_spell"), death.selfSpells());
    assertEquals(0, NpcWorldFlags.get("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA"));
    assertEquals(500, player.getQuestFlag("__FLAG_ARENA_LEVEL"));
  }

  @Test
  void arenaMonsterBinsPersistTheirParticipationLevelEffect() throws Exception {
    var arenaMonsters =
        MonsterRegistry.load().stream()
            .filter(def -> def.getName().matches("ArenaMobXP\\d+"))
            .toList();
    assertFalse(arenaMonsters.isEmpty());
    for (var monster : arenaMonsters) {
      String level = monster.getName().replaceFirst("^ArenaMobXP", "");
      assertEquals(Integer.parseInt(level), NpcScriptRuntime.arenaSlice(monster.getName()));
    }
    Player player = new Player();
    player.setQuestFlag("__FLAG_ARENA_LEVEL", 60);
    var level70 =
        arenaMonsters.stream()
            .filter(monster -> "ArenaMobXP70".equals(monster.getName()))
            .findFirst()
            .orElseThrow();
    NpcScriptRuntime.arenaDeath(level70.getName(), player);
    assertEquals(70, player.getQuestFlag("__FLAG_ARENA_LEVEL"));
  }

  @Test
  void everyArenaMonsterHasAVisibleAnimationDefinition() throws Exception {
    var arenaMonsters =
        MonsterRegistry.load().stream()
            .filter(def -> def.getName().matches("ArenaMobXP\\d+"))
            .toList();
    assertFalse(arenaMonsters.isEmpty());
    assertTrue(
        arenaMonsters.stream()
            .allMatch(
                def ->
                    def.getWalkPattern() != null
                        && !def.getWalkPattern().isBlank()
                        && def.getAttackPattern() != null
                        && !def.getAttackPattern().isBlank()
                        && def.getDeathPattern() != null
                        && !def.getDeathPattern().isBlank()));
  }

  @Test
  void clerkAndOwnerUseJavaBehavior() throws Exception {
    ScriptedNpc clerk =
        (ScriptedNpc) NpcFactoryRegistry.create("ColosseumClerk", new NpcContext(null));
    assertNotNull(clerk.publicBehavior());
    assertTrue(clerk.usesJavaBehavior());
    assertEquals(null, clerk.getSpec().sourceScript());
    ScriptedNpc owner =
        (ScriptedNpc) NpcFactoryRegistry.create("ColosseumOwner", new NpcContext(null));
    assertTrue(owner.usesJavaBehavior());
    assertEquals(null, owner.getSpec().sourceScript());
  }

  @Test
  void wardenVortimerUsesJavaBehavior() throws Exception {
    ScriptedNpc vortimer =
        (ScriptedNpc) NpcFactoryRegistry.create("WardenVortimer", new NpcContext(null));
    assertTrue(vortimer.usesJavaBehavior());
    assertEquals(null, vortimer.getSpec().sourceScript());
  }
}
