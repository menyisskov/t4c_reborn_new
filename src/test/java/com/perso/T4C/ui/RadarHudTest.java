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
}
