package com.perso.T4C.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public record ResolvedTile(
    int tileX, int tileY, TextureRegion region, boolean mirrorX, String spriteName, boolean tmpl) {}
