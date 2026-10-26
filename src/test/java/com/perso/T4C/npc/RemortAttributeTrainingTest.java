package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.PlayerStateMapper;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.behavior.RebirthBehavior;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class RemortAttributeTrainingTest {
  private static Player reborn() {
    Player player = new Player();
    player.setRebirthCount(50);
    player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", 50);
    player.setQuestFlag("__FLAG_REMORT_PROCESS", 1);
    player.setEndurance(RebirthBehavior.startingAttributeFor(50));
    player.setQuestFlag("__FLAG_REMORT_POINTS", 1000);
    return player;
  }

  @Test
  void bulkTrainingStopsAtTheNewCapWithoutSpendingExcessEnergy() throws Exception {
    Player player = reborn();
    var npc = new RemortNPC2(new NpcContext(null));
    npc.publicBehavior().onKeyword(new NpcBehaviorContext(npc, player), "endurance 1000");
    assertEquals(
        RebirthBehavior.startingAttributeFor(50) + RebirthBehavior.MAX_ATTRIBUTE_TRAINING_BONUS,
        player.getEndurance());
    int remaining = 900;
    assertEquals(remaining, player.getQuestFlag("__FLAG_REMORT_POINTS"));
    npc.publicBehavior().onYesNo(new NpcBehaviorContext(npc, player), "remort_endurance", true);
    assertEquals(remaining, player.getQuestFlag("__FLAG_REMORT_POINTS"));
  }

  @Test
  void anExistingSaveAtTheOldLimitCanPurchaseTheNextPoint() throws Exception {
    Player old = reborn();
    old.setEndurance(RebirthBehavior.startingAttributeFor(50) + 10);
    old.setQuestFlag("__FLAG_REMORT_POINTS", 1);
    Player player = new Player();
    PlayerStateMapper.applyToPlayer(PlayerStateMapper.fromPlayer(old), player);
    var npc = new RemortNPC2(new NpcContext(null));
    var context = new NpcBehaviorContext(npc, player);
    assertTrue(npc.publicBehavior().onKeyword(context, "endurance"));
    assertEquals("remort_endurance", context.pendingYesNo());
    npc.publicBehavior().onYesNo(context, context.pendingYesNo(), true);
    assertEquals(old.getEndurance() + 1, player.getEndurance());
    assertEquals(0, player.getQuestFlag("__FLAG_REMORT_POINTS"));
    npc.publicBehavior().onYesNo(context, "remort_endurance", true);
    assertEquals(old.getEndurance() + 1, player.getEndurance());
    assertEquals(0, player.getQuestFlag("__FLAG_REMORT_POINTS"));
  }
}
