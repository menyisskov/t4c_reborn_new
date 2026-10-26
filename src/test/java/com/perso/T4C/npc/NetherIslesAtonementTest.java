package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.Neoflare;
import com.perso.T4C.monster.Purifier;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class NetherIslesAtonementTest {
  @Test
  void killingAPurifierBlocksLuteQuestUntilANeoflareKill() throws Exception {
    Player player = new Player();
    player.setQuestFlag("__QUEST_DIONYSUS_RECHARGE_HARP", 1);
    Purifier purifier = new Purifier(Purifier.definition(), 0f, 0f);
    Neoflare neoflare = new Neoflare(Neoflare.definition(), 0f, 0f);
    Glaenshenmilandira npc = new Glaenshenmilandira(new NpcContext(null));
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    purifier.onDeath(player);
    assertTrue(behavior.onKeyword(context, "Lute of Peace"));
    assertEquals(1, player.getQuestFlag("__QUEST_DIONYSUS_RECHARGE_HARP"));

    neoflare.onDeath(player);
    assertTrue(behavior.onKeyword(context, "Lute of Peace"));
    assertEquals(2, player.getQuestFlag("__QUEST_DIONYSUS_RECHARGE_HARP"));
  }
}
