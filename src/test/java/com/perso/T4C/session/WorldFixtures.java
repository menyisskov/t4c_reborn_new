package com.perso.T4C.session;

import com.perso.T4C.online.OnlineConnection;
import com.perso.T4C.online.model.CreatureView;
import com.perso.T4C.online.model.GroundLootView;
import com.perso.T4C.online.model.NpcView;
import java.util.List;

/** Fake server views for the remote world tests: one fixed region, the own actor always present. */
final class WorldFixtures {
  static final long OWN = 7L;
  static final OnlineConnection.MapInfo MAP =
      new OnlineConnection.MapInfo(100, 200, 80, 60, true, 6, 0, 0, "avalon", 4L);

  private WorldFixtures() {}

  static OnlineConnection.Actor actor(long id, double x, double y, int hp, int max) {
    return new OnlineConnection.Actor(id, x, y, 1L, 0L, hp, max);
  }

  static OnlineConnection.View view(
      long tick,
      List<OnlineConnection.Actor> others,
      List<CreatureView> creatures,
      List<NpcView> npcs,
      List<GroundLootView> loot) {
    var players = new java.util.ArrayList<OnlineConnection.Actor>();
    players.add(actor(OWN, 3.5, 5.5, 100, 100));
    players.addAll(others);
    var snapshot =
        new OnlineConnection.Snapshot(tick, players, 1L, creatures, MAP.region(), npcs, loot);
    return new OnlineConnection.View(OWN, MAP, snapshot, null, List.of());
  }
}
