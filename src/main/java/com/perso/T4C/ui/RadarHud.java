package com.perso.T4C.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.monster.core.MonsterRank;
import com.perso.T4C.npc.core.BaseNPC;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Round radar in the top-right corner of the game view. The local player sits in the centre;
 * everything within {@link #RANGE} world pixels shows as a dot, oriented like the screen: other
 * players blue, NPCs green, monsters yellow, bosses red (bigger, drawn on top).
 */
public final class RadarHud {
  public static final float DIAMETER = 150f;
  public static final float RANGE = 40f * GameConstants.GRID_W;
  static final Color PLAYER_COLOR = new Color(0.25f, 0.55f, 1f, 1f);
  static final Color NPC_COLOR = new Color(0.25f, 0.9f, 0.3f, 1f);
  static final Color MONSTER_COLOR = new Color(1f, 0.85f, 0.1f, 1f);
  static final Color BOSS_COLOR = new Color(1f, 0.15f, 0.15f, 1f);
  private static final Color SHADOW = new Color(0f, 0f, 0f, 0.85f);
  private static final int DOT = 7;
  private static final int BOSS_DOT = 10;
  private static final float MARGIN = 10f;
  private static final float TOP = 36f;

  public enum Kind {
    PLAYER,
    NPC,
    MONSTER,
    BOSS;

    Color color() {
      return switch (this) {
        case PLAYER -> PLAYER_COLOR;
        case NPC -> NPC_COLOR;
        case MONSTER -> MONSTER_COLOR;
        case BOSS -> BOSS_COLOR;
      };
    }
  }

  private final Texture disc;
  private final Texture dot;
  private final Map<String, Boolean> bossByName = new HashMap<>();
  private final Vector2 offset = new Vector2();

  public RadarHud() {
    disc = createDisc((int) DIAMETER);
    dot = createDot(16);
  }

  /** Hostile NPCs (Skraug, a guard you provoked...) read as monsters; the rest as NPCs. */
  public static Kind classify(BaseNPC npc) {
    return npc.isHostile() ? Kind.MONSTER : Kind.NPC;
  }

  public Kind classify(BaseMonster monster) {
    boolean boss =
        bossByName.computeIfAbsent(
            String.valueOf(monster.getName()), name -> MonsterRank.isBoss(monster));
    return boss ? Kind.BOSS : Kind.MONSTER;
  }

  /**
   * Maps a world offset from the player to a radar offset, or returns false when it is out of
   * range. Kept static and allocation-free so it can be unit tested without a GL context.
   */
  public static boolean project(float dx, float dy, Vector2 out) {
    float distanceSquared = dx * dx + dy * dy;
    if (distanceSquared > RANGE * RANGE) return false;
    float scale = (DIAMETER / 2f - BOSS_DOT / 2f) / RANGE;
    out.set(dx * scale, dy * scale);
    return true;
  }

  public void render(
      SpriteBatch batch,
      float viewportWidth,
      Vector2 center,
      List<BaseMonster> monsters,
      List<BaseNPC> npcs,
      List<Vector2> otherPlayers) {
    if (center == null) return;
    float x = viewportWidth - DIAMETER - MARGIN;
    float y = TOP;
    float cx = x + DIAMETER / 2f;
    float cy = y + DIAMETER / 2f;
    Color previous = new Color(batch.getColor());
    batch.setColor(Color.WHITE);
    batch.draw(disc, x, y, DIAMETER, DIAMETER);
    if (npcs != null) {
      for (BaseNPC npc : npcs) {
        if (npc != null) drawBlip(batch, cx, cy, center, npc.getPosition(), classify(npc));
      }
    }
    // Bosses go in a second pass so a crowd of ordinary monsters never hides them.
    drawMonsters(batch, cx, cy, center, monsters, Kind.MONSTER);
    drawMonsters(batch, cx, cy, center, monsters, Kind.BOSS);
    if (otherPlayers != null) {
      for (Vector2 other : otherPlayers) drawBlip(batch, cx, cy, center, other, Kind.PLAYER);
    }
    drawDot(batch, cx, cy, 6, Color.WHITE);
    batch.setColor(previous);
  }

  private void drawMonsters(
      SpriteBatch batch,
      float cx,
      float cy,
      Vector2 center,
      List<BaseMonster> monsters,
      Kind wanted) {
    if (monsters == null) return;
    for (BaseMonster monster : monsters) {
      if (monster == null || monster.isDead()) continue;
      Vector2 position = monster.getPosition();
      if (!project(position.x - center.x, position.y - center.y, offset)) continue;
      if (classify(monster) != wanted) continue;
      drawDot(
          batch,
          cx + offset.x,
          cy + offset.y,
          wanted == Kind.BOSS ? BOSS_DOT : DOT,
          wanted.color());
    }
  }

  private void drawBlip(
      SpriteBatch batch, float cx, float cy, Vector2 center, Vector2 position, Kind kind) {
    if (position == null) return;
    if (!project(position.x - center.x, position.y - center.y, offset)) return;
    drawDot(batch, cx + offset.x, cy + offset.y, kind == Kind.BOSS ? BOSS_DOT : DOT, kind.color());
  }

  private void drawDot(SpriteBatch batch, float x, float y, int size, Color color) {
    batch.setColor(SHADOW);
    batch.draw(dot, x - size / 2f - 1f, y - size / 2f - 1f, size + 2f, size + 2f);
    batch.setColor(color);
    batch.draw(dot, x - size / 2f, y - size / 2f, size, size);
  }

  public void dispose() {
    disc.dispose();
    dot.dispose();
  }

  private static Texture createDisc(int size) {
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setBlending(Pixmap.Blending.None);
    float r = size / 2f;
    for (int py = 0; py < size; py++) {
      for (int px = 0; px < size; px++) {
        float dx = px + 0.5f - r;
        float dy = py + 0.5f - r;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        if (d > r) continue;
        int rgba;
        if (d > r - 2.5f) rgba = Color.rgba8888(0.87f, 0.62f, 0f, 1f); // gold rim
        // Inner ring at half range: roughly the edge of what is on screen.
        else if (Math.abs(d - r / 2f) < 0.6f) rgba = Color.rgba8888(0.87f, 0.62f, 0f, 0.28f);
        else rgba = Color.rgba8888(0.03f, 0.04f, 0.06f, 0.62f);
        pixmap.drawPixel(px, py, rgba);
      }
    }
    Texture texture = new Texture(pixmap);
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    pixmap.dispose();
    return texture;
  }

  private static Texture createDot(int size) {
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    pixmap.setBlending(Pixmap.Blending.None);
    pixmap.setColor(Color.WHITE);
    pixmap.fillCircle(size / 2, size / 2, size / 2 - 1);
    Texture texture = new Texture(pixmap);
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    pixmap.dispose();
    return texture;
  }
}
