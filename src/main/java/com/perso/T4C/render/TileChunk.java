package com.perso.T4C.render;

/**
 * Container holding fully resolved tiles for a fixed-size chunk.
 *
 * @param tiles length = CHUNK_SIZE*CHUNK_SIZE
 */
public record TileChunk(int chunkX, int chunkY, ResolvedTile[] tiles) {
}
