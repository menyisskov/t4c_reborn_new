package com.perso.T4C.server;

import com.perso.T4C.server.persistence.OnlineStore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
  private boolean loaded, dirty, reading;

  RegionGroundLoot(RegionMapLoader.Spec spec) {
    this.spec=spec;
    this.area=new OnlineStore.TileRect(spec.originX(),spec.originY(),
        (long)spec.originX()+spec.width(),(long)spec.originY()+spec.height());
  }

  int world() { return spec.world(); }
  OnlineStore.TileRect area() { return area; }
  boolean loaded() { return loaded; }
  boolean needsRead() { return dirty && !reading; }
  boolean reading() { return reading; }
  OnlineStore.GroundLoot get(UUID id) { return live.get(id); }
  boolean contains(UUID id) { return live.containsKey(id); }
  boolean owns(OnlineStore.GroundLoot loot) { return spec.contains(loot.world(),loot.x(),loot.y()); }

  /** Replaces the cache with a database snapshot, keeping any change committed while it was read. */
  void replace(List<OnlineStore.GroundLoot> found) {
    var fresh=new LinkedHashMap<UUID,OnlineStore.GroundLoot>();
    for (var loot : found) if (owns(loot) && !changedDuringRead.contains(loot.id())) fresh.put(loot.id(),loot);
    for (var id : changedDuringRead) {
      var current=live.get(id);
      if (current!=null) fresh.put(id,current);
    }
    live.clear();
    live.putAll(fresh);
    changedDuringRead.clear();
    loaded=true;
  }

  /** Publishes one committed drop. Only ever called after its kill commits and drains. */
  void publish(OnlineStore.GroundLoot loot) {
    if (!owns(loot)) throw new IllegalArgumentException("Loot outside region");
    live.put(loot.id(),loot);
    noteChange(loot.id());
  }

  /** Removes a drop after its claim commits, or after the database reports it expired or claimed. */
  void remove(UUID id) {
    live.remove(id);
    noteChange(id);
  }

  void invalidate() { dirty=true; }

  void beginRead() { changedDuringRead.clear(); dirty=false; reading=true; }

  /** Finished read; a failed read leaves the cache dirty for the next cadence. */
  void endRead(List<OnlineStore.GroundLoot> found) {
    reading=false;
    if (found==null) { changedDuringRead.clear(); dirty=true; }
    else replace(found);
  }

  void abortRead() { reading=false; changedDuringRead.clear(); dirty=true; }

  /** Copy for filtering or tests; never handed to clients as-is. */
  List<OnlineStore.GroundLoot> snapshot() { return new ArrayList<>(live.values()); }

  private void noteChange(UUID id) {
    if (reading) { changedDuringRead.add(id); dirty=true; }
  }
}
