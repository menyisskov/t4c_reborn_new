package com.perso.T4C.online;

import com.perso.T4C.i18n.I18n;
import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.online.model.CharacterProgress;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.spell.SpellRegistry;
import com.perso.T4C.spell.engine.SpellEngine;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Castable spells and usable potions for the native selection control, derived from committed
 * character progress and the server's authored spell catalog. Rows are advisory: the
 * server still decides mana, cooldown, exhaustion and effect, and refusals come back as message keys.
 * Number keys 1-9 address rows, as in {@link OnlineInventoryRows}.
 */
final class OnlineAbilityChoices {
  static final int MAX_CHOICES = OnlineInventoryRows.MAX_CHOICES;

  /** {@code manaCostLabel} is the authored cost as the spell book shows it, never a retyped number. */
  record SpellChoice(String key, String name, String manaCostLabel, boolean requiresCreatureTarget) {}

  /** {@code instance} is what {@link OnlineConnection#useItem} sends; {@code quantity} is for display. */
  record PotionChoice(UUID instance, String itemKey, String name, int quantity) {}

  /** Authored spell target type "an area around the creature the caster targets". */
  private static final int AREA_AROUND_CREATURE = 17;

  private OnlineAbilityChoices() {}

  /** Learned stage-1/2 spells the current engine resolves, in sorted key order. */
  static List<SpellChoice> castableSpells(CharacterProgress progress, SpellContentV2 catalog) {
    if (progress == null || catalog == null) return List.of();
    var rows = new ArrayList<SpellChoice>();
    for (var key : progress.spells().stream().sorted().toList()) {
      var spell = catalog.spells().get(key);
      if (spell == null || spell.stage() > OnlineSpellAllowlistV2.ENABLED_STAGE || !SpellEngine.supported(spell)) continue;
      // Target type 17 is an area around a creature the caster aims at (Fireball, Boulders, Cinderburst): cast with no target the
      // engine answers wrong_target (T4C-0331 live run). Type 19 areas are aimed at a tile and type 18 are centred on the caster.
      boolean creatureTarget = switch (spell.kind()) {
        case NUKE, DRAIN, DEBUFF, DOT -> true;
        case AREA_DAMAGE -> spell.targetType() == AREA_AROUND_CREATURE;
        default -> false;
      };
      rows.add(new SpellChoice(key, spellName(key), manaLabel(spell.manaCost()), creatureTarget));
    }
    return List.copyOf(rows);
  }

  static List<PotionChoice> usablePotions(CharacterProgress progress, SpellContent allowlist) {
    return usablePotions(progress, allowlist, ItemRegistry::findByKey);
  }

  /**
   * Carried, unequipped single-charge consumables whose spell the server allows as an item effect.
   * Mirrors the server's fail-closed checks so the control never offers what is always refused.
   */
  static List<PotionChoice> usablePotions(
      CharacterProgress progress, SpellContent allowlist, Function<String, ItemDefinition> items) {
    if (progress == null || allowlist == null) return List.of();
    var itemSpellIds = new HashSet<Integer>();
    allowlist.spells().values().stream()
        .filter(spell -> spell.use() == SpellContent.Use.ITEM)
        .forEach(spell -> itemSpellIds.add(spell.id()));
    var worn = new HashSet<>(progress.equipment().values());
    var rows = new ArrayList<PotionChoice>();
    for (var item : progress.inventory()) {
      if (worn.contains(item.id())) continue;
      var definition = items.apply(item.key());
      if (definition == null || definition.getBodyPart() != null || definition.getNbCharges() > 1) continue;
      if (!definition.isUnlimitedUse() && item.charges() != 1) continue;
      if (definition.getSpells().stream().noneMatch(spell -> itemSpellIds.contains(spell.getSpellId()))) continue;
      rows.add(new PotionChoice(item.id(), item.key(), I18n.resolve(definition.getName()), item.quantity()));
    }
    return List.copyOf(rows);
  }

  /** Digit 1 is the first row; only the first nine rows are reachable. */
  static <T> Optional<T> choose(List<T> rows, int digit) {
    if (digit < 1 || digit > rows.size() || digit > MAX_CHOICES) return Optional.empty();
    return Optional.of(rows.get(digit - 1));
  }

  private static String spellName(String key) {
    var data = SpellRegistry.findByName(key);
    return data == null ? key : I18n.resolve(data.getName());
  }

  private static String manaLabel(String formula) {
    String cost = formula == null || formula.isBlank() ? "0" : formula.trim();
    return I18n.key("ui.spell_stat.mana") + " " + cost;
  }
}
