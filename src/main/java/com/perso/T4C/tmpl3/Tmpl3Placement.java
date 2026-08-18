package com.perso.T4C.tmpl3;

import java.util.List;

public final class Tmpl3Placement {
  private final Tmpl3Point point;
  private final TerrainContext context;
  private final MaskColor[][] expectedMask;
  private final List<Tmpl3Candidate> candidates;
  private Tmpl3Candidate chosen;

  public Tmpl3Placement(
      Tmpl3Point point,
      TerrainContext context,
      MaskColor[][] expectedMask,
      List<Tmpl3Candidate> candidates) {
    this.point = point;
    this.context = context;
    this.expectedMask = expectedMask;
    this.candidates = List.copyOf(candidates);
    this.chosen = candidates.get(0);
  }

  public Tmpl3Point point() {
    return point;
  }

  public TerrainContext context() {
    return context;
  }

  public MaskColor[][] expectedMask() {
    return expectedMask;
  }

  public List<Tmpl3Candidate> candidates() {
    return candidates;
  }

  public Tmpl3Candidate chosen() {
    return chosen;
  }

  public void choose(Tmpl3Candidate chosen) {
    this.chosen = chosen;
  }
}
