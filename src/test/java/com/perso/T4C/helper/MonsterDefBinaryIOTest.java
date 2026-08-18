package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.MonsterDef;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MonsterDefBinaryIOTest {
  @Test
  void monsterDefinitionRoundTrips(@TempDir Path dir) throws Exception {
    MonsterDef source =
        new MonsterDef(
            "brown_rat",
            "${monster.brown_rat.name}",
            10,
            0,
            1,
            5,
            1,
            3,
            60_000L,
            "walk",
            "attack",
            "death",
            "atk.wav",
            "death.wav",
            "hit.wav",
            1,
            5,
            List.of(new MonsterDef.LootDrop("Cheese", 0.25f)),
            true,
            2.5f,
            8,
            9,
            10,
            5,
            5,
            5,
            1,
            new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},
            3,
            5,
            1,
            4,
            100,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            40,
            1,
            3,
            true,
            List.of(new MonsterDef.Attack("1d4+1", 1, 50, 0, 0, 0)),
            true,
            5,
            List.of("Rat"),
            java.util.Map.of("OnDeath", "GiveItem(42)"));
    File file = dir.resolve("monsters.bin").toFile();
    MonsterDefBinaryIO.write(file, List.of(source));
    MonsterDef read = MonsterDefBinaryIO.read(file).get(0);
    assertEquals(source.getName(), read.getName());
    assertEquals(source.getHealth(), read.getHealth());
    assertEquals(source.getHitDamageMin(), read.getHitDamageMin());
    assertEquals(source.getHitDamageMax(), read.getHitDamageMax());
    assertEquals(source.getLoot().size(), read.getLoot().size());
    assertEquals(source.getLoot().get(0).getItem(), read.getLoot().get(0).getItem());
    assertEquals(source.getStr(), read.getStr());
    assertEquals(source.getResists().length, read.getResists().length);
    assertEquals(source.getLevel(), read.getLevel());
    assertEquals(source.getAggro(), read.getAggro());
    assertEquals(source.getClan(), read.getClan());
    assertEquals(source.isCanAttack(), read.isCanAttack());
    assertEquals(source.getAttacks().size(), read.getAttacks().size());
    assertEquals(source.getAttacks().get(0).getName(), read.getAttacks().get(0).getName());
    assertEquals(source.isTameable(), read.isTameable());
    assertEquals(source.getTameMaxLevel(), read.getTameMaxLevel());
    assertEquals(source.getSpawnAliases(), read.getSpawnAliases());
    assertEquals(source.getSourceEvents(), read.getSourceEvents());
  }

  @Test
  void unsupportedVersionIsRejected(@TempDir Path dir) throws Exception {
    File file = dir.resolve("old-monsters.bin").toFile();
    try (DataOutputStream out = new DataOutputStream(BinaryIOUtils.openOutputStream(file, 1024))) {
      out.write("T4CMON".getBytes(StandardCharsets.US_ASCII));
      BinaryIOUtils.writeShortLE(out, (short) 99);
      BinaryIOUtils.writeIntLE(out, 0);
    }
    assertThrows(GameException.class, () -> MonsterDefBinaryIO.read(file));
  }
}
