package com.perso.T4C.spell;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.player.Player;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class SpellEffectManager {
  public record SummonRequest(String type, String definitionKey) {}

  public record Impact(
      int healthDelta, List<SummonRequest> summons, int drainedHealth, boolean vaporize) {}

  public record TargetExhaustion(long attackMillis, long mentalMillis, long moveMillis) {}

  public record PlayerUtility(
      Integer teleportTileX,
      Integer teleportTileY,
      Integer teleportWorldZ,
      boolean invisibilityApplied,
      boolean detectInvisibleApplied,
      boolean detectHiddenApplied,
      int dispelledEffects) {}

  @FunctionalInterface
  public interface PeriodicImpactCallback {
    void apply(SpellData spell, Player caster, BaseMonster target);
  }

  private static final long DEFAULT_TIMER_FREQUENCY_MS = 1_000L;
  private final List<TimedHook> hooks = new ArrayList<>();

  public Impact resolve(
      SpellData spell, Player caster, BaseMonster target, double rangeFromCenter) {
    if (spell == null || caster == null || target == null)
      return new Impact(0, List.of(), 0, false);
    int delta = resolveHealthDelta(spell, caster, target, rangeFromCenter);
    int drainedHealth = hasDrainLifeEffect(spell) ? Math.max(0, -delta) : 0;
    List<SummonRequest> summons = new ArrayList<>();
    boolean vaporize = false;
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect == null) continue;
      if (effect.getEffectType() == 6) {
        String type = parameter(effect, 1);
        String key = parameter(effect, 2);
        if (type != null && key != null) summons.add(new SummonRequest(type, key));
      } else if (effect.getEffectType() == 13) {
        int spellId = evaluate(parameter(effect, 1), caster, target, rangeFromCenter);
        int chance = evaluate(parameter(effect, 2), caster, target, rangeFromCenter);
        if (spellId > 0 && chance > 0 && ThreadLocalRandom.current().nextInt(101) <= chance) {
          dispel(target, spellId);
        }
      } else if (effect.getEffectType() == 12) {
        vaporize = true;
      }
    }
    return new Impact(delta, List.copyOf(summons), drainedHealth, vaporize);
  }

  public boolean hasVaporizeEffect(SpellData spell) {
    return spell != null
        && spell.getT4cEffects().stream()
            .anyMatch(effect -> effect != null && effect.getEffectType() == 12);
  }

  public int resolvePlayerHealthDelta(SpellData spell, Player caster) {
    if (spell == null || caster == null) return 0;
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect != null && effect.getEffectType() == 1) {
        String formula = parameter(effect, 1);
        if (formula != null) {
          DiceFormula.Context base = SpellCastingService.context(caster);
          DiceFormula.Context context =
              new DiceFormula.Context(
                  base.str,
                  base.end,
                  base.agi,
                  base.intel,
                  base.wil,
                  base.wis,
                  base.luck,
                  base.level,
                  0,
                  100,
                  100,
                  100,
                  100,
                  100,
                  100,
                  elementalPower(caster, "fire"),
                  elementalPower(caster, "earth"),
                  elementalPower(caster, "air"),
                  elementalPower(caster, "water"),
                  elementalPower(caster, "light"),
                  elementalPower(caster, "dark"));
          int delta = DiceFormula.of(formula).evaluate(context);
          if (delta != 0) return delta;
        }
      }
    }
    int low = Math.min(spell.getMinDamage(), spell.getMaxDamage());
    int high = Math.max(spell.getMinDamage(), spell.getMaxDamage());
    return low == high ? low : ThreadLocalRandom.current().nextInt(low, high + 1);
  }

  private static boolean hasDrainLifeEffect(SpellData spell) {
    return spell.getT4cEffects().stream()
        .anyMatch(effect -> effect != null && effect.getEffectType() == 10);
  }

  public int resolvePlayerManaDelta(SpellData spell, Player caster) {
    if (spell == null || caster == null) return 0;
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect == null
          || effect.getEffectType() != 2
          || !"MANA".equalsIgnoreCase(parameter(effect, 2))) continue;
      return evaluatePlayer(parameter(effect, 3), caster);
    }
    return 0;
  }

  public List<SummonRequest> resolvePositionSummons(SpellData spell) {
    if (spell == null) return List.of();
    List<SummonRequest> summons = new ArrayList<>();
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect != null && effect.getEffectType() == 6) {
        String type = parameter(effect, 1);
        String key = parameter(effect, 2);
        if (type != null && key != null) summons.add(new SummonRequest(type, key));
      }
    }
    return List.copyOf(summons);
  }

  public List<SpellData.SpellEffect> resolvePlayerBuffEffects(SpellData spell, Player caster) {
    if (spell == null || caster == null) return List.of();
    return resolvePlayerBuffEffects(spell, casterContext(caster));
  }

  public List<SpellData.SpellEffect> resolvePlayerBuffEffects(
      SpellData spell, DiceFormula.Context casterContext) {
    if (spell == null || casterContext == null) return List.of();
    boolean hasDuration = resolveDurationSeconds(spell, casterContext) > 0;
    List<SpellData.SpellEffect> resolved = new ArrayList<>();
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect == null || effect.getEffectType() != 2) continue;
      String originalAttribute = parameter(effect, 2);
      String attribute = normalizeBoostAttribute(originalAttribute);
      if (attribute == null) continue;
      if ("mana".equals(attribute) && !hasDuration) continue;
      int amount = evaluate(parameter(effect, 3), casterContext);
      resolved.add(
          new SpellData.SpellEffect(
              "ATTRIBUTE", attribute, Integer.toString(amount), originalAttribute));
    }
    return List.copyOf(resolved);
  }

  public int resolveDurationSeconds(SpellData spell, Player caster) {
    if (spell == null || caster == null) return 0;
    return resolveDurationSeconds(spell, casterContext(caster));
  }

  public int resolveDurationSeconds(SpellData spell, DiceFormula.Context casterContext) {
    if (spell == null || casterContext == null) return 0;
    long millis = Math.max(0, evaluate(spell.getDuration(), casterContext));
    return millis <= 0
        ? 0
        : (int) Math.min(Integer.MAX_VALUE, Math.max(1L, (millis + 999L) / 1_000L));
  }

  public PlayerUtility applyPlayerUtilityEffects(SpellData spell, Player caster) {
    if (spell == null || caster == null) {
      return new PlayerUtility(null, null, null, false, false, false, 0);
    }
    Integer x = null, y = null, z = null;
    boolean invisible = false;
    boolean detectInvisible = false;
    boolean detectHidden = false;
    int dispelled = 0;
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect == null) continue;
      if (effect.getEffectType() == 7) {
        x = evaluatePlayer(parameter(effect, 1), caster);
        y = evaluatePlayer(parameter(effect, 2), caster);
        z = evaluatePlayer(parameter(effect, 3), caster);
      } else if (effect.getEffectType() == 11) {
        x = Math.round(caster.resolveRespawnWorldX() / com.perso.T4C.config.GameConstants.GRID_W);
        y = Math.round(caster.resolveRespawnWorldY() / com.perso.T4C.config.GameConstants.GRID_H);
        z = caster.resolveRespawnWorldZ();
      } else if (effect.getEffectType() == 3) {
        int flagId = evaluatePlayer(parameter(effect, 1), caster);
        int value = evaluatePlayer(parameter(effect, 2), caster);
        if (flagId > 0) caster.setQuestFlag("legacy:viewflag:" + flagId, value);
      } else if (effect.getEffectType() == 13) {
        int targetSpellId = evaluatePlayer(parameter(effect, 1), caster);
        int chance = Math.max(0, Math.min(100, evaluatePlayer(parameter(effect, 2), caster)));
        SpellData targetSpell = SpellRegistry.findById(targetSpellId);
        if (targetSpell != null
            && chance > 0
            && ThreadLocalRandom.current().nextInt(101) <= chance
            && caster.dispelBuff(targetSpell.getName())) {
          dispelled++;
        }
      } else if (effect.getEffectType() == 15) {
        int chance = Math.max(0, Math.min(100, evaluatePlayer(parameter(effect, 1), caster)));
        if (chance > 0 && ThreadLocalRandom.current().nextInt(101) <= chance) {
          long duration = Math.max(1_000L, evaluatePlayer(spell.getDuration(), caster));
          caster.setHiddenFor(duration);
          invisible = true;
        }
      } else if (effect.getEffectType() == 16 || effect.getEffectType() == 17) {
        int chance = Math.max(0, Math.min(100, evaluatePlayer(parameter(effect, 1), caster)));
        if (chance > 0 && ThreadLocalRandom.current().nextInt(101) <= chance) {
          long duration = Math.max(1_000L, evaluatePlayer(spell.getDuration(), caster));
          if (effect.getEffectType() == 16) {
            caster.setDetectInvisibleFor(duration);
            detectInvisible = true;
          } else {
            caster.setDetectHiddenFor(duration);
            detectHidden = true;
          }
        }
      } else if (effect.getEffectType() == 14) {
        int chance = Math.max(0, Math.min(100, evaluatePlayer(parameter(effect, 4), caster)));
        if (chance > 0 && ThreadLocalRandom.current().nextInt(101) <= chance) {
          long attack = Math.max(0L, evaluatePlayer(parameter(effect, 1), caster));
          long mental = Math.max(0L, evaluatePlayer(parameter(effect, 2), caster));
          long move = Math.max(0L, evaluatePlayer(parameter(effect, 3), caster));
          caster.applyExhaustion(mental, move, attack);
        }
      }
    }
    return new PlayerUtility(x, y, z, invisible, detectInvisible, detectHidden, dispelled);
  }

  public void installTimedHooks(SpellData source, Player caster, BaseMonster target) {
    if (source == null || caster == null || target == null) return;
    boolean targetIsSelf = source.getTargetType() == 5;
    long duration = Math.max(0, evaluateHook(source.getDuration(), caster, target, targetIsSelf));
    for (SpellData.T4cEffect effect : source.getT4cEffects()) {
      if (effect == null
          || effect.getEffectType() != 9
          || !"OnTimer".equalsIgnoreCase(parameter(effect, 2))) continue;
      int linkedId = evaluateHook(parameter(effect, 1), caster, target, targetIsSelf);
      int chance =
          Math.max(
              0, Math.min(100, evaluateHook(parameter(effect, 3), caster, target, targetIsSelf)));
      SpellData linked = SpellRegistry.findById(linkedId);
      if (linked == null || chance <= 0) continue;
      long initialDelay =
          Math.max(0L, evaluateHook(parameter(effect, 4), caster, target, targetIsSelf));
      long now = System.currentTimeMillis();
      hooks.removeIf(h -> h.target == target && h.sourceSpellId == source.getSpellId());
      if (duration <= 0L) {
        hooks.add(
            new TimedHook(
                source.getSpellId(),
                caster,
                target,
                linked,
                chance,
                now + initialDelay,
                Long.MAX_VALUE,
                0L,
                true));
        continue;
      }
      if (initialDelay == 0L) initialDelay = DEFAULT_TIMER_FREQUENCY_MS;
      long frequency = evaluateHook(source.getFrequency(), caster, target, targetIsSelf);
      if (frequency <= 0L) frequency = DEFAULT_TIMER_FREQUENCY_MS;
      hooks.add(
          new TimedHook(
              source.getSpellId(),
              caster,
              target,
              linked,
              chance,
              now + initialDelay,
              now + duration,
              frequency,
              false));
    }
  }

  public TargetExhaustion resolveTargetExhaustion(
      SpellData spell, Player caster, BaseMonster target) {
    if (spell == null || caster == null || target == null) return new TargetExhaustion(0L, 0L, 0L);
    boolean targetIsSelf = spell.getTargetType() == 5;
    return new TargetExhaustion(
        Math.max(0L, evaluateHook(spell.getAttackExhaustion(), caster, target, targetIsSelf)),
        Math.max(0L, evaluateHook(spell.getMentalExhaustion(), caster, target, targetIsSelf)),
        Math.max(0L, evaluateHook(spell.getPhysicalExhaustion(), caster, target, targetIsSelf)));
  }

  public TargetExhaustion resolveExplicitTargetExhaustion(
      SpellData spell, Player caster, BaseMonster target) {
    if (spell == null || caster == null || target == null) return new TargetExhaustion(0L, 0L, 0L);
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect == null || effect.getEffectType() != 14) continue;
      int chance = Math.max(0, Math.min(100, evaluate(parameter(effect, 4), caster, target, 0d)));
      if (chance <= 0 || ThreadLocalRandom.current().nextInt(101) > chance) continue;
      return new TargetExhaustion(
          Math.max(0L, evaluate(parameter(effect, 1), caster, target, 0d)),
          Math.max(0L, evaluate(parameter(effect, 2), caster, target, 0d)),
          Math.max(0L, evaluate(parameter(effect, 3), caster, target, 0d)));
    }
    return new TargetExhaustion(0L, 0L, 0L);
  }

  public int resolveTargetDodgeModifier(SpellData spell, Player caster, BaseMonster target) {
    if (spell == null || caster == null || target == null) return 0;
    boolean targetIsSelf = spell.getTargetType() == 5;
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect != null
          && effect.getEffectType() == 2
          && "dodge".equalsIgnoreCase(parameter(effect, 2))) {
        return evaluateHook(parameter(effect, 3), caster, target, targetIsSelf);
      }
    }
    return 0;
  }

  public void update(PeriodicImpactCallback callback) {
    if (callback == null || hooks.isEmpty()) return;
    long now = System.currentTimeMillis();
    List<TimedHook> triggered = new ArrayList<>();
    Iterator<TimedHook> iterator = hooks.iterator();
    while (iterator.hasNext()) {
      TimedHook hook = iterator.next();
      if (hook.target.isDead() || now >= hook.expiresAt) {
        iterator.remove();
        continue;
      }
      if (now < hook.nextAt) continue;
      if (hook.oneShot) {
        iterator.remove();
        if (ThreadLocalRandom.current().nextInt(101) <= hook.chance) {
          triggered.add(hook);
        }
        continue;
      }
      hook.nextAt = now + hook.frequency;
      if (ThreadLocalRandom.current().nextInt(101) <= hook.chance) {
        triggered.add(hook);
      }
    }
    for (TimedHook hook : triggered) {
      callback.apply(hook.linkedSpell, hook.caster, hook.target);
    }
  }

  public int dispel(BaseMonster target, int sourceSpellId) {
    int before = hooks.size();
    hooks.removeIf(
        h -> h.target == target && (sourceSpellId <= 0 || h.sourceSpellId == sourceSpellId));
    return before - hooks.size();
  }

  private static int resolveHealthDelta(
      SpellData spell, Player caster, BaseMonster target, double range) {
    for (SpellData.T4cEffect effect : spell.getT4cEffects()) {
      if (effect != null && (effect.getEffectType() == 1 || effect.getEffectType() == 10)) {
        String center = parameter(effect, 1);
        String ranged = parameter(effect, 2);
        String formula = range > 0d && ranged != null && !ranged.isBlank() ? ranged : center;
        int evaluated = evaluate(formula, caster, target, range);
        if (evaluated != 0) return evaluated;
      }
    }
    int low = Math.min(spell.getMinDamage(), spell.getMaxDamage());
    int high = Math.max(spell.getMinDamage(), spell.getMaxDamage());
    return low == high ? low : ThreadLocalRandom.current().nextInt(low, high + 1);
  }

  private static int evaluate(String formula, Player caster, BaseMonster target, double range) {
    if (formula == null || formula.isBlank()) return 0;
    int fire = resistance(target, 1);
    int earth = resistance(target, 2);
    int air = resistance(target, 3);
    int water = resistance(target, 4);
    int light = resistance(target, 5);
    int dark = resistance(target, 6);
    DiceFormula.Context context =
        new DiceFormula.Context(
            caster.getEffectiveStrength(),
            caster.getEffectiveEndurance(),
            caster.getEffectiveDexterity(),
            caster.getEffectiveIntelligence(),
            0,
            caster.getEffectiveWisdom(),
            0,
            caster.getLevel(),
            0,
            fire,
            earth,
            air,
            water,
            light,
            dark,
            elementalPower(caster, "fire"),
            elementalPower(caster, "earth"),
            elementalPower(caster, "air"),
            elementalPower(caster, "water"),
            elementalPower(caster, "light"),
            elementalPower(caster, "dark"));
    String withRange =
        formula.replaceAll("(?<![A-Za-z_.])r(?![A-Za-z_])", Double.toString(Math.max(0d, range)));
    return DiceFormula.of(withRange).evaluate(context);
  }

  private static int evaluateHook(
      String formula, Player caster, BaseMonster target, boolean targetIsSelf) {
    if (!targetIsSelf) return evaluate(formula, caster, target, 0d);
    if (formula == null || formula.isBlank()) return 0;
    DiceFormula.Context context =
        new DiceFormula.Context(
            target.getCombatStrength(),
            target.getCombatEndurance(),
            target.getCombatAgility(),
            target.getCombatIntelligence(),
            0,
            0,
            0,
            target.getCombatLevel(),
            0,
            resistance(target, 1),
            resistance(target, 2),
            resistance(target, 3),
            resistance(target, 4),
            resistance(target, 5),
            resistance(target, 6),
            100,
            100,
            100,
            100,
            100,
            100);
    return DiceFormula.of(formula).evaluate(context);
  }

  private static int resistance(BaseMonster target, int element) {
    int value = target.getElementResistance(element);
    return value <= 0 ? 100 : value;
  }

  private static int elementalPower(Player caster, String element) {
    return caster.getElementPower(element);
  }

  private static int evaluatePlayer(String formula, Player caster) {
    return evaluate(formula, casterContext(caster));
  }

  private static int evaluate(String formula, DiceFormula.Context context) {
    if (formula == null || formula.isBlank()) return 0;
    return DiceFormula.of(formula).evaluate(context);
  }

  private static DiceFormula.Context casterContext(Player caster) {
    DiceFormula.Context base = SpellCastingService.context(caster);
    return new DiceFormula.Context(
        base.str,
        base.end,
        base.agi,
        base.intel,
        base.wil,
        base.wis,
        base.luck,
        base.level,
        0,
        100,
        100,
        100,
        100,
        100,
        100,
        elementalPower(caster, "fire"),
        elementalPower(caster, "earth"),
        elementalPower(caster, "air"),
        elementalPower(caster, "water"),
        elementalPower(caster, "light"),
        elementalPower(caster, "dark"));
  }

  private static String normalizeBoostAttribute(String attribute) {
    if (attribute == null) return null;
    return switch (attribute.trim().toLowerCase()) {
      case "str", "strength" -> "str";
      case "agi", "agility", "dex", "dexterity" -> "dex";
      case "end", "endurance" -> "end";
      case "int", "intelligence" -> "int";
      case "wis", "wisdom" -> "wis";
      case "ac", "armor class" -> "armorClass";
      case "max hp", "maxhp" -> "maxHp";
      case "attack" -> "skill:attack";
      case "dodge" -> "skill:dodge";
      case "skill 35", "archery" -> "skill:archery";
      case "skill 9" -> "skill:9";
      case "skill 29" -> "skill:29";
      case "air", "fire", "water", "earth", "light", "dark" ->
          "power:" + attribute.trim().toLowerCase();
      case "mana" -> "mana";
      case "radiance" -> "radiance";
      case "r_air" -> "resist:air";
      case "r_fire" -> "resist:fire";
      case "r_water" -> "resist:water";
      case "r_earth" -> "resist:earth";
      case "r_light" -> "resist:light";
      case "r_dark" -> "resist:dark";
      case "exp", "experience", "xp" -> "exp";
      case "unlimited" -> "unlimited";
      default -> null;
    };
  }

  private static String parameter(SpellData.T4cEffect effect, int id) {
    if (effect == null || effect.getParameters() == null) return null;
    for (SpellData.T4cEffect.EffectParam parameter : effect.getParameters()) {
      if (parameter != null && parameter.getParamId() == id) return parameter.getExpression();
    }
    return null;
  }

  private static final class TimedHook {
    private final int sourceSpellId;
    private final Player caster;
    private final BaseMonster target;
    private final SpellData linkedSpell;
    private final int chance;
    private long nextAt;
    private final long expiresAt;
    private final long frequency;
    private final boolean oneShot;

    private TimedHook(
        int sourceSpellId,
        Player caster,
        BaseMonster target,
        SpellData linkedSpell,
        int chance,
        long nextAt,
        long expiresAt,
        long frequency,
        boolean oneShot) {
      this.sourceSpellId = sourceSpellId;
      this.caster = caster;
      this.target = target;
      this.linkedSpell = linkedSpell;
      this.chance = chance;
      this.nextAt = nextAt;
      this.expiresAt = expiresAt;
      this.frequency = frequency;
      this.oneShot = oneShot;
    }
  }
}
