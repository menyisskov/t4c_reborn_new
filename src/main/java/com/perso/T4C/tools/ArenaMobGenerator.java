package com.perso.T4C.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Extends the Colosseum arena's ArenaMobXP monster ladder from level 500 up to 750 (step 25,
 * matching the existing 300-500 spacing). Every scalable stat is extrapolated with an ordinary
 * least-squares linear fit computed from the 9 existing data points (levels 300, 325, ..., 500),
 * exactly as requested: "increase their stats linearly based on current linear equation". Note
 * that health and xpOnDeath are actually slightly convex (accelerating) in the original data, so
 * the linear fit slightly undershoots what a quadratic continuation would give — that's expected
 * and intentional given the "linear" instruction.
 */
public final class ArenaMobGenerator {
  private ArenaMobGenerator() {}

  private record DataPoint(
      int level,
      int health,
      long xpOnDeath,
      int diceMax,
      int diceBonus,
      long acMaxRaw) {}

  // Levels 300-500, step 25: the clean, evenly-spaced portion of the existing ladder.
  private static final List<DataPoint> DATA =
      List.of(
          new DataPoint(300, 21361, 1996275, 515, 405, 1125515264L),
          new DataPoint(325, 24716, 2781103, 559, 438, 1126301696L),
          new DataPoint(350, 28306, 3757615, 601, 472, 1127153664L),
          new DataPoint(375, 32144, 4953688, 644, 506, 1127940096L),
          new DataPoint(400, 36217, 6396077, 687, 540, 1128792064L),
          new DataPoint(425, 40540, 8113191, 729, 574, 1129578496L),
          new DataPoint(450, 45097, 10138310, 772, 608, 1130430464L),
          new DataPoint(475, 49903, 12501070, 816, 641, 1131216896L),
          new DataPoint(500, 54943, 15236180, 858, 675, 1132068864L));

  private static final int[] NEW_LEVELS = {525, 550, 575, 600, 625, 650, 675, 700, 725, 750};

