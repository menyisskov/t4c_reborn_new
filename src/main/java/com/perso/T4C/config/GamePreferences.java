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
    private boolean lightGraphics;
    private boolean highQualityEffects = true;
    private boolean animatedWater = true;
    private boolean animatedDecorLights = true;
    private boolean weatherEffects = true;
    private boolean dithering = true;
    private boolean transparentGui = true;
    private boolean seraphAnimation = true;
    private boolean statusEffects = true;
    private boolean xpBarText = true;
    private boolean displayGold = true;
    private boolean fullscreenMacros = true;
    private boolean lockTarget;
    private boolean highQualityFont;
    private boolean zoomEnabled = true;
    private boolean itemDetails = true;
    private boolean newHealthBar = true;
    private boolean oldStatusBar;
    private boolean newShadows;
    private boolean logNpcMessages;
    private boolean logPlayerMessages;
    private boolean chatLogging;
    private String chatLogFilename = "t4c-chat.log";
}
