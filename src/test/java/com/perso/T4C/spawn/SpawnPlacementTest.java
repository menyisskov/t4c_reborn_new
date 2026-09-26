package com.perso.T4C.spawn;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.perso.T4C.content.ForkContent;
import com.perso.T4C.helper.CollisionReader;
import com.perso.T4C.helper.CollisionType;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * T4C-0063: everything this fork places in the world has to stand somewhere a player can actually
 * walk to. Thirty-three spawns did not - Harbormaster Rangor was in the sea west of Windhowl, and
 * every single Sunken Chancel creature including its boss was in open water, which made "Tide
 * Warden's Plea" impossible to finish. Nothing caught it, because a blocked tile is only visible
 * when you walk to it in-game.
 *
 * <p>Two things are checked, because only one of them is obvious:
 *
 * <ul>
 *   <li>the tile itself does not block movement - deep and shallow water both do, so a spawn in
 *       the sea is unreachable however pretty it looks on the map;
 *   <li>the tile belongs to a reasonably sized stretch of connected walkable ground, so a spawn
 *       cannot be quietly parked on a one-tile sandbar that passes the first check and is still
 *       impossible to stand next to.
 * </ul>
 *
 * <p>Deliberately scoped to {@link ForkContent} rather than every spawn in the game: the inherited
 * legacy content has around 200 spawns on blocking tiles, which is its own (much larger) piece of
 * work and not something this fork should be held up by.
 */
class SpawnPlacementTest {
  /** How much connected walkable ground counts as "somewhere you can get to". */
  private static final int MIN_CONNECTED_WALKABLE_TILES = 200;

  private static CollisionReader collision;

  @BeforeAll
  static void loadWorld() throws IOException {
    collision = new CollisionReader(new File("assets/maps/worldmap/worldmap.colbin"));
    MonsterJsonLoader.loadAndRegister();
  }

  private static boolean walkable(int x, int y) {
    return !CollisionType.fromValue(collision.getCollision(x, y)).isBlocksMovement();
  }

  /**
   * A spawn's {@code type} is a short alias ({@code "Mordrenn"}); the compendium's content lists
   * use the registry's display name ({@code "Mordrenn the Drowned Inquisitor"}). Resolve through
   * the registry so an alias can never silently drop a monster out of this check.
   */
  private static boolean isForkContent(SpawnDefinition spawn) {
    if (ForkContent.NEW_NPC_IDS.contains(spawn.type())
        || ForkContent.ACTIVATED_NPC_IDS.contains(spawn.type())) {
      return true;
    }
    MonsterDef def = MonsterRegistry.findByName(spawn.type());
    String name = def != null ? def.getName() : spawn.type();
    return ForkContent.NEW_MONSTER_NAMES.contains(name)
        || ForkContent.ACTIVATED_MONSTER_NAMES.contains(name);
  }

  /** Flood fill outwards, giving up as soon as we have seen enough to call the ground usable. */
  private static int connectedWalkableTiles(int startX, int startY) {
    if (!walkable(startX, startY)) return 0;
    Set<Long> seen = new HashSet<>();
    Deque<int[]> queue = new ArrayDeque<>();
    seen.add(key(startX, startY));
    queue.add(new int[] {startX, startY});
    int count = 0;
    while (!queue.isEmpty() && count < MIN_CONNECTED_WALKABLE_TILES) {
      int[] at = queue.poll();
      count++;
      for (int[] step : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
        int nx = at[0] + step[0];
        int ny = at[1] + step[1];
        if (walkable(nx, ny) && seen.add(key(nx, ny))) queue.add(new int[] {nx, ny});
      }
    }
    return count;
  }

  private static long key(int x, int y) {
    return ((long) x << 32) ^ (y & 0xffffffffL);
  }

  private static List<SpawnDefinition> forkSpawnsOnTheWorldmap() {
    List<SpawnDefinition> out = new ArrayList<>();
    List<SpawnDefinition> all = new ArrayList<>(SpawnRegistry.npcs());
    all.addAll(SpawnRegistry.monsters());
    // worldZ 0 only: the other levels are separate maps with their own collision data, which this
    // reader does not hold.
    for (SpawnDefinition s : all) {
      if (s.z() == 0 && isForkContent(s)) out.add(s);
    }
    return out;
  }

  @Test
  void nothingThisForkPlacedStandsOnGroundYouCannotWalkOn() {
    List<String> stranded = new ArrayList<>();
    for (SpawnDefinition s : forkSpawnsOnTheWorldmap()) {
      if (!walkable(s.x(), s.y())) {
        stranded.add(
            s.type()
                + " at ("
                + s.x()
                + ","
                + s.y()
                + ") is on "
                + CollisionType.fromValue(collision.getCollision(s.x(), s.y())).getDisplayName());
      }
    }
    assertTrue(
        stranded.isEmpty(),
        "these spawns are on tiles that block movement, so a player can never reach them: "
            + stranded);
  }

  @Test
  void nothingThisForkPlacedIsMaroonedOnAScrapOfGround() {
    List<String> marooned = new ArrayList<>();
    for (SpawnDefinition s : forkSpawnsOnTheWorldmap()) {
      if (!walkable(s.x(), s.y())) continue; // already reported by the test above
      int room = connectedWalkableTiles(s.x(), s.y());
      if (room < MIN_CONNECTED_WALKABLE_TILES) {
        marooned.add(s.type() + " at (" + s.x() + "," + s.y() + ") has only " + room + " tiles");
      }
    }
    assertTrue(
        marooned.isEmpty(),
        "these spawns stand on an isolated scrap of walkable ground (fewer than "
            + MIN_CONNECTED_WALKABLE_TILES
            + " connected tiles), which in practice means nobody can stand next to them: "
            + marooned);
  }
}
