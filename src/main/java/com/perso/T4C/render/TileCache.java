package com.perso.T4C.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TileCache {
  public static final Map<String, TextureRegion> regionCache = new ConcurrentHashMap<>();

  public static int clearTmplRegionsOnly() {
    int removed = 0;
    try {
      for (Map.Entry<String, TextureRegion> e : regionCache.entrySet()) {
        String key = e.getKey();
        if (key == null) continue;
        if (!key.contains("|a=") || !key.contains("|b=")) continue;
        TextureRegion tr = e.getValue();
        if (tr != null && tr.getTexture() != null) {
          try {
            tr.getTexture().dispose();
          } catch (Throwable ignored) {
          }
        }
        regionCache.remove(key);
        removed++;
      }
    } catch (Throwable ignored) {
    }
    return removed;
  }

  public static void clearAll() {
    try {
      for (TextureRegion tr : regionCache.values()) {
        if (tr != null && tr.getTexture() != null) {
          try {
            tr.getTexture().dispose();
          } catch (Throwable ignored) {
          }
        }
      }
    } catch (Throwable ignored) {
    }
    regionCache.clear();
  }
}
