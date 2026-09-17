package com.perso.T4C.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GamePreferences {
  private float musicVolume = 0.7f;
  private float effectsVolume = 1f;
  private float brightness = 1f;
  private boolean fullscreen = false;
  private boolean vSync = true;
  private boolean enable32FPS = false;
  private boolean showHudValues = true;
  private boolean transparentGui = true;
  private boolean seraphAnimation = true;
  private boolean xpBarText = true;
  private boolean highQualityFont;
  private boolean logNpcMessages;
  private boolean logPlayerMessages;
  private boolean chatLogging;
  private String chatLogFilename = "t4c-chat.log";
  private List<MacroBinding> macros = new ArrayList<>();
}
