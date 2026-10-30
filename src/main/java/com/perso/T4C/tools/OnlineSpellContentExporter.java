package com.perso.T4C.tools;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.spell.BuffAttributes;
import com.perso.T4C.spell.SpellAreaTargeting;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

/**
 * Build-time bridge that exports only the spells the online server may run or grant: the castable
 * self heal, grant-only quest spells, restorative consumables, and the Potion of Fury buff consumable.
 */
public final class OnlineSpellContentExporter {
  static final String HEAL_LIGHT = "spell.heal_light";
  static final String RENEW_ARMOR = "spell.renew_armor";

  private OnlineSpellContentExporter() {}

  public static SpellContent snapshot() {
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
    return snapshot(ItemRegistry.allByKey().values());
  }

  static SpellContent snapshot(Iterable<ItemDefinition> items) {
    var spells = new TreeMap<String, SpellContent.Spell>();
    spells.put(HEAL_LIGHT, export(required(HEAL_LIGHT), SpellContent.Use.CAST));
    spells.put(RENEW_ARMOR, export(required(RENEW_ARMOR), SpellContent.Use.GRANT_ONLY));
    // In-scope potions are finite consumables with no equipment slot and a restorative spell, except
    // Potion of Fury, whose supported authored strength buff is handled through the v2 engine path.
    for (var item : items) {
      if (item.getBodyPart() != null || item.getNbCharges() <= 0 || item.getSpells().isEmpty()
          || (item.getNbCharges() > 1 && item.isUnlimitedUse())) continue;
      for (var itemSpell : item.getSpells()) {
        // Unresolved or unsupported consumable spells are out of online scope and fail closed by omission.
        var spell = SpellRegistry.findById(itemSpell.getSpellId());
        if (spell != null && (isPlainRestore(spell)
            || ("item.potion_of_fury".equals(item.getKey()) && isSupportedBuff(spell))))
          spells.putIfAbsent(spell.getKey(), export(spell, SpellContent.Use.ITEM));
      }
    }
    return new SpellContent(SpellContent.VERSION, spells);
  }

  private static SpellData required(String key) {
    var spell = SpellRegistry.findByName(key);
    if (spell == null) throw new IllegalArgumentException("Missing authored spell " + key);
    return spell;
  }

  /**
   * A health restore (effect 1) or a mana restore (effect 2 on MANA), the only consumable effects the
   * shared rules resolve server-side. Buffs, cures and teleports stay out and are refused online.
   */
  private static boolean isPlainRestore(SpellData spell) {
    var effects = spell.getT4cEffects();
    return effects != null && !effects.isEmpty()
        && effects.stream().allMatch(effect -> effect != null
            && (effect.getEffectType() == 1 || (effect.getEffectType() == 2 && restoresMana(effect))));
  }

  private static boolean restoresMana(SpellData.T4cEffect effect) {
    return effect.getParameters().stream()
        .anyMatch(param -> param.getParamId() == 2 && "MANA".equalsIgnoreCase(param.getExpression()));
  }

  /** Potion of Fury is the one authored buff consumable currently wired through the online item path. */
  private static boolean isSupportedBuff(SpellData spell) {
    var effects = spell.getT4cEffects();
    return effects != null && !effects.isEmpty()
        && effects.stream().allMatch(effect -> effect != null && effect.getEffectType() == 2
            && BuffAttributes.MODELLED.contains(BuffAttributes.normalize(effect.getParameters().stream()
                .filter(param -> param.getParamId() == 2).map(SpellData.T4cEffect.EffectParam::getExpression)
                .findFirst().orElse(null))));
  }

  static SpellContent.Spell export(SpellData spell, SpellContent.Use use) {
    List<SpellContent.Effect> effects = (spell.getT4cEffects() == null ? List.<SpellData.T4cEffect>of() : spell.getT4cEffects())
        .stream()
        .map(effect -> new SpellContent.Effect(effect.getEffectType(),
            effect.getParameters().stream()
                .map(param -> new SpellContent.Param(param.getParamId(), param.getExpression())).toList()))
        .toList();
    return new SpellContent.Spell(spell.getKey(), spell.getSpellId(), use, spell.getTargetType(),
        spell.isAttack(), SpellAreaTargeting.isSelfCenteredAttack(spell), spell.isPvp(),
        spell.isLineOfSight(), orZero(spell.getManaCost()), spell.getSuccessRate(),
        orZero(spell.getMentalExhaustion()), orZero(spell.getPhysicalExhaustion()),
        orZero(spell.getAttackExhaustion()), spell.getCooldownSeconds(), effects);
  }

  private static String orZero(String formula) {
    return formula == null ? "0" : formula;
  }

  public static String render(SpellContent content) {
    return OnlineContentJson.render(content);
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Expected output JSON path");
    var path = Path.of(args[0]);
    if (path.getParent() != null) Files.createDirectories(path.getParent());
    Files.writeString(path, render(snapshot()));
    System.out.println("Exported castable, item and grant-only spell definitions; buffs and other effects are not executable online.");
  }
}
