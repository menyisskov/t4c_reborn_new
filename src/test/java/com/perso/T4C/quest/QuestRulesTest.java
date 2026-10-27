package com.perso.T4C.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;

class QuestRulesTest {
  private static QuestDef hunt(int required) {
    return new QuestDef(
        "renewed_wards", "Renewed Wards", "warden_aelric", "Skeleton", required, 0, 10, 10, 5, 500,
        400, "offer", "done", "already done");
  }

  private static ToIntFunction<String> reader(Map<String, Integer> flags) {
    return key -> flags.getOrDefault(key, 0);
  }

  private static void apply(Map<String, Integer> flags, Map<String, Integer> writes) {
    flags.putAll(writes);
  }

  @Test
  void acceptingStartsNotStartedQuestOnceAndNeverRestarts() {
    QuestDef quest = hunt(3);
    Map<String, Integer> flags = new HashMap<>();
    Map<String, Integer> first = QuestRules.accept(reader(flags), quest);
    apply(flags, first);
    assertEquals(QuestRules.STATUS_ACTIVE, QuestRules.status(reader(flags), quest));
    assertEquals(0, QuestRules.kills(reader(flags), quest));

    apply(flags, QuestRules.creditObjective(reader(flags), quest, true, 0, 10, 10));
    assertTrue(QuestRules.accept(reader(flags), quest).isEmpty());
    assertEquals(1, QuestRules.kills(reader(flags), quest));
  }

  @Test
  void killCreditStopsAtTheRequirement() {
    QuestDef quest = hunt(2);
    Map<String, Integer> flags = new HashMap<>();
    apply(flags, QuestRules.accept(reader(flags), quest));
    apply(flags, QuestRules.creditObjective(reader(flags), quest, true, 0, 10, 10));
    apply(flags, QuestRules.creditObjective(reader(flags), quest, true, 0, 10, 10));
    assertEquals(2, QuestRules.kills(reader(flags), quest));
    assertTrue(
        QuestRules.creditObjective(reader(flags), quest, true, 0, 10, 10).isEmpty());
  }

  @Test
  void circleIncludesTheRadiusEdgeAndRejectsOneBeyond() {
    QuestDef quest = hunt(5);
    assertTrue(QuestRules.inObjectiveArea(quest, 0, 15, 10));
    assertFalse(QuestRules.inObjectiveArea(quest, 0, 16, 10));
    assertTrue(QuestRules.inObjectiveArea(quest, 0, 13, 14));
    assertFalse(QuestRules.inObjectiveArea(quest, 0, 14, 14));
    assertFalse(QuestRules.inObjectiveArea(quest, 1, 10, 10));
  }

  @Test
  void activationFlagSharesTheKillCounterAndCountsAsActive() {
    QuestDef quest =
        new QuestDef(
            "gate_quest", "Gate", "giver", "Skeleton", 4, 0, 10, 10, 5, 0, 0, "o", "c", "d",
            "shared_gate");
    assertEquals("quest.shared_gate.kills", QuestRules.killsFlag(quest));
    Map<String, Integer> flags = new HashMap<>(Map.of("shared_gate", 1));
    assertEquals(QuestRules.STATUS_ACTIVE, QuestRules.status(reader(flags), quest));
    apply(flags, QuestRules.creditObjective(reader(flags), quest, true, 0, 10, 10));
    assertEquals(1, flags.get("quest.shared_gate.kills"));
  }

  @Test
  void killLogCountsEveryKillEvenWithoutAnyQuest() {
    String flag = QuestRules.killLogFlag("Skeleton");
    assertEquals("killlog.Skeleton", flag);
    Map<String, Integer> flags = new HashMap<>();
    apply(flags, QuestRules.recordKillLog(reader(flags), flag));
    apply(flags, QuestRules.recordKillLog(reader(flags), flag));
    assertEquals(2, flags.get(flag));
  }

  @Test
  void creditNeedsActiveQuestMatchingTargetAndCircle() {
    QuestDef quest = hunt(3);
    Map<String, Integer> flags = new HashMap<>();
    assertTrue(QuestRules.creditObjective(reader(flags), quest, true, 0, 10, 10).isEmpty());
    apply(flags, QuestRules.accept(reader(flags), quest));
    assertTrue(QuestRules.creditObjective(reader(flags), quest, false, 0, 10, 10).isEmpty());
    assertTrue(QuestRules.creditObjective(reader(flags), quest, true, 0, 40, 40).isEmpty());
  }

  @Test
  void completionFlagsMarkCompletedWithFullKillCount() {
    QuestDef quest = hunt(3);
    Map<String, Integer> writes = QuestRules.completionFlags(quest);
    assertEquals(QuestRules.STATUS_COMPLETED, writes.get("quest.renewed_wards.status"));
    assertEquals(3, writes.get("quest.renewed_wards.kills"));
  }
}
