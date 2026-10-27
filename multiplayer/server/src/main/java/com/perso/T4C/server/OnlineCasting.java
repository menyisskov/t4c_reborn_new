package com.perso.T4C.server;

import com.perso.T4C.helper.DiceFormula;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.server.persistence.OnlineStore;
import com.perso.T4C.spell.SpellCastRules;
import com.perso.T4C.spell.SpellHealRules;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Server-side self-cast drafts. Each call reads committed progress and returns one immutable draft
 * for the caller to commit once; a refused or failed cast returns no draft and changes nothing.
 * Shared with {@link OnlineItemUse}, which applies the same rules to consumable spells.
 */
final class OnlineCasting {
  static final String MENTAL_EXHAUSTION = "mental_exhaustion";
  static final String PHYSICAL_EXHAUSTION = "physical_exhaustion";
  static final String ATTACK_EXHAUSTION = "attack_exhaustion";
  static final String STUN = "stun";

  /** Draft to commit (no gold or experience) and what the cast changed, for the reply. */
  record Result(CharacterProgress progress, int healthGained, int manaSpent, int manaGained) {
    Result {
      Objects.requireNonNull(progress);
    }
  }

  /** Health and mana the spell restores, resolved once from its authored formulas. */
  record Effects(int healthDelta, int manaDelta) {}

  private OnlineCasting() {}

  static Result prepareSelfCast(OnlineStore.CharacterState saved, String spellKey, long now,
      RandomGenerator random, ItemContent items, SpellCatalog spells) {
    var progress = aliveProgress(saved, now);
    if (!progress.spells().contains(spellKey)) throw rejected("not_learned");
    var spell = castable(spells, spellKey);
    var effects = effects(spell, progress, items, random);
    var outcome = outcome(progress, spell, false, now, random, items);
    return apply(progress, spell, effects, outcome, now);
  }

  /** The committed progress of a living character with no active buff; anything else is refused. */
  static CharacterProgress aliveProgress(OnlineStore.CharacterState saved, long now) {
    var progress = saved.progress();
    if (progress == null) throw rejected("uninitialized_character");
    if (progress.vitals().hp() == 0) throw rejected("dead");
    // Buff effects are not integrated online yet, so they fail closed as they do for combat.
    if (progress.buffs().stream().anyMatch(buff -> buff.expiresAt() > now)) throw rejected("unsupported_effects");
    return progress;
  }

  static SpellContent.Spell castable(SpellCatalog spells, String spellKey) {
    try {
      return spells.castable(spellKey);
    } catch (IllegalArgumentException unsupported) {
      throw rejected("unsupported_spell");
    }
  }

  /** Health and mana the spell would restore against committed stats; a missing formula restores nothing. */
  static Effects effects(SpellContent.Spell spell, CharacterProgress progress, ItemContent items, RandomGenerator random) {
    var context = context(progress, items);
    var healFormulas = new ArrayList<String>();
    int manaDelta = 0;
    boolean manaSeen = false;
    for (var effect : spell.effects()) {
      if (effect.type() == 1) {
        String formula = parameter(effect, 1);
        if (formula != null) healFormulas.add(formula);
      } else if (effect.type() == 2 && !manaSeen) {
        if (!"MANA".equalsIgnoreCase(parameter(effect, 2))) throw rejected("unsupported_spell");
        manaSeen = true;
        String formula = parameter(effect, 3);
        manaDelta = formula == null || formula.isBlank() ? 0 : DiceFormula.of(formula).evaluate(context);
      }
    }
    // The catalog carries no min/max damage, so a heal with no non-zero formula restores nothing.
    int healthDelta = SpellHealRules.healthDelta(healFormulas, 0, 0, context, random);
    return new Effects(healthDelta, manaDelta);
  }

