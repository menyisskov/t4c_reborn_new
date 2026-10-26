package com.perso.T4C.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

class RadarHudTest {

  @Test
  void keepsScreenOrientationAndStaysInsideTheDisc() {
    Vector2 out = new Vector2();
    assertTrue(RadarHud.project(RadarHud.RANGE, 0f, out));
    assertTrue(out.x > 0f && out.x < RadarHud.DIAMETER / 2f);
    assertEquals(0f, out.y, 1e-4f);
    assertTrue(RadarHud.project(0f, -RadarHud.RANGE / 2f, out));
    assertTrue(out.y < 0f);
  }

  @Test
  void hidesEverythingOutOfRange() {
    Vector2 out = new Vector2();
    assertFalse(RadarHud.project(RadarHud.RANGE, 1f, out));
  }

  @Test
  void eachKindHasItsOwnColor() {
    assertEquals(RadarHud.PLAYER_COLOR, RadarHud.Kind.PLAYER.color());
    assertEquals(RadarHud.NPC_COLOR, RadarHud.Kind.NPC.color());
    assertEquals(RadarHud.MONSTER_COLOR, RadarHud.Kind.MONSTER.color());
    assertEquals(RadarHud.BOSS_COLOR, RadarHud.Kind.BOSS.color());
  }

  @Test
  void radarAndItsHitAreaFollowTheLowerRightCornerWhenTheWindowChanges() {
    float width = 1280, height = 768;
    float cx = RadarHud.left(width) + RadarHud.DIAMETER / 2;
    float cy = RadarHud.top(height) + RadarHud.DIAMETER / 2;
    assertTrue(cx > width / 2 && cy > height / 2);
    assertTrue(RadarHud.contains(cx, cy, width, height));
    assertFalse(RadarHud.contains(cx, RadarHud.DIAMETER / 2, width, height));
    assertEquals(120, RadarHud.top(height + 120) - RadarHud.top(height), .001f);
    assertTrue(RadarHud.contains(cx + 160, cy + 120, width + 160, height + 120));
  }
}
