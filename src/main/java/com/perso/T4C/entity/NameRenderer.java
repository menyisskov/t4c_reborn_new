package com.perso.T4C.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.perso.T4C.ui.FontManager;
import com.perso.T4C.ui.SystemMessage;
import com.perso.T4C.helper.SpriteLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renders entity names in a classic T4C 1.25 style:
 * - No background box
 * - Black shadow + subtle pixel outline
 * - Bitmap font rendering only
 */
@Slf4j
public class NameRenderer {

    private static final int FONT_SIZE = 16;
    private static final Color TEXT_COLOR = SystemMessage.MESSAGE_COLOR;
    private static final Color DIALOG_TEXT_COLOR = new Color(230f / 255f, 230f / 255f, 230f / 255f, 1f);
    private static final Color KEYWORD_COLOR = new Color(180f / 255f, 1f, 0f, 1f);
    private static final Color SHADOW_COLOR = Color.BLACK;
    private static final int SHADOW_OFFSET_X = 1;
    private static final int SHADOW_OFFSET_Y = -1;
    private static final float OUTLINE_OFFSET = 1f;
    private static final float VERTICAL_OFFSET = 90f; 
    private static final float HORIZONTAL_OFFSET = 15f;
    private static final float DIALOG_PADDING = 6f;
    private static final float DIALOG_MAX_TEXT_WIDTH = 389f;
    private static final int DIALOG_MAX_LINES = 14;
    private static final int DIALOG_BORDER_SIZE = 4;
    private static TextureRegion dialogBack;
    private static TextureRegion dialogBottom;
    private static TextureRegion dialogRight;
    private static TextureRegion dialogBottomRight;
    private static boolean dialogSpritesLoaded;

    /**
     * Render an entity name above a sprite (T4C-like).
     *
     * @param batch active SpriteBatch
     * @param name entity name
     * @param centerX sprite center X (world)
     * @param topY sprite top Y (world)
     * @param entityWidth width of the entity sprite
     * @param entityHeight height of the entity sprite
     */
    public static void renderName(SpriteBatch batch, String name, float centerX, float topY, float entityWidth, float entityHeight) {
        if (name == null || name.isEmpty()) {
            return;
        }

        try {
            BitmapFont font = FontManager.getInstance().getT4CBeaulieuFont(
                    FONT_SIZE,
                    TEXT_COLOR,
                    0f,
                    Color.CLEAR,
                    0,
                    0,
                    Color.CLEAR
            );

            GlyphLayout layout = new GlyphLayout(font, name);

            float x = centerX - layout.width / 2f + HORIZONTAL_OFFSET;
            float y = topY - VERTICAL_OFFSET;

            font.setColor(SHADOW_COLOR);
            font.draw(batch, name,
                    x + SHADOW_OFFSET_X,
                    y + SHADOW_OFFSET_Y
            );

            font.draw(batch, name, x - OUTLINE_OFFSET, y);
            font.draw(batch, name, x + OUTLINE_OFFSET, y);
            font.draw(batch, name, x, y - OUTLINE_OFFSET);
            font.draw(batch, name, x, y + OUTLINE_OFFSET);

            font.setColor(TEXT_COLOR);
            font.draw(batch, name, x, y);

        } catch (Throwable t) {
            log.warn("Failed to render entity name: {}", name, t);
        }
    }

    public static void renderNameWithKeyword(SpriteBatch batch, String text, String keyword, float centerX, float topY, float entityWidth, float entityHeight) {
        renderNameWithKeywords(batch, text, keyword == null ? List.of() : List.of(keyword),
                centerX, topY, entityWidth, entityHeight);
    }