  public static void main(String[] args) throws IOException {
    String outputDir = args.length > 0 ? args[0] : "assets/monsters";
    new java.io.File(outputDir).mkdirs();
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    double[] healthFit = fit(DATA.stream().mapToDouble(DataPoint::level).toArray(),
        DATA.stream().mapToDouble(DataPoint::health).toArray());
    double[] xpFit = fit(DATA.stream().mapToDouble(DataPoint::level).toArray(),
        DATA.stream().mapToDouble(d -> d.xpOnDeath()).toArray());
    double[] diceMaxFit = fit(DATA.stream().mapToDouble(DataPoint::level).toArray(),
        DATA.stream().mapToDouble(DataPoint::diceMax).toArray());
    double[] diceBonusFit = fit(DATA.stream().mapToDouble(DataPoint::level).toArray(),
        DATA.stream().mapToDouble(DataPoint::diceBonus).toArray());
    double[] acFit = fit(DATA.stream().mapToDouble(DataPoint::level).toArray(),
        DATA.stream().mapToDouble(d -> Float.intBitsToFloat((int) d.acMaxRaw())).toArray());

    System.out.printf(
        "health = %.4f*level + %.4f%n", healthFit[0], healthFit[1]);
    System.out.printf("xpOnDeath = %.4f*level + %.4f%n", xpFit[0], xpFit[1]);
    System.out.printf("diceMax = %.4f*level + %.4f%n", diceMaxFit[0], diceMaxFit[1]);
    System.out.printf("diceBonus = %.4f*level + %.4f%n", diceBonusFit[0], diceBonusFit[1]);
    System.out.printf("armorClass(decoded) = %.6f*level + %.6f%n", acFit[0], acFit[1]);

    int written = 0;
    for (int level : NEW_LEVELS) {
      int health = (int) Math.round(healthFit[0] * level + healthFit[1]);
      long xp = Math.round(xpFit[0] * level + xpFit[1]);
      int diceMax = (int) Math.round(diceMaxFit[0] * level + diceMaxFit[1]);
      int diceBonus = (int) Math.round(diceBonusFit[0] * level + diceBonusFit[1]);
      float acDecoded = (float) (acFit[0] * level + acFit[1]);
      long acRaw = Float.floatToIntBits(acDecoded) & 0xFFFFFFFFL;

      int str = level + 15;
      int endAgiWisWill = (int) Math.round(0.9 * level + 14);
      int intel = (int) Math.round(1.2 * level + 15);
      int luck = (int) Math.round(0.2 * level + 15);
      int dodge = 4 * level + 10;
      int combatAttack = 12 * level + 10;

      Map<String, Object> json = new java.util.LinkedHashMap<>();
      json.put("name", "ArenaMobXP" + level);
      json.put("displayName", "${monster.arenamobxp" + level + "}");
      json.put("health", health);
      json.put("xpPerHit", 0);
      json.put("xpOnDeath", xp);
      json.put("hitDamageMin", 1);
      json.put("hitDamageMax", 4);
      json.put("respawnTime", 30000);
      json.put("walkPattern", "Agmorkian#h");
      json.put("attackPattern", "AgmorkianA#h");
      json.put("deathPattern", "AgmorkianC!p");
      json.put("soundAttack", "Kraanian Attack.wav");
      json.put("soundDeath", "Kraanian Dying.wav");
      json.put("soundHit", "Kraanian Hit.wav");
      json.put("gold", Map.of("min", 0, "max", 0));
      json.put("loot", List.of());
      json.put(
          "stats",
          Map.of(
              "str", str, "end", endAgiWisWill, "agi", endAgiWisWill, "intel", intel,
              "will", endAgiWisWill, "wis", endAgiWisWill, "luck", luck));
      json.put("resists", new int[] {46, 94, 46, 94, 70, 5000, 100, 100, 100, 100, 100, 100});
      json.put("level", level);
      json.put("dodge", dodge);
      json.put("armorClass", Map.of("min", 0, "max", acRaw));
      json.put("appearance", 20036);
      json.put("aggro", 100);
      json.put("clan", 0);
      json.put("canAttack", true);
      json.put(
          "attacks",
          List.of(
              Map.of(
                  "formula", "1d " + diceMax + " + " + diceBonus + " ",
                  "combatAttack", combatAttack,
                  "percentage", 20)));
      json.put("tameable", false);
      json.put("tameMaxLevel", 0);
      json.put("spawnAliases", List.of());
      json.put(
          "sourceEvents",
          Map.of(
              "OnPopup",
              " \r\n  \tCastSpellSelf(\"spell.mob_arena_major_regeneration_spell\") \t\r\n\tSimpleMonster::OnPopup( UNIT_FUNC_PARAM );\r\n",
              "OnDeath",
              "\r\n    INIT_HANDLER\r\n\tif( target != NULL )\r\n\t{\r\n\t\tGiveItem(41702)\r\n\t\tPRIVATE_SYSTEM_MESSAGE(INTL( 10682, \"You receive a battle token for your efforts.\"))\r\n\t}\r\n    CLOSE_HANDLER\r\n\r\n\tCastSpellSelf(\"spell.mob_arena_level_spell\")\r\n\r\n\tSimpleMonster::OnDeath( UNIT_FUNC_PARAM );\r\n",
              "OnDestroy",
              "\r\n\tIF(CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) > 0)\r\n\t\tGiveGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA, CheckGlobalFlag(__GLOBAL_FLAG_NUMBER_MONSTERS_IN_ARENA) - 1)\r\n\tENDIF\r\n\r\n\tSimpleMonster::OnDestroy( UNIT_FUNC_PARAM );\r\n",
              "@spell.spell.mob_arena_level_spell",
              "GiveFlag(__FLAG_ARENA_LEVEL," + level + ")"));

      String fileName = "arenamobxp" + level + ".json";
      try (Writer writer =
          new OutputStreamWriter(
              new FileOutputStream(outputDir + "/" + fileName), StandardCharsets.UTF_8)) {
        gson.toJson(json, writer);
      }
      written++;
    }
    System.out.println("Wrote " + written + " arena mob JSON files to " + outputDir);
  }

  /** Ordinary least-squares linear fit. Returns {slope, intercept}. */
  private static double[] fit(double[] x, double[] y) {
    int n = x.length;
    double meanX = java.util.Arrays.stream(x).average().orElseThrow();
    double meanY = java.util.Arrays.stream(y).average().orElseThrow();
    double sxy = 0;
    double sxx = 0;
    for (int i = 0; i < n; i++) {
      sxy += (x[i] - meanX) * (y[i] - meanY);
      sxx += (x[i] - meanX) * (x[i] - meanX);
    }
    double slope = sxy / sxx;
    double intercept = meanY - slope * meanX;
    return new double[] {slope, intercept};
  }
}
