package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.npc.registry.*;
import com.perso.T4C.npc.script.ScriptedNpc;
import com.perso.T4C.player.Player;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScriptedNpcKeywordTest {
  private final NpcSpec.DialogueTopic topic =
      new NpcSpec.DialogueTopic(List.of("sorts"), null, List.of());

  @Test
  void keywordMatchingIgnoresCasePunctuationAndAccents() {
    assertTrue(ScriptedNpc.matches(topic, "SORTS !"));
    assertTrue(ScriptedNpc.matches(topic, "Montrez-moi vos sorts, Iraltok."));
    assertTrue(
        ScriptedNpc.matches(
            new NpcSpec.DialogueTopic(List.of("guérison"), null, List.of()), "GUERISON."));
  }

  @Test
  void keywordMatchingUsesWholeWords() {
    assertFalse(ScriptedNpc.matches(topic, "ressorts"));
  }

  @Test
  void namingOneCmdAndWordSaysTheWholeSentence() {
    String script =
        """
                CmdAND(INTL(1, "READY"), INTL(2, "REBORN"))
                    SetYesNo(REBIRTH)
                Command2(INTL(2, "REBORN"), INTL(3, "REBIRTH"))
                    INTL(4, "Once you are reborn...")
                """;

    assertEquals("READY REBORN", ScriptedNpc.sentenceForKeyword(script, "ready"));
    assertEquals("READY REBORN", ScriptedNpc.sentenceForKeyword(script, "reborn"));
    assertEquals("ready reborn", ScriptedNpc.sentenceForKeyword(script, "ready reborn"));
    assertEquals("bonjour", ScriptedNpc.sentenceForKeyword(script, "bonjour"));
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
