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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.google.gson.Gson;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.NpcShopContent;
import com.perso.T4C.online.model.ShopOffersReply;
import com.perso.T4C.screens.MapRenderer;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Developer preview, not shipped: opens the real ground renderer at the online start tile (no network,
 * no server, no ports), builds Sigfried's real listing from the exported vendor stock, draws the talk
 * panel's shop line and the buy and sell lists with the panel's own text code, and saves PNG
 * screenshots. Run from the repository root with the test classpath:
 * {@code java -cp ... com.perso.T4C.online.OnlineShopPanelPreview <output-directory>}.
 */
public final class OnlineShopPanelPreview extends ApplicationAdapter {
  private static final int WIDTH = 960;
  private static final int HEIGHT = 640;

  private final Path outputDir;
  private SpriteBatch ground, decor, hud;
  private BitmapFont font;
  private MapReader map;
  private MapRenderer renderer;
  private final OrthographicCamera camera = new OrthographicCamera();
  private final OrthographicCamera hudCamera = new OrthographicCamera();
  private final OnlineShopPanel panel = new OnlineShopPanel();
  private CharacterProgress progress;
  private ShopOffersReply listing;
  private boolean talk;
  private String lastMessage;
  private int frame;
  private int stage;

  OnlineShopPanelPreview(Path outputDir) {
    this.outputDir = outputDir;
  }

  public static void main(String[] args) {
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Online shop panel preview");
    config.setWindowedMode(WIDTH, HEIGHT);
    config.setResizable(false);
    config.disableAudio(true);
    config.useVsync(true);
    new Lwjgl3Application(new OnlineShopPanelPreview(Path.of(args.length > 0 ? args[0] : ".")), config);
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
      var stock = new Gson().fromJson(
          Files.readString(Path.of("multiplayer/server/src/main/resources/content/npc-shops-v2.json")),
          NpcShopContent.class);
      var sigfried = stock.vendors().stream().filter(v -> v.npcId().equals("Sigfried")).findFirst().orElseThrow();
      var buy = sigfried.offers().stream()
          .map(o -> new ShopOffersReply.Line(o.itemKey(), o.buyPrice(), 0)).toList();
      listing = new ShopOffersReply(12, 1840, "Sigfried", buy, List.of(
          new ShopOffersReply.Line("item.healing_potion", 21, 3),
          new ShopOffersReply.Line("item.wooden_club", 4, 1),
          new ShopOffersReply.Line("item.torch", 2, 5)));
    } catch (Exception failure) {
      throw new IllegalStateException(failure);
    }
    camera.setToOrtho(true, WIDTH, HEIGHT);
    camera.position.set(NEW_CHARACTER_START_TILE_X * GRID_W, NEW_CHARACTER_START_TILE_Y * GRID_H, 0);
    camera.update();
    ItemJsonLoader.loadAndRegister();
    progress = new CharacterProgress(
        1, "warrior", "male", new CharacterProgress.Attributes(95, 85, 75, 75, 75),
        new CharacterProgress.Vitals(27, 40, 0, 0), new CharacterProgress.Advancement(3, 0, 0, 0, 0),
        List.of(new CharacterProgress.Item(UUID.randomUUID(), "item.healing_potion", 3, 1)), Map.of(), Map.of(),
        Set.of(), Map.of(), List.of(), 0, new CharacterProgress.Respawn(0, 1, 1), List.of(), Map.of(), 0, null);
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
    font.draw(hud, OnlineHudText.progressLine(progress, 0, panel.isOpen() ? panel.gold() : 1840), 16, HEIGHT - 60);
    font.draw(hud, I18n.key("online.client.combat_controls"), 16, 24);
    if (lastMessage != null) font.draw(hud, lastMessage, 16, 46);
    if (talk) drawTalk();
    if (panel.isOpen())
      OnlineAbilityMenu.draw(hud, font, panel.lines(key -> I18n.key(key, key)), WIDTH - 420, HEIGHT - 16);
    hud.end();
    frame++;
    if (frame < 30 || frame % 10 != 0) return;
    switch (stage) {
      case 0 -> { talk = true; stage = 1; }
      case 1 -> {
        save("online-shop-talk.png");
        talk = false;
        panel.open("Sigfried", I18n.key("npc.sigfried"), listing);
        stage = 2;
      }
      case 2 -> {
        save("online-shop-buy.png");
        panel.cycleQuantity();
        panel.toggleMode();
        stage = 3;
      }
      case 3 -> {
        save("online-shop-sell.png");
        panel.toggleMode();
        panel.refresh(new ShopOffersReply(13, 1822, "Sigfried", listing.buy(), listing.sell()));
        lastMessage = I18n.message("online.shop.bought", 2, 18);
        stage = 4;
      }
      case 4 -> {
        save("online-shop-after-purchase.png");
        Gdx.app.exit();
      }
      default -> {}
    }
  }

  /** Same layout and keys as the client's talk panel, with the shop line the client adds for a shopkeeper. */
  private void drawTalk() {
    float line = font.getLineHeight();
    float y = HEIGHT - 130;
    font.draw(hud, I18n.message("online.talk.header", I18n.key("npc.sigfried")), 16, y);
    y -= line * 1.5f;
    var layout = font.draw(hud, I18n.key("npc.welcome.sigfried"), 16, y, WIDTH - 32, Align.left, true);
    y -= layout.height + line;
    font.draw(hud, I18n.key("online.talk.shop"), 16, y);
    y -= line;
    font.draw(hud, I18n.key("online.talk.close"), 16, y);
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
