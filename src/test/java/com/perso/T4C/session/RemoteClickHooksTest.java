package com.perso.T4C.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.LighthavenSamaritan;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.npc.core.NPCManager;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.objects.GroundItem;
import com.perso.T4C.objects.GroundItemManager;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The two click hooks an online session installs (T4C-0292): a click on loot or on an NPC becomes
 * an intent and changes nothing locally; without a hook both stay exactly as they were (pinned by
 * {@code GroundItemPickupCharacterizationTest} and {@code PuppetSwitchCharacterizationTest}).
 */
class RemoteClickHooksTest {
  @BeforeAll
  static void items() {
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister();
  }

  @Test
  void aClickOnLootIsHandedToTheHookAndChangesNothingLocally() {
    GroundItemManager ground = new GroundItemManager();
    GroundItem dagger = GroundItem.ofItem("item.dagger", 100, 100);
    ground.addRemoteItem(dagger);
    List<GroundItem> asked = new ArrayList<>();
    ground.setRemotePickup(
        item -> {
          asked.add(item);
          return true;
        });
    Player player = new Player();
    player.setStrength(500);

    // Far beyond the local reach: the server, not the client, judges distance.
    GroundItemManager.PickupResult result = ground.pickUpAt(100, 100, player, 99, 3);

    assertEquals(GroundItemManager.PickupStatus.REQUESTED, result.status);
    assertEquals(dagger.describe(), result.label);
    assertEquals(List.of(dagger), asked);
    assertEquals(0, InventoryService.count(player, "item.dagger"), "nothing is added locally");
    // Still on the ground: only the next snapshot removes it.
    assertTrue(ground.pickUpAt(100, 100, player, 99, 3).status != GroundItemManager.PickupStatus.NONE);
  }

  @Test
  void aHookThatDoesNotKnowTheItemLeavesTheClickUnhandledAndAClickElsewhereIsNone() {
    GroundItemManager ground = new GroundItemManager();
    ground.addRemoteItem(GroundItem.ofItem("item.dagger", 100, 100));
    ground.setRemotePickup(item -> false);
    Player player = new Player();
    assertEquals(
        GroundItemManager.PickupStatus.NONE, ground.pickUpAt(100, 100, player, 0, 3).status);
    ground.setRemotePickup(item -> true);
    assertEquals(
        GroundItemManager.PickupStatus.NONE, ground.pickUpAt(400, 400, player, 0, 3).status);
  }

  @Test
  void clearingTheHookRestoresTheLocalPickup() {
    GroundItemManager ground = new GroundItemManager();
    ground.addRemoteItem(GroundItem.ofItem("item.dagger", 100, 100));
    ground.setRemotePickup(item -> true);
    ground.setRemotePickup(null);
    Player player = new Player();
    player.setStrength(500);
    assertEquals(
        GroundItemManager.PickupStatus.TOO_FAR, ground.pickUpAt(100, 100, player, 9, 3).status);
    assertEquals(
        GroundItemManager.PickupStatus.PICKED_UP, ground.pickUpAt(100, 100, player, 1, 3).status);
  }

  private static BaseNPC samaritan(NPCManager manager) {
    BaseNPC npc = new LighthavenSamaritan(new NpcContext(null));
    manager.addNPC(npc);
    npc.puppetPlace(640f, 320f);
    return npc;
  }

  @Test
  void aPuppetNpcClickIsHandedToTheHookAndStartsNoConversation() {
    NPCManager manager = new NPCManager(null);
    manager.setPuppetMode(true);
    BaseNPC npc = samaritan(manager);
    List<BaseNPC> asked = new ArrayList<>();
    manager.setRemoteInteract(
        clicked -> {
          asked.add(clicked);
          return true;
        });
    Player player = new Player();
    player.getCoordinates().setX(640f + 32f);
    player.getCoordinates().setY(320f);

    assertTrue(manager.onClick(npc.getPosition().x, npc.getPosition().y, player));

    assertEquals(1, asked.size());
    assertSame(npc, asked.get(0));
    assertFalse(manager.talkToActiveNpc("hello", player), "no local conversation was opened");
  }

  @Test
  void aPuppetNpcClickWithoutAHookOrOffTheNpcIsNotHandled() {
    NPCManager manager = new NPCManager(null);
    manager.setPuppetMode(true);
    BaseNPC npc = samaritan(manager);
    Player player = new Player();
    assertFalse(manager.onClick(npc.getPosition().x, npc.getPosition().y, player));
    int[] calls = {0};
    manager.setRemoteInteract(
        clicked -> {
          calls[0]++;
          return true;
        });
    assertFalse(manager.onClick(5000f, 5000f, player));
    assertEquals(0, calls[0]);
  }

  @Test
  void whenTwoNpcClickBoxesOverlapTheClickGoesToTheFigureUnderTheCursor() {
    NPCManager manager = new NPCManager(null);
    manager.setPuppetMode(true);
    BaseNPC behind = samaritan(manager);
    BaseNPC front = new LighthavenSamaritan(new NpcContext(null));
    manager.addNPC(front);
    front.puppetPlace(640f, 368f);
    List<BaseNPC> asked = new ArrayList<>();
    manager.setRemoteInteract(
        clicked -> {
          asked.add(clicked);
          return true;
        });
    Player player = new Player();
    boolean sawOverlap = false;
    boolean behindWon = false;
    boolean frontWon = false;
    for (float y = 200f; y <= 440f; y += 1f) {
      boolean overBehind = behind.isMouseOver(640f, y);
      boolean overFront = front.isMouseOver(640f, y);
      if (!overBehind && !overFront) continue;
      asked.clear();
      assertTrue(manager.onClick(640f, y, player));
      BaseNPC expected;
      if (overBehind && overFront) {
        sawOverlap = true;
        float db = behind.mouseOverDistanceSquared(640f, y);
        float df = front.mouseOverDistanceSquared(640f, y);
        expected = db < df ? behind : df < db ? front : front;
      } else {
        expected = overBehind ? behind : front;
      }
      assertSame(expected, asked.get(0), "click at y=" + y);
      assertSame(expected, manager.findNpcAt(640f, y));
      if (expected == behind) behindWon = true; else frontWon = true;
    }
    assertTrue(sawOverlap, "the two boxes overlap on some rows");
    assertTrue(behindWon && frontWon, "each figure can be clicked, including the one behind");
  }
}
