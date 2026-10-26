package com.perso.T4C.tools;

import com.perso.T4C.combat.SeraphAuraService;
import com.perso.T4C.config.GameConstants;
import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.helper.XpCurve;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterExperience;
import com.perso.T4C.monster.core.MonsterRegistry;
import com.perso.T4C.monster.json.MonsterJsonLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Runtime-backed XP budget, not a combat simulation or a promise of observed leveling speed. */
public final class HuntingExperienceReport {
  private record Stage(int from, String monster) {}

  private record ObservedFarm(String monster, long previousFullKill) {}

  // An accessible progression through the established hunting fields; no boss farming.
  private static final List<Stage> ROUTE =
      List.of(
          new Stage(150, "Barrow Wight"),
          new Stage(175, "Kraanian Wyrmling"),
          new Stage(200, "Fey Warden"),
          new Stage(225, "Moonwake Revenant"),
          new Stage(250, "Veilbound Wraith"),
          new Stage(275, "Sundered Sentinel"),
          new Stage(300, "Ashbound Exile"),
          new Stage(340, "Nullguard"),
          new Stage(375, "Rift Wraith"));

  private HuntingExperienceReport() {}

  public static void main(String[] args) throws Exception {
    MonsterJsonLoader.loadAndRegister();
    var curve = XpCurve.loadDefault();
    var report = new StringBuilder("# Hunting experience through level 400\n\n");
    report.append(
        "Target: 5-10 hours of appropriate solo hunting per 100 levels, including 300-400. "
            + "This budget uses the runtime monster registry and level requirements. It is not "
            + "a measured combat simulation; build, hit chance, defenses, grouping and travel "
            + "determine actual time. No quests, personal XP bonuses or bosses are counted.\n\n");
    report.append(
        "The Fantasy addon table is the scale reference: "
            + "[T4C Fantasy Add-Ons](https://www.t4cfantasy.com/Addon). "
            + "Our curve extends that scale through our level-400 cap.\n\n");
    report.append(
        "## Live calibration\n\n"
            + "The owner reported going from level 234 to 274 in about ten minutes while "
            + "killing Fey Wardens and Moonlit Stalkers on 26 October 2026. The kill count "
            + "and mixture were not recorded. The table below scales that observed pace "
            + "by each farm's old and current full-kill rewards. It assumes the same kill "
            + "rate and personal bonuses; it is a conditional projection, not a measured "
            + "post-change result or a forecast for every later region.\n\n"
            + "| Farm | Previous full-kill XP | Current full-kill XP | Hours per 100 levels at the observed kill rate |\n"
            + "| --- | ---: | ---: | ---: |\n");
    for (var observed :
        List.of(
            new ObservedFarm("Fey Warden", 392310600L),
            new ObservedFarm("Moonlit Stalker", 499437808L))) {
      long current =
          fullKill(
              java.util.Objects.requireNonNull(MonsterRegistry.findByName(observed.monster())));
      double hours = 10d / 60 * 100 / 40 * observed.previousFullKill() / current;
      report.append(
          String.format(
              Locale.ROOT,
              "| %s | %,d | %,d | %.2f |%n",
              observed.monster(),
              observed.previousFullKill(),
              current,
              hours));
    }
    report.append('\n');
    report.append(
        "## Ordinary hunting rewards\n\n"
            + "XP includes the combat and server multipliers, before personal bonuses. "
            + "A full kill includes damage XP for exactly one health bar, without regeneration.\n\n"
            + "| Monster | Level | HP | Death XP | Full-kill XP |\n"
            + "| --- | ---: | ---: | ---: | ---: |\n");
    for (var stage : ROUTE) {
      var monster = monster(stage);
      report.append(
          String.format(
              Locale.ROOT,
              "| %s | %d | %,d | %,d | %,d |%n",
              monster.getName(),
              monster.getLevel(),
              monster.getHealth(),
              scaled(MonsterExperience.reward(monster.getXpOnDeath())),
              fullKill(monster)));
    }
    appendPackSurvivability(report);
    report.append(
        "\n## Progression budget\n\n"
            + "Each level starts at zero progress and rounds its kills up independently; "
            + "real XP overflow carries into the next level, so this slightly overstates kills. "
            + "The sustained DPS ranges below divide total HP by the target session time. "
            + "Damage is summed across all targets and the whole session, including "
            + "movement and downtime. No arbitrary pack-size assumption is used. These "
            + "are planning requirements, not measured build performance; the live "
            + "observation above is the calibration evidence.\n\n"
            + "| Levels | Kills | HP to defeat | Sustained DPS for 10 hours | Sustained DPS for 5 hours |\n"
            + "| --- | ---: | ---: | ---: | ---: |\n");
    for (int[] band : new int[][] {{150, 200}, {200, 300}, {300, 400}}) {
      long kills = 0;
      long health = 0;
      for (int level = band[0]; level < band[1]; level++) {
        var monster = monster(stageFor(level));
        long count = Math.ceilDiv(curve.getXpToNextLevel(level), fullKill(monster));
        kills += count;
        health += count * monster.getHealth();
      }
      // The 150-200 row covers half of a hundred-level target interval.
      double hours = (band[1] - band[0]) / 100d;
      report.append(
          String.format(
              Locale.ROOT,
              "| %d-%d | %,d | %,d | %,.0f | %,.0f |%n",
              band[0],
              band[1],
              kills,
              health,
              requiredSustainedDps(health, hours * 10),
              requiredSustainedDps(health, hours * 5)));
    }
    Path output = Path.of(args.length == 0 ? "docs/hunting-experience.md" : args[0]);
    Files.writeString(output, report);
    System.out.println("Wrote " + output);
  }

