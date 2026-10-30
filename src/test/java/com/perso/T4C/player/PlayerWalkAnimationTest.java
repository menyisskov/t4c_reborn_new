package com.perso.T4C.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.perso.T4C.config.NativeTimingProfile;
import org.junit.jupiter.api.Test;

class PlayerWalkAnimationTest {
  private static final float EPSILON = 1e-5f;

  @Test
  void oneStrideOfGroundAdvancesExactlyOneWalkFrame() {
    PlayerAnimations animations = new Player().getAnimations();

    animations.update(0.001f, true, PlayerAnimations.walkStridePixels());

    assertEquals(
        NativeTimingProfile.current().frameDurationSeconds(), animations.getAnimTimer(), EPSILON);
  }

  @Test
  void theLegsFollowTheGroundNotTheClock() {
    PlayerAnimations slowFrames = new Player().getAnimations();
    PlayerAnimations slowClock = new Player().getAnimations();

    for (int i = 0; i < 10; i++) {
      slowFrames.update(0.001f, true, 5f);
      slowClock.update(1f, true, 5f);
    }

    assertEquals(slowFrames.getAnimTimer(), slowClock.getAnimTimer(), EPSILON);
  }

  @Test
  void standingStillOrBlockedFreezesTheLegsAtTheRestFrame() {
    PlayerAnimations animations = new Player().getAnimations();
    animations.update(0.05f, true, 40f);

    animations.update(0.05f, false, 40f);

    assertEquals(0f, animations.getAnimTimer(), 0f);
  }

  @Test
  void movingWithoutCoveringGroundDoesNotAdvanceTheLegs() {
    PlayerAnimations animations = new Player().getAnimations();

    animations.update(0.05f, true, 0f);
    animations.update(0.05f, true, -3f);

    assertEquals(0f, animations.getAnimTimer(), 0f);
  }

  @Test
  void strideIsTheOriginalGamesEightPixelsInEitherTimingProfile() {
    assertEquals(8f, PlayerAnimations.walkStridePixels(), 0f);
  }

  @Test
  void walkedDistanceIsHorizontalEquivalentAndIgnoresTeleports() {
    assertEquals(32f, PlayerAnimations.walkDistance(0f, 0f, 32f, 16f), EPSILON);
    assertEquals(32f, PlayerAnimations.walkDistance(0f, 0f, 0f, 16f), EPSILON);
    assertEquals(0f, PlayerAnimations.walkDistance(0f, 0f, 1_000f, 0f), 0f);
    assertEquals(0f, PlayerAnimations.walkDistance(Float.NaN, Float.NaN, 5f, 5f), 0f);
  }

  @Test
  void aWalkingPlayerCoversGroundAndTheLegsCountIt() {
    Player player = new Player();
    player.setMapBounds(10_000, 10_000);
    player.setWorldPosition(320f, 320f, 0);
    player.update(0.016f);
    float before = player.getAnimations().getAnimTimer();

    for (int i = 0; i < 30; i++) {
      player.move(1f, 0f, 0.016f);
      player.update(0.016f);
    }

    float stridesWalked =
        (player.getCoordinates().getX() - 320f) / PlayerAnimations.walkStridePixels();
    float framesPlayed =
        (player.getAnimations().getAnimTimer() - before)
            / NativeTimingProfile.current().frameDurationSeconds();
    assertEquals(stridesWalked, framesPlayed, 0.01f);
  }
}
