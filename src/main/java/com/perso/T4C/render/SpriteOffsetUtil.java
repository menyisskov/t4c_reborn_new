package com.perso.T4C.render;

import com.badlogic.gdx.math.Vector2;
/**
 * Class representing SpriteOffsetUtil.
 */

public final class SpriteOffsetUtil {
    private SpriteOffsetUtil() {
    }

    public static Vector2 selectOffset(Vector2 offset1, Vector2 offset2, boolean flipX, Vector2 fallback) {
        if (flipX) {
            return offset2 != null ? offset2 : fallback;
        }
        return offset1 != null ? offset1 : fallback;
    }
}
