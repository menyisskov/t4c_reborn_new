package com.perso.T4C.combat;

import com.badlogic.gdx.math.Vector2;
import com.perso.T4C.helper.CollisionManager;

import static com.perso.T4C.config.GameConstants.GRID_H;
import static com.perso.T4C.config.GameConstants.GRID_W;

/** Collision-aware geometric combat checks shared by melee, bows and spells. */
public final class CombatGeometry {
    private CombatGeometry() {
    }

    public static boolean hasLineOfSight(Vector2 from, Vector2 to) {
        if (from == null || to == null || !CollisionManager.getInstance().isInitialized()) return true;
        float distance = from.dst(to);
        if (distance <= 0.001f) return true;
        int steps = Math.max(1, (int) Math.ceil(distance / (Math.min(GRID_W, GRID_H) * 0.5f)));
        for (int i = 1; i < steps; i++) {
            float alpha = i / (float) steps;
            if (CollisionManager.getInstance().blocksLineOfSight(
                    from.x + (to.x - from.x) * alpha,
                    from.y + (to.y - from.y) * alpha)) return false;
        }
        return true;
    }
}
