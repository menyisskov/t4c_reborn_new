package com.perso.T4C.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.config.Paths;

public final class SoundManager {
  private static AssetManager assets;
  private static Music ambientMusic;
  private static String ambientFileName;

  private SoundManager() {}

  public static void init(AssetManager mgr) {
    assets = mgr;
  }

  private static String normalize(String fileName) {
    if (fileName == null) return null;
    String f = fileName.trim();
    return f.isEmpty() ? null : f;
  }

  private static void ensureLoadedAs(String path, Class<?> type) {
    try {
      if (assets == null) return;
      if (!assets.isLoaded(path)) {
        assets.load(path, type);
        assets.finishLoadingAsset(path);
      }
    } catch (Throwable ignored) {
    }
  }

  private static void playIfAvailable(String fileName, float volume) {
    if (volume <= 0f) return;
    String f = normalize(fileName);
    if (f == null) return;
    String path = Paths.SOUNDS_DIR + "/" + f;
    if (assets == null) return;
    try {
      if (assets.isLoaded(path)) {
        try {
          Sound s = assets.get(path, Sound.class);
          if (s != null) {
            s.play(volume);
            return;
          }
        } catch (Throwable ignored) {
        }
        try {
          Music m = assets.get(path, Music.class);
          if (m != null) {
            m.setLooping(false);
            m.setPosition(0f);
            m.setVolume(volume);
            m.play();
            return;
          }
        } catch (Throwable ignored) {
        }
      }
      String lc = f.toLowerCase();
      if (lc.endsWith(".wav")) {
        ensureLoadedAs(path, Sound.class);
        try {
          Sound s = assets.get(path, Sound.class);
          if (s != null) {
            s.play(volume);
            return;
          }
        } catch (Throwable ignored) {
        }
        ensureLoadedAs(path, Music.class);
        try {
          Music m = assets.get(path, Music.class);
          if (m != null) {
            m.setLooping(false);
            m.setPosition(0f);
            m.setVolume(volume);
            m.play();
            return;
          }
        } catch (Throwable ignored) {
        }
      } else if (lc.endsWith(".mp3") || lc.endsWith(".ogg")) {
        ensureLoadedAs(path, Music.class);
        try {
          Music m = assets.get(path, Music.class);
          if (m != null) {
            m.setLooping(false);
            m.setPosition(0f);
            m.setVolume(volume);
            m.play();
            return;
          }
        } catch (Throwable ignored) {
        }
        ensureLoadedAs(path, Sound.class);
        try {
          Sound s = assets.get(path, Sound.class);
          if (s != null) {
            s.play(volume);
            return;
          }
        } catch (Throwable ignored) {
        }
      } else {
        ensureLoadedAs(path, Sound.class);
        try {
          Sound s = assets.get(path, Sound.class);
          if (s != null) {
            s.play(volume);
            return;
          }
        } catch (Throwable ignored) {
        }
        ensureLoadedAs(path, Music.class);
        try {
          Music m = assets.get(path, Music.class);
          if (m != null) {
            m.setLooping(false);
            m.setPosition(0f);
            m.setVolume(volume);
            m.play();
            return;
          }
        } catch (Throwable ignored) {
        }
      }
    } catch (Throwable ignored) {
    }
  }

  public static void animateSound(String fileName) {
    playIfAvailable(fileName, GamePreferencesStore.get().getEffectsVolume());
  }

  public static void interfaceSound(String fileName) {
    playIfAvailable(fileName, GamePreferencesStore.get().getEffectsVolume());
  }

  public static void reverseAnimateSound(String fileName) {
    playIfAvailable(fileName, GamePreferencesStore.get().getEffectsVolume());
  }

  public static void playAmbient(String fileName) {
    String f = normalize(fileName);
    if (f == null) {
      stopAmbient();
      return;
    }
    if (f.equals(ambientFileName) && ambientMusic != null && ambientMusic.isPlaying()) {
      return;
    }
    stopAmbient();
    try {
      String path = Paths.SOUNDS_DIR + "/" + f;
      ambientMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
      ambientMusic.setLooping(true);
      ambientMusic.setVolume(GamePreferencesStore.get().getMusicVolume());
      ambientMusic.play();
      ambientFileName = f;
    } catch (Throwable ignored) {
      ambientMusic = null;
      ambientFileName = null;
    }
  }

  public static void stopAmbient() {
    try {
      if (ambientMusic != null) {
        ambientMusic.stop();
        ambientMusic.dispose();
      }
    } catch (Throwable ignored) {
    } finally {
      ambientMusic = null;
      ambientFileName = null;
    }
  }

  public static void applyVolumes() {
    if (ambientMusic != null) {
      ambientMusic.setVolume(GamePreferencesStore.get().getMusicVolume());
      if (!ambientMusic.isPlaying()) ambientMusic.play();
    }
  }
}
