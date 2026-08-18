package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MainGameScreenLoadingTest {
  @Test
  void loadingProgressHasStableNonZeroStepCount() {
    int count = MainGameScreen.loadingStepCount();
    assertTrue(count > 0);
    for (int completed = 0; completed <= count; completed++) {
      float progress = completed / (float) count;
      assertTrue(progress >= 0f && progress <= 1f);
      assertEquals(completed, Math.round(progress * count));
    }
  }
}
