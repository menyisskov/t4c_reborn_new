package com.perso.T4C.online;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.online.model.CharacterProgress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OnlineQuestJournalTest {
  @Test
  void projectsOnlyCommittedSupportedQuestFlagsAndKeepsObjectivesReadOnly() {
    var flags =
        Map.of(
            "quest.renewed_wards.status", 1,
            "quest.renewed_wards.kills", 5,
            "quest.lighthaven_samaritan_rats.status", 1,
            "quest.__NEWBIE_QUEST.kills", 9,
            "quest.unhosted.status", 1);
    var progress = progress(flags);

    var entries = OnlineQuestJournal.entries(progress);

    assertEquals(2, entries.size());
    var wards = entries.stream().filter(entry -> entry.id().equals("renewed_wards")).findFirst().orElseThrow();
    assertEquals(5, wards.kills());
    assertEquals(12, wards.requiredKills());
    var rats = entries.stream().filter(entry -> entry.id().equals("lighthaven_samaritan_rats")).findFirst().orElseThrow();
    assertEquals(9, rats.kills());
    assertEquals(15, rats.requiredKills());
    assertTrue(
        OnlineQuestJournal.lines(progress).stream().anyMatch(line -> line.contains("5/12")));
    assertEquals(flags, progress.questFlags());
  }

  @Test
  void showsCompletedSupportedQuestAndOmitsNeverStartedQuest() {
    var progress = progress(Map.of("quest.lighthaven_samaritan_rats.status", 2));

    var entries = OnlineQuestJournal.entries(progress);

    assertEquals(1, entries.size());
    assertEquals("lighthaven_samaritan_rats", entries.getFirst().id());
    assertTrue(entries.getFirst().completed());
    assertTrue(
        OnlineQuestJournal.lines(progress).stream().anyMatch(line -> line.contains("Completed")));
    assertEquals(List.of(), OnlineQuestJournal.entries(null));
  }

  private static CharacterProgress progress(Map<String, Integer> flags) {
    return new CharacterProgress(2, "warrior", "male",
        new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(10, 10, 0, 0),
        new CharacterProgress.Advancement(1, 0, 0, 0, 0), List.of(), Map.of(), Map.of(), Set.of(),
        flags == null ? Map.of() : flags, List.of(), 0,
        new CharacterProgress.Respawn(0, 0, 0), List.of(), Map.of(), 0, null);
  }
}
