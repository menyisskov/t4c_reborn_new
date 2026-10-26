package com.perso.T4C.online;

import static com.perso.T4C.config.GameConstants.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.AppearanceDefaultsCatalog;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.ModifSprites;
import com.perso.T4C.helper.SpriteLoader;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.player.PlayerAnimations;
import com.perso.T4C.player.PlayerMovement;
import com.perso.T4C.render.ObjectRenderer;
import com.perso.T4C.screens.MapRenderer;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Native online entry point sharing the offline renderer and sprites, with no local player model.
 */
public final class OnlineClient extends ApplicationAdapter {
  private final URI endpoint;
  private volatile boolean focused = true;
  private OnlineConnection connection;
  private MapReader map;
  private MapRenderer renderer;
  private SpriteBatch ground, decor, hud;
  private BitmapFont font;
  private final OrthographicCamera camera = new OrthographicCamera();
  private final OrthographicCamera hudCamera = new OrthographicCamera();
  private final Map<Long, Avatar> avatars = new HashMap<>();
  private long lastTick = -1;
  private String error;

  private static final class Avatar {
    final Vector2 position = new Vector2();
    final PlayerAnimations animation;
    final PlayerMovement facing = new PlayerMovement();
    boolean moving;

    Avatar(long id) {
      var parts = new ArrayList<Object>();
      AppearanceDefaultsCatalog.nakedParts(
              id % 2 == 0 ? AppearanceDefaultsCatalog.FEMALE : AppearanceDefaultsCatalog.MALE)
          .forEach(
              (part, sprite) -> {
                parts.add(part);
                parts.add(sprite);
              });
      animation = new PlayerAnimations(parts.toArray());
    }

    void position(float x, float y, boolean first) {
      moving = !first && (x != position.x || y != position.y);
      if (moving) facing.faceToward(position.x, position.y, x, y);
      position.set(x, y);
    }
  }

  public OnlineClient(URI endpoint) {
    this.endpoint = endpoint;
  }

