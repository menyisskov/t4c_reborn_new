package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.quest.QuestDef;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuestDefBinaryIOTest {
  @Test
  void questDefinitionRoundTrips(@TempDir Path dir) throws Exception {
    QuestDef source =
        new QuestDef(
            "test_rats",
            "${quest.test_rats.title}",
            "TestNpc",
            "Brown Rat",
            10,
            1,
            304,
            383,
            120,
            500,
            300,
            "${quest.test_rats.offer}",
            "${quest.test_rats.completion}",
            "${quest.test_rats.completed}",
            "__TEST_RATS_ACTIVE");
    File file = dir.resolve("quests.bin").toFile();
    QuestDefBinaryIO.write(file, List.of(source));
    QuestDef read = QuestDefBinaryIO.read(file).get(0);
    assertEquals(source.getId(), read.getId());
    assertEquals(source.getTitle(), read.getTitle());
    assertEquals(source.getGiverNpc(), read.getGiverNpc());
    assertEquals(source.getTargetMonster(), read.getTargetMonster());
    assertEquals(source.getRequiredKills(), read.getRequiredKills());
    assertEquals(source.getTargetWorldZ(), read.getTargetWorldZ());
    assertEquals(source.getAreaCenterX(), read.getAreaCenterX());
    assertEquals(source.getAreaCenterY(), read.getAreaCenterY());
    assertEquals(source.getAreaRadiusTiles(), read.getAreaRadiusTiles());
    assertEquals(source.getRewardGold(), read.getRewardGold());
    assertEquals(source.getRewardXp(), read.getRewardXp());
    assertEquals(source.getOfferText(), read.getOfferText());
    assertEquals(source.getCompletionText(), read.getCompletionText());
    assertEquals(source.getCompletedText(), read.getCompletedText());
    assertEquals(source.getActivationFlag(), read.getActivationFlag());
  }

  @Test
  void unsupportedVersionIsRejected(@TempDir Path dir) throws Exception {
    File file = dir.resolve("old-quests.bin").toFile();
    try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
      out.write("T4CQST".getBytes(StandardCharsets.US_ASCII));
      BinaryIOUtils.writeShortLE(out, (short) 0);
      BinaryIOUtils.writeIntLE(out, 0);
    }
    assertThrows(GameException.class, () -> QuestDefBinaryIO.read(file));
  }
}