    public static void renderNameWithKeywords(SpriteBatch batch, String text, List<String> keywords,
                                               float centerX, float topY, float entityWidth, float entityHeight) {
        if (text == null || text.isEmpty()) {
            return;
        }
        try {
            BitmapFont font = FontManager.getInstance().getNpcDialogFont(DIALOG_TEXT_COLOR);
            BitmapFont keywordFont = FontManager.getInstance().getNpcDialogFont(KEYWORD_COLOR);
            List<String> lines = wrapDialogText(font, text);
            float lineHeight = font.getLineHeight();
            float contentWidth = 0f;
            for (String line : lines) contentWidth = Math.max(contentWidth, new GlyphLayout(font, line).width);
            float boxWidth = contentWidth + DIALOG_PADDING * 2f;
            float boxHeight = lineHeight * lines.size() + DIALOG_PADDING * 2f;
            float boxX = centerX - boxWidth / 2f;
            // TFCObject::DrawTalkText: x -= width / 2; y -= height.
            float boxY = topY - boxHeight;
            drawDialogBox(batch, boxX, boxY, boxWidth, boxHeight);
            float x = boxX + DIALOG_PADDING;
            float y = boxY + DIALOG_PADDING;

            List<String> keywordLower = keywords == null ? List.of() : keywords.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(value -> value.toLowerCase(Locale.ROOT)).toList();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                float lineY = y + (lineHeight * i);

                String lineLower = line.toLowerCase(Locale.ROOT);
                int index = 0;
                float cursorX = x;
                while (index < line.length()) {
                    KeywordHit keywordHit = findFirstWholeWord(lineLower, keywordLower, index);
                    int hit = keywordHit.index;
                    String segment;
                    if (hit < 0) {
                        segment = line.substring(index);
                        index = line.length();
                    } else {
                        segment = line.substring(index, hit);
                        index = hit;
                    }

                    if (!segment.isEmpty()) {
                        font.setColor(TEXT_COLOR);
                        font.draw(batch, segment, cursorX, lineY);
                        GlyphLayout segLayout = new GlyphLayout(font, segment);
                        cursorX += segLayout.width;
                    }

                    if (hit >= 0) {
                        String keySegment = line.substring(hit, hit + keywordHit.word.length());
                        keywordFont.setColor(KEYWORD_COLOR);
                        keywordFont.draw(batch, keySegment, cursorX, lineY);
                        GlyphLayout keyLayout = new GlyphLayout(keywordFont, keySegment);
                        cursorX += keyLayout.width;
                        index = hit + keywordHit.word.length();
                    }
                }
            }
        } catch (Throwable t) {
            log.warn("Failed to render entity dialog", t);
        }
    }

    private static KeywordHit findFirstWholeWord(String text, List<String> words, int fromIndex) {
        int best = -1;
        String bestWord = "";
        for (String word : words) {
            int candidate = findWholeWord(text, word, fromIndex);
            if (candidate >= 0 && (best < 0 || candidate < best || (candidate == best && word.length() > bestWord.length()))) {
                best = candidate;
                bestWord = word;
            }
        }
        return new KeywordHit(best, bestWord);
    }

    private record KeywordHit(int index, String word) { }

    private static void drawDialogBox(SpriteBatch batch, float x, float y, float width, float height) {
        loadDialogSprites();
        if (dialogBack == null) return;
        batch.setColor(Color.WHITE);
        int innerWidth = Math.max(0, Math.min(Math.round(width) - DIALOG_BORDER_SIZE,
                dialogBack.getRegionWidth()));
        int innerHeight = Math.max(0, Math.min(Math.round(height) - DIALOG_BORDER_SIZE,
                dialogBack.getRegionHeight()));

        // CDisplayTextBox::Draw uses the native 400x300 sprites and clip rects.
        // Cropping TextureRegions is the SpriteBatch equivalent; scaling would
        // deform the original border and background texture.
        drawCropped(batch, dialogBack, x, y, innerWidth, innerHeight);
        drawCropped(batch, dialogBottom, x, y + height - DIALOG_BORDER_SIZE,
                innerWidth, DIALOG_BORDER_SIZE);
        drawCropped(batch, dialogRight, x + width - DIALOG_BORDER_SIZE, y,
                DIALOG_BORDER_SIZE, innerHeight);
        drawCropped(batch, dialogBottomRight, x + width - DIALOG_BORDER_SIZE,
                y + height - DIALOG_BORDER_SIZE, DIALOG_BORDER_SIZE, DIALOG_BORDER_SIZE);
    }

    private static void drawCropped(SpriteBatch batch, TextureRegion source, float x, float y,
                                    int width, int height) {
        if (source == null || width <= 0 || height <= 0) return;
        int croppedWidth = Math.min(width, source.getRegionWidth());
        int croppedHeight = Math.min(height, source.getRegionHeight());
        TextureRegion cropped = new TextureRegion(source, 0, 0, croppedWidth, croppedHeight);
        // The world camera uses screen-style (Y-down) coordinates. As for NPC
        // sprites, flip the quad vertically so the source's top border remains
        // at the top instead of appearing next to the bottom border.
        batch.draw(cropped, x, y + croppedHeight, croppedWidth, -croppedHeight);
    }

    private static List<String> wrapDialogText(BitmapFont font, String text) {
        List<String> result = new ArrayList<>();
        for (String paragraph : text.split("\\n", -1)) {
            if (result.size() >= DIALOG_MAX_LINES) break;
            if (paragraph.isEmpty()) {
                result.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.trim().split("\\s+")) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (line.length() > 0 && new GlyphLayout(font, candidate).width > DIALOG_MAX_TEXT_WIDTH) {
                    result.add(line.toString());
                    if (result.size() >= DIALOG_MAX_LINES) break;
                    line.setLength(0);
                    line.append(word);
                } else {
                    if (line.length() > 0) line.append(' ');
                    line.append(word);
                }
            }
            if (result.size() < DIALOG_MAX_LINES && line.length() > 0) result.add(line.toString());
        }
        return result;
    }

    /** Matches legacy dialog links as complete words, never inside "healing". */
    private static int findWholeWord(String text, String word, int fromIndex) {
        if (word == null || word.isEmpty()) return -1;
        int hit = text.indexOf(word, Math.max(0, fromIndex));
        while (hit >= 0) {
            int end = hit + word.length();
            boolean startsAtBoundary = hit == 0 || !Character.isLetterOrDigit(text.charAt(hit - 1));
            boolean endsAtBoundary = end == text.length() || !Character.isLetterOrDigit(text.charAt(end));
            if (startsAtBoundary && endsAtBoundary) return hit;
            hit = text.indexOf(word, hit + 1);
        }
        return -1;
    }

    private static void loadDialogSprites() {
        if (dialogSpritesLoaded) return;
        dialogSpritesLoaded = true;
        try {
            SpriteLoader loader = SpriteLoader.getInstance();
            dialogBack = loader.getRegionFromSpriteName("V3_DialogBoxBack");
            dialogBottom = loader.getRegionFromSpriteName("V3_DialogBoxBackBottom");
            dialogRight = loader.getRegionFromSpriteName("V3_DialogBoxBackRight");
            dialogBottomRight = loader.getRegionFromSpriteName("V3_DialogBoxBackBottomRight");
        } catch (Throwable t) {
            log.warn("Failed to load original NPC dialog-box sprites", t);
        }
    }

    public static void invalidateDialogResources() {
        dialogSpritesLoaded = false;
        dialogBack = null;
        dialogBottom = null;
        dialogRight = null;
        dialogBottomRight = null;
    }

    public static List<Rectangle> getWordBounds(String text, String word, float centerX, float topY, float entityWidth, float entityHeight) {
        if (text == null || text.isEmpty() || word == null || word.isEmpty()) {
            return List.of();
        }

        try {
            BitmapFont font = FontManager.getInstance().getNpcDialogFont(DIALOG_TEXT_COLOR);
            BitmapFont keywordFont = FontManager.getInstance().getNpcDialogFont(KEYWORD_COLOR);
            float lineHeight = font.getLineHeight();

            List<String> lines = wrapDialogText(font, text);
            float contentWidth = 0f;
            for (String line : lines) contentWidth = Math.max(contentWidth, new GlyphLayout(font, line).width);
            float boxWidth = contentWidth + DIALOG_PADDING * 2f;
            float boxHeight = lineHeight * lines.size() + DIALOG_PADDING * 2f;
            float x = centerX - boxWidth / 2f + DIALOG_PADDING;
            float y = topY - boxHeight + DIALOG_PADDING;
            String wordLower = word.toLowerCase(Locale.ROOT);
            List<Rectangle> results = new ArrayList<>();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String lineLower = line.toLowerCase(Locale.ROOT);
                float lineY = y + (lineHeight * i);
                int index = findWholeWord(lineLower, wordLower, 0);
                while (index >= 0) {
                    String prefix = line.substring(0, index);
                    String token = line.substring(index, index + word.length());
                    GlyphLayout prefixLayout = new GlyphLayout(font, prefix);
                    GlyphLayout tokenLayout = new GlyphLayout(keywordFont, token);
                    float rx = x + prefixLayout.width;
                    float ry = lineY;
                    float padding = 3f;
                    results.add(new Rectangle(rx - padding, ry - padding, tokenLayout.width + padding * 2f, lineHeight + padding * 2f));
                    index = findWholeWord(lineLower, wordLower, index + word.length());
                }
            }

            return results;
        } catch (Throwable t) {
            log.warn("Failed to compute word bounds: {}", word, t);
            return List.of();
        }
    }
}
