package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** T4C-0033: Grandmaster Tholvenn forges a Godsforged item only once the player holds both a
 * Tempered Godcore and a Bound Godsigil at once - a check a single QuestDef can't express
 * natively (see GrandmasterTholvenn.javaBehavior()). */
class GrandmasterTholvennTest {
  private static final String CORE = "item.tempered_godcore";
  private static final String SIGIL = "item.bound_godsigil";

  @BeforeEach
  void loadJsonItems() {
    ItemJsonLoader.loadAndRegister("assets/items");
  }

  @AfterEach
  void reset() {
    ItemRegistry.resetAdditionalDefinitions();
  }

  @Test
  void forgesTheItemOnceBothComponentsArePresentAndConsumesBoth() throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    GrandmasterTholvenn npc = new GrandmasterTholvenn(new NpcContext(quests));
    Player player = new Player();
    player.setStrength(500);
    player.getInventory().add(CORE);
    player.getInventory().add(SIGIL);
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "warblade"));

    assertEquals(0, InventoryService.count(player, CORE));
    assertEquals(0, InventoryService.count(player, SIGIL));
    assertEquals(1, InventoryService.count(player, "godsforged_warblade"));
    assertEquals(
        QuestService.STATUS_COMPLETED,
        player.getQuestFlag(
            QuestService.statusFlag(QuestRegistry.findById("forge_godsforged_warblade"))));
  }

  @Test
  void refusesToForgeWithOnlyOneComponentAndConsumesNothing() throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    GrandmasterTholvenn npc = new GrandmasterTholvenn(new NpcContext(quests));
    Player player = new Player();
    player.getInventory().add(SIGIL);
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertTrue(behavior.onKeyword(context, "stormbow"));

    assertEquals(1, InventoryService.count(player, SIGIL));
    assertEquals(0, InventoryService.count(player, "godsforged_stormbow"));
    assertEquals(
        QuestService.STATUS_NOT_STARTED,
        player.getQuestFlag(
            QuestService.statusFlag(QuestRegistry.findById("forge_godsforged_stormbow"))));
  }

  @Test
  void aSecondForgeOfTheSameItemJustReportsItsAlreadyDone() throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    GrandmasterTholvenn npc = new GrandmasterTholvenn(new NpcContext(quests));
    Player player = new Player();
    player.setStrength(500);
    player.getInventory().add(CORE);
    player.getInventory().add(SIGIL);
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);
    assertTrue(behavior.onKeyword(context, "torc"));
    assertEquals(1, InventoryService.count(player, "godsforged_torc_of_the_first_pact"));

    player.getInventory().add(CORE);
    player.getInventory().add(SIGIL);
    assertTrue(behavior.onKeyword(context, "torc"));

    assertEquals(1, InventoryService.count(player, "godsforged_torc_of_the_first_pact"));
  }

  @Test
  void unrelatedKeywordsFallThroughToTheDeclarativeTopics() throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    GrandmasterTholvenn npc = new GrandmasterTholvenn(new NpcContext(quests));
    Player player = new Player();
    NpcBehavior behavior = npc.javaBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(npc, player);

    assertFalse(behavior.onKeyword(context, "tholvenn"));
  }
}
