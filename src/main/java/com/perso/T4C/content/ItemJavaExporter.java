package com.perso.T4C.content;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class ItemJavaExporter {
  public static final Path DEFAULT_OUTPUT = Path.of("src/main/java/com/perso/T4C/item/definition");

  private ItemJavaExporter() {}

  public static void export(List<ItemDefinition> items) throws IOException {
    export(items, DEFAULT_OUTPUT);
  }

  public static void export(List<ItemDefinition> items, Path output) throws IOException {
    Files.createDirectories(output);
    for (ItemDefinition item : items == null ? List.<ItemDefinition>of() : items) {
      if (item == null || item.getKey() == null) continue;
      String className = className(item.getKey(), item.getNumId());
      Files.writeString(
          output.resolve(className + ".java"), source(className, item), StandardCharsets.UTF_8);
    }
    StringBuilder registry =
        new StringBuilder(
            "package com.perso.T4C.item.definition;\n\nimport com.perso.T4C.item.ItemDefinition;\nimport java.util.List;\n\npublic final class ItemDefinitions {\n  private ItemDefinitions() {}\n  public static List<ItemDefinition> all() {\n    return List.of(\n");
    boolean first = true;
    for (ItemDefinition item : items == null ? List.<ItemDefinition>of() : items) {
      if (item == null || item.getKey() == null) continue;
      if (!first) registry.append(",\n");
      registry
          .append("        ")
          .append(className(item.getKey(), item.getNumId()))
          .append(".definition()");
      first = false;
    }
    registry.append("\n    );\n  }\n}\n");
    Files.writeString(
        output.resolve("ItemDefinitions.java"), registry.toString(), StandardCharsets.UTF_8);
  }

  private static String source(String className, ItemDefinition i) {
    return "package com.perso.T4C.item.definition;\n\nimport com.perso.T4C.item.ItemDefinition;\nimport com.perso.T4C.player.BodyPart;\nimport java.util.List;\n\npublic final class "
        + className
        + " {\n  private "
        + className
        + "() {}\n  public static ItemDefinition definition() {\n    return new ItemDefinition(\n        "
        + lit(i.getKey())
        + ", "
        + lit(i.getName())
        + ", "
        + enumValue(i.getBodyPart())
        + ",\n        "
        + lit(i.getAppearanceEquippedPrimary())
        + ", "
        + enumValue(i.getSecondaryBodyPart())
        + ", "
        + lit(i.getAppearanceEquippedSecondary())
        + ",\n        "
        + lit(i.getAppearanceInventory())
        + ", "
        + i.getPrice()
        + "L, "
        + i.getWeight()
        + "L, "
        + i.getArmorClass()
        + "d,\n        "
        + i.getDodgeLost()
        + "L, "
        + i.getMinEnd()
        + "L, "
        + i.getReqAttack()
        + "L, "
        + i.getReqStr()
        + "L, "
        + i.getReqAgi()
        + "L,\n        "
        + i.getMinInt()
        + "L, "
        + i.getMinWis()
        + "L, "
        + i.getAttackSpeed()
        + "d, "
        + i.isUnique()
        + ", "
        + i.isBow()
        + ", "
        + i.isUnlimitedUse()
        + ",\n        "
        + i.getNumId()
        + ", "
        + i.getStructure()
        + ", "
        + i.getAppearanceId()
        + ", "
        + lit(i.getDmgFormula())
        + ", "
        + lit(i.getAtkDelay())
        + ",\n        "
        + i.getRadiance()
        + ", "
        + i.getNbCharges()
        + ", "
        + i.isCanSummon()
        + ", "
        + lit(i.getLockName())
        + ", "
        + i.getLockDiff()
        + ",\n        "
        + lit(i.getSignText())
        + ", "
        + i.getContainerGold()
        + ", "
        + i.getGlobalRespawn()
        + ", "
        + i.getLocalRespawn()
        + ",\n        "
        + spells(i.getSpells())
        + ", "
        + boosts(i.getBoosts())
        + ", "
        + groups(i.getContainerLootGroups())
        + ", "
        + i.isUndroppable()
        + ");\n  }\n}\n";
  }

  private static String spells(List<ItemDefinition.ItemSpell> values) {
    if (values == null || values.isEmpty()) return "List.of()";
    StringBuilder s = new StringBuilder("List.of(");
    for (int n = 0; n < values.size(); n++) {
      if (n > 0) s.append(", ");
      ItemDefinition.ItemSpell v = values.get(n);
      s.append("new ItemDefinition.ItemSpell(")
          .append(v.getSpellId())
          .append(", ")
          .append(v.getLevel())
          .append(", ")
          .append(v.getChance())
          .append(")");
    }
    return s.append(")").toString();
  }

  private static String boosts(List<ItemDefinition.ItemBoost> values) {
    if (values == null || values.isEmpty()) return "List.of()";
    StringBuilder s = new StringBuilder("List.of(");
    for (int n = 0; n < values.size(); n++) {
      if (n > 0) s.append(", ");
      ItemDefinition.ItemBoost v = values.get(n);
      s.append("new ItemDefinition.ItemBoost(")
          .append(v.getBoostId())
          .append(", ")
          .append(v.getStatId())
          .append(", ")
          .append(lit(v.getExpression()))
          .append(", ")
          .append(v.getMinWis())
          .append(", ")
          .append(v.getMinInt())
          .append(")");
    }
    return s.append(")").toString();
  }

  private static String groups(List<ItemDefinition.ContainerLootGroup> values) {
    if (values == null || values.isEmpty()) return "List.of()";
    StringBuilder s = new StringBuilder("List.of(");
    for (int n = 0; n < values.size(); n++) {
      if (n > 0) s.append(", ");
      s.append("new ItemDefinition.ContainerLootGroup(List.of(");
      List<String> keys = values.get(n).getItemKeys();
      for (int j = 0; j < keys.size(); j++) {
        if (j > 0) s.append(", ");
        s.append(lit(keys.get(j)));
      }
      s.append("))");
    }
    return s.append(")").toString();
  }

  private static String enumValue(BodyPart v) {
    return v == null ? "null" : "BodyPart." + v.name();
  }

  private static String lit(String v) {
    if (v == null) return "null";
    return "\""
        + v.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n")
        + "\"";
  }

  private static String className(String key, int numId) {
    String raw = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
    StringBuilder s = new StringBuilder("Item");
    for (String p : raw.split("_"))
      if (!p.isEmpty()) s.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
    return s.append("_").append(Math.max(0, numId)).toString();
  }
}
