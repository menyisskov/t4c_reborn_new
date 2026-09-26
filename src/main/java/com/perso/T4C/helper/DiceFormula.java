package com.perso.T4C.helper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DiceFormula {
  public static final class Context {
    public final int str, end, agi, intel, wil, wis, luck, level;
    public final int arrowDmg;
    public final int targetResistDark, targetResistLight;
    public final int targetResistFire, targetResistEarth, targetResistAir, targetResistWater;
    public final int selfFire, selfEarth, selfAir, selfWater, selfLight, selfDark;

    /**
     * The caster's own <em>true</em> readings, keyed by the canonical {@code self.true_*} suffix
     * ("str", "dodge", "r_fire", ...). "True" means gear and permanent bonuses but not currently
     * active spell buffs, so a buff that reads its own stat refreshes instead of compounding. Empty
     * when the context was not built for a player; missing keys fall back to {@link
     * #selfTrueFallback}.
     */
    public final Map<String, Integer> selfTrue;

    public Context(int str, int end, int agi, int intel, int wil, int wis, int luck, int level) {
      this(str, end, agi, intel, wil, wis, luck, level, 0, 0, 0, 100);
    }

    public Context(
        int str,
        int end,
        int agi,
        int intel,
        int wil,
        int wis,
        int luck,
        int level,
        int arrowDmg,
        int targetResistDark,
        int targetResistLight) {
      this(
          str,
          end,
          agi,
          intel,
          wil,
          wis,
          luck,
          level,
          arrowDmg,
          targetResistDark,
          targetResistLight,
          100);
    }

    public Context(
        int str,
        int end,
        int agi,
        int intel,
        int wil,
        int wis,
        int luck,
        int level,
        int arrowDmg,
        int targetResistDark,
        int targetResistLight,
        int selfLight) {
      this(
          str,
          end,
          agi,
          intel,
          wil,
          wis,
          luck,
          level,
          arrowDmg,
          100,
          100,
          100,
          100,
          targetResistLight,
          targetResistDark,
          100,
          100,
          100,
          100,
          selfLight,
          100);
    }

    public Context(
        int str,
        int end,
        int agi,
        int intel,
        int wil,
        int wis,
        int luck,
        int level,
        int arrowDmg,
        int targetResistFire,
        int targetResistEarth,
        int targetResistAir,
        int targetResistWater,
        int targetResistLight,
        int targetResistDark,
        int selfFire,
        int selfEarth,
        int selfAir,
        int selfWater,
        int selfLight,
        int selfDark) {
      this.str = str;
      this.end = end;
      this.agi = agi;
      this.intel = intel;
      this.wil = wil;
      this.wis = wis;
      this.luck = luck;
      this.level = level;
      this.arrowDmg = arrowDmg;
      this.targetResistDark = targetResistDark;
      this.targetResistLight = targetResistLight;
      this.targetResistFire = targetResistFire;
      this.targetResistEarth = targetResistEarth;
      this.targetResistAir = targetResistAir;
      this.targetResistWater = targetResistWater;
      this.selfFire = selfFire;
      this.selfEarth = selfEarth;
      this.selfAir = selfAir;
      this.selfWater = selfWater;
      this.selfLight = selfLight;
      this.selfDark = selfDark;
      this.selfTrue = Map.of();
    }

    private Context(Context base, Map<String, Integer> selfTrue) {
      this.str = base.str;
      this.end = base.end;
      this.agi = base.agi;
      this.intel = base.intel;
      this.wil = base.wil;
      this.wis = base.wis;
      this.luck = base.luck;
      this.level = base.level;
      this.arrowDmg = base.arrowDmg;
      this.targetResistDark = base.targetResistDark;
      this.targetResistLight = base.targetResistLight;
      this.targetResistFire = base.targetResistFire;
      this.targetResistEarth = base.targetResistEarth;
      this.targetResistAir = base.targetResistAir;
      this.targetResistWater = base.targetResistWater;
      this.selfFire = base.selfFire;
      this.selfEarth = base.selfEarth;
      this.selfAir = base.selfAir;
      this.selfWater = base.selfWater;
      this.selfLight = base.selfLight;
      this.selfDark = base.selfDark;
      this.selfTrue = selfTrue;
    }

    /** Returns a copy of this context carrying the given true readings. */
    public Context withSelfTrue(Map<String, Integer> readings) {
      if (readings == null || readings.isEmpty()) return this;
      Map<String, Integer> canonical = new LinkedHashMap<>();
      readings.forEach((key, value) -> canonical.put(canonicalSelfTrueKey(key), value));
      return new Context(this, Collections.unmodifiableMap(canonical));
    }

    public static final Context ZERO = new Context(0, 0, 0, 0, 0, 0, 0, 0);
  }

  private final String original;

  private DiceFormula(String formula) {
    this.original = formula == null ? "" : formula.trim();
  }

  public static DiceFormula of(String formula) {
    return new DiceFormula(formula);
  }

  public int roll() {
    return roll(Context.ZERO);
  }

  public int roll(Context ctx) {
    if (original.isEmpty() || "0".equals(original)) return 0;
    try {
      return (int) Math.max(0, new Parser(prepare(original, ctx)).parseExpr());
    } catch (Exception e) {
      return 0;
    }
  }

  public int evaluate(Context ctx) {
    if (original.isEmpty() || "0".equals(original)) return 0;
    try {
      double value = new Parser(prepare(original, ctx)).parseExpr();
      if (!Double.isFinite(value)) return 0;
      return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, value));
    } catch (Exception e) {
      return 0;
    }
  }

  public int min() {
    return min(Context.ZERO);
  }

  public int min(Context ctx) {
    if (original.isEmpty() || "0".equals(original)) return 0;
    try {
      return (int) Math.max(0, new MinParser(prepare(original, ctx)).parseExpr());
    } catch (Exception e) {
      return 0;
    }
  }

  public int max() {
    return max(Context.ZERO);
  }

  public int max(Context ctx) {
    if (original.isEmpty() || "0".equals(original)) return 0;
    try {
      return (int) Math.max(0, new MaxParser(prepare(original, ctx)).parseExpr());
    } catch (Exception e) {
      return 0;
    }
  }

  public String getOriginal() {
    return original;
  }

  @Override
  public String toString() {
    return original;
  }

  private static String prepare(String raw, Context ctx) {
    String s = raw.replaceAll("\\s+", "");
    s = substituteVars(s, ctx);
    s = replaceDiceOp(s);
    return s;
  }

  private static String substituteVars(String s, Context ctx) {
    s = s.replace("target.true_r_dark", String.valueOf(ctx.targetResistDark));
    s = s.replace("target.true_r_light", String.valueOf(ctx.targetResistLight));
    s = s.replace("target.true_r_fire", String.valueOf(ctx.targetResistFire));
    s = s.replace("target.true_r_earth", String.valueOf(ctx.targetResistEarth));
    s = s.replace("target.true_r_air", String.valueOf(ctx.targetResistAir));
    s = s.replace("target.true_r_water", String.valueOf(ctx.targetResistWater));
    s = s.replace("target.true_light", String.valueOf(ctx.selfLight));
    s = s.replace("target.true_fire", String.valueOf(ctx.selfFire));
    s = s.replace("target.true_earth", String.valueOf(ctx.selfEarth));
    s = s.replace("target.true_air", String.valueOf(ctx.selfAir));
    s = s.replace("target.true_water", String.valueOf(ctx.selfWater));
    s = s.replace("target.true_dark", String.valueOf(ctx.selfDark));
    s = s.replace("target.r_dark", String.valueOf(ctx.targetResistDark));
    s = s.replace("target.r_light", String.valueOf(ctx.targetResistLight));
    s = s.replace("target.r_fire", String.valueOf(ctx.targetResistFire));
    s = s.replace("target.r_earth", String.valueOf(ctx.targetResistEarth));
    s = s.replace("target.r_air", String.valueOf(ctx.targetResistAir));
    s = s.replace("target.r_water", String.valueOf(ctx.targetResistWater));
    s = substituteSelfTrue(s, ctx);
    s = s.replace("self.level", String.valueOf(ctx.level));
    s = s.replace("self.intel", String.valueOf(ctx.intel));
    s = s.replace("self.int", String.valueOf(ctx.intel));
    s = s.replace("self.luck", String.valueOf(ctx.luck));
    s = s.replace("self.str", String.valueOf(ctx.str));
    s = s.replace("self.end", String.valueOf(ctx.end));
    s = s.replace("self.agi", String.valueOf(ctx.agi));
    s = s.replace("self.wil", String.valueOf(ctx.wil));
    s = s.replace("self.wis", String.valueOf(ctx.wis));
    s = s.replace("self.light", String.valueOf(ctx.selfLight));
    s = s.replace("self.fire", String.valueOf(ctx.selfFire));
    s = s.replace("self.earth", String.valueOf(ctx.selfEarth));
    s = s.replace("self.air", String.valueOf(ctx.selfAir));
    s = s.replace("self.water", String.valueOf(ctx.selfWater));
    s = s.replace("self.dark", String.valueOf(ctx.selfDark));
    s = s.replace("arrow_dmg", String.valueOf(ctx.arrowDmg));
    return s;
  }

  private static final Pattern SELF_TRUE = Pattern.compile("self\\.true_([A-Za-z0-9_]+)");

  private static String substituteSelfTrue(String s, Context ctx) {
    Matcher matcher = SELF_TRUE.matcher(s);
    StringBuilder out = new StringBuilder();
    while (matcher.find()) {
      String key = canonicalSelfTrueKey(matcher.group(1));
      Integer reading = ctx.selfTrue.get(key);
      matcher.appendReplacement(
          out, Integer.toString(reading != null ? reading : selfTrueFallback(key, ctx)));
    }
    matcher.appendTail(out);
    return out.toString();
  }

  static String canonicalSelfTrueKey(String key) {
    String normalized = key == null ? "" : key.trim().toLowerCase();
    return switch (normalized) {
      case "dex", "agility" -> "agi";
      case "intel", "intelligence" -> "int";
      case "strength" -> "str";
      case "endurance" -> "end";
      case "wisdom" -> "wis";
      default -> normalized;
    };
  }

  /**
   * What a {@code self.true_*} variable reads when the context carries no reading for it — used by
   * monster casts and by the compendium exporter, which have no player to ask. Falls back to the
   * context's effective values, and to the same resistance baselines {@code Player} uses, rather
   * than to 0.
   */
  private static int selfTrueFallback(String key, Context ctx) {
    return switch (key) {
      case "str" -> ctx.str;
      case "end" -> ctx.end;
      case "agi" -> ctx.agi;
      case "int" -> ctx.intel;
      case "wis" -> ctx.wis;
      case "wil" -> ctx.wil;
      case "luck" -> ctx.luck;
      case "level" -> ctx.level;
      case "fire" -> ctx.selfFire;
      case "earth" -> ctx.selfEarth;
      case "air" -> ctx.selfAir;
      case "water" -> ctx.selfWater;
      case "light" -> ctx.selfLight;
      case "dark" -> ctx.selfDark;
      case "r_light" -> 5000;
      case "r_fire", "r_earth", "r_air", "r_water", "r_dark" -> 100;
      default -> 0;
    };
  }

  private static String replaceDiceOp(String s) {
    char[] c = s.toCharArray();
    for (int i = 1; i < c.length - 1; i++) {
      if (c[i] == 'd' || c[i] == 'D') {
        char prev = c[i - 1], next = c[i + 1];
        if ((Character.isDigit(prev) || prev == ')') && (Character.isDigit(next) || next == '(')) {
          c[i] = '@';
        }
      }
    }
    return new String(c);
  }

  private static class Parser {
    final String s;
    int pos;

    Parser(String s) {
      this.s = s;
    }

    double parseExpr() {
      double v = parseTerm();
      while (pos < s.length()) {
        char op = s.charAt(pos);
        if (op == '+') {
          pos++;
          v += parseTerm();
        } else if (op == '-') {
          pos++;
          v -= parseTerm();
        } else break;
      }
      return v;
    }

    double parseTerm() {
      double v = parseFactor();
      while (pos < s.length()) {
        char op = s.charAt(pos);
        if (op == '*') {
          pos++;
          v *= parseFactor();
        } else if (op == '/') {
          pos++;
          double d = parseFactor();
          v = d == 0 ? 0 : v / d;
        } else if (op == '@') {
          pos++;
          v = rollDice((int) v, (int) parseFactor());
        } else break;
      }
      return v;
    }

    double parseFactor() {
      if (pos < s.length() && s.startsWith("if(", pos)) {
        return parseIf();
      }
      if (pos < s.length() && s.charAt(pos) == '(') {
        pos++;
        double v = parseExpr();
        if (pos < s.length() && s.charAt(pos) == ')') pos++;
        return v;
      }
      if (pos < s.length() && s.charAt(pos) == '-') {
        pos++;
        return -parseFactor();
      }
      return parseNumber();
    }

    double parseIf() {
      pos += 3;
      double left = parseExpr();
      char c1 = s.charAt(pos++);
      char c2 =
          (pos < s.length() && (s.charAt(pos) == '=' || s.charAt(pos) == '>'))
              ? s.charAt(pos++)
              : 0;
      double right = parseExpr();
      boolean cond;
      if (c1 == '<' && c2 == '=') cond = left <= right;
      else if (c1 == '>' && c2 == '=') cond = left >= right;
      else if (c1 == '!' && c2 == '=') cond = left != right;
      else if (c1 == '=' && c2 == '=') cond = left == right;
      else if (c1 == '<') cond = left < right;
      else if (c1 == '>') cond = left > right;
      else cond = left == right;
      if (pos < s.length() && s.charAt(pos) == '?') pos++;
      double trueVal = parseExpr();
      if (pos < s.length() && s.charAt(pos) == ':') pos++;
      double falseVal = parseExpr();
      if (pos < s.length() && s.charAt(pos) == ')') pos++;
      return cond ? trueVal : falseVal;
    }

    double parseNumber() {
      int start = pos;
      while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) pos++;
      if (start == pos) return 0;
      return Double.parseDouble(s.substring(start, pos));
    }

    double rollDice(int count, int faces) {
      if (count <= 0 || faces <= 0) return 0;
      int total = 0;
      for (int i = 0; i < count; i++) total += ThreadLocalRandom.current().nextInt(faces) + 1;
      return total;
    }
  }

  private static final class MinParser extends Parser {
    MinParser(String s) {
      super(s);
    }

    @Override
    double rollDice(int count, int faces) {
      return count;
    }
  }

  private static final class MaxParser extends Parser {
    MaxParser(String s) {
      super(s);
    }

    @Override
    double rollDice(int count, int faces) {
      return (double) count * faces;
    }
  }
}
