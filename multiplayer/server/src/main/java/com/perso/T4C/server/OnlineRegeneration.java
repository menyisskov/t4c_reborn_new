package com.perso.T4C.server;

import com.perso.T4C.combat.RegenerationRules;
import com.perso.T4C.combat.SkillBonusRules;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.ItemContent;
import java.util.HashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * Natural health and mana regeneration for committed online progress. The offline game rolls
 * {@link RegenerationRules} once per two seconds; here the same rule is applied for the whole
 * ticks that elapsed since a persisted anchor, so the committed vitals are current before any
 * draft that reads or changes them. The anchor is an absolute UTC millisecond value stored in
 * {@code cooldowns}, so reconnecting can neither skip nor repeat ticks.
 */
final class OnlineRegeneration {
  static final String ANCHOR = "regen_anchor";
  static final long TICK_MILLIS = (long) (RegenerationRules.REGEN_INTERVAL_SECONDS * 1000f);
  /** Longest gap settled tick by tick (one hour); a longer gap is bounded to this many ticks. */
  static final int MAX_TICKS = (int) (3_600_000L / TICK_MILLIS);

  private OnlineRegeneration() {}

  /** Settles using attributes plus committed equipment boosts. */
  static CharacterProgress settle(CharacterProgress progress, long now, RandomGenerator random, ItemContent items) {
    return settle(progress, now, random, OnlineEquipmentBonuses.resolve(progress, items));
  }

  /**
   * Applies whole elapsed ticks and advances the anchor by exactly those ticks, so the remainder
   * of a partial tick is kept. A missing anchor, a dead character or a clock that moved backwards
   * re-anchors at {@code now} without regenerating. A gap longer than {@link #MAX_TICKS} is
   * bounded and re-anchored at {@code now}; ticks stop being rolled once both vitals are full.
   */
  static CharacterProgress settle(CharacterProgress progress, long now, RandomGenerator random,
      Map<Integer, Integer> bonuses) {
    long anchor = progress.cooldowns().getOrDefault(ANCHOR, -1L);
    var vitals = progress.vitals();
    if (anchor < 0 || anchor > now || vitals.hp() == 0) return reanchor(progress, now);
    long elapsed = (now - anchor) / TICK_MILLIS;
    if (elapsed == 0) return progress;
    boolean bounded = elapsed > MAX_TICKS;
    int ticks = (int) Math.min(elapsed, MAX_TICKS);
    var attributes = progress.attributes();
    var buffs = OnlineBuffs.totals(progress, now);
    int endurance = attributes.endurance() + buffs.getOrDefault("end", 0) + bonuses.getOrDefault(2, 0);
    int intelligence = attributes.intelligence() + buffs.getOrDefault("int", 0) + bonuses.getOrDefault(1, 0);
    int wisdom = attributes.wisdom() + buffs.getOrDefault("wis", 0) + bonuses.getOrDefault(4, 0);
    int rapidHealing = SkillBonusRules.effective("rapid_healing",
        progress.skills().getOrDefault("rapid_healing", 0), buffs.getOrDefault("skill:rapid_healing", 0),
        stat -> bonuses.getOrDefault(stat, 0));
    int hp = vitals.hp();
    int mana = vitals.mana();
    for (int i = 0; i < ticks && (hp < vitals.maxHp() || mana < vitals.maxMana()); i++) {
      hp = RegenerationRules.regenerateHp(hp, vitals.maxHp(), endurance, rapidHealing, random);
      mana = RegenerationRules.regenerateMana(mana, vitals.maxMana(), intelligence, wisdom, random);
    }
    var settled = progress;
    if (hp != vitals.hp() || mana != vitals.mana()) {
      settled = ProgressEdits.withVitals(settled, new CharacterProgress.Vitals(hp, vitals.maxHp(), mana, vitals.maxMana()));
    }
    return withAnchor(settled, bounded ? now : Math.addExact(anchor, ticks * TICK_MILLIS));
  }

  /** Starts a fresh anchor without regenerating: used for dead characters and, by default, at join. */
  static CharacterProgress reanchor(CharacterProgress progress, long now) {
    return progress.cooldowns().getOrDefault(ANCHOR, -1L) == now ? progress : withAnchor(progress, now);
  }

  private static CharacterProgress withAnchor(CharacterProgress progress, long anchor) {
    var cooldowns = new HashMap<>(progress.cooldowns());
    cooldowns.put(ANCHOR, anchor);
    return ProgressEdits.withCooldowns(progress, cooldowns);
  }
}
