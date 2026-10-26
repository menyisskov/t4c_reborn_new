package com.perso.T4C.world;

/** Original collision codes, independent of their editor colors and labels. */
public final class CollisionRules {
  private CollisionRules() {}

  public static boolean blocksMovement(int code) {
    return code < 0 || code > 16 || (code >= 1 && code <= 4);
  }

  public static boolean blocksLineOfSight(int code) {
    return code < 0 || code > 16 || code == 1 || code == 2 || code == 9;
  }
}
