package com.perso.T4C.npc;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.ChaosDemon;
import com.perso.T4C.monster.DarknessDemon;
import com.perso.T4C.monster.r163OlinHaadGuard3;
import com.perso.T4C.monster.r164OlinHaadGuard4;
import com.perso.T4C.monster.r165OlinHaadGuard5;
import com.perso.T4C.monster.r241TerrorDemon;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

/**
 * T4C-0407: the Raven's Dust demon crypt. The three Chaos centre portals only open for a player who has
 * killed the Terror, Chaos and Darkness demons, and Olin Haad counts the guards a player has cleared.
 * Nothing set those per-player flags before, so Lothar (and the Caledbolg) behind the portals could not
 * be reached by playing the game.
 */
class AddonDemonAndGuardCountersTest {
  private static final String TERROR = "ADDON_TERROR_DEMON_KILLED";
  private static final String CHAOS = "ADDON_CHAOS_DEMON_KILLED";
  private static final String DARKNESS = "ADDON_DARKNESS_DEMON_KILLED";
  private static final String GUARDS = "ADDON_NUMBER_OF_OLIN_HAAD_GUARDS_KILLED";

  private static Player playerAtOrigin() {
    Player player = new Player();
    player.setMapBounds(10_000, 10_000);
    player.setWorldPosition(0f, 0f, 1);
    return player;
  }

  private static float tileX(Player player) {
    return player.getPositionVector().x / GRID_W;
  }

  private static float tileY(Player player) {
    return player.getPositionVector().y / GRID_H;
  }

  /** Talks to a portal standing next to the player and reports whether it moved them. */
  private static boolean entersCentre(NpcBehavior behavior, ScriptedNpc portal, Player player) {
    float x = player.getPositionVector().x;
    float y = player.getPositionVector().y;
    behavior.onConversationStart(new NpcBehaviorContext(portal, player));
    return player.getPositionVector().x != x || player.getPositionVector().y != y;
  }

  @Test
  void eachDemonDeathSetsOnlyItsOwnFlag() throws Exception {
    Player player = new Player();
    new r241TerrorDemon(r241TerrorDemon.definition(), 0f, 0f).onDeath(player);
    assertEquals(1, player.getQuestFlag(TERROR));
    assertEquals(0, player.getQuestFlag(CHAOS));
    assertEquals(0, player.getQuestFlag(DARKNESS));

    new ChaosDemon(ChaosDemon.definition(), 0f, 0f).onDeath(player);
    assertEquals(1, player.getQuestFlag(CHAOS));
    assertEquals(0, player.getQuestFlag(DARKNESS));

    new DarknessDemon(DarknessDemon.definition(), 0f, 0f).onDeath(player);
    assertEquals(1, player.getQuestFlag(DARKNESS));
  }

  @Test
  void aMonsterDyingWithNoKillerIsHarmless() throws Exception {
    new ChaosDemon(ChaosDemon.definition(), 0f, 0f).onDeath(null);
    new r165OlinHaadGuard5(r165OlinHaadGuard5.definition(), 0f, 0f).onDeath(null);
  }

  @Test
  void theCentrePortalsStayShutUntilAllThreeDemonsAreDeadThenTakeThePlayerToLothar() throws Exception {
    ChaosWestToCenterPortal west = new ChaosWestToCenterPortal(new NpcContext(null));
    ChaosEastToCenterPortal east = new ChaosEastToCenterPortal(new NpcContext(null));
    ChaosSouthToCenterPortal south = new ChaosSouthToCenterPortal(new NpcContext(null));
    Player player = playerAtOrigin();

    // Nobody killed anything: every portal refuses.
    assertTrue(
        !entersCentre(west.javaBehavior(), west, player)
            && !entersCentre(east.javaBehavior(), east, player)
            && !entersCentre(south.javaBehavior(), south, player));

    new r241TerrorDemon(r241TerrorDemon.definition(), 0f, 0f).onDeath(player);
    new ChaosDemon(ChaosDemon.definition(), 0f, 0f).onDeath(player);
    assertTrue(!entersCentre(west.javaBehavior(), west, player), "two of three demons is still not enough");

    new DarknessDemon(DarknessDemon.definition(), 0f, 0f).onDeath(player);
    assertTrue(entersCentre(west.javaBehavior(), west, player), "all three demons dead opens the portal");
    assertTrue(tileX(player) > 1500 && tileY(player) > 1600, "the player lands in the centre room, near Lothar");
  }

  @Test
  void anotherPlayersKillsDoNotOpenThePortalForYou() throws Exception {
    Player killer = new Player();
    new r241TerrorDemon(r241TerrorDemon.definition(), 0f, 0f).onDeath(killer);
    new ChaosDemon(ChaosDemon.definition(), 0f, 0f).onDeath(killer);
    new DarknessDemon(DarknessDemon.definition(), 0f, 0f).onDeath(killer);
    Player bystander = playerAtOrigin();

    ChaosSouthToCenterPortal south = new ChaosSouthToCenterPortal(new NpcContext(null));
    assertTrue(!entersCentre(south.javaBehavior(), south, bystander));
  }

  @Test
  void everyKindOfOlinHaadGuardCountsTowardTwentyAndTheCountIsCapped() throws Exception {
    Player player = new Player();
    for (int i = 0; i < 7; i++) {
      new r163OlinHaadGuard3(r163OlinHaadGuard3.definition(), 0f, 0f).onDeath(player);
      new r164OlinHaadGuard4(r164OlinHaadGuard4.definition(), 0f, 0f).onDeath(player);
      new r165OlinHaadGuard5(r165OlinHaadGuard5.definition(), 0f, 0f).onDeath(player);
    }
    assertEquals(21, player.getQuestFlag(GUARDS));
    assertTrue(player.getQuestFlag(GUARDS) > 19, "Olin Haad's 'more than 19' gate is satisfied");

    for (int i = 0; i < 200; i++) {
      new r163OlinHaadGuard3(r163OlinHaadGuard3.definition(), 0f, 0f).onDeath(player);
    }
    assertEquals(99, player.getQuestFlag(GUARDS));
  }
}
