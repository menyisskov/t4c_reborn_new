package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.i18n.I18n;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class OnlineTalkPanelTest {
  @Test
  void aQuestGiverListsItsAuthoredKeywordsAndChoosingSendsTheTopicIndex() {
    var panel = new OnlineTalkPanel();
    panel.open("WardenAelric", "Warden Aelric");

    assertTrue(panel.isOpen());
    var lines = panel.lines();
    assertEquals(I18n.message("online.talk.header", "Warden Aelric"), lines.get(0));
    assertEquals(4, lines.size(), "title, two topics, hint: " + lines);
    assertEquals(I18n.key("online.talk.panel_hint"), lines.get(3));
    String first = I18n.key("npc.topic_keyword.wardenaelric.0.0");
    assertTrue(lines.get(1).toLowerCase().contains(first.toLowerCase()), lines.get(1));
    assertTrue(lines.get(1).startsWith("1. "));
    assertEquals(OptionalInt.of(0), panel.choose(1), "the quest offer is topic 0");
    assertEquals(OptionalInt.of(1), panel.choose(2));
    assertTrue(panel.choose(3).isEmpty());
  }

  @Test
  void anNpcWithoutAuthoredTopicsLeavesThePanelClosed() {
    var panel = new OnlineTalkPanel();
    panel.open("NoSuchNpc", "Nobody");
    assertFalse(panel.isOpen());
    assertTrue(panel.lines().isEmpty());
    assertTrue(panel.choose(1).isEmpty());
  }

  @Test
  void closingForgetsTheNpc() {
    var panel = new OnlineTalkPanel();
    panel.open("WardenAelric", "Warden Aelric");
    panel.close();
    assertFalse(panel.isOpen());
    assertNull(panel.npc());
    assertTrue(panel.choose(1).isEmpty());
  }
}
