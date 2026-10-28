package com.perso.T4C.server;

import com.perso.T4C.online.model.*;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.helper.CharacterClass;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.LongSupplier;

/** Account reads cannot occupy simulation/gameplay persistence workers or unbounded peer memory. */
final class AccountRosterService implements AutoCloseable {
  static final class PeerLimiter {
    private record Budget(double credits,long at,long seen) {}
    private final Map<String,Budget> peers=new HashMap<>();
    private final LongSupplier clock;
    private static final int CAPACITY=1024;
    private static final long TTL=TimeUnit.MINUTES.toNanos(5);
    PeerLimiter(LongSupplier clock) { this.clock=clock; }
    synchronized void admit(String peer) {
      long now=clock.getAsLong();peers.entrySet().removeIf(e->now-e.getValue().seen()>TTL);
      var old=peers.get(peer);
      if(old==null && peers.size()>=CAPACITY) throw new PreviewWorld.Rejected(429,"account_rate");
      double credits=old==null ? 40 : Math.min(40,old.credits()+Math.max(0,now-old.at())/1_000_000_000.0*.5);
      peers.put(peer,new Budget(credits>=1 ? credits-1 : credits,now,now));
      if(credits<1) throw new PreviewWorld.Rejected(429,"account_rate");
    }
  }
  static final int CLOSE_TIMEOUT_SECONDS=12;
  private boolean closed;
  private final Set<CompletableFuture<?>> pending=ConcurrentHashMap.newKeySet();
  private final OnlineStore store;
  private final HostedRegionPlan plan;
  private final PeerLimiter limiter=new PeerLimiter(System::nanoTime);
  private final ThreadPoolExecutor workers=new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(20),
      Thread.ofPlatform().name("account-roster-",0).factory(),new ThreadPoolExecutor.AbortPolicy());
  AccountRosterService(OnlineStore store,HostedRegionPlan plan) { this.store=store;this.plan=plan; }
  private synchronized <T> CompletableFuture<T> submit(String peer,Callable<T> action) {
    if(closed) throw new PreviewWorld.Rejected(503,"account_closed");
    limiter.admit(peer);var result=new CompletableFuture<T>();
    pending.add(result);result.whenComplete((value,error)->pending.remove(result));
    try { workers.execute(()-> {
      if(result.isDone()) return;
      try { result.complete(action.call()); }
      catch(OnlineStore.CreationConflict rejected) { result.completeExceptionally(new PreviewWorld.Rejected(409,rejected.code())); }
      catch(OnlineStore.Conflict rejected) { result.completeExceptionally(new PreviewWorld.Rejected(401,"account")); }
      catch(PreviewWorld.Rejected rejected) { result.completeExceptionally(rejected); }
      catch(Exception failure) { result.completeExceptionally(new PreviewWorld.Rejected(503,"account_unavailable")); }
    }); } catch(RejectedExecutionException busy) { pending.remove(result);throw new PreviewWorld.Rejected(503,"account_busy"); }
    return result;
  }

  CompletableFuture<Void> authenticate(String peer,String secret) {
    return submit(peer,()-> { store.authenticateAccount(secret);return null; });
  }

  CompletableFuture<OnlineCharacterCreation.Result> create(String peer,String secret,
      OnlineCharacterCreation.Request request) {
    return submit(peer,()-> {
      CharacterClass type=resolveCharacterClass(request.characterClass());
      var hostedStart=plan.regions().getFirst();
      var spec=hostedStart.spec();
      var map=hostedStart.map();
      var position=new CharacterProgress.Respawn(spec.world(),map.originX+map.spawnX,map.originY+map.spawnY);
      var respawn=new CharacterProgress.Respawn(com.perso.T4C.config.GameConstants.PLAYER_RESPAWN_TILE_Z,
          com.perso.T4C.config.GameConstants.PLAYER_RESPAWN_TILE_X,
          com.perso.T4C.config.GameConstants.PLAYER_RESPAWN_TILE_Y);
      if(!plan.canStand(position) || !plan.canStand(respawn))
        throw new PreviewWorld.Rejected(503,"account_unavailable");
      var created=store.createAuthenticated(secret,request,position.world(),position.x(),position.y(),()-> {
        var initial=OnlineCharacterCreator.prepare(type,request.gender(),request.evil(),new java.security.SecureRandom());
        return new OnlineStore.InitialState(initial.progress(),initial.gold());
      });
      var row=created.character();
      var progress=row.progress();
      var summary=new OnlineRoster.CharacterSummary(row.id(),row.name(),progress.characterClass(),
          progress.gender(),progress.advancement().level(),row.world(),created.inUse() ? "in_use" : "ready");
      return new OnlineCharacterCreation.Result(request.requestId(),summary);
    });
  }

  static CharacterClass resolveCharacterClass(String classId) {
    return switch(classId) {
        case "warrior" -> CharacterClass.WARRIOR;
        case "paladin" -> CharacterClass.PALADIN;
        case "cleric" -> CharacterClass.CLERIC;
        case "healer" -> CharacterClass.HEALER;
        case "mage" -> CharacterClass.MAGE;
        case "battlemage" -> CharacterClass.BATTLE_MAGE;
        default -> throw new PreviewWorld.Rejected(400,"unsupported_class");
      };
  }

  CompletableFuture<OnlineRoster> request(String peer,String secret,UUID after) {
    return submit(peer,()-> {
        var rows=store.roster(secret,after);var summaries=new ArrayList<OnlineRoster.CharacterSummary>();
        for(var row:rows.subList(0,Math.min(rows.size(),OnlineRoster.PAGE_SIZE))) {
          var c=row.character();var p=c.progress();String availability;
          if(p==null) availability="uninitialized";
          else if(!plan.canStand(new CharacterProgress.Respawn(c.world(),c.x(),c.y()))) availability="unhosted_region";
          else if(p.respawn()==null || !plan.canStand(p.respawn())) availability="unavailable_respawn";
          else availability=row.inUse() ? "in_use" : "ready";
          summaries.add(new OnlineRoster.CharacterSummary(c.id(),c.name(),p==null ? null : p.characterClass(),
              p==null ? null : p.gender(),p==null ? null : p.advancement().level(),c.world(),availability));
        }
        return new OnlineRoster(summaries,rows.size()>OnlineRoster.PAGE_SIZE ? summaries.getLast().id() : null);
    });
  }
  @Override public synchronized void close() {
    closed=true;
    for(var result:pending) result.completeExceptionally(new PreviewWorld.Rejected(503,"account_closed"));
    workers.shutdownNow();boolean interrupted=false;
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(CLOSE_TIMEOUT_SECONDS);
    try {
      while(!workers.isTerminated()) {
        long remaining=deadline-System.nanoTime();if(remaining<=0) throw new IllegalStateException("Account workers did not terminate");
        try { workers.awaitTermination(remaining,TimeUnit.NANOSECONDS); }
        catch(InterruptedException signal) { interrupted=true; }
      }
    } finally { if(interrupted) Thread.currentThread().interrupt(); }
  }

}
