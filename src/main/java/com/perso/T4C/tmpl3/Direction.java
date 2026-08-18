package com.perso.T4C.tmpl3;

public enum Direction {
  N(0, -1, true),
  NE(1, -1, false),
  E(1, 0, true),
  SE(1, 1, false),
  S(0, 1, true),
  SW(-1, 1, false),
  W(-1, 0, true),
  NW(-1, -1, false);
  public final int dx;
  public final int dy;
  public final boolean cardinal;

  Direction(int dx, int dy, boolean cardinal) {
    this.dx = dx;
    this.dy = dy;
    this.cardinal = cardinal;
  }

  public Direction opposite() {
    return switch (this) {
      case N -> S;
      case NE -> SW;
      case E -> W;
      case SE -> NW;
      case S -> N;
      case SW -> NE;
      case W -> E;
      case NW -> SE;
    };
  }
}
