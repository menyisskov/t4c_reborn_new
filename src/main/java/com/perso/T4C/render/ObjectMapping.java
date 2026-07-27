package com.perso.T4C.render;

import lombok.AllArgsConstructor;

/**
 * Class representing ObjectMapping.
 */

@AllArgsConstructor
public class ObjectMapping {
    public final int id;
    public final String sprite;
    public final boolean clickAnimate;
    public final boolean mirror;
    public final String animateSound;
    public final String reverseAnimateSound;
    public final boolean alwaysBehindEntities;
    public final String displayName;
    public final int depthTileOffsetY;

    public ObjectMapping(int id, String sprite, boolean clickAnimate, boolean mirror, String animateSound, String reverseAnimateSound, boolean alwaysBehindEntities, String displayName) {
        this(id, sprite, clickAnimate, mirror, animateSound, reverseAnimateSound, alwaysBehindEntities, displayName, 0);
    }

    public ObjectMapping(String sprite, boolean clickAnimate, boolean mirror, String animateSound, String reverseAnimateSound, boolean alwaysBehindEntities, String displayName) {
        this(0, sprite, clickAnimate, mirror, animateSound, reverseAnimateSound, alwaysBehindEntities, displayName);
    }

    public ObjectMapping(String sprite, boolean clickAnimate, boolean mirror, String animateSound, String reverseAnimateSound, boolean alwaysBehindEntities, String displayName, int depthTileOffsetY) {
        this(0, sprite, clickAnimate, mirror, animateSound, reverseAnimateSound, alwaysBehindEntities, displayName, depthTileOffsetY);
    }
}
