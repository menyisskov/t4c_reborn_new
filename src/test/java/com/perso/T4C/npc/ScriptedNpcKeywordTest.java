package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.perso.T4C.npc.core.NpcSpec;
import com.perso.T4C.npc.core.ScriptedNpc;
import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScriptedNpcKeywordTest {
  private final NpcSpec.DialogueTopic topic =
      new NpcSpec.DialogueTopic(List.of("spells"), null, List.of());

  @Test
  void keywordMatchingIgnoresCasePunctuationAndAccents() {
    assertTrue(ScriptedNpc.matches(topic, "SPELLS!"));
    assertTrue(ScriptedNpc.matches(topic, "Show me your spells, Iraltok."));
    assertTrue(
        ScriptedNpc.matches(
            new NpcSpec.DialogueTopic(List.of("café"), null, List.of()), "CAFE."));
  }

  @Test
  void keywordMatchingUsesWholeWords() {
    assertFalse(ScriptedNpc.matches(topic, "misspells"));
  }

  @Test
  void translatedDialogueReplacesPlayerNamePlaceholders() throws Exception {
    Player player = new Player();
    player.setName("Alyssa");

    assertEquals(
        "Hello Alyssa, a messenger brought this parchment.",
        ScriptedNpc.resolvePlayerName("Hello %s, a messenger brought this parchment.", player));
    assertEquals(
        "Alyssa, welcome back Alyssa.",
        ScriptedNpc.resolvePlayerName("%s, welcome back %s.", player));

    String kilhiamWelcome = ScriptedNpc.resolvePlayerName("${npc.welcome.kilhiam}", player);
    assertTrue(kilhiamWelcome.startsWith("Hello Alyssa, a messenger"));
    assertFalse(kilhiamWelcome.contains("%s"));
  }
}
