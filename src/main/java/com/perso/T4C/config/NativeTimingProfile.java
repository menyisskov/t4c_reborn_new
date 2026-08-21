package com.perso.T4C.config;

/** Timing and movement values matching the native 1.68 client profiles. */
public record NativeTimingProfile(int fps, int framing, int movX, int movY, int done) {
  public static final NativeTimingProfile FPS_17 = new NativeTimingProfile(17, 1, 8, 4, 4);
  public static final NativeTimingProfile FPS_34 = new NativeTimingProfile(34, 2, 4, 2, 8);

  public float frameDurationSeconds() { return 1f / (fps * framing); }

  public static NativeTimingProfile current() {
    return GamePreferencesStore.get().isEnable32FPS() ? FPS_34 : FPS_17;
  }
}
