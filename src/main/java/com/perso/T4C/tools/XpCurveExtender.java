package com.perso.T4C.tools;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.mapping.definition.XpCurveDefinitions;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Extends the level cap from 200 to 500 by continuing the existing XP curve's power-law growth
 * (xpToNextLevel(level) is close to a constant * level^exponent across the original 1-199 data;
 * fitted here via log-log least squares rather than hardcoded, so it faithfully continues the
 * game's actual curve instead of switching to a different shape at the seam).
 */
public final class XpCurveExtender {
  private XpCurveExtender() {}

  private static final int NEW_MAX_LEVEL = 500;

  public static void main(String[] args) throws IOException {
    String outputPath =
        args.length > 0
            ? args[0]
            : "src/main/java/com/perso/T4C/mapping/definition/XpCurveDefinitions.java";

    List<XpCurve.Entry> existing = new java.util.ArrayList<>(XpCurveDefinitions.all());
    existing.sort(java.util.Comparator.comparingInt(XpCurve.Entry::getLevel));

    // Fit ln(xpToNextLevel) = ln(C) + b*ln(level) using levels 2-199 (level 1 and the level-200
    // cap-marker entry, whose xpToNextLevel is 0, aren't real growth data points).
    List<double[]> points = new java.util.ArrayList<>();
    for (XpCurve.Entry e : existing) {
      if (e.getLevel() >= 2 && e.getXpToNextLevel() > 0) {
        points.add(new double[] {Math.log(e.getLevel()), Math.log(e.getXpToNextLevel())});
      }
    }
    double[] fit = fit(points);
    double b = fit[0];
    double lnC = fit[1];
    System.out.printf("xpToNextLevel(level) ~= %.6f * level^%.6f%n", Math.exp(lnC), b);

    int lastKnownLevel = existing.get(existing.size() - 1).getLevel(); // 200
    int runningTotal = existing.get(existing.size() - 1).getTotalXp(); // totalXp already at 200

    List<XpCurve.Entry> result = new java.util.ArrayList<>();
    // Keep levels 1..(lastKnownLevel-1) exactly as authored.
    for (XpCurve.Entry e : existing) {
      if (e.getLevel() < lastKnownLevel) {
        result.add(e);
      }
    }

    for (int level = lastKnownLevel; level <= NEW_MAX_LEVEL; level++) {
      int xpToNext =
          level == NEW_MAX_LEVEL ? 0 : (int) Math.round(Math.exp(lnC) * Math.pow(level, b));
      result.add(new XpCurve.Entry(level, xpToNext, runningTotal));
      runningTotal += xpToNext;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("package com.perso.T4C.mapping.definition;\n\n");
    sb.append("import com.perso.T4C.helper.XpCurve;\n");
    sb.append("import java.util.List;\n\n");
    sb.append("public final class XpCurveDefinitions {\n");
    sb.append("  private XpCurveDefinitions() {}\n\n");
    sb.append("  public static List<XpCurve.Entry> all() {\n");
    sb.append("    return List.of(\n");
    for (int i = 0; i < result.size(); i++) {
      XpCurve.Entry e = result.get(i);
      sb.append("        new XpCurve.Entry(")
          .append(e.getLevel())
          .append(", ")
          .append(e.getXpToNextLevel())
          .append(", ")
          .append(e.getTotalXp())
          .append(")");
      sb.append(i == result.size() - 1 ? ");\n" : ",\n");
    }
    sb.append("  }\n");
    sb.append("}\n");

    Files.writeString(Path.of(outputPath), sb.toString(), StandardCharsets.UTF_8);
    System.out.println(
        "Wrote " + result.size() + " XP curve entries (levels 1-" + NEW_MAX_LEVEL + ") to " + outputPath);
  }

  private static double[] fit(List<double[]> points) {
    int n = points.size();
    double meanX = points.stream().mapToDouble(p -> p[0]).average().orElseThrow();
    double meanY = points.stream().mapToDouble(p -> p[1]).average().orElseThrow();
    double sxy = 0;
    double sxx = 0;
    for (double[] p : points) {
      sxy += (p[0] - meanX) * (p[1] - meanY);
      sxx += (p[0] - meanX) * (p[0] - meanX);
    }
    double slope = sxy / sxx;
    double intercept = meanY - slope * meanX;
    return new double[] {slope, intercept};
  }
}
