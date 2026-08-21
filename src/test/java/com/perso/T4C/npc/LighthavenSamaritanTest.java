package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.npc.core.NpcContext;
import com.perso.T4C.npc.core.NpcFactoryRegistry;
import com.perso.T4C.player.Player;
import com.perso.T4C.quest.QuestDef;
import com.perso.T4C.quest.QuestRegistry;
import com.perso.T4C.quest.QuestService;
import org.junit.jupiter.api.Test;

class LighthavenSamaritanTest {
  @Test
  void registryCreatesTheNativeSamaritanWithoutReadingNpcDefinitions() throws Exception {
    assertEquals(460, NpcFactoryRegistry.registrations().size());
    assertInstanceOf(
        LighthavenSamaritan.class,
        NpcFactoryRegistry.create(LighthavenSamaritan.ID, new NpcContext(null)));
    assertNull(NpcFactoryRegistry.create("NotMigratedYet", new NpcContext(null)));
  }

  @Test
  void ownsOriginalAppearanceStatsAndEveryCommandTopic() throws Exception {
    LighthavenSamaritan npc = new LighthavenSamaritan(new NpcContext(null));

    assertEquals(LighthavenSamaritan.ID, npc.getTypeId());
    assertEquals(LighthavenSamaritan.SPRITE_BASE, npc.getAnimations().getSpriteBase());
    assertEquals(100, npc.getLevel());
    assertEquals(1_000_000, npc.getMaxHp());
    assertEquals(1_000_000, npc.getArmorClass());
    assertEquals(39, npc.getTopics().size());
    assertTrue(npc.isPassiveOnAttack());

    LighthavenSamaritan.DialogueTopic prerequisite =
        npc.getTopics().stream()
            .filter(topic -> "prerequisites".equals(topic.id()))
            .findFirst()
            .orElseThrow();
    assertTrue(prerequisite.matches("pre requisite"));
    assertFalse(prerequisite.matches("pre"));
  }

  @Test
  void errandStartsTheNativeRatQuest() throws Exception {
    QuestService quests = new QuestService(XpCurve.loadDefault(), null, null);
    LighthavenSamaritan npc = new LighthavenSamaritan(new NpcContext(quests));
    Player player = new Player();
    LighthavenSamaritan.DialogueTopic errand =
        npc.getTopics().stream()
            .filter(topic -> "errand".equals(topic.id()))
            .findFirst()
            .orElseThrow();

    npc.onTopic(errand, player);

    QuestDef quest = QuestRegistry.findById("lighthaven_samaritan_rats");
    assertNotNull(quest);
    assertEquals(QuestService.STATUS_ACTIVE, player.getQuestFlag(QuestService.statusFlag(quest)));
  }
}
