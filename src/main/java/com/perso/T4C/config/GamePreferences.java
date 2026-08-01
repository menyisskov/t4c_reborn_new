package com.perso.T4C.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Mutable, persisted user preferences. Values are sanitized by
 * {@link GamePreferencesStore} whenever they enter the application.
 */
@Getter
@Setter
public class GamePreferences {
    private float musicVolume = 0.7f;
    private float effectsVolume = 1f;
    private float brightness = 1f;
    private boolean fullscreen = false;
    private boolean vSync = true;
    private boolean showHudValues = true;
}
