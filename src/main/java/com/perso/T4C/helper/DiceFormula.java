package com.perso.T4C.helper;

import java.util.concurrent.ThreadLocalRandom;

public final class DiceFormula {
  public static final class Context {
    public final int str, end, agi, intel, wil, wis, luck, level;
    public final int arrowDmg;
    public final int targetResistDark, targetResistLight;
    public final int targetResistFire, targetResistEarth, targetResistAir, targetResistWater;
    public final int selfFire, selfEarth, selfAir, selfWater, selfLight, selfDark;

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
