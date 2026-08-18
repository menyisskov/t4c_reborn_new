package com.perso.T4C;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.QuestDefBinaryIO;
import com.perso.T4C.quest.QuestDef;
import java.io.File;
import java.util.Map;
import org.junit.jupiter.api.Test;

class T4CContentStudioQuestValidationTest {
  @Test
  void questEditorPayloadsExposeResolvedText() throws Exception {
    T4CContentStudio studio = new T4CContentStudio();
    QuestDef quest = QuestDefBinaryIO.read(new File(Paths.QUESTS_BIN)).get(0);
    Map<String, Object> questPayload = studio.questToMap(quest);
    assertEquals("Les rats du sous-sol du temple", questPayload.get("title"));
    assertFalse(String.valueOf(questPayload.get("offerText")).contains("${"));
    assertFalse(String.valueOf(questPayload.get("completionText")).contains("${"));
    assertFalse(String.valueOf(questPayload.get("completedText")).contains("${"));
  }
}
