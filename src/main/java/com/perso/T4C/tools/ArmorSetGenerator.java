package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perso.T4C.item.ItemBalance;
import com.perso.T4C.player.BodyPart;
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
 *
 * <p>T4C-0027: every flavor follows {@link ItemBalance}. AC comes from the piece's slot, the
 * tier's endurance requirement and the flavor's class; fire/water/dark flavors require and grant
 * intelligence, earth/light wisdom, air both; warrior/archer flavors require strength/agility and
 * grant it plus attack/archery and an endurance bonus. Every flavor also resists all six elements.
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

  /**
   * {@code magePrimary}/{@code mageSecondary}: an intelligence (fire/water/dark) or wisdom
   * (earth/light) flavor's main and other casting-stat requirement; {@code hybridEach} is air's
   * intelligence and wisdom requirement; {@code classPrimary} is warrior strength / archer agility.
   * Every other bonus total is a full set's worth (three single items' budget from
   * {@link ItemBalance}); flatResistTotal/powerTotal/classEnduranceTotal are the set's own
   * elemental-resistance, themed-power and warrior/archer endurance totals.
   */
  private record Tier(
      String namePrefix,
      String keyPrefix,
      long minEnd,
      long magePrimary,
      long mageSecondary,
      long hybridEach,
      long classPrimary,
      int legacyResistPerPiece,
      int flatResistTotal,
      int powerTotal,
      int classEnduranceTotal) {}

  private static final int SET_BUDGET_ITEMS = 3;

  private static final Tier TIER1 =
      new Tier("Ancient Celestial", "ancient_celestial", 400, 300, 75, 190, 350, 20, 70, 100, 60);
  private static final Tier TIER2 =
      new Tier("Empyrean", "empyrean", 550, 450, 110, 280, 500, 30, 100, 150, 90);

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
    boolean isElemental = classStat == null;
    Element themedElement =
        isElemental
            ? ELEMENTS.stream().filter(e -> e.key().equals(flavor)).findFirst().orElseThrow()
            : null;

    long str = 0, agi = 0, intel = 0, wis = 0;
    if (!isElemental) {
      if (classStat.equals("strength")) str = tier.classPrimary();
      else agi = tier.classPrimary();
    } else if (flavor.equals("air")) {
      intel = tier.hybridEach();
      wis = tier.hybridEach();
    } else if (flavor.equals("earth") || flavor.equals("light")) {
      intel = tier.mageSecondary();
      wis = tier.magePrimary();
    } else {
      intel = tier.magePrimary();
      wis = tier.mageSecondary();
    }
    ItemBalance.Archetype archetype = ItemBalance.archetype(str, agi, intel, wis, false);
    double p = ItemBalance.primaryRequirement(archetype, str, agi, intel, wis);
    int mainStatTotal = SET_BUDGET_ITEMS * ItemBalance.mainStatBonus(archetype, p);
    int skillTotal = SET_BUDGET_ITEMS * ItemBalance.combatSkillBonus(p);

    Map<Element, int[]> resistSplitByElement = new LinkedHashMap<>();
    for (Element element : ELEMENTS) {
      resistSplitByElement.put(element, splitByWeight(tier.flatResistTotal(), totalAc));
    }
    int[] powerSplit = splitByWeight(tier.powerTotal(), totalAc);
    int[] mainStatSplit = splitByWeight(mainStatTotal, totalAc);
    int[] skillSplit = splitByWeight(skillTotal, totalAc);
    int[] classEnduranceSplit = splitByWeight(tier.classEnduranceTotal(), totalAc);

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
      json.armorClass =
          ItemBalance.expectedArmorClass(
              BodyPart.valueOf(piece.bodyPart()), tier.minEnd(), archetype);
      json.dodgeLost = 0L;
      json.requirements.endurance = tier.minEnd();
      json.requirements.strength = str;
      json.requirements.agility = agi;
      json.requirements.intelligence = intel;
      json.requirements.wisdom = wis;
      json.appearanceId = piece.appearanceId();
      json.undroppable = false;

      List<com.perso.T4C.item.json.ItemJsonDef.BoostJson> boosts = new ArrayList<>();
      for (Element element : ELEMENTS) {
        int legacyBase =
            element.legacy() && !piece.bodyPart().equals("BELT") ? tier.legacyResistPerPiece() : 0;
        int total = legacyBase + resistSplitByElement.get(element)[i];
        if (total > 0) {
          boosts.add(boost(nextBoostId++, element.resistStatId(), total));
        }
      }
      int mainStat = mainStatSplit[i];
      if (isElemental) {
        if (powerSplit[i] > 0) {
          boosts.add(boost(nextBoostId++, themedElement.powerStatId(), powerSplit[i]));
        }
        if (mainStat > 0) {
          if (intel > 0 && archetype != ItemBalance.Archetype.WIS_MAGE)
            boosts.add(boost(nextBoostId++, 1, mainStat));
          if (wis > 0 && archetype != ItemBalance.Archetype.INT_MAGE)
            boosts.add(boost(nextBoostId++, 4, mainStat));
        }
      } else {
        boolean warrior = classStat.equals("strength");
        if (mainStat > 0) boosts.add(boost(nextBoostId++, warrior ? 3 : 6, mainStat));
        if (skillSplit[i] > 0) boosts.add(boost(nextBoostId++, warrior ? 8 : 10035, skillSplit[i]));
        if (classEnduranceSplit[i] > 0) boosts.add(boost(nextBoostId++, 2, classEnduranceSplit[i]));
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
