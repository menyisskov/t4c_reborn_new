package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.perso.T4C.npc.core.BaseNPC;
import java.util.List;
import org.junit.jupiter.api.Test;

class BaseNpcDialogFormattingTest {
  @Test
  void usesByeAsTheSharedFarewellLink() {
    assertEquals("> Bye.", BaseNPC.FAREWELL_DIALOG_LINK);
  }

  @Test
  void removesAllQuotationMarksFromDisplayedNpcDialogue() {
    assertEquals(
        "Speak of magic, oracle and portal.",
        BaseNPC.removeNpcDialogueQuotes("Speak of \"magic\", “oracle” and «portal»."));
  }

  @Test
  void removesQuotesOnlyAroundInteractiveKeywords() {
    assertEquals(
        "Tell me of Dark One, but keep \"this quote\".",
        BaseNPC.stripDialogKeywordQuotes(
            "Tell me of \"Dark One\", but keep \"this quote\".", List.of("dark one")));
  }

  @Test
  void supportsGuillemetAndCurlyQuotationMarks() {
    assertEquals(
        "Seek magic then oracle.",
        BaseNPC.stripDialogKeywordQuotes(
            "Seek «magic» then “oracle”.", List.of("magic", "oracle")));
  }
}
