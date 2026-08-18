package com.perso.T4C.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PlayerExhaustionAnimationTest {
  @Test
  void physicalSpellExhaustionStopsWalkAnimationAndPath() throws Exception {
    Player player = new Player();
    player.setMapBounds(1_000, 1_000);
    player.move(1f, 0f, 0.016f);
    player.getMovement().setPath(List.of(new PlayerMovement.PathNode(10, 10)));
    assertTrue(player.getMovement().isMoving());
    player.applyExhaustion(1_000L, 750L, 750L);
    assertFalse(player.getMovement().isMoving());
    assertTrue(player.getMovement().getPath().isEmpty());
  }
}
