package com.perso.T4C.content;

import com.perso.T4C.spell.SpellData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class SpellJavaExporter {
  public static final Path DEFAULT_OUTPUT =
      Path.of("src/main/java/com/perso/T4C/spell/generated");

  private SpellJavaExporter() {}

  public static void export(List<SpellData> spells) throws IOException {
    export(spells, DEFAULT_OUTPUT);
  }

  public static void export(List<SpellData> spells, Path outputDirectory) throws IOException {
    Files.createDirectories(outputDirectory);
    for (SpellData spell : spells == null ? List.<SpellData>of() : spells) {
      if (spell == null) continue;
      String className = className(spell.getName(), spell.getSpellId());
      Files.writeString(
          outputDirectory.resolve(className + ".java"), source(className, spell), StandardCharsets.UTF_8);
    }
  }

  private static String source(String className, SpellData s) {
    StringBuilder out = new StringBuilder();
    out.append("package com.perso.T4C.spell.generated;\n\n")
        .append("import com.perso.T4C.spell.SpellData;\n\n")
        .append("public final class ").append(className).append(" extends SpellData {\n")
        .append("  public ").append(className).append("() {\n    super(\n")
        .append("        ").append(lit(s.getName())).append(",\n")
        .append("        ").append(lit(s.getDescription())).append(",\n")
        .append("        ").append(lit(s.getManaCost())).append(",\n")
        .append("        ").append(s.getRadius()).append(", ").append(s.getMinInt()).append(", ")
        .append(s.getMinWis()).append(", ").append(s.getMinLevel()).append(",\n")
        .append("        ").append(s.isAttack()).append(", ").append(s.isLineOfSight()).append(",\n")
        .append("        ").append(lit(s.getIconId())).append(", ")
        .append(lit(s.getProjectileSpell())).append(", ").append(lit(s.getImpactSpell())).append(",\n")
        .append("        ").append(s.getMinDamage()).append(", ").append(s.getMaxDamage()).append(",\n")
        .append("        ").append(lit(s.getSound())).append(", ").append(lit(s.getSoundImpact())).append(",\n")
        .append("        ").append(s.getCooldownSeconds()).append(", ")
        .append(lit(s.getDuration())).append(", ").append(lit(s.getFrequency())).append(",\n")
        .append("        ").append(s.getPrice()).append(", ").append(buff(s.getBuff())).append(",\n")
        .append("        ").append(s.getSpellId()).append(", ").append(s.getElement()).append(", ")
        .append(s.getTargetType()).append(", ").append(s.getAttackType()).append(",\n")
        .append("        ").append(lit(s.getSuccessRate())).append(", ")
        .append(lit(s.getMentalExhaustion())).append(", ").append(lit(s.getPhysicalExhaustion())).append(", ")
        .append(lit(s.getAttackExhaustion())).append(",\n")
        .append("        ").append(s.getVisualEffect()).append(", ").append(s.getVisualEffectTarget()).append(", ")
        .append(s.isPvp()).append(", ").append(effects(s.getT4cEffects())).append(");\n")
        .append("  }\n}\n");
    return out.toString();
  }

  private static String buff(SpellData.SpellBuff b) {
    if (b == null) return "null";
    String effects = b.getEffects() == null ? "java.util.List.of()" : "java.util.List.of(" + b.getEffects().stream()
        .map(e -> "new SpellData.SpellEffect(" + lit(e.getType()) + ", " + lit(e.getAttribute()) + ", "
            + lit(e.getAmount()) + ", " + lit(e.getDescription()) + ")")
        .reduce((a, c) -> a + ", " + c).orElse("") + ")";
    return "new SpellData.SpellBuff(" + b.getDurationSeconds() + ", " + b.getUnlimited() + ", " + effects + ")";
  }

  private static String effects(List<SpellData.T4cEffect> values) {
    if (values == null || values.isEmpty()) return "java.util.List.of()";
    return "java.util.List.of(" + values.stream().map(e -> "new SpellData.T4cEffect(" + e.getEffectType() + ", java.util.List.of(" 
        + (e.getParameters() == null ? "" : e.getParameters().stream().map(p -> "new SpellData.T4cEffect.EffectParam(" + p.getParamId() + ", " + lit(p.getExpression()) + ")").reduce((a, c) -> a + ", " + c).orElse("")) + "))").reduce((a, c) -> a + ", " + c).orElse("") + ")";
  }

  private static String lit(String value) {
    if (value == null) return "null";
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n") + "\"";
  }

  private static String className(String name, int id) {
    String raw = name == null ? "spell_" + id : name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
    String[] parts = raw.split("_");
    StringBuilder result = new StringBuilder("Spell");
    for (String part : parts) if (!part.isEmpty()) result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
    return result.length() == 5 ? result.append(id).toString() : result.toString();
  }
}
