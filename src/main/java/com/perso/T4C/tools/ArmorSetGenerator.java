package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One-off generator that produces the 96 JSON item files for the two new "Ancient Celestial
 * Plate" (tier 1, x2 of Ancient Platemail) and "Empyrean Platemail" (tier 2, x3) armor sets, each
 * in 8 flavors (6 elemental + warrior + archer), 6 pieces per set. Every bonus total is split
 * across the 6 pieces proportional to that piece's share of the set's total Armor Class (the same
 * weighting Ancient Platemail's own AC distribution implies), using largest-remainder rounding so
 * each stat's declared total is hit exactly once the full set is worn.
 */
public final class ArmorSetGenerator {
  private ArmorSetGenerator() {}

  private static int nextBoostId = 20000;

  private record Piece(
      String slotWord,
      String bodyPart,
      String secondaryBodyPart,
      String appearanceEquippedPrimary,
      String appearanceEquippedSecondary,
      String appearanceInventory,
      long weight,
      int appearanceId,
      double platemailAc) {}

  private record Element(String key, String label, int resistStatId, int powerStatId, boolean legacy) {}

  private static final List<Piece> PIECES =
      List.of(
          new Piece("armor", "BODY", null, "PupPlateBody", null, "64kInvPlateArmorSleeves", 8, 264, 57.25),
          new Piece("boots", "FEET", null, "PupPlateFoot", null, "64kInvPlateArmorFeet", 3, 265, 17.145),
          new Piece(
              "gauntlets",
              "LEFT_HAND",
              "RIGHT_HAND",
              "PupPlateGloveL",
              "PupPlateGloveR",
              "64kInvPlateGlove",
              3,
              263,
              17.145),
          new Piece("helmet", "HEAD", null, "PupPlateHelm", null, "64kInvPlateArmorHelm", 3, 267, 16.51),
          new Piece("leggings", "LEGS", null, "PupPlateLegs", null, "64kInvPlateArmorLegs", 5, 266, 19.05),
          new Piece("protector", "BELT", null, null, null, "64kInvBelt", 2, 235, 12.70));

  private static final List<Element> ELEMENTS =
      List.of(
          new Element("air", "Air", 12, 16, true),
          new Element("fire", "Fire", 13, 17, true),
          new Element("water", "Water", 14, 18, true),
          new Element("earth", "Earth", 15, 19, true),
          new Element("light", "Light", 21, 23, false),
          new Element("dark", "Dark", 22, 24, true));

  private record Tier(
      String namePrefix,
      String keyPrefix,
      int acMultiplier,
      long minEnd,
      long minInt,
      long minWis,
      int flatResistTotal,
      int powerTotal,
      int statTotal,
      int comboTotal) {}

  private static final Tier TIER1 =
      new Tier("Ancient Celestial", "ancient_celestial", 2, 400, 150, 150, 70, 100, 100, 750);
  private static final Tier TIER2 =
      new Tier("Empyrean", "empyrean", 3, 550, 200, 200, 100, 150, 150, 1000);

  private static final List<String> ELEMENTAL_FLAVORS =
      List.of("fire", "dark", "water", "air", "earth", "light");

  public static void main(String[] args) throws IOException {
    String outputDir = args.length > 0 ? args[0] : "assets/items";
    new java.io.File(outputDir).mkdirs();
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    int written = 0;
    for (Tier tier : List.of(TIER1, TIER2)) {
      for (String flavor : ELEMENTAL_FLAVORS) {
        written += writeSet(outputDir, gson, tier, flavor, null);
      }
      written += writeSet(outputDir, gson, tier, "warrior", "strength");
      written += writeSet(outputDir, gson, tier, "archer", "agility");
    }
    System.out.println("Wrote " + written + " item JSON files to " + outputDir);
  }

  private static int writeSet(
      String outputDir, Gson gson, Tier tier, String flavor, String classStat) throws IOException {
    double totalAc = PIECES.stream().mapToDouble(Piece::platemailAc).sum();
    Map<Element, int[]> resistSplitByElement = new LinkedHashMap<>();
    for (Element element : ELEMENTS) {
      resistSplitByElement.put(element, splitByWeight(tier.flatResistTotal(), totalAc));
    }
    int[] powerSplit = splitByWeight(tier.powerTotal(), totalAc);
    int[] statSplit = splitByWeight(tier.statTotal(), totalAc);
    int[] comboSplit = splitByWeight(tier.comboTotal(), totalAc);

    boolean isElemental = classStat == null;
    Element themedElement =
        isElemental
            ? ELEMENTS.stream().filter(e -> e.key().equals(flavor)).findFirst().orElseThrow()
            : null;

    int written = 0;
    for (int i = 0; i < PIECES.size(); i++) {
      Piece piece = PIECES.get(i);
      com.perso.T4C.item.json.ItemJsonDef json = new com.perso.T4C.item.json.ItemJsonDef();
      String flavorLabel = capitalize(flavor);
      json.key = tier.keyPrefix() + "_" + flavor + "_" + piece.slotWord();
      json.name = tier.namePrefix() + " " + flavorLabel + " " + capitalize(piece.slotWord());
      json.bodyPart = piece.bodyPart();
      json.secondaryBodyPart = piece.secondaryBodyPart();
      json.appearanceEquippedPrimary = piece.appearanceEquippedPrimary();
      json.appearanceEquippedSecondary = piece.appearanceEquippedSecondary();
      json.appearanceInventory = piece.appearanceInventory();
      json.price = 0L;
      json.weight = piece.weight();
      json.armorClass = round(piece.platemailAc() * tier.acMultiplier());
      json.dodgeLost = 0L;
      json.requirements.endurance = tier.minEnd();
      json.requirements.intelligence = tier.minInt();
      json.requirements.wisdom = tier.minWis();
      json.appearanceId = piece.appearanceId();
      json.undroppable = false;

      if (!isElemental) {
        if (classStat.equals("strength")) {
          json.requirements.strength = 500L;
        } else {
          json.requirements.agility = 500L;
        }
      }

      List<com.perso.T4C.item.json.ItemJsonDef.BoostJson> boosts = new ArrayList<>();
      for (Element element : ELEMENTS) {
        int legacyBase =
            element.legacy() && !piece.bodyPart().equals("BELT")
                ? 10 * tier.acMultiplier()
                : 0;
        int extra = resistSplitByElement.get(element)[i];
        int total = legacyBase + extra;
        if (total > 0) {
          boosts.add(boost(nextBoostId++, element.resistStatId(), total));
        }
      }
      if (isElemental) {
        int power = powerSplit[i];
        if (power > 0) {
          boosts.add(boost(nextBoostId++, themedElement.powerStatId(), power));
        }
      } else {
        int stat = statSplit[i];
        int combo = comboSplit[i];
        if (stat > 0) {
          boosts.add(boost(nextBoostId++, classStat.equals("strength") ? 3 : 6, stat));
        }
        if (combo > 0) {
          boosts.add(boost(nextBoostId++, classStat.equals("strength") ? 8 : 10035, combo));
        }
      }
      json.boosts = boosts;

      String fileName = tier.keyPrefix() + "_" + flavor + "_" + piece.slotWord() + ".json";
      try (Writer writer =
          new OutputStreamWriter(
              new FileOutputStream(outputDir + "/" + fileName), StandardCharsets.UTF_8)) {
        gson.toJson(json, writer);
      }
      written++;
    }
    return written;
  }

  private static com.perso.T4C.item.json.ItemJsonDef.BoostJson boost(int boostId, int statId, int value) {
    com.perso.T4C.item.json.ItemJsonDef.BoostJson b = new com.perso.T4C.item.json.ItemJsonDef.BoostJson();
    b.boostId = boostId;
    b.statId = statId;
    b.expression = String.valueOf(value);
    return b;
  }

  private static int[] splitByWeight(int total, double totalAc) {
    double[] raw = new double[PIECES.size()];
    for (int i = 0; i < PIECES.size(); i++) {
      raw[i] = total * (PIECES.get(i).platemailAc() / totalAc);
    }
    int[] floors = new int[raw.length];
    int sumFloors = 0;
    for (int i = 0; i < raw.length; i++) {
      floors[i] = (int) Math.floor(raw[i]);
      sumFloors += floors[i];
    }
    int remainder = total - sumFloors;
    Integer[] order = new Integer[raw.length];
    for (int i = 0; i < order.length; i++) order[i] = i;
    java.util.Arrays.sort(order, (a, b) -> Double.compare(raw[b] - floors[b], raw[a] - floors[a]));
    for (int i = 0; i < remainder; i++) {
      floors[order[i]]++;
    }
    return floors;
  }

  private static double round(double v) {
    return Math.round(v * 1000.0) / 1000.0;
  }

  private static String capitalize(String s) {
    return s.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + s.substring(1);
  }
}
