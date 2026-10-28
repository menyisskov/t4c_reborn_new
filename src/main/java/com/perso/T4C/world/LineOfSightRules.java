package com.perso.T4C.world;

/**
 * Spell line of sight, shared by single-player and the online server.
 *
 * <p>The spell rule is the one single-player has always used: walk the straight segment in world
 * pixels, sampling every half of the smaller tile dimension, and ask the caller whether the sampled
 * point blocks sight. Only the interior samples count: neither endpoint is tested, so a caster or
 * target standing against a wall is not blocked by it, and two points inside the same hair-width
 * are always visible. The arithmetic is deliberately the original single-precision arithmetic so a
 * result never changes with the refactor.
 *
 * <p>The online server also has a stricter supercover ray ({@code PreviewMap.hasLineOfSight}) that
 * it uses for melee and creature attacks. {@link Mode} names both so a caller states which rule it
 * wants; spells must use {@link Mode#SPELL_SAMPLED} to match single-player.
 */
public final class LineOfSightRules {
  /** Which line-of-sight rule a caller wants. */
  public enum Mode {
    /** Single-player's sampled pixel walk; the rule for every spell. */
    SPELL_SAMPLED,
    /** The server's strict tile ray (endpoints and corner-touching walls block); not for spells. */
    SERVER_SUPERCOVER
  }

  /** Whether the world pixel at (x, y) blocks sight. */
  @FunctionalInterface
  public interface Blocker {
    boolean blocks(float worldX, float worldY);
  }

  private LineOfSightRules() {}

  /**
   * Single-player's spell line of sight over world pixels. {@code gridWidth} and {@code gridHeight}
   * are the tile size in pixels (32 x 16 in the game).
   */
  public static boolean sampled(
      float fromX, float fromY, float toX, float toY, int gridWidth, int gridHeight, Blocker blocker) {
    float dx = toX - fromX;
    float dy = toY - fromY;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);
    if (distance <= 0.001f) {
      return true;
    }
    float step = Math.min(gridWidth, gridHeight) * 0.5f;
    int steps = Math.max(1, (int) Math.ceil(distance / step));
    float stepX = dx / steps;
    float stepY = dy / steps;
    for (int i = 1; i < steps; i++) {
      float checkX = fromX + stepX * i;
      float checkY = fromY + stepY * i;
      if (blocker.blocks(checkX, checkY)) {
        return false;
      }
    }
    return true;
  }
}
