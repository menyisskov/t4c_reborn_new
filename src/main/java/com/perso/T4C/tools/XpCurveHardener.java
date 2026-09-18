package com.perso.T4C.tools;

import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.mapping.definition.XpCurveDefinitions;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Regenerates the XP curve so a single kill can never skip several levels at high level, and so
 * the required XP grows increasingly (not just linearly) harder to earn the higher you go.
 *
 * <p>Levels 1-99 are kept exactly as authored (unchanged low/mid-level pacing). From level 100
 * onward, xpToNextLevel(level) = round(BASE * x^p(level)), where x = level/100 and p(level) =
 * 2.5 + (level-100)/200 — an exponent that itself grows with level, so the curve's steepness
 * compounds rather than staying a fixed power law. BASE is read from the existing level-100
 * entry, so the curve is continuous at the 99/100 seam (no discontinuity for anyone near it).
 *
 * <p>This deliberately outgrows every known monster's xpOnDeath scaling (verified against the
 * highest-XP legacy content, the ArenaMobXP family, which itself grows roughly as level^4 —
 * see docs/design notes) well before level 500, and by a wide margin by level 1000, the new
 * cap. All accumulation here is in {@code long} — the previous generator
 * (XpCurveExtender, since removed) accumulated totalXp in an {@code int} and silently wrapped
 * negative starting at level 541; that bug is why this class exists.
 */
public final class XpCurveHardener {
  private XpCurveHardener() {}

  private static final int LAST_UNCHANGED_LEVEL = 99;
  private static final int SEAM_LEVEL = 100;
  private static final int NEW_MAX_LEVEL = 1000;

  public static void main(String[] args) throws IOException {
    String outputPath =
        args.length > 0
            ? args[0]
            : "src/main/java/com/perso/T4C/mapping/definition/XpCurveDefinitions.java";

    List<XpCurve.Entry> existing = new java.util.ArrayList<>(XpCurveDefinitions.all());
    existing.sort(java.util.Comparator.comparingInt(XpCurve.Entry::getLevel));

    XpCurve.Entry seam =
        existing.stream()
            .filter(e -> e.getLevel() == SEAM_LEVEL)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No level-100 entry in existing curve"));
    long base = seam.getXpToNextLevel();

    List<XpCurve.Entry> result = new java.util.ArrayList<>();
    for (XpCurve.Entry e : existing) {
      if (e.getLevel() <= LAST_UNCHANGED_LEVEL) {
        result.add(e);
      }
    }

    long runningTotal = seam.getTotalXp();
    for (int level = SEAM_LEVEL; level <= NEW_MAX_LEVEL; level++) {
      long xpToNext = level == NEW_MAX_LEVEL ? 0 : xpToNextLevel(level, base);
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
          .append("L, ")
          .append(e.getTotalXp())
          .append("L)");
      sb.append(i == result.size() - 1 ? ");\n" : ",\n");
    }
    sb.append("  }\n");
    sb.append("}\n");

    Files.writeString(Path.of(outputPath), sb.toString(), StandardCharsets.UTF_8);
    System.out.println(
        "Wrote " + result.size() + " XP curve entries (levels 1-" + NEW_MAX_LEVEL + ") to " + outputPath);
  }

  private static long xpToNextLevel(int level, long base) {
    double x = level / 100.0;
    double p = 2.5 + (level - SEAM_LEVEL) / 200.0;
    return Math.round(base * Math.pow(x, p));
  }
}
