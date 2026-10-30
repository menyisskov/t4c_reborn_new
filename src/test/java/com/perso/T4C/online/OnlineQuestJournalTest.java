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
            "quest.unhosted.status", 1);
    var progress = progress(flags);

    var entries = OnlineQuestJournal.entries(progress);

    assertEquals(1, entries.size());
    assertEquals("renewed_wards", entries.getFirst().id());
    assertEquals(5, entries.getFirst().kills());
    assertEquals(12, entries.getFirst().requiredKills());
    assertTrue(
        OnlineQuestJournal.lines(progress).stream().anyMatch(line -> line.contains("5/12")));
    assertEquals(flags, progress.questFlags());
  }

  @Test
  void showsCompletedSupportedQuestAndOmitsNeverStartedQuest() {
    var progress = progress(Map.of("quest.renewed_wards.status", 2));

    var entries = OnlineQuestJournal.entries(progress);

    assertEquals(1, entries.size());
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
