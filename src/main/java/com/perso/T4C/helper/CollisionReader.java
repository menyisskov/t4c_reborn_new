package com.perso.T4C.helper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * Reads collision data from a binary file.
 * File format: width (4 bytes), height (4 bytes), then width*height bytes (collision values)
 */
@Slf4j
public class CollisionReader {

    @Getter
    private final int width;
    @Getter
    private final int height;
    private final byte[] collisionData;

    public CollisionReader(File file) throws IOException {
        CollisionMapIO.CollisionMap map = CollisionMapIO.read(file);
        width = map.getWidth();
        height = map.getHeight();
        collisionData = map.getData();
        log.info("Loaded collision map: {}x{}", width, height);
    }

    /**
     * Gets collision value at grid coordinates (x, y)
     * @return 0 = no collision, 1-4 = collision types
     */
    public int getCollision(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 1; // Out of bounds = collision
        }
        return collisionData[y * width + x] & 0xFF;
    }

    /**
     * Checks if there's a collision at grid coordinates (x, y)
     */
    public boolean hasCollision(int x, int y) {
        return CollisionType.fromValue(getCollision(x, y)).isBlocksMovement();
    }

    public boolean blocksLineOfSight(int x, int y) {
        return CollisionType.fromValue(getCollision(x, y)).isBlocksLineOfSight();
    }

}