  public static void main(String[] args) {
    URI endpoint = URI.create(args.length == 0 ? "http://127.0.0.1:18765" : args[0]);
    var client = new OnlineClient(endpoint);
    var config = new Lwjgl3ApplicationConfiguration();
    config.setTitle(I18n.key("online.client.title"));
    config.setWindowedMode(960, 640);
    config.setResizable(true);
    config.useVsync(true);
    config.disableAudio(true);
    config.setWindowListener(
        new Lwjgl3WindowAdapter() {
          @Override
          public void focusLost() {
            client.focused = false;
            if (client.connection != null) client.connection.direction(0, 0);
          }

          @Override
          public void focusGained() {
            client.focused = true;
          }
        });
    new Lwjgl3Application(client, config);
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
      camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      camera.position.set(
          NEW_CHARACTER_START_TILE_X * GRID_W, NEW_CHARACTER_START_TILE_Y * GRID_H, 0);
      camera.update();
      reconnect();
    } catch (Exception failure) {
      error = I18n.key("online.client.load_error");
      Gdx.app.error("online", "Could not load online client", failure);
    }
  }

  private void reconnect() {
    if (connection != null) connection.close();
    avatars.values().forEach(a -> a.animation.dispose());
    avatars.clear();
    lastTick = -1;
    connection = new OnlineConnection(endpoint);
  }

  private int key(int positive, int alternatePositive, int negative, int alternateNegative) {
    return (Gdx.input.isKeyPressed(positive) || Gdx.input.isKeyPressed(alternatePositive) ? 1 : 0)
        - (Gdx.input.isKeyPressed(negative) || Gdx.input.isKeyPressed(alternateNegative) ? 1 : 0);
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
    if (connection != null
        && Gdx.input.isKeyJustPressed(Input.Keys.R)
        && !"connected".equals(connection.status())) reconnect();
    var view = connection == null ? null : connection.view();
    if (connection != null)
      connection.direction(
          focused ? key(Input.Keys.RIGHT, Input.Keys.D, Input.Keys.LEFT, Input.Keys.A) : 0,
          focused ? key(Input.Keys.DOWN, Input.Keys.S, Input.Keys.UP, Input.Keys.W) : 0);
    if (view != null && view.snapshot().tick() != lastTick) {
      lastTick = view.snapshot().tick();
      var present = new HashSet<Long>();
      for (var actor : view.snapshot().players()) {
        present.add(actor.id());
        boolean first = !avatars.containsKey(actor.id());
        var avatar = avatars.computeIfAbsent(actor.id(), Avatar::new);
        avatar.position(
            (float) (view.map().originX() + actor.x()) * GRID_W,
            (float) (view.map().originY() + actor.y()) * GRID_H,
            first);
      }
      avatars
          .entrySet()
          .removeIf(
              e -> {
                if (present.contains(e.getKey())) return false;
                e.getValue().animation.dispose();
                return true;
              });
      var own = avatars.get(view.ownId());
      if (own != null) camera.position.set(own.position.x, own.position.y, 0);
    }
    if (view == null) avatars.values().forEach(a -> a.moving = false);
    camera.update();
    if (renderer != null) {
      int x = (int) (camera.position.x / GRID_W), y = (int) (camera.position.y / GRID_H);
      int rx = (int) camera.viewportWidth / GRID_W / 2 + 18,
          ry = (int) camera.viewportHeight / GRID_H / 2 + 18;
      int x0 = Math.max(0, x - rx), x1 = Math.min(map.getWidth() - 1, x + rx);
      int y0 = Math.max(0, y - ry), y1 = Math.min(map.getHeight() - 1, y + ry);
      renderer.warmCaches(x0, x1, y0, y1, 32, 2048, 512);
      renderer.renderGroundOnly(camera, x0, x1, y0, y1);
      var actors = new ArrayList<ObjectRenderer.RenderItem>();
      for (var avatar : avatars.values()) {
        avatar.animation.update(Math.min(Gdx.graphics.getDeltaTime(), 0.1f), avatar.moving);
        var item = new ObjectRenderer.RenderItem();
        item.y = (float) Math.floor(avatar.position.y / GRID_H) + 2f;
        item.renderAction =
            () ->
                avatar.animation.render(
                    decor,
                    avatar.position,
                    avatar.facing.getCurrentAngle(),
                    avatar.facing.isFlipX(),
                    avatar.moving);
        actors.add(item);
      }
      renderer.renderEntitiesWithDecors(camera, -1, -1, x0, x1, y0, y1, actors);
    }
    hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    hud.setProjectionMatrix(hudCamera.combined);
    hud.begin();
    font.setColor(Color.WHITE);
    String state =
        error != null
            ? error
            : I18n.key(
                "online.client." + (connection == null ? "connecting" : connection.status()));
    font.draw(hud, state, 16, Gdx.graphics.getHeight() - 16);
    font.draw(hud, I18n.key("online.client.controls"), 16, 24);
    if (view != null)
      font.draw(
          hud,
          I18n.message("online.client.players", view.ownId(), avatars.size()),
          16,
          Gdx.graphics.getHeight() - 38);
    hud.end();
  }

  @Override
  public void resize(int width, int height) {
    camera.viewportWidth = Math.max(1, width);
    camera.viewportHeight = Math.max(1, height);
    camera.update();
  }

  @Override
  public void dispose() {
    if (connection != null) connection.close();
    avatars.values().forEach(a -> a.animation.dispose());
    if (renderer != null) renderer.dispose();
    if (map != null)
      try {
        map.close();
      } catch (Exception ignored) {
      }
    if (font != null) font.dispose();
    if (ground != null) ground.dispose();
    if (decor != null) decor.dispose();
    if (hud != null) hud.dispose();
  }
}
