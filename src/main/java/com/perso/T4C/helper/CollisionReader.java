package com.perso.T4C.helper;

import java.io.File;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollisionReader {
  @Getter private final int width;
  @Getter private final int height;
  private final byte[] collisionData;

  public CollisionReader(File file) throws IOException {
    CollisionMapIO.CollisionMap map = CollisionMapIO.read(file);
    width = map.getWidth();
    height = map.getHeight();
    collisionData = map.getData();
    log.info("Loaded collision map: {}x{}", width, height);
  }

  public int getCollision(int x, int y) {
    if (x < 0 || x >= width || y < 0 || y >= height) {
      return 1;
    }
    return collisionData[y * width + x] & 0xFF;
  }

  public boolean hasCollision(int x, int y) {
    return CollisionType.fromValue(getCollision(x, y)).isBlocksMovement();
  }

  public boolean blocksLineOfSight(int x, int y) {
    return CollisionType.fromValue(getCollision(x, y)).isBlocksLineOfSight();
  }
}
