package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.player.BodyPart;
import com.perso.T4C.player.Player;
import com.perso.T4C.render.SpellRenderer;
import com.perso.T4C.session.RemoteGameSession;
import com.perso.T4C.session.RemoteLink;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MainGameScreenRemoteBowPresentationTest {
  private Application previousApplication;

  @AfterEach
  void restoreApplication() {
    Gdx.app = previousApplication;
  }

  @Test
  void onlyAnAcceptedRemoteBowAttackQueuesAVisualAndItsImpactCannotDamageLocally()
      throws Exception {
    List<Runnable> renderQueue = new ArrayList<>();
    installRenderQueue(renderQueue);
    AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> pending =
        new AtomicReference<>();
    AtomicInteger attacks = new AtomicInteger();
    RemoteGameSession session = remoteSession(pending, attacks);
    MainGameScreen screen = screen(session);
    Player player = archer();
    set(screen, "player", player);
    BaseMonster target = installTarget(screen, session);
    set(screen, "currentAttackTarget", target);
    AtomicInteger projectiles = new AtomicInteger();
    AtomicReference<Runnable> impact = new AtomicReference<>();
    set(
        screen,
        "spellRenderer",
        new SpellRenderer(null) {
          @Override
          public boolean launchProjectile(
              String sprite,
              BaseMonster projectileTarget,
              float x,
              float y,
              boolean flipX,
              Runnable onImpact) {
            projectiles.incrementAndGet();
            impact.set(onImpact);
            return true;
          }
        });

    int playerHp = player.getCurrentHp();
    int targetHp = target.getHealth();
    tick(screen);
    assertEquals(1, attacks.get());
    assertTrue(renderQueue.isEmpty());

    pending.get().complete(new OnlineConnection.CommandResult(true, null));
    assertEquals(1, renderQueue.size(), "the accepted result schedules presentation on render thread");
    assertEquals(0, projectiles.get(), "the network completion thread only queues visual work");

    renderQueue.remove(0).run();
    assertEquals(1, projectiles.get());
    assertTrue(impact.get() != null);
    impact.get().run();
    assertEquals(targetHp, target.getHealth(), "the projectile callback is presentation-only");
    assertEquals(playerHp, player.getCurrentHp());
    assertTrue(player.isAttackReady(), "remote presentation does not start local cooldown state");
  }

  @Test
  void refusedBusyAndStaleGenerationResultsDoNotShowTheBowPresentation() throws Exception {
    List<Runnable> renderQueue = new ArrayList<>();
    installRenderQueue(renderQueue);
    List<CompletableFuture<OnlineConnection.CommandResult>> pending = new ArrayList<>();
    AtomicInteger attacks = new AtomicInteger();
    RemoteGameSession session = remoteSession(pending, attacks);
    MainGameScreen screen = screen(session);
    set(screen, "player", archer());
    BaseMonster target = installTarget(screen, session);
    set(screen, "currentAttackTarget", target);
    AtomicInteger projectiles = new AtomicInteger();
    set(
        screen,
        "spellRenderer",
        new SpellRenderer(null) {
          @Override
          public boolean launchProjectile(
              String sprite,
              BaseMonster projectileTarget,
              float x,
              float y,
              boolean flipX,
              Runnable onImpact) {
            projectiles.incrementAndGet();
            return true;
          }
        });

    tick(screen);
    pending.get(0).complete(new OnlineConnection.CommandResult(false, null, "online.attack.busy"));
    assertEquals(2, attacks.get(), "the command's one busy retry keeps its existing lifecycle");
    pending.get(1).complete(new OnlineConnection.CommandResult(false, null, "online.attack.busy"));
    assertTrue(renderQueue.isEmpty(), "a final busy refusal has no presentation");

    set(screen, "nextRemoteWeaponAttackAtMs", 0L);
    tick(screen);
    pending.get(2).complete(new OnlineConnection.CommandResult(true, null));
    long generation = (long) get(screen, "remoteWeaponAttackGeneration");
    set(screen, "remoteWeaponAttackGeneration", generation + 1);
    assertEquals(1, renderQueue.size());
    renderQueue.remove(0).run();
    assertEquals(0, projectiles.get(), "a queued result from an old target lifecycle is discarded");
  }

  @Test
  void clickedRemoteBowAttacksPresentOnlyAfterAcceptedResult() throws Exception {
    List<Runnable> renderQueue = new ArrayList<>();
    installRenderQueue(renderQueue);
    List<CompletableFuture<OnlineConnection.CommandResult>> pending = new ArrayList<>();
    AtomicInteger attacks = new AtomicInteger();
    RemoteGameSession session = remoteSession(pending, attacks);
    MainGameScreen screen = screen(session);
    set(screen, "player", archer());
    BaseMonster target = installTarget(screen, session);
    AtomicInteger projectiles = new AtomicInteger();
    set(
        screen,
        "spellRenderer",
        new SpellRenderer(null) {
          @Override
          public boolean launchProjectile(
              String sprite,
              BaseMonster projectileTarget,
              float x,
              float y,
              boolean flipX,
              Runnable onImpact) {
            projectiles.incrementAndGet();
            return true;
          }
        });
    Method clicked =
        MainGameScreen.class.getDeclaredMethod("handleRemoteAttackTargetSelected", BaseMonster.class);
    clicked.setAccessible(true);

    clicked.invoke(screen, target);
    assertEquals(1, attacks.get());
    pending.get(0).complete(new OnlineConnection.CommandResult(true, null));
    assertEquals(1, renderQueue.size(), "the clicked Archer's accepted result is rendered");
    renderQueue.remove(0).run();
    assertEquals(1, projectiles.get());

    clicked.invoke(screen, target);
    pending.get(1).complete(new OnlineConnection.CommandResult(false, null, "online.attack.busy"));
    assertEquals(3, attacks.get(), "the click keeps the command layer's bounded busy retry");
    pending.get(2).complete(new OnlineConnection.CommandResult(false, null, "online.attack.busy"));
    assertTrue(renderQueue.isEmpty(), "a refused clicked attack does not show a projectile");
    assertEquals(1, projectiles.get());
  }

  private void installRenderQueue(List<Runnable> renderQueue) {
    previousApplication = Gdx.app;
    Gdx.app =
        (Application)
            Proxy.newProxyInstance(
                Application.class.getClassLoader(),
                new Class<?>[] {Application.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("postRunnable")) {
                    renderQueue.add((Runnable) args[0]);
                    return null;
                  }
                  if (method.getReturnType() == boolean.class) return false;
                  if (method.getReturnType() == int.class) return 0;
                  if (method.getReturnType() == long.class) return 0L;
                  return null;
                });
  }

  private static RemoteGameSession remoteSession(
      AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> pending,
      AtomicInteger attacks)
      throws Exception {
    List<CompletableFuture<OnlineConnection.CommandResult>> futures = new ArrayList<>();
    return remoteSession(futures, attacks, pending);
  }

  private static RemoteGameSession remoteSession(
      List<CompletableFuture<OnlineConnection.CommandResult>> futures, AtomicInteger attacks)
      throws Exception {
    return remoteSession(futures, attacks, null);
  }

  private static RemoteGameSession remoteSession(
      List<CompletableFuture<OnlineConnection.CommandResult>> futures,
      AtomicInteger attacks,
      AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> latest)
      throws Exception {
    RemoteLink link =
        (RemoteLink)
            Proxy.newProxyInstance(
                RemoteLink.class.getClassLoader(),
                new Class<?>[] {RemoteLink.class},
                (proxy, method, args) -> {
                  if (method.getName().equals("view")) return view();
                  if (method.getName().equals("attack")) {
                    attacks.incrementAndGet();
                    var future = new CompletableFuture<OnlineConnection.CommandResult>();
                    futures.add(future);
                    if (latest != null) latest.set(future);
                    return future;
                  }
                  if (java.util.concurrent.CompletionStage.class.isAssignableFrom(
                      method.getReturnType())) {
                    return CompletableFuture.completedFuture(
                        new OnlineConnection.CommandResult(true, null));
                  }
                  return null;
                });
    return new RemoteGameSession(link, null);
  }

  private static OnlineConnection.View view() {
    var map = new OnlineConnection.MapInfo(0, 0, 20, 20, true, 0, 0, 0, "test", 1L);
    var snapshot =
        new OnlineConnection.Snapshot(
            1,
            List.of(new OnlineConnection.Actor(7, 1, 1, 0, 0, 100, 100)),
            1L,
            List.of(new CreatureView(21, "Training 1 Skeleton", 1, 0, 20, 20, "idle", null, null)),
            map.region(),
            List.of(),
            List.of());
    return new OnlineConnection.View(7, map, snapshot, null, List.of());
  }

  private static MainGameScreen screen(RemoteGameSession session) throws Exception {
    MainGameScreen screen = newScreenWithoutLibGdx();
    set(screen, "session", session);
    set(screen, "textInputActiveSupplier", (java.util.function.BooleanSupplier) () -> false);
    return screen;
  }

  private static Player archer() {
    Player player = new Player();
    player.setMaxHp(100);
    player.setCurrentHp(100);
    player.setWorldPosition(0, 0, 0);
    var bow =
        ItemRegistry.load().stream()
            .filter(item -> item.getBodyPart() == BodyPart.WEAPON && item.isBow())
            .findFirst()
            .orElseThrow();
    player.getEquippedItems().put(BodyPart.WEAPON, bow.getKey());
    player.getEquippedItems().put(BodyPart.WEAPON2, "item.wooden_arrow");
    player.setCombatMode(true);
    return player;
  }

  private static BaseMonster installTarget(MainGameScreen screen, RemoteGameSession session)
      throws Exception {
    MonsterManager manager =
        session
            .remoteWorld()
            .loadEntities(new com.perso.T4C.session.WorldEntityInputs(null, null, null, null, null))
            .monsters();
    set(screen, "monsterManager", manager);
    return manager.getMonsters().get(0);
  }

  private static void tick(MainGameScreen screen) throws Exception {
    Method method = MainGameScreen.class.getDeclaredMethod("performRemoteWeaponAttackTick");
    method.setAccessible(true);
    method.invoke(screen);
  }

  private static Object get(Object target, String name) throws Exception {
    Field field = MainGameScreen.class.getDeclaredField(name);
    field.setAccessible(true);
    return field.get(target);
  }

  private static void set(Object target, String name, Object value) throws Exception {
    Field field = MainGameScreen.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static MainGameScreen newScreenWithoutLibGdx() throws Exception {
    Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    return (MainGameScreen)
        ((sun.misc.Unsafe) field.get(null)).allocateInstance(MainGameScreen.class);
  }
}
