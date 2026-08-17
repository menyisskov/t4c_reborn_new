package com.perso.T4C.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.perso.T4C.MyGame;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.ui.FontManager;

/** Loads a selected character over several rendered frames. */
final class CharacterLoadingScreen implements Screen {
    private static final float BAR_WIDTH = 314f;
    private static final float BAR_HEIGHT = 12f;
    private static final float FRAME_WIDTH = 360f;
    private static final float FRAME_HEIGHT = 26f;

    private final MyGame game;
    private final CharacterSelectionScreen selectionScreen;
    private final MainGameScreen mainScreen;
    private final SpriteBatch batch;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ScreenViewport viewport = new ScreenViewport(camera);
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private final TextureRegion emptyBar;
    private final TextureRegion progressBar;
    private final TextureRegion progressFrame;
    private boolean finished;
    private boolean readyToEnter;

    CharacterLoadingScreen(MyGame game, CharacterSelectionScreen selectionScreen) {
        this.game = game;
        this.selectionScreen = selectionScreen;
        this.mainScreen = MainGameScreen.createProgressive(game);
        this.batch = game.batch;
        this.font = FontManager.getInstance().getT4CBeaulieuFont(22, Color.WHITE);
        this.emptyBar = loadSprite("GUI_BackChStat_Empty");
        this.progressBar = loadSprite("GUI_BackChStat_XP");
        this.progressFrame = loadSprite("64kTameProgressFrame");
        camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private static TextureRegion loadSprite(String name) {
        try {
            TextureRegion region = SpriteLoader.getInstance().getRegionFromSpriteName(name);
            if (region == null) throw new IllegalStateException("Missing loading-bar sprite: " + name);
            return region;
        } catch (Exception error) {
            throw new IllegalStateException("Unable to load loading-bar sprite: " + name, error);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float progress = mainScreen.getCompletedLoadingSteps() / (float) MainGameScreen.loadingStepCount();
        drawProgress(progress);

        if (finished) return;
        if (readyToEnter) {
            finished = true;
            selectionScreen.dispose();
            game.setScreen(mainScreen);
            dispose();
            return;
        }
        try {
            boolean hasMore = mainScreen.advanceLoading();
            if (!hasMore) {
                // Keep the loading screen for one final frame so 100% is visible.
                readyToEnter = true;
            }
        } catch (Exception error) {
            finished = true;
            mainScreen.dispose();
            selectionScreen.showLoadError(error);
            game.setScreen(selectionScreen);
            dispose();
        }
    }

    private void drawProgress(float progress) {
        float centerX = camera.viewportWidth / 2f;
        float centerY = camera.viewportHeight / 2f;
        float barX = centerX - BAR_WIDTH / 2f;
        float barY = centerY;

        String title = I18n.key("character.loading");
        String percent = Math.round(progress * 100f) + " %";
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        GuiDraw.drawRegionFlipped(batch, progressFrame,
                centerX - FRAME_WIDTH / 2f, barY - (FRAME_HEIGHT - BAR_HEIGHT) / 2f,
                FRAME_WIDTH, FRAME_HEIGHT);
        GuiDraw.drawRegionFlipped(batch, emptyBar, barX, barY, BAR_WIDTH, BAR_HEIGHT);
        if (progress > 0f) {
            GuiDraw.drawRegionFlipped(batch, progressBar, barX, barY,
                    BAR_WIDTH * progress, BAR_HEIGHT);
        }
        layout.setText(font, title);
        font.draw(batch, title, centerX - layout.width / 2f, barY - 38f);
        layout.setText(font, percent);
        font.draw(batch, percent, centerX - layout.width / 2f,
                barY + BAR_HEIGHT + 14f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void show() { }
    @Override public void hide() { }
    @Override public void pause() { }
    @Override public void resume() { }

    @Override
    public void dispose() {
        // Textures and fonts are owned by the shared SpriteLoader and FontManager caches.
    }
}
