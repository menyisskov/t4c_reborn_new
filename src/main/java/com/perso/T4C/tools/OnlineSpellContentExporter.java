package com.perso.T4C.tools;

import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.spell.SpellAreaTargeting;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

/**
 * Build-time bridge that exports only the spells the online server may run or grant: the castable
 * self heal, grant-only quest spells, and plain-heal spells carried by consumable items.
 */
public final class OnlineSpellContentExporter {
  static final String HEAL_LIGHT = "spell.heal_light";
  static final String RENEW_ARMOR = "spell.renew_armor";

  private OnlineSpellContentExporter() {}

  public static SpellContent snapshot() {
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
    var spells = new TreeMap<String, SpellContent.Spell>();
    spells.put(HEAL_LIGHT, export(required(HEAL_LIGHT), SpellContent.Use.CAST));
    spells.put(RENEW_ARMOR, export(required(RENEW_ARMOR), SpellContent.Use.GRANT_ONLY));
    // In-scope potions are consumables: one charge, no equipment slot, carrying a spell.
    for (var item : ItemRegistry.allByKey().values()) {
      if (item.getBodyPart() != null || item.getNbCharges() != 1 || item.getSpells().isEmpty()) continue;
      for (var itemSpell : item.getSpells()) {
        // Unresolved or non-heal consumable spells are out of online scope and fail closed by omission.
        var spell = SpellRegistry.findById(itemSpell.getSpellId());
        if (spell != null && isPlainHeal(spell)) spells.putIfAbsent(spell.getKey(), export(spell, SpellContent.Use.ITEM));
      }
    }
    return new SpellContent(SpellContent.VERSION, spells);
  }

  private static SpellData required(String key) {
    var spell = SpellRegistry.findByName(key);
    if (spell == null) throw new IllegalArgumentException("Missing authored spell " + key);
    return spell;
  }

  private static boolean isPlainHeal(SpellData spell) {
    var effects = spell.getT4cEffects();
    return effects != null && !effects.isEmpty()
        && effects.stream().allMatch(effect -> effect != null && effect.getEffectType() == 1);
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
