package com.perso.T4C.world;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.helper.CollisionManager;
import com.perso.T4C.helper.CollisionReader;
import java.io.File;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Proves the shared spell line-of-sight rule gives, over the real world collision map, exactly the
 * answers single-player's own walk gave before it was extracted. {@code legacy} below is the
 * original {@code MainGameScreen.hasLineOfSight} body, copied unchanged.
 */
@ResourceLock("CollisionManager")
class SpellLineOfSightGoldenTest {
  private static final File WORLD = new File("assets/maps/worldmap/worldmap.colbin");

  @BeforeEach
  void load() throws Exception {
    CollisionManager.getInstance().initialize(new CollisionReader(WORLD));
  }

  @AfterEach
  void clear() {
    CollisionManager.getInstance().clear();
  }

  private static boolean legacy(Vector2 from, Vector2 to) {
    float dx = to.x - from.x;
    float dy = to.y - from.y;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);
    if (distance <= 0.001f) {
      return true;
    }
    float step = Math.min(GRID_W, GRID_H) * 0.5f;
    int steps = Math.max(1, (int) Math.ceil(distance / step));
    float stepX = dx / steps;
    float stepY = dy / steps;
    for (int i = 1; i < steps; i++) {
      float checkX = from.x + stepX * i;
      float checkY = from.y + stepY * i;
      if (CollisionManager.getInstance().blocksLineOfSight(checkX, checkY)) {
        return false;
      }
    }
    return true;
  }

  private static boolean shared(Vector2 from, Vector2 to) {
    return LineOfSightRules.sampled(from.x, from.y, to.x, to.y, GRID_W, GRID_H,
        CollisionManager.getInstance()::blocksLineOfSight);
  }

  @Test
  void sharedRuleMatchesTheOriginalWalkOnEveryRealMapSample() {
    var random = new Random(20261028L);
    int width = CollisionManager.getInstance().getCollisionWidth();
    int height = CollisionManager.getInstance().getCollisionHeight();
    int visible = 0, blocked = 0;
    long digest = 17;
    for (int i = 0; i < 200_000; i++) {
      // Half the samples cluster in the game's starting area, half roam the whole world; every
      // target is within the 20-tile cast range, at arbitrary sub-tile positions.
      float cx = i % 2 == 0 ? (2767 + random.nextInt(60) - 30 + random.nextFloat()) * GRID_W
          : (random.nextInt(width - 50) + 25 + random.nextFloat()) * GRID_W;
      float cy = i % 2 == 0 ? (1086 + random.nextInt(40) - 20 + random.nextFloat()) * GRID_H
          : (random.nextInt(height - 50) + 25 + random.nextFloat()) * GRID_H;
      var from = new Vector2(cx, cy);
      var to = new Vector2(cx + (random.nextFloat() * 40 - 20) * GRID_W, cy + (random.nextFloat() * 40 - 20) * GRID_H);
      boolean expected = legacy(from, to);
      assertEquals(expected, shared(from, to), "Mismatch from " + from + " to " + to);
      digest = digest * 31 + (expected ? 1 : 2);
      if (expected) visible++; else blocked++;
    }
    // Golden totals recorded from the original walk: guards both the rule and the map data.
    assertEquals(GOLDEN_VISIBLE, visible);
    assertEquals(GOLDEN_BLOCKED, blocked);
    assertEquals(GOLDEN_DIGEST, digest);
  }

  @Test
  void edgeCasesKeepTheirOriginalAnswers() {
    var a = new Vector2(100f, 100f);
    assertTrue(shared(a, a.cpy()));
    assertTrue(shared(a, new Vector2(100.0005f, 100f)));
    assertEquals(legacy(a, new Vector2(1e6f, 1e6f)), shared(a, new Vector2(1e6f, 1e6f)));
    assertEquals(legacy(new Vector2(-50f, -50f), a), shared(new Vector2(-50f, -50f), a));
  }

  private static final int GOLDEN_VISIBLE = 135745;
  private static final int GOLDEN_BLOCKED = 64255;
  private static final long GOLDEN_DIGEST = -7959235772963176604L;
}
