package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.companion.*;
import com.perso.T4C.npc.companion.CompanionDef;
import com.perso.T4C.npc.companion.CompanionSpellTrigger;
import com.perso.T4C.player.BodyPart;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CompanionDefBinaryIOTest {
  @Test
  void companionDefinitionRoundTrips(@TempDir Path dir) throws Exception {
    CompanionDef source =
        new CompanionDef(
            "wolf_pup",
            "Wolf Pup",
            List.of(new CompanionDef.Part(BodyPart.BODY, "WolfBody")),
            "WolfBase",
            50,
            5.0f,
            2,
            6,
            0.5f,
            1.2f,
            List.of(
                new CompanionDef.SpellEntry(
                    "bite", CompanionSpellTrigger.ATTACK, 1, 2.0f, 0.0f, 3, 8, 0.2f, 1.0f)));
    File file = dir.resolve("companions.bin").toFile();
    CompanionDefBinaryIO.write(file, List.of(source));
    CompanionDef read = CompanionDefBinaryIO.read(file).get(0);
    assertEquals(source.getId(), read.getId());
    assertEquals(source.getParts().size(), read.getParts().size());
    assertEquals(source.getParts().get(0).getBodyPart(), read.getParts().get(0).getBodyPart());
    assertEquals(source.getSpriteBase(), read.getSpriteBase());
    assertEquals(source.getBaseHp(), read.getBaseHp());
    assertEquals(source.getHpPerLevel(), read.getHpPerLevel());
    assertEquals(source.getDamageMin(), read.getDamageMin());
    assertEquals(source.getDamageMax(), read.getDamageMax());
    assertEquals(source.getSpells().size(), read.getSpells().size());
    assertEquals(source.getSpells().get(0).getSpellKey(), read.getSpells().get(0).getSpellKey());
    assertEquals(source.getSpells().get(0).getTrigger(), read.getSpells().get(0).getTrigger());
  }

  @Test
  void unsupportedVersionIsRejected(@TempDir Path dir) throws Exception {
    File file = dir.resolve("old-companions.bin").toFile();
    try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
      out.write("T4CCMP".getBytes(StandardCharsets.US_ASCII));
      BinaryIOUtils.writeShortLE(out, (short) 0);
      BinaryIOUtils.writeIntLE(out, 0);
    }
    assertThrows(GameException.class, () -> CompanionDefBinaryIO.read(file));
  }
}
