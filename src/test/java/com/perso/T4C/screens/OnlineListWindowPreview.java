package com.perso.T4C.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perso.T4C.gui.screen.OnlineListWindow;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.online.OnlineTalkPanel;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

/**
 * Developer preview, not shipped and not a golden: draws the online lists in the journal window
 * with representative lines (no network, no server) on a flat background and saves PNGs. Run
 * from the repository root with the test classpath: {@code java -cp ...
 * com.perso.T4C.screens.OnlineListWindowPreview <output-directory>}.
 */
public final class OnlineListWindowPreview extends ApplicationAdapter {
  private static final int WIDTH = 1280;
  private static final int HEIGHT = 720;

  private final Path outputDir;
  private SpriteBatch batch;
  private final OrthographicCamera camera = new OrthographicCamera();
  private OnlineListWindow window;
  private List<String> lines = List.of();
  private int frame;
  private int stage;

  OnlineListWindowPreview(Path outputDir) {
    this.outputDir = outputDir;
  }

  public static void main(String[] args) {
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Online list window preview");
    config.setWindowedMode(WIDTH, HEIGHT);
    config.setResizable(false);
    config.disableAudio(true);
    config.useVsync(true);
    new Lwjgl3Application(
        new OnlineListWindowPreview(Path.of(args.length > 0 ? args[0] : ".")), config);
  }

  @Override
  public void create() {
    batch = new SpriteBatch();
    camera.setToOrtho(true, WIDTH, HEIGHT);
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister();
    window = new OnlineListWindow(() -> lines, () -> {});
  }

  private void show(int step) {
    switch (step) {
      case 0 ->
          lines =
              List.of(
                  I18n.key("online.inventory.title"),
                  I18n.message("online.inventory.unequip_row", 1, "Apprentice Robe", "BODY"),
                  I18n.message("online.inventory.unequip_row", 2, "Oak Staff", "WEAPON"),
                  I18n.message("online.inventory.equip_row", 3, "Leather Boots"),
                  I18n.message("online.inventory.equip_row", 4, "Light Healing Potion") + " x3",
                  I18n.key("online.inventory.close"));
      case 1 ->
          lines =
              List.of(
                  I18n.key("online.ability.spells_title"),
                  I18n.message("online.ability.spell_row", 1, "Light Heal", "2"),
                  I18n.message("online.ability.spell_row", 2, "Stone Shard", "2"),
                  I18n.key("online.ability.spells_close"));
      case 2 -> {
        lines =
            List.of(
                I18n.message("online.shop.title_buy", I18n.resolve("${npc.sigfried}")),
                I18n.message("online.shop.gold", 212),
                I18n.message("online.shop.buy_row", 1, "Rusted Short Sword", 9),
                I18n.message("online.shop.buy_row", 2, "Rusted Dagger", 6),
                I18n.message("online.shop.buy_row", 3, "Rusted Hand Axe", 787),
                I18n.message("online.shop.quantity", 1),
                I18n.key("online.shop.hint_buy"));
      }
      default -> {
        var talk = new OnlineTalkPanel();
        talk.open("WardenAelric", I18n.resolve("${npc.wardenaelric}"));
        lines = talk.lines();
      }
    }
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.16f, 0.2f, 0.12f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    if (frame == 0) show(0);
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    window.render(batch);
    batch.end();
    frame++;
    if (frame < 20 || frame % 10 != 0) return;
    String[] names = {"online-list-inventory.png", "online-list-spells.png", "online-list-shop.png", "online-list-talk.png"};
    save(names[stage]);
    stage++;
    if (stage >= names.length) {
      Gdx.app.exit();
      return;
    }
    show(stage);
  }

  private void save(String name) {
    int w = Gdx.graphics.getBackBufferWidth();
    int h = Gdx.graphics.getBackBufferHeight();
    byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
    ByteBuffer target = pixmap.getPixels();
    BufferUtils.copy(pixels, 0, target, pixels.length);
    PixmapIO.writePNG(
        new com.badlogic.gdx.files.FileHandle(outputDir.resolve(name).toFile()), pixmap);
    pixmap.dispose();
    System.out.println("saved " + outputDir.resolve(name));
  }

  @Override
  public void dispose() {
    if (window != null) window.dispose();
    if (batch != null) batch.dispose();
  }
}
