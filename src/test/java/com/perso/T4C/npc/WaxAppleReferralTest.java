package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class WaxAppleReferralTest {
  @Test
  void elysanasWaxAppleReferralOpensSamuelsCommissionButPoisonAppleDoesNot() throws Exception {
    ElysanaBlackrose elysana = new ElysanaBlackrose(new NpcContext(null));
    Player referred = new Player();
    NpcBehaviorContext referral = new NpcBehaviorContext(elysana, referred);

    assertEquals(0, referred.getQuestFlag("__QUEST_WAX_APPLE"));
    elysana.javaBehavior().onKeyword(referral, "wax");
    assertEquals(1, referred.getQuestFlag("__QUEST_WAX_APPLE"));

    Player poisonCustomer = new Player();
    NpcBehaviorContext poison = new NpcBehaviorContext(elysana, poisonCustomer);
    assertTrue(elysana.javaBehavior().onKeyword(poison, "poison apple"));
    assertEquals(0, poisonCustomer.getQuestFlag("__QUEST_WAX_APPLE"));
  }
}
