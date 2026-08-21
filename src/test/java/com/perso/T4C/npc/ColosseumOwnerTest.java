package com.perso.T4C.npc;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class ColosseumOwnerTest {

  @Test
  void fightYesThenYesTeleportsToColosseum() throws Exception {
    ScriptedNpc owner =
        (ScriptedNpc) NpcFactoryRegistry.create("ColosseumOwner", new NpcContext(null));
    NpcBehavior behavior = owner.publicBehavior();
    Player player = new Player();
    NpcBehaviorContext context = new NpcBehaviorContext(owner, player);

    assertTrue(behavior.onKeyword(context, "FIGHT"));
    assertEquals("fight", context.pendingYesNo());

    assertTrue(behavior.onYesNo(context, "fight", true));
    assertEquals("arena", context.pendingYesNo());

    assertTrue(behavior.onYesNo(context, "arena", true));
    assertEquals(1, player.getQuestFlag("__FLAG_USER_HAS_ENTERED_COLOSSEUM"));
    assertEquals(1725 * GRID_W, player.getCoordinates().getX());
    assertEquals(1835 * GRID_H, player.getCoordinates().getY());
  }
}