  private static Stage stageFor(int level) {
    Stage result = ROUTE.getFirst();
    for (var stage : ROUTE) {
      if (stage.from() > level) break;
      result = stage;
    }
    return result;
  }

  private static void appendPackSurvivability(StringBuilder report) {
    var wraith = java.util.Objects.requireNonNull(MonsterRegistry.findByName("Veilbound Wraith"));
    var formula = DiceFormula.of(wraith.getAttacks().getFirst().getName());
    int low = formula.min();
    int high = formula.max();
    int armor = 432;
    int health = 9000;
    double mean = 0;
    for (int raw = low; raw <= high; raw++) mean += Math.max(0, raw - armor);
    mean /= high - low + 1;
    double dps = 14 * mean / GameConstants.MONSTER_ATTACK_COOLDOWN;
    report.append("\n## Dense-pack survivability\n\n");
    report.append(
        "The owner reported 12-14 simultaneous attackers and survival dependent on a "
            + "fifty-rebirth healing aura. A live Veilbound Wraith log on 26 October 2026 "
            + "contained 14,586 post-mitigation damage in one two-second window against "
            + "10,465 HP. The earlier XP calibration did not establish survivability. "
            + "Preserve dense fields while reducing ordinary melee outliers.\n\n");
    report.append(
        String.format(
            Locale.ROOT,
            "The runtime Wraith roll is now %,d-%,d raw damage. With the separately reported "
                + "%,d HP / %d AC baseline, fourteen Wraiths landing every attack at the unchanged "
                + "%.1f-second cooldown deal about %,.0f average damage per second before any "
                + "healing. That is %.1f seconds of mean HP drain, excluding the initial volley's "
                + "timing, misses, kills, movement and other defenses. Two complete maximum-damage "
                + "volleys deal at most %,d damage. This is an analytical reaction-window benchmark, "
                + "not a measured fight or a promise to tank indefinitely.\n\n",
            low,
            high,
            health,
            armor,
            GameConstants.MONSTER_ATTACK_COOLDOWN,
            dps,
            dps == 0 ? 0 : health / dps,
            2 * 14 * Math.max(0, high - armor)));
    report.append("| Rebirths | Healing trigger chance per landed hit |\n| --- | ---: |\n");
    for (int rebirths : new int[] {10, 15, 50}) {
      report.append(
          String.format(
              Locale.ROOT,
              "| %d | %.1f%% |%n",
              rebirths,
              SeraphAuraService.effectivePercent(SeraphAuraService.healingChance(rebirths))));
    }
    report.append(
        "\nThese are trigger odds only; healing amount, stats and overhealing also "
            + "differ between builds. The fifty-rebirth aura is not the ordinary-farm baseline.\n");
  }

  private static MonsterDef monster(Stage stage) {
    return java.util.Objects.requireNonNull(
        MonsterRegistry.findByName(stage.monster()), stage.monster());
  }

  private static long fullKill(MonsterDef monster) {
    return scaled(
        MonsterExperience.reward(monster.getXpOnDeath())
            + MonsterExperience.damageReward(monster.getXpPerHit(), monster.getHealth()));
  }

  private static long scaled(long experience) {
    return Math.round(experience * (double) GameConstants.SERVER_XP_RATE);
  }

  private static double requiredSustainedDps(long health, double hours) {
    return health / (hours * 3600);
  }
}
