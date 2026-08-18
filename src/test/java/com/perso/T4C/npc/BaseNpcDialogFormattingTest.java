package com.perso.T4C.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.npc.core.*;
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
        "Parlez de magie, oracle et portail.",
        BaseNPC.removeNpcDialogueQuotes("Parlez de \"magie\", “oracle” et «portail»."));
  }

  @Test
  void removesQuotesOnlyAroundInteractiveKeywords() {
    assertEquals(
        "Parlez-moi de Dark One, mais gardez \"cette citation\".",
        BaseNPC.stripDialogKeywordQuotes(
            "Parlez-moi de \"Dark One\", mais gardez \"cette citation\".", List.of("dark one")));
  }

  @Test
  void supportsFrenchAndCurlyQuotationMarks() {
    assertEquals(
        "Cherchez magie puis oracle.",
        BaseNPC.stripDialogKeywordQuotes(
            "Cherchez «magie» puis “oracle”.", List.of("magie", "oracle")));
  }
}
