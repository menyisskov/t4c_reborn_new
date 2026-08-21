package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.NpcScripts;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class NpcScriptsTest {

  @Test
  void sabrinaHealsFromJavaBehavior() throws Exception {
    assertTrue(NpcScripts.hasMacro("__SPELL_MOB_COMPLETE_HEAL_TARGET_SPELL"));
    assertEquals(10270, NpcScripts.macro("__SPELL_MOB_COMPLETE_HEAL_TARGET_SPELL"));

    ScriptedNpc sabrina = new Sabrina(new NpcContext(null));
    NpcBehavior behavior = sabrina.publicBehavior();
    Player player = new Player();
    player.setName("Hero");
    NpcBehaviorContext context = new NpcBehaviorContext(sabrina, player);

    player.setQuestFlag("__RATS_KILLED", 0);
    behavior.onConversationStart(context);

    player.setQuestFlag("__RATS_KILLED", 25);
    behavior.onConversationStart(context);

    assertTrue(behavior.onKeyword(context, "HEAL"));
  }

  @Test
  void vortimerGivesMadHouseKeyFromJavaBehavior() throws Exception {
    Player player = new Player();
    player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", 1);
    ScriptedNpc vortimer =
        (ScriptedNpc) NpcFactoryRegistry.create("WardenVortimer", new NpcContext(null));
    NpcBehavior behavior = vortimer.publicBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(vortimer, player);

    assertTrue(behavior.onKeyword(context, "KEY"));
    assertEquals("giveKey", context.pendingYesNo());
    assertTrue(behavior.onYesNo(context, "giveKey", true));
    assertEquals(1, InventoryService.count(player, "item.mad_house_key"));
    assertEquals(1, InventoryService.count(player, "mad_house_key"));

    ScriptedNpc door =
        (ScriptedNpc) NpcFactoryRegistry.create("MadDoorEntrance", new NpcContext(null));
    player.setWorldPosition(
        door.getTileX() * com.perso.T4C.config.GameConstants.GRID_W,
        door.getTileY() * com.perso.T4C.config.GameConstants.GRID_H,
        0);
    door.publicBehavior().onConversationStart(new NpcBehaviorContext(door, player));
    assertEquals(2704 * com.perso.T4C.config.GameConstants.GRID_W, player.getCoordinates().getX());
    assertEquals(2226 * com.perso.T4C.config.GameConstants.GRID_H, player.getCoordinates().getY());
  }

  @Test
  void glaenshenGivesWillOfArtherkFromJavaBehavior() throws Exception {
    Player player = new Player();
    player.setQuestFlag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 16);
    ScriptedNpc glaen =
        (ScriptedNpc) NpcFactoryRegistry.create("Glaenshenmilandira", new NpcContext(null));
    NpcBehavior behavior = glaen.publicBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(glaen, player);

    behavior.onConversationStart(context);
    assertEquals("glaen_will", context.pendingYesNo());
    assertTrue(behavior.onYesNo(context, "glaen_will", true));
    assertTrue(InventoryService.count(player, "item.will_of_artherk") >= 1);
    assertEquals(17, player.getQuestFlag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST"));
  }

  @Test
  void factoryCatalogueHasNoEmbeddedCppConversation() {
    NpcScripts.reload();
    NpcScripts.Entry portal = NpcScripts.find("PortalJ4");
    assertNotNull(portal);
    assertFalse(portal.hasConversation());
    assertFalse(portal.hasEvent("OnPopup"));
  }
}
