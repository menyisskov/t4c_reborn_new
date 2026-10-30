package com.perso.T4C.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterManager;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.player.Player;
import com.perso.T4C.session.RemoteGameSession;
import com.perso.T4C.session.RemoteLink;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MainGameScreenRemoteAutoAttackTest {
  @Test
  void remoteHostileSpellAllowsOnlyOneCastAtATimeAndWaitsForCooldownReply() throws Exception {
    AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> pending =
        new AtomicReference<>();
    AtomicInteger casts = new AtomicInteger();
    AtomicReference<OnlineConnection.View> view = new AtomicReference<>(view(List.of(
        new CreatureView(21, "Training 1 Skeleton", 1, 0, 20, 20, "idle", null, null))));
    RemoteLink link = spellLink(pending, casts, view);
    MainGameScreen screen = screen(link);
    Player player = player();
    set(screen, "player", player);
    set(screen, "currentAttackTarget", installTarget(screen));
    set(screen, "currentAttackSpell", hostileSpell());

    tick(screen);
    tick(screen);
    assertEquals(1, casts.get(), "a pending cast must not overlap another cast");
    assertEquals(20, ((BaseMonster) get(screen, "currentAttackTarget")).getHealth(),
        "online spell intents never apply local damage");

    long serverCooldownUntil = System.currentTimeMillis() + 60_000L;
    pending.get().complete(cooldownReply(serverCooldownUntil));
    ((RemoteGameSession) get(screen, "session")).commands().pump();
    long retryAt = (long) get(screen, "remoteAutoSpellCooldownUntilMs");
    assertEquals(serverCooldownUntil, retryAt, "the server's committed cooldown controls the retry");
    tick(screen);
    assertEquals(1, casts.get());
    set(screen, "remoteAutoSpellCooldownUntilMs", 0L);
    tick(screen);
    assertEquals(2, casts.get(), "the selected hostile spell retries after its cooldown wait");
  }

  @Test
  void remoteHostileSpellStopsOnMovementDeadTargetAndTerminalRefusal() throws Exception {
    AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> pending =
        new AtomicReference<>();
    AtomicInteger casts = new AtomicInteger();
    AtomicReference<OnlineConnection.View> view = new AtomicReference<>(view(List.of(
        new CreatureView(21, "Training 1 Skeleton", 1, 0, 20, 20, "idle", null, null))));
    RemoteLink link = spellLink(pending, casts, view);
    MainGameScreen screen = screen(link);
    Player player = player();
    set(screen, "player", player);
    BaseMonster monster = installTarget(screen);
    set(screen, "currentAttackTarget", monster);
    set(screen, "currentAttackSpell", hostileSpell());

    tick(screen);
    player.getMovement().puppetMoving(true);
    tick(screen);
    player.getMovement().stop();
    pending.get().complete(new OnlineConnection.CommandResult(true, null));
    ((RemoteGameSession) get(screen, "session")).commands().pump();
    tick(screen);
    assertEquals(1, casts.get(), "movement cancels repeats even if the outstanding cast later succeeds");

    set(screen, "currentAttackTarget", monster);
    set(screen, "currentAttackSpell", hostileSpell());
    Field dead = BaseMonster.class.getDeclaredField("isDead");
    dead.setAccessible(true);
    dead.setBoolean(monster, true);
    tick(screen);
    assertEquals(1, casts.get(), "a dead target is not recast");

    monster = installTarget(screen);
    set(screen, "currentAttackTarget", monster);
    set(screen, "currentAttackSpell", hostileSpell());
    tick(screen);
    pending.get().complete(new OnlineConnection.CommandResult(false, null, "online.ability.busy"));
    ((RemoteGameSession) get(screen, "session")).commands().pump();
    assertEquals(3, casts.get(), "the command layer makes one bounded retry after busy");
    pending.get().complete(new OnlineConnection.CommandResult(false, null, "online.ability.busy"));
    ((RemoteGameSession) get(screen, "session")).commands().pump();
    assertTrue(get(screen, "currentAttackSpell") != null, "transient busy replies retain auto-repeat");
    assertTrue((long) get(screen, "remoteAutoSpellCooldownUntilMs") > System.currentTimeMillis());

    set(screen, "remoteAutoSpellCooldownUntilMs", 0L);
    tick(screen);
    assertEquals(4, casts.get(), "the spell retries after the short busy delay");
    pending.get().complete(new OnlineConnection.CommandResult(false, null, "online.cast.wrong_target"));
    ((RemoteGameSession) get(screen, "session")).commands().pump();
    assertNull(get(screen, "currentAttackSpell"), "terminal server refusals stop auto-repeat");
  }

  @Test
  void repeatsValidTargetIntentsOnlyAfterPriorReplyAndLeavesLocalCombatStateUntouched()
      throws Exception {
    AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> pending =
        new AtomicReference<>();
    AtomicInteger attacks = new AtomicInteger();
    AtomicReference<OnlineConnection.View> view = new AtomicReference<>(view(List.of(
        new CreatureView(21, "Training 1 Skeleton", 1, 0, 20, 20, "idle", null, null))));
    RemoteLink link = link(pending, attacks, view);
    MainGameScreen screen = screen(link);
    Player player = player();
    set(screen, "player", player);
    BaseMonster monster = installTarget(screen);
    player.setCombatMode(true);
    int hp = player.getCurrentHp();
    assertTrue(player.isAttackReady());

    tick(screen);
    assertEquals(1, attacks.get());
    tick(screen);
    assertEquals(1, attacks.get(), "a pending request must not be duplicated");
    pending.get().complete(new OnlineConnection.CommandResult(true, null));
    assertTrue((long) get(screen, "nextRemoteWeaponAttackAtMs") - System.currentTimeMillis() >= 900,
        "an accepted attack waits for the equipped weapon interval");
    set(screen, "nextRemoteWeaponAttackAtMs", 0L);
    tick(screen);

    assertEquals(2, attacks.get());
    assertEquals(hp, player.getCurrentHp());
    assertTrue(player.isAttackReady());
    assertFalse(monster.isDead());
    assertEquals(20, monster.getHealth());
  }

  @Test
  void stopsWhenCombatGateOrSnapshotTargetIsInvalid() throws Exception {
    AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> pending =
        new AtomicReference<>();
    AtomicInteger attacks = new AtomicInteger();
    AtomicReference<OnlineConnection.View> currentView = new AtomicReference<>(view(List.of(
        new CreatureView(21, "Training 1 Skeleton", 1, 0, 20, 20, "idle", null, null))));
    RemoteLink link = link(pending, attacks, currentView);
    MainGameScreen screen = screen(link);
    Player player = player();
    set(screen, "player", player);
    installTarget(screen);

    tick(screen);
    assertEquals(0, attacks.get(), "combat mode is required");
    player.setCombatMode(true);
    player.stunFor(10_000);
    tick(screen);
    assertEquals(0, attacks.get(), "stun stops auto attack");
    player.stunFor(0);
    currentView.set(view(List.of(), 2L));
    ((com.perso.T4C.session.RemoteGameSession) get(screen, "session")).remoteWorld()
        .tickEntities(new com.perso.T4C.session.EntityTick(null, null, player, player.getPositionVector(),
            0, 0, 1, 0, 1, 0));
    tick(screen);
    assertEquals(0, attacks.get(), "a target missing from the latest snapshot is not attackable");
  }

  @Test
  void cooldownReplyPacesNextIntentAtWeaponTiming() throws Exception {
    AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> pending =
        new AtomicReference<>();
    AtomicInteger attacks = new AtomicInteger();
    AtomicReference<OnlineConnection.View> currentView = new AtomicReference<>(view(List.of(
        new CreatureView(21, "Training 1 Skeleton", 1, 0, 20, 20, "idle", null, null))));
    RemoteLink link = link(pending, attacks, currentView);
    MainGameScreen screen = screen(link);
    Player player = player();
    set(screen, "player", player);
    installTarget(screen);
    player.setCombatMode(true);

    tick(screen);
    pending.get().complete(new OnlineConnection.CommandResult(false, null, "online.attack.cooldown", List.of(), null));
    long due = (long) get(screen, "nextRemoteWeaponAttackAtMs");
    assertTrue(due > System.currentTimeMillis());
    tick(screen);
    assertEquals(1, attacks.get());
  }

  private static RemoteLink link(
      AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> pending,
      AtomicInteger attacks,
      AtomicReference<OnlineConnection.View> view) {
    return (RemoteLink)
        java.lang.reflect.Proxy.newProxyInstance(
            RemoteLink.class.getClassLoader(),
            new Class<?>[] {RemoteLink.class},
            (proxy, method, args) -> {
              if (method.getName().equals("view")) return view.get();
              if (method.getName().equals("attack")) {
                attacks.incrementAndGet();
                var future = new CompletableFuture<OnlineConnection.CommandResult>();
                pending.set(future);
                return future;
              }
              if (method.getReturnType() == CompletableFuture.class
                  || java.util.concurrent.CompletionStage.class.isAssignableFrom(method.getReturnType()))
                return CompletableFuture.completedFuture(new OnlineConnection.CommandResult(true, null));
              return null;
            });
  }

  private static RemoteLink spellLink(
      AtomicReference<CompletableFuture<OnlineConnection.CommandResult>> pending,
      AtomicInteger casts,
      AtomicReference<OnlineConnection.View> view) {
    return (RemoteLink)
        java.lang.reflect.Proxy.newProxyInstance(
            RemoteLink.class.getClassLoader(),
            new Class<?>[] {RemoteLink.class},
            (proxy, method, args) -> {
              if (method.getName().equals("view")) return view.get();
              if (method.getName().equals("cast")) {
                casts.incrementAndGet();
                var future = new CompletableFuture<OnlineConnection.CommandResult>();
                pending.set(future);
                return future;
              }
              if (method.getReturnType() == CompletableFuture.class
                  || java.util.concurrent.CompletionStage.class.isAssignableFrom(method.getReturnType()))
                return CompletableFuture.completedFuture(new OnlineConnection.CommandResult(true, null));
              return null;
            });
  }

  private static com.perso.T4C.spell.SpellData hostileSpell() {
    return new com.perso.T4C.spell.SpellData(
        "spell.fireball", "", "10", 1, 2, 0, 0, true, false, null, null, null,
        0, 0, null, null, 0, null, null, 0, null, 1, 0, 1, 0,
        null, null, null, null, 0, 0, false, List.of());
  }

  private static OnlineConnection.CommandResult cooldownReply(long until) {
    var progress =
        new com.perso.T4C.online.model.CharacterProgress(
            1,
            "warrior",
            "male",
            new com.perso.T4C.online.model.CharacterProgress.Attributes(10, 10, 10, 10, 10),
            new com.perso.T4C.online.model.CharacterProgress.Vitals(10, 20, 5, 10),
            new com.perso.T4C.online.model.CharacterProgress.Advancement(1, 0, 0, 0, 0),
            List.of(),
            java.util.Map.of(),
            java.util.Map.of(),
            java.util.Set.of("spell.fireball"),
            java.util.Map.of(),
            List.of(),
            0,
            new com.perso.T4C.online.model.CharacterProgress.Respawn(0, 1, 1),
            List.of(),
            java.util.Map.of("spell.fireball", until),
            0,
            null);
    var character =
        new com.perso.T4C.online.model.OnlineCharacterView(
            java.util.UUID.randomUUID(), "Tester", 0, 0, 1, progress);
    return new OnlineConnection.CommandResult(false, character, "spell_cooldown");
  }

  private static OnlineConnection.View view(List<CreatureView> creatures) {
    return view(creatures, 1L);
  }

  private static OnlineConnection.View view(List<CreatureView> creatures, long tick) {
    var map = new OnlineConnection.MapInfo(0, 0, 20, 20, true, 0, 0, 0, "test", 1L);
    var snapshot = new OnlineConnection.Snapshot(
        tick, List.of(new OnlineConnection.Actor(7, 1, 1, 0, 0, 100, 100)), 1L,
        creatures, map.region(), List.of(), List.of());
    return new OnlineConnection.View(7, map, snapshot, null, List.of());
  }

  private static MainGameScreen screen(RemoteLink link) throws Exception {
    MainGameScreen screen = newScreenWithoutLibGdx();
    set(screen, "session", new RemoteGameSession(link, null));
    set(screen, "remoteConnection", null);
    set(screen, "textInputActiveSupplier", (java.util.function.BooleanSupplier) () -> false);
    return screen;
  }

  private static Player player() {
    Player player = new Player();
    player.setMaxHp(100);
    player.setCurrentHp(100);
    player.setWorldPosition(0, 0, 0);
    return player;
  }

  private static BaseMonster installTarget(MainGameScreen screen) throws Exception {
    RemoteGameSession session = (RemoteGameSession) get(screen, "session");
    MonsterManager monsters =
        session.remoteWorld()
            .loadEntities(new com.perso.T4C.session.WorldEntityInputs(null, null, null, null, null))
            .monsters();
    set(screen, "monsterManager", monsters);
    return monsters.getMonsters().get(0);
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
    return (MainGameScreen) ((sun.misc.Unsafe) field.get(null)).allocateInstance(MainGameScreen.class);
  }
}
