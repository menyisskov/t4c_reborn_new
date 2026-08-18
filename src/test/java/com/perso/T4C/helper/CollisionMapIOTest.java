package com.perso.T4C.helper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CollisionMapIOTest {
  @TempDir Path temporaryDirectory;

  @Test
  void preservesEveryOriginalCollisionType() throws Exception {
    byte[] values = new byte[16];
    for (int i = 0; i < values.length; i++) {
      values[i] = (byte) i;
    }
    File file = temporaryDirectory.resolve("map.colbin").toFile();
    CollisionMapIO.write(file, 8, 2, values);
    CollisionMapIO.CollisionMap restored = CollisionMapIO.read(file);
    assertEquals(8, restored.getWidth());
    assertEquals(2, restored.getHeight());
    assertArrayEquals(values, restored.getData());
  }

  @Test
  void appliesOriginalMovementAndLineOfSightPolicies() {
    assertTrue(CollisionType.ABSOLUTE.isBlocksMovement());
    assertTrue(CollisionType.FLY_OVER.isBlocksMovement());
    assertTrue(CollisionType.DEEP_WATER.isBlocksMovement());
    assertTrue(CollisionType.SHALLOW_WATER.isBlocksMovement());
    assertFalse(CollisionType.FORCE_FIELD.isBlocksMovement());
    assertTrue(CollisionType.ABSOLUTE.isBlocksLineOfSight());
    assertTrue(CollisionType.FLY_OVER.isBlocksLineOfSight());
    assertTrue(CollisionType.FORCE_FIELD.isBlocksLineOfSight());
    assertFalse(CollisionType.DEEP_WATER.isBlocksLineOfSight());
  }
}
