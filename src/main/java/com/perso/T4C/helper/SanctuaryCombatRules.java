package com.perso.T4C.helper;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.death.DeathPenaltyService;

/** Safe-haven tiles protect both sides of a fight, including at projectile impact. */
public final class SanctuaryCombatRules {
  private SanctuaryCombatRules() {}

  public static boolean isProtected(Vector2 position) {
    return position != null
        && DeathPenaltyService.isSafeHaven(
            CollisionManager.getInstance().getCollisionValue(position.x, position.y));
  }

  public static boolean canFight(Vector2 attacker, Vector2 target) {
    return !isProtected(attacker) && !isProtected(target);
  }
}
