package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpriteBinIOTest {
  private static final String BASE = "sprites";

  private static SpriteBinIO.Packed sprite(String name, int pngBytes) {
    byte[] png = new byte[pngBytes];
    for (int i = 0; i < png.length; i++) {
      png[i] = (byte) (name.hashCode() + i);
    }
    return new SpriteBinIO.Packed(name, 32, 16, 1, 2, 3, 4, 0, png);
  }

  private static List<SpriteBinIO.Packed> spritesSpanning(int shards) {
    int pngBytes = 64 * 1024;
    long perShard = SpriteBinIO.SHARD_MAX_BYTES / pngBytes + 1;
    List<SpriteBinIO.Packed> sprites = new ArrayList<>();
    for (int i = 0; i < perShard * shards; i++) {
      sprites.add(sprite("Sprite_" + i, pngBytes));
    }
    return sprites;
  }

  private static void assertSameSprites(
      List<SpriteBinIO.Packed> expected, List<SpriteBinIO.Packed> actual) {
    assertEquals(expected.size(), actual.size(), "sprite count");
    for (int i = 0; i < expected.size(); i++) {
      SpriteBinIO.Packed e = expected.get(i);
      SpriteBinIO.Packed a = actual.get(i);
      assertEquals(e.name(), a.name(), "name at " + i);
      assertEquals(e.width(), a.width(), "width at " + i);
      assertEquals(e.height(), a.height(), "height at " + i);
      assertEquals(e.off1X(), a.off1X(), "off1X at " + i);
      assertEquals(e.off1Y(), a.off1Y(), "off1Y at " + i);
      assertEquals(e.off2X(), a.off2X(), "off2X at " + i);
      assertEquals(e.off2Y(), a.off2Y(), "off2Y at " + i);
      assertEquals(e.type(), a.type(), "type at " + i);
      assertArrayEquals(e.png(), a.png(), "png at " + i);
    }
  }

  @Test
  void smallLibraryRoundTripsThroughASingleShard(@TempDir Path dir) throws Exception {
    List<SpriteBinIO.Packed> sprites =
        List.of(
            sprite("Ground_Water (1, 1)", 128),
            sprite("CastleWall_North", 4096),
            sprite("Café_Naïve_Test", 64));
    assertEquals(1, SpriteBinIO.writeSharded(dir, BASE, sprites));
    assertTrue(Files.isRegularFile(dir.resolve("sprites_0.bin")));
    assertFalse(Files.exists(dir.resolve("sprites_1.bin")));
    assertSameSprites(sprites, SpriteBinIO.readAllToList(dir, BASE));
  }

  @Test
  void largeLibraryIsSplitAndConcatenatedBackInOrder(@TempDir Path dir) throws Exception {
    List<SpriteBinIO.Packed> sprites = spritesSpanning(3);
    int shardCount = SpriteBinIO.writeSharded(dir, BASE, sprites);
    assertTrue(shardCount >= 3, "expected at least 3 shards, got " + shardCount);
    for (int i = 0; i < shardCount; i++) {
      assertTrue(Files.isRegularFile(dir.resolve("sprites_" + i + ".bin")), "shard " + i);
    }
    assertSameSprites(sprites, SpriteBinIO.readAllToList(dir, BASE));
  }

  @Test
  void everyShardStaysUnderTheHundredMegabyteLimit(@TempDir Path dir) throws Exception {
    SpriteBinIO.writeSharded(dir, BASE, spritesSpanning(3));
    for (Path shard : SpriteBinIO.resolveShards(dir, BASE)) {
      assertTrue(
          Files.size(shard) < 100L * 1024 * 1024,
          shard.getFileName() + " is " + Files.size(shard) + " bytes");
    }
  }

  @Test
  void shrinkingRewriteRemovesStaleShards(@TempDir Path dir) throws Exception {
    int before = SpriteBinIO.writeSharded(dir, BASE, spritesSpanning(3));
    assertTrue(before >= 3);
    List<SpriteBinIO.Packed> smaller = List.of(sprite("Lonely", 256));
    assertEquals(1, SpriteBinIO.writeSharded(dir, BASE, smaller));
    for (int i = 1; i < before; i++) {
      assertFalse(Files.exists(dir.resolve("sprites_" + i + ".bin")), "stale shard " + i);
    }
    assertSameSprites(smaller, SpriteBinIO.readAllToList(dir, BASE));
  }

  @Test
  void shardsAreOrderedNumericallyNotLexicographically(@TempDir Path dir) throws Exception {
    List<SpriteBinIO.Packed> sprites = spritesSpanning(12);
    int shardCount = SpriteBinIO.writeSharded(dir, BASE, sprites);
    assertTrue(shardCount >= 10, "expected 10+ shards, got " + shardCount);
    List<Path> shards = SpriteBinIO.resolveShards(dir, BASE);
    for (int i = 0; i < shards.size(); i++) {
      assertEquals("sprites_" + i + ".bin", shards.get(i).getFileName().toString());
    }
    assertSameSprites(sprites, SpriteBinIO.readAllToList(dir, BASE));
  }

  @Test
  void monolithicLibraryIsStillReadWhenNoShardExists(@TempDir Path dir) throws Exception {
    List<SpriteBinIO.Packed> sprites = List.of(sprite("Legacy_A", 512), sprite("Legacy_B", 128));
    SpriteBinIO.writePayload(dir.resolve("sprites.bin"), sprites);
    assertEquals(List.of(dir.resolve("sprites.bin")), SpriteBinIO.resolveShards(dir, BASE));
    assertSameSprites(sprites, SpriteBinIO.readAllToList(dir, BASE));
  }

  @Test
  void shardsWinOverALeftoverMonolithAndItIsRemovedOnWrite(@TempDir Path dir) throws Exception {
    SpriteBinIO.writePayload(dir.resolve("sprites.bin"), List.of(sprite("Stale_Legacy", 64)));
    List<SpriteBinIO.Packed> sprites = List.of(sprite("Fresh", 256));
    SpriteBinIO.writeSharded(dir, BASE, sprites);
    assertFalse(Files.exists(dir.resolve("sprites.bin")), "legacy monolith should be removed");
    assertSameSprites(sprites, SpriteBinIO.readAllToList(dir, BASE));
  }

  @Test
  void emptyDirectoryResolvesToNoShards(@TempDir Path dir) throws Exception {
    assertTrue(SpriteBinIO.resolveShards(dir, BASE).isEmpty());
    assertTrue(SpriteBinIO.readAllToList(dir, BASE).isEmpty());
  }

  @Test
  void unrelatedBinFilesAreNotMistakenForShards(@TempDir Path dir) throws Exception {
    SpriteBinIO.writeSharded(dir, BASE, List.of(sprite("Real", 128)));
    Files.writeString(dir.resolve("sprites_backup.bin"), "not a shard");
    Files.writeString(dir.resolve("sprites2.bin"), "not a shard either");
    Files.writeString(dir.resolve("sprites_1x.bin"), "nor this");
    assertEquals(List.of(dir.resolve("sprites_0.bin")), SpriteBinIO.resolveShards(dir, BASE));
  }
}
