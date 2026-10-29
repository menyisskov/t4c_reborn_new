package com.perso.T4C.audio;

import com.badlogic.gdx.assets.AssetManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test double for the sound system: installed as the sound manager's asset manager it records the
 * file of every attempt to play a sound (the manager asks {@code isLoaded} first) and plays
 * nothing. Tests that use it hold the {@code sound.manager} resource lock.
 */
public final class SoundSpy extends AssetManager {
  private final List<String> asked = Collections.synchronizedList(new ArrayList<>());

  public static SoundSpy install() {
    SoundSpy spy = new SoundSpy();
    SoundManager.init(spy);
    return spy;
  }

  public static void remove() {
    SoundManager.init(null);
  }

  @Override
  public synchronized boolean isLoaded(String fileName) {
    asked.add(fileName);
    return false;
  }

  public List<String> asked() {
    return List.copyOf(asked);
  }

  public void clear() {
    asked.clear();
  }
}
