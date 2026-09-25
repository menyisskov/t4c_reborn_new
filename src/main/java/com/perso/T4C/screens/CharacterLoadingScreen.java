package com.perso.T4C.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.perso.T4C.MyGame;
import com.perso.T4C.gui.core.GuiDraw;
import com.perso.T4C.helper.LocalCharacterStore;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;

final class CharacterLoadingScreen implements Screen {
  private static final float BACKGROUND_DIM = 0.35f;
  private final MyGame game;
  private final CharacterSelectionScreen selectionScreen;
  private final MainGameScreen mainScreen;
  private final SpriteBatch batch;
  private final OrthographicCamera camera = new OrthographicCamera();
  private final ScreenViewport viewport = new ScreenViewport(camera);
  private final LoadingBar loadingBar = new LoadingBar();
  private final TextureRegion background;
  private final String title;
  private boolean finished;
  private boolean readyToEnter;

  CharacterLoadingScreen(MyGame game, CharacterSelectionScreen selectionScreen) {
    this.game = game;
    this.selectionScreen = selectionScreen;
    this.mainScreen = MainGameScreen.createProgressive(game);
    this.batch = game.batch;
    this.background = loadBackground();
    LocalCharacterStore.CharacterSlot slot = LocalCharacterStore.getActiveCharacter();
    this.title =
        slot == null
            ? I18n.key("character.loading")
            : I18n.message("character.loading.named", slot.name());
    camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  private static TextureRegion loadBackground() {
    try {
      return SpriteLoader.getInstance().getRegionFromSpriteName("Back01_1280");
    } catch (Exception ignored) {
      return null;
    }
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    float progress =
        mainScreen.getCompletedLoadingSteps() / (float) MainGameScreen.loadingStepCount();
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
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    if (background != null) {
      batch.setColor(BACKGROUND_DIM, BACKGROUND_DIM, BACKGROUND_DIM, 1f);
      GuiDraw.drawRegionFlipped(
          batch, background, 0f, 0f, camera.viewportWidth, camera.viewportHeight);
      batch.setColor(Color.WHITE);
    }
    loadingBar.draw(batch, camera.viewportWidth / 2f, camera.viewportHeight / 2f, progress, title);
    batch.end();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  @Override
  public void show() {}

  @Override
  public void hide() {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void dispose() {}
}
