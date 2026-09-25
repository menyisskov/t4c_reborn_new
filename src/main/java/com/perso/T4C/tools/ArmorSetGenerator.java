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
 * grant it plus attack/archery and an endurance bonus. Every flavor also resists all five
 * non-light elements (T4C-0030: no item ever grants light resistance, light-flavored gear
 * included - see {@link ItemBalance}'s class doc).
 *
 * <p>After the two tiers it also writes the single themed sets in {@link #THEMED_SETS} (Centaur
 * Slaying, Drowned Inquisition, Cinderforged) with the same split and rules. Every key prefix
 * this generator writes is listed in {@link ItemBalance#GENERATED_SET_PREFIXES}; add a new set's
 * prefix there too.
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
      double platemailAc,
      int structure) {
    Piece(
        String slotWord,
        String bodyPart,
        String secondaryBodyPart,
        String appearanceEquippedPrimary,
        String appearanceEquippedSecondary,
        String appearanceInventory,
        long weight,
        int appearanceId,
        double platemailAc) {
      this(
          slotWord,
          bodyPart,
          secondaryBodyPart,
          appearanceEquippedPrimary,
          appearanceEquippedSecondary,
          appearanceInventory,
          weight,
          appearanceId,
          platemailAc,
          2);
    }
  }

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

  /**
   * An archer set's seventh piece: a quiver (off-hand, quiver structure so a bow can fire with
   * it). It has no Armor Class of its own (the off-hand slot carries none), but it takes a
   * belt-sized share of the set's bonus split so it isn't an empty slot, plus a flat
   * weapon-damage flavor bonus (see {@link ThemedSet#quiverDamageBonus}).
   */
  private static final Piece QUIVER =
      new Piece(
          "quiver",
          "WEAPON2",
          null,
          null,
          null,
          "64kIconQuiver",
          3,
          0,
          12.70,
          com.perso.T4C.config.GameConstants.QUIVER_STRUCTURE_ID);

  private static final List<Piece> PIECES_WITH_QUIVER;

  static {
    List<Piece> withQuiver = new ArrayList<>(PIECES);
    withQuiver.add(QUIVER);
    PIECES_WITH_QUIVER = List.copyOf(withQuiver);
  }

  /**
   * A single named set outside the two 8-flavor tiers (T4C content pass after T4C-0037): one
   * flavor, its own key prefix ({@code <tier.keyPrefix>_<slotWord>}) and hand-picked display
   * names per piece. Same proportional split and {@link ItemBalance} rules as the tiers; on top,
   * {@code doubledResistStatId} (0 for none) doubles the set's flat resistance total in its theme
   * element - the "doubled theme resistance" flavor extra from DESIGN_GUIDELINES.md (never
   * light) - and {@code quiverDamageBonus} is the quiver's flat weapon-damage extra.
   */
  private record ThemedSet(
      Tier tier,
      String flavor,
      String classStat,
      List<Piece> pieces,
      Map<String, String> pieceNames,
      int doubledResistStatId,
      int quiverDamageBonus) {}

  /** Resistance per element for the themed sets scales with the endurance requirement the same
   * way the two tiers do: legacy per-piece base about endurance / 20 and flat total about
   * endurance * 0.175 (tier 1: 400 -> 20/70, tier 2: 550 -> 30/100). Power is three single
   * items' worth, P/10 x 3. */
  private static final List<ThemedSet> THEMED_SETS =
      List.of(
          // Archer set matching the Bow of Centaur Slaying (Windhowl Marches, Centaur King).
          // Trades Empyrean's endurance/resistance for more agility/archery.
          new ThemedSet(
              new Tier("Centaur Slaying", "centaur_slaying", 500, 0, 0, 0, 600, 25, 90, 0, 80),
              "archer",
              "agility",
              PIECES_WITH_QUIVER,
              Map.of(
                  "armor", "Armor of Centaur Slaying",
                  "boots", "Boots of Centaur Slaying",
                  "gauntlets", "Gauntlets of Centaur Slaying",
                  "helmet", "Helm of Centaur Slaying",
                  "leggings", "Leggings of Centaur Slaying",
                  "protector", "Belt of Centaur Slaying",
                  "quiver", "Quiver of Centaur Slaying"),
              0,
              15),
          // Water intelligence set for The Sunken Chancel (level 38-50), doubled dark resistance
          // for the drowned priesthood's undead side.
          new ThemedSet(
              new Tier("Drowned Inquisition", "drowned_inquisition", 200, 180, 45, 0, 0, 10, 35, 54, 0),
              "water",
              null,
              PIECES,
              Map.of(
                  "armor", "Drowned Inquisition Vestments",
                  "boots", "Drowned Inquisition Boots",
                  "gauntlets", "Drowned Inquisition Gloves",
                  "helmet", "Drowned Inquisition Hood",
                  "leggings", "Drowned Inquisition Leggings",
                  "protector", "Drowned Inquisition Sash"),
              22,
              0),
          // Fire intelligence set for Cinderreach Hills (level 58-70), doubled fire resistance.
          new ThemedSet(
              new Tier("Cinderforged", "cinderforged", 280, 240, 60, 0, 0, 14, 50, 72, 0),
              "fire",
              null,
              PIECES,
              Map.of(
                  "armor", "Cinderforged Hauberk",
                  "boots", "Cinderforged Boots",
                  "gauntlets", "Cinderforged Gauntlets",
                  "helmet", "Cinderforged Helm",
                  "leggings", "Cinderforged Leggings",
                  "protector", "Cinderforged Girdle"),
              13,
              0));

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
    // Themed sets come after the two tiers so their boost ids continue the same counter
    // without renumbering (and so rewriting) any of the 96 tier pieces.
    for (ThemedSet set : THEMED_SETS) {
      written += writeSet(outputDir, gson, set);
    }
    System.out.println("Wrote " + written + " item JSON files to " + outputDir);
  }

  private static int writeSet(
      String outputDir, Gson gson, Tier tier, String flavor, String classStat) throws IOException {
    return writeSet(
        outputDir, gson, new ThemedSet(tier, flavor, classStat, PIECES, null, 0, 0));
  }

  private static int writeSet(String outputDir, Gson gson, ThemedSet set) throws IOException {
    Tier tier = set.tier();
    String flavor = set.flavor();
    String classStat = set.classStat();
    List<Piece> pieces = set.pieces();
    boolean themed = set.pieceNames() != null;
    double totalAc = pieces.stream().mapToDouble(Piece::platemailAc).sum();
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
      int flatTotal =
          element.resistStatId() == set.doubledResistStatId()
              ? tier.flatResistTotal() * 2
              : tier.flatResistTotal();
      resistSplitByElement.put(element, splitByWeight(flatTotal, pieces, totalAc));
    }
    int[] powerSplit = splitByWeight(tier.powerTotal(), pieces, totalAc);
    int[] mainStatSplit = splitByWeight(mainStatTotal, pieces, totalAc);
    int[] skillSplit = splitByWeight(skillTotal, pieces, totalAc);
    int[] classEnduranceSplit = splitByWeight(tier.classEnduranceTotal(), pieces, totalAc);

    int written = 0;
    for (int i = 0; i < pieces.size(); i++) {
      Piece piece = pieces.get(i);
      com.perso.T4C.item.json.ItemJsonDef json = new com.perso.T4C.item.json.ItemJsonDef();
      String flavorLabel = capitalize(flavor);
      json.key =
          themed
              ? tier.keyPrefix() + "_" + piece.slotWord()
              : tier.keyPrefix() + "_" + flavor + "_" + piece.slotWord();
      json.name =
          themed
              ? set.pieceNames().get(piece.slotWord())
              : tier.namePrefix() + " " + flavorLabel + " " + capitalize(piece.slotWord());
      json.structure = piece.structure();
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
        // No item ever grants light resistance (ItemBalance's "never light" rule) - light power
        // is still fine (see themedElement below), only light resist is off-limits.
        if (element.resistStatId() == ItemBalance.LIGHT_RESIST_STAT_ID) continue;
        int legacyBase =
            element.legacy()
                    && !piece.bodyPart().equals("BELT")
                    && !piece.bodyPart().equals("WEAPON2")
                ? tier.legacyResistPerPiece()
                : 0;
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
      if (piece == QUIVER && set.quiverDamageBonus() > 0) {
        boosts.add(boost(nextBoostId++, 10, set.quiverDamageBonus()));
      }
      json.boosts = boosts;

      String fileName = json.key + ".json";
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

  private static int[] splitByWeight(int total, List<Piece> pieces, double totalAc) {
    double[] raw = new double[pieces.size()];
    for (int i = 0; i < pieces.size(); i++) {
      raw[i] = total * (pieces.get(i).platemailAc() / totalAc);
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
