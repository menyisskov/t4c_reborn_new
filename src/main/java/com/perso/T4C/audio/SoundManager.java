package com.perso.T4C.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.config.Paths;

/**
 * Centralized utility to play audio assets (Sound / Music) from the AssetManager.
 * <p>
 * - Does not throw exceptions if an asset is missing or fails to load.
 * - If an asset is not yet loaded, SoundManager loads it synchronously
 * using finishLoadingAsset before playback.
 * - Tries Sound first (ideal for short SFX), then falls back to Music if needed.
 */
public final class SoundManager {

    private static AssetManager assets;
    private static Music ambientMusic;
    private static String ambientFileName;

    private SoundManager() {
    }

    /**
     * Initializes the SoundManager with the shared AssetManager.
     */
    public static void init(AssetManager mgr) {
        assets = mgr;
    }

    /**
     * Normalizes a file name (trim, null-safe).
     */
    private static String normalize(String fileName) {
        if (fileName == null) return null;
        String f = fileName.trim();
        return f.isEmpty() ? null : f;
    }

    /**
     * Ensures an asset is loaded as the given type.
     * Loading is blocking to guarantee immediate availability.
     */
    private static void ensureLoadedAs(String path, Class<?> type) {
        try {
            if (assets == null) return;
            if (!assets.isLoaded(path)) {
                assets.load(path, type);
                assets.finishLoadingAsset(path);
            }
        } catch (Throwable ignored) {
            // Ignore loading failures to avoid breaking gameplay
        }
    }

    /**
     * Attempts to play an audio file if available.
     * Handles Sound / Music fallback logic transparently.
     */
    private static void playIfAvailable(String fileName) {
        if (!GameConstants.ENABLE_SOUNDS) return;

        String f = normalize(fileName);
        if (f == null) return;

        String path = Paths.SOUNDS_DIR + "/" + f;
        if (assets == null) return;

        try {
            // If already loaded, try Sound first, then Music
            if (assets.isLoaded(path)) {
                try {
                    Sound s = assets.get(path, Sound.class);
                    if (s != null) {
                        s.play();
                        return;
                    }
                } catch (Throwable ignored) {
                }

                try {
                    Music m = assets.get(path, Music.class);
                    if (m != null) {
                        m.setLooping(false);
                        m.setPosition(0f);
                        m.setVolume(1f);
                        m.play();
                        return;
                    }
                } catch (Throwable ignored) {
                }
            }

            // Not loaded yet: decide loading strategy based on extension
            String lc = f.toLowerCase();

            if (lc.endsWith(".wav")) {
                // Prefer Sound for WAV (short SFX)
                ensureLoadedAs(path, Sound.class);
                try {
                    Sound s = assets.get(path, Sound.class);
                    if (s != null) {
                        s.play();
                        return;
                    }
                } catch (Throwable ignored) {
                }

                // Fallback to Music if Sound failed
                ensureLoadedAs(path, Music.class);
                try {
                    Music m = assets.get(path, Music.class);
                    if (m != null) {
                        m.setLooping(false);
                        m.setPosition(0f);
                        m.setVolume(1f);
                        m.play();
                        return;
                    }
                } catch (Throwable ignored) {
                }

            } else if (lc.endsWith(".mp3") || lc.endsWith(".ogg")) {
                // Prefer Music for streamed formats
                ensureLoadedAs(path, Music.class);
                try {
                    Music m = assets.get(path, Music.class);
                    if (m != null) {
                        m.setLooping(false);
                        m.setPosition(0f);
                        m.setVolume(1f);
                        m.play();
                        return;
                    }
                } catch (Throwable ignored) {
                }

                // Fallback to Sound
                ensureLoadedAs(path, Sound.class);
                try {
                    Sound s = assets.get(path, Sound.class);
                    if (s != null) {
                        s.play();
                        return;
                    }
                } catch (Throwable ignored) {
                }

            } else {
                // Unknown extension: try Sound, then Music
                ensureLoadedAs(path, Sound.class);
                try {
                    Sound s = assets.get(path, Sound.class);
                    if (s != null) {
                        s.play();
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
                        m.setVolume(1f);
                        m.play();
                        return;
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
            // Never break the game because of audio issues
        }
    }

    /**
     * Plays an animation-related sound effect.
     */
    public static void animateSound(String fileName) {
        playIfAvailable(fileName);
    }

    /**
     * Plays a reverse animation-related sound effect.
     */
    public static void reverseAnimateSound(String fileName) {
        playIfAvailable(fileName);
    }

    /**
     * Plays a looping background ambience/music track. This intentionally uses
     * Gdx.audio directly because WAV files may already be loaded as Sound in the
     * shared AssetManager, while ambience needs Music streaming semantics.
     */
    public static void playAmbient(String fileName) {
        if (!GameConstants.ENABLE_SOUNDS) {
            stopAmbient();
            return;
        }

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
            ambientMusic.setVolume(0.7f);
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
}