  /**
   * Runs the shared cast rules against committed state. Activation failure is a refusal with no
   * spend, matching the offline roll; {@code knownRequired} is false for consumables.
   */
  static SpellCastRules.Outcome outcome(CharacterProgress progress, SpellContent.Spell spell, boolean knownRequired,
      long now, RandomGenerator random, ItemContent items) {
    var cooldowns = progress.cooldowns();
    var input = new SpellCastRules.CastInput(
        new SpellCastRules.Spell(spell.targetType(), spell.attack(), false, spell.pvp(), spell.lineOfSight(),
            spell.manaCost(), spell.successRate(), spell.mentalExhaustion(), spell.physicalExhaustion(),
            spell.attackExhaustion(), spell.cooldownSeconds()),
        knownRequired,
        progress.spells().contains(spell.key()),
        SpellCastRules.TargetKind.SELF,
        false,
        0f,
        true,
        // Sanctuary protects only hostile or area targets; a self-target heal is never blocked by it.
        false,
        false,
        SpellCastRules.isActive(cooldowns.getOrDefault(spell.key(), 0L), now),
        SpellCastRules.isActive(cooldowns.getOrDefault(MENTAL_EXHAUSTION, 0L), now)
            || SpellCastRules.isActive(cooldowns.getOrDefault(STUN, 0L), now),
        false,
        progress.vitals().mana(),
        context(progress, items),
        random::nextInt);
    var outcome = SpellCastRules.evaluate(input);
    if (outcome.success()) return outcome;
    if (outcome.verdict() == SpellCastRules.Verdict.ACTIVATION_FAILED) throw rejected("spell_failed");
    throw rejected(outcome.verdict().name().toLowerCase(Locale.ROOT));
  }

  /** Spends mana, applies healing and mana restore with the clamps offline uses, and starts timers. */
  static Result apply(CharacterProgress progress, SpellContent.Spell spell, Effects effects,
      SpellCastRules.Outcome outcome, long now) {
    var vitals = progress.vitals();
    int mana = vitals.mana() - outcome.manaCost();
    int hp = vitals.hp();
    int healed = 0;
    if (effects.healthDelta() > 0) {
      int next = Math.min(vitals.maxHp(), hp + effects.healthDelta());
      healed = next - hp;
      hp = next;
    }
    int manaAfterCost = mana;
    if (effects.manaDelta() != 0) mana = Math.max(0, Math.min(vitals.maxMana(), mana + effects.manaDelta()));
    var updatedVitals = new CharacterProgress.Vitals(hp, vitals.maxHp(), mana, vitals.maxMana());
    var cooldowns = new HashMap<>(progress.cooldowns());
    if (outcome.cooldownSeconds() > 0) {
      start(cooldowns, spell.key(), Math.addExact(now, outcome.cooldownSeconds() * 1000L));
    }
    start(cooldowns, MENTAL_EXHAUSTION, now, outcome.mentalMillis());
    start(cooldowns, PHYSICAL_EXHAUSTION, now, outcome.physicalMillis());
    start(cooldowns, ATTACK_EXHAUSTION, now, outcome.attackMillis());
    var updated = ProgressEdits.withCooldowns(ProgressEdits.withVitals(progress, updatedVitals), cooldowns);
    return new Result(updated, healed, outcome.manaCost(), mana - manaAfterCost);
  }

  /** Absolute expiries never shorten an existing timer, so reconnecting cannot reset one. */
  private static void start(Map<String, Long> cooldowns, String key, long now, long millis) {
    if (millis > 0) start(cooldowns, key, Math.addExact(now, millis));
  }

  private static void start(Map<String, Long> cooldowns, String key, long until) {
    cooldowns.merge(key, until, Math::max);
  }

  /** Attributes plus equipment boosts, with the light power the healing formulas read. */
  static DiceFormula.Context context(CharacterProgress progress, ItemContent items) {
    var bonuses = OnlineEquipmentBonuses.resolve(progress, items);
    var base = progress.attributes();
    int skill = progress.skills().getOrDefault("light", 0);
    int light = (skill <= 0 ? 100 : skill)
        + progress.questFlags().getOrDefault("legacy:power:light", 0)
        + bonuses.getOrDefault(23, 0);
    return new DiceFormula.Context(
        base.strength() + bonuses.getOrDefault(3, 0),
        base.endurance() + bonuses.getOrDefault(2, 0),
        base.dexterity() + bonuses.getOrDefault(6, 0),
        base.intelligence() + bonuses.getOrDefault(1, 0),
        0,
        base.wisdom() + bonuses.getOrDefault(4, 0),
        0,
        progress.advancement().level(),
        0,
        100,
        100,
        light);
  }

  private static String parameter(SpellContent.Effect effect, int id) {
    return effect.params().stream().filter(param -> param.id() == id)
        .map(SpellContent.Param::expression).findFirst().orElse(null);
  }

  static PreviewWorld.Rejected rejected(String code) {
    return new PreviewWorld.Rejected(409, code);
  }
}
