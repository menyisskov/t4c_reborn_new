package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.quest.CrimsonscaleLetterRules;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OnlineQuestFlagsTest {
  private static final QuestCatalog QUESTS = QuestCatalog.bundled();

  @Test void letterAndCatalogedQuestFlagsAreSupportedWithinTheirRanges() {
    assertTrue(OnlineQuestFlags.supported(Map.of(), QUESTS));
    assertTrue(OnlineQuestFlags.supported(Map.of(CrimsonscaleLetterRules.LETTER_FLAG, 1,
        "quest.renewed_wards.status", 2, "quest.renewed_wards.kills", 12, "killlog.Skeleton", 4), QUESTS));
    assertTrue(OnlineQuestFlags.supported(Map.of(
        "quest.__NEWBIE_QUEST.kills", 15,
        "quest.lighthaven_samaritan_rats.status", 1), QUESTS));
    assertFalse(OnlineQuestFlags.supported(Map.of("quest.__NEWBIE_QUEST.kills", 16), QUESTS));
    assertFalse(OnlineQuestFlags.supported(Map.of("quest.renewed_wards.kills", 13), QUESTS));
    assertFalse(OnlineQuestFlags.supported(Map.of("quest.renewed_wards.status", 3), QUESTS));
  }

  @Test void unlistedFlagsAndOutOfRangeValuesStayUnsupported() {
    assertFalse(OnlineQuestFlags.supported(Map.of("unimplemented.quest", 1), QUESTS));
    assertFalse(OnlineQuestFlags.supported(Map.of(CrimsonscaleLetterRules.LETTER_FLAG, 0), QUESTS));
    assertFalse(OnlineQuestFlags.supported(Map.of("killlog.Skeleton", -1), QUESTS));
  }
}
