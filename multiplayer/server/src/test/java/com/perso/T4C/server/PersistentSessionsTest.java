package com.perso.T4C.server;

import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.CharacterClass;
import com.perso.T4C.server.persistence.CharacterProgress;
import com.perso.T4C.server.persistence.OnlineStore;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Consequential join between the tick-owned state and the real PostgreSQL transaction queue. */
@EnabledIfEnvironmentVariable(named="T4C_DB_URL", matches=".+")
class PersistentSessionsTest {
  @Test void gameplayWritesDoNotPostponeTheMovementCheckpointDeadline() throws Exception {
    var store=OnlineStore.fromEnvironment(); store.migrate(); var key=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(3));
    var created=store.create(key.accountId(),"Checkpoint deadline",0,3.5,3.5,prepared.progress(),prepared.gold());
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var world=new PreviewWorld(map); var sessions=new PersistentSessions(store,map,world);
    long started=System.nanoTime(); var lease=store.acquire(key.secret(),created.id());
    String token=sessions.join(lease,started).token();
    try {
      for (int i=0;i<PreviewWorld.TICK_RATE*5-1;i++) sessions.step();
      var result=sessions.commitGameplayEvent(token,created.revision(),UUID.randomUUID(),1,0,prepared.progress()).toCompletableFuture();
      world.input(token,0,1,0); world.step();
      double movedX=world.position(token).x();
      // Reach the original deadline while the reward is in flight, then drain at the same tick
      // cadence. Resetting the deadline for the reward would strand this movement until much later.
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
      for (int i=0;i<30 && sessions.character(token).x()!=movedX && System.nanoTime()<deadline;i++) {
        sessions.step(); Thread.sleep(100);
      }
      assertEquals(movedX,sessions.character(token).x());
      assertTrue(result.get(1,TimeUnit.SECONDS).applied());
      assertEquals(created.gold()+1,sessions.character(token).gold());
    } finally { sessions.shutdown(); }
  }

  @Test void gameplayWaitsForCheckpointAndLogoutWaitsForGameplayBeforeSavingMovement() throws Exception {
    var pause=new AtomicBoolean();
    var entered=new CountDownLatch(1);
    var release=new CountDownLatch(1);
    var store=new OnlineStore(() -> {
      if (pause.get()) {
        entered.countDown();
        try { if (!release.await(5,TimeUnit.SECONDS)) throw new SQLException("Test gate timeout"); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new SQLException(e); }
      }
      return connection();
    });
    store.migrate(); var key=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(1));
    var created=store.create(key.accountId(),"Serialized gameplay",0,103.5,203.5,prepared.progress(),prepared.gold());
    var map=new PreviewMap(new boolean[8][8],100,200,3,3);
    var world=new PreviewWorld(map); var sessions=new PersistentSessions(store,map,world);
    long started=System.nanoTime(); var lease=store.acquire(key.secret(),created.id());
    String token=sessions.join(lease,started).token();
    var progress=questProgress(prepared.progress());
    try {
      pause.set(true);
      for (int i=0;i<PreviewWorld.TICK_RATE*5;i++) sessions.step();
      assertTrue(entered.await(3,TimeUnit.SECONDS));
      var result=sessions.commitGameplayEvent(token,created.revision(),UUID.randomUUID(),7,11,progress).toCompletableFuture();
      assertFalse(result.isDone());
      assertEquals(created,sessions.character(token),"Uncommitted progress must not be published");
      var busy=assertThrows(PreviewWorld.Rejected.class,() ->
          sessions.commitGameplayEvent(token,created.revision(),UUID.randomUUID(),1,1,progress));
      assertEquals("busy",busy.getMessage());
      world.input(token,0,1,0); world.step(); sessions.step();
      double movedX=map.originX+world.position(token).x();
      assertTrue(movedX>created.x(),"Database stalls must not stop the actor");
      var closed=sessions.leave(token);
      assertFalse(closed.isDone()); assertFalse(result.isDone());
      pause.set(false); release.countDown();
      await(sessions,closed); closed.get();
      assertTrue(result.get().applied());
      var restored=store.load(key.accountId(),created.id());
      assertEquals(progress,restored.progress());
      assertEquals(created.gold()+7,restored.gold()); assertEquals(11,restored.experience());
      assertEquals(movedX,restored.x());
      var replacement=store.acquire(key.secret(),created.id()); // Final save released the lease.
      store.release(replacement);
      assertThrows(PreviewWorld.Rejected.class,() -> sessions.character(token));
    } finally { pause.set(false); release.countDown(); sessions.shutdown(); }
  }

  @Test void lostCommitAcknowledgementDisconnectsAndRecoveryRetriesTheSameEventOnce() throws Exception {
    var loseAck=new AtomicBoolean();
    var store=new OnlineStore(() -> {
      var actual=connection();
      return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},
          (proxy,method,args) -> {
            try {
              Object value=method.invoke(actual,args);
              if (method.getName().equals("commit") && loseAck.compareAndSet(true,false))
                throw new SQLException("Injected lost commit acknowledgement");
              return value;
            } catch (InvocationTargetException e) { throw e.getCause(); }
          });
    });
    store.migrate(); var key=store.provisionAccount();
    var prepared=OnlineCharacterCreator.prepare(CharacterClass.WARRIOR,"male",false,new Random(2));
    var created=store.create(key.accountId(),"Uncertain gameplay",0,3.5,3.5,prepared.progress(),prepared.gold());
    var map=new PreviewMap(new boolean[8][8],0,0,3,3);
    var world=new PreviewWorld(map); var sessions=new PersistentSessions(store,map,world);
    long started=System.nanoTime(); var lease=store.acquire(key.secret(),created.id());
    String token=sessions.join(lease,started).token(); UUID event=UUID.randomUUID();
    var progress=questProgress(prepared.progress());
    try {
      var stale=assertThrows(PreviewWorld.Rejected.class,() ->
          sessions.commitGameplayEvent(token,created.revision()+1,event,7,11,progress));
      assertEquals("revision",stale.getMessage());
      loseAck.set(true);
      var result=sessions.commitGameplayEvent(token,created.revision(),event,7,11,progress).toCompletableFuture();
      await(sessions,result);
      assertThrows(ExecutionException.class,result::get);
      assertNull(world.position(token));
      assertThrows(PreviewWorld.Rejected.class,() -> sessions.character(token));
      var recovered=store.load(key.accountId(),created.id());
      assertEquals(progress,recovered.progress()); assertEquals(created.gold()+7,recovered.gold());
      // Advance only this failed writer's lease; real lease timing is covered separately.
      try (var c=connection(); var s=c.prepareStatement(
          "UPDATE online_characters SET session_until=clock_timestamp()-interval '1 second' WHERE id=?")) {
        s.setObject(1,created.id()); s.executeUpdate();
      }
      started=System.nanoTime(); var replacement=store.acquire(key.secret(),created.id());
      String rejoined=sessions.join(replacement,started).token();
      var retry=sessions.commitGameplayEvent(rejoined,recovered.revision(),event,7,11,progress).toCompletableFuture();
      await(sessions,retry);
      assertFalse(retry.get().applied());
      assertEquals(recovered,sessions.character(rejoined));
    } finally { sessions.shutdown(); }
  }

  private static void await(PersistentSessions sessions,CompletableFuture<?> result) throws Exception {
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(8);
    while (!result.isDone() && System.nanoTime()<deadline) { sessions.step(); Thread.sleep(5); }
    assertTrue(result.isDone(),"Persistence completion timed out");
  }

  private static CharacterProgress questProgress(CharacterProgress p) {
    return new CharacterProgress(p.version(),p.characterClass(),p.gender(),p.attributes(),p.vitals(),p.advancement(),
        p.inventory(),p.equipment(),p.skills(),p.spells(),Map.of("test.quest",1),p.storage(),p.storageGold(),
        p.respawn(),p.buffs(),p.cooldowns(),p.hiddenUntil(),p.companion());
  }

  private static Connection connection() throws SQLException {
    return DriverManager.getConnection(System.getenv("T4C_DB_URL"),System.getenv("T4C_DB_USER"),System.getenv("T4C_DB_PASSWORD"));
  }
}
