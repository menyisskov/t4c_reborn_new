package com.perso.T4C.npc;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcWorldFlags;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ColosseumClerkTest {

  @AfterEach
  void resetArenaOccupancy() {
    NpcWorldFlags.set("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA", 0);
  }

  @Test
  void originalTwoStepLeavingDialogueTeleportsOutside() throws Exception {
    ColosseumClerk clerk = new ColosseumClerk(new NpcContext(null));
    NpcBehavior behavior = clerk.publicBehavior();
    Player player = new Player();
    NpcBehaviorContext context = new NpcBehaviorContext(clerk, player);

    assertTrue(behavior.onKeyword(context, "leave"));
    assertTrue(behavior.onKeyword(context, "leaving"));
    assertEquals(343 * GRID_W, player.getCoordinates().getX());
    assertEquals(492 * GRID_H, player.getCoordinates().getY());
  }

  @Test
  void fightWithLevelSetsSnappedDifficultyAndAsksToFight() throws Exception {
    ColosseumClerk clerk = new ColosseumClerk(new NpcContext(null));
    NpcBehavior behavior = clerk.publicBehavior();
    Player player = new Player();
    NpcBehaviorContext context = new NpcBehaviorContext(clerk, player);

    assertTrue(behavior.onKeyword(context, "fight 500"));
    assertEquals(500, context.flag("__FLAG_USER_LEVEL_SLICE"));
    assertEquals(500, context.flag("__FLAG_ARENA_LEVEL"));
    assertEquals(1, context.flag("__FLAG_USER_HAS_CHANGED_DIFFICULTY_LEVEL"));
  }

  @Test
  void fightWithLevelAndOpponentsSetsBoth() throws Exception {
    ColosseumClerk clerk = new ColosseumClerk(new NpcContext(null));
    NpcBehavior behavior = clerk.publicBehavior();
    Player player = new Player();
    NpcBehaviorContext context = new NpcBehaviorContext(clerk, player);

    assertTrue(behavior.onKeyword(context, "fight 500 3"));
    assertEquals(500, context.flag("__FLAG_ARENA_LEVEL"));
    assertEquals(3, context.flag("__ARENA_OPPONENTS"));
  }

  @Test
  void clerkRefusesCommandsWhileAnArenaMonsterRemains() throws Exception {
    ColosseumClerk clerk = new ColosseumClerk(new NpcContext(null));
    NpcBehavior behavior = clerk.publicBehavior();
    Player player = new Player();
    player.setWorldPosition(100 * GRID_W, 100 * GRID_H, 0);
    NpcBehaviorContext context = new NpcBehaviorContext(clerk, player);
    NpcWorldFlags.set("__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA", 1);

    assertTrue(behavior.onKeyword(context, "leaving"));
    assertEquals(100 * GRID_W, player.getCoordinates().getX());
    assertEquals(100 * GRID_H, player.getCoordinates().getY());
  }
}
