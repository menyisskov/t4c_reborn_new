package com.perso.T4C.tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.screens.MapRenderer;
import java.io.File;
import java.util.List;

/** Captures actual runtime terrain, decor and objects through a hidden OpenGL window. */
public final class MapSceneCapture {
  private MapSceneCapture() {}

  public static void main(String[] args) {
    if (args.length != 6) {
      throw new IllegalArgumentException(
          "Usage: MapSceneCapture x y width height scale output.png");
    }
    int x = Integer.parseInt(args[0]), y = Integer.parseInt(args[1]);
    int width = Integer.parseInt(args[2]), height = Integer.parseInt(args[3]);
    double scale = Double.parseDouble(args[4]);
    int pixelsX = (int) Math.ceil(width * 32 * scale);
    int pixelsY = (int) Math.ceil(height * 16 * scale);
    if (x < 0
        || y < 0
        || width <= 0
        || height <= 0
        || !Double.isFinite(scale)
        || pixelsX < 1
        || pixelsY < 1
        || pixelsX > 8192
        || pixelsY > 8192) {
      throw new IllegalArgumentException("Invalid crop or output larger than 8192x8192");
    }
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("T4C map capture");
    config.setWindowedMode(64, 64);
    config.setInitialVisible(false);
    config.disableAudio(true);
    new Lwjgl3Application(
        new ApplicationAdapter() {
          @Override
          public void create() {
            try (MapReader map = new MapReader(new File(Paths.MAP))) {
              if ((long) x + width > map.getWidth() || (long) y + height > map.getHeight()) {
                throw new IllegalArgumentException("Crop exceeds map bounds");
              }
              SpriteLoader sprites = SpriteLoader.getInstance();
              sprites.loadSpriteBin(Paths.SPRITE_BIN);
              SpriteBatch ground = new SpriteBatch(), decor = new SpriteBatch();
              MapRenderer renderer =
                  new MapRenderer(map, sprites, ground, decor, null, ModifSprites.empty(), 0);
              FrameBuffer buffer = new FrameBuffer(Pixmap.Format.RGBA8888, pixelsX, pixelsY, false);
              OrthographicCamera camera = new OrthographicCamera();
              camera.setToOrtho(true, width * 32, height * 16);
              camera.position.set((x + width / 2f) * 32, (y + height / 2f) * 16, 0);
              camera.update();
              buffer.begin();
              Gdx.gl.glClearColor(0, 0, 0, 1);
              Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
              int minX = Math.max(0, x - 32), minY = Math.max(0, y - 32);
              int maxX = Math.min(map.getWidth() - 1, x + width + 32);
              int maxY = Math.min(map.getHeight() - 1, y + height + 32);
              renderer.renderGroundOnly(camera, minX, maxX, minY, maxY);
              renderer.renderEntitiesWithDecors(camera, -1, -1, minX, maxX, minY, maxY, List.of());
              Pixmap pixels = Pixmap.createFromFrameBuffer(0, 0, pixelsX, pixelsY);
              var output = Gdx.files.absolute(new File(args[5]).getAbsolutePath());
              output.parent().mkdirs();
              PixmapIO.writePNG(output, pixels, -1, true);
              pixels.dispose();
              buffer.end();
              buffer.dispose();
              renderer.dispose();
              ground.dispose();
              decor.dispose();
              System.out.println("Captured " + output.path());
            } catch (Exception e) {
              throw new IllegalStateException("Map capture failed", e);
            } finally {
              Gdx.app.exit();
            }
          }
        },
        config);
  }
}
