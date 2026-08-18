package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.spell.SpellData;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpellBinaryIOTest {
  @TempDir Path temporaryDirectory;

  @Test
  void versionFourKeepsTimerFrequency() throws Exception {
    SpellData spell =
        new SpellData(
            "Poison", "", "1", 0, 0, 0, 1, true, true, "", null, null, 0, 0, null, null, 0, "20100",
            "6000", 0, null, 10020, 2, 11, 2, "100", "1000", "750", "750", 0, 0, false, List.of());
    File binary = temporaryDirectory.resolve("spells.bin").toFile();
    SpellBinaryIO.write(binary, List.of(spell));
    SpellData restored = SpellBinaryIO.read(binary).get(0);
    assertEquals("6000", restored.getFrequency());
    assertEquals(10020, restored.getSpellId());
  }
}
