package com.perso.T4C.tools;

import com.perso.T4C.online.model.SpellContent;
import com.perso.T4C.online.model.SpellContentV2;
import com.perso.T4C.online.model.SpellContentV2.Kind;
import com.perso.T4C.item.ItemRegistry;
import com.perso.T4C.item.json.ItemJsonLoader;
import com.perso.T4C.spell.SpellAreaTargeting;
import com.perso.T4C.spell.SpellData;
import com.perso.T4C.spell.SpellRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Build-time bridge that exports player spells plus supported item-only buff spells (spells-v2)
 * with the engine classification and the numbers the online spell engine reads. spells-v1 remains
 * the live cast allowlist; its item entries support restorative consumables and modeled buff potions.
 */
public final class OnlineSpellContentV2Exporter {
  private static final Set<String> ITEM_BUFFS = Set.of(
      "item.potion_of_fury", "item.potion_of_nimbleness", "item.hyper_potion_of_nimbleness");

  private OnlineSpellContentV2Exporter() {}

  public static SpellContentV2 snapshot() {
    var spells = new TreeMap<String, SpellContentV2.Spell>();
    for (SpellData spell : SpellRegistry.playerCastableSpells()) {
      spells.put(spell.getKey(), export(spell));
    }
    // These item buffs are not player-castable, but the socket item path resolves their authored
    // effects through the same v2 buff engine and persistent buff state as a learned buff spell.
    ItemRegistry.resetAdditionalDefinitions();
    ItemJsonLoader.loadAndRegister();
    for (var itemKey : ITEM_BUFFS) {
      var item = ItemRegistry.findByKey(itemKey);
      if (item == null) continue;
      for (var itemSpell : item.getSpells()) {
        var spell = SpellRegistry.findById(itemSpell.getSpellId());
        if (spell != null) spells.put(spell.getKey(), export(spell));
      }
    }
    return new SpellContentV2(SpellContentV2.VERSION, spells);
  }

  static SpellContentV2.Spell export(SpellData spell) {
    Kind kind = classify(spell);
    int stage = stage(spell, kind);
    List<SpellContent.Effect> effects = spell.getT4cEffects().stream()
        .map(effect -> new SpellContent.Effect(effect.getEffectType(),
            effect.getParameters().stream()
                .map(param -> new SpellContent.Param(param.getParamId(), param.getExpression())).toList()))
        .toList();
    return new SpellContentV2.Spell(spell.getKey(), spell.getSpellId(), kind, stage, "stage-" + stage,
        spell.getTargetType(), spell.isAttack(), SpellAreaTargeting.isSelfCenteredAttack(spell), spell.isPvp(),
        spell.isLineOfSight(), orZero(spell.getManaCost()), orZero(spell.getSuccessRate()),
        orZero(spell.getMentalExhaustion()), orZero(spell.getPhysicalExhaustion()),
        orZero(spell.getAttackExhaustion()), spell.getCooldownSeconds(),
        spell.getRadius(), spell.getElement(), spell.getAttackType(), spell.getMinDamage(), spell.getMaxDamage(),
        spell.getDuration() == null ? "0" : spell.getDuration(),
        spell.getFrequency() == null ? "0" : spell.getFrequency(),
        new SpellContentV2.Learning(spell.getMinLevel(), spell.getMinInt(), spell.getMinWis()),
        new SpellContentV2.Visual(spell.getProjectileSpell(), spell.getImpactSpell(), spell.getSound(),
            spell.getSoundImpact(), spell.getVisualEffect(), spell.getVisualEffectTarget()),
        effects, dispelTargets(spell));
  }

  /** Id to key for every spell a dispel effect names that single-player can resolve. */
  private static java.util.Map<Integer, String> dispelTargets(SpellData spell) {
    var targets = new TreeMap<Integer, String>();
    for (var effect : spell.getT4cEffects()) {
      if (effect == null || effect.getEffectType() != 13) continue;
      String expression = param(effect, 1);
      if (expression == null) continue;
      try {
        int id = Integer.parseInt(expression.trim());
        var target = SpellRegistry.findById(id);
        if (target != null) targets.put(id, target.getKey());
      } catch (NumberFormatException computed) {
        throw new IllegalArgumentException("Dispel target of " + spell.getKey() + " is not a literal id");
      }
    }
    return targets;
  }

