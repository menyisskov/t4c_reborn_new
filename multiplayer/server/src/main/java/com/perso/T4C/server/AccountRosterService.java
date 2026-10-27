package com.perso.T4C.server;

import com.perso.T4C.online.model.*;
import com.perso.T4C.server.persistence.OnlineStore;
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
  private final Set<CompletableFuture<OnlineRoster>> pending=ConcurrentHashMap.newKeySet();
  private final OnlineStore store;
  private final HostedRegionPlan plan;
  private final PeerLimiter limiter=new PeerLimiter(System::nanoTime);
  private final ThreadPoolExecutor workers=new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(20),
      Thread.ofPlatform().name("account-roster-",0).factory(),new ThreadPoolExecutor.AbortPolicy());
  AccountRosterService(OnlineStore store,HostedRegionPlan plan) { this.store=store;this.plan=plan; }
  synchronized CompletableFuture<OnlineRoster> request(String peer,String secret,UUID after) {
    if(closed) throw new PreviewWorld.Rejected(503,"account_closed");
    limiter.admit(peer);var result=new CompletableFuture<OnlineRoster>();
    pending.add(result);result.whenComplete((value,error)->pending.remove(result));
    try { workers.execute(()-> {
      if(result.isDone()) return;
      try {
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
        result.complete(new OnlineRoster(summaries,rows.size()>OnlineRoster.PAGE_SIZE ? summaries.getLast().id() : null));
      } catch(OnlineStore.Conflict rejected) { result.completeExceptionally(new PreviewWorld.Rejected(401,"account")); }
      catch(Exception failure) { result.completeExceptionally(new PreviewWorld.Rejected(503,"account_unavailable")); }
    }); } catch(RejectedExecutionException busy) { pending.remove(result);throw new PreviewWorld.Rejected(503,"account_busy"); }
    return result;
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
