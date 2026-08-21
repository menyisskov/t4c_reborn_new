package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.InventoryService;
import com.perso.T4C.npc.core.NpcScriptEngine;
import com.perso.T4C.npc.core.NpcScripts;
import com.perso.T4C.player.Player;
import org.junit.jupiter.api.Test;

class NpcScriptsTest {

  @Test
  void loadsTokenizedSabrinaScriptFromOriginalCpp() {
    NpcScripts.Entry sabrina = NpcScripts.find("Sabrina");
    assertNotNull(sabrina);
    assertTrue(sabrina.hasConversation());
    assertEquals("Sabrina", sabrina.sourceClass());
    assertTrue(NpcScripts.hasMacro("__SPELL_MOB_COMPLETE_HEAL_TARGET_SPELL"));
    assertEquals(10270, NpcScripts.macro("__SPELL_MOB_COMPLETE_HEAL_TARGET_SPELL"));

    Player player = new Player();
    player.setName("Hero");
    player.setQuestFlag("__RATS_KILLED", 0);
    NpcScriptEngine.Result help =
        NpcScriptEngine.begin(sabrina.sourceScript(), "Sabrina", player);
    assertEquals(I18n.key("npc.cpp.intl.7376"), help.text());
    assertTrue(help.endConversation());

    player.setQuestFlag("__RATS_KILLED", 25);
    NpcScriptEngine.Result done =
        NpcScriptEngine.begin(sabrina.sourceScript(), "Sabrina", player);
    assertTrue(done.text().contains("Hero"));
    assertFalse(done.endConversation());

    NpcScriptEngine.Result heal =
        NpcScriptEngine.respond(sabrina.sourceScript(), "Sabrina", "HEAL", player);
    assertTrue(heal.heal());
    assertTrue(
        heal.targetSpells().stream()
            .anyMatch(id -> id.contains("COMPLETE_HEAL") || id.equals("10270")));
  }

  @Test
  void mapsOriginalSpellTeachMacrosToSpellKeys() {
    Player player = new Player();
    String script =
        """
                Command("LEARN")
                    AddTeachSkill(__SPELL_FIRE_DART, 5, 532)
                    SendTeachSkillList
                """;
    NpcScriptEngine.Result learn = NpcScriptEngine.respond(script, "Iraltok", "learn", player);
    assertFalse(learn.taughtSpells().isEmpty() && learn.taughtSkills().contains("__SPELL_FIRE_DART"));
    assertTrue(
        learn.taughtSpells().stream().anyMatch(id -> id.toLowerCase().contains("fire"))
            || learn.skillOffers().stream()
                .anyMatch(offer -> offer.skill().toLowerCase().contains("fire")));
  }

  @Test
  void vortimerGiveItemResolvesOriginalMadHouseKeyMacroIntoInventory() {
    Player player = new Player();
    player.setQuestFlag("__FLAG_NUMBER_OF_REMORTS", 1);
    NpcScripts.Entry vortimer = NpcScripts.find("WardenVortimer");
    assertNotNull(vortimer);
    NpcScriptEngine.Result ask =
        NpcScriptEngine.respond(vortimer.sourceScript(), "WardenVortimer", "KEY", player);
    assertEquals("GiveKey", ask.pendingYesNo());
    NpcScriptEngine.Result given =
        NpcScriptEngine.respondYesNo(
            vortimer.sourceScript(), "WardenVortimer", ask.pendingYesNo(), true, player);
    assertTrue(
        given.systemMessages().stream()
            .anyMatch(message -> message.contains("mad house key")));
    assertEquals(1, InventoryService.count(player, "item.mad_house_key"));
    assertEquals(1, InventoryService.count(player, "mad_house_key"));
    NpcScripts.Entry door = NpcScripts.find("MadDoorEntrance");
    assertNotNull(door);
    NpcScriptEngine.Result enter =
        NpcScriptEngine.begin(door.sourceScript(), "MadDoorEntrance", player);
    assertTrue(
        enter.systemMessages().stream()
            .anyMatch(message -> message.contains("open the door")));
  }

  @Test
  void glaenshenGivesWillOfArtherkItemAndFormatsXpReward() {
    Player player = new Player();
    player.setQuestFlag("__QUEST_FLAG_WILL_OF_ARTHERK_QUEST", 16);
    NpcScripts.Entry glaen = NpcScripts.find("Glaenshenmilandira");
    assertNotNull(glaen);
    NpcScriptEngine.Result ask =
        NpcScriptEngine.begin(glaen.sourceScript(), "Glaenshenmilandira", player);
    assertEquals("WILL", ask.pendingYesNo());
    NpcScriptEngine.Result given =
        NpcScriptEngine.respondYesNo(
            glaen.sourceScript(), "Glaenshenmilandira", ask.pendingYesNo(), true, player);
    assertTrue(InventoryService.count(player, "item.will_of_artherk") >= 1);
    assertEquals(250000, given.xp());
    assertTrue(
        given.systemMessages().stream()
            .anyMatch(message -> message.contains("250000") && message.contains("experience")));
    assertFalse(
        given.systemMessages().stream().anyMatch(message -> message.contains("%u")));
  }
}
