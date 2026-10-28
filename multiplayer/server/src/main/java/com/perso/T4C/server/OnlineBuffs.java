package com.perso.T4C.server;

import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.spell.BuffAttributes;
import com.perso.T4C.spell.engine.SpellEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Timed spell buffs in committed progress. A buff stores the amounts rolled when it was cast (they
 * are never re-rolled), an absolute expiry, and nothing else. Expiry is lazy: every reader asks for
 * the buffs active at its own {@code now} ({@code expiresAt > now}, so {@code now == expiresAt} is
 * expired, as in single-player), and the settle in {@link PersistentSessions#character} removes the
 * expired ones and takes back the maximum health they gave.
 *
 * <p>Logged-out time never runs a buff down: every finite expiry is moved later at join by the time
 * since the character was last settled online ({@link #ANCHOR}), the same idea as the regeneration
 * anchor. A buff laid by an older server (no amounts) cannot be interpreted and is refused, as are
 * amounts for attributes the server does not apply, so nothing is ever half applied.
 */
final class OnlineBuffs {
  /** Absolute UTC millis of the last time this character's buffs were settled while it was online. */
  static final String ANCHOR = "effects_anchor";

  private OnlineBuffs() {}

  /** Buffs still running at {@code now}. */
  static java.util.List<CharacterProgress.Buff> active(CharacterProgress progress, long now) {
    return progress.buffs().stream().filter(buff -> buff.expiresAt() > now).toList();
  }

  /** The buff amounts by normalised attribute, summed over the buffs running at {@code now}. */
  static Map<String, Integer> totals(CharacterProgress progress, long now) {
    var totals = new HashMap<String, Integer>();
    for (var buff : active(progress, now)) buff.modifiers().forEach((key, amount) -> totals.merge(key, amount, Integer::sum));
    return totals;
  }

  /** Active buff amounts per spell key, for Renew Armor's one-after-another refresh. */
  static Map<String, Map<String, Integer>> activeBySpell(CharacterProgress progress, long now) {
    var bySpell = new HashMap<String, Map<String, Integer>>();
    for (var buff : active(progress, now)) bySpell.put(buff.spell(), buff.modifiers());
    return bySpell;
  }

  /**
   * Refuses (IllegalArgumentException) a running buff the server cannot interpret: one without stored
   * amounts, or with an attribute outside {@link BuffAttributes#MODELLED}. Callers that fold buffs into
   * numbers call this first.
   */
  static void requireModelled(CharacterProgress progress, long now) {
    for (var buff : active(progress, now)) {
      if (buff.appliedAt() == 0 && buff.modifiers().isEmpty())
        throw new IllegalArgumentException("Active buff without stored amounts is not integrated");
      for (var key : buff.modifiers().keySet())
        if (!BuffAttributes.MODELLED.contains(key))
          throw new IllegalArgumentException("Active buff stat effects are not integrated: " + key);
    }
  }

  /** Same as {@link #requireModelled} but as the 409 a player-facing request answers with. */
  static void requireModelledOrReject(CharacterProgress progress, long now) {
    try {
      requireModelled(progress, now);
    } catch (IllegalArgumentException unsupported) {
      throw new PreviewWorld.Rejected(409, "unsupported_effects");
    }
  }

  /** Removes expired buffs, taking back the maximum health they gave; the same instance when none expired. */
  static CharacterProgress expire(CharacterProgress progress, long now) {
    var expired = progress.buffs().stream().filter(buff -> buff.expiresAt() <= now).toList();
    if (expired.isEmpty()) return progress;
    var vitals = progress.vitals();
    int maxHp = vitals.maxHp();
    for (var buff : expired) maxHp = Math.max(1, maxHp - buff.modifiers().getOrDefault("maxHp", 0));
    var kept = new ArrayList<>(progress.buffs());
    kept.removeAll(expired);
    var withVitals = ProgressEdits.withVitals(progress,
        new CharacterProgress.Vitals(Math.min(vitals.hp(), maxHp), maxHp, vitals.mana(), vitals.maxMana()));
    return withBuffs(withVitals, kept);
  }

  /**
   * Lays or refreshes a buff: one per spell, replacing the amounts and expiry (never stacking), exactly
   * as {@code Player.applyBuff}. Maximum health follows the amounts; current health is left as it is.
   */
  static CharacterProgress apply(CharacterProgress progress, SpellEngine.Event.ApplyBuff event, long now) {
    var amounts = new HashMap<String, Integer>();
    for (var modifier : event.modifiers()) amounts.merge(modifier.attribute(), modifier.amount(), Integer::sum);
    long expiresAt = event.durationSeconds() <= 0 ? Long.MAX_VALUE
        : Math.addExact(now, event.durationSeconds() * 1000L);
    var next = new CharacterProgress.Buff(event.spellKey(), expiresAt, 0, 0, amounts, now);
    var buffs = new ArrayList<CharacterProgress.Buff>();
    int maxHp = progress.vitals().maxHp();
    for (var buff : progress.buffs()) {
      if (!buff.spell().equals(event.spellKey())) { buffs.add(buff); continue; }
      maxHp = Math.max(1, maxHp - buff.modifiers().getOrDefault("maxHp", 0));
    }
    buffs.add(next);
    maxHp = Math.max(1, maxHp + amounts.getOrDefault("maxHp", 0));
    var vitals = progress.vitals();
    var withVitals = ProgressEdits.withVitals(progress,
        new CharacterProgress.Vitals(Math.min(vitals.hp(), maxHp), maxHp, vitals.mana(), vitals.maxMana()));
    return withBuffs(withVitals, buffs);
  }

  /** Removes the buff of this spell if one is running at {@code now}; the same instance when there is none. */
  static CharacterProgress dispel(CharacterProgress progress, String spellKey, long now) {
    var found = active(progress, now).stream().filter(buff -> buff.spell().equals(spellKey)).findFirst();
    if (found.isEmpty()) return progress;
    var kept = new ArrayList<>(progress.buffs());
    kept.remove(found.get());
    var vitals = progress.vitals();
    int maxHp = Math.max(1, vitals.maxHp() - found.get().modifiers().getOrDefault("maxHp", 0));
    var withVitals = ProgressEdits.withVitals(progress,
        new CharacterProgress.Vitals(Math.min(vitals.hp(), maxHp), maxHp, vitals.mana(), vitals.maxMana()));
    return withBuffs(withVitals, kept);
  }

  /**
   * At join, moves every finite expiry later by the time the character spent logged out, and anchors at
   * the join. A character without buffs, or with no anchor yet, only gains the anchor when it has buffs.
   */
  static CharacterProgress shiftForJoin(CharacterProgress progress, long joinedAt) {
    long anchor = progress.cooldowns().getOrDefault(ANCHOR, -1L);
    if (anchor >= joinedAt) return progress;
    if (progress.buffs().isEmpty()) return progress;
    long away = anchor < 0 ? 0 : joinedAt - anchor;
    var shifted = new ArrayList<CharacterProgress.Buff>();
    for (var buff : progress.buffs()) {
      long expiresAt = buff.expiresAt() == Long.MAX_VALUE ? buff.expiresAt()
          : Math.addExact(buff.expiresAt(), away);
      shifted.add(new CharacterProgress.Buff(buff.spell(), expiresAt, buff.casterIntelligence(),
          buff.casterWisdom(), buff.modifiers(), buff.appliedAt()));
    }
    return anchored(withBuffs(progress, shifted), joinedAt);
  }

  /** Records that the character was online and settled at {@code now}; only meaningful while it has buffs. */
  static CharacterProgress anchored(CharacterProgress progress, long now) {
    if (progress.buffs().isEmpty() || progress.cooldowns().getOrDefault(ANCHOR, -1L) == now) return progress;
    var cooldowns = new HashMap<>(progress.cooldowns());
    cooldowns.put(ANCHOR, now);
    return ProgressEdits.withCooldowns(progress, cooldowns);
  }

  private static CharacterProgress withBuffs(CharacterProgress p, java.util.List<CharacterProgress.Buff> buffs) {
    int version = buffs.isEmpty() ? p.version() : Math.max(p.version(), 2);
    return new CharacterProgress(version, p.characterClass(), p.gender(), p.attributes(), p.vitals(),
        p.advancement(), p.inventory(), p.equipment(), p.skills(), p.spells(), p.questFlags(), p.storage(),
        p.storageGold(), p.respawn(), buffs, p.cooldowns(), p.hiddenUntil(), p.companion());
  }
}
