package com.perso.T4C.world;

import com.badlogic.gdx.graphics.Color;

public class DayNightCycle {
  private static final float DAY_SECONDS = 24f * 3600f;
  private static final float NIGHT_START_HOUR = 18f;
  private static final float NIGHT_END_HOUR = 6f;
  private static final float MIN_AMBIENT = 5f / 30f;
  private static final float TRANSITION_HOURS = 1f;
  private float elapsedSeconds;
  private boolean forceDaylight;
  private boolean unlit;

  public DayNightCycle() {
    this(7f * 3600f);
  }

  public DayNightCycle(float initialHour) {
    this.elapsedSeconds = initialHour * 3600f;
  }

  public void update(float delta) {
    elapsedSeconds = (elapsedSeconds + delta) % DAY_SECONDS;
  }

  public void forceDaylight(boolean force) {
    this.forceDaylight = force;
  }

  public void setUnlitWorld(boolean unlit) {
    this.unlit = unlit;
  }

  public float getHour() {
    return elapsedSeconds / 3600f;
  }

  public void setHour(float hour) {
    elapsedSeconds = ((hour % 24f) + 24f) % 24f * 3600f;
  }

  public boolean isNight() {
    float hour = getHour();
    return hour < NIGHT_END_HOUR || hour >= NIGHT_START_HOUR;
  }

  public float getAmbientLevel() {
    if (forceDaylight) {
      return 1f;
    }
    if (unlit) {
      return MIN_AMBIENT;
    }
    float hour = getHour();
    if (hour >= NIGHT_END_HOUR + TRANSITION_HOURS && hour < NIGHT_START_HOUR - TRANSITION_HOURS) {
      return 1f;
    }
    if (hour >= NIGHT_START_HOUR + TRANSITION_HOURS || hour < NIGHT_END_HOUR - TRANSITION_HOURS) {
      return MIN_AMBIENT;
    }
    if (hour >= NIGHT_START_HOUR - TRANSITION_HOURS && hour < NIGHT_START_HOUR + TRANSITION_HOURS) {
      float t = (hour - (NIGHT_START_HOUR - TRANSITION_HOURS)) / (2f * TRANSITION_HOURS);
      return 1f - t * (1f - MIN_AMBIENT);
    }
    float wrappedHour = hour < NIGHT_END_HOUR ? hour + 24f : hour;
    float t = (wrappedHour - (NIGHT_END_HOUR + 24f - TRANSITION_HOURS)) / (2f * TRANSITION_HOURS);
    return MIN_AMBIENT + t * (1f - MIN_AMBIENT);
  }

  public Color getOverlayColor() {
    float ambient = getAmbientLevel();
    return new Color(0f, 0f, 0f, 1f - ambient);
  }
}
