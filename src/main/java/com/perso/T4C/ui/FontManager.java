package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.perso.T4C.config.GamePreferencesStore;
import com.perso.T4C.config.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FontManager {
  private static final int FONT_SUPERSAMPLING = 2;
  private static final float FONT_RENDER_SCALE = 1f / FONT_SUPERSAMPLING;
  private static FontManager instance;
  private final Map<String, BitmapFont> fontCache = new HashMap<>();
  private final Set<BitmapFont> externalFonts = new HashSet<>();

  private FontManager() {}

  public static FontManager getInstance() {
    if (instance == null) {
      instance = new FontManager();
    }
    return instance;
  }

  public static float logicalScale(float scale) {
    return scale * FONT_RENDER_SCALE;
  }

  public BitmapFont getT4CBeaulieuFont(int size, Color color) {
    String key = "t4c_" + size + "_" + color.toString();
    return fontCache.computeIfAbsent(key, k -> createT4CBeaulieuFont(size, color));
  }

  public BitmapFont getT4CBeaulieuFont(
      int size,
      Color color,
      float borderWidth,
      Color borderColor,
      int shadowOffsetX,
      int shadowOffsetY,
      Color shadowColor) {
    String key = "t4c_styled_" + size + "_" + color.toString() + "_" + borderWidth;
    return fontCache.computeIfAbsent(
        key,
        k ->
            createT4CBeaulieuFontStyled(
                size, color, borderWidth, borderColor, shadowOffsetX, shadowOffsetY, shadowColor));
  }

  public BitmapFont getJetBrainsMonoFont(int size, Color color) {
    String key = "jetbrains_" + size + "_" + color.toString();
    return fontCache.computeIfAbsent(key, k -> createJetBrainsMonoFont(size, color));
  }

  public BitmapFont getNpcDialogFont(Color color) {
    String key = "npc_dialog_14_" + color.toString();
    return fontCache.computeIfAbsent(key, k -> createNpcDialogFont(color));
  }

  public BitmapFont getTahomaFont(int size, Color color, boolean bold) {
    String key = "tahoma_" + (bold ? "bold_" : "") + size + "_" + color.toString();
    return fontCache.computeIfAbsent(key, k -> createTahomaFont(size, color, bold));
  }

  public BitmapFont getHaettenschweilerFont(int size, Color color) {
    String key = "haettenschweiler_" + size + "_" + color.toString();
    return fontCache.computeIfAbsent(key, k -> createHaettenschweilerFont(size, color));
  }

  public void applyHighQuality(boolean enabled) {
    Texture.TextureFilter filter =
        enabled ? Texture.TextureFilter.Linear : Texture.TextureFilter.Nearest;
    for (BitmapFont font : fontCache.values()) {
      applyFilter(font, filter);
    }
    for (BitmapFont font : externalFonts) {
      applyFilter(font, filter);
    }
  }

  public void registerExternalFont(BitmapFont font) {
    if (font == null) return;
    externalFonts.add(font);
    applyFilter(font, currentFilter());
  }

  public void unregisterExternalFont(BitmapFont font) {
    if (font != null) externalFonts.remove(font);
  }

  public void applyCurrentQuality(BitmapFont font) {
    applyFilter(font, currentFilter());
  }

  private Texture.TextureFilter currentFilter() {
    return GamePreferencesStore.get().isHighQualityFont()
        ? Texture.TextureFilter.Linear
        : Texture.TextureFilter.Nearest;
  }

  private static void applyFilter(BitmapFont font, Texture.TextureFilter filter) {
    if (font == null || font.getRegions() == null) return;
    for (TextureRegion region : font.getRegions()) {
      if (region != null && region.getTexture() != null) {
        region.getTexture().setFilter(filter, filter);
      }
    }
  }

  private BitmapFont createT4CBeaulieuFont(int size, Color color) {
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Paths.FONT));
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = size * FONT_SUPERSAMPLING;
    parameter.color = color;
    parameter.flip = true;
    BitmapFont font = generator.generateFont(parameter);
    generator.dispose();
    return prepare(font);
  }

  private BitmapFont createT4CBeaulieuFontStyled(
      int size,
      Color color,
      float borderWidth,
      Color borderColor,
      int shadowOffsetX,
      int shadowOffsetY,
      Color shadowColor) {
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Paths.FONT));
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = size * FONT_SUPERSAMPLING;
    parameter.color = color;
    parameter.borderWidth = borderWidth * FONT_SUPERSAMPLING;
    parameter.borderColor = borderColor;
    parameter.shadowOffsetX = shadowOffsetX * FONT_SUPERSAMPLING;
    parameter.shadowOffsetY = shadowOffsetY * FONT_SUPERSAMPLING;
    parameter.shadowColor = shadowColor;
    parameter.flip = true;
    BitmapFont font = generator.generateFont(parameter);
    generator.dispose();
    return prepare(font);
  }

  private BitmapFont createJetBrainsMonoFont(int size, Color color) {
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(
            Gdx.files.internal(com.perso.T4C.config.Paths.FONT_JETBRAINS_MONO));
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = size * FONT_SUPERSAMPLING;
    parameter.color = color;
    parameter.flip = true;
    BitmapFont font = generator.generateFont(parameter);
    generator.dispose();
    return prepare(font);
  }

  private BitmapFont createNpcDialogFont(Color color) {
    String windowsDirectory = System.getenv("WINDIR");
    if (windowsDirectory == null || windowsDirectory.isBlank()) {
      return createJetBrainsMonoFont(14, color);
    }
    com.badlogic.gdx.files.FileHandle verdana =
        Gdx.files.absolute(windowsDirectory + "\\Fonts\\verdana.ttf");
    if (!verdana.exists()) {
      return createJetBrainsMonoFont(14, color);
    }
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(verdana);
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = 14 * FONT_SUPERSAMPLING;
    parameter.color = color;
    parameter.flip = true;
    BitmapFont font = generator.generateFont(parameter);
    generator.dispose();
    return prepare(font);
  }

  private BitmapFont createTahomaFont(int size, Color color, boolean bold) {
    String windowsDirectory = System.getenv("WINDIR");
    if (windowsDirectory == null || windowsDirectory.isBlank()) {
      return createJetBrainsMonoFont(size, color);
    }
    String fileName = bold ? "tahomabd.ttf" : "tahoma.ttf";
    com.badlogic.gdx.files.FileHandle tahoma =
        Gdx.files.absolute(windowsDirectory + "\\Fonts\\" + fileName);
    if (!tahoma.exists()) {
      return createJetBrainsMonoFont(size, color);
    }
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(tahoma);
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = size * FONT_SUPERSAMPLING;
    parameter.color = color;
    parameter.flip = true;
    BitmapFont font = generator.generateFont(parameter);
    generator.dispose();
    return prepare(font);
  }

  private BitmapFont createChewyFont(int size, Color color) {
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal(com.perso.T4C.config.Paths.FONT_CHEWY));
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = size * FONT_SUPERSAMPLING;
    parameter.color = color;
    parameter.flip = true;
    BitmapFont font = generator.generateFont(parameter);
    generator.dispose();
    font.getData().setLineHeight(27f * FONT_SUPERSAMPLING);
    return prepare(font);
  }

  private BitmapFont createHaettenschweilerFont(int size, Color color) {
    String windowsDirectory = System.getenv("WINDIR");
    if (windowsDirectory == null || windowsDirectory.isBlank()) {
      return createChewyFont(size, color);
    }
    com.badlogic.gdx.files.FileHandle hatten =
        Gdx.files.absolute(windowsDirectory + "\\Fonts\\HATTEN.TTF");
    if (!hatten.exists()) {
      return createChewyFont(size, color);
    }
    FreeTypeFontGenerator generator = new FreeTypeFontGenerator(hatten);
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = size * FONT_SUPERSAMPLING;
    parameter.color = color;
    parameter.flip = true;
    BitmapFont font = generator.generateFont(parameter);
    generator.dispose();
    font.getData().setLineHeight(27f * FONT_SUPERSAMPLING);
    return prepare(font);
  }

  private BitmapFont prepare(BitmapFont font) {
    font.getData().setScale(FONT_RENDER_SCALE);
    applyFilter(font, currentFilter());
    return font;
  }

  public void dispose() {
    for (BitmapFont font : fontCache.values()) {
      if (font != null) {
        font.dispose();
      }
    }
    fontCache.clear();
    externalFonts.clear();
  }
}
