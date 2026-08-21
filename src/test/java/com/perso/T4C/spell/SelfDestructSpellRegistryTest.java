package com.perso.T4C.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.perso.T4C.npc.core.NpcScripts;
import org.junit.jupiter.api.Test;

class SelfDestructSpellRegistryTest {

  @Test
  void originalSelfDestructMacrosResolveToRegisteredSpells() {
    assertEquals(10797, NpcScripts.macro("__SPELL_SELF_DESTRUCT_20_SECONDS"));
    assertEquals(10765, NpcScripts.macro("__SPELL_SELF_DESTRUCT_5_SECONDS"));
    assertNotNull(SpellRegistry.findByName("spell.self_destruct_20_seconds"));
    assertEquals(10797, SpellRegistry.findByName("spell.self_destruct_20_seconds").getSpellId());
    assertNotNull(SpellRegistry.findById(10797));
    assertNotNull(SpellRegistry.findById(10765));
  }

  @Test
  void remortPortalJ4HasNoEmbeddedCppPopupEvent() {
    NpcScripts.reload();
    NpcScripts.Entry portal = NpcScripts.find("PortalJ4");
    assertNotNull(portal);
    assertFalse(portal.hasConversation());
    assertFalse(portal.hasEvent("OnPopup"));
  }
}
