package com.perso.T4C.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.Texture;
import com.perso.T4C.config.Paths;
import com.perso.T4C.config.GamePreferencesStore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages fonts used throughout the application.
 * Provides T4C Beaulieu and JetBrains Mono fonts with various sizes and configurations.
 */
public class FontManager {

    private static FontManager instance;

    private final Map<String, BitmapFont> fontCache = new HashMap<>();
    private final Set<BitmapFont> externalFonts = new HashSet<>();

    private FontManager() {
        // Private constructor for singleton
    }

    /**
     * Gets the singleton instance of FontManager.
     */
    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    /**
     * Gets or creates a T4C Beaulieu font with the specified size and color.
     *
     * @param size  Font size in pixels
     * @param color Font color
     * @return BitmapFont instance
     */
    public BitmapFont getT4CBeaulieuFont(int size, Color color) {
        String key = "t4c_" + size + "_" + color.toString();
        return fontCache.computeIfAbsent(key, k -> createT4CBeaulieuFont(size, color));
    }

    /**
     * Gets or creates a T4C Beaulieu font with border and shadow effects.
     *
     * @param size          Font size in pixels
     * @param color         Font color
     * @param borderWidth   Border width
     * @param borderColor   Border color
     * @param shadowOffsetX Shadow X offset
     * @param shadowOffsetY Shadow Y offset
     * @param shadowColor   Shadow color
     * @return BitmapFont instance
     */
    public BitmapFont getT4CBeaulieuFont(int size, Color color, float borderWidth, Color borderColor, int shadowOffsetX, int shadowOffsetY, Color shadowColor) {
        String key = "t4c_styled_" + size + "_" + color.toString() + "_" + borderWidth;
        return fontCache.computeIfAbsent(key, k -> createT4CBeaulieuFontStyled(size, color, borderWidth, borderColor, shadowOffsetX, shadowOffsetY, shadowColor));
    }

    /**
     * Gets or creates a JetBrains Mono font with the specified size and color.
     *
     * @param size  Font size in pixels
     * @param color Font color
     * @return BitmapFont instance
     */
    public BitmapFont getJetBrainsMonoFont(int size, Color color) {
        String key = "jetbrains_" + size + "_" + color.toString();
        return fontCache.computeIfAbsent(key, k -> createJetBrainsMonoFont(size, color));
    }

    /** The original CDisplayTextBox font: Verdana, 14 px. */
    public BitmapFont getNpcDialogFont(Color color) {
        String key = "npc_dialog_14_" + color.toString();
        return fontCache.computeIfAbsent(key, k -> createNpcDialogFont(color));
    }

    /**
     * Gets or creates a Tahoma (or Tahoma Bold) font with the specified size and color.
     *
     * @param size  Font size in pixels
     * @param color Font color
     * @param bold  Whether to use Tahoma Bold
     * @return BitmapFont instance
     */
    public BitmapFont getTahomaFont(int size, Color color, boolean bold) {
        String key = "tahoma_" + (bold ? "bold_" : "") + size + "_" + color.toString();
        return fontCache.computeIfAbsent(key, k -> createTahomaFont(size, color, bold));
    }

    /**
     * Gets or creates a Haettenschweiler font with the specified size and color.
     *
     * @param size  Font size in pixels
     * @param color Font color
     * @return BitmapFont instance
     */
    public BitmapFont getHaettenschweilerFont(int size, Color color) {
        String key = "haettenschweiler_" + size + "_" + color.toString();
        return fontCache.computeIfAbsent(key, k -> createHaettenschweilerFont(size, color));
    }

    /** Applies the original high-font switch immediately to every cached font. */
    public void applyHighQuality(boolean enabled) {
        Texture.TextureFilter filter = enabled ? Texture.TextureFilter.Linear : Texture.TextureFilter.Nearest;
        for (BitmapFont font : fontCache.values()) {
            applyFilter(font, filter);
        }
        for (BitmapFont font : externalFonts) {
            applyFilter(font, filter);
        }
    }

    /** Registers a font not owned by the cache so the quality switch updates it too. */
    public void registerExternalFont(BitmapFont font) {
        if (font == null) return;
        externalFonts.add(font);
        applyFilter(font, currentFilter());
    }

