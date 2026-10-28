package com.perso.T4C.server;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;
import static org.junit.jupiter.api.Assertions.*;

import com.perso.T4C.helper.BinaryIOUtils;
import com.perso.T4C.world.CollisionRules;
import com.perso.T4C.world.LineOfSightRules;
import java.io.DataInputStream;
import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Spells must see the same line of sight online as in single-player. {@code legacy} is the original
 * single-player walk (copied unchanged) run over the raw world collision file; the server's spell
 * rule must agree with it everywhere. The server's stricter attack ray genuinely disagrees, and the
 * exact size of that disagreement is recorded below so it cannot drift unnoticed.
 */
class SpellLineOfSightParityTest {
  private static final Path WORLD = Path.of("../../assets/maps/worldmap/worldmap.colbin");

  private static boolean legacy(byte[] data, int w, int h, float fx, float fy, float tx, float ty) {
    float dx = tx - fx;
    float dy = ty - fy;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);
    if (distance <= 0.001f) {
      return true;
    }
    float step = Math.min(GRID_W, GRID_H) * 0.5f;
    int steps = Math.max(1, (int) Math.ceil(distance / step));
    float stepX = dx / steps;
    float stepY = dy / steps;
    for (int i = 1; i < steps; i++) {
      int gx = (int) ((fx + stepX * i) / GRID_W);
      int gy = (int) ((fy + stepY * i) / GRID_H);
      if (gx < 0 || gy < 0 || gx >= w || gy >= h || CollisionRules.blocksLineOfSight(data[gy * w + gx] & 255)) {
        return false;
      }
    }
    return true;
  }

  @Test void serverSpellRuleEqualsSinglePlayersWalkAndDiffersFromTheAttackRay() throws Exception {
    var map = PreviewMap.load(WORLD);
    int w, h;
    byte[] data;
    try (var in = new DataInputStream(BinaryIOUtils.openInputStream(WORLD.toFile(), 1 << 20))) {
      w = BinaryIOUtils.readIntLE(in);
      h = BinaryIOUtils.readIntLE(in);
      data = in.readNBytes(w * h);
    }
    var random = new Random(20261028L);
    int agree = 0, sampledOnly = 0, rayOnly = 0, bothVisible = 0, bothBlocked = 0;
    for (int i = 0; i < 100_000; i++) {
      double fx = 1 + random.nextDouble() * (map.width - 2), fy = 1 + random.nextDouble() * (map.height - 2);
      double tx = Math.max(0.5, Math.min(map.width - 0.5, fx + random.nextDouble() * 30 - 15));
      double ty = Math.max(0.5, Math.min(map.height - 0.5, fy + random.nextDouble() * 30 - 15));
      boolean expected = legacy(data, w, h, (float) ((fx + map.originX) * GRID_W), (float) ((fy + map.originY) * GRID_H),
          (float) ((tx + map.originX) * GRID_W), (float) ((ty + map.originY) * GRID_H));
      boolean spell = map.hasLineOfSight(LineOfSightRules.Mode.SPELL_SAMPLED, fx, fy, tx, ty);
      assertEquals(expected, spell, "spell rule differs from single-player at " + fx + "," + fy + " -> " + tx + "," + ty);
      assertEquals(spell, map.hasSpellLineOfSight(fx, fy, tx, ty));
      boolean ray = map.hasLineOfSight(LineOfSightRules.Mode.SERVER_SUPERCOVER, fx, fy, tx, ty);
      assertEquals(map.hasLineOfSight(fx, fy, tx, ty), ray, "supercover mode is the unchanged server ray");
      if (spell == ray) agree++;
      if (spell && ray) bothVisible++;
      if (!spell && !ray) bothBlocked++;
      if (spell && !ray) sampledOnly++;
      if (!spell && ray) rayOnly++;
    }
    // Recorded sizes of the genuine difference between the two rules over 100,000 real-map pairs.
    assertEquals(100_000, agree + sampledOnly + rayOnly);
    assertEquals("bothVisible=" + EXPECT_BOTH_VISIBLE + " bothBlocked=" + EXPECT_BOTH_BLOCKED
            + " spellSeesRayBlocked=" + EXPECT_SAMPLED_ONLY + " raySeesSpellBlocked=" + EXPECT_RAY_ONLY,
        "bothVisible=" + bothVisible + " bothBlocked=" + bothBlocked + " spellSeesRayBlocked=" + sampledOnly
            + " raySeesSpellBlocked=" + rayOnly);
  }

  @Test void spellRuleRefusesPositionsOutsideTheHostedMapAndKeepsEndpointsUntested() throws Exception {
    var map = PreviewMap.load(WORLD);
    assertFalse(map.hasSpellLineOfSight(-1, 1, 2, 2));
    assertFalse(map.hasSpellLineOfSight(1, 1, map.width, 2));
    assertFalse(map.hasSpellLineOfSight(Double.NaN, 1, 2, 2));
    assertTrue(map.hasSpellLineOfSight(map.spawnX, map.spawnY, map.spawnX, map.spawnY));
    assertSame(LineOfSightRules.Mode.SPELL_SAMPLED, PreviewMap.SPELL_LINE_OF_SIGHT);
  }

  // The attack ray is strictly stricter (it also blocks on endpoint tiles and wall corners the
  // sampled walk skips): it never sees what the spell rule cannot.
  private static final int EXPECT_BOTH_VISIBLE = 24127;
  private static final int EXPECT_BOTH_BLOCKED = 73473;
  private static final int EXPECT_SAMPLED_ONLY = 2400;
  private static final int EXPECT_RAY_ONLY = 0;
}
