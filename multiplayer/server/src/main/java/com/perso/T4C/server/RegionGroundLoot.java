package com.perso.T4C.server;

import com.perso.T4C.server.persistence.OnlineStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Simulation-owned view of one region's live ground loot. Database rows decide every claim; this
 * cache only pre-checks pickups and holds committed drops until they are claimed or expire. It is
 * changed only on the simulation thread, after commit or after a database read completes.
 */
final class RegionGroundLoot {
  private final RegionMapLoader.Spec spec;
  private final OnlineStore.TileRect area;
  private final Map<UUID,OnlineStore.GroundLoot> live=new LinkedHashMap<>();
  // Ids committed or removed while a read is running. That read may be older than the change.
  private final Set<UUID> changedDuringRead=new HashSet<>();
  // Monotonic time at which each entry's owner-window figure was true (publication, or the start of the read).
  private final Map<UUID,Long> observedAt=new HashMap<>();
  // Recently claimed ids and who claimed them, so a late pickup can say "someone else took it". Bounded.
  private final Map<UUID,UUID> recentlyClaimed=new LinkedHashMap<>() {
    @Override protected boolean removeEldestEntry(Map.Entry<UUID,UUID> eldest) { return size()>RECENT_CLAIMS; }
  };
  private static final int RECENT_CLAIMS=256;
  private final LongSupplier nanos;
  private long readStartedAt;
  private boolean loaded, dirty, reading;

  RegionGroundLoot(RegionMapLoader.Spec spec) { this(spec,System::nanoTime); }

  RegionGroundLoot(RegionMapLoader.Spec spec,LongSupplier nanos) {
    this.nanos=nanos;
    this.spec=spec;
    this.area=new OnlineStore.TileRect(spec.originX(),spec.originY(),
        (long)spec.originX()+spec.width(),(long)spec.originY()+spec.height());
  }

  int world() { return spec.world(); }
  OnlineStore.TileRect area() { return area; }
  /** Streamed regions read only the cells around players (WORLD_STREAMING_DESIGN.md 3.6), never the whole world. */
  boolean streamed() { return spec.streamed(); }
  private java.util.function.Supplier<List<OnlineStore.TileRect>> coveredAreas=List::of;
  void coveredAreas(java.util.function.Supplier<List<OnlineStore.TileRect>> supplier) { coveredAreas=java.util.Objects.requireNonNull(supplier); }
  List<OnlineStore.TileRect> coveredAreas() { return coveredAreas.get(); }
  boolean loaded() { return loaded; }
  boolean needsRead() { return dirty && !reading; }
  boolean reading() { return reading; }
  OnlineStore.GroundLoot get(UUID id) { return live.get(id); }
  boolean contains(UUID id) { return live.containsKey(id); }
  boolean owns(OnlineStore.GroundLoot loot) { return spec.contains(loot.world(),loot.x(),loot.y()); }

  /** Replaces the cache with a database snapshot, keeping any change committed while it was read. */
  void replace(List<OnlineStore.GroundLoot> found) { replace(found,nanos.getAsLong()); }

  private void replace(List<OnlineStore.GroundLoot> found,long now) {
    var fresh=new LinkedHashMap<UUID,OnlineStore.GroundLoot>();
    var stamps=new HashMap<UUID,Long>();
    for (var loot : found) if (owns(loot) && !changedDuringRead.contains(loot.id())) { fresh.put(loot.id(),loot);stamps.put(loot.id(),now); }
    for (var id : changedDuringRead) {
      var current=live.get(id);
      if (current!=null) { fresh.put(id,current);stamps.put(id,observedAt.getOrDefault(id,now)); }
    }
    live.clear();
    live.putAll(fresh);
    observedAt.clear();
    observedAt.putAll(stamps);
    changedDuringRead.clear();
    loaded=true;
  }

  /** Publishes one committed drop. Only ever called after its kill commits and drains. */
  void publish(OnlineStore.GroundLoot loot) {
    if (!owns(loot)) throw new IllegalArgumentException("Loot outside region");
    live.put(loot.id(),loot);
    observedAt.put(loot.id(),nanos.getAsLong());
    noteChange(loot.id());
  }

  /** Removes a drop after its claim commits, or after the database reports it expired or claimed. */
  void remove(UUID id) {
    live.remove(id);
    observedAt.remove(id);
    noteChange(id);
  }

  /** Removes a drop whose claim just committed, remembering who took it. */
  void removeClaimed(UUID id,UUID claimant) {
    remove(id);
    recentlyClaimed.put(id,claimant);
  }

  /** True when this region saw another character claim the drop. Says nothing about who. */
  boolean takenByOther(UUID id,UUID asking) {
    var claimant=recentlyClaimed.get(id);
    return claimant!=null && !claimant.equals(asking);
  }

  void invalidate() { dirty=true; }

  void beginRead() { changedDuringRead.clear(); dirty=false; reading=true; readStartedAt=nanos.getAsLong(); }

  /** Finished read; a failed read leaves the cache dirty for the next cadence. */
  void endRead(List<OnlineStore.GroundLoot> found) {
    reading=false;
    if (found==null) { changedDuringRead.clear(); dirty=true; }
    else {
      replace(found,readStartedAt);
      // A per-cell read that came back full may have left drops out; the snapshot reports it as truncated.
      readCapped=spec.streamed() && found.size()>=OnlineStore.LOOT_CELLS_LIMIT;
    }
  }

  private boolean readCapped;
  /** True when the last database read of a streamed region hit its row cap, so the cached list may be incomplete. */
  boolean readCapped() { return readCapped; }

  void abortRead() { reading=false; changedDuringRead.clear(); dirty=true; }

  /** Copy for filtering or tests; never handed to clients as-is. */
  List<OnlineStore.GroundLoot> snapshot() { return new ArrayList<>(live.values()); }

  /**
   * The public view: nearest-first callers get copies whose owner window is counted down to now. Once
   * the window is over the owner is cleared and the remainder is zero. The database still decides claims.
   */
  List<OnlineStore.GroundLoot> view() {
    long now=nanos.getAsLong();
    var result=new ArrayList<OnlineStore.GroundLoot>(live.size());
    for (var loot : live.values()) {
      long remaining=loot.ownerRemainingMillis();
      if (remaining>0) {
        long elapsed=Math.max(0,now-observedAt.getOrDefault(loot.id(),now))/1_000_000L;
        remaining=Math.max(0,remaining-elapsed);
      }
      if (remaining==loot.ownerRemainingMillis()) result.add(loot);
      else result.add(new OnlineStore.GroundLoot(loot.id(),loot.world(),loot.x(),loot.y(),loot.key(),loot.quantity(),
          loot.charges(),remaining>0 ? loot.ownerCharacter() : null,remaining));
    }
    return result;
  }

  private void noteChange(UUID id) {
    if (reading) { changedDuringRead.add(id); dirty=true; }
  }
}
