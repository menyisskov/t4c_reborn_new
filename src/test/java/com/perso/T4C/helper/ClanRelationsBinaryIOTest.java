package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.MonsterClan;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClanRelationsBinaryIOTest {
  @Test
  void clanRelationRoundTrips(@TempDir Path dir) throws Exception {
    ClanRelationsBinaryIO.Entry source =
        new ClanRelationsBinaryIO.Entry(MonsterClan.ANIMAL, MonsterClan.DEMON);
    File file = dir.resolve("clans.bin").toFile();
    ClanRelationsBinaryIO.write(file, List.of(source));
    ClanRelationsBinaryIO.Entry read = ClanRelationsBinaryIO.read(file).get(0);
    assertEquals(source.source, read.source);
    assertEquals(source.target, read.target);
  }

  @Test
  void unsupportedVersionIsRejected(@TempDir Path dir) throws Exception {
    File file = dir.resolve("old-clans.bin").toFile();
    try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
      out.write("T4CCLN".getBytes(StandardCharsets.US_ASCII));
      BinaryIOUtils.writeShortLE(out, (short) 0);
      BinaryIOUtils.writeIntLE(out, 0);
    }
    assertThrows(GameException.class, () -> ClanRelationsBinaryIO.read(file));
  }
}
