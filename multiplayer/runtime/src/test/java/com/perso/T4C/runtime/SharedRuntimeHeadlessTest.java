package com.perso.T4C.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.behavior.NpcBehavior;
import com.perso.T4C.npc.behavior.NpcBehaviorContext;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class SharedRuntimeHeadlessTest {
  @Test
  void originalAuthoredNpcMutatesPlayerWithoutDesktopBackend() throws Exception {
    ClassLoader loader = getClass().getClassLoader();
    assertNull(loader.getResource("com/badlogic/gdx/backends/lwjgl3/Lwjgl3Application.class"));
    assertNull(loader.getResource("com/badlogic/gdx/graphics/g2d/freetype/FreeTypeFontGenerator.class"));

    Player player = new Player();
    player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", 1);
    ScriptedNpc vortimer =
        (ScriptedNpc) NpcFactoryRegistry.create("WardenVortimer", new NpcContext(null));
    assertNotNull(vortimer);
    NpcBehavior behavior = vortimer.publicBehavior();
    NpcBehaviorContext context = new NpcBehaviorContext(vortimer, player);

    assertTrue(behavior.onKeyword(context, "KEY"));
    assertEquals("giveKey", context.pendingYesNo());
    assertTrue(behavior.onYesNo(context, "giveKey", true));
    assertEquals(1, InventoryService.count(player, "item.mad_house_key"));
  }
}
