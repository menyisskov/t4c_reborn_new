package com.perso.T4C;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perso.T4C.config.MapDefinition;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.helper.GameCursorManager;
import com.perso.T4C.helper.MapReader;
import com.perso.T4C.helper.PlayerStateStore;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.OnlineAccountClient;
import com.perso.T4C.screens.OnlineLobbyScreen;
import com.perso.T4C.screens.LoadingScreen;
import com.perso.T4C.screens.MainGameScreen;
import com.perso.T4C.ui.FontManager;
import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyGame extends Game {
  private static final Logger log = LoggerFactory.getLogger(MyGame.class);
  public SpriteBatch batch;
  public AssetManager assetManager;
  public BitmapFont customFont;
  public Cursor customCursor;
  public GameCursorManager cursorManager;
  private final Map<MapDefinition, MapReader> preloadedMaps = new ConcurrentHashMap<>();
  private ExecutorService mapPreloadExecutor;
  private final URI onlineEndpoint;
  private final String onlineAccountKey;
  private final com.perso.T4C.online.OnlineLobbyModel onlineLobbyModel;
  private OnlineConnection onlineConnection;

  public MyGame() {
    this(null,null,null);
  }

  protected MyGame(URI onlineEndpoint,String onlineAccountKey,
      com.perso.T4C.online.OnlineLobbyModel onlineLobbyModel) {
    this.onlineEndpoint=onlineEndpoint;
    this.onlineAccountKey=onlineAccountKey;
    this.onlineLobbyModel=onlineLobbyModel;
  }

  /** Opens the native lobby inside the real game lifecycle so play can hand off to MainGameScreen. */
  public static void launchOnlineLobby(URI endpoint,String accountKey,
      com.perso.T4C.online.OnlineLobbyModel lobbyModel) {
    var config=new Lwjgl3ApplicationConfiguration();
    config.setTitle(com.perso.T4C.i18n.I18n.key("online.client.title"));
    var preferences=com.perso.T4C.config.GamePreferencesStore.get();
    if(preferences.isFullscreen()) config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
    else config.setWindowedMode(com.perso.T4C.config.GameConstants.WINDOW_WIDTH,
        com.perso.T4C.config.GameConstants.WINDOW_HEIGHT);
    config.setResizable(false);
    config.useVsync(preferences.isVSync());
    new Lwjgl3Application(new MyGame(endpoint,accountKey,lobbyModel),config);
  }

  public boolean onlineLobbyLaunch() { return onlineLobbyModel!=null; }

  public void showOnlineLobby() {
    if (onlineConnection!=null) { onlineConnection.close();onlineConnection=null; }
    var screen=OnlineLobbyScreen.forAccountClient(batch,onlineLobbyModel,
        new OnlineAccountClient(onlineEndpoint,onlineAccountKey),new OnlineLobbyScreen.Listener() {
          @Override public void play(OnlineConnection.Credentials chosen) {
            if (getScreen() instanceof OnlineLobbyScreen lobby) lobby.dispose();
            onlineConnection=new OnlineConnection(onlineEndpoint,chosen);
            setScreen(new OnlineHandoffScreen());
          }
          @Override public void back() { Gdx.app.exit(); }
        });
    setScreen(screen);
  }

  /** Returns a disconnected remote character to account selection so it can rejoin cleanly. */
  public void returnToOnlineLobby(MainGameScreen remoteScreen) {
    if (onlineLobbyModel==null || getScreen()!=remoteScreen || !remoteScreen.isRemoteSession()) return;
    // The server keeps the session lease for a while after the link ends; hold the rejoin.
    onlineLobbyModel.beginReturnFromGame();
    remoteScreen.dispose();
    onlineLobbyModel.finishReturnFromGame(
        com.perso.T4C.online.OnlineLobbyLaunch.leaseDeadline(System.currentTimeMillis()));
    showOnlineLobby();
  }

  public static void main(String[] args) {
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    com.perso.T4C.config.GamePreferences preferences =
        com.perso.T4C.config.GamePreferencesStore.get();
    config.setTitle("T4C Reborn");
    if (preferences.isFullscreen()) {
      config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
    } else {
      config.setWindowedMode(
          com.perso.T4C.config.GameConstants.WINDOW_WIDTH,
          com.perso.T4C.config.GameConstants.WINDOW_HEIGHT);
    }
    config.setResizable(false);
    config.useVsync(preferences.isVSync());
    config.setWindowIcon(
        Files.FileType.Absolute, Paths.get("App_icon.png").toAbsolutePath().toString());
    new Lwjgl3Application(new MyGame(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);
    com.perso.T4C.monster.json.MonsterJsonLoader.loadAndRegister();
    com.perso.T4C.item.json.ItemJsonLoader.loadAndRegister();
    batch = new SpriteBatch();
    assetManager = new AssetManager();
    cursorManager = new GameCursorManager();
    setScreen(new LoadingScreen(this));
  }

  public synchronized void startMapPreloadAsync() {
    if (mapPreloadExecutor != null) {
      return;
    }
    mapPreloadExecutor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread t = new Thread(r, "map-preloader");
              t.setDaemon(true);
              t.setPriority(Thread.MIN_PRIORITY);
              return t;
            });
    for (MapDefinition mapDefinition : MapDefinition.values()) {
      mapPreloadExecutor.submit(
          () -> {
            try {
              getOrLoadMapReader(mapDefinition);
            } catch (Exception e) {
              log.warn("Failed to preload map {}", mapDefinition.name(), e);
            }
          });
    }
    mapPreloadExecutor.shutdown();
  }

  public MapReader getOrLoadMapReader(MapDefinition mapDefinition) throws GameException {
    return preloadedMaps.computeIfAbsent(
        mapDefinition,
        def -> {
          try {
            String mapFile =
                com.badlogic.gdx.Gdx.files.internal(def.getMapPath()).file().getAbsolutePath();
            return new MapReader(new File(mapFile));
          } catch (Exception e) {
            throw new RuntimeException("Failed to load map " + def.name(), e);
          }
        });
  }

  @Override
  public void dispose() {
    try {
      if (getScreen() instanceof MainGameScreen mgs) {
        var p = mgs.getPlayer();
        if (p != null && !mgs.isRemoteSession()) {
          PlayerStateStore.save(p);
        }
      }
    } catch (Throwable t) {
      log.warn("Failed to persist player state on dispose", t);
    }
    if (batch != null) batch.dispose();
    if (onlineConnection!=null) onlineConnection.close();
    FontManager.getInstance().dispose();
    if (assetManager != null) assetManager.dispose();
    if (customFont != null) customFont.dispose();
    if (cursorManager != null) cursorManager.dispose(this);
    if (mapPreloadExecutor != null) {
      mapPreloadExecutor.shutdownNow();
    }
    for (MapReader mapReader : preloadedMaps.values()) {
      try {
        mapReader.close();
      } catch (Exception ignored) {
      }
    }
    preloadedMaps.clear();
  }

  private final class OnlineHandoffScreen implements com.badlogic.gdx.Screen {
    private final OrthographicCamera camera=new OrthographicCamera();
    OnlineHandoffScreen() { camera.setToOrtho(true,Gdx.graphics.getWidth(),Gdx.graphics.getHeight()); }
    @Override public void render(float delta) {
      Gdx.gl.glClearColor(0,0,0,1);Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);
      batch.setProjectionMatrix(camera.combined);batch.begin();
      String status=onlineConnection.status();
      String message = "connected".equals(status)
          ? com.perso.T4C.i18n.I18n.key("online.lobby.status.ready")
          : com.perso.T4C.i18n.I18n.key("online.client.connecting");
      customFont.draw(batch,message,32,Gdx.graphics.getHeight()-48);
      batch.end();
      if (com.perso.T4C.online.OnlineClient.persistentViewReady(onlineConnection.view())) {
        try { setScreen(new MainGameScreen(MyGame.this,onlineConnection)); }
        catch (GameException failure) { Gdx.app.error("online","Could not load the online game screen",failure);Gdx.app.exit(); }
      } else if ("disconnected".equals(status)) {
        Gdx.app.postRunnable(() -> {
          if (getScreen() instanceof OnlineHandoffScreen) showOnlineLobby();
        });
      }
    }
    @Override public void resize(int width,int height) { camera.setToOrtho(true,width,height); }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {}
  }
}