  private static String orZero(String formula) {
    return formula == null ? "0" : formula;
  }

  /** The engine stage that first enables a spell kind (SPELL_ENGINE_DESIGN section 6). */
  static int stage(SpellData spell, Kind kind) {
    return switch (kind) {
      case NUKE, DRAIN, HEAL -> 1;
      case BUFF, DEBUFF, DETECTION, DISPEL -> 2;
      case AREA_DAMAGE, AREA_HEAL, SHIELD, GATEWAY -> 3;
      // A damage-over-time spell is only as simple as its tick. Poison, Poison Arrow and Plague link tick
      // spells that do not exist (single-player installs no timer for them), so they are one hit. Entangle
      // links real ones that slow and weaken the creature, which needs creature movement and defence state
      // the server does not model yet: it stays at stage 4.
      case DOT -> linksExistingSpell(spell) ? 4 : 3;
      case PORTAL, RECALL -> 4;
      case SPECIAL -> spell.getKey().equals("spell.renew_armor") ? 2 : 4;
    };
  }

  private static boolean linksExistingSpell(SpellData spell) {
    for (var effect : spell.getT4cEffects()) {
      if (effect == null || effect.getEffectType() != 9) continue;
      String expression = param(effect, 1);
      if (expression == null) continue;
      try {
        if (SpellRegistry.findById(Integer.parseInt(expression.trim())) != null) return true;
      } catch (NumberFormatException computed) {
        return true;
      }
    }
    return false;
  }

  /** Classifies a player spell by what its effects and target actually do. */
  static Kind classify(SpellData spell) {
    String key = spell.getKey();
    if (key.equals("spell.renew_armor") || key.equals("spell.tame_beast")) return Kind.SPECIAL;
    var effects = spell.getT4cEffects();
    if (has(effects, 11)) return Kind.RECALL;
    if (has(effects, 6)) return Kind.PORTAL;
    if (has(effects, 10)) return Kind.DRAIN;
    if (has(effects, 15) || has(effects, 16) || has(effects, 17)) return Kind.DETECTION;
    if (has(effects, 13)) return Kind.DISPEL;
    if (has(effects, 9)) {
      boolean onHit = effects.stream().anyMatch(e -> e.getEffectType() == 9
          && "OnHit".equalsIgnoreCase(param(e, 2)));
      if (onHit) return Kind.SHIELD;
      // Gateways hook the caster itself (target type 5); damage over time lands on a target.
      return spell.getTargetType() == 5 && !spell.isAttack() ? Kind.GATEWAY : Kind.DOT;
    }
    if (has(effects, 2) && !has(effects, 1)) return hostile(spell) ? Kind.DEBUFF : Kind.BUFF;
    if (has(effects, 1)) {
      if (!spell.isAttack()) return spell.getTargetType() == 15 ? Kind.AREA_HEAL : Kind.HEAL;
      return spell.getTargetType() == 11 ? Kind.NUKE : Kind.AREA_DAMAGE;
    }
    throw new IllegalArgumentException("Unclassified spell " + key);
  }

  private static boolean hostile(SpellData spell) {
    return spell.getTargetType() == 11 || spell.isAttack();
  }

  private static boolean has(List<SpellData.T4cEffect> effects, int type) {
    return effects.stream().anyMatch(effect -> effect != null && effect.getEffectType() == type);
  }

  private static String param(SpellData.T4cEffect effect, int id) {
    return effect.getParameters().stream().filter(p -> p.getParamId() == id)
        .map(SpellData.T4cEffect.EffectParam::getExpression).findFirst().orElse(null);
  }

  public static String render(SpellContentV2 content) {
    return OnlineContentJson.render(content);
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("Expected output JSON path");
    var path = Path.of(args[0]);
    if (path.getParent() != null) Files.createDirectories(path.getParent());
    Files.writeString(path, render(snapshot()));
    System.out.println("Exported all player spells with engine classification (spells-v2).");
  }
}