    public void unregisterExternalFont(BitmapFont font) {
        if (font != null) externalFonts.remove(font);
    }

    private Texture.TextureFilter currentFilter() {
        return GamePreferencesStore.get().isHighQualityFont()
                ? Texture.TextureFilter.Linear : Texture.TextureFilter.Nearest;
    }

    private static void applyFilter(BitmapFont font, Texture.TextureFilter filter) {
        if (font == null || font.getRegions() == null) return;
        for (TextureRegion region : font.getRegions()) {
            if (region != null && region.getTexture() != null) {
                region.getTexture().setFilter(filter, filter);
            }
        }
    }

    /**
     * Creates a T4C Beaulieu font with basic configuration.
     */
    private BitmapFont createT4CBeaulieuFont(int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Paths.FONT));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.flip = true;

        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        return prepare(font);
    }

    /**
     * Creates a T4C Beaulieu font with border and shadow effects.
     */
    private BitmapFont createT4CBeaulieuFontStyled(int size, Color color, float borderWidth, Color borderColor, int shadowOffsetX, int shadowOffsetY, Color shadowColor) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Paths.FONT));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.borderWidth = borderWidth;
        parameter.borderColor = borderColor;
        parameter.shadowOffsetX = shadowOffsetX;
        parameter.shadowOffsetY = shadowOffsetY;
        parameter.shadowColor = shadowColor;
        parameter.flip = true;

        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        return prepare(font);
    }

    /**
     * Creates a JetBrains Mono font.
     */
    private BitmapFont createJetBrainsMonoFont(int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(com.perso.T4C.config.Paths.FONT_JETBRAINS_MONO));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
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
        com.badlogic.gdx.files.FileHandle verdana = Gdx.files.absolute(
                windowsDirectory + "\\Fonts\\verdana.ttf");
        if (!verdana.exists()) {
            return createJetBrainsMonoFont(14, color);
        }
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(verdana);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 14;
        parameter.color = color;
        parameter.flip = true;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return prepare(font);
    }

    /**
     * Creates a Tahoma (or Tahoma Bold) font loaded from the Windows system fonts directory.
     * Falls back to JetBrains Mono if Tahoma is not available on the host system.
     */
    private BitmapFont createTahomaFont(int size, Color color, boolean bold) {
        String windowsDirectory = System.getenv("WINDIR");
        if (windowsDirectory == null || windowsDirectory.isBlank()) {
            return createJetBrainsMonoFont(size, color);
        }
        String fileName = bold ? "tahomabd.ttf" : "tahoma.ttf";
        com.badlogic.gdx.files.FileHandle tahoma = Gdx.files.absolute(
                windowsDirectory + "\\Fonts\\" + fileName);
        if (!tahoma.exists()) {
            return createJetBrainsMonoFont(size, color);
        }
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(tahoma);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.flip = true;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return prepare(font);
    }

    /**
     * Creates a Chewy font.
     */
    private BitmapFont createChewyFont(int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(com.perso.T4C.config.Paths.FONT_CHEWY));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.flip = true;

        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        font.getData().setLineHeight(27f);
        return prepare(font);
    }

    /**
     * Creates a Haettenschweiler font loaded from the Windows system fonts directory.
     * Falls back to Chewy if Haettenschweiler is not available on the host system.
     */
    private BitmapFont createHaettenschweilerFont(int size, Color color) {
        String windowsDirectory = System.getenv("WINDIR");
        if (windowsDirectory == null || windowsDirectory.isBlank()) {
            return createChewyFont(size, color);
        }
        com.badlogic.gdx.files.FileHandle hatten = Gdx.files.absolute(
                windowsDirectory + "\\Fonts\\HATTEN.TTF");
        if (!hatten.exists()) {
            return createChewyFont(size, color);
        }
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(hatten);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.flip = true;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        font.getData().setLineHeight(27f);
        return prepare(font);
    }

    private BitmapFont prepare(BitmapFont font) {
        applyFilter(font, currentFilter());
        return font;
    }

    /**
     * Clears the font cache and disposes all fonts.
     * Should be called when the application exits.
     */
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

