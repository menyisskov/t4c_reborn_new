package com.perso.T4C.online;

import static com.perso.T4C.config.GameConstants.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.screens.MapRenderer;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Developer preview, not shipped: opens the real ground renderer at the online start tile (no network,
 * no server, no ports), draws the real spell and potion lists with the client's own HUD text code on a
 * fake character, and saves PNG screenshots. Run from the repository root with the test classpath:
 * {@code java -cp ... com.perso.T4C.online.OnlineAbilityMenuPreview <output-directory>}.
 */
public final class OnlineAbilityMenuPreview extends ApplicationAdapter {
  private static final int WIDTH = 960;
  private static final int HEIGHT = 640;

  private final Path outputDir;
  private SpriteBatch ground, decor, hud;
  private BitmapFont font;
  private MapReader map;
  private MapRenderer renderer;
  private final OrthographicCamera camera = new OrthographicCamera();
  private final OrthographicCamera hudCamera = new OrthographicCamera();
  private final OnlineAbilityMenu menu = new OnlineAbilityMenu();
  private SpellContent allowlist;
  private CharacterProgress progress;
  private CharacterProgress warrior;
  private int frame;
  private int stage;

  OnlineAbilityMenuPreview(Path outputDir) {
    this.outputDir = outputDir;
  }

  public static void main(String[] args) {
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Online ability list preview");
    config.setWindowedMode(WIDTH, HEIGHT);
    config.setResizable(false);
    config.disableAudio(true);
    config.useVsync(true);
    new Lwjgl3Application(new OnlineAbilityMenuPreview(Path.of(args.length > 0 ? args[0] : ".")), config);
  }

  @Override
  public void create() {
    ground = new SpriteBatch();
    decor = new SpriteBatch();
    hud = new SpriteBatch();
    font = new BitmapFont();
    try {
      var sprites = SpriteLoader.getInstance();
      sprites.loadSpriteBin(Paths.SPRITE_BIN);
      map = new MapReader(new File(Paths.MAP));
      renderer = new MapRenderer(map, sprites, ground, decor, null, ModifSprites.empty(), 0);
    } catch (Exception failure) {
      throw new IllegalStateException(failure);
    }
    camera.setToOrtho(true, WIDTH, HEIGHT);
    camera.position.set(NEW_CHARACTER_START_TILE_X * GRID_W, NEW_CHARACTER_START_TILE_Y * GRID_H, 0);
    camera.update();
    ItemJsonLoader.loadAndRegister();
    allowlist = OnlineSpellAllowlist.bundled();
    if (allowlist == null) throw new IllegalStateException("client spell allowlist missing");
    var potion = ItemRegistry.allByKey().values().stream()
        .filter(i -> i.getBodyPart() == null && i.getNbCharges() == 1 && !i.isUnlimitedUse())
        .filter(i -> i.getSpells().stream().anyMatch(s -> allowlist.spells().values().stream()
            .anyMatch(a -> a.use() == SpellContent.Use.ITEM && a.id() == s.getSpellId())))
        .findFirst().orElseThrow();
    String potionKey = potion.getKey().startsWith("item.") ? potion.getKey() : "item." + potion.getKey();
    progress = new CharacterProgress(
        1, "warrior", "male", new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(27, 40, 8, 20), new CharacterProgress.Advancement(3, 0, 0, 0, 0),
        List.of(new CharacterProgress.Item(UUID.randomUUID(), potionKey, 3, 1)), Map.of(), Map.of(),
        Set.of("spell.heal_light"), Map.of(), List.of(), 0, new CharacterProgress.Respawn(0, 1, 1),
        List.of(), Map.of(), 0, null);
    warrior = new CharacterProgress(
        1, "warrior", "male", new CharacterProgress.Attributes(10, 10, 10, 10, 10),
        new CharacterProgress.Vitals(60, 60, 0, 0), new CharacterProgress.Advancement(3, 0, 0, 0, 0),
        List.of(), Map.of(), Map.of(), Set.of(), Map.of(), List.of(), 0, new CharacterProgress.Respawn(0, 1, 1),
        List.of(), Map.of(), 0, null);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    int x = (int) (camera.position.x / GRID_W), y = (int) (camera.position.y / GRID_H);
    int rx = WIDTH / GRID_W / 2 + 18, ry = HEIGHT / GRID_H / 2 + 18;
    int x0 = Math.max(0, x - rx), x1 = Math.min(map.getWidth() - 1, x + rx);
    int y0 = Math.max(0, y - ry), y1 = Math.min(map.getHeight() - 1, y + ry);
    renderer.warmCaches(x0, x1, y0, y1, 32, 2048, 512);
    renderer.renderGroundOnly(camera, x0, x1, y0, y1);
    hudCamera.setToOrtho(false, WIDTH, HEIGHT);
    hud.setProjectionMatrix(hudCamera.combined);
    hud.begin();
    font.setColor(Color.WHITE);
    font.draw(hud, I18n.key("online.client.persistent"), 16, HEIGHT - 16);
    font.draw(hud, I18n.message("online.client.players", 1, 1), 16, HEIGHT - 38);
    font.draw(hud, OnlineHudText.progressLine(stage >= 4 ? warrior : progress, 0, 200), 16, HEIGHT - 60);
    font.draw(hud, I18n.key("online.client.combat_controls"), 16, 24);
    if (stage == 2) font.draw(hud, I18n.message("online.ability.result", 9, -2), 16, 46);
    if (menu.isOpen()) OnlineAbilityMenu.draw(hud, font, menu.lines(progress, allowlist), WIDTH - 360, HEIGHT - 16);
    hud.end();
    frame++;
    if (frame < 30 || frame % 10 != 0) return;
    switch (stage) {
      case 0 -> {
        menu.toggle(OnlineAbilityMenu.Kind.SPELLS, true);
        stage = 10;
      }
      case 10 -> {
        save("online-ability-menu-spells.png");
        menu.toggle(OnlineAbilityMenu.Kind.POTIONS, true);
        stage = 11;
      }
      case 11 -> {
        save("online-ability-menu-potions.png");
        menu.close();
        stage = 2;
      }
      case 2 -> {
        save("online-ability-menu-result.png");
        stage = 3;
      }
      case 3 -> {
        save("online-hud-mana-mage.png");
        stage = 4;
      }
      case 4 -> {
        save("online-hud-mana-warrior.png");
        Gdx.app.exit();
      }
      default -> {}
    }
  }

  private void save(String name) {
    int w = Gdx.graphics.getBackBufferWidth();
    int h = Gdx.graphics.getBackBufferHeight();
    byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
    for (int i = 3; i < pixels.length; i += 4) pixels[i] = (byte) 255;
    Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
    ByteBuffer target = pixmap.getPixels();
    BufferUtils.copy(pixels, 0, target, pixels.length);
    PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(outputDir.resolve(name).toFile()), pixmap);
    pixmap.dispose();
    System.out.println("saved " + outputDir.resolve(name));
  }

  @Override
  public void dispose() {
    if (renderer != null) renderer.dispose();
    if (font != null) font.dispose();
    if (ground != null) ground.dispose();
    if (decor != null) decor.dispose();
    if (hud != null) hud.dispose();
  }
}
