package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.Delwobble;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

/** T4C-0066: the "Audience to Bane Blackblood" quest gate - asking Dwarthon Stoneface about
 * "Bane" starts it, killing Delwobble (see monster/Delwobble.java) finishes it, and Bane himself
 * (BaneBlackblood.java's onConversationStart) reads the "__QUEST_DWARTHON_STONEFACE" flag this
 * quest drives, separately from his own pre-existing "__QUEST_ROYAL_KEY4" negotiation counter. */
class DwarthonStonefaceBaneQuestTest {
  private static final String FLAG = "__QUEST_DWARTHON_STONEFACE";

  @Test
  void askingAboutBaneStartsTheQuest() throws Exception {
    DwarthonStoneface npc = new DwarthonStoneface(new NpcContext(null));
    Player player = new Player();
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "bane"));

    assertEquals(3, player.getQuestFlag(FLAG));
  }

  @Test
  void askingAgainWhileDelwobbleIsStillAliveDoesNotResetProgress() throws Exception {
    DwarthonStoneface npc = new DwarthonStoneface(new NpcContext(null));
    Player player = new Player();
    player.setQuestFlag(FLAG, 3);
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "BANE"));

    assertEquals(3, player.getQuestFlag(FLAG));
  }

  @Test
  void killingDelwobbleCompletesTheQuestOnlyForAPlayerWhoAcceptedIt() throws Exception {
    Delwobble monster = new Delwobble(delwobbleDefinition(), 0f, 0f);
    Player questing = new Player();
    questing.setQuestFlag(FLAG, 3);
    Player bystander = new Player();

    monster.onDeath(questing);
    monster.onDeath(bystander);

    assertEquals(5, questing.getQuestFlag(FLAG));
    assertEquals(0, bystander.getQuestFlag(FLAG));
  }

  @Test
  void doesNotTouchBaneSSeparateRoyalKeyCounter() throws Exception {
    // "__QUEST_DWARTHON_STONEFACE" (this quest's own flag) and "__QUEST_ROYAL_KEY4" (Bane's own,
    // pre-existing Royal Key #4 negotiation counter) are two independent flags, despite both
    // gating dialogue on the same NPC - completing this quest must not advance the other one.
    BaneBlackblood npc = new BaneBlackblood(new NpcContext(null));
    Player player = new Player();
    player.setQuestFlag(FLAG, 5);
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    behavior.onKeyword(context, "royal key");

    assertEquals(0, player.getQuestFlag("__QUEST_ROYAL_KEY4"));
  }

  @Test
  void refusesBaneSOwnQuestBusinessWithoutAudience() throws Exception {
    // Codex caught, on the first version of this fix, that the audience gate only changed Bane's
    // greeting - his own transactional keywords worked regardless. A player who never talked to
    // Dwarthon (flag unset, the common case) must be refused all three.
    BaneBlackblood npc = new BaneBlackblood(new NpcContext(null));
    Player player = new Player();
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);
    player.setQuestFlag("__QUEST_KRAANIAN_EYES", 2);
    player.setQuestFlag("__QUEST_ROYAL_KEY4", 5);

    assertTrue(behavior.onKeyword(context, "ingredient"));
    assertTrue(behavior.onKeyword(context, "kraanian eye"));
    assertTrue(behavior.onKeyword(context, "royal key"));

    assertEquals(0, com.perso.T4C.item.InventoryService.count(player, "blood_dagger"));
    assertEquals(0, com.perso.T4C.item.InventoryService.count(player, "kraanian_eyes"));
    assertEquals(0, com.perso.T4C.item.InventoryService.count(player, "royal_key_4"));
    // The royal key counter must not silently advance for a refused attempt either.
    assertEquals(5, player.getQuestFlag("__QUEST_ROYAL_KEY4"));
  }

  @Test
  void allowsBaneSOwnQuestBusinessOnceAudienceIsGranted() throws Exception {
    BaneBlackblood npc = new BaneBlackblood(new NpcContext(null));
    Player player = new Player();
    player.setQuestFlag(FLAG, 5);
    player.setQuestFlag("__QUEST_ROYAL_KEY4", 5);
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "royal key"));

    assertTrue(com.perso.T4C.item.InventoryService.count(player, "royal_key_4") > 0);
  }

  private static MonsterDef delwobbleDefinition() {
    return Delwobble.definition();
  }
}
