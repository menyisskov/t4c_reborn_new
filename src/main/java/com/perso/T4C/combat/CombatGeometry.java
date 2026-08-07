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
        return traceLineOfSight(from, to, false);
    }

    /**
     * Line of sight for starting a conversation. Railings such as cemetery gates are open enough to
     * talk across, so they are skipped here while still blocking movement and attacks.
     */
    public static boolean hasTalkLineOfSight(Vector2 from, Vector2 to) {
        return traceLineOfSight(from, to, true);
    }

    private static boolean traceLineOfSight(Vector2 from, Vector2 to, boolean forTalking) {
        if (from == null || to == null || !CollisionManager.getInstance().isInitialized()) return true;
        float distance = from.dst(to);
        if (distance <= 0.001f) return true;
        int steps = Math.max(1, (int) Math.ceil(distance / (Math.min(GRID_W, GRID_H) * 0.5f)));
        for (int i = 1; i < steps; i++) {
            float alpha = i / (float) steps;
            float x = from.x + (to.x - from.x) * alpha;
            float y = from.y + (to.y - from.y) * alpha;
            boolean blocked = forTalking
                    ? CollisionManager.getInstance().blocksTalkLineOfSight(x, y)
                    : CollisionManager.getInstance().blocksLineOfSight(x, y);
            if (blocked) return false;
        }
        return true;
    }
}
