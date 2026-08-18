package com.perso.T4C.tmpl3;

import java.util.Comparator;
import java.util.List;

public final class Tmpl3Matcher {
  private final List<Tmpl3Mask> masks;

  public Tmpl3Matcher(List<Tmpl3Mask> masks) {
    this.masks = List.copyOf(masks);
  }

  public List<Tmpl3Candidate> findBestCandidates(MaskColor[][] expected, int limit) {
    return masks.stream()
        .map(mask -> new Tmpl3Candidate(mask, score(expected, mask)))
        .sorted(
            Comparator.comparingDouble(Tmpl3Candidate::terrainScore)
                .thenComparingInt(Tmpl3Candidate::id))
        .limit(Math.max(1, limit))
        .toList();
  }

  private double score(MaskColor[][] expected, Tmpl3Mask candidate) {
    MaskColor[][] actual = candidate.pixels();
    double score = 0d;
    for (int y = 0; y < Tmpl3Mask.HEIGHT; y++) {
      for (int x = 0; x < Tmpl3Mask.WIDTH; x++) {
        MaskColor e = expected[y][x];
        MaskColor a = actual[y][x];
        if (e == MaskColor.UNKNOWN || a == MaskColor.UNKNOWN) {
          continue;
        }
        if (e != a) {
          score += pixelWeight(x, y);
        }
      }
    }
    return score;
  }

  private static double pixelWeight(int x, int y) {
    boolean edge = x == 0 || y == 0 || x == Tmpl3Mask.WIDTH - 1 || y == Tmpl3Mask.HEIGHT - 1;
    if (edge) {
      return 5d;
    }
    boolean diagonal =
        Math.abs((x / 31d) - (y / 15d)) < 0.10d || Math.abs((x / 31d) - (1d - y / 15d)) < 0.10d;
    if (diagonal) {
      return 4d;
    }
    double dx = Math.abs(x - 15.5d) / 15.5d;
    double dy = Math.abs(y - 7.5d) / 7.5d;
    boolean centerDiamond = dx + dy < 0.55d;
    return centerDiamond ? 2d : 1d;
  }
}
